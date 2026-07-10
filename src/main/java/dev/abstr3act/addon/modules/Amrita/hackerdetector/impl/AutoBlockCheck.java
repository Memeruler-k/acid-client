package dev.abstr3act.addon.modules.Amrita.hackerdetector.impl;

import dev.abstr3act.addon.modules.Amrita.hackerdetector.Check;
import meteordevelopment.meteorclient.events.packets.PacketEvent.Receive;
import net.minecraft.entity.player.PlayerEntity;

public class AutoBlockCheck extends Check {
    private int blockingTime;

    @Override
    public String getName() {
        return "AutoBlock";
    }

    @Override
    public void onPacketReceive(Receive event, PlayerEntity entity) {
    }

    @Override
    public void onUpdate(PlayerEntity player) {
        if (player.isBlocking()) {
            this.blockingTime++;
        } else {
            this.blockingTime = 0;
        }

        if (this.blockingTime > 5 && player.handSwinging) {
            this.flag(player, "Swing when using item or blocking");
        }
    }
}
