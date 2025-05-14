package com.tribhaskar.springkafkago.service;

import com.tribhaskar.springkafkago.config.KafkaTopicConfig;
import com.tribhaskar.springkafkago.model.Notification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Service
public class NotificationProducer {

    private static final Logger logger = LoggerFactory.getLogger(NotificationProducer.class);

    @Autowired
    private KafkaTemplate<String, Notification> kafkaTemplate;

    public void sendNotification(Notification notification) {
        // Set generated ID and timestamp if not already set
        if (notification.getId() == null) {
            notification.setId(UUID.randomUUID().toString());
        }
        if (notification.getTimestamp() == null) {
            notification.setTimestamp(LocalDateTime.now());
        }

        // Send to main notifications topic
        sendToTopic(KafkaTopicConfig.NOTIFICATION_TOPIC, notification);

        // Also send to specific topic based on notification type
        String specificTopic = null;
        switch (notification.getType().toUpperCase()) {
            case "EMAIL":
                specificTopic = KafkaTopicConfig.EMAIL_TOPIC;
                break;
            case "SMS":
                specificTopic = KafkaTopicConfig.SMS_TOPIC;
                break;
            case "PUSH":
                specificTopic = KafkaTopicConfig.PUSH_TOPIC;
                break;
            default:
                // Only send to main topic if type is unknown
                return;
        }

        sendToTopic(specificTopic, notification);
    }

    private void sendToTopic(String topic, Notification notification) {
        try {
            // Using key as recipient to ensure all notifications for the same recipient
            // go to the same partition (ordering)
            CompletableFuture<SendResult<String, Notification>> future =
                    kafkaTemplate.send(topic, notification.getRecipient(), notification);

            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    logger.info("Sent message=[{}] with offset=[{}] to topic=[{}]",
                            notification,
                            result.getRecordMetadata().offset(),
                            topic);
                } else {
                    logger.error("Unable to send message=[{}] due to : {}", notification, ex.getMessage());
                }
            });

            // Can be removed if you don't want to block
            future.get();
        } catch (InterruptedException | ExecutionException e) {
            logger.error("Error sending notification to topic {}: {}", topic, e.getMessage());
            Thread.currentThread().interrupt();
        }
    }
}
