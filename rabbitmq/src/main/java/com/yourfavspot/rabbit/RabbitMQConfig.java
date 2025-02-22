package com.yourfavspot.rabbit;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // Konfiguracja dla Notification Service
    public static final String NOTIFICATION_QUEUE = "notification.queue";
    public static final String NOTIFICATION_EXCHANGE = "notification.exchange";

    // Konfiguracja dla User Service -> Location Service (żądania)
    public static final String LOCATION_REQUEST_QUEUE = "location.request.queue";
    public static final String LOCATION_REQUEST_EXCHANGE = "location.request.exchange";
    public static final String LOCATION_CHECK_ROUTING_KEY = "location.check";
    public static final String LOCATION_BULK_ROUTING_KEY = "location.getBulk";

    // Konfiguracja dla Location Service -> User Service (odpowiedzi)
    public static final String USER_RESPONSE_QUEUE = "user.response.queue";
    public static final String USER_RESPONSE_EXCHANGE = "user.response.exchange";
    public static final String USER_RESPONSE_ROUTING_KEY = "user.response";


    //adding new personal location
    public static final String LOCATION_ADD_QUEUE = "location.add.queue";
    public static final String LOCATION_ADD_EXCHANGE = "location.add.exchange";
    public static final String LOCATION_ADD_ROUTING_KEY = "location.add";

    // Kolejka do powiadomień
    @Bean
    public Queue notificationQueue() {
        return new Queue(NOTIFICATION_QUEUE, true); // Trwała kolejka
    }
    @Bean
    public FanoutExchange notificationExchange() {
        return new FanoutExchange(NOTIFICATION_EXCHANGE);
    }
    @Bean
    public Binding notificationBinding(@Qualifier("notificationQueue") Queue queue, FanoutExchange notificationExchange) {
        return BindingBuilder.bind(queue).to(notificationExchange);
    }



    // Kolejka żądań location service
    @Bean
    public Queue locationRequestQueue() {
        return new Queue(LOCATION_REQUEST_QUEUE, true);
    }

    // Exchange dla żądań do Location Service (Direct)
    @Bean
    public DirectExchange locationRequestExchange() {
        return new DirectExchange(LOCATION_REQUEST_EXCHANGE);
    }

    // Powiązanie kolejki żądań z exchange'em dla operacji "check"
    @Bean
    public Binding locationCheckBinding(@Qualifier("locationRequestQueue") Queue queue,
                                        @Qualifier("locationRequestExchange") DirectExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(LOCATION_CHECK_ROUTING_KEY);
    }

    // Powiązanie kolejki żądań z exchange'em dla operacji "getBulk"
    @Bean
    public Binding locationBulkBinding(@Qualifier("locationRequestQueue") Queue queue,
                                       @Qualifier("locationRequestExchange") DirectExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(LOCATION_BULK_ROUTING_KEY);
    }

    //Nowa kolejka do dodawnaia nowych personalnych lokaluzacji
    @Bean
    public Queue locationAddQueue() {
        return new Queue(LOCATION_ADD_QUEUE, true);
    }
    @Bean
    public DirectExchange locationAddExchange() {
        return new DirectExchange(LOCATION_ADD_EXCHANGE);
    }
    @Bean
    public Binding locationAddBinding(@Qualifier("locationAddQueue") Queue queue,
                                      @Qualifier("locationAddExchange") DirectExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(LOCATION_ADD_ROUTING_KEY);
    }




    // Kolejka dla odpowiedzi do User Service
    @Bean
    public Queue userResponseQueue() {
        return new Queue(USER_RESPONSE_QUEUE, true);
    }

    // Exchange dla odpowiedzi do User Service (Direct)
    @Bean
    public DirectExchange userResponseExchange() {
        return new DirectExchange(USER_RESPONSE_EXCHANGE);
    }

    // Powiązanie kolejki odpowiedzi z exchange'em
    @Bean
    public Binding userResponseBinding(@Qualifier("userResponseQueue") Queue queue,
                                       @Qualifier("userResponseExchange") DirectExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(USER_RESPONSE_ROUTING_KEY);
    }

    // RabbitTemplate do wysyłania i odbierania wiadomości
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(new Jackson2JsonMessageConverter());
        return rabbitTemplate;
    }

    // Producent wiadomości
    @Bean
    public RabbitMQMessageProducer rabbitMQMessageProducer(RabbitTemplate rabbitTemplate) {
        return new RabbitMQMessageProducer(rabbitTemplate);
    }

    // Konwerter wiadomości JSON
    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
