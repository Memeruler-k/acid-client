package dev.abstr3act.addon.utils.math.inv;

import dev.abstr3act.addon.utils.math.InventoryUtility;
import meteordevelopment.meteorclient.MeteorClient;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public record SearchInvResult(int slot, boolean found, ItemStack stack) {
    private static final SearchInvResult NOT_FOUND_RESULT = new SearchInvResult(-1, false, null);

    public static SearchInvResult notFound() {
        return NOT_FOUND_RESULT;
    }

    @NotNull
    public static SearchInvResult inOffhand(ItemStack stack) {
        return new SearchInvResult(999, true, stack);
    }

    public boolean isHolding() {
        return MeteorClient.mc.player == null ? false : MeteorClient.mc.player.getInventory().selectedSlot == this.slot;
    }

    public boolean isInHotBar() {
        return this.slot < 9;
    }

    public void switchTo() {
        if (this.found && this.isInHotBar()) {
            InventoryUtility.switchTo(this.slot);
        }
    }

    public void switchToSilent() {
        if (this.found && this.isInHotBar()) {
            InventoryUtility.switchToSilent(this.slot);
        }
    }
}
