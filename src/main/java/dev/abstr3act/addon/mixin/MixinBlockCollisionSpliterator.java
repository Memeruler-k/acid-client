package dev.abstr3act.addon.mixin;

import dev.abstr3act.addon.events.EventCollision;
import meteordevelopment.meteorclient.MeteorClient;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockCollisionSpliterator;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(
    value = {BlockCollisionSpliterator.class},
    priority = 800
)
public abstract class MixinBlockCollisionSpliterator {
    @Redirect(
        method = {"computeNext"},
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/BlockView;getBlockState(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/block/BlockState;"
        )
    )
    private BlockState computeNextHook(BlockView instance, BlockPos blockPos) {
        EventCollision event = new EventCollision(instance.getBlockState(blockPos), blockPos);
        MeteorClient.EVENT_BUS.post(event);
        return event.getState();
    }
}
