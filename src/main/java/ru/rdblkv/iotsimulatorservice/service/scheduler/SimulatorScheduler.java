package ru.rdblkv.iotsimulatorservice.service.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import ru.rdblkv.iotsimulatorservice.api.client.HealthDataClient;
import ru.rdblkv.iotsimulatorservice.config.SimulatorProperties;
import ru.rdblkv.iotsimulatorservice.model.ActivityProfile;
import ru.rdblkv.iotsimulatorservice.repo.SimulatorDeviceRepository;
import ru.rdblkv.iotsimulatorservice.service.HealthDataGenerator;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

@Component
@RequiredArgsConstructor
@Slf4j
public class SimulatorScheduler {

    private final SimulatorDeviceRepository repo;
    private final HealthDataGenerator generator;
    private final HealthDataClient client;
    private final SimulatorProperties props;

    private final Map<UUID, Integer> nextDelaySec = new ConcurrentHashMap<>();

    @Scheduled(fixedDelayString = "${simulator.schedule.tick-seconds:5}000")
    public void tick() {
        var now = Instant.now();

        repo.findAllByStatus("CONNECTED")
                .flatMap(device -> {
                    int delay = nextDelaySec.computeIfAbsent(device.getUserId(), u -> randomDelay());
                    Instant last = device.getLastSentAt();
                    boolean shouldSend = (last == null) || Duration.between(last, now).getSeconds() >= delay;
                    if (!shouldSend) return Mono.empty();

                    var payload = generator.generate(device.getDeviceId(), ActivityProfile.valueOf(device.getActivityProfile()));

                    return client.send(device.getUserId(), payload)
                            .then(Mono.defer(() -> {
                                device.setLastSentAt(payload.timestamp());
                                nextDelaySec.put(device.getUserId(), randomDelay());
                                return repo.save(device);
                            }))
                            .doOnSuccess(v -> log.info("Sent data userId={} deviceId={}", device.getUserId(), device.getDeviceId()))
                            .then();
                })
                .onErrorContinue((e, obj) -> log.warn("Scheduler error: {}", e.getMessage()))
                .subscribe();
    }

    private int randomDelay() {
        return ThreadLocalRandom.current().nextInt(props.getMinSeconds(), props.getMaxSeconds() + 1);
    }
}