package org.itstep.weather;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.itstep.weather.data.Coord;
import org.itstep.weather.data.WeatherData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

public class OpenWeatherMap {

    private final LoadingCache<Coord, WeatherData> loadingCache;

    private static final Logger log = LoggerFactory.getLogger(OpenWeatherMap.class);

    public OpenWeatherMap(String apiKey) {
        this.loadingCache = CacheBuilder.newBuilder()
                .expireAfterWrite(Duration.ofHours(1))
                .build(new LoadWeather(apiKey));
    }

    private static class LoadWeather extends CacheLoader<Coord, WeatherData> {
        private final HttpClient httpClient = HttpClient.newBuilder().build();
        private final ObjectMapper objectMapper;

        public static final String URL_PATTERN = "https://api.openweathermap.org/data/2.5/weather?lat={lat}&lon={lon}&appid={apiKey}&units=metric";

        private final String apiKey;

        public LoadWeather(String apiKey) {
            this.apiKey = apiKey;
            this.objectMapper = new ObjectMapper();
        }

        @Override
        public WeatherData load(Coord coord) throws Exception {
            String url = URL_PATTERN.replace("{lat}", coord.getLat().toString())
                    .replace("{lon}", coord.getLon().toString())
                    .replace("{apiKey}", apiKey);
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .GET()
                    .uri(URI.create(url))
                    .build();

            HttpResponse<String> httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            String body = httpResponse.body();
            return objectMapper.readValue(body, WeatherData.class);
        }
    }

    public Optional<WeatherData> getCurrentWeather(Coord coord) {
        log.info("Try get weather for {}", coord);
        WeatherData weatherData = null;
        try {
            weatherData = loadingCache.get(coord);
            log.info("Loaded {}", weatherData);
        } catch (ExecutionException e) {
            log.error(e.getLocalizedMessage(), e);
        }
        return Optional.ofNullable(weatherData);
    }
}
