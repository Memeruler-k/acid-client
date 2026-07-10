package dev.abstr3act.addon.modules.Seraphim;

import dev.abstr3act.addon.Compassion;
import dev.abstr3act.addon.events.EventPlayerUpdate;
import dev.abstr3act.addon.module.SeraphimModule;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;

public class MultiTask extends SeraphimModule {
    public MultiTask() {
        super(Compassion.SERAPHIM, "MultiTask", ".");
    }

    @EventHandler
    public void onUpdate(EventPlayerUpdate event) {
        if (this.mc.crosshairTarget instanceof BlockHitResult crossHair
            && crossHair.getBlockPos() != null
            && this.mc.options.attackKey.isPressed()
            && !this.mc.world.getBlockState(crossHair.getBlockPos()).isAir()) {
            this.mc.interactionManager.attackBlock(crossHair.getBlockPos(), crossHair.getSide());
            this.mc.player.swingHand(Hand.MAIN_HAND);
        }

        if (this.mc.crosshairTarget instanceof EntityHitResult ehr
            && ehr.getEntity() != null
            && this.mc.options.attackKey.isPressed()
            && this.mc.player.getAttackCooldownProgress(0.5F) > 0.9F) {
            this.mc.interactionManager.attackEntity(this.mc.player, ehr.getEntity());
            this.mc.player.swingHand(Hand.MAIN_HAND);
        }
    }
}
