package com.chatapp.controller;

import com.chatapp.entity.Message;
import com.chatapp.repository.MessageRepository;
import com.chatapp.service.AIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class ChatController {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private AIService aiService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat")
    @SendTo("/topic/messages")
    public Map<String, Object> sendMessage(Message message) {

        // Save user message
        message.setTimestamp(LocalDateTime.now());
        messageRepository.save(message);

        boolean isBotChat = message.getReceiver() != null &&
                            message.getReceiver().equalsIgnoreCase("bot");

        Map<String, Object> userResponse = new HashMap<>();
        userResponse.put("sender", message.getSender());
        userResponse.put("receiver", message.getReceiver());
        userResponse.put("content", message.getContent());
        userResponse.put("timestamp", message.getTimestamp().toString());

        if (isBotChat) {
            // Bot chat — no suggestions, AI will auto reply
            userResponse.put("suggestions", Collections.emptyList());

            // Send AI auto reply after 0.5s delay
            new Thread(() -> {
                try {
                    Thread.sleep(500);
                    String aiReply = aiService.getSmartReply(message.getContent());

                    Map<String, Object> botResponse = new HashMap<>();
                    botResponse.put("sender", "🤖 AI Bot");
                    botResponse.put("receiver", message.getSender());
                    botResponse.put("content", aiReply);
                    botResponse.put("timestamp", LocalDateTime.now().toString());
                    botResponse.put("suggestions", Collections.emptyList());

                    messagingTemplate.convertAndSend("/topic/messages", botResponse);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();

        } else {
            // Person-to-person chat — show AI suggestions
            List<String> suggestions = aiService.getSmartReplies(message.getContent());
            userResponse.put("suggestions", suggestions);
        }

        return userResponse;
    }
}