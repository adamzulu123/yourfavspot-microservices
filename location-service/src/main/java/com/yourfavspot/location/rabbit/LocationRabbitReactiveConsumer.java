package com.yourfavspot.location.rabbit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yourfavspot.common.AddLocationRequest;
import com.yourfavspot.common.AddLocationResponse;
import com.yourfavspot.common.CheckLocationRequest;
import com.yourfavspot.common.LocationResponse;
import com.yourfavspot.location.model.LocationModel;
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

    public LocationRabbitReactiveConsumer(
            Receiver receiver,
            LocationService locationService,
            ObjectMapper objectMapper,
            RabbitMQMessageReactiveProducer producer){
        this.receiver = receiver;
        this.locationService = locationService;
        this.objectMapper = objectMapper;
        this.producer = producer;

    }

    //todo: pewnie wykrozystam też ja pozniej do operacji na publiczych lokalizacjach
    //nasłuchiwacz do obslugi kolejki do dodawania publiczych lokalizacji do ulubionych
    @EventListener(ContextRefreshedEvent.class)
    public void consume() {
        log.info("Spring context refreshed, starting reactive consumer for queue: {}", RabbitMQConfig.LOCATION_REQUEST_QUEUE);
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

    //todo: bedzie wykorzystywana do wszelkiego rodzaju operacji na prywatnych zapisanych lokalizacjach
    //nasłuchiwacz na kolejke do dodawania nowych lokalizacji
    @EventListener(ContextRefreshedEvent.class)
    public void consumeAddLocation(){
        log.info("Spring context refreshed, starting reactive consumer for : {}", RabbitMQConfig.LOCATION_ADD_QUEUE);
        receiver.consumeAutoAck(RabbitMQConfig.LOCATION_ADD_QUEUE)
                //flatMap - pozwala asychronicznie przetwarzać kolejne przychodzące wiadomosci do kolejki w sposób asycnroniczny
                .flatMap(message -> {
                    AddLocationRequest request;
                    try{
                        request = objectMapper.readValue(message.getBody(), AddLocationRequest.class);
                    }catch (Exception e) {
                        log.error("Error processing message", e);
                        return Mono.empty();
                    }
                    log.info("Received add location request: {}", request);
                    LocationModel location = new LocationModel();
                    location.setUserId(request.userId().toString());
                    location.setName(request.name());
                    location.setDescription(request.description());
                    location.setCategory(request.type());
                    location.setPersonal(true);
                    location.setLocation(request.coordinates());

                    //dodawanie lokalizacji do bazy oraz wysyłanie odpowiediz do kolejki
                    return locationService.createLocation(location)
                            .flatMap(createdLocation -> {
                                AddLocationResponse response = new AddLocationResponse(
                                        request.userId(), createdLocation.getId(), true, "Location created!");
                                return producer.publishReactive(RabbitMQConfig.USER_RESPONSE_EXCHANGE_REACTOR,
                                        RabbitMQConfig.USER_RESPONSE_ADD_ROUTING_KEY_REACTOR, response);
                            })
                            //gdy coś pójdzie nie tak w stworzeniu nowej lokalizacji
                            .onErrorResume(e -> {
                                log.error("Error in reactive consumer", e);
                                AddLocationResponse response = new AddLocationResponse(
                                        request.userId(), null, false, "Failed to add location!"
                                );
                                return producer.publishReactive(RabbitMQConfig.USER_RESPONSE_EXCHANGE_REACTOR,
                                        RabbitMQConfig.USER_RESPONSE_ADD_ROUTING_KEY_REACTOR, response);
                            });

                })
                .doOnError(e -> log.error("Error in reactive consumer", e))
                .subscribe();

    }


}
