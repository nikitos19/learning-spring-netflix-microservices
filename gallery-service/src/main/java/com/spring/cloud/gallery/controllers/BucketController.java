package com.spring.cloud.gallery.controllers;

import com.mongodb.DuplicateKeyException;
import com.spring.cloud.gallery.exceptions.BucketNotFoundException;
import com.spring.cloud.gallery.models.Bucket;
import com.spring.cloud.gallery.payload.ErrorResponse;
import com.spring.cloud.gallery.repositories.BucketRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.Valid;
import java.time.Duration;

@Slf4j
@RestController
public class BucketController {

    @Autowired
    private Environment env;

    @Autowired
    private BucketRepository bucketRepository;

    @GetMapping("/")
    public String home() {
        String home = "Gallery-Service running at port: " + env.getProperty("local.server.port");
        log.info(home);
        return home;
    }

    @GetMapping("/buckets")
    public Flux<Bucket> getAllBuckets() {
        return bucketRepository.findAll();
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/buckets")
    public Mono<Bucket> createBucket(@Valid @RequestBody Bucket bucket) {
        return bucketRepository.save(bucket);
    }

    @GetMapping("/buckets/{id}")
    public Mono<ResponseEntity<Bucket>> getBucketById(@PathVariable(value = "id") String bucketId) {
        return bucketRepository.findById(bucketId)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PutMapping("/buckets/{id}")
    public Mono<ResponseEntity<Bucket>> updateBucket(@PathVariable(value = "id") String bucketId,
                                                     @Valid @RequestBody Bucket bucket) {
        return bucketRepository.findById(bucketId)
                .flatMap(existingBucket -> {
                    existingBucket.setDescription(bucket.getDescription());
                    existingBucket.setImageLink(bucket.getImageLink());
                    return bucketRepository.save(existingBucket);
                })
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/buckets/{id}")
    public Mono<ResponseEntity<Void>> deleteBucket(@PathVariable(value = "id") String bucketId) {
        return bucketRepository.findById(bucketId)
                .flatMap(existingBucket ->
                        bucketRepository.delete(existingBucket)
                                .then(Mono.just(new ResponseEntity<Void>(HttpStatus.OK)))
                )
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/buckets")
    public Mono<Void> deleteAllBuckets() {
        return bucketRepository.deleteAll();
    }

    // Buckets are Sent to the client as Server Sent Events
    @GetMapping(value = "/buckets/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Bucket> streamAllBuckets() {
        return bucketRepository.findAll();
    }

    // Get default value every 1 second
    @GetMapping(value = "/buckets/stream/default", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Bucket> emitBuckets() {
        return Flux.interval(Duration.ofSeconds(1))
                .map(val -> new Bucket("" + val, "Python", "default theme", 0, "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcS2f2NovvIAZjv9jGeSmzXnWnkiIXZX2VR7i2e-v_V756pWxFSS"));
    }

    // Get all Bucket from the database (every 1 second you will receive 1 record from the DB)
    @GetMapping(value = "/buckets/stream/delay", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Bucket> streamAllBucketsDelay() {
        log.info("Get data from database (WebClient on User-Service side)");
        return bucketRepository.findAll().delayElements(Duration.ofSeconds(2));
    }

    @ExceptionHandler(DuplicateKeyException.class)
    public ResponseEntity handleDuplicateKeyException(DuplicateKeyException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse("A Bucket with the same title already exists"));
    }

    @ExceptionHandler(BucketNotFoundException.class)
    public ResponseEntity handleBucketNotFoundException(BucketNotFoundException ex) {
        return ResponseEntity.notFound().build();
    }
}
