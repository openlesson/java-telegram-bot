package org.itstep.bot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;
import java.util.Properties;

public class EchoBot extends TelegramLongPollingBot {

    public static final Logger logger = LoggerFactory.getLogger(EchoBot.class);

    public static final String CONFIG_FILE = "bot.properties";

    private Properties properties;

    public EchoBot() throws IOException {
        properties = new Properties();
        logger.debug("Read property from file {}", CONFIG_FILE);
        properties.load(getClass().getClassLoader().getResourceAsStream(CONFIG_FILE));
    }

    @Override
    public void onUpdateReceived(Update update) {
        logger.debug("Received message");
        if (update.hasMessage() && update.getMessage().hasText()) {
            Message receiveMessage = update.getMessage();
            logger.debug("Message text {}", receiveMessage.getText());
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(String.valueOf(receiveMessage.getChatId()));
            sendMessage.setText(update.getMessage().getText());
            try {
                execute(sendMessage);
            } catch (TelegramApiException e) {
                logger.error(e.getLocalizedMessage(), e);
            }
        }
    }

    @Override
    public String getBotUsername() {
        return properties.getProperty("name");
    }

    @Override
    public String getBotToken() {
        return properties.getProperty("token");
    }

}


