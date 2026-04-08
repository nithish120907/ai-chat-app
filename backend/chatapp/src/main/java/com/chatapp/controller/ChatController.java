package com.chatapp.controller;

import com.chatapp.entity.Message;
import com.chatapp.repository.MessageRepository;
import com.chatapp.service.AIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class ChatController {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private AIService aiService;

    @MessageMapping("/chat")
    @SendTo("/topic/messages")
    public Map<String, Object> sendMessage(Message message) {
        message.setTimestamp(LocalDateTime.now());
        messageRepository.save(message);

        List<String> suggestions = aiService.getSmartReplies(message.getContent());

        Map<String, Object> response = new HashMap<>();
        response.put("sender", message.getSender());
        response.put("receiver", message.getReceiver());
        response.put("content", message.getContent());
        response.put("timestamp", message.getTimestamp().toString());
        response.put("suggestions", suggestions);

        return response;
    }
}
