package ek.osnb.demo.ai.openai;

import ek.osnb.demo.ai.AiProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@ConditionalOnProperty(prefix = "app.ai", name = "provider", havingValue = "openai")
class OpenAiApi {
    private static final Logger log = LoggerFactory.getLogger(OpenAiApi.class);
    private final RestClient client;

    public OpenAiApi(RestClient.Builder builder, AiProperties properties) {
        String apiKey = properties.apiKey();
        String baseUrl = properties.baseUrl();
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalArgumentException("OpenAI API-key must be provided in application.properties");
        }

        if (baseUrl == null || baseUrl.isBlank()) {
            throw new IllegalArgumentException("OpenAI API baseUrl must be provided in application.properties");
        }

        this.client = builder.clone()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .build();
    }

    public OpenAiResponse getResponse(OpenAiRequest request) {
        log.debug("Sending request to OpenAI. model={}, max_output_tokens={}",
                request.model(), request.maxOutputTokens());

        return client.post()
                .uri("/v1/responses")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (_, res) -> {
                    log.warn("Received client error response from OpenAI: {}", res.getStatusCode());
                    throw new RuntimeException("Client error: " + res.getStatusCode());
                })
                .onStatus(HttpStatusCode::is5xxServerError, (_, res) -> {
                    log.warn("Received server error response from OpenAI: {}", res.getStatusCode());
                    throw new RuntimeException("Server error: " + res.getStatusCode());
                })
                .body(OpenAiResponse.class);
    }
}
