package com.spring.cloud.user.services;

import com.spring.cloud.user.models.Bucket;
import reactor.core.publisher.Flux;

public interface WebClientService {
    Flux<Bucket> getAllBuckets();
}
