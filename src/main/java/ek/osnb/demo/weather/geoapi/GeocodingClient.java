package ek.osnb.demo.weather.geoapi;

public interface GeocodingClient {
    Location getLatLon(String city);
}
