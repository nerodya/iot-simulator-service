package ru.rdblkv.iotsimulatorservice.config;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import ru.rdblkv.iotsimulatorservice.model.ActivityProfile;

import java.time.LocalTime;

@Data
@Component
@ConfigurationProperties(prefix = "simulator.schedule")
public class SimulatorProperties {
    private int minSeconds = 30;
    private int maxSeconds = 60;
    private int tickSeconds = 5;

    private Boost boost = new Boost();

    @Data
    public static class Boost {
        private boolean enabled = false;

        // формат в YAML: "19:00"
        private LocalTime start = LocalTime.of(19, 0);
        private LocalTime end = LocalTime.of(21, 0);

        private ActivityProfile profile = ActivityProfile.HIGH_ACTIVITY;

        // например "Europe/Moscow". Если null/пусто — берём systemDefault()
        private String zone;
    }
}