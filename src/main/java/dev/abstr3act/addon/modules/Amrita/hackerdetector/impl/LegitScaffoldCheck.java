package dev.abstr3act.addon.modules.Amrita.hackerdetector.impl;

import dev.abstr3act.addon.modules.Amrita.hackerdetector.Check;
import dev.abstr3act.addon.utils.seraphim.movement.MovementUtils;
import dev.abstr3act.addon.utils.timers.Timer;
import meteordevelopment.meteorclient.events.packets.PacketEvent.Receive;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;

public class LegitScaffoldCheck extends Check {
    private final Timer timer = new Timer();
    private int sneakFlag;

    @Override
    public String getName() {
        return "LegitScaffold";
    }

    @Override
    public void onPacketReceive(Receive event, PlayerEntity entity) {
    }

    @Override
    public void onUpdate(PlayerEntity player) {
        if (player.isSneaking()) {
            this.timer.reset();
            this.sneakFlag++;
        }

        if (this.timer.passed(140L)) {
            this.sneakFlag = 0;
        }

        if (player.getPitch() > 75.0F
            && player.getPitch() < 90.0F
            && player.handSwinging
            && player.getMainHandStack() != null
            && player.getMainHandStack().getItem() instanceof BlockItem) {
            if (MovementUtils.getSpeed(player) >= 0.1 && player.isOnGround() && this.sneakFlag > 5) {
                this.flag(player, "Sneak too fast [flag = " + this.sneakFlag + "]");
            }

            if (MovementUtils.getSpeed(player) >= 0.21 && !player.isOnGround() && this.sneakFlag > 5) {
                this.flag(player, "Sneak too fast");
            }
        }
    }
}
