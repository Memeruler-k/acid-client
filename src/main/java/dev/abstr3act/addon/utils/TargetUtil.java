package dev.abstr3act.addon.utils;

import dev.abstr3act.addon.modules.Fragment.AntiBot;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;

public class TargetUtil {
    public static boolean noKillAura = false;

    public static boolean isBot(LivingEntity entity) {
        return ((AntiBot) Modules.get().get(AntiBot.class)).isActive() && entity instanceof PlayerEntity && !(entity instanceof ClientPlayerEntity)
            ? ((AntiBot) Modules.get().get(AntiBot.class)).inBotList(entity)
            : false;
    }
}
