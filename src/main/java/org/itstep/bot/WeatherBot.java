package org.itstep.bot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.abilitybots.api.bot.AbilityBot;
import org.telegram.abilitybots.api.objects.Ability;
import org.telegram.abilitybots.api.objects.Locality;
import org.telegram.abilitybots.api.objects.Privacy;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

public class WeatherBot extends AbilityBot {

    public static final Logger logger = LoggerFactory.getLogger(WeatherBot.class);

    public WeatherBot(String botToken, String botUsername) {
        super(botToken, botUsername);
    }

    public Ability start() {
        return Ability.builder()
                .name("start")
                .info("Simple weather bot")
                .input(0)
                .locality(Locality.ALL)
                .privacy(Privacy.PUBLIC)
                .action(ctx -> silent.send("Hello " + ctx.user().getUserName(), ctx.chatId()))
                .build();
    }

    public Ability weather() {
        return Ability.builder()
                .name("weather")
                .info("Get weather for your location")
                .input(0)
                .locality(Locality.ALL)
                .privacy(Privacy.PUBLIC)
                .action(ctx -> {
                    SendMessage sendMessage = new SendMessage();
                    sendMessage.setText("Please send your location");
                    sendMessage.setReplyMarkup(weatherKeyboard());
                    sendMessage.setChatId(String.valueOf(ctx.chatId()));
                    silent.execute(sendMessage);
                })
                .reply(update -> {
                    logger.info("Location: {}", update.getMessage().getLocation());
                    // TODO: вернуть погоду для указанной локации
                    SendMessage sendMessage = new SendMessage();
                    sendMessage.setText("I will send you weather for your location later");
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
