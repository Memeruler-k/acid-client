package dev.abstr3act.addon.mixin;

import dev.abstr3act.addon.utils.RotationUtil;
import dev.abstr3act.addon.utils.render.shaders.AnimationUtility;
import meteordevelopment.meteorclient.MeteorClient;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin({LivingEntityRenderer.class})
public abstract class LivingEntityRendererMixin<T extends LivingEntity, M extends EntityModel<T>> {
    @Unique
    float yawA;
    @Unique
    float pitchA;

    @ModifyVariable(
        method = {"render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V"},
        ordinal = 2,
        at = @At(
            value = "STORE",
            ordinal = 0
        )
    )
    public float changeYaw2(float oldValue, LivingEntity entity) {
        if (RotationUtil.currentYaw == null) {
            return oldValue;
        } else {
            this.yawA = AnimationUtility.ease(this.yawA, RotationUtil.currentYaw, 5.0F);
            return entity.equals(MeteorClient.mc.player) ? this.yawA : oldValue;
        }
    }

    @ModifyVariable(
        method = {"render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V"},
        ordinal = 3,
        at = @At(
            value = "STORE",
            ordinal = 0
        )
    )
    public float changeHeadYaw2(float oldValue, LivingEntity entity) {
        if (RotationUtil.currentYaw == null) {
            return oldValue;
        } else {
            this.yawA = AnimationUtility.ease(this.yawA, RotationUtil.currentYaw, 5.0F);
            return entity.equals(MeteorClient.mc.player) ? this.yawA : oldValue;
        }
    }

    @ModifyVariable(
        method = {"render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V"},
        ordinal = 5,
        at = @At(
            value = "STORE",
            ordinal = 3
        )
    )
    public float changePitch2(float oldValue, LivingEntity entity) {
        if (RotationUtil.currentPitch == null) {
            return oldValue;
        } else {
            this.pitchA = AnimationUtility.ease(this.pitchA, RotationUtil.currentPitch, 5.0F);
            return entity.equals(MeteorClient.mc.player) ? this.pitchA : oldValue;
        }
    }
}
