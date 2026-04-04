package ru.rdblkv.iotsimulatorservice.service;


import org.springframework.stereotype.Component;
import ru.rdblkv.iotsimulatorservice.dto.HealthDataRequest;
import ru.rdblkv.iotsimulatorservice.model.ActivityProfile;

import java.time.Instant;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class HealthDataGenerator {

    public HealthDataRequest generate(String deviceId, ActivityProfile profile) {
        var r = ThreadLocalRandom.current();

        int stepsDelta;
        int hrMin;
        int hrMax;
        double calMin;
        double calMax;

        switch (profile) {
            case LOW_ACTIVITY -> {
                stepsDelta = r.nextInt(10, 120);
                hrMin = 60; hrMax = 95;
                calMin = 10; calMax = 80;
            }
            case MEDIUM_ACTIVITY -> {
                stepsDelta = r.nextInt(80, 400);
                hrMin = 65; hrMax = 120;
                calMin = 40; calMax = 250;
            }
            case HIGH_ACTIVITY -> {
                stepsDelta = r.nextInt(200, 900);
                hrMin = 75; hrMax = 140;
                calMin = 120; calMax = 600;
            }
            default -> throw new IllegalStateException("Unexpected profile: " + profile);
        }

        int systolic = r.nextInt(110, 141);
        int diastolic = r.nextInt(70, 91);
        int spo2 = r.nextInt(95, 101);

        double glucose = r.nextDouble(4.0, 7.5); // опционально, но оставим всегда

        return HealthDataRequest.builder()
                .deviceId(deviceId)
                .heartRate(r.nextInt(hrMin, hrMax + 1))
                .steps(stepsDelta)
                .calories(round2(r.nextDouble(calMin, calMax)))
                .systolicPressure(systolic)
                .diastolicPressure(diastolic)
                .spo2(spo2)
                .glucose(round2(glucose))
                .source("wearable")
                .timestamp(Instant.now())
                .build();
    }

    private double round2(double v) {
        return Math.round(v * 100.0) / 100.0;
    }
}