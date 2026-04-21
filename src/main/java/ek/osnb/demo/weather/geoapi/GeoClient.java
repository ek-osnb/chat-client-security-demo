package ek.osnb.demo.weather.geoapi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
class GeoClient implements GeocodingClient {
    private static final Logger log = LoggerFactory.getLogger(GeoClient.class);
    private final RestClient geoClient;

    public GeoClient(@Qualifier("geoCodingRestClient") RestClient geoClient) {
        this.geoClient = geoClient;
    }


    public Location getLatLon(String city) {
        // ADD ?name={city}&count=1&language=en&format=json
        log.debug("Getting lat/lon for city: {}", city);
        GeoResult geoResult = geoClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/search")
                        .queryParam("name", city)
                        .queryParam("count", 1)
                        .queryParam("language", "en")
                        .queryParam("format", "json")
                        .build())
                .retrieve()
                .onStatus(status -> status.value() == 404, (_, res) -> {
                    log.warn("City not found: {}. Received 404 from geocoding API.", city);
                    throw new RuntimeException("City not found: " + city);
                })
                .onStatus(HttpStatusCode::is5xxServerError, (_, res) -> {
                    log.warn("Received server error response from OpenAI: {}", res.getStatusCode());
                    throw new RuntimeException("Server error: " + res.getStatusCode());
                })
                .body(GeoResult.class);
        if (geoResult == null || geoResult.results() == null || geoResult.results().isEmpty()) {
            throw new RuntimeException("City not found: " + city);
        }
        return geoResult.results().getFirst();
    }
}
