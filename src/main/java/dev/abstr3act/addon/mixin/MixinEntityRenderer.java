package dev.abstr3act.addon.mixin;

import dev.abstr3act.addon.modules.Compassion.AutoLag;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({EntityRenderer.class})
public abstract class MixinEntityRenderer<T extends Entity> {
    @Inject(
        method = {"render"},
        at = {@At("HEAD")},
        cancellable = true
    )
    private void onRender(T entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        if (entity instanceof ItemEntity && ((AutoLag) Modules.get().get(AutoLag.class)).isActive()) {
            ci.cancel();
        }
    }
}
