package ek.osnb.demo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

@Configuration
class RestClientConfig {

    @Bean
    RestClient.Builder restClientBuilder() {
        return RestClient.builder()
                // Rate limiting, retries, timeouts etc. can be configured here
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);

    }

    @Bean
    RestClient openAiRestClient(
            RestClient.Builder builder,
            @Value("${chatgpt.api.key}") String apiKey,
            @Value("${chatgpt.api.baseUrl}") String baseUrl
    ) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalArgumentException("OpenAI API-key must be provided in application.properties");
        }

        if (baseUrl == null || baseUrl.isBlank()) {
            throw new IllegalArgumentException("OpenAI API baseUrl must be provided in application.properties");
        }

        return builder.clone()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .build();
    }

    @Bean
    RestClient weatherRestClient(
            RestClient.Builder b,
            @Value("${weather.api.baseUrl}") String baseUrl
    ) {

        if (baseUrl == null || baseUrl.isBlank()) {
            throw new IllegalArgumentException("External API baseUrl must be provided in application.properties");
        }

        return b.clone()
                .baseUrl(baseUrl)
                .build();
    }

    @Bean
    RestClient geoCodingRestClient(
            RestClient.Builder b,
            @Value("${geocoding.api.baseUrl}") String baseUrl
    ) {

        if (baseUrl == null || baseUrl.isBlank()) {
            throw new IllegalArgumentException("External API baseUrl must be provided in application.properties");
        }

        return b.clone()
                .baseUrl(baseUrl)
                .build();
    }
}
