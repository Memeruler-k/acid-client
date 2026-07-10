package dev.abstr3act.addon.utils.render;

import com.mojang.blaze3d.platform.GlStateManager.DstFactor;
import com.mojang.blaze3d.platform.GlStateManager.SrcFactor;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.abstr3act.addon.modules.Lacrymira.ColorSetting;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import net.minecraft.client.render.*;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;
import org.joml.Matrix4f;

import java.awt.*;

public class CaptureMark {
    public static final Identifier capture2 = Identifier.of("acid", "render/capture_2.png");
    public static final Identifier capture = Identifier.of("acid", "render/capture.png");
    public static final Identifier capture_rounded = Identifier.of("acid", "render/capture_rounded.png");
    public static final Identifier capture_triangle = Identifier.of("acid", "render/capture_triangle.png");
    private static float espValue = 1.0F;
    private static float prevEspValue;
    private static float espSpeed = 1.0F;
    private static boolean flipSpeed;

    public static Identifier getCaptures(Captures captures) {
        switch (captures) {
            case CAPTURE:
                return capture;
            case CAPTURE2:
                return capture2;
            case CAPTURE_ROUNDED:
                return capture_rounded;
            case CAPTURE_TRIANGLE:
                return capture_triangle;
            case null:
            default:
                return null;
        }
    }

