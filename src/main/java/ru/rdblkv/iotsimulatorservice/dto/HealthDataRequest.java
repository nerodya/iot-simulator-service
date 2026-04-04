package ru.rdblkv.iotsimulatorservice.dto;

import lombok.Builder;

import java.time.Instant;

@Builder
public record HealthDataRequest(
        String deviceId,
        Integer heartRate,
        Integer steps,
        Double calories,
        Integer systolicPressure,
        Integer diastolicPressure,
        Double glucose,
        Integer spo2,
        String source,
        Instant timestamp
) {}