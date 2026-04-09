package com.chatapp.config;

import com.chatapp.service.OnlineUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Component
public class WebSocketEventListener {

    @Autowired
    private OnlineUserService onlineUserService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @EventListener
    public void handleConnect(SessionConnectedEvent event) {
        String username = event.getUser() != null ? event.getUser().getName() : null;
        if (username != null) {
            onlineUserService.addUser(username);
            System.out.println("🟢 User connected: " + username);
            // Broadcast updated online users to everyone
            messagingTemplate.convertAndSend("/topic/online-users",
                onlineUserService.getOnlineUsers());
        }
    }

    @EventListener
    public void handleDisconnect(SessionDisconnectEvent event) {
        String username = event.getUser() != null ? event.getUser().getName() : null;
        if (username != null) {
            onlineUserService.removeUser(username);
            System.out.println("🔴 User disconnected: " + username);
            // Broadcast updated online users to everyone
            messagingTemplate.convertAndSend("/topic/online-users",
                onlineUserService.getOnlineUsers());
        }
    }
}