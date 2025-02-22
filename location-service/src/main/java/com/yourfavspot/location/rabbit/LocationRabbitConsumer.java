package com.yourfavspot.location.rabbit;

import com.yourfavspot.common.CheckLocationRequest;
import com.yourfavspot.common.LocationResponse;
import com.yourfavspot.location.service.LocationService;
import com.yourfavspot.rabbit.RabbitMQConfig;
import com.yourfavspot.rabbit.RabbitMQMessageProducer;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Slf4j
@EnableRabbit
@Service
@AllArgsConstructor
public class LocationRabbitConsumer {

    private final LocationService locationService;
    private final RabbitMQMessageProducer producer;

    @RabbitListener(queues = RabbitMQConfig.LOCATION_REQUEST_QUEUE)
    public void handleLocationCheckRequest(CheckLocationRequest request) {
        log.info("Received location check request: userId={}, locationId={}", request.userId(), request.locationId());
        locationService.checkLocationExists(request.locationId())
                .subscribe(exists -> {
                    LocationResponse response = new LocationResponse(request.userId(), request.locationId(), exists);
                    producer.publish(RabbitMQConfig.USER_RESPONSE_EXCHANGE, RabbitMQConfig.USER_RESPONSE_ROUTING_KEY, response);
                });
    }
}
