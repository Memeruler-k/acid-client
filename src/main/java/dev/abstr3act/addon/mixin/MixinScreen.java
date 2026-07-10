package dev.abstr3act.addon.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.abstr3act.addon.utils.render.Render2DEngine;
import meteordevelopment.meteorclient.MeteorClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.Identifier;
import org.lwjgl.opengl.GL13;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({Screen.class})
public abstract class MixinScreen {
    @Inject(
        method = {"renderPanoramaBackground"},
        at = {@At("HEAD")},
        cancellable = true
    )
    public void renderPanoramaBackgroundHook(DrawContext context, float delta, CallbackInfo ci) {
        if (MeteorClient.mc.world == null) {
            float screenWidth = MeteorClient.mc.getWindow().getScaledWidth();
            float screenHeight = MeteorClient.mc.getWindow().getScaledHeight();
            float scaleFactor = 1.2F;
            float scaledWidth = screenWidth * scaleFactor;
            float scaledHeight = screenHeight * scaleFactor;
            float maxOffsetX = (scaledWidth - screenWidth) / 2.0F;
            float maxOffsetY = (scaledHeight - screenHeight) / 2.0F;
            Identifier background = Identifier.of("acid", "gui/bg_7.png");
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.enableDepthTest();
            GL13.glEnable(32925);
            RenderSystem.setShaderTexture(0, background);
            Render2DEngine.renderTextureX(context.getMatrices(), -maxOffsetX, -maxOffsetY, scaledWidth, scaledHeight, 0.0F, 0.0F, 1920.0, 1080.0, 1920.0, 1080.0);
            GL13.glDisable(32925);
            RenderSystem.disableBlend();
            RenderSystem.disableDepthTest();
            ci.cancel();
        }
    }
}
