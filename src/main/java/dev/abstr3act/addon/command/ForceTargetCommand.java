package dev.abstr3act.addon.command;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.abstr3act.addon.utils.abnormally.AChatUtils;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.commands.arguments.PlayerListEntryArgumentType;
import meteordevelopment.meteorclient.events.world.TickEvent.Pre;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.awt.image.BufferedImage;

public class ForceTargetCommand extends Command {
    public static PlayerEntity target;
    public BufferedImage buffered;

    public ForceTargetCommand() {
        super("forcetarget", "forcetarget", new String[0]);
        MeteorClient.EVENT_BUS.subscribe(this);
    }

    public static PlayerEntity gp2pe(GameProfile profile) {
        if (mc.world != null && mc.player != null) {
            for (PlayerEntity player : mc.world.getPlayers()) {
                if (player.getGameProfile().getId().equals(profile.getId())) {
                    return player;
                }
            }

            return null;
        } else {
            return null;
        }
    }

    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(literal("set").then(argument("player", PlayerListEntryArgumentType.create()).executes(context -> {
            if (PlayerListEntryArgumentType.get(context).getProfile() == null) {
                AChatUtils.sendMsgSeraphim(Text.of(Formatting.RED + "Target is null!"));
                return 1;
            } else {
                target = gp2pe(PlayerListEntryArgumentType.get(context).getProfile());
                if (target == null) {
                    AChatUtils.sendMsgSeraphim(Text.of(Formatting.RED + "Target is null!"));
                    return 1;
                } else {
                    AChatUtils.sendMsgSeraphim(Text.of("Force setting the current target to " + Formatting.WHITE + target.getName().getString()));
                    return 1;
                }
            }
        })));
        builder.then(literal("clear").executes(context -> {
            target = null;
            AChatUtils.sendMsgSeraphim(Text.of("Cleared target."));
            return 1;
        }));
    }

    @EventHandler
    private void onTick(Pre event) {
        if (target != null) {
            if (target.isDead() || target.getHealth() <= 0.0F || target.deathTime > 0 || !target.isAlive()) {
                AChatUtils.sendMsgSeraphim(Text.of("Successfully destroyed target!"));
                target = null;
            }
        }
    }
}
