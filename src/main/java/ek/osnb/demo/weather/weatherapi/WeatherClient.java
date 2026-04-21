package ek.osnb.demo.weather.weatherapi;

public interface WeatherClient {
    WeatherResponse getWeather(double lat, double lon);
}
