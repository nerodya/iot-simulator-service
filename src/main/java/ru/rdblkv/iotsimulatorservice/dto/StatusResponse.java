package ru.rdblkv.iotsimulatorservice.dto;

import lombok.Builder;

import java.time.Instant;

@Builder
public record StatusResponse(
        boolean connected,
        String deviceId,
        Instant connectedAt,
        Instant lastSentAt
) {}