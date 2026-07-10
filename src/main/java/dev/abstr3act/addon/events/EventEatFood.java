package dev.abstr3act.addon.events;

import net.minecraft.item.ItemStack;

public class EventEatFood {
    private final ItemStack stack;

    public EventEatFood(ItemStack stack) {
        this.stack = stack;
    }

    public ItemStack getFood() {
        return this.stack;
    }
}
