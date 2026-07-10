package dev.abstr3act.addon.modules.Amrita.hackerdetector.impl;

import dev.abstr3act.addon.modules.Amrita.hackerdetector.Check;
import meteordevelopment.meteorclient.events.packets.PacketEvent.Receive;
import net.minecraft.entity.player.PlayerEntity;

public class NoFallCheck extends Check {
    boolean fall;

    @Override
    public String getName() {
        return "NoFall";
    }

    @Override
    public void onPacketReceive(Receive event, PlayerEntity player) {
    }

    @Override
    public void onUpdate(PlayerEntity player) {
        if (player.fallDistance > 3.0F) {
            this.fall = true;
        }

        if (this.fall && player.fallDistance == 0.0F && player.hurtTime == 0 && !player.isTouchingWater() && player.isOnGround()) {
            this.flag(player, "Not taking any damage");
            this.fall = false;
        }
    }
}
