package dev.abstr3act.addon.modules.Amrita.hackerdetector.impl;

import dev.abstr3act.addon.modules.Amrita.hackerdetector.Check;
import meteordevelopment.meteorclient.events.packets.PacketEvent.Receive;
import net.minecraft.entity.player.PlayerEntity;

public class AngleCheck extends Check {
    @Override
    public void onUpdate(PlayerEntity player) {
        if (Math.abs(player.getYaw() - player.prevYaw) > 50.0F && player.handSwingProgress != 0.0F) {
            this.flag(player, "Too fast rotate speed[speed = " + Math.abs(player.getYaw() - player.prevYaw) + "]");
        }

        if (player.getPitch() > 90.0F || player.prevPitch < -90.0F) {
            this.flag(player, "Invalid rotation pitch[pitch = " + player.getPitch() + "]");
        }
    }

    @Override
    public String getName() {
        return "Angle";
    }

    @Override
    public void onPacketReceive(Receive event, PlayerEntity entity) {
    }
}
