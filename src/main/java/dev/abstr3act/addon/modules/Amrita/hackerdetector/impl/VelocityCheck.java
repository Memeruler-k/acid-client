package dev.abstr3act.addon.modules.Amrita.hackerdetector.impl;

import dev.abstr3act.addon.modules.Amrita.hackerdetector.Check;
import dev.abstr3act.addon.utils.seraphim.movement.MovementUtils;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.packets.PacketEvent.Receive;
import net.minecraft.entity.player.PlayerEntity;

public class VelocityCheck extends Check {
    public int vl;

    @Override
    public String getName() {
        return "Velocity";
    }

    @Override
    public void onPacketReceive(Receive event, PlayerEntity player) {
    }

    @Override
    public void onUpdate(PlayerEntity player) {
        if (player.hurtTime > 6
            && player.hurtTime < 12
            && player.prevX == player.getX()
            && player.getZ() == player.prevZ
            && !MeteorClient.mc.world.canCollide(player, player.getBoundingBox().expand(0.05, 0.0, 0.05))) {
            this.vl++;
            if (this.vl >= 50) {
                this.flag(player, "Invalid velocity [speed = " + MovementUtils.getSpeed(player) + "]");
                this.vl = 0;
            }
        }
    }
}
