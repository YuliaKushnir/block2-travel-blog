package org.example.block2travelblog.service;

import lombok.RequiredArgsConstructor;
import org.example.block2travelblog.messaging.EmailMessage;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

/**
 * Service implementation for sending post creation notifications.
 * Publishes {@link EmailMessage} events to RabbitMQ exchange.
 */
@Service
@RequiredArgsConstructor
public class PostCreatedNotificationServiceImpl implements PostCreatedNotificationService {

    private final RabbitTemplate rabbitTemplate;

    /**
     * Sends a notification about a newly created post.
     * Publishes the message to the "travel-blog" exchange with routing key "post.created".
     *
     * @param emailMessage the email message to send
     */
    @Override
    public void sendPostCreatedNotification(EmailMessage emailMessage){
        rabbitTemplate.convertAndSend("travel-blog", "post.created", emailMessage);
    }
}
