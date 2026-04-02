package com.pagamento.pag_facil.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE_TRANSACTION_NOTIFICATION = "transaction.notification.exchange";
    public static final String QUEUE_NOTIFICATION = "transaction.queue.notification";
    public static final String QUEUE_NOTIFICATION_RETRY = "transaction.queue.notification.retry";
    public static final String QUEUE_NOTIFICATION_DLQ = "transaction.queue.notification.dlq";
    public static final String ROUTING_KEY_NOTIFICATION = "transaction.notification";
    public static final String ROUTING_KEY_NOTIFICATION_RETRY = "transaction.notification.retry";
    public static final String ROUTING_KEY_NOTIFICATION_DLQ = "transaction.notification.dlq";

    @Bean
    public TopicExchange notificationExchange(){
        return new TopicExchange(EXCHANGE_TRANSACTION_NOTIFICATION, true, false);
    }

    @Bean
    public Queue queueNotification(){
        return QueueBuilder.durable(QUEUE_NOTIFICATION)
                .withArgument("x-dead-letter-exchange", EXCHANGE_TRANSACTION_NOTIFICATION)
                .withArgument("x-dead-letter-routing-key", ROUTING_KEY_NOTIFICATION_RETRY)
                .build();
    }

    @Bean
    public Queue queueNotificationRetry(){
        return QueueBuilder.durable(QUEUE_NOTIFICATION_RETRY)
                .withArgument("x-dead-letter-exchange", EXCHANGE_TRANSACTION_NOTIFICATION)
                .withArgument("x-dead-letter-routing-key", ROUTING_KEY_NOTIFICATION)
                .withArgument("x-message-ttl", 10000)
                .build();
    }

    @Bean
    public Queue queueNotificationDlq(){
        return QueueBuilder.durable(QUEUE_NOTIFICATION_DLQ).build();
    }

    @Bean
    public Binding bindingNotification(Queue queueNotification, TopicExchange notificationExchange){
        return BindingBuilder.bind(queueNotification)
                .to(notificationExchange)
                .with(ROUTING_KEY_NOTIFICATION);
    }

    @Bean
    public Binding bindingNotificationRetry(Queue queueNotificationRetry, TopicExchange notificationExchange){
        return BindingBuilder.bind(queueNotificationRetry)
                .to(notificationExchange)
                .with(ROUTING_KEY_NOTIFICATION_RETRY);
    }

    @Bean
    public Binding bindingNotificationDlq(Queue queueNotificationDlq, TopicExchange notificationExchange){
        return BindingBuilder.bind(queueNotificationDlq)
                .to(notificationExchange)
                .with(ROUTING_KEY_NOTIFICATION_DLQ);
    }

    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory){
        RabbitAdmin admin = new RabbitAdmin(connectionFactory);
        admin.setAutoStartup(true);
        // Avoid startup failure if a queue already exists with incompatible arguments; prefer manual cleanup on the broker.
        admin.setIgnoreDeclarationExceptions(true);
        return admin;
    }

    @Bean
    public MessageConverter jsonMessageConverter(){
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter jsonMessageConverter){
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter);
        return template;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory, MessageConverter jsonMessageConverter){
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        factory.setMessageConverter(jsonMessageConverter);
        return factory;
    }
    @Bean
    public ApplicationRunner rabbitinit(RabbitAdmin rabbitAdmin){
        return args -> rabbitAdmin.initialize();
    }

}
