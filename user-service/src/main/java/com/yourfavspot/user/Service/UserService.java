package com.yourfavspot.user.Service;

import com.yourfavspot.common.AddLocationRequest;
import com.yourfavspot.common.CheckLocationRequest;
import com.yourfavspot.common.NotificationRequest;
import com.yourfavspot.rabbit.RabbitMQConfig;
import com.yourfavspot.rabbit.RabbitMQMessageProducer;
import com.yourfavspot.rabbit.RabbitMQMessageReactiveProducer;
import com.yourfavspot.user.Model.*;
import com.yourfavspot.user.Repository.FavoriteLocationRepository;
import com.yourfavspot.user.Repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import reactor.core.publisher.Mono;

@Slf4j
@AllArgsConstructor
@Service
public class UserService {
    private final UserRepository userRepository;
    private final RestTemplate restTemplate;
    private final RabbitMQMessageProducer producer;
    private final RabbitMQMessageReactiveProducer reactiveProducer;
    private final FavoriteLocationRepository favoriteLocationRepository;
    private final KeycloakAdminClient keycloakAdminClient;
    private final PasswordEncoder passwordEncoder;

    /*
    public void registerUser(UserRegistrationRequest request) {
        User user = User.builder()
                .firstName(request.firstName())
                .lastName(request.lastName())
                .email(request.email())
                .password(request.password())
                .build();
        // todo: email (not taken?) and password valid? store customer in db
        // todo: check if fraudster
        userRepository.saveAndFlush(user);
        FraudCheckResponse fraudCheckResponse = restTemplate.getForObject(
                "http://FRAUD-SERVICE/api/v1/fraud-check/{userId}",
                FraudCheckResponse.class,
                user.getId());

        if(fraudCheckResponse.isFraudster()){
            throw new IllegalStateException("Fraudster");
        }

        // todo: poprawka tego pozniej aby wysyłac notification w poprawny sposob
        // Przygotowanie powiadomienia, które ma trafić do RabbitMQ
        NotificationRequest notificationRequest = new NotificationRequest(
                user.getId(),
                user.getEmail(),
                "Welcome to the system!"
        );

        // Wysłanie powiadomienia do RabbitMQ (do wymiany fanout)
        //rabbitMQMessageProducer.publish(notificationRequest);


    }
    */

    public boolean registerUserDto (UserDto userDto) {
        boolean createdkeycloak = keycloakAdminClient.createUser(userDto);
        if(!createdkeycloak) {
            log.error("Could not create keycloak user");
            return false;
        }

        User user = User.builder()
                .firstName(userDto.getFirstName())
                .lastName(userDto.getLastName())
                .email(userDto.getEmail())
                .username(userDto.getUsername() != null ? userDto.getUsername() : userDto.getEmail())
                .enabled(userDto.isEnabled())
                .build();

        userRepository.save(user);
        return true;
    }

    public LoginResponse loginUser(LoginRequest loginRequest) {
        return keycloakAdminClient.login(loginRequest.getUsername(), loginRequest.getPassword());
    }






    //sychronicznie - klasyczna komunkacja - teraz nie działa bo kolejka uzywana jest do projectreactor
    public void checkAndAddFavoriteLocation(Integer userId, String locationId) {
        CheckLocationRequest request = new CheckLocationRequest(userId, locationId);
        log.info("Sending location check request: userId={}, locationId={}", userId, locationId);
        //producer.publish(RabbitMQConfig.LOCATION_REQUEST_EXCHANGE, RabbitMQConfig.LOCATION_CHECK_ROUTING_KEY, request);
        // Uwaga: Odpowiedź przyjdzie asynchronicznie do UserRabbitConsumer
    }

    //reactive adding location do favorite
    public Mono<Void> checkAndAddFavoriteLocationReactive(Integer userId, String locationId) {
        CheckLocationRequest request = new CheckLocationRequest(userId, locationId);
        log.info("Sending location check request: userId={}, locationId={}", userId, locationId);
        return reactiveProducer.publishReactive(RabbitMQConfig.LOCATION_REQUEST_EXCHANGE,
                RabbitMQConfig.LOCATION_CHECK_ROUTING_KEY, request)
                .doOnSuccess(unused -> log.info("Successfully sent location check request for userId={}, locationId={}", userId, locationId))
                .doOnError(error -> log.error("Error sending location check request for userId={}, locationId={}", userId, locationId, error));
    }


    //todo : zmienić na reaktywą komunikację przez reactor rabbitmq
    //adding new personal location - reactive
    public Mono<Void> addNewLocationReactive(Integer userId, AddLocationRequest request) {
        AddLocationRequest message = new AddLocationRequest(
                userId,
                request.name(),
                request.description(),
                request.type(),
                request.coordinates()
        );
        return reactiveProducer.publishReactive(RabbitMQConfig.LOCATION_ADD_EXCHANGE,
                RabbitMQConfig.LOCATION_ADD_ROUTING_KEY, message)
                .doOnSuccess(unused -> log.info("Successfully sent request to add new location to the map by user: ={}, message={}",
                        userId, message))
                .doOnError(error -> log.error("Error sending request to add new location:", error));
    }

    //sychronicznie - aktualnie nie działa bo z tym serwisem chcemy komunikację asychroniczną
    public void addNewLocation(Integer userId, AddLocationRequest request) {
        AddLocationRequest message = new AddLocationRequest(
                userId,
                request.name(),
                request.description(),
                request.type(),
                request.coordinates()
        );
        log.info("Sending location request: userId={}, locationId={}", userId, request.name());
        //producer.publish(RabbitMQConfig.LOCATION_ADD_EXCHANGE, RabbitMQConfig.LOCATION_ADD_ROUTING_KEY, message);
    }



}

