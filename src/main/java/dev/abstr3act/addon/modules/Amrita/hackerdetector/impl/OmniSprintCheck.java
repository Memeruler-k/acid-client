package dev.abstr3act.addon.modules.Amrita.hackerdetector.impl;

import dev.abstr3act.addon.modules.Amrita.hackerdetector.Check;
import dev.abstr3act.addon.utils.seraphim.movement.MovementUtils;
import meteordevelopment.meteorclient.events.packets.PacketEvent.Receive;
import net.minecraft.entity.player.PlayerEntity;

public class OmniSprintCheck extends Check {
    @Override
    public String getName() {
        return "OmniSprint";
    }

    @Override
    public void onPacketReceive(Receive event, PlayerEntity player) {
    }

    @Override
    public void onUpdate(PlayerEntity player) {
        if (player.isSprinting() && (player.forwardSpeed < 0.0F || player.forwardSpeed == 0.0F && player.sidewaysSpeed != 0.0F)) {
            this.flag(player, "Sprinting when moving backward [motion = " + MovementUtils.getSpeed(player) + "]");
        }
    }
}
