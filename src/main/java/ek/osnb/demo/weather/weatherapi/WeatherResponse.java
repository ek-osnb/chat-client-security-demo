package ek.osnb.demo.weather.weatherapi;

import com.fasterxml.jackson.annotation.JsonAlias;

public record WeatherResponse(
        @JsonAlias("current_weather")
        CurrentWeather currentWeather
) {}
