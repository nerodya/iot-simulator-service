package ru.rdblkv.iotsimulatorservice.service;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.rdblkv.iotsimulatorservice.api.client.HealthDataClient;
import ru.rdblkv.iotsimulatorservice.model.ActivityProfile;
import ru.rdblkv.iotsimulatorservice.repo.SimulatorDeviceRepository;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class IotGenerationService {

    private final SimulatorDeviceRepository deviceRepository;
    private final HealthDataGenerator generator;
    private final HealthDataClient healthDataClient;

    /**
     * Генерирует и отправляет одну запись health-данных для текущего пользователя.
     * Используется для тестов (POST /iot/generate-once) [1].
     */
    public Mono<Void> generateOnce(UUID userId) {
        return deviceRepository.findByUserId(userId)
                .switchIfEmpty(Mono.error(new IllegalStateException("Device not connected")))
                .flatMap(device -> {
                    if (!"CONNECTED".equals(device.getStatus())) {
                        return Mono.error(new IllegalStateException("Device is disconnected"));
                    }

                    var payload = generator.generate(device.getDeviceId(), ActivityProfile.valueOf(device.getActivityProfile()));

                    return healthDataClient.send(userId, payload)
                            .then(Mono.defer(() -> {
                                device.setLastSentAt(payload.timestamp() != null ? payload.timestamp() : Instant.now());
                                return deviceRepository.save(device).then();
                            }));
                });
    }
}