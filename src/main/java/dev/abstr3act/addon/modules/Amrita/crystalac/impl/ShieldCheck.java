package dev.abstr3act.addon.modules.Amrita.crystalac.impl;

import dev.abstr3act.addon.modules.Amrita.crystalac.Check;
import meteordevelopment.meteorclient.events.packets.PacketEvent.Receive;
import net.minecraft.entity.player.PlayerEntity;

public class ShieldCheck extends Check {
    int tick;
    private int blockingTime;

    @Override
    public String getName() {
        return "Shield";
    }

    @Override
    public void onPacketReceive(Receive event, PlayerEntity entity) {
    }

    @Override
    public void onUpdate(PlayerEntity player) {
        if (player.isBlocking()) {
            this.tick++;
        }

        if (this.tick >= 60) {
            this.flag(player, "傻逼盾牌狗用你全家遗照当盾牌使");
            this.tick = 0;
        }
    }
}
