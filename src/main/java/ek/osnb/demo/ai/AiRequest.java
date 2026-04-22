package ek.osnb.demo.ai;

public record AiRequest(
        String input,
        String instructions,
        Integer maxOutputTokens
) {
}
