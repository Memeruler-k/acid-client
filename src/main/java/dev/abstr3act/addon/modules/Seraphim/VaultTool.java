package dev.abstr3act.addon.modules.Seraphim;

import dev.abstr3act.addon.Compassion;
import dev.abstr3act.addon.module.SeraphimModule;
import meteordevelopment.meteorclient.events.world.TickEvent.Post;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.entity.VaultBlockEntity;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;

public class VaultTool extends SeraphimModule {
    public VaultTool() {
        super(Compassion.SERAPHIM, "VaultTool", ".");
    }

    @EventHandler
    private void onTick(Post event) {
        if (this.mc.crosshairTarget != null && this.mc.crosshairTarget instanceof BlockHitResult) {
            if (this.mc.world.getBlockEntity(((BlockHitResult) this.mc.crosshairTarget).getBlockPos()) instanceof VaultBlockEntity entity
                && entity.getSharedData().getDisplayItem().getItem().equals(Items.HEAVY_CORE)) {
                wait(() -> this.sendPacket(new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND, (BlockHitResult) this.mc.crosshairTarget, 0)), 50L);
            }
        }
    }
}
