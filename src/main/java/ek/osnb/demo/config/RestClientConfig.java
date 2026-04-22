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
    RestClient weatherRestClient(
            RestClient.Builder b,
            @Value("${external.api.weather.baseUrl}") String baseUrl
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
            @Value("${external.api.geocoding.baseUrl}") String baseUrl
    ) {

        if (baseUrl == null || baseUrl.isBlank()) {
            throw new IllegalArgumentException("External API baseUrl must be provided in application.properties");
        }

        return b.clone()
                .baseUrl(baseUrl)
                .build();
    }
}
