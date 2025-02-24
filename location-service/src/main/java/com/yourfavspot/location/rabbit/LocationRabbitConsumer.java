package com.yourfavspot.location.rabbit;

import com.yourfavspot.common.AddLocationRequest;
import com.yourfavspot.common.AddLocationResponse;
import com.yourfavspot.common.CheckLocationRequest;
import com.yourfavspot.common.LocationResponse;
import com.yourfavspot.location.model.LocationModel;
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

    /* //zastapione za pomocą reactor rabbitmq
    @RabbitListener(queues = RabbitMQConfig.LOCATION_REQUEST_QUEUE)
    public void handleLocationCheckRequest(CheckLocationRequest request) {
        log.info("Received location check request: userId={}, locationId={}", request.userId(), request.locationId());
        locationService.checkLocationExists(request.locationId())
                .subscribe(exists -> {
                    LocationResponse response = new LocationResponse(request.userId(), request.locationId(), exists);
                    producer.publish(RabbitMQConfig.USER_RESPONSE_EXCHANGE, RabbitMQConfig.USER_RESPONSE_ROUTING_KEY, response);
                });
    }
     */

    //todo:  zmiana tego na komunikację reatywną - reactor rabbitmq
    @RabbitListener(queues = RabbitMQConfig.LOCATION_ADD_QUEUE)
    public void handleLocationAddRequest(AddLocationRequest request) {
        log.info("Received add location request: userId={}, name={}", request.userId(), request.name());

        LocationModel location = new LocationModel();
        location.setUserId(request.userId());
        location.setName(request.name());
        location.setDescription(request.description());
        location.setCategory(request.type());
        location.setPersonal(true);
        location.setLocation(request.coordinates());

        locationService.createLocation(location)
                .subscribe(createdLocation -> {
                    //po udanym utworzeniu lokalizacji wysyłamy powiadomienie o sukcesie
                    AddLocationResponse response = new AddLocationResponse(
                            request.userId(), createdLocation.getId(), true, "Location created!"
                    );
                    producer.publish(RabbitMQConfig.USER_RESPONSE_EXCHANGE, RabbitMQConfig.USER_RESPONSE_ROUTING_KEY, response);
                }, error -> {
                    //jesli nie udało się utworzyc kolejki!
                    log.error("Failed to add location", error);
                    AddLocationResponse response = new AddLocationResponse(
                            request.userId(), null, false, "Failed to add location!"
                    );
                    producer.publish(RabbitMQConfig.USER_RESPONSE_EXCHANGE, RabbitMQConfig.USER_RESPONSE_ROUTING_KEY, response);
                });

    }
}











