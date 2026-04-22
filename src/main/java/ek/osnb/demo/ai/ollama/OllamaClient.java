package ek.osnb.demo.ai.ollama;

import ek.osnb.demo.ai.AiClient;
import ek.osnb.demo.ai.AiProperties;
import ek.osnb.demo.ai.AiRequest;
import ek.osnb.demo.ai.AiResponse;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Service
@ConditionalOnProperty(prefix = "app.ai", name = "provider", havingValue = "ollama")
class OllamaClient implements AiClient {
    private final OllamaApi ollamaApi;
    private final AiProperties properties;

    OllamaClient(OllamaApi ollamaApi, AiProperties properties) {
        this.ollamaApi = ollamaApi;
        this.properties = properties;
    }

    @Override
    public AiResponse generate(AiRequest request) {
        List<OllamaMessage> messages = new ArrayList<>();
        if (StringUtils.hasText(request.instructions())) {
            messages.add(new OllamaMessage("system", request.instructions()));
        }
        messages.add(new OllamaMessage("user", request.input()));

        OllamaRequest ollamaRequest = new OllamaRequest(
                properties.model(),
                messages,
                false
        );

        OllamaResponse response = ollamaApi.getResponse(ollamaRequest);
        String text = extractText(response);
        return new AiResponse(text.trim());
    }

    private String extractText(OllamaResponse response) {
        if (response == null || response.message() == null) {
            return "";
        }
        String content = response.message().content();
        return content != null ? content : "";
    }
}

