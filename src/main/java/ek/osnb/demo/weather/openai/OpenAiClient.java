package ek.osnb.demo.weather.openai;

import ek.osnb.demo.weather.openai.request.OpenAiRequest;
import ek.osnb.demo.weather.openai.response.OpenAiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
class OpenAiClient implements OpenAiChatClient {
    private static final Logger log = LoggerFactory.getLogger(OpenAiClient.class);
    private final RestClient client;

    public OpenAiClient(@Qualifier("openAiRestClient") RestClient client) {
        this.client = client;
    }

    public OpenAiResponse getResponse(OpenAiRequest request) {
        log.debug("Sending request to OpenAI. model={}, max_output_tokens={}",
                request.model(), request.max_output_tokens());
        return client.post()
                .uri("/responses")
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
