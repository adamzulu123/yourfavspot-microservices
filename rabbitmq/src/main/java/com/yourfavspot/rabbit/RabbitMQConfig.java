package com.yourfavspot.rabbit;

import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import reactor.rabbitmq.*;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.Connection;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.awt.desktop.QuitEvent;
import java.util.Arrays;
import java.util.List;


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

    //konfiguracja ale do reaktywnych odpowiedzi  z location service
    public static final String USER_RESPONSE_QUEUE_REACTOR = "user.response.queue.reactor";
    public static final String USER_RESPONSE_EXCHANGE_REACTOR = "user.response.exchange.reactor";
    public static final String USER_RESPONSE_ROUTING_KEY_REACTOR = "user.response.reactor";
    public static final String USER_RESPONSE_ADD_ROUTING_KEY_REACTOR = "user.response.add.reactor";

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



    // Kolejka żądań location service - synchroniczne!
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



    //Nowa kolejka do dodawnaia nowych personalnych lokaluzacji - używaliśmy do komunikacji klasycznej
    //teraz zmieniam na reaktywną z reactor rabbitmq
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




    // Kolejka dla odpowiedzi do User Service - sychroniczne
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



    //reactive location service -> user serwice (odpowiedz) - kolejka reaktywna !
    @Bean
    public Queue userResponseQueueReactor() {
        return new Queue(USER_RESPONSE_QUEUE_REACTOR, true);
    }

    @Bean
    public DirectExchange userResponseExchangeReactor() {
        return new DirectExchange(USER_RESPONSE_EXCHANGE_REACTOR);
    }

    //nowy binding dla innego typu wiadomości możemy sobie dodać tworząć nowa metoda jak w location request tym synchronicznym
    //albo możemy sobie zwróić tutaj liste bindingów i tym razem użyje tego dla mniejszej ilosci kodu
    //todo: sprawdzić czy działa !!!
    @Bean
    public Binding userResponseBindingReactor(@Qualifier("userResponseQueueReactor") Queue queue,
                                                    @Qualifier("userResponseExchangeReactor") DirectExchange exchange) {
       Binding binding1 = BindingBuilder.bind(queue).to(exchange).with(USER_RESPONSE_ROUTING_KEY_REACTOR);
       //Binding binding2 = BindingBuilder.bind(queue).to(exchange).with(USER_RESPONSE_ADD_ROUTING_KEY_REACTOR);
       //return Arrays.asList(binding1, binding2);
        return binding1;
    }
    @Bean
    public Binding userResponseBindingAddReactor(@Qualifier("userResponseQueueReactor") Queue queue,
                                                 @Qualifier("userResponseExchangeReactor") DirectExchange exchange){
        return BindingBuilder.bind(queue).to(exchange).with(USER_RESPONSE_ADD_ROUTING_KEY_REACTOR);
    }




    //conncection factory do komunikacji synchronicznej
    @Bean
    public ConnectionFactory springRabbitConnectionFactory() {
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory("localhost");
        connectionFactory.setUsername("guest");
        connectionFactory.setPassword("guest");
        return connectionFactory;
    }
    // RabbitTemplate do wysyłania i odbierania wiadomości - synchronicznie
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory springRabbitConnectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(springRabbitConnectionFactory);
        rabbitTemplate.setMessageConverter(new Jackson2JsonMessageConverter());
        return rabbitTemplate;
    }
    // Producent wiadomości
    @Bean
    public RabbitMQMessageProducer rabbitMQMessageProducer(RabbitTemplate rabbitTemplate) {
        return new RabbitMQMessageProducer(rabbitTemplate);
    }




    //komunikacja reaktywna, asynchroniczna !
    @Bean
    public com.rabbitmq.client.ConnectionFactory reactiveConnectionFactory() {
        com.rabbitmq.client.ConnectionFactory factory = new com.rabbitmq.client.ConnectionFactory();
        factory.setHost("localhost");
        factory.setPort(5672);
        factory.setUsername("guest");
        factory.setPassword("guest");
        return factory;
    }

    @Bean
    public Receiver receiver(com.rabbitmq.client.ConnectionFactory reactiveConnectionFactory) {
        return RabbitFlux.createReceiver(new ReceiverOptions().connectionFactory(reactiveConnectionFactory));
    }

    @Bean
    public Sender sender(com.rabbitmq.client.ConnectionFactory reactiveConnectionFactory) {
        return RabbitFlux.createSender(new SenderOptions().connectionFactory(reactiveConnectionFactory));
    }




    // Konwerter wiadomości JSON - używany do synchronicznej i asychronicznej
    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }



}
