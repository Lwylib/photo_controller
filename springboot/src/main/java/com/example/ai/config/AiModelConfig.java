package com.example.ai.config;

import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;

@Configuration
public class AiModelConfig {
    private static final Logger log = LoggerFactory.getLogger(AiModelConfig.class);

    private static final String DASHSCOPE_BASE_URL = "https://dashscope.aliyuncs.com/compatible-mode/v1";
    private static final String DEEPSEEK_BASE_URL = "https://api.deepseek.com/v1";

    @Autowired
    private AiModelProperties props;

    @Bean
    public StreamingChatLanguageModel streamingChatLanguageModel() {
        String provider = props.getProvider();
        log.info("Initializing AI model, provider={}, model={}", provider, props.getModelName());

        switch (provider.toLowerCase()) {
            case "dashscope":
                return OpenAiStreamingChatModel.builder()
                        .baseUrl(DASHSCOPE_BASE_URL)
                        .apiKey(props.getApiKey())
                        .modelName(props.getModelName())
                        .maxTokens(props.getMaxTokens())
                        .temperature(props.getTemperature())
                        .timeout(Duration.ofSeconds(120))
                        .build();
            case "openai":
                return OpenAiStreamingChatModel.builder()
                        .apiKey(props.getApiKey())
                        .modelName(props.getModelName())
                        .maxTokens(props.getMaxTokens())
                        .temperature(props.getTemperature())
                        .timeout(Duration.ofSeconds(120))
                        .build();
            case "deepseek":
                return OpenAiStreamingChatModel.builder()
                        .baseUrl(DEEPSEEK_BASE_URL)
                        .apiKey(props.getApiKey())
                        .modelName(props.getModelName())
                        .maxTokens(props.getMaxTokens())
                        .temperature(props.getTemperature())
                        .timeout(Duration.ofSeconds(120))
                        .build();
            default:
                throw new IllegalArgumentException(
                        "不支持的 AI provider: " + provider + "，可选值: dashscope / openai / deepseek / ollama");
        }
    }
}
