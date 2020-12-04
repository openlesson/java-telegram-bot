package org.itstep.bot;

import org.itstep.Config;
import org.itstep.weather.OpenWeatherMap;
import org.itstep.weather.data.Coord;
import org.itstep.weather.data.Main;
import org.itstep.weather.data.WeatherData;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.abilitybots.api.bot.AbilityBot;
import org.telegram.abilitybots.api.objects.Ability;
import org.telegram.abilitybots.api.objects.MessageContext;
import org.telegram.abilitybots.api.util.AbilityUtils;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Location;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

import static org.telegram.abilitybots.api.objects.Locality.ALL;
import static org.telegram.abilitybots.api.objects.Privacy.PUBLIC;

public class WeatherBot extends AbilityBot {

    public static final Logger logger = LoggerFactory.getLogger(WeatherBot.class);

    private final OpenWeatherMap openWeatherMap;

    public WeatherBot(String botToken, String botUsername) {
        super(botToken, botUsername);
        openWeatherMap = new OpenWeatherMap(Config.getOpenWeatherMapApiKey());
    }

    public Ability start() {
        return Ability
                .builder()
                .name("start")
                .info("Start your bot")
                .input(0)
                .locality(ALL)
                .privacy(PUBLIC)
                .action(messageContext -> silent.send("Type /weather command to get current weather for your location",
                        AbilityUtils.getChatId(messageContext.update())))
                .build();
    }

    public Ability location() {
        Map<Integer, Location> locations = db.getMap("locations");
        final String message = "Please send your new location";
        return Ability
                .builder()
                .name("location")
                .info("Setup your location")
                .input(0)
                .locality(ALL)
                .privacy(PUBLIC)
                .action(ctx -> {
                    logger.info("Send request for location");
                    silent.execute(makeSendMessageGetLocation(ctx, message));
                })
                .reply(update -> {
                    Location loc = update.getMessage().getLocation();
                    logger.info("User response location: {}", loc);
                    User user = AbilityUtils.getUser(update);
                    locations.put(user.getId(), loc);
                    SendMessage sendMessage = new SendMessage(String.valueOf(AbilityUtils.getChatId(update)),
                            "Now you can get weather for this location by command /weather");
                    sendMessage.setReplyMarkup(clearWeatherKeyboard());
                    silent.execute(sendMessage);
                }, isReplyHasLocation(), isReplyToMessage(message))
                .build();
    }

    public Ability weather() {
        Map<Integer, Location> locations = db.getMap("locations");
        String message = "Please send your location";
        return Ability.builder()
                .name("weather")
                .info("Get weather for your location")
                .input(0)
                .locality(ALL)
                .privacy(PUBLIC)
                .action(ctx -> {
                    Integer id = ctx.user().getId();
                    if (locations.containsKey(id)) {
                        Location loc = locations.get(id);
                        logger.info("Get weather for stored location: {}", loc);
                        silent.execute(makeWeatherMessage(ctx.chatId(), loc));
                    } else {
                        logger.info("Send request for location");
                        silent.execute(makeSendMessageGetLocation(ctx, message));
                    }
                })
                .reply(update -> {
                    Location loc = update.getMessage().getLocation();
                    logger.info("User response location: {}", loc);
                    User user = AbilityUtils.getUser(update);
                    locations.put(user.getId(), loc);
                    silent.execute(new SendMessage(String.valueOf(AbilityUtils.getChatId(update)),
                            "Your current location is sored. You can change your location by command /location"));
                    silent.execute(makeWeatherMessage(AbilityUtils.getChatId(update), loc));
                }, isReplyHasLocation(), isReplyToMessage(message))
                .build();
    }

    private Predicate<Update> isReplyHasLocation() {
        return update -> update.hasMessage() && update.getMessage().hasLocation();
    }

    private Predicate<Update> isReplyToMessage(String message) {
        return upd -> {
            Message reply = upd.getMessage().getReplyToMessage();
            return reply.hasText() && reply.getText().equalsIgnoreCase(message);
        };
    }

    private Predicate<Update> isReplyToBot() {
        return upd -> upd.getMessage().getReplyToMessage().getFrom().getUserName().equalsIgnoreCase(getBotUsername());
    }

    @NotNull
    private SendMessage makeSendMessageGetLocation(MessageContext ctx, String message) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setText(message);
        sendMessage.setReplyMarkup(weatherKeyboard());
        sendMessage.setChatId(String.valueOf(ctx.chatId()));
        return sendMessage;
    }

    private SendMessage makeWeatherMessage(Long chatId, Location loc) {
        SendMessage sendMessage = new SendMessage();
        Optional<WeatherData> currentWeather = openWeatherMap.getCurrentWeather(new Coord(loc.getLongitude(), loc.getLatitude()));
        if (currentWeather.isPresent()) {
            WeatherData weatherData = currentWeather.get();
            Main main = weatherData.getMain();
            sendMessage.setText(String.format("Current weather for %s: Temperature: %s℃, Pressure: %s kPa",
                    weatherData.getName(),
                    main.getTemp(), main.getPressure()));
        } else {
            sendMessage.setText("Service not found");
        }
        sendMessage.setChatId(String.valueOf(chatId));
        sendMessage.setReplyMarkup(clearWeatherKeyboard());
        return sendMessage;
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
