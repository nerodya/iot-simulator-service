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

        int stepsMin;
        int stepsMax;
        int hrMin;
        int hrMax;
        double calMin;
        double calMax;

        switch (profile) {
            case LOW_ACTIVITY -> {
                stepsMin = 10;  stepsMax = 120;
                hrMin = 60; hrMax = 95;
                calMin = 10; calMax = 80;
            }
            case MEDIUM_ACTIVITY -> {
                stepsMin = 80;  stepsMax = 400;
                hrMin = 65; hrMax = 120;
                calMin = 40; calMax = 250;
            }
            case HIGH_ACTIVITY -> {
                stepsMin = 200; stepsMax = 900;
                hrMin = 75; hrMax = 140;
                calMin = 120; calMax = 600;
            }
            default -> throw new IllegalStateException("Unexpected profile: " + profile);
        }

        boolean restEvent = r.nextDouble() < restProbability(profile);
        boolean spikeEvent = !restEvent && r.nextDouble() < spikeProbability(profile);

        int steps;
        int heartRate;
        double calories;

        if (restEvent) {
            // почти нет шагов + HR ниже/норм
            steps = r.nextInt(0, 40);
            heartRate = clampInt((int) Math.round(r.nextDouble(55, 85) + r.nextGaussian() * 3), 45, 200);
            calories = clampDouble(r.nextDouble(5, 40) * noiseFactor(r, 0.15), 0, 5000);
        } else if (spikeEvent) {
            // резкий всплеск: шаги/пульс/калории выше обычного профиля
            steps = (int) Math.round(r.nextInt(stepsMin, stepsMax + 1) * r.nextDouble(1.7, 3.5));
            heartRate = clampInt((int) Math.round(r.nextInt(hrMin, hrMax + 1) + r.nextInt(15, 40)), 45, 205);
            calories = clampDouble(r.nextDouble(calMin, calMax) * r.nextDouble(1.5, 3.0) * noiseFactor(r, 0.25), 0, 20000);
        } else {
            // обычный режим, но добавим "шум" чтобы соседние значения сильнее гуляли
            steps = (int) Math.round(r.nextInt(stepsMin, stepsMax + 1) * noiseFactor(r, 0.35));
            heartRate = clampInt((int) Math.round(r.nextInt(hrMin, hrMax + 1) * noiseFactor(r, 0.12)), 45, 200);
            calories = clampDouble(r.nextDouble(calMin, calMax) * noiseFactor(r, 0.30), 0, 20000);
        }

        // давление/SpO2: расширяем разброс + редкие выбросы
        int systolic = clampInt((int) Math.round(r.nextDouble(108, 150) + r.nextGaussian() * 4), 85, 200);
        int diastolic = clampInt((int) Math.round(r.nextDouble(68, 95) + r.nextGaussian() * 3), 50, 130);

        int spo2 = clampInt((int) Math.round(r.nextDouble(94, 100) + r.nextGaussian() * 1.2), 85, 100);
        if (r.nextDouble() < 0.02) { // 2% редкий "плохой" замер
            spo2 = clampInt(spo2 - r.nextInt(3, 9), 85, 100);
        }

        double glucose = round2(r.nextDouble(4.0, 7.5)); // как было [2]

        return HealthDataRequest.builder()
                .deviceId(deviceId)
                .heartRate(heartRate)
                .steps(steps)
                .calories(round2(calories))
                .systolicPressure(systolic)
                .diastolicPressure(diastolic)
                .spo2(spo2)
                .glucose(glucose)
                .source("wearable")
                .timestamp(Instant.now())
                .build();
    }

    private double restProbability(ActivityProfile profile) {
        return switch (profile) {
            case LOW_ACTIVITY -> 0.10;
            case MEDIUM_ACTIVITY -> 0.06;
            case HIGH_ACTIVITY -> 0.03;
        };
    }

    private double spikeProbability(ActivityProfile profile) {
        return switch (profile) {
            case LOW_ACTIVITY -> 0.03;
            case MEDIUM_ACTIVITY -> 0.05;
            case HIGH_ACTIVITY -> 0.07;
        };
    }

    /**
     * amplitude=0.30 => примерно диапазон ~[0.70..1.30] с учётом гаусса и обрезки.
     */
    private double noiseFactor(ThreadLocalRandom r, double amplitude) {
        double n = 1.0 + r.nextGaussian() * amplitude;
        // обрежем, чтобы не улетало
        double min = Math.max(0.05, 1.0 - 2.5 * amplitude);
        double max = 1.0 + 2.5 * amplitude;
        return clampDouble(n, min, max);
    }

    private int clampInt(int v, int min, int max) {
        return Math.max(min, Math.min(max, v));
    }

    private double clampDouble(double v, double min, double max) {
        return Math.max(min, Math.min(max, v));
    }

    private double round2(double v) {
        return Math.round(v * 100.0) / 100.0;
    }
}