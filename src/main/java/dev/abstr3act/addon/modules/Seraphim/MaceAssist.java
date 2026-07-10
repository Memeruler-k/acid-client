package dev.abstr3act.addon.modules.Seraphim;

import dev.abstr3act.addon.Compassion;
import dev.abstr3act.addon.module.SeraphimModule;
import dev.abstr3act.addon.utils.InventoryUtility;
import meteordevelopment.meteorclient.events.entity.player.AttackEntityEvent;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;

public class MaceAssist extends SeraphimModule {
    public MaceAssist() {
        super(Compassion.SERAPHIM, "MaceAssist", ".");
    }

    @EventHandler
    public void onAttack(AttackEntityEvent event) {
        if (this.mc.player.fallDistance >= 3.0F && !(event.entity instanceof PlayerEntity)) {
            InvUtils.swap(InvUtils.find(new Item[]{Items.MACE}).slot(), false);
        }

        if (event.entity instanceof PlayerEntity && this.mc.player.fallDistance >= 3.0F && !this.mc.player.isFallFlying()) {
            if (((PlayerEntity) event.entity).isBlocking()) {
                InvUtils.swap(InventoryUtility.findAxeItem(this.mc.player.getInventory()), false);
            } else {
                InvUtils.swap(InvUtils.find(new Item[]{Items.MACE}).slot(), false);
            }
        }
    }
}
