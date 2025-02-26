package com.yourfavspot.location.repository;

import com.yourfavspot.location.model.LocationModel;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface LocationRepository extends ReactiveMongoRepository<LocationModel, String> {
    Flux<LocationModel> findAllByUserIdOrSharedWithContains(Integer userId);
    /*
    //nie ma potrzeby nadpisywania tych metod bo są one domyslnie generowane.e
    Mono<LocationModel> findById(String id);
    Flux<LocationModel> findAll();
    Mono<LocationModel> save(LocationModel entity);
    Mono<Void> deleteById(String id);
     */

}
