package com.yourfavspot.notification;


import com.rabbitmq.client.Channel; // Dla parametru Channel
import com.yourfavspot.common.NotificationRequest; // Dla NotificationRequest
import com.yourfavspot.rabbit.RabbitMQConfig; // Dla QUEUE_NAME

import org.springframework.amqp.rabbit.annotation.EnableRabbit; // Dla @EnableRabbit
import org.springframework.amqp.rabbit.annotation.RabbitListener; // Dla @RabbitListener
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header; // Dla @Header
import org.springframework.stereotype.Service; // Dla @Service
import java.io.IOException; // Dla IOException
@EnableRabbit
@Service
public class NotificationConsumer {
    @RabbitListener(queues = RabbitMQConfig.NOTIFICATION_QUEUE, messageConverter = "messageConverter", ackMode = "MANUAL")
    public void receiveMessage(NotificationRequest notificationRequest, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {
        System.out.println("Received notification: " + notificationRequest.message());
        //channel.basicAck(tag, false); // Zakomentuj, aby wiadomość została w kolejce
    }
}
