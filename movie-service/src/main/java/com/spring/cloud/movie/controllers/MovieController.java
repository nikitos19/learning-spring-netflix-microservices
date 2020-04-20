package com.spring.cloud.movie.controllers;

import com.mongodb.DuplicateKeyException;
import com.spring.cloud.movie.exceptions.MovieNotFoundException;
import com.spring.cloud.movie.models.Movie;
import com.spring.cloud.movie.payload.ErrorResponse;
import com.spring.cloud.movie.repositories.MovieRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.Valid;

@Slf4j
@RestController
public class MovieController {

    @Autowired
    private Environment env;
    @Autowired
    private MovieRepository movieRepository;

    @GetMapping("/")
    public String home() {
        return "Movie-Service running at port: " + env.getProperty("local.server.port");
    }

    @GetMapping("/movies")
    public Flux<Movie> getAllMovies() {
        return movieRepository.findAll();
    }

    @GetMapping("/movies/{id}")
    public Mono<ResponseEntity<Movie>> getMovieById(@PathVariable String id) {
        return movieRepository.findById(id)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PostMapping("/movies")
    public Mono<Movie> createMovie(@Valid @RequestBody Movie movie) {
        return movieRepository.save(movie);
    }

    @PutMapping("/movies/{id}")
    public Mono<ResponseEntity<Movie>> updateMovie(@PathVariable(value = "id") String movieId,
                                                   @Valid @RequestBody Movie movie) {
        return movieRepository.findById(movieId)
                .flatMap(existingMovie -> {
                    existingMovie.setGenre(movie.getGenre());
                    existingMovie.setImageLink(movie.getImageLink());
                    return movieRepository.save(existingMovie);
                })
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/movies/{id}")
    public Mono<ResponseEntity<Void>> deleteMovie(@PathVariable(value = "id") String movieId) {

        return movieRepository.findById(movieId)
                .flatMap(existingMovie ->
                        movieRepository.delete(existingMovie)
                                .then(Mono.just(new ResponseEntity<Void>(HttpStatus.OK)))
                )
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/movies")
    public Mono<Void> deleteAllMovies(){
        return movieRepository.deleteAll();
    }

    @ExceptionHandler(DuplicateKeyException.class)
    public ResponseEntity handleDuplicateKeyException(DuplicateKeyException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse("A movie with the same title already exists"));
    }

    @ExceptionHandler(MovieNotFoundException.class)
    public ResponseEntity handleBucketNotFoundException(MovieNotFoundException ex) {
        return ResponseEntity.notFound().build();
    }
}
