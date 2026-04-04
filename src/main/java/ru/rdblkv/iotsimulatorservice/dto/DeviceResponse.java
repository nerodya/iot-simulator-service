package ru.rdblkv.iotsimulatorservice.dto;

import lombok.Builder;
import ru.rdblkv.iotsimulatorservice.model.ActivityProfile;
import ru.rdblkv.iotsimulatorservice.model.DeviceStatus;

import java.time.Instant;
import java.util.UUID;

@Builder
public record DeviceResponse(
        String id,
        UUID userId,
        String deviceId,
        DeviceStatus status,
        ActivityProfile activityProfile,
        Instant connectedAt,
        Instant lastSentAt
) {}