package ek.osnb.demo.ai.ollama;

import ek.osnb.demo.ai.AiProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@ConditionalOnProperty(prefix = "app.ai", name = "provider", havingValue = "ollama")
class OllamaApi {
    private static final Logger log = LoggerFactory.getLogger(OllamaApi.class);

    private final RestClient client;

    public OllamaApi(RestClient.Builder builder, AiProperties properties) {
        String baseUrl = properties.baseUrl();
        if (baseUrl == null || baseUrl.isBlank()) {
            throw new IllegalArgumentException("Ollama baseUrl must be provided in application.properties");
        }

        this.client = builder.clone()
                .baseUrl(baseUrl)
                .build();
    }

    public OllamaResponse getResponse(OllamaRequest request) {
        log.debug("Sending request to Ollama. model={}", request.model());

        return client.post()
                .uri("/api/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (_, res) -> {
                    log.warn("Received client error response from Ollama: {}", res.getStatusCode());
                    throw new RuntimeException("Client error: " + res.getStatusCode());
                })
                .onStatus(HttpStatusCode::is5xxServerError, (_, res) -> {
                    log.warn("Received server error response from Ollama: {}", res.getStatusCode());
                    throw new RuntimeException("Server error: " + res.getStatusCode());
                })
                .body(OllamaResponse.class);
    }
}

