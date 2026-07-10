package dev.abstr3act.addon.mixin;

import dev.abstr3act.addon.modules.Seraphim.MotionCamera;
import meteordevelopment.meteorclient.MeteorClient;
import net.minecraft.client.render.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin({Camera.class})
public abstract class MixinCamera {
    @ModifyArgs(
        method = {"update"},
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/render/Camera;setPos(DDD)V"
        )
    )
    private void setPosHook(Args args) {
        if (MotionCamera.INSTANCE.isActive() && !MeteorClient.mc.options.getPerspective().isFirstPerson()) {
            args.setAll(new Object[]{MotionCamera.INSTANCE.prevRenderX, MotionCamera.INSTANCE.prevRenderY, MotionCamera.INSTANCE.prevRenderZ});
        }
    }
}
