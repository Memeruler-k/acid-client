package dev.abstr3act.addon.modules.Amrita.crystalac.impl;

import dev.abstr3act.addon.modules.Amrita.crystalac.Check;
import meteordevelopment.meteorclient.events.packets.PacketEvent.Receive;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ElytraItem;

public class ElytraCheck extends Check {
    int ticks;

    @Override
    public String getName() {
        return "Elytra";
    }

    @Override
    public void onPacketReceive(Receive event, PlayerEntity entity) {
    }

    @Override
    public void onUpdate(PlayerEntity player) {
        if (player.getInventory().getArmorStack(2).getItem() instanceof ElytraItem && !player.isOnGround()) {
            this.ticks++;
        }

        if (this.ticks > 40) {
            this.flag(player, "操你妈死苍蝇你妈是不是日本神风特攻队");
            this.ticks = 0;
        }
    }
}
