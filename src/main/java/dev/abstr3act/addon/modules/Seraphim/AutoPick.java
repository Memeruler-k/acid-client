package dev.abstr3act.addon.modules.Seraphim;

import dev.abstr3act.addon.Compassion;
import dev.abstr3act.addon.events.EventInteractBlock;
import dev.abstr3act.addon.module.SeraphimModule;
import dev.abstr3act.addon.utils.InventoryUtility;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.BedBlock;
import net.minecraft.block.Block;

public class AutoPick extends SeraphimModule {
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();

    public AutoPick() {
        super(Compassion.SERAPHIM, "AutoPick", "BedPVP Assist");
    }

    @EventHandler
    public void onInteractBlock(EventInteractBlock event) {
        Block block = this.mc.world.getBlockState(event.getHitResult().getBlockPos()).getBlock();
        if (block instanceof BedBlock) {
            int slot = InventoryUtility.findBedItem(this.mc.player.getInventory());
            InvUtils.move().from(slot).to(this.mc.player.getInventory().selectedSlot);
        }
    }
}
