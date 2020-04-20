package com.spring.cloud.user.controllers;

import com.spring.cloud.user.exceptions.UserServiceException;
import com.spring.cloud.user.models.Bucket;
import com.spring.cloud.user.services.WebClientService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@Slf4j
@RestController
public class UserController {

    @Autowired
    private Environment env;
    @Autowired
    private WebClientService webClientService;

    @GetMapping("/")
    public String home() {
        String home = "User-Service running at port: " + env.getProperty("local.server.port");
        log.info(home);
        return home;
    }

    @GetMapping(value = "/buckets", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Bucket> getAllBuckets() {
        return webClientService.getAllBuckets();
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<UserServiceException> handleWebClientResponseException(DataAccessException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new UserServiceException("A Bucket with the same title already exists"));
    }
}