    public static void render(Entity target, Captures c) {
        Camera camera = MeteorClient.mc.gameRenderer.getCamera();
        double tPosX = Render2DEngine.interpolate(target.prevX, target.getX(), MeteorClient.mc.getRenderTickCounter().getTickDelta(true)) - camera.getPos().x;
        double tPosY = Render2DEngine.interpolate(target.prevY, target.getY(), MeteorClient.mc.getRenderTickCounter().getTickDelta(true)) - camera.getPos().y;
        double tPosZ = Render2DEngine.interpolate(target.prevZ, target.getZ(), MeteorClient.mc.getRenderTickCounter().getTickDelta(true)) - camera.getPos().z;
        MatrixStack matrices = new MatrixStack();
        RenderSystem.disableDepthTest();
        RenderSystem.disableCull();
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(camera.getYaw() + 180.0F));
        matrices.translate(tPosX, tPosY + target.getEyeHeight(target.getPose()) / 2.0F, tPosZ);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-camera.getYaw()));
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
        matrices.multiply(
            RotationAxis.POSITIVE_Z
                .rotationDegrees(Render2DEngine.interpolateFloat(prevEspValue, espValue, MeteorClient.mc.getRenderTickCounter().getTickDelta(true)))
        );
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(SrcFactor.SRC_ALPHA, DstFactor.ONE);
        RenderSystem.setShaderTexture(0, getCaptures(c));
        matrices.translate(-0.75, -0.75, -0.01);
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        RenderSystem.setShader(GameRenderer::getPositionTexColorProgram);
        BufferBuilder bufferBuilder = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
        bufferBuilder.vertex(matrix, 0.0F, 1.5F, 0.0F).texture(0.0F, 1.0F).color(getColor(90).getRGB());
        bufferBuilder.vertex(matrix, 1.5F, 1.5F, 0.0F).texture(1.0F, 1.0F).color(getColor(0).getRGB());
        bufferBuilder.vertex(matrix, 1.5F, 0.0F, 0.0F).texture(1.0F, 0.0F).color(getColor(180).getRGB());
        bufferBuilder.vertex(matrix, 0.0F, 0.0F, 0.0F).texture(0.0F, 0.0F).color(getColor(270).getRGB());
        BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
        RenderSystem.enableCull();
        RenderSystem.enableDepthTest();
        RenderSystem.blendFunc(SrcFactor.SRC_ALPHA, DstFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.disableBlend();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    public static Color getColor(int count) {
        ColorSetting colorSetting = (ColorSetting) Modules.get().get(ColorSetting.class);
        int colorSpeed = colorSetting.colorSpeed.get();

        return switch ((colorModeEn) colorSetting.mode.get()) {
            case Sky -> Render2DEngine.skyRainbow(colorSpeed, count);
            case LightRainbow -> Render2DEngine.rainbow(colorSpeed, count, 0.6F, 1.0F, 1.0F);
            case Rainbow -> Render2DEngine.rainbow(colorSpeed, count, 1.0F, 1.0F, 1.0F);
            case Fade -> Render2DEngine.fade(colorSpeed, count, (SettingColor) colorSetting.hcolor1.get(), 1.0F);
            case DoubleColor -> Render2DEngine.TwoColoreffect(
                (SettingColor) colorSetting.hcolor1.get(), (SettingColor) colorSetting.acolor.get(), colorSpeed, count
            );
            case Analogous -> Render2DEngine.interpolateColorsBackAndForth(
                colorSpeed,
                count,
                new Color(((SettingColor) colorSetting.hcolor1.get()).getPacked()),
                Render2DEngine.getAnalogousColor((SettingColor) colorSetting.acolor.get()),
                true
            );
            default -> new Color(((SettingColor) colorSetting.hcolor1.get()).getPacked());
        };
    }

    public static void renderUwU(Entity target, double x, double y, double z, Identifier identifier, float scale1, float scale2) {
        Camera camera = MeteorClient.mc.gameRenderer.getCamera();
        double tPosX = Render2DEngine.interpolate(target.prevX, target.getX(), MeteorClient.mc.getRenderTickCounter().getTickDelta(true)) - camera.getPos().x;
        double tPosY = Render2DEngine.interpolate(target.prevY, target.getY(), MeteorClient.mc.getRenderTickCounter().getTickDelta(true)) - camera.getPos().y;
        double tPosZ = Render2DEngine.interpolate(target.prevZ, target.getZ(), MeteorClient.mc.getRenderTickCounter().getTickDelta(true)) - camera.getPos().z;
        MatrixStack matrices = new MatrixStack();
        RenderSystem.disableDepthTest();
        RenderSystem.disableCull();
        matrices.translate(tPosX, tPosY + target.getEyeHeight(target.getPose()) / 2.0F, tPosZ);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-camera.getYaw()));
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(SrcFactor.SRC_ALPHA, DstFactor.ONE);
        RenderSystem.setShaderTexture(0, identifier);
        matrices.translate(x, y, z);
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        BufferBuilder bufferBuilder = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
        RenderSystem.setShader(GameRenderer::getPositionTexColorProgram);
        bufferBuilder.vertex(matrix, 0.0F, scale1, 0.0F).texture(0.0F, 0.0F).color(255, 255, 255, 255);
        bufferBuilder.vertex(matrix, scale1, scale1, 0.0F).texture(scale2, 0.0F).color(255, 255, 255, 255);
        bufferBuilder.vertex(matrix, scale1, 0.0F, 0.0F).texture(scale2, scale2).color(255, 255, 255, 255);
        bufferBuilder.vertex(matrix, 0.0F, 0.0F, 0.0F).texture(0.0F, scale2).color(255, 255, 255, 255);
        RenderSystem.disableBlend();
        RenderSystem.enableDepthTest();
        RenderSystem.blendFunc(SrcFactor.ONE, DstFactor.ZERO);
        BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
        RenderSystem.enableCull();
        RenderSystem.enableDepthTest();
        RenderSystem.blendFunc(SrcFactor.SRC_ALPHA, DstFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.disableBlend();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    public static void tick() {
        prevEspValue = espValue;
        espValue = espValue + espSpeed;
        if (espSpeed > 25.0F) {
            flipSpeed = true;
        }

        if (espSpeed < -25.0F) {
            flipSpeed = false;
        }

        espSpeed = flipSpeed ? espSpeed - 0.5F : espSpeed + 0.5F;
    }

    public static enum Captures {
        CAPTURE2,
        CAPTURE,
        CAPTURE_ROUNDED,
        CAPTURE_TRIANGLE;
    }

    public static enum colorModeEn {
        Static,
        Sky,
        LightRainbow,
        Rainbow,
        Fade,
        DoubleColor,
        Analogous;
    }
}
