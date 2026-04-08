package com.chatapp.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class AIService {

    @Value("${huggingface.api.key:disabled}")
    private String apiKey;

    private static final Map<String, List<String>> REPLIES = new HashMap<>();

    static {
        REPLIES.put("hi",          Arrays.asList("Hey! How's it going?", "Hello there!", "Hi! 👋"));
        REPLIES.put("hello",       Arrays.asList("Hey!", "Hello! How are you?", "Hi there!"));
        REPLIES.put("how are you", Arrays.asList("I'm good, thanks!", "Doing well! You?", "Great, how about you?"));
        REPLIES.put("bye",         Arrays.asList("Goodbye! 👋", "See you later!", "Take care!"));
        REPLIES.put("thanks",      Arrays.asList("You're welcome!", "No problem!", "Anytime! 😊"));
        REPLIES.put("ok",          Arrays.asList("Sounds good!", "Got it!", "Alright!"));
        REPLIES.put("yes",         Arrays.asList("Great!", "Awesome!", "Perfect!"));
        REPLIES.put("no",          Arrays.asList("Okay, got it.", "No worries!", "Understood."));
        REPLIES.put("help",        Arrays.asList("Sure, what do you need?", "I'm here to help!", "Tell me more!"));
        REPLIES.put("good",        Arrays.asList("Glad to hear it! 😊", "That's great!", "Awesome!"));
        REPLIES.put("bad",         Arrays.asList("Sorry to hear that.", "Hope it gets better!", "I'm here if you need to talk."));
        REPLIES.put("lol",         Arrays.asList("😄", "Haha!", "That's funny!"));
        REPLIES.put("sure",        Arrays.asList("Great!", "Let's do it!", "Sounds good!"));
        REPLIES.put("what",        Arrays.asList("Can you elaborate?", "Tell me more!", "I'm listening."));
        REPLIES.put("why",         Arrays.asList("Good question!", "Let me think...", "That's a deep one!"));
    }

    public List<String> getSmartReplies(String message) {
        String lower = message.toLowerCase().trim();
        for (Map.Entry<String, List<String>> entry : REPLIES.entrySet()) {
            if (lower.contains(entry.getKey())) {
                return entry.getValue();
            }
        }
        return Arrays.asList("Okay!", "Got it!", "Sounds good!");
    }

    public String getSmartReply(String message) {
        List<String> replies = getSmartReplies(message);
        return replies.get(new Random().nextInt(replies.size()));
    }
}