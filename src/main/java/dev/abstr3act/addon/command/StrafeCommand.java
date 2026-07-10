package dev.abstr3act.addon.command;

import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.abstr3act.addon.utils.seraphim.movement.MovementUtils;
import meteordevelopment.meteorclient.commands.Command;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.command.CommandSource;
import net.minecraft.screen.ScreenHandler;

public class StrafeCommand extends Command {
    public static Screen savedScreen;
    public static ScreenHandler savedScreenHandler;

    public StrafeCommand() {
        super("strafe", "Use random string to fuck your enemy up", new String[0]);
    }

    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(argument("strafe", FloatArgumentType.floatArg(-1000.0F, 1000.0F)).executes(context -> {
            float amount = FloatArgumentType.getFloat(context, "strafe");
            MovementUtils.strafe(amount);
            return 1;
        }));
    }
}
