package dev.abstr3act.addon.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.abstr3act.addon.module.BaseModule;
import dev.abstr3act.addon.utils.Render3DEngine;
import dev.abstr3act.addon.utils.render.shaders.satin.impl.ReloadableShaderEffectManager;
import meteordevelopment.meteorclient.MeteorClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.resource.ResourceFactory;
import net.minecraft.util.math.RotationAxis;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({GameRenderer.class})
public abstract class MixinGameRenderer {
    @Shadow
    public abstract void tick();

    @Inject(
        method = {"loadPrograms"},
        at = {@At("RETURN")}
    )
    private void loadSatinPrograms(ResourceFactory factory, CallbackInfo ci) {
        ReloadableShaderEffectManager.INSTANCE.reload(factory);
    }

    @Inject(
        at = {@At(
            value = "FIELD",
            target = "Lnet/minecraft/client/render/GameRenderer;renderHand:Z",
            opcode = 180,
            ordinal = 0
        )},
        method = {"renderWorld"}
    )
    void render3dHook(RenderTickCounter tickCounter, CallbackInfo ci) {
        if (!BaseModule.fullNullCheck()) {
            Camera camera = MeteorClient.mc.gameRenderer.getCamera();
            MatrixStack matrixStack = new MatrixStack();
            RenderSystem.getModelViewStack().pushMatrix().mul(matrixStack.peek().getPositionMatrix());
            matrixStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
            matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(camera.getYaw() + 180.0F));
            RenderSystem.applyModelViewMatrix();
            Render3DEngine.lastProjMat.set(RenderSystem.getProjectionMatrix());
            Render3DEngine.lastModMat.set(RenderSystem.getModelViewMatrix());
            Render3DEngine.lastWorldSpaceMatrix.set(matrixStack.peek().getPositionMatrix());
            Render3DEngine.onRender3D(matrixStack);
            RenderSystem.getModelViewStack().popMatrix();
            RenderSystem.applyModelViewMatrix();
        }
    }
}
