package dev.abstr3act.addon.modules.Amrita.hackerdetector.impl;

import dev.abstr3act.addon.modules.Amrita.hackerdetector.Check;
import meteordevelopment.meteorclient.events.packets.PacketEvent.Receive;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;

public class ScaffoldCheck extends Check {
    private float yaw;
    private float cacheYaw;
    private boolean rotate;

    @Override
    public String getName() {
        return "Scaffold";
    }

    @Override
    public void onPacketReceive(Receive event, PlayerEntity player) {
    }

    @Override
    public void onUpdate(PlayerEntity player) {
        this.cacheYaw = this.yaw;
        this.yaw = player.getYaw();
        if (this.cacheYaw == this.yaw + 180.0F) {
            this.rotate = true;
        }

        if (player.handSwinging
            && player.getPitch() > 70.0F
            && player.getMainHandStack() != null
            && player.getMainHandStack().getItem() instanceof BlockItem
            && !player.isSneaking()
            && this.rotate) {
            this.flag(player, "Scaffold [pitch = " + player.getPitch() + "]");
            this.rotate = false;
        }
    }
}
