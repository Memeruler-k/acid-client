package dev.abstr3act.addon.modules.Abnormally;

import dev.abstr3act.addon.Compassion;
import dev.abstr3act.addon.module.AbnormallyModule;
import meteordevelopment.meteorclient.events.game.SendMessageEvent;
import meteordevelopment.orbit.EventHandler;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AutoCompassion extends AbnormallyModule {
    public AutoCompassion() {
        super(Compassion.ABNORMALLY, "AntiCompassion", "Prevent your message to block by server");
    }

    public static String replaceWithTriangles(String input) {
        StringBuilder result = new StringBuilder();
        boolean toggle = true;
        Pattern pattern = Pattern.compile("[^\\p{Punct}\\s]");
        Matcher matcher = pattern.matcher(input);

        int lastIndex;
        for (lastIndex = 0; matcher.find(); lastIndex = matcher.end()) {
            result.append(input, lastIndex, matcher.start());
            result.append(toggle ? "△" : "▽");
            toggle = !toggle;
        }

        result.append(input.substring(lastIndex));
        return result.toString();
    }

    public void onActivate() {
    }

    public void onDeactivate() {
    }

    @EventHandler
    public void onMessageSend(SendMessageEvent event) {
        String value = event.message;
        event.message = String.valueOf(replaceWithTriangles(value));
    }
}
