package dev.abstr3act.addon.mixin;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.abstr3act.addon.utils.render.Render2DEngine;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.SplashOverlay;
import net.minecraft.resource.ResourceReload;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.*;
import java.util.Optional;
import java.util.function.Consumer;

@Mixin({SplashOverlay.class})
public abstract class MixinSplashOverlay {
    private static final Identifier LOGO = Identifier.of("acid", "gui/lg_2.png");
    @Final
    @Shadow
    private boolean reloading;
    @Shadow
    private float progress;
    @Shadow
    private long reloadCompleteTime = -1L;
    @Shadow
    private long reloadStartTime = -1L;
    @Final
    @Shadow
    private ResourceReload reload;
    @Final
    @Shadow
    private Consumer<Optional<Throwable>> exceptionHandler;

    private static int withAlpha(int color, int alpha) {
        return color & 16777215 | alpha << 24;
    }

    @Inject(
        method = {"render"},
        at = {@At("HEAD")},
        cancellable = true
    )
    public void render(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        ci.cancel();
        this.renderCustom(context, mouseX, mouseY, delta);
    }

    public void renderCustom(DrawContext context, int mouseX, int mouseY, float delta) {
        MinecraftClient mc = MinecraftClient.getInstance();
        int i = mc.getWindow().getScaledWidth();
        int j = mc.getWindow().getScaledHeight();
        long l = Util.getMeasuringTimeMs();
        if (this.reloading && this.reloadStartTime == -1L) {
            this.reloadStartTime = l;
        }

        float f = this.reloadCompleteTime > -1L ? (float) (l - this.reloadCompleteTime) / 1000.0F : -1.0F;
        float g = this.reloadStartTime > -1L ? (float) (l - this.reloadStartTime) / 500.0F : -1.0F;
        float h;
        if (f >= 1.0F) {
            if (mc.currentScreen != null) {
                mc.currentScreen.render(context, 0, 0, delta);
            }

            int k = MathHelper.ceil((1.0F - MathHelper.clamp(f - 1.0F, 0.0F, 1.0F)) * 255.0F);
            context.fill(0, 0, i, j, withAlpha(new Color(4152434).getRGB(), k));
            h = 1.0F - MathHelper.clamp(f - 1.0F, 0.0F, 1.0F);
        } else if (this.reloading) {
            if (mc.currentScreen != null && g < 1.0F) {
                mc.currentScreen.render(context, mouseX, mouseY, delta);
            }

            int k = MathHelper.ceil(MathHelper.clamp(g, 0.15, 1.0) * 255.0);
            context.fill(0, 0, i, j, withAlpha(new Color(4152434).getRGB(), k));
            h = MathHelper.clamp(g, 0.0F, 1.0F);
        } else {
            int k = new Color(4152434).getRGB();
            float m = (k >> 16 & 0xFF) / 255.0F;
            float n = (k >> 8 & 0xFF) / 255.0F;
            float o = (k & 0xFF) / 255.0F;
            GlStateManager._clearColor(m, n, o, 1.0F);
            GlStateManager._clear(16384, MinecraftClient.IS_SYSTEM_MAC);
            h = 1.0F;
        }

        int var20 = (int) (context.getScaledWindowWidth() * 0.5);
        int p = (int) (context.getScaledWindowHeight() * 0.5);
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(770, 1);
        RenderSystem.setShaderColor(0.1F, 0.1F, 0.1F, h);
        context.drawTexture(LOGO, var20 - 150, p - 35, 0.0F, 0.0F, 300, 70, 300, 70);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, h);
        Render2DEngine.addWindow(context.getMatrices(), var20 - 150, p - 35, var20 - 150 + 300.0F * this.progress, p + 35, 1.0);
        context.drawTexture(LOGO, var20 - 150, p - 35, 0.0F, 0.0F, 300, 70, 300, 70);
        Render2DEngine.popWindow();
        float t = this.reload.getProgress();
        this.progress = MathHelper.clamp(this.progress * 0.95F + t * 0.050000012F, 0.0F, 1.0F);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
        if (f >= 2.0F) {
            mc.setOverlay(null);
        }

        if (this.reloadCompleteTime == -1L && this.reload.isComplete() && (!this.reloading || g >= 2.0F)) {
            try {
                this.reload.throwException();
                this.exceptionHandler.accept(Optional.empty());
            } catch (Throwable var17) {
                this.exceptionHandler.accept(Optional.of(var17));
            }

            this.reloadCompleteTime = Util.getMeasuringTimeMs();
            if (mc.currentScreen != null) {
                mc.currentScreen.init(mc, mc.getWindow().getScaledWidth(), mc.getWindow().getScaledHeight());
            }
        }
    }
}
