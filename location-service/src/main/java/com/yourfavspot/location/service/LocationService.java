package com.yourfavspot.location.service;


import com.yourfavspot.location.model.LocationModel;
import com.yourfavspot.location.repository.LocationRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@AllArgsConstructor
@Service //dzięki temu spring tworzy instancję (beana) wartstwy serwisowej, która będzie mogła być wstrzykiwana do komponentów
public class LocationService {

    private LocationRepository locationRepository;

    public Mono<LocationModel> createLocation(LocationModel locationModel) {
        return validateLocation(locationModel)
                .flatMap(locationRepository::save)
                .doOnSuccess(location -> log.info("Location saved with ID: {}", location.getId()))
                .doOnError(e -> log.error("Error saving location: {}", e.getMessage()));
    }

    public Mono<LocationModel> getLocation(String id) {
        return locationRepository.findById(id);
    }

    public Mono<Void> deleteLocation(String id) {
        return locationRepository.deleteById(id);
    }

    public Flux<LocationModel> getAllLocations() {
        return locationRepository.findAll();
    }

    public Mono<Boolean> checkLocationExists(String locationId) {
        return locationRepository.existsById(locationId)
                .doOnSuccess(exists -> log.info("Location {} exists: {}", locationId, exists))
                .doOnError(e -> log.error("Error checking location {}: {}", locationId, e.getMessage()));
    }


    private Mono<LocationModel> validateLocation(LocationModel locationModel) {
        if (locationModel == null) {
            return Mono.error(new IllegalArgumentException("Invalid location model"));
        }
        if(locationModel.getLocation() == null || locationModel.getLocation().length != 2) {
            return Mono.error(new IllegalArgumentException("Coordinates must be exactly 2 values"));
        }

        double latitude = locationModel.getLocation()[0];
        double longitude = locationModel.getLocation()[1];

        if (latitude < -90 || latitude > 90) {
            return Mono.error(new IllegalArgumentException("Latitude must be between -90 and 90 degrees"));
        }
        if (longitude < -180 || longitude > 180) {
            return Mono.error(new IllegalArgumentException("Longitude must be between -180 and 180 degrees"));
        }

        return Mono.just(locationModel);
    }
}
