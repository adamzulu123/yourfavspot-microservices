package com.yourfavspot.user.Service;

import com.yourfavspot.common.CheckLocationRequest;
import com.yourfavspot.common.NotificationRequest;
import com.yourfavspot.rabbit.RabbitMQConfig;
import com.yourfavspot.rabbit.RabbitMQMessageProducer;
import com.yourfavspot.user.Model.FavoriteLocation;
import com.yourfavspot.user.Model.FraudCheckResponse;
import com.yourfavspot.user.Model.User;
import com.yourfavspot.user.Model.UserRegistrationRequest;
import com.yourfavspot.user.Repository.FavoriteLocationRepository;
import com.yourfavspot.user.Repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
    private final FavoriteLocationRepository favoriteLocationRepository;


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

        /* todo: poprawka tego pozniej aby wysyłac notification w poprawny sposob
        // Przygotowanie powiadomienia, które ma trafić do RabbitMQ
        NotificationRequest notificationRequest = new NotificationRequest(
                user.getId(),
                user.getEmail(),
                "Welcome to the system!"
        );

        // Wysłanie powiadomienia do RabbitMQ (do wymiany fanout)
        //rabbitMQMessageProducer.publish(notificationRequest);

         */
    }


    public void checkAndAddFavoriteLocation(Integer userId, String locationId) {
        CheckLocationRequest request = new CheckLocationRequest(userId, locationId);
        log.info("Sending location check request: userId={}, locationId={}", userId, locationId);
        producer.publish(RabbitMQConfig.LOCATION_REQUEST_EXCHANGE, RabbitMQConfig.LOCATION_CHECK_ROUTING_KEY, request);
        // Uwaga: Odpowiedź przyjdzie asynchronicznie do UserRabbitConsumer
    }



}

