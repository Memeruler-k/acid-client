package dev.abstr3act.addon.mixin;

import dev.abstr3act.addon.modules.Seraphim.NoSlowV2;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({Block.class})
public abstract class MixinBlock {
    @Inject(
        method = {"getVelocityMultiplier"},
        at = {@At("HEAD")},
        cancellable = true
    )
    public void getVelocityMultiplierHook(CallbackInfoReturnable<Float> cir) {
        if (((NoSlowV2) Modules.get().get(NoSlowV2.class)).isActive()) {
            if (((NoSlowV2) Modules.get().get(NoSlowV2.class)).soulSand.get() && ((Block) (Object) (this)) == Blocks.SOUL_SAND) {
                cir.setReturnValue(Blocks.DIRT.getVelocityMultiplier());
            }

            if (((NoSlowV2) Modules.get().get(NoSlowV2.class)).honey.get() && ((Block) (Object) (this)) == Blocks.HONEY_BLOCK) {
                cir.setReturnValue(Blocks.DIRT.getVelocityMultiplier());
            }
        }
    }

    @Inject(
        method = {"getSlipperiness"},
        at = {@At("HEAD")},
        cancellable = true
    )
    public void getSlipperinessHook(CallbackInfoReturnable<Float> cir) {
        if (((NoSlowV2) Modules.get().get(NoSlowV2.class)).isActive()) {
            if (((NoSlowV2) Modules.get().get(NoSlowV2.class)).slime.get() && ((Block) (Object) (this)) == Blocks.SLIME_BLOCK) {
                cir.setReturnValue(Blocks.DIRT.getSlipperiness());
            }

            if (((NoSlowV2) Modules.get().get(NoSlowV2.class)).ice.get()
                && (((Block) (Object) (this)) == Blocks.ICE || ((Block) (Object) (this)) == Blocks.PACKED_ICE || ((Block) (Object) (this)) == Blocks.BLUE_ICE || ((Block) (Object) (this)) == Blocks.FROSTED_ICE)
                && !MeteorClient.mc.options.jumpKey.isPressed()) {
                cir.setReturnValue(Blocks.DIRT.getSlipperiness());
            }
        }
    }
}
