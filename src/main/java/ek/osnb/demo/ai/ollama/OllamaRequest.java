package ek.osnb.demo.ai.ollama;

import java.util.List;

record OllamaRequest(
        String model,
        List<OllamaMessage> messages,
        boolean stream
) {
}

record OllamaMessage(String role, String content) {
}

