package ek.osnb.demo.ai;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.ai")
public record AiProperties(
        String provider,
        String baseUrl,
        String apiKey,
        String model,
        Integer maxOutputTokens) {
}
