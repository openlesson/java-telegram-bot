package org.itstep.bot;

import org.telegram.abilitybots.api.bot.AbilityBot;
import org.telegram.abilitybots.api.objects.Ability;
import org.telegram.abilitybots.api.objects.Locality;
import org.telegram.abilitybots.api.objects.Privacy;

public class WeatherBot extends AbilityBot {
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

    @Override
    public int creatorId() {
        return 517806843;
    }
}
