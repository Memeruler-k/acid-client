package dev.abstr3act.addon.mixin;

import dev.abstr3act.addon.manager.RotationManager;
import dev.abstr3act.addon.modules.Compassion.MoveFix;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({LivingEntityRenderer.class})
public abstract class MixinLivingEntityRenderer<T extends LivingEntity, M extends EntityModel<T>> {
    @Unique
    private LivingEntity lastEntity;
    @Unique
    private float originalYaw;
    @Unique
    private float originalHeadYaw;
    @Unique
    private float originalBodyYaw;
    @Unique
    private float originalPitch;
    @Unique
    private float originalPrevYaw;
    @Unique
    private float originalPrevHeadYaw;
    @Unique
    private float originalPrevBodyYaw;

    @Inject(
        method = {"render"},
        at = {@At("HEAD")}
    )
    public void onRenderPre(T livingEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo ci) {
        if (MoveFix.INSTANCE.isActive()) {
            if (MinecraftClient.getInstance().player != null && livingEntity == MinecraftClient.getInstance().player && MoveFix.INSTANCE.rotation.get()) {
                this.originalYaw = livingEntity.getYaw();
                this.originalHeadYaw = livingEntity.headYaw;
                this.originalBodyYaw = livingEntity.bodyYaw;
                this.originalPitch = livingEntity.getPitch();
                this.originalPrevYaw = livingEntity.prevYaw;
                this.originalPrevHeadYaw = livingEntity.prevHeadYaw;
                this.originalPrevBodyYaw = livingEntity.prevBodyYaw;
                livingEntity.setYaw(RotationManager.getRenderYawOffset());
                livingEntity.headYaw = RotationManager.getRotationYawHead();
                livingEntity.bodyYaw = RotationManager.getRenderYawOffset();
                livingEntity.setPitch(RotationManager.getRenderPitch());
                livingEntity.prevYaw = RotationManager.getPrevRenderYawOffset();
                livingEntity.prevHeadYaw = RotationManager.getPrevRotationYawHead();
                livingEntity.prevBodyYaw = RotationManager.getPrevRenderYawOffset();
                livingEntity.prevPitch = RotationManager.getPrevPitch();
            }

            this.lastEntity = livingEntity;
        }
    }

    @Inject(
        method = {"render"},
        at = {@At("TAIL")}
    )
    public void onRenderPost(T livingEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo ci) {
        if (MoveFix.INSTANCE.isActive()
            && MinecraftClient.getInstance().player != null
            && livingEntity == MinecraftClient.getInstance().player
            && MoveFix.INSTANCE.rotation.get()) {
            livingEntity.setYaw(this.originalYaw);
            livingEntity.headYaw = this.originalHeadYaw;
            livingEntity.bodyYaw = this.originalBodyYaw;
            livingEntity.setPitch(this.originalPitch);
            livingEntity.prevYaw = this.originalPrevYaw;
            livingEntity.prevHeadYaw = this.originalPrevHeadYaw;
            livingEntity.prevBodyYaw = this.originalPrevBodyYaw;
            livingEntity.prevPitch = this.originalPitch;
        }
    }
}
