package com.yourfavspot.rabbit;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.rabbitmq.OutboundMessage;
import reactor.rabbitmq.Sender;


@Slf4j
@Component
public class RabbitMQMessageReactiveProducer {

    private final Sender sender;
    private final ObjectMapper objectMapper;

    public RabbitMQMessageReactiveProducer(Sender sender, ObjectMapper objectMapper) {
        this.sender = sender;
        this.objectMapper = objectMapper;
    }

    public Mono<Void> publishReactive(String exchange, String routingKey, Object message) {
        return Mono.fromCallable(() -> objectMapper.writeValueAsBytes(message))
                .flatMap(body -> {
                    OutboundMessage outboundMessage = new OutboundMessage(exchange, routingKey, body);
                    log.info("Publishing reactively to exchange: {} with routingKey: {}. Payload: {}", exchange, routingKey, message);
                    return sender.send(Mono.just(outboundMessage));
                })
                .then();
    }
}
