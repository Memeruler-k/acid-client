package dev.abstr3act.addon.command;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.abstr3act.addon.font.FontRenderers;
import meteordevelopment.meteorclient.commands.Command;
import net.minecraft.command.CommandSource;

import java.awt.*;
import java.io.IOException;

public class FontCommand extends Command {
    public FontCommand() {
        super("font", "Use random string to fuck your enemy up", new String[0]);
    }

    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(literal("set").then(argument("size", IntegerArgumentType.integer()).executes(context -> {
            int size = IntegerArgumentType.getInteger(context, "size");

            try {
                FontRenderers.user_text = FontRenderers.create(size, FontRenderers.GEOSANS_LIGHT);
            } catch (FontFormatException | IOException var3) {
                var3.printStackTrace();
            }

            return 1;
        })));
    }
}
