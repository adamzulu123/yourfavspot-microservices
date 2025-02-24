package com.yourfavspot.user.rabbit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Delivery;
import com.yourfavspot.common.AddLocationResponse;
import com.yourfavspot.common.LocationResponse;
import com.yourfavspot.rabbit.RabbitMQConfig;
import com.yourfavspot.user.Model.FavoriteLocation;
import com.yourfavspot.user.Model.User;
import com.yourfavspot.user.Repository.FavoriteLocationRepository;
import com.yourfavspot.user.Repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.rabbitmq.Receiver;

import java.time.LocalDateTime;

@Slf4j
@Service
@AllArgsConstructor
public class UserRabbitConsumerReactive {
    private final Receiver receiver;
    private final ObjectMapper objectMapper;
    private final UserRepository userRepository;
    private final FavoriteLocationRepository favoriteLocationRepository;

    /*
    @EventListener(ContextRefreshedEvent.class) - służy do nasłuchiwania na konkretne zdarzenie w konteksie aplikacji
    czyli jest to zdarzenie które pozwala na uruchomienie kodu nasłuchiwacza dopiero po załadowaniu całego kontekstu
    aplikacji, czyli po inicjacji wszystkich Bean, dzięki czemu wiemy że kolejka już istnieje.
    Po uruchomieniu aplikacji konsument jest uruchamiany i zaczyna nasłuchiwać na kolejkę RabbitMQ.
     */
    @EventListener(ContextRefreshedEvent.class)
    public void consume(){
        log.info("Kontekst Springa odświeżony, uruchamiam reaktywnego konsumenta dla kolejki: {}", RabbitMQConfig.USER_RESPONSE_QUEUE_REACTOR);
        //receiver.consumeAutoAck() -> konsument jest rejestrowany do nasłuchiwania na kolejce do automatycznego
        //potwierdzania i obierania wiadomosci.
        receiver.consumeAutoAck(RabbitMQConfig.USER_RESPONSE_QUEUE_REACTOR)
                .doOnSubscribe(subscription -> log.info("Subscribed to RabbitMQ queue"))
                .flatMap(message -> {
                    String routingKey = message.getEnvelope().getRoutingKey(); //pobieranie routing key
                    log.info("Received message with routing key: {}", routingKey);
                    //intellj podpowiedział zeby tak ładnie zrobić
                    return switch (routingKey) {
                        case RabbitMQConfig.USER_RESPONSE_ROUTING_KEY_REACTOR -> handleUserResponse(message);
                        case RabbitMQConfig.USER_RESPONSE_ADD_ROUTING_KEY_REACTOR -> handleUserAddResponse(message);
                        default -> {
                            log.warn("Unknown routing key: {}", routingKey);
                            yield Mono.empty();
                        }
                    };

                })
                .doOnError(e -> log.error("Error in reactive consumer", e))
                .subscribe();

    }

    public Mono<Void> handleUserResponse(Delivery message) {
        try {
            LocationResponse locationResponse = objectMapper.readValue(message.getBody(), LocationResponse.class);
            log.info("Received location check response: {}", locationResponse);

            if (locationResponse.exists()) {
                User user = userRepository.findById(locationResponse.userId())
                        .orElseThrow(() -> new RuntimeException("User with id " + locationResponse.userId() + " not found"));

                FavoriteLocation favoriteLocation = new FavoriteLocation(
                        null, user, locationResponse.locationId(), LocalDateTime.now());

                favoriteLocationRepository.save(favoriteLocation);
                log.info("Location {} successfully added to the repository for user: {}", locationResponse.locationId(), locationResponse.userId());
            } else {
                log.warn("Location {} does not exist for user: {}", locationResponse.locationId(), locationResponse.userId());
            }
        } catch (Exception e) {
            log.error("Error processing message", e);
            return Mono.empty();
        }
        return Mono.empty();
    }


    public Mono<Void> handleUserAddResponse(Delivery message) {
        try{
            AddLocationResponse response = objectMapper.readValue(message.getBody(), AddLocationResponse.class);
            log.info("Received location add response: {}", response);

            if (response.success()) {
                User user = userRepository.findById(response.userId())
                        .orElseThrow(() -> new RuntimeException("User with id " + response.userId() + " not found"));

                FavoriteLocation favoriteLocation = new FavoriteLocation(
                        null, user, response.locationId(), LocalDateTime.now()
                );
                favoriteLocationRepository.save(favoriteLocation);
                log.info("Successfully added to the repository for user: {}", response.userId());
            }else{
                log.warn("Location response was empty: {}", response);
            }

        }catch (Exception e) {
            log.error("Error processing message", e);
            return Mono.empty();
        }
        return Mono.empty();
    }


}
