package com.spring.cloud.gallery.repositories;

import com.spring.cloud.gallery.models.Bucket;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BucketRepository extends ReactiveMongoRepository<Bucket, String> {
}
