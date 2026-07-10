package dev.abstr3act.addon.modules.Seraphim;

import dev.abstr3act.addon.Compassion;
import dev.abstr3act.addon.module.SeraphimModule;
import meteordevelopment.meteorclient.events.game.ReceiveMessageEvent;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.StringListSetting.Builder;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;

import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Repeat extends SeraphimModule {
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
    private final Setting<List<String>> target = this.sgGeneral
        .add(
            ((Builder) ((Builder) ((Builder) new Builder().name("messages")).description("Messages to use for spam.")).defaultValue(List.of("Meteor on Crack!")))
                .build()
        );

    public Repeat() {
        super(Compassion.SERAPHIM, "Repeat", "Repeat others message");
    }

    public static String formatMessage(String input) {
        String regex = "^\\s*(?:<[^>]+>\\s*|\\[[^]]+]\\s*|[^\\s:]+:\\s*)?(.*)$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);
        return matcher.find() ? matcher.group(1) : "";
    }

    public static String extractPlayer(String input) {
        String regex = "(?:<([^>]+)>|\\[([^]]+)]|([^\\s]+):?)";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);
        if (matcher.find()) {
            return matcher.group(1) != null ? matcher.group(1) : (matcher.group(2) != null ? matcher.group(2) : matcher.group(3));
        } else {
            return "";
        }
    }

    @EventHandler
    public void onReceivedMessage(ReceiveMessageEvent event) {
        String message = formatMessage(event.getMessage().getString());
        String player = extractPlayer(event.getMessage().getString());
        if (!Objects.equals(message, "") && !Objects.equals(player, "")) {
            boolean containsAny = (this.target.get()).stream().anyMatch(player::contains);
            if (containsAny) {
                ChatUtils.sendPlayerMsg(message);
            }
        }
    }
}
