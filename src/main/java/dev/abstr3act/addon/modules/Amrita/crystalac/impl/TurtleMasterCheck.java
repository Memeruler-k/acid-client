package dev.abstr3act.addon.modules.Amrita.crystalac.impl;

import dev.abstr3act.addon.modules.Amrita.crystalac.Check;
import dev.abstr3act.addon.utils.timers.Timer;
import meteordevelopment.meteorclient.events.packets.PacketEvent.Receive;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;

public class TurtleMasterCheck extends Check {
    private final Timer timer = new Timer();
    int i;

    @Override
    public String getName() {
        return "TurtleMaster";
    }

    @Override
    public void onPacketReceive(Receive event, PlayerEntity entity) {
    }

    @Override
    public void onUpdate(PlayerEntity player) {
        if (player.hasStatusEffect(StatusEffects.SLOWNESS) && player.hasStatusEffect(StatusEffects.RESISTANCE) && this.timer.passed(2000L)) {
            this.i++;
        }

        if (this.i >= 80) {
            this.flag(player, "傻狗大乌龟杨超操死你妈");
            this.i = 0;
        }
    }
}
