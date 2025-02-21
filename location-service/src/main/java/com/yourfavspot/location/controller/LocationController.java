package com.yourfavspot.location.controller;

import com.yourfavspot.location.model.LocationModel;
import com.yourfavspot.location.service.LocationService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("api/v1/location")
public class LocationController {
    private final LocationService locationService;

    @PostMapping("/create")
    public Mono<ResponseEntity<LocationModel>> createLocation(@RequestBody LocationModel locationModel) {
        return locationService.createLocation(locationModel)
                //tworzymy status 201 (OK) i w cele zwracamy zapisany obiekt location
                .map(location -> ResponseEntity.status(HttpStatus.CREATED).body(location))
                .onErrorResume(IllegalArgumentException.class, e ->
                        Mono.just(ResponseEntity.badRequest().body(null))
                );
    }

    @GetMapping
    public Flux<LocationModel> getAllLocations() {
        return locationService.getAllLocations();
    }

    @GetMapping("/{id}")
    public Mono<LocationModel> getLocation(@PathVariable String id) {
        return locationService.getLocation(id);
    }



}
