package ek.osnb.demo.weather.weatherapi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
class OpenMeteoWeatherClient implements WeatherClient {
    private static final Logger log = LoggerFactory.getLogger(OpenMeteoWeatherClient.class);
    private final RestClient weatherClient;

    public OpenMeteoWeatherClient(@Qualifier("weatherRestClient") RestClient weatherClient) {
        this.weatherClient = weatherClient;
    }


    public WeatherResponse getWeather(double lat, double lon) {
        // forecast?latitude=56.16&longitude=10.20&current_weather=true
        log.debug("Getting weather for lat: {}, lon: {}", lat, lon);

        return weatherClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/forecast")
                        .queryParam("latitude", lat)
                        .queryParam("longitude", lon)
                        .queryParam("current_weather", true)
                        .build())
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (_, res) -> {
                    log.warn("Received client error response from weather API: {}", res.getStatusCode());
                    throw new RuntimeException("Client error: " + res.getStatusCode());
                })
                .onStatus(HttpStatusCode::is5xxServerError, (_, res) -> {
                    log.warn("Received server error response from weather API: {}", res.getStatusCode());
                    throw new RuntimeException("Server error (weather service): " + res.getStatusCode());
                })
                .body(WeatherResponse.class);
    }
}
