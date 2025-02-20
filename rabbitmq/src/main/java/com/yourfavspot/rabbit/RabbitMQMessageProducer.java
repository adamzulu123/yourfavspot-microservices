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

    public void publish(Object payload) {
        log.info("Publishing to using routingKey . Payload: {}", payload);
        amqpTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, "", payload);  // Pusty routing key
    }
}
