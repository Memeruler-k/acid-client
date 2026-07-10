package dev.abstr3act.addon.modules.Amrita.hackerdetector.impl;

import dev.abstr3act.addon.modules.Amrita.hackerdetector.Check;
import meteordevelopment.meteorclient.events.packets.PacketEvent.Receive;
import net.minecraft.entity.player.PlayerEntity;

public class NoSlowCheck extends Check {
    private int sprintBuffer = 0;
    private int motionBuffer = 0;

    @Override
    public String getName() {
        return "NoSlow";
    }

    @Override
    public void onPacketReceive(Receive event, PlayerEntity player) {
    }

    @Override
    public void onUpdate(PlayerEntity player) {
        if (player.isUsingItem() || player.isBlocking()) {
            if (player.isSprinting()) {
                if (++this.sprintBuffer > 5) {
                    this.flag(player, "Sprinting when using item or blocking [sprint = " + player.isSprinting() + ", using = " + player.isUsingItem() + "]");
                }

                return;
            }

            double dx = player.prevX - player.getX();
            double dz = player.prevZ - player.getZ();
            if (dx * dx + dz * dz > 0.07 && ++this.motionBuffer > 10 && player.hurtTime == 0) {
                this.flag(player, "Not sprinting but keep in sprint motion when blocking [motionBuffer = " + this.motionBuffer + "]");
                this.motionBuffer = 7;
                return;
            }

            this.motionBuffer = this.motionBuffer - (this.motionBuffer > 0 ? 1 : 0);
            this.sprintBuffer = this.sprintBuffer - (this.sprintBuffer > 0 ? 1 : 0);
        }
    }
}
