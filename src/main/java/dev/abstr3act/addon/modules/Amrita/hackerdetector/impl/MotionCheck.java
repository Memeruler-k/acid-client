package dev.abstr3act.addon.modules.Amrita.hackerdetector.impl;

import dev.abstr3act.addon.modules.Amrita.hackerdetector.Check;
import meteordevelopment.meteorclient.events.packets.PacketEvent.Receive;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;

public class MotionCheck extends Check {
    public static boolean isMoving(PlayerEntity player) {
        return player != null && (player.forwardSpeed != 0.0F || player.sidewaysSpeed != 0.0F);
    }

    public static double getBaseMoveSpeed(PlayerEntity player) {
        double baseSpeed = 0.2873;
        if (player.hasStatusEffect(StatusEffects.SPEED)) {
            int amplifier = player.getStatusEffect(StatusEffects.SPEED).getAmplifier();
            baseSpeed *= 1.0 + 0.2 * (amplifier + 1);
        }

        return baseSpeed;
    }

    @Override
    public String getName() {
        return "InvalidMotion";
    }

    @Override
    public void onPacketReceive(Receive event, PlayerEntity entity) {
    }

    @Override
    public void onUpdate(PlayerEntity player) {
        double base = getBaseMoveSpeed(player);
        double speed = Math.hypot(player.getVelocity().x, player.getVelocity().z);
        if (speed > base * 1.25 && player.hurtTime == 0 && !player.isFallFlying()) {
            this.flag(player, "Move too fast [velocity = " + speed + "]");
        }

        if (!player.isOnGround() && !isMoving(player) && player.getVelocity().y == 0.0 && player.groundCollision) {
            this.flag(player, "Not moving on air for a long time");
        }
    }
}
