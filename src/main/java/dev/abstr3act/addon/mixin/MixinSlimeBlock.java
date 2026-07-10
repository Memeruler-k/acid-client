package dev.abstr3act.addon.mixin;

import dev.abstr3act.addon.modules.Seraphim.NoSlowV2;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.block.BlockState;
import net.minecraft.block.SlimeBlock;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({SlimeBlock.class})
public class MixinSlimeBlock {
    @Inject(
        method = {"onSteppedOn"},
        at = {@At("HEAD")},
        cancellable = true
    )
    public void onSteppedOnHook(World world, BlockPos pos, BlockState state, Entity entity, CallbackInfo ci) {
        if (((NoSlowV2) Modules.get().get(NoSlowV2.class)).isActive() && ((NoSlowV2) Modules.get().get(NoSlowV2.class)).slime.get()) {
            ci.cancel();
        }
    }
}
