package ek.osnb.demo.weather.openai;

import ek.osnb.demo.weather.openai.request.OpenAiRequest;
import ek.osnb.demo.weather.openai.response.OpenAiResponse;

public interface OpenAiChatClient {
    OpenAiResponse getResponse(OpenAiRequest request);
}
