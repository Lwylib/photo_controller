package com.example.ai.controller;

import com.example.ai.domain.AiConversation;
import com.example.ai.domain.AiMessage;
import com.example.ai.service.IAiChatService;
import com.example.common.Result;
import com.example.entity.Account;
import com.example.utils.TokenUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@RestController
@RequestMapping("/ai/chat")
public class AiChatController {

    @Autowired
    private IAiChatService aiChatService;

    @GetMapping("/conversations")
    public Result listConversations() {
        Account user = TokenUtils.getCurrentUser();
        List<AiConversation> list = aiChatService.listConversations(Long.valueOf(user.getId()));
        return Result.success(list);
    }

    @PostMapping("/conversations")
    public Result createConversation(@RequestParam(required = false) String model) {
        Account user = TokenUtils.getCurrentUser();
        AiConversation conv = aiChatService.createConversation(Long.valueOf(user.getId()), model);
        return Result.success(conv);
    }

    @PutMapping("/conversations/{id}/title")
    public Result renameConversation(@PathVariable Long id, @RequestParam String title) {
        Account user = TokenUtils.getCurrentUser();
        aiChatService.renameConversation(id, title, Long.valueOf(user.getId()));
        return Result.success();
    }

    @DeleteMapping("/conversations/{id}")
    public Result deleteConversation(@PathVariable Long id) {
        Account user = TokenUtils.getCurrentUser();
        aiChatService.deleteConversation(id, Long.valueOf(user.getId()));
        return Result.success();
    }

    @GetMapping("/conversations/{id}/messages")
    public Result listMessages(@PathVariable Long id) {
        Account user = TokenUtils.getCurrentUser();
        List<AiMessage> messages = aiChatService.listMessages(id, Long.valueOf(user.getId()));
        return Result.success(messages);
    }

    @GetMapping(value = "/stream", produces = "text/event-stream;charset=UTF-8")
    public SseEmitter stream(@RequestParam Long conversationId, @RequestParam String message) {
        SseEmitter emitter = new SseEmitter(0L);
        Account user = TokenUtils.getCurrentUser();
        Long userId = Long.valueOf(user.getId());
        new Thread(() -> aiChatService.chat(conversationId, message, userId, emitter)).start();
        return emitter;
    }
}
