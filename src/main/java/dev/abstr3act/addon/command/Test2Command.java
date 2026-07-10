package dev.abstr3act.addon.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.abstr3act.addon.utils.TextColorParser;
import dev.abstr3act.addon.utils.abnormally.AChatUtils;
import meteordevelopment.meteorclient.commands.Command;
import net.minecraft.command.CommandSource;
import net.minecraft.text.Text;

import java.awt.*;
import java.util.Map;

public class Test2Command extends Command {
    public Test2Command() {
        super("test2", "Use random string to fuck your enemy up", new String[0]);
    }

    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(literal("debug").executes(context -> {
            Text text = Text.literal("Hello ").styled(style -> style.withColor(16711680)).append(Text.literal("World").styled(style -> style.withColor(65280)));
            Map<String, Color> textColors = TextColorParser.parseText(text);
            textColors.forEach((str, color) -> {
                AChatUtils.sendMsgAmrita(text);
                AChatUtils.sendMsgAmrita(Text.of("Text: " + str + ", Color: " + color));
            });
            return 1;
        }));
    }
}
