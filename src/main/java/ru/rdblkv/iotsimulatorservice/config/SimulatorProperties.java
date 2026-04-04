package ru.rdblkv.iotsimulatorservice.config;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "simulator.schedule")
public class SimulatorProperties {
    private int minSeconds = 30;
    private int maxSeconds = 60;
    private int tickSeconds = 5;
}