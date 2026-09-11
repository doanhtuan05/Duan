package com.web.app.service;

import java.time.Instant;
import java.util.Map;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class RealtimeService {
    private final SimpMessagingTemplate messagingTemplate;

    public RealtimeService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void publish(String type) {
        Object payload = Map.of("type", type, "at", Instant.now().toString());
        messagingTemplate.convertAndSend("/topic/updates", payload);
    }

    public void publishForCustomer(String type, Integer customerId) {
        Object payload = Map.of("type", type, "customerId", customerId, "at", Instant.now().toString());
        messagingTemplate.convertAndSend("/topic/updates", payload);
    }
}
