package com.yourfavspot.location.rabbit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yourfavspot.common.CheckLocationRequest;
import com.yourfavspot.common.LocationResponse;
import com.yourfavspot.location.service.LocationService;
import com.yourfavspot.rabbit.RabbitMQConfig;
import com.yourfavspot.rabbit.RabbitMQMessageReactiveProducer;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.rabbitmq.Receiver;
import reactor.rabbitmq.Sender;

import java.io.IOException;

@Slf4j
@Service
public class LocationRabbitReactiveConsumer {
    private final Receiver receiver;
    private final LocationService locationService;
    private final ObjectMapper objectMapper;
    private final RabbitMQMessageReactiveProducer producer;
    private final Queue locationRequestQueue;

    public LocationRabbitReactiveConsumer(
            Receiver receiver,
            LocationService locationService,
            ObjectMapper objectMapper,
            RabbitMQMessageReactiveProducer producer,
            @Qualifier("locationRequestQueue") Queue locationRequestQueue) {
        this.receiver = receiver;
        this.locationService = locationService;
        this.objectMapper = objectMapper;
        this.producer = producer;
        this.locationRequestQueue = locationRequestQueue;
    }

    @EventListener(ContextRefreshedEvent.class)
    public void consume() {
        log.info("Kontekst Springa odświeżony, uruchamiam reaktywnego konsumenta dla kolejki: {}", RabbitMQConfig.LOCATION_REQUEST_QUEUE);
        receiver.consumeAutoAck(RabbitMQConfig.LOCATION_REQUEST_QUEUE)
                .flatMap(message -> {
                    try {
                        CheckLocationRequest request = objectMapper.readValue(message.getBody(), CheckLocationRequest.class);
                        log.info("Received location check request: {}", request);

                        return locationService.checkLocationExists(request.locationId())
                                .flatMap(exists -> {
                                    LocationResponse response = new LocationResponse(
                                            request.userId(),
                                            request.locationId(),
                                            exists
                                    );
                                    return producer.publishReactive(
                                            RabbitMQConfig.USER_RESPONSE_EXCHANGE_REACTOR,
                                            RabbitMQConfig.USER_RESPONSE_ROUTING_KEY_REACTOR,
                                            response
                                    );
                                });
                    } catch (Exception e) {
                        log.error("Error processing message", e);
                        return Mono.empty();
                    }
                })
                .doOnError(e -> log.error("Error in reactive consumer", e))
                .subscribe();

    }
}
