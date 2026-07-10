package dev.abstr3act.addon.utils;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.abstr3act.addon.enums.ColorMode;
import dev.abstr3act.addon.utils.color.Color;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import net.minecraft.client.render.*;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;

public class Render3DUtils {
    private static float prevCircleStep;
    private static float circleStep;

    public static void updateJello(float step) {
        prevCircleStep = circleStep;
        circleStep += step;
    }

    public static void drawJello(MatrixStack matrix, Entity target, SettingColor color, int segments) {
        double cs = prevCircleStep + (circleStep - prevCircleStep) * MeteorClient.mc.getRenderTickCounter().getTickDelta(true);
        double prevSinAnim = absSinAnimation(cs - 0.45F);
        double sinAnim = absSinAnimation(cs);
        double x = target.prevX
            + (target.getX() - target.prevX) * MeteorClient.mc.getRenderTickCounter().getTickDelta(true)
            - MeteorClient.mc.getEntityRenderDispatcher().camera.getPos().getX();
        double y = target.prevY
            + (target.getY() - target.prevY) * MeteorClient.mc.getRenderTickCounter().getTickDelta(true)
            - MeteorClient.mc.getEntityRenderDispatcher().camera.getPos().getY()
            + prevSinAnim * target.getHeight();
        double z = target.prevZ
            + (target.getZ() - target.prevZ) * MeteorClient.mc.getRenderTickCounter().getTickDelta(true)
            - MeteorClient.mc.getEntityRenderDispatcher().camera.getPos().getZ();
        double nextY = target.prevY
            + (target.getY() - target.prevY) * MeteorClient.mc.getRenderTickCounter().getTickDelta(true)
            - MeteorClient.mc.getEntityRenderDispatcher().camera.getPos().getY()
            + sinAnim * target.getHeight();
        matrix.push();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.begin(DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);

        for (int i = 0; i <= segments; i++) {
            float cos = (float) (
                x
                    + Math.cos(i * 6.28 / segments)
                    * (target.getBoundingBox().maxX - target.getBoundingBox().minX + (target.getBoundingBox().maxZ - target.getBoundingBox().minZ))
                    * 0.5
            );
            float sin = (float) (
                z
                    + Math.sin(i * 6.28 / segments)
                    * (target.getBoundingBox().maxX - target.getBoundingBox().minX + (target.getBoundingBox().maxZ - target.getBoundingBox().minZ))
                    * 0.5
            );
            bufferBuilder.vertex(matrix.peek().getPositionMatrix(), cos, (float) nextY, sin).color(color.getPacked());
            bufferBuilder.vertex(matrix.peek().getPositionMatrix(), cos, (float) y, sin).color(Render2DUtils.injectAlpha(color, 0).getPacked());
        }

        BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
        matrix.pop();
    }

    public static void drawCircle(MatrixStack matrix, Entity target, int segments, double customRadius) {
        double x = target.prevX
            + (target.getX() - target.prevX) * MeteorClient.mc.getRenderTickCounter().getTickDelta(true)
            - MeteorClient.mc.getEntityRenderDispatcher().camera.getPos().getX();
        double y = target.prevY
            + (target.getY() - target.prevY) * MeteorClient.mc.getRenderTickCounter().getTickDelta(true)
            - MeteorClient.mc.getEntityRenderDispatcher().camera.getPos().getX();
        double z = target.prevZ
            + (target.getZ() - target.prevZ) * MeteorClient.mc.getRenderTickCounter().getTickDelta(true)
            - MeteorClient.mc.getEntityRenderDispatcher().camera.getPos().getZ();
        double radius = customRadius > 0.0
            ? customRadius
            : (target.getBoundingBox().maxX - target.getBoundingBox().minX + target.getBoundingBox().maxZ - target.getBoundingBox().minZ) * 0.5;
        matrix.push();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.begin(DrawMode.LINE_STRIP, VertexFormats.POSITION_COLOR);
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);

        for (int i = 0; i <= segments; i++) {
            float angle = (float) (i * Math.PI * 2.0 / segments);
            float cos = (float) (x + Math.cos(angle) * radius);
            float sin = (float) (z + Math.sin(angle) * radius);
            bufferBuilder.vertex(matrix.peek().getPositionMatrix(), cos, (float) y, sin).color(255, 255, 255, 255);
        }

        BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
        matrix.pop();
    }

    public static void drawJelloRainbow(MatrixStack matrix, Entity target, SettingColor color, int segments) {
        double cs = prevCircleStep + (circleStep - prevCircleStep) * MeteorClient.mc.getRenderTickCounter().getTickDelta(true);
        double prevSinAnim = absSinAnimation(cs - 0.45F);
        double sinAnim = absSinAnimation(cs);
        double x = target.prevX
            + (target.getX() - target.prevX) * MeteorClient.mc.getRenderTickCounter().getTickDelta(true)
            - MeteorClient.mc.getEntityRenderDispatcher().camera.getPos().getX();
        double y = target.prevY
            + (target.getY() - target.prevY) * MeteorClient.mc.getRenderTickCounter().getTickDelta(true)
            - MeteorClient.mc.getEntityRenderDispatcher().camera.getPos().getY()
            + prevSinAnim * target.getHeight();
        double z = target.prevZ
            + (target.getZ() - target.prevZ) * MeteorClient.mc.getRenderTickCounter().getTickDelta(true)
            - MeteorClient.mc.getEntityRenderDispatcher().camera.getPos().getZ();
        double nextY = target.prevY
            + (target.getY() - target.prevY) * MeteorClient.mc.getRenderTickCounter().getTickDelta(true)
            - MeteorClient.mc.getEntityRenderDispatcher().camera.getPos().getY()
            + sinAnim * target.getHeight();
        matrix.push();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.begin(DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        float totalRadians = (float) (Math.PI * 2);

        for (int i = 0; i <= segments; i++) {
            float angle = i * totalRadians / segments;
            float cos = (float) (
                x
                    + Math.cos(angle)
                    * (target.getBoundingBox().maxX - target.getBoundingBox().minX + (target.getBoundingBox().maxZ - target.getBoundingBox().minZ))
                    * 0.5
            );
            float sin = (float) (
                z
                    + Math.sin(angle)
                    * (target.getBoundingBox().maxX - target.getBoundingBox().minX + (target.getBoundingBox().maxZ - target.getBoundingBox().minZ))
                    * 0.5
            );
            float normalizedSegment = (float) i / segments;
            float red = (float) (Math.sin(normalizedSegment * totalRadians) * 0.5 + 0.5);
            float green = (float) (Math.sin(normalizedSegment * totalRadians + 2.0F) * 0.5 + 0.5);
            float blue = (float) (Math.sin(normalizedSegment * totalRadians + 4.0F) * 0.5 + 0.5);
            SettingColor segmentColor = new SettingColor((int) (red * 255.0F), (int) (green * 255.0F), (int) (blue * 255.0F), color.a);
            bufferBuilder.vertex(matrix.peek().getPositionMatrix(), cos, (float) nextY, sin).color(segmentColor.getPacked());
            bufferBuilder.vertex(matrix.peek().getPositionMatrix(), cos, (float) y, sin).color(Render2DUtils.injectAlpha(segmentColor, 0).getPacked());
        }

        BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
        matrix.pop();
    }

    public static void drawJello(MatrixStack matrix, Entity target, SettingColor colorStart, SettingColor colorEnd, int segments) {
        double cs = prevCircleStep + (circleStep - prevCircleStep) * MeteorClient.mc.getRenderTickCounter().getTickDelta(true);
        double prevSinAnim = absSinAnimation(cs - 0.45F);
        double sinAnim = absSinAnimation(cs);
        double x = target.prevX
            + (target.getX() - target.prevX) * MeteorClient.mc.getRenderTickCounter().getTickDelta(true)
            - MeteorClient.mc.getEntityRenderDispatcher().camera.getPos().getX();
        double y = target.prevY
            + (target.getY() - target.prevY) * MeteorClient.mc.getRenderTickCounter().getTickDelta(true)
            - MeteorClient.mc.getEntityRenderDispatcher().camera.getPos().getY()
            + prevSinAnim * target.getHeight();
        double z = target.prevZ
            + (target.getZ() - target.prevZ) * MeteorClient.mc.getRenderTickCounter().getTickDelta(true)
            - MeteorClient.mc.getEntityRenderDispatcher().camera.getPos().getZ();
        double nextY = target.prevY
            + (target.getY() - target.prevY) * MeteorClient.mc.getRenderTickCounter().getTickDelta(true)
            - MeteorClient.mc.getEntityRenderDispatcher().camera.getPos().getY()
            + sinAnim * target.getHeight();
        matrix.push();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.begin(DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        float totalRadians = (float) (Math.PI * 2);

        for (int i = 0; i <= segments; i++) {
            float angle = i * totalRadians / segments;
            float cos = (float) (
                x
                    + Math.cos(angle)
                    * (target.getBoundingBox().maxX - target.getBoundingBox().minX + (target.getBoundingBox().maxZ - target.getBoundingBox().minZ))
                    * 0.5
            );
            float sin = (float) (
                z
                    + Math.sin(angle)
                    * (target.getBoundingBox().maxX - target.getBoundingBox().minX + (target.getBoundingBox().maxZ - target.getBoundingBox().minZ))
                    * 0.5
            );
            float lerpFactor;
            if (i <= segments / 2) {
                lerpFactor = (float) i / (segments / 2);
            } else {
                lerpFactor = (float) (i - segments / 2) / (segments / 2);
            }

            int red;
            int green;
            int blue;
            if (i <= segments / 2) {
                red = (int) (colorStart.r + lerpFactor * (colorEnd.r - colorStart.r));
                green = (int) (colorStart.g + lerpFactor * (colorEnd.g - colorStart.g));
                blue = (int) (colorStart.b + lerpFactor * (colorEnd.b - colorStart.b));
            } else {
                red = (int) (colorEnd.r + lerpFactor * (colorStart.r - colorEnd.r));
                green = (int) (colorEnd.g + lerpFactor * (colorStart.g - colorEnd.g));
                blue = (int) (colorEnd.b + lerpFactor * (colorStart.b - colorEnd.b));
            }

            int alpha = (int) (colorStart.a + lerpFactor * (colorEnd.a - colorStart.a));
            SettingColor segmentColor = new SettingColor(red, green, blue, alpha);
            bufferBuilder.vertex(matrix.peek().getPositionMatrix(), cos, (float) nextY, sin).color(segmentColor.getPacked());
            bufferBuilder.vertex(matrix.peek().getPositionMatrix(), cos, (float) y, sin).color(Render2DUtils.injectAlpha(segmentColor, 0).getPacked());
        }

        BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
        matrix.pop();
    }

    public static int getColor(ColorMode.ColorSettings settings, int stage) {
        return switch ((ColorMode) settings.mode.get()) {
            case Custom -> ((SettingColor) settings.color.get()).getPacked();
            case Rainbow -> rainbow(stage, 1.0F, 1.0F).getPacked();
            case TwoColor -> getColor2(settings, stage).getPacked();
        };
    }

    public static Color TwoColoreffect(SettingColor cl1, SettingColor cl2, double speed) {
        double thing = speed / 4.0 % 1.0;
        float val = MathHelper.clamp((float) Math.sin((Math.PI * 6) * thing) / 2.0F + 0.5F, 0.0F, 1.0F);
        return new Color(
            (int) lerp(cl1.r / 255.0F, cl2.r / 255.0F, val),
            (int) lerp(cl1.g / 255.0F, cl2.g / 255.0F, val),
            (int) lerp(cl1.b / 255.0F, cl2.b / 255.0F, val)
        );
    }

    public static float lerp(float a, float b, float f) {
        return a + f * (b - a);
    }

    public static Color getColor2(ColorMode.ColorSettings settings, int offset) {
        return TwoColoreffect(
            (SettingColor) settings.color.get(),
            (SettingColor) settings.color2.get(),
            Math.abs(System.currentTimeMillis() / 10L) / 100.0 + offset * ((20.0F - (settings.colorOffset.get()).intValue()) / 200.0F)
        );
    }

    public static Color rainbow(int delay, float saturation, float brightness) {
        double rainbow = Math.ceil((float) (System.currentTimeMillis() + delay) / 16.0F);
        rainbow %= 360.0;
        return new Color(java.awt.Color.HSBtoRGB((float) (rainbow / 360.0), saturation, brightness));
    }

    public static Color rainbow(int speed, int index, float saturation, float brightness, float opacity) {
        int angle = (int) ((System.currentTimeMillis() / speed + index) % 360L);
        float hue = angle / 360.0F;
        Color color = new Color(java.awt.Color.HSBtoRGB(hue, saturation, brightness));
        return new Color(color.r, color.g, color.b, Math.max(0, Math.min(255, (int) (opacity * 255.0F))));
    }

    private static double absSinAnimation(double input) {
        return Math.abs(1.0 + Math.sin(input)) / 2.0;
    }
}
