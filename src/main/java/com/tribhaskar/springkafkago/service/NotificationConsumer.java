package com.tribhaskar.springkafkago.service;

import com.tribhaskar.springkafkago.config.KafkaTopicConfig;
import com.tribhaskar.springkafkago.model.Notification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class NotificationConsumer {

    private static final Logger logger = LoggerFactory.getLogger(NotificationConsumer.class);

    @KafkaListener(topics = KafkaTopicConfig.NOTIFICATION_TOPIC, groupId = "${spring.kafka.consumer.group-id}")
    public void consumeNotification(Notification notification) {
        logger.info("Received notification: {}", notification);
        processNotification(notification);
    }

    @KafkaListener(topics = KafkaTopicConfig.EMAIL_TOPIC, groupId = "${spring.kafka.consumer.group-id}")
    public void consumeEmailNotification(Notification notification) {
        logger.info("Processing EMAIL notification: {}", notification);
        sendEmail(notification);
    }

    @KafkaListener(topics = KafkaTopicConfig.SMS_TOPIC, groupId = "${spring.kafka.consumer.group-id}")
    public void consumeSmsNotification(Notification notification) {
        logger.info("Processing SMS notification: {}", notification);
        sendSms(notification);
    }

    @KafkaListener(topics = KafkaTopicConfig.PUSH_TOPIC, groupId = "${spring.kafka.consumer.group-id}")
    public void consumePushNotification(Notification notification) {
        logger.info("Processing PUSH notification: {}", notification);
        sendPushNotification(notification);
    }

    private void processNotification(Notification notification) {
        // General notification processing logic
        // This could include saving to a database, analytics, etc.
        logger.info("General processing for notification ID: {}", notification.getId());
    }

    private void sendEmail(Notification notification) {
        // Email specific logic would go here
        // In a real application, this would connect to an email service
        logger.info("Sending EMAIL to {} with subject: {}",
                notification.getRecipient(),
                notification.getSubject());
    }

    private void sendSms(Notification notification) {
        // SMS specific logic would go here
        // In a real application, this would connect to an SMS service
        logger.info("Sending SMS to {}: {}",
                notification.getRecipient(),
                notification.getContent());
    }

    private void sendPushNotification(Notification notification) {
        // Push notification specific logic would go here
        // In a real application, this would connect to a push notification service
        logger.info("Sending PUSH notification to {}: {}",
                notification.getRecipient(),
                notification.getContent());
    }
}
