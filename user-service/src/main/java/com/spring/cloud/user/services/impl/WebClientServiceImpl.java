package com.spring.cloud.user.services.impl;

import com.spring.cloud.user.models.Bucket;
import com.spring.cloud.user.services.WebClientService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

@Slf4j
@Service
public class WebClientServiceImpl implements WebClientService {

    private static final String API_MIME_TYPE = "application/json";
    private static final String API_BASE_URL = "http://localhost:8081";
    private static final String USER_AGENT = "User Service";

    private WebClient webClient;

    public WebClientServiceImpl() {
        this.webClient = WebClient.builder()
                .baseUrl(API_BASE_URL)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, API_MIME_TYPE)
                .defaultHeader(HttpHeaders.USER_AGENT, USER_AGENT)
                .build();
    }

    @Override
    public Flux<Bucket> getAllBuckets() {
        return webClient.get()
                .uri("/buckets/stream/delay")
                .exchange()
                .flatMapMany(clientResponse -> clientResponse.bodyToFlux(Bucket.class));
    }
}
