package dev.abstr3act.addon.command;

import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import dev.abstr3act.addon.notifications.Notification;
import dev.abstr3act.addon.notifications.NotificationsManager;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import net.minecraft.command.CommandSource;

import java.awt.*;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class NotificationsCommand extends Command {
    public NotificationsCommand() {
        super("notifications", "Sends a dummy notification", new String[0]);
    }

    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(
            argument("mode", new NotificationsArgumentType())
                .executes(
                    context -> {
                        NotificationCommandType arg = (NotificationCommandType) context.getArgument(
                            "mode", NotificationCommandType.class
                        );
                        if (arg == NotificationCommandType.SEND) {
                            NotificationsManager.add(
                                new Notification("hey, this is a test!", "very cool indeed yes Test12312312312312312312313", new Color((int) (Math.random() * 1.6777216E7)))
                            );
                            ChatUtils.info("Notifications", new Object[]{"Successfully triggered notification!"});
                        } else if (arg == NotificationCommandType.CLEAR) {
                            NotificationsManager.clearNotifications();
                            ChatUtils.warning("Notifications", new Object[]{"Successfully cleared notifications!"});
                        }

                        return 1;
                    }
                )
        );
    }

    private static enum NotificationCommandType {
        SEND,
        CLEAR;
    }

    private static class NotificationsArgumentType implements ArgumentType<NotificationCommandType> {
        private static final DynamicCommandExceptionType NO_SUCH_MODULE = new DynamicCommandExceptionType(o -> new LiteralMessage("Type  " + o + " doesn't exist."));
        private static final List<String> EXAMPLES = Arrays.stream(NotificationCommandType.values())
            .map(Enum::name)
            .collect(Collectors.toList());

        public NotificationCommandType parse(StringReader reader) throws CommandSyntaxException {
            String argument = reader.readString();

            try {
                return NotificationCommandType.valueOf(argument.toUpperCase());
            } catch (Exception var4) {
                throw NO_SUCH_MODULE.create(argument);
            }
        }

        public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
            return CommandSource.suggestMatching(Arrays.stream(NotificationCommandType.values()).map(Enum::name), builder);
        }

        public Collection<String> getExamples() {
            return EXAMPLES;
        }
    }
}
