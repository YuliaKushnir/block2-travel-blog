package org.example.block2travelblog.service;

import org.example.block2travelblog.messaging.EmailMessage;
import org.springframework.stereotype.Service;

/**
 * Service Interface for sending notification about new post.
 */
@Service
public interface PostCreatedNotificationService {
    void sendPostCreatedNotification(EmailMessage emailMessage);
}
