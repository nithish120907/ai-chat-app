package com.chatapp.controller;

import com.chatapp.entity.Message;
import com.chatapp.repository.MessageRepository;
import com.chatapp.service.AIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;
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
    public void sendMessage(Message message, Principal principal) {

        String sender = message.getSender().toLowerCase();
        String receiver = message.getReceiver().toLowerCase();

        System.out.println("📨 Principal: " + (principal != null ? principal.getName() : "NULL"));
        System.out.println("📨 Sender: " + sender + " → Receiver: " + receiver);

        try {
            message.setSender(sender);
            message.setReceiver(receiver);
            message.setTimestamp(LocalDateTime.now());
            messageRepository.save(message);
        } catch (Exception e) {
            System.out.println("❌ Error saving: " + e.getMessage());
        }

        boolean isBotChat = receiver.equalsIgnoreCase("bot");

        Map<String, Object> userResponse = new HashMap<>();
        userResponse.put("sender", sender);
        userResponse.put("receiver", receiver);
        userResponse.put("content", message.getContent());
        userResponse.put("timestamp", LocalDateTime.now().toString());
        userResponse.put("suggestions", Collections.emptyList());

        if (isBotChat) {
            // Send to sender via both private queue AND personal topic
            messagingTemplate.convertAndSendToUser(sender, "/queue/messages", userResponse);
            messagingTemplate.convertAndSend("/topic/chat." + sender, userResponse);

            new Thread(() -> {
                try {
                    Thread.sleep(500);
                    String aiReply = aiService.getSmartReply(message.getContent());

                    Map<String, Object> botResponse = new HashMap<>();
                    botResponse.put("sender", "🤖 AI Bot");
                    botResponse.put("receiver", sender);
                    botResponse.put("content", aiReply);
                    botResponse.put("timestamp", LocalDateTime.now().toString());
                    botResponse.put("suggestions", Collections.emptyList());

                    messagingTemplate.convertAndSendToUser(sender, "/queue/messages", botResponse);
                    messagingTemplate.convertAndSend("/topic/chat." + sender, botResponse);
                } catch (Exception e) {
                    System.out.println("❌ AI error: " + e.getMessage());
                    Map<String, Object> fallback = new HashMap<>();
                    fallback.put("sender", "🤖 AI Bot");
                    fallback.put("receiver", sender);
                    fallback.put("content", "Sorry, I am unavailable right now.");
                    fallback.put("timestamp", LocalDateTime.now().toString());
                    fallback.put("suggestions", Collections.emptyList());
                    messagingTemplate.convertAndSendToUser(sender, "/queue/messages", fallback);
                    messagingTemplate.convertAndSend("/topic/chat." + sender, fallback);
                }
            }).start();

        } else {
            try {
                List<String> suggestions = aiService.getSmartReplies(message.getContent());
                userResponse.put("suggestions", suggestions);
            } catch (Exception e) {
                userResponse.put("suggestions", Collections.emptyList());
            }

            System.out.println("📤 Sending to: " + sender + " and " + receiver);

            // ✅ Send via BOTH private queue AND personal topic (fallback)
            messagingTemplate.convertAndSendToUser(sender, "/queue/messages", userResponse);
            messagingTemplate.convertAndSend("/topic/chat." + sender, userResponse);

            messagingTemplate.convertAndSendToUser(receiver, "/queue/messages", userResponse);
            messagingTemplate.convertAndSend("/topic/chat." + receiver, userResponse);
        }
    }
}