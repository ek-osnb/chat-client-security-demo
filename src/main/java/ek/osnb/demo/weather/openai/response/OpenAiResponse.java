package ek.osnb.demo.weather.openai.response;

import java.util.List;

public record OpenAiResponse(String id, List<OpenAiOutput> output) {}

