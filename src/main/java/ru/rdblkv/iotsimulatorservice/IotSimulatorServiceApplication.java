package ru.rdblkv.iotsimulatorservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class IotSimulatorServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(IotSimulatorServiceApplication.class, args);
    }

}
