package com.chatapp.controller;

import com.chatapp.entity.Message;
import com.chatapp.repository.MessageRepository;
import com.chatapp.service.AIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
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
    public void sendMessage(Message message) {

        // Save user message
        try {
            message.setTimestamp(LocalDateTime.now());
            messageRepository.save(message);
        } catch (Exception e) {
            System.out.println("❌ Error saving message: " + e.getMessage());
        }

        boolean isBotChat = message.getReceiver() != null &&
                            message.getReceiver().equalsIgnoreCase("bot");

        Map<String, Object> userResponse = new HashMap<>();
        userResponse.put("sender", message.getSender());
        userResponse.put("receiver", message.getReceiver());
        userResponse.put("content", message.getContent());
        userResponse.put("timestamp", LocalDateTime.now().toString());
        userResponse.put("suggestions", Collections.emptyList());

        if (isBotChat) {
            // ✅ Send only to sender (private)
            messagingTemplate.convertAndSendToUser(
                message.getSender(),
                "/queue/messages",
                userResponse
            );

            // AI auto reply
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

                    // ✅ Send AI reply only to sender (private)
                    messagingTemplate.convertAndSendToUser(
                        message.getSender(),
                        "/queue/messages",
                        botResponse
                    );
                } catch (Exception e) {
                    System.out.println("❌ AI reply error: " + e.getMessage());

                    Map<String, Object> fallback = new HashMap<>();
                    fallback.put("sender", "🤖 AI Bot");
                    fallback.put("receiver", message.getSender());
                    fallback.put("content", "Sorry, I am unavailable right now.");
                    fallback.put("timestamp", LocalDateTime.now().toString());
                    fallback.put("suggestions", Collections.emptyList());

                    messagingTemplate.convertAndSendToUser(
                        message.getSender(),
                        "/queue/messages",
                        fallback
                    );
                }
            }).start();

        } else {
            // Person-to-person — get AI suggestions
            try {
                List<String> suggestions = aiService.getSmartReplies(message.getContent());
                userResponse.put("suggestions", suggestions);
            } catch (Exception e) {
                System.out.println("❌ Suggestions error: " + e.getMessage());
                userResponse.put("suggestions", Collections.emptyList());
            }

            // ✅ Send to BOTH sender and receiver only (private)
            messagingTemplate.convertAndSendToUser(
                message.getSender(),
                "/queue/messages",
                userResponse
            );
            messagingTemplate.convertAndSendToUser(
                message.getReceiver(),
                "/queue/messages",
                userResponse
            );
        }
    }
}