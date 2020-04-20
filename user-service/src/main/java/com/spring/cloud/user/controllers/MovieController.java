package com.spring.cloud.user.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
public class MovieController {

    @Autowired
    private RestTemplate restTemplate;

    @GetMapping("/movies")
    public String getAllMovies() {
        return restTemplate.getForObject("http://movie-service/movies", String.class);
    }

    @GetMapping("/movies/info")
    public String getInfoFromMovieService() {
        return restTemplate.getForObject("http://movie-service/", String.class);
    }
}
