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
import java.time.LocalTime;
import java.time.ZoneId;
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

                    ActivityProfile baseProfile = ActivityProfile.valueOf(device.getActivityProfile());
                    ActivityProfile effectiveProfile = resolveEffectiveProfile(baseProfile);

                    var payload = generator.generate(device.getDeviceId(), effectiveProfile);

                    return client.send(device.getUserId(), payload)
                            .then(Mono.defer(() -> {
                                device.setLastSentAt(payload.timestamp());
                                nextDelaySec.put(device.getUserId(), randomDelay());
                                return repo.save(device);
                            }))
                            .doOnSuccess(v -> log.info("Sent data userId={} deviceId={} profile={}",
                                    device.getUserId(), device.getDeviceId(), effectiveProfile))
                            .then();
                })
                .onErrorContinue((e, obj) -> log.warn("Scheduler error: {}", e.getMessage()))
                .subscribe();
    }

    private int randomDelay() {
        return ThreadLocalRandom.current().nextInt(props.getMinSeconds(), props.getMaxSeconds() + 1);
    }

    private ActivityProfile resolveEffectiveProfile(ActivityProfile baseProfile) {
        var boost = props.getBoost();
        if (boost == null || !boost.isEnabled()) return baseProfile;

        ZoneId zone = (boost.getZone() == null || boost.getZone().isBlank())
                ? ZoneId.systemDefault()
                : ZoneId.of(boost.getZone());

        LocalTime now = LocalTime.now(zone);
        if (isInWindow(now, boost.getStart(), boost.getEnd())) {
            return boost.getProfile() != null ? boost.getProfile() : ActivityProfile.HIGH_ACTIVITY;
        }
        return baseProfile;
    }

    /**
     * - start <= end: обычное (19:00-21:00)
     * - start > end: через полночь (22:00-02:00)
     */
    private boolean isInWindow(LocalTime t, LocalTime start, LocalTime end) {
        if (start == null || end == null) return false;
        if (start.equals(end)) return true; // весь день
        if (start.isBefore(end)) {
            return !t.isBefore(start) && t.isBefore(end);
        }
        // через полночь
        return !t.isBefore(start) || t.isBefore(end);
    }
}