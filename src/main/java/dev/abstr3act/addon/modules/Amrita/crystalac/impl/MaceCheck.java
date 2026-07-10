package dev.abstr3act.addon.modules.Amrita.crystalac.impl;

import dev.abstr3act.addon.modules.Amrita.crystalac.Check;
import meteordevelopment.meteorclient.events.packets.PacketEvent.Receive;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.MaceItem;

public class MaceCheck extends Check {
    boolean fall;
    int i;

    @Override
    public String getName() {
        return "Mace";
    }

    @Override
    public void onPacketReceive(Receive event, PlayerEntity player) {
    }

    @Override
    public void onUpdate(PlayerEntity player) {
        if (player.fallDistance > 3.0F && player.getMainHandStack().getItem() instanceof MaceItem) {
            this.i++;
        }

        if (this.i >= 20) {
            this.flag(player, "操你妈死苍蝇一锤子干死你妈逼");
            this.i = 0;
        }
    }
}
