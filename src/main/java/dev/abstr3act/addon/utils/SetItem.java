package dev.abstr3act.addon.utils;

import meteordevelopment.meteorclient.MeteorClient;
import net.minecraft.item.ItemStack;

public class SetItem {
    public static void set(ItemStack item, int slot) {
        MeteorClient.mc.interactionManager.clickCreativeStack(item, slot);
    }

    public static void setMainHand(ItemStack item) {
        MeteorClient.mc.interactionManager.clickCreativeStack(item, 36 + MeteorClient.mc.player.getInventory().selectedSlot);
    }

    public static void setOffHand(ItemStack item) {
        MeteorClient.mc.interactionManager.clickCreativeStack(item, 45);
    }
}
