package com.yourfavspot.user.rabbit;


import com.yourfavspot.common.AddLocationResponse;
import com.yourfavspot.common.LocationResponse;
import com.yourfavspot.rabbit.RabbitMQConfig;
import com.yourfavspot.user.Model.FavoriteLocation;
import com.yourfavspot.user.Model.User;
import com.yourfavspot.user.Repository.FavoriteLocationRepository;
import com.yourfavspot.user.Repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@EnableRabbit
@Service
@AllArgsConstructor
@RabbitListener(queues = RabbitMQConfig.USER_RESPONSE_QUEUE)
public class UserRabbitConsumer {

    private final UserRepository userRepository;
    private final FavoriteLocationRepository favoriteLocationRepository;

    @RabbitHandler
    public void handleLocationResponse(LocationResponse locationResponse) {
        if(locationResponse.exists()) {
            log.info("Location {} exists, adding to favorites for user {}", locationResponse.locationId(), locationResponse.userId());
            User user = userRepository.findById(locationResponse.userId())
                    .orElseThrow(()-> new RuntimeException("User with id " + locationResponse.userId() + " not found"));

            FavoriteLocation favoriteLocation =
                    new FavoriteLocation(null, user, locationResponse.locationId(), LocalDateTime.now());

            favoriteLocationRepository.save(favoriteLocation);
            log.info("Location saved to the repository {} for user : {}", favoriteLocation, locationResponse.userId());

        }else {
            log.warn("Location {} does not exist", locationResponse.locationId());
        }
    }

    //obsluga odpowiedzi o dodaniu nowej wiadomości
    @RabbitHandler
    public void handleAddLocationResponse(AddLocationResponse response) {
        if(response.success()){
            log.info("Location {} successfully added", response.locationId());
            User user = userRepository.findById(response.userId())
                    .orElseThrow(()-> new RuntimeException("User with id " + response.userId() + " not found"));

            FavoriteLocation favoriteLocation = new FavoriteLocation(
                    null, user, response.locationId(), LocalDateTime.now());

            favoriteLocationRepository.save(favoriteLocation);
            log.info("Location {} successfully added to the repository", response.locationId());
        }
        else {
            log.warn("Location {} not successfully added", response.locationId());
        }
    }

}
