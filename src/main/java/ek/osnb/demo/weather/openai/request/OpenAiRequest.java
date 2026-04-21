package ek.osnb.demo.weather.openai.request;

public record OpenAiRequest(
        OpenAiModel model,
        String input,
        String instructions,
        Integer max_output_tokens
) {
}

