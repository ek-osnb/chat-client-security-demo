package ek.osnb.demo.weather.openai.response;

import java.util.List;

public record OpenAiOutput(String id, List<OpenAiContent> content) {}
