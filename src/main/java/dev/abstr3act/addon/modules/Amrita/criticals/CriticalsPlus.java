package dev.abstr3act.addon.modules.Amrita.criticals;

import dev.abstr3act.addon.Compassion;
import dev.abstr3act.addon.module.AmritaModule;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.combat.Criticals;
import meteordevelopment.meteorclient.utils.entity.DamageUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;

public class CriticalsPlus extends AmritaModule {
    private static MinecraftClient mc = MinecraftClient.getInstance();

    public CriticalsPlus() {
        super(Compassion.AMRITA, "CriticalsV2", "Better criticals module");
    }

    public static boolean canCrit() {
        return !mc.player.isOnGround() && mc.player.fallDistance > 0.0F;
    }

    public static boolean skipCrit() {
        return !mc.player.isOnGround() || mc.player.isSubmergedInWater() || mc.player.isInLava() || mc.player.isClimbing();
    }

    public static boolean allowCrit() {
        if (canCrit()) {
            return true;
        } else {
            return ((Criticals) Modules.get().get(Criticals.class)).isActive() ? !skipCrit() : false;
        }
    }

    public static boolean needCrit(Entity entity) {
        return entity instanceof LivingEntity livingEntity ? livingEntity.getHealth() >= DamageUtils.getAttackDamage(mc.player, livingEntity) : false;
    }
}
