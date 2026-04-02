package com.pagamento.pag_facil.integration.rabbit;

import com.pagamento.pag_facil.config.RabbitMQConfig;
import com.pagamento.pag_facil.dto.TransferNotificationMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationProducer {

    private final RabbitTemplate rabbitTemplate;

    public void sendNotification(TransferNotificationMessage message ){
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_TRANSACTION_NOTIFICATION, RabbitMQConfig.ROUTING_KEY_NOTIFICATION, message);
    }

}
