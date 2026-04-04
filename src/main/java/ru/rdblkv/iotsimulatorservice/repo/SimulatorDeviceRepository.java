package ru.rdblkv.iotsimulatorservice.repo;


import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.rdblkv.iotsimulatorservice.model.SimulatorDevice;

import java.util.UUID;

public interface SimulatorDeviceRepository extends ReactiveCrudRepository<SimulatorDevice, String> {
    Mono<SimulatorDevice> findByUserId(UUID userId);
    Flux<SimulatorDevice> findAllByStatus(String status); // "CONNECTED"
}