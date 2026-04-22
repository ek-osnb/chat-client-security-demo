package ek.osnb.demo.ai.openai;

import java.util.List;

record OpenAiResponse(String id, List<OpenAiOutput> output) {}
record OpenAiOutput(String id, List<OpenAiContent> content) {}
record OpenAiContent(String text) {}

