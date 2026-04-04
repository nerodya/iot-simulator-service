package ru.rdblkv.iotsimulatorservice.dto;


import lombok.Builder;
import ru.rdblkv.iotsimulatorservice.model.ActivityProfile;

import java.time.Instant;

@Builder
public record ConnectResponse(
        String deviceId,
        String status,
        ActivityProfile activityProfile,
        Instant connectedAt
) {}