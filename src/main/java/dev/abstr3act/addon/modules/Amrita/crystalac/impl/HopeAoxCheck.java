package dev.abstr3act.addon.modules.Amrita.crystalac.impl;

import dev.abstr3act.addon.modules.Amrita.crystalac.Check;
import dev.abstr3act.addon.utils.timers.Timer;
import meteordevelopment.meteorclient.events.packets.PacketEvent.Receive;
import net.minecraft.entity.player.PlayerEntity;

public class HopeAoxCheck extends Check {
    private final Timer timer = new Timer();
    int i;

    @Override
    public String getName() {
        return "HopeAox";
    }

    @Override
    public void onPacketReceive(Receive event, PlayerEntity entity) {
    }

    @Override
    public void onUpdate(PlayerEntity player) {
        if (player.getName().getString().toLowerCase().contains("hope_aox")) {
            this.flag(player, "hope_aox你妈死了我操死你妈逼个窝囊废剑冢东西能不能滚出水晶圈");
        }
    }
}
