package ek.osnb.demo.weather;


import ek.osnb.demo.weather.geoapi.GeocodingClient;
import ek.osnb.demo.weather.openai.OpenAiChatClient;
import ek.osnb.demo.weather.weatherapi.WeatherClient;
import ek.osnb.demo.weather.geoapi.Location;
import ek.osnb.demo.weather.openai.request.OpenAiModel;
import ek.osnb.demo.weather.openai.request.OpenAiRequest;
import ek.osnb.demo.weather.openai.response.OpenAiContent;
import ek.osnb.demo.weather.openai.response.OpenAiResponse;
import ek.osnb.demo.weather.weatherapi.WeatherResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;


@Service
public class AiWeatherService {
    private static final Logger log = LoggerFactory.getLogger(AiWeatherService.class);
    private final OpenAiChatClient openAiClient;
    private final GeocodingClient geoClient;
    private final WeatherClient weatherClient;

    private final Integer MAX_OUTPUT_TOKENS = 800;
    private static final String FALLBACK_MESSAGE = "I'm sorry, I can't help you with that.";
    private static final String CITY_EXTRACTION_INSTRUCTIONS = """
            Extract the city from the user's message.
            
            Rules:
            - Return only the city name.
            - If no city is present, return exactly: NONE
            - Do not include explanation, punctuation, markdown, or extra words.
            - If multiple cities are present, return the most relevant one for a weather request.
            """;

    private static final String ANSWER_INSTRUCTIONS = """
            You are a weather assistant.
            
            You will be given:
            1. The user's original question
            2. Trusted weather data from an external API
            
            Rules:
            - Answer only using the trusted weather data provided.
            - Do not invent details.
            - If the weather data is insufficient to answer, respond exactly:
              I'm sorry, I can't help you with that.
            - Keep the answer concise and user-friendly.
            """;

    public AiWeatherService(OpenAiChatClient openAiClient, GeocodingClient geoClient, WeatherClient weatherClient) {
        this.openAiClient = openAiClient;
        this.geoClient = geoClient;
        this.weatherClient = weatherClient;
    }


    public record ResponseDto(String response) {
    }

    public ResponseDto prompt(String prompt) {
        if (prompt == null || prompt.isBlank()) {
            return new ResponseDto(FALLBACK_MESSAGE);
        }

        try {
            log.debug("Extracting city from prompt: {}", prompt);
            Optional<String> city = extractCityFromPrompt(prompt);
            if (city.isEmpty()) {
                log.warn("No city extracted from prompt: {}", prompt);
                return new ResponseDto(FALLBACK_MESSAGE);
            }

            Location location = geoClient.getLatLon(city.get());
            if (location == null) {
                log.warn("Geo lookup returned null for city={}", city.get());
                return new ResponseDto(FALLBACK_MESSAGE);
            }

            WeatherResponse weather = weatherClient.getWeather(location.latitude(), location.longitude());
            if (weather == null) {
                log.warn("Weather lookup returned null for city={}, lat={}, lon={}",
                        city.get(), location.latitude(), location.longitude());
                return new ResponseDto(FALLBACK_MESSAGE);
            }

            String enrichedInput = buildEnrichedInput(prompt, city.get(), weather);

            var request = new OpenAiRequest(
                    OpenAiModel.GPT_4_1,
                    enrichedInput,
                    ANSWER_INSTRUCTIONS,
                    MAX_OUTPUT_TOKENS);

            log.debug("Sending weather answer request to OpenAI for city={}, prompt={}", city.get(), prompt);
            OpenAiResponse aiResponse = openAiClient.getResponse(request);
            String text = extractText(aiResponse);

            if (!StringUtils.hasText(text)) {
                return new ResponseDto(FALLBACK_MESSAGE);
            }

            return new ResponseDto(text.trim());
        } catch (Exception ex) {
            log.error("Error processing prompt: {}", prompt, ex);
            return new ResponseDto(FALLBACK_MESSAGE);
        }
    }

    private Optional<String> extractCityFromPrompt(String prompt) {
//        String instructions = """
//                Extract the city name from the following user question.
//                If no city name is mentioned, respond with "No city mentioned".
//                If there are a city name mentioned, responsd with only the city name, without any additional text or formatting.
//                """;
        var request = new OpenAiRequest(
                OpenAiModel.GPT_4_1,
                prompt,
                CITY_EXTRACTION_INSTRUCTIONS,
                50
        );
        OpenAiResponse response = openAiClient.getResponse(request);
        String extracted = extractText(response).trim();
        if (!StringUtils.hasText(extracted) || "NONE".equalsIgnoreCase(extracted)) {
            return Optional.empty();
        }

        return Optional.of(extracted);
    }


    private ResponseDto toDto(OpenAiResponse response) {
        String text = response.output().stream()
                .flatMap(output -> output.content().stream())
                .map(OpenAiContent::text)
                .filter(Objects::nonNull)
                .collect(Collectors.joining("\n"));

        return new ResponseDto(text);
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
                .filter(StringUtils::hasText)
                .collect(Collectors.joining("\n"));
    }

    private String buildEnrichedInput(String userPrompt, String city, WeatherResponse weather) {
        return """
                User question:
                %s
                
                Resolved city:
                %s
                
                Trusted weather data:
                %s
                """.formatted(
                sanitize(userPrompt),
                sanitize(city),
                sanitize(formatWeatherData(weather))
        );
    }

    private String sanitize(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\u0000", "").trim();
    }

    private String formatWeatherData(WeatherResponse weather) {
        return """
                Temperature: %s
                Wind speed (km/h): %s
                """.formatted(
                weather.currentWeather().temperature(),
                weather.currentWeather().windspeed()
        );
    }

}
