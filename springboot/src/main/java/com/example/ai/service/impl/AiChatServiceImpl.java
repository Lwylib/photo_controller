package com.example.ai.service.impl;

import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import com.example.ai.config.AiModelProperties;
import com.example.ai.domain.AiConversation;
import com.example.ai.domain.AiMessage;
import com.example.ai.mapper.AiChatMapper;
import com.example.ai.service.IAiChatService;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.StreamingResponseHandler;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.output.Response;

@Service
public class AiChatServiceImpl implements IAiChatService {
    private static final Logger log = LoggerFactory.getLogger(AiChatServiceImpl.class);

    @Autowired private StreamingChatLanguageModel streamingModel;
    @Autowired private AiModelProperties modelProps;
    @Autowired private AiChatMapper aiChatMapper;

    @Override
    public AiConversation createConversation(Long userId, String model) {
        AiConversation conv = new AiConversation();
        conv.setUserId(userId);
        conv.setTitle("新对话");
        conv.setModel(model == null ? modelProps.getModelName() : model);
        conv.setStatus(1);
        aiChatMapper.insertConversation(conv);
        return conv;
    }

    @Override
    public List<AiConversation> listConversations(Long userId) {
        List<AiConversation> list = aiChatMapper.selectConversationsByUserId(userId);
        return list != null ? list : new ArrayList<>();
    }

    @Override
    public List<AiMessage> listMessages(Long conversationId, Long userId) {
        AiConversation conv = aiChatMapper.selectConversationById(conversationId, userId);
        if (conv == null) return new ArrayList<>();
        return aiChatMapper.selectMessagesByConversationId(conversationId);
    }

    @Override
    public int renameConversation(Long id, String title, Long userId) {
        return aiChatMapper.updateConversationTitle(id, title, userId);
    }

    @Override
    public int deleteConversation(Long id, Long userId) {
        aiChatMapper.deleteMessagesByConversationId(id);
        return aiChatMapper.deleteConversation(id, userId);
    }

    @Override
    public void chat(Long conversationId, String userInput, Long userId, SseEmitter emitter) {
        AiConversation conv = aiChatMapper.selectConversationById(conversationId, userId);
        if (conv == null) {
            sendSse(emitter, "error", "会话不存在或无权限");
            return;
        }
        AiMessage userMsg = new AiMessage();
        userMsg.setConversationId(conversationId);
        userMsg.setRole("user");
        userMsg.setContent(userInput);
        aiChatMapper.insertMessage(userMsg);

        if ("新对话".equals(conv.getTitle())) {
            String autoTitle = userInput.length() > 15 ? userInput.substring(0, 15) + "…" : userInput;
            aiChatMapper.updateConversationTitle(conversationId, autoTitle, userId);
        }

        List<ChatMessage> messages = buildMessages(conversationId);
        StringBuilder fullReply = new StringBuilder();

        streamingModel.generate(messages, new StreamingResponseHandler<dev.langchain4j.data.message.AiMessage>() {
            @Override
            public void onNext(String token) {
                fullReply.append(token);
                sendSse(emitter, "message", token);
            }
            @Override
            public void onComplete(Response<dev.langchain4j.data.message.AiMessage> response) {
                if (fullReply.length() > 0) {
                    AiMessage aiMsg = new AiMessage();
                    aiMsg.setConversationId(conversationId);
                    aiMsg.setRole("assistant");
                    aiMsg.setContent(fullReply.toString());
                    if (response.tokenUsage() != null) {
                        aiMsg.setTokens(response.tokenUsage().totalTokenCount());
                    }
                    aiChatMapper.insertMessage(aiMsg);
                }
                sendSse(emitter, "done", "[DONE]");
                emitter.complete();
            }
            @Override
            public void onError(Throwable error) {
                log.error("LangChain4j error", error);
                sendSse(emitter, "error", "AI service error: " + error.getMessage());
                emitter.completeWithError(error);
            }
        });
    }

    private List<ChatMessage> buildMessages(Long conversationId) {
        List<AiMessage> history = aiChatMapper.selectMessagesByConversationId(conversationId);
        List<ChatMessage> list = new ArrayList<>();
        String system = modelProps.getSystemPrompt();
        if (system != null && !system.isEmpty()) list.add(SystemMessage.from(system));
        int max = modelProps.getMaxHistoryMessages();
        int start = Math.max(0, history.size() - max);
        for (int i = start; i < history.size(); i++) {
            AiMessage m = history.get(i);
            if ("user".equals(m.getRole())) list.add(UserMessage.from(m.getContent()));
            else list.add(dev.langchain4j.data.message.AiMessage.from(m.getContent()));
        }
        return list;
    }

    private void sendSse(SseEmitter emitter, String event, String data) {
        try { emitter.send(SseEmitter.event().name(event).data(data)); }
        catch (Exception e) { log.warn("SSE send failed: event={}", event); }
    }
}
