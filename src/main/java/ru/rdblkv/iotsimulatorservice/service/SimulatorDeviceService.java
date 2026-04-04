package ru.rdblkv.iotsimulatorservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.rdblkv.iotsimulatorservice.dto.StatusResponse;
import ru.rdblkv.iotsimulatorservice.model.DeviceStatus;
import ru.rdblkv.iotsimulatorservice.model.SimulatorDevice;
import ru.rdblkv.iotsimulatorservice.repo.SimulatorDeviceRepository;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class SimulatorDeviceService {

    private final SimulatorDeviceRepository repo;

    public Mono<SimulatorDevice> connect(UUID userId) {
        var now = Instant.now();

        return repo.findByUserId(userId)
                .flatMap(existing -> {
                    if (DeviceStatus.CONNECTED.name().equals(existing.getStatus())) return Mono.just(existing);
                    existing.setStatus(DeviceStatus.CONNECTED.name());
                    existing.setConnectedAt(now);
                    return repo.save(existing);
                })
                .switchIfEmpty(Mono.defer(() -> {
                    var device = SimulatorDevice.builder()
                            .id(UUID.randomUUID().toString())
                            .isNew(true)
                            .userId(userId)
                            .deviceId("sim-" + userId)
                            .status(DeviceStatus.CONNECTED.name())
                            .activityProfile(randomProfile())
                            .connectedAt(now)
                            .lastSentAt(null)
                            .build();
                    return repo.save(device);
                }));
    }

    public Mono<Void> disconnect(UUID userId) {
        return repo.findByUserId(userId)
                .flatMap(d -> {
                    d.setStatus(DeviceStatus.DISCONNECTED.name());
                    return repo.save(d).then();
                })
                .then();
    }

    public Mono<StatusResponse> status(UUID userId) {
        return repo.findByUserId(userId)
                .map(d -> new StatusResponse(
                        DeviceStatus.CONNECTED.name().equals(d.getStatus()),
                        d.getDeviceId(),
                        d.getConnectedAt(),
                        d.getLastSentAt()
                ))
                .defaultIfEmpty(new StatusResponse(false, null, null, null));
    }

    public Mono<SimulatorDevice> device(UUID userId) {
        return repo.findByUserId(userId);
    }

    private String randomProfile() {
        var vals = new String[]{"LOW_ACTIVITY", "MEDIUM_ACTIVITY", "HIGH_ACTIVITY"};
        return vals[ThreadLocalRandom.current().nextInt(vals.length)];
    }
}