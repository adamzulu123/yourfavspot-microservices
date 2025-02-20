package com.yourfavspot.user.Service;

import com.yourfavspot.common.NotificationRequest;
import com.yourfavspot.rabbit.RabbitMQMessageProducer;
import com.yourfavspot.user.Model.FraudCheckResponse;
import com.yourfavspot.user.Model.User;
import com.yourfavspot.user.Model.UserRegistrationRequest;
import com.yourfavspot.user.Repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@AllArgsConstructor
@Service
public class UserService {
    private final UserRepository userRepository;
    private final RestTemplate restTemplate;
    private final RabbitMQMessageProducer rabbitMQMessageProducer;


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

        // Przygotowanie powiadomienia, które ma trafić do RabbitMQ
        NotificationRequest notificationRequest = new NotificationRequest(
                user.getId(),
                user.getEmail(),
                "Welcome to the system!"
        );

        // Wysłanie powiadomienia do RabbitMQ (do wymiany fanout)
        rabbitMQMessageProducer.publish(notificationRequest);
    }





        // todo: send notification
}

