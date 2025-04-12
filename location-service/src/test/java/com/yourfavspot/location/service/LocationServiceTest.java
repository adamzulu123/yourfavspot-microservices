package com.yourfavspot.location.service;


import com.yourfavspot.location.model.LocationModel;
import com.yourfavspot.location.model.LocationVisibility;
import com.yourfavspot.location.repository.LocationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatcher;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;


/**
 * when().thenReturn() --> konfiguracja zachowania mocka. when().thenThrow() --> symulujemy wyrzucanie wyjątku
 * StepVerifier --> służy do testowania strumieni Mono i Flux
 * expectNext() == sprawdza czy otrzymany obiekt jest równy oczekiwanemu
 * expectNextMatches() === weryfikaja obiektu z ->
 * expectNextCount() === sprawdza liczbę otrzymanych obiektów
 * .expectError(IllegalArgumentException.class) == sprawdza czy strumień zakończył się błędem
 * .verifyComplete() -> sprawdza czy strumień się zamknął
 * verify() -> sprawdza czy metoda mocka została wywołana
 *
 */



@ExtendWith(MockitoExtension.class)
public class LocationServiceTest {

    @Mock
    private LocationRepository locationRepository;

    @InjectMocks
    private LocationService locationService;

    @Test
    public void createLocation_success() {
        LocationModel location = new LocationModel();
        location.setName("Test Spot");
        location.setLocation(new double[]{50.0, 20.0});
        location.setUserId("123");
        location.setVisibility(LocationVisibility.PRIVATE);

        LocationModel savedLocation = new LocationModel();
        savedLocation.setId("test-id");

        when(locationRepository.save(location)).thenReturn(Mono.just(savedLocation));

        StepVerifier.create(locationService.createLocation(location))
                .expectNextMatches(l -> l.getId().matches("test-id"))
                .verifyComplete();

    }

    @Test
    public void createLocation_failure() {
        LocationModel location = new LocationModel();
        location.setName("Test Spot");
        location.setLocation(new double[]{150.0, 20.0});
        location.setUserId("123");
        location.setVisibility(LocationVisibility.PRIVATE);

        StepVerifier.create(locationService.createLocation(location))
                .expectErrorMatches(throwable -> throwable instanceof IllegalArgumentException)
                .verify();
    }

    @Test
    public void getLocation_success() {
        LocationModel location = new LocationModel();
        location.setId("test-id");
        location.setName("Test Spot");

        when(locationRepository.findById("test-id")).thenReturn(Mono.just(location));

        StepVerifier.create(locationService.getLocation(location.getId()))
                .expectNextMatches(l -> l.getId().matches("test-id"))
                .verifyComplete();
    }

    @Test
    public void getLocation_failure() {

        when(locationRepository.findById("test-id")).thenReturn(Mono.empty());

        StepVerifier.create(locationService.getLocation("test-id"))
                .expectNextCount(0)
                .verifyComplete();

    }

    @Test
    public void updateLocation_success() {
        LocationModel location = new LocationModel();
        location.setId("test-id");
        location.setName("Test Spot");
        location.setLocation(new double[]{50.0, 20.0});
        location.setUserId("123");
        location.setVisibility(LocationVisibility.PRIVATE);

        LocationModel updatedLocation = new LocationModel();
        updatedLocation.setName("Updated Place");
        updatedLocation.setDescription("New description");
        updatedLocation.setLocation(new double[]{51.0, 21.0});

        when(locationRepository.findById("test-id")).thenReturn(Mono.just(location));
        when(locationRepository.save(location)).thenAnswer(i -> {
            LocationModel saved = i.getArgument(0);
            return Mono.just(saved);
        });

        StepVerifier.create(locationService.updateLocation(location.getId(), updatedLocation))
                .assertNext(l -> {
                    assertEquals("test-id", l.getId());
                    assertEquals("Updated Place", l.getName());
                    assertEquals("New description", l.getDescription());
                    assertArrayEquals(new double[]{51.0, 21.0}, l.getLocation(), 0.001);
                    assertNotNull(l.getUpdatedAt());
                })
                .verifyComplete();

    }


}
