package com.example.ai.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "ai.model")
public class AiModelProperties {
    private String provider = "dashscope";
    private String apiKey;
    private String modelName = "qwen-plus";
    private String baseUrl = "http://localhost:11434";
    private Integer maxTokens = 2048;
    private Double temperature = 0.7;
    private Integer maxHistoryMessages = 20;
    private String systemPrompt = "你是一个专业、友好的 AI 助手，请用中文回答问题。";

    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }
    public String getApiKey() { return apiKey; }
    public void setApiKey(String apiKey) { this.apiKey = apiKey; }
    public String getModelName() { return modelName; }
    public void setModelName(String modelName) { this.modelName = modelName; }
    public String getBaseUrl() { return baseUrl; }
    public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
    public Integer getMaxTokens() { return maxTokens; }
    public void setMaxTokens(Integer maxTokens) { this.maxTokens = maxTokens; }
    public Double getTemperature() { return temperature; }
    public void setTemperature(Double temperature) { this.temperature = temperature; }
    public Integer getMaxHistoryMessages() { return maxHistoryMessages; }
    public void setMaxHistoryMessages(Integer maxHistoryMessages) { this.maxHistoryMessages = maxHistoryMessages; }
    public String getSystemPrompt() { return systemPrompt; }
    public void setSystemPrompt(String systemPrompt) { this.systemPrompt = systemPrompt; }
}
