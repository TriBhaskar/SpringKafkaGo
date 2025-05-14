package com.tribhaskar.springkafkago.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

    private String id;
    private String recipient;
    private String subject;
    private String content;
    private String type; // SMS, EMAIL, PUSH, etc.
    private LocalDateTime timestamp;
    private boolean read;

    // Additional constructor for convenience
    public Notification(String recipient, String subject, String content, String type) {
        this.recipient = recipient;
        this.subject = subject;
        this.content = content;
        this.type = type;
        this.timestamp = LocalDateTime.now();
        this.read = false;
    }
}
