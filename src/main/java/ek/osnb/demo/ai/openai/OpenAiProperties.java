package ek.osnb.demo.ai.openai;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.ai.openai")
public record OpenAiProperties(
        String model,
        Integer maxOutputTokens
) {}
