package dev.abstr3act.addon.mixin;

import dev.abstr3act.addon.utils.FontFix;
import meteordevelopment.meteorclient.renderer.GL;
import meteordevelopment.meteorclient.renderer.Mesh;
import meteordevelopment.meteorclient.renderer.text.CustomTextRenderer;
import meteordevelopment.meteorclient.renderer.text.FontFace;
import meteordevelopment.meteorclient.renderer.text.TextRenderer;
import meteordevelopment.meteorclient.utils.render.color.Color;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.nio.ByteBuffer;

@Mixin(
    value = {CustomTextRenderer.class},
    remap = false
)
public abstract class CustomTextRendererMixin implements TextRenderer {
    @Unique
    FontFix[] fixedFonts = new FontFix[5];
    @Shadow
    @Final
    private Mesh mesh;
    @Shadow
    private boolean building;
    @Shadow
    private boolean scaleOnly;
    @Shadow
    private double fontScale = 1.0;
    @Shadow
    private double scale = 1.0;
    @Unique
    private FontFix fixedFont;

    @Inject(
        method = {"<init>"},
        at = {@At("RETURN")},
        locals = LocalCapture.CAPTURE_FAILHARD
    )
    public void onInit(FontFace fontFace, CallbackInfo ci, byte[] bytes, ByteBuffer buffer) {
        for (int i = 0; i < this.fixedFonts.length; i++) {
            this.fixedFonts[i] = new FontFix(buffer, (int) Math.round(27.0 * (i * 0.5 + 1.0)));
        }
    }

    @Overwrite
    public double getWidth(String text, int length, boolean shadow) {
        if (text.isEmpty()) {
            return 0.0;
        } else {
            FontFix font = this.building ? this.fixedFont : this.fixedFonts[0];
            return (font.getWidth(text, length) + (shadow ? 1 : 0)) * this.scale / 1.5;
        }
    }

    @Overwrite
    public double getHeight(boolean shadow) {
        FontFix font = this.building ? this.fixedFont : this.fixedFonts[0];
        return (font.getHeight() + 1 + (shadow ? 1 : 0)) * this.scale / 1.5;
    }

    @Overwrite
    public void begin(double scale, boolean scaleOnly, boolean big) {
        if (this.building) {
            throw new RuntimeException("CustomTextRenderer.begin() called twice");
        } else {
            if (!scaleOnly) {
                this.mesh.begin();
            }

            if (big) {
                this.fixedFont = this.fixedFonts[this.fixedFonts.length - 1];
            } else {
                double scaleA = Math.floor(scale * 10.0) / 10.0;
                int scaleI;
                if (scaleA >= 3.0) {
                    scaleI = 5;
                } else if (scaleA >= 2.5) {
                    scaleI = 4;
                } else if (scaleA >= 2.0) {
                    scaleI = 3;
                } else if (scaleA >= 1.5) {
                    scaleI = 2;
                } else {
                    scaleI = 1;
                }

                this.fixedFont = this.fixedFonts[scaleI - 1];
            }

            this.building = true;
            this.scaleOnly = scaleOnly;
            this.fontScale = this.fixedFont.getHeight() / 27.0;
            this.scale = 1.0 + (scale - this.fontScale) / this.fontScale;
        }
    }

    @Overwrite
    public void end(MatrixStack matrices) {
        if (!this.building) {
            throw new RuntimeException("CustomTextRenderer.end() called without calling begin()");
        } else {
            if (!this.scaleOnly) {
                this.mesh.end();
                GL.bindTexture(this.fixedFont.texture.getGlId());
                this.mesh.render(matrices);
            }

            this.building = false;
            this.scale = 1.0;
        }
    }

    /**
     * @author
     * @reason
     */
    @Overwrite
    public double render(String text, double x, double y, Color color, boolean shadow) {
        boolean wasBuilding = this.building;
        if (!wasBuilding) {
            this.begin();
        }

        double width;
        if (shadow) {
            int preShadowA = CustomTextRenderer.SHADOW_COLOR.a;
            CustomTextRenderer.SHADOW_COLOR.a = (int) (color.a / 255.0 * preShadowA);
            width = this.fixedFont
                .render(
                    this.mesh, text, x + this.fontScale * this.scale / 1.5, y + this.fontScale * this.scale / 1.5, CustomTextRenderer.SHADOW_COLOR, this.scale / 1.5
                );
            this.fixedFont.render(this.mesh, text, x, y, color, this.scale / 1.5);
            CustomTextRenderer.SHADOW_COLOR.a = preShadowA;
        } else {
            width = this.fixedFont.render(this.mesh, text, x, y, color, this.scale / 1.5);
        }

        if (!wasBuilding) {
            this.end();
        }

        return width;
    }
}
