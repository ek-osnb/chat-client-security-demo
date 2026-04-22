package ek.osnb.demo.ai.openai;

import ek.osnb.demo.ai.AiClient;
import ek.osnb.demo.ai.AiProperties;
import ek.osnb.demo.ai.AiRequest;
import ek.osnb.demo.ai.AiResponse;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@ConditionalOnProperty(prefix = "app.ai", name = "provider", havingValue = "openai")
class OpenAiClient implements AiClient {
    private final OpenAiApi openAiApi;
    private final AiProperties properties;

    OpenAiClient(OpenAiApi openAiApi, AiProperties properties) {
        this.openAiApi = openAiApi;
        this.properties = properties;
    }

    @Override
    public AiResponse generate(AiRequest request) {
        OpenAiRequest openAiRequest = new OpenAiRequest(
                properties.model(),
                request.input(),
                request.instructions(),
                this.getMaxTokens(request.maxOutputTokens())
        );
        OpenAiResponse response = openAiApi.getResponse(openAiRequest);
        String text = extractText(response);
        return new AiResponse(text.trim());
    }

    private String extractText(OpenAiResponse response) {
        if (response == null || response.output() == null) {
            return "";
        }

        return response.output().stream()
                .filter(Objects::nonNull)
                .flatMap(output -> output.content().stream())
                .filter(Objects::nonNull)
                .map(OpenAiContent::text)
                .filter(org.springframework.util.StringUtils::hasText)
                .collect(java.util.stream.Collectors.joining("\n"));
    }

    private int getMaxTokens(int requestedMaxTokens) {
        if (requestedMaxTokens > 0 && requestedMaxTokens <= properties.maxOutputTokens()) {
            return requestedMaxTokens;
        }
        return properties.maxOutputTokens();

    }
}
