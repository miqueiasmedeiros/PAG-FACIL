package com.pagamento.pag_facil.integration.rabbit;

import com.pagamento.pag_facil.config.RabbitMQConfig;
import com.pagamento.pag_facil.dto.NotificationDTO;
import com.pagamento.pag_facil.dto.TransferNotificationMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.amqp.core.Message;
import com.rabbitmq.client.Channel;

@Component
public class NotificationConsumer {
    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationConsumer.class);

    private final RestClient restClient;
    private final RabbitTemplate rabbitTemplate;

    public NotificationConsumer(RestClient.Builder builder, RabbitTemplate rabbitTemplate) {
        this.restClient = builder.baseUrl("https://util.devi.tools/api/v1/notify").build();
        this.rabbitTemplate = rabbitTemplate;
    }


    @RabbitListener(queues = RabbitMQConfig.QUEUE_NOTIFICATION, containerFactory = "rabbitListenerContainerFactory")
    public void receiveNotification(TransferNotificationMessage message,
                                    Message rawMessage,
                                    Channel channel,
                                    @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) throws Exception {

        LOGGER.info("receiving notification {}...", message);

        try {
            var response = restClient.get().retrieve().toEntity(NotificationDTO.class);

            if(response.getStatusCode().isError() || !response.getBody().message()) {
                throw new RuntimeException("Notification failed" + message);
            }
            LOGGER.info("notification received successfully {}...", response.getBody());
            channel.basicAck(deliveryTag, false);
        } catch (Exception ex) {
            long attempts = extractDeathCount(rawMessage);
            if (attempts >= 3) {
                rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_TRANSACTION_NOTIFICATION,
                        RabbitMQConfig.ROUTING_KEY_NOTIFICATION_DLQ, message);
                LOGGER.error("notification failed, routed to DLQ after {} attempts: {}", attempts, ex.getMessage());
                channel.basicAck(deliveryTag, false);
                return;
            }
            LOGGER.warn("notification failed, sending to retry (attempt {}): {}", attempts + 1, ex.getMessage());
            channel.basicNack(deliveryTag, false, false);
        }
    }

    private long extractDeathCount(Message message) {
        var deathHeader = message.getMessageProperties().getXDeathHeader();
        if (deathHeader == null || deathHeader.isEmpty()) {
            return 0;
        }
        var first = deathHeader.get(0);
        Object count = first.get("count");
        if (count instanceof Long l) {
            return l;
        }
        if (count instanceof Integer i) {
            return i.longValue();
        }
        return 0;
    }
}
