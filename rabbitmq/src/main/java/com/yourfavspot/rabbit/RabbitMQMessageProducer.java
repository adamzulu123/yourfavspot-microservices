package com.yourfavspot.rabbit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import org.springframework.amqp.core.AmqpTemplate;
@Slf4j
public class RabbitMQMessageProducer {
    private final AmqpTemplate amqpTemplate;

    public RabbitMQMessageProducer(AmqpTemplate amqpTemplate) {
        this.amqpTemplate = amqpTemplate;
    }

    public void publish(String exchange, String routingKey, Object payload) {
        log.info("Publishing to exchange: {} with routingKey: {}. Payload: {}", exchange, routingKey, payload);
        amqpTemplate.convertAndSend(exchange, routingKey, payload);
    }
}
