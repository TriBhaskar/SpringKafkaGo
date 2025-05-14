package com.tribhaskar.springkafkago.controller;

import com.tribhaskar.springkafkago.model.Notification;
import com.tribhaskar.springkafkago.service.NotificationProducer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationProducer notificationProducer;

    @Autowired
    public NotificationController(NotificationProducer notificationProducer) {
        this.notificationProducer = notificationProducer;
    }

    @PostMapping
    public ResponseEntity<String> sendNotification(@RequestBody Notification notification) {
        try {
            notificationProducer.sendNotification(notification);
            return new ResponseEntity<>("Notification sent successfully!", HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to send notification: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/email")
    public ResponseEntity<String> sendEmailNotification(@RequestBody Notification notification) {
        try {
            notification.setType("EMAIL");
            notificationProducer.sendNotification(notification);
            return new ResponseEntity<>("Email notification sent successfully!", HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to send email notification: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/sms")
    public ResponseEntity<String> sendSmsNotification(@RequestBody Notification notification) {
        try {
            notification.setType("SMS");
            notificationProducer.sendNotification(notification);
            return new ResponseEntity<>("SMS notification sent successfully!", HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to send SMS notification: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/push")
    public ResponseEntity<String> sendPushNotification(@RequestBody Notification notification) {
        try {
            notification.setType("PUSH");
            notificationProducer.sendNotification(notification);
            return new ResponseEntity<>("Push notification sent successfully!", HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to send push notification: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
