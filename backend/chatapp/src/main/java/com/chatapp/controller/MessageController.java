package com.chatapp.controller;

import com.chatapp.entity.Message;
import com.chatapp.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/messages")
public class MessageController {

    @Autowired
    private MessageRepository messageRepository;

    // Get chat history between two users
    @GetMapping("/history")
    public List<Message> getChatHistory(
            @RequestParam String user1,
            @RequestParam String user2) {

        return messageRepository
            .findBySenderAndReceiverOrSenderAndReceiverOrderByTimestampAsc(
                user1, user2, user2, user1
            );
    }
}