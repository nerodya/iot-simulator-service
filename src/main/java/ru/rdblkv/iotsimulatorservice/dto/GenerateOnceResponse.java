package ru.rdblkv.iotsimulatorservice.dto;

import lombok.Builder;

import java.time.Instant;

@Builder
public record GenerateOnceResponse(
        String deviceId,
        boolean sent,
        Instant timestamp
) {}