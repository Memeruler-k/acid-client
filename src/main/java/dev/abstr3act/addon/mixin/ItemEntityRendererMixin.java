package dev.abstr3act.addon.mixin;

import dev.abstr3act.addon.modules.Compassion.AutoLag;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.ItemEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.ItemEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({ItemEntityRenderer.class})
public abstract class ItemEntityRendererMixin {
    @Inject(
        method = {"render"},
        at = {@At("HEAD")},
        cancellable = true
    )
    private void cancelRender(
        ItemEntity itemEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo ci
    ) {
        if (((AutoLag) Modules.get().get(AutoLag.class)).isActive()) {
            ci.cancel();
        }
    }
}
