package com.chatapp.controller;

import com.chatapp.service.OnlineUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/users")
public class OnlineUserController {

    @Autowired
    private OnlineUserService onlineUserService;

    @GetMapping("/online")
    public Set<String> getOnlineUsers() {
        return onlineUserService.getOnlineUsers();
    }
}