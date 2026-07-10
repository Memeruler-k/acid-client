package dev.abstr3act.addon.mixin;

import dev.abstr3act.addon.modules.Seraphim.NoSlowV2;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.block.BlockState;
import net.minecraft.block.SweetBerryBushBlock;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({SweetBerryBushBlock.class})
public class MixinSweetBerryBushBlock {
    @Inject(
        method = {"onEntityCollision"},
        at = {@At("HEAD")},
        cancellable = true
    )
    public void onEntityCollisionHook(BlockState state, World world, BlockPos pos, Entity entity, CallbackInfo ci) {
        if (((NoSlowV2) Modules.get().get(NoSlowV2.class)).isActive() && ((NoSlowV2) Modules.get().get(NoSlowV2.class)).sweetBerryBush.get()) {
            ci.cancel();
        }
    }
}
