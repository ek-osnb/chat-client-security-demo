package ek.osnb.demo.ai.openai;

import com.fasterxml.jackson.annotation.JsonProperty;

record OpenAiRequest(
        String model,
        String input,
        String instructions,
        @JsonProperty("max_output_tokens")
        Integer maxOutputTokens
) {
}

