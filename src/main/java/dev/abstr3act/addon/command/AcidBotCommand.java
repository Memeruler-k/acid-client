package dev.abstr3act.addon.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.abstr3act.addon.modules.Seraphim.acidbot.AcidUtils;
import dev.abstr3act.addon.utils.abnormally.AChatUtils;
import meteordevelopment.meteorclient.commands.Command;
import net.minecraft.command.CommandSource;
import net.minecraft.text.Text;

import java.awt.image.BufferedImage;

public class AcidBotCommand extends Command {
    public BufferedImage buffered;

    public AcidBotCommand() {
        super("acidbot", "Use random string to fuck your enemy up", new String[0]);
    }

    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(literal("clear").executes(context -> {
            AChatUtils.sendMsgSeraphim(Text.of("Cleared."));
            AcidUtils.conversationHistory.clear();
            return 1;
        }));
    }
}
