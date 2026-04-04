package ru.rdblkv.iotsimulatorservice.api.client;


import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import ru.rdblkv.iotsimulatorservice.dto.HealthDataRequest;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class HealthDataClient {

    private final WebClient.Builder webClientBuilder;

    @Value("${health-data.base-url}")
    private String baseUrl;

    public Mono<Void> send(UUID userId, HealthDataRequest payload) {
        return webClientBuilder.build()
                .post()
                .uri(baseUrl + "/api/health-data")
                .header("X-User-Id", userId.toString())
                .header(HttpHeaders.CONTENT_TYPE, "application/json")
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(Void.class);
    }
}