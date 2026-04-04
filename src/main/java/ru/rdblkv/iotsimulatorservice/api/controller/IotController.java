package ru.rdblkv.iotsimulatorservice.api.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import ru.rdblkv.iotsimulatorservice.dto.StatusResponse;
import ru.rdblkv.iotsimulatorservice.model.SimulatorDevice;
import ru.rdblkv.iotsimulatorservice.service.IotGenerationService;
import ru.rdblkv.iotsimulatorservice.service.SimulatorDeviceService;

import java.util.UUID;

@RestController
@RequestMapping("/iot")
@RequiredArgsConstructor
public class IotController {

    private final SimulatorDeviceService service;
    private final IotGenerationService generationService;

    @PostMapping("/connect")
    public Mono<SimulatorDevice> connect(@RequestHeader("X-User-Id") String userId) {
        return service.connect(UUID.fromString(userId));
    }

    @PostMapping("/disconnect")
    public Mono<Void> disconnect(@RequestHeader("X-User-Id") String userId) {
        return service.disconnect(UUID.fromString(userId));
    }

    @GetMapping("/status")
    public Mono<StatusResponse> status(@RequestHeader("X-User-Id") String userId) {
        return service.status(UUID.fromString(userId));
    }

    @GetMapping("/device")
    public Mono<SimulatorDevice> device(@RequestHeader("X-User-Id") String userId) {
        return service.device(UUID.fromString(userId));
    }

    @PostMapping("/generate-once")
    public Mono<Void> generateOnce(@RequestHeader("X-User-Id") String userId) {
        return generationService.generateOnce(UUID.fromString(userId));
    }
}