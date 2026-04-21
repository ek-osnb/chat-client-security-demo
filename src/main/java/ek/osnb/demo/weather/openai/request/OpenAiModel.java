package ek.osnb.demo.weather.openai.request;

import com.fasterxml.jackson.annotation.JsonValue;

public enum OpenAiModel {
    GPT_4_1("gpt-4.1"),
    GPT_5_2("gpt-5.2");

    private final String modelName;

    OpenAiModel(String modelName) {
        this.modelName = modelName;
    }
    @JsonValue
    public String getModelName() {
        return modelName;
    }
}
