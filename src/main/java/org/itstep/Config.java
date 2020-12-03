package org.itstep;

public class Config {
    public static String getOpenWeatherMapApiKey() {
        return System.getenv("OPEN_WEATHER_MAP_API");
    }

    public static String getBotToken() {
        return System.getenv("BOT_TOKEN");
    }

    public static String getBotName() {
        return System.getenv("BOT_NAME");
    }
}
