package org.itstep.bot;

import org.itstep.weather.OpenWeatherMap;
import org.itstep.weather.data.Coord;
import org.itstep.weather.data.Main;
import org.itstep.weather.data.WeatherData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.abilitybots.api.bot.AbilityBot;
import org.telegram.abilitybots.api.objects.Ability;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Location;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.Optional;

import static org.telegram.abilitybots.api.objects.Locality.ALL;
import static org.telegram.abilitybots.api.objects.Locality.USER;
import static org.telegram.abilitybots.api.objects.Privacy.ADMIN;
import static org.telegram.abilitybots.api.objects.Privacy.PUBLIC;

public class WeatherBot extends AbilityBot {

    public static final Logger logger = LoggerFactory.getLogger(WeatherBot.class);

    private final OpenWeatherMap openWeatherMap;

    public WeatherBot(String botToken, String botUsername) {
        super(botToken, botUsername);
        openWeatherMap = new OpenWeatherMap(System.getenv("OPEN_WEATHER_MAP_API"));
    }

    public Ability sayHelloWorld() {
        return Ability
                .builder()
                .name("hello")
                .info("says hello world!")
                .input(0)
                .locality(USER)
                .privacy(ADMIN)
                .action(ctx -> silent.send("Hello world!", ctx.chatId()))
                .post(ctx -> silent.send("Bye world!", ctx.chatId()))
                .build();
    }

    public Ability start() {
        return Ability.builder()
                .name("start")
                .info("Simple weather bot")
                .input(0)
                .locality(ALL)
                .privacy(PUBLIC)
                .action(ctx -> silent.send("Hello " + ctx.user().getUserName(), ctx.chatId()))
                .build();
    }

    public Ability weather() {
        return Ability.builder()
                .name("weather")
                .info("Get weather for your location")
                .input(0)
                .locality(ALL)
                .privacy(PUBLIC)
                .action(ctx -> {
                    SendMessage sendMessage = new SendMessage();
                    sendMessage.setText("Please send your location");
                    sendMessage.setReplyMarkup(weatherKeyboard());
                    sendMessage.setChatId(String.valueOf(ctx.chatId()));
                    silent.execute(sendMessage);
                })
                .reply(update -> {
                    Location loc = update.getMessage().getLocation();
                    logger.info("Location: {}", loc);
                    // TODO: вернуть погоду для указанной локации
                    SendMessage sendMessage = new SendMessage();
                    Optional<WeatherData> currentWeather = openWeatherMap.getCurrentWeather(new Coord(loc.getLongitude(), loc.getLatitude()));
                    if(currentWeather.isPresent()) {
                        WeatherData weatherData = currentWeather.get();
                        Main main = weatherData.getMain();
                        sendMessage.setText(String.format("Current weather for %s: Temperature: %s℃, Pressure: %s kPa",
                                weatherData.getName(),
                                main.getTemp(), main.getPressure()));
                    } else {
                        sendMessage.setText("Service not found");
                    }
                    sendMessage.setChatId(String.valueOf(update.getMessage().getChatId()));
                    sendMessage.setReplyMarkup(clearWeatherKeyboard());
                    silent.execute(sendMessage);
                }, update -> update.hasMessage() && update.getMessage().hasLocation())
                .build();
    }

    private ReplyKeyboard clearWeatherKeyboard() {
        ReplyKeyboardRemove replyKeyboardRemove = new ReplyKeyboardRemove();
        replyKeyboardRemove.setRemoveKeyboard(true);
        return replyKeyboardRemove;
    }

    public ReplyKeyboardMarkup weatherKeyboard() {
        KeyboardRow keyboardRow = new KeyboardRow();
        KeyboardButton buttonYourLocation = new KeyboardButton("Your location");
        buttonYourLocation.setRequestLocation(true);
        keyboardRow.add(buttonYourLocation);
        return ReplyKeyboardMarkup.builder()
                .keyboardRow(keyboardRow)
                .build();
    }

    @Override
    public int creatorId() {
        return 517806843;
    }
}
