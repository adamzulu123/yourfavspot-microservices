package com.yourfavspot.location.controller;

import com.yourfavspot.location.model.LocationModel;
import com.yourfavspot.location.service.LocationService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("api/v1/location")
public class LocationController {
    private final LocationService locationService;

    @PostMapping("/create")
    public Mono<ResponseEntity<LocationModel>> createLocation(@RequestBody LocationModel locationModel){

        //@AuthenticationPrincipal Jwt jwt
        //String userId = jwt.getClaim("sub");  // No parsing needed, just use the string directly
        //locationModel.setUserId(userId);

        return locationService.createLocation(locationModel)
                //tworzymy status 201 (OK) i w cele zwracamy zapisany obiekt location
                .map(location -> ResponseEntity.status(HttpStatus.CREATED).body(location))
                .onErrorResume(IllegalArgumentException.class, e ->
                        Mono.just(ResponseEntity.badRequest().body(null))
                );
    }

    @GetMapping("/allLocations")
    public Flux<LocationModel> getAllLocations() {
        return locationService.getAllLocations();
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<LocationModel>> getLocation(@PathVariable("id") String id) {
        return locationService.getLocation(id)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.status(HttpStatus.FORBIDDEN).build());
    }

    @GetMapping("/myLocations")
    public Flux<LocationModel> getUserLocations(@RequestHeader("userId") Integer userId){
        return locationService.getAllUserLocations(userId);
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deleteLocation(@PathVariable("id") String id) {
        return locationService.deleteLocation(id)
                //tutaj robimy przez flatMap() a nie za pomocą map, bo zmieniamy typ to znaczy odpowiedz jakś dostaliśmy
                //od serwisu, czyli Mono<Boolean> musimy zamienic na ResposeEntity<Void> , a jak wiemy map()
                //pozwala tylko na zmianę wewnątrz obiektu Mono (map() pozwala tylko na zmianę wartości Mono,
                //ale nie wpływa na jego strukture
                .flatMap(deleted -> deleted
                        ? Mono.just(ResponseEntity.ok().build())
                        : Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).build()));

    }

    //prosty update nazwy, kategorii i opisu miejscówki
    @PutMapping("/update/{id}")
    public Mono<ResponseEntity<LocationModel>> updateLocation(@PathVariable("id") String id,
                                                              @RequestBody LocationModel locationModel) {
        return locationService.updateLocation(id, locationModel)
                .flatMap(updatedLocation -> updatedLocation != null
                        ? Mono.just(ResponseEntity.ok(updatedLocation))
                        : Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).build()));
    }


    //todo:
    // 1) zmiana danych istniejącej lokalizacji
    // 2) udostępnianie lokalizacji innym użytkownikom i usuwanie udostępnienia
    // 3) wyszukiwanie lokalizacji



}
