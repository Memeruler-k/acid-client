package dev.abstr3act.addon.mixin;

import dev.abstr3act.addon.modules.Seraphim.AntiWeb;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.block.BlockState;
import net.minecraft.block.CobwebBlock;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket.Action;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({CobwebBlock.class})
public class MixinCobwebBlock {
    @Inject(
        method = {"onEntityCollision"},
        at = {@At("HEAD")},
        cancellable = true
    )
    public void onEntityCollisionHook(BlockState state, World world, BlockPos pos, Entity entity, CallbackInfo ci) {
        if (((AntiWeb) Modules.get().get(AntiWeb.class)).isActive()
            && ((AntiWeb) Modules.get().get(AntiWeb.class)).mode.get() == AntiWeb.Mode.Ignore
            && entity == MeteorClient.mc.player) {
            ci.cancel();
            if (((AntiWeb) Modules.get().get(AntiWeb.class)).grim.get()) {
                MeteorClient.mc
                    .interactionManager
                    .sendSequencedPacket(MeteorClient.mc.world, id -> new PlayerActionC2SPacket(Action.STOP_DESTROY_BLOCK, pos, Direction.UP, id));
            }
        }
    }
}
