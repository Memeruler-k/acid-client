package dev.abstr3act.addon.utils.render;

import com.mojang.blaze3d.platform.GlStateManager.DstFactor;
import com.mojang.blaze3d.platform.GlStateManager.SrcFactor;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.abstr3act.addon.modules.Compassion.BlurSetting;
import dev.abstr3act.addon.utils.render.shaders.BlurProgram;
import dev.abstr3act.addon.utils.render.shaders.HudShader;
import dev.abstr3act.addon.utils.render.shaders.MainMenuProgram;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import net.minecraft.client.render.*;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import org.apache.commons.lang3.RandomStringUtils;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Stack;

public class Render2DEngine {
    public static final Identifier star = Identifier.of("acid", "particles/star.png");
    public static final Identifier heart = Identifier.of("acid", "particles/heart.png");
    public static final Identifier pure = Identifier.of("acid", "particles/hit_pure.png");
    public static final Identifier firefly = Identifier.of("acid", "particles/firefly.png");
    public static final Identifier bubble = Identifier.of("acid", "particles/hitbubble.png");
    static final Stack<Rectangle> clipStack = new Stack<>();
    public static HudShader HUD_SHADER;
    public static BlurProgram BLUR_PROGRAM;
    public static HashMap<Integer, BlurredShadow> shadowCache = new HashMap<>();
    public static HashMap<Integer, BlurredShadow> shadowCache1 = new HashMap<>();
    public static MainMenuProgram MAIN_MENU_PROGRAM;

    public static void initShaders() {
        HUD_SHADER = new HudShader();
        BLUR_PROGRAM = new BlurProgram();
    }

    public static boolean isHovered(double mouseX, double mouseY, double x, double y, double width, double height) {
        return mouseX >= x && mouseX - width <= x && mouseY >= y && mouseY - height <= y;
    }

    public static void startAntiAtlas() {
        GL11.glEnable(2832);
        GL11.glEnable(2848);
        GL11.glHint(3153, 4354);
        GL11.glHint(3154, 4354);
    }

    public static void doAntiAtlas() {
        GL11.glTexParameteri(3553, 10242, 10497);
        GL11.glTexParameteri(3553, 10243, 10497);
        GL11.glTexParameteri(3553, 10241, 9729);
        GL11.glTexParameteri(3553, 10240, 9729);
    }

    public static void stopAntiAtlas() {
        GL11.glDisable(2832);
        GL11.glDisable(2848);
    }

    public static Color applyOpacity(Color color, float opacity) {
        opacity = Math.min(1.0F, Math.max(0.0F, opacity));
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), (int) (color.getAlpha() * opacity));
    }

    public static int applyOpacity(int color_int, float opacity) {
        opacity = Math.min(1.0F, Math.max(0.0F, opacity));
        Color color = new Color(color_int);
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), (int) (color.getAlpha() * opacity)).getRGB();
    }

    public static void popWindow() {
        clipStack.pop();
        if (clipStack.empty()) {
            endScissor();
        } else {
            Rectangle r = clipStack.peek();
            beginScissor(r.x, r.y, r.x1, r.y1);
        }
    }

    public static void drawMainMenuShader(MatrixStack matrices, float x, float y, float width, float height) {
        BufferBuilder bb = preShaderDraw(matrices, x, y, width, height);
        MAIN_MENU_PROGRAM.setParameters(x, y, width, height);
        MAIN_MENU_PROGRAM.use();
        BufferRenderer.drawWithGlobalProgram(bb.end());
        endRender();
    }

    public static void endScissor() {
        RenderSystem.disableScissor();
    }

    public static void addWindow(MatrixStack stack, float x, float y, float x1, float y1, double animation_factor) {
        float h = y + y1;
        float h2 = (float) (h * (1.0 - MathUtility.clamp(animation_factor, 0.0, 1.0025F)));
        float y3 = y + h2;
        float x4 = x1;
        float y4 = y1 - h2;
        if (x1 < x) {
            x4 = x;
        }

        if (y4 < y3) {
            y4 = y3;
        }

        addWindow(stack, new Rectangle(x, y3, x4, y4));
    }

    public static void addWindow(MatrixStack stack, Rectangle r1) {
        Matrix4f matrix = stack.peek().getPositionMatrix();
        Vector4f coord = new Vector4f(r1.x, r1.y, 0.0F, 1.0F);
        Vector4f end = new Vector4f(r1.x1, r1.y1, 0.0F, 1.0F);
        coord.mulTranspose(matrix);
        end.mulTranspose(matrix);
        float x = coord.x();
        float y = coord.y();
        float endX = end.x();
        float endY = end.y();
        Rectangle r = new Rectangle(x, y, endX, endY);
        if (clipStack.empty()) {
            clipStack.push(r);
            beginScissor(r.x, r.y, r.x1, r.y1);
        } else {
            Rectangle lastClip = clipStack.peek();
            float lsx = lastClip.x;
            float lsy = lastClip.y;
            float lstx = lastClip.x1;
            float lsty = lastClip.y1;
            float nsx = MathHelper.clamp(r.x, lsx, lstx);
            float nsy = MathHelper.clamp(r.y, lsy, lsty);
            float nstx = MathHelper.clamp(r.x1, nsx, lstx);
            float nsty = MathHelper.clamp(r.y1, nsy, lsty);
            clipStack.push(new Rectangle(nsx, nsy, nstx, nsty));
            beginScissor(nsx, nsy, nstx, nsty);
        }
    }

    public static void beginScissor(double x, double y, double endX, double endY) {
        double width = endX - x;
        double height = endY - y;
        width = Math.max(0.0, width);
        height = Math.max(0.0, height);
        float d = (float) MeteorClient.mc.getWindow().getScaleFactor();
        int ay = (int) ((MeteorClient.mc.getWindow().getScaledHeight() - (y + height)) * d);
        RenderSystem.enableScissor((int) (x * d), ay, (int) (width * d), (int) (height * d));
    }

    public static void drawHudBase2(
        MatrixStack matrices, float x, float y, float width, float height, float radius, Color color, float blurStrenth, float blurOpacity, float animationFactor
    ) {
        blurStrenth *= animationFactor;
        blurOpacity = (float) interpolate(1.0, blurOpacity, animationFactor);
        Color c = interpolateColorC(Color.BLACK, color, animationFactor);
        drawRoundedBlur(matrices, x, y, width, height, radius, c, blurStrenth, blurOpacity);
    }

    public static void setupRender() {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    public static void setRectanglePoints(BufferBuilder buffer, Matrix4f matrix, float x, float y, float x1, float y1) {
        buffer.vertex(matrix, x, y, 0.0F);
        buffer.vertex(matrix, x, y1, 0.0F);
        buffer.vertex(matrix, x1, y1, 0.0F);
        buffer.vertex(matrix, x1, y, 0.0F);
    }

    public static BufferBuilder preShaderDraw(MatrixStack matrices, float x, float y, float width, float height) {
        int scale = 1;
        x += (float) (0.5 * scale);
        y += (float) (0.5 * scale);
        matrices.push();
        matrices.scale(scale, scale, 1.0F);
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        BufferBuilder buffer = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION);
        setRectanglePoints(buffer, matrix, x, y, x + width, y + height);
        return buffer;
    }

    private static void setInverseRectanglePoints(BufferBuilder buffer, Matrix4f matrix, float x1, float y1, float x2, float y2) {
        buffer.vertex(matrix, x1, y1, 0.0F).color(255, 0, 0, 255);
        buffer.vertex(matrix, x2, y1, 0.0F).color(255, 0, 0, 255);
        buffer.vertex(matrix, x2, y2, 0.0F).color(255, 0, 0, 255);
        buffer.vertex(matrix, x1, y2, 0.0F).color(255, 0, 0, 255);
    }

    public static void drawRoundedBlur(MatrixStack matrices, float x, float y, float width, float height, float radius, Color c1) {
        BlurSetting blurSetting = (BlurSetting) Modules.get().get(BlurSetting.class);
        drawRoundedBlur(
            matrices,
            x,
            y,
            width,
            height,
            (blurSetting.radius.get()).intValue(),
            c1,
            (blurSetting.blurStrength.get()).floatValue(),
            (blurSetting.blurOpacity.get()).floatValue()
        );
    }

    public static void drawRoundedBlur2(MatrixStack matrices, float x, float y, float width, float height, float radius, Color c1) {
        BlurSetting blurSetting = (BlurSetting) Modules.get().get(BlurSetting.class);
        drawRoundedBlur2(
            matrices,
            x,
            y,
            width,
            height,
            (blurSetting.radius.get()).intValue(),
            c1,
            (blurSetting.blurStrength.get()).floatValue(),
            (blurSetting.blurOpacity.get()).floatValue()
        );
    }

    public static void drawRoundedBlur2(
        MatrixStack matrices, float x, float y, float width, float height, float radius, Color c1, float blurStrenth, float blurOpacity
    ) {
        BufferBuilder bb = preShaderDraw(matrices, x, y, width, height);
        BLUR_PROGRAM.setParameters(x, y, width, height, radius, c1, blurStrenth, blurOpacity);
        BLUR_PROGRAM.use();
        BufferRenderer.drawWithGlobalProgram(bb.end());
        endRender();
    }

    public static void drawRoundedBlur(
        MatrixStack matrices, float x, float y, float width, float height, float radius, Color c1, float blurStrenth, float blurOpacity
    ) {
        BufferBuilder bb2 = preShaderDraw(matrices, x - 10.0F, y - 10.0F, width + 20.0F, height + 20.0F);
        BLUR_PROGRAM.setParameters(x, y, width, height, radius, c1, blurStrenth, blurOpacity);
        BLUR_PROGRAM.use();
        BufferRenderer.drawWithGlobalProgram(bb2.end());
    }

    public static void drawOutlineShader(MatrixStack matrices, float x, float y, float width, float height) {
        BufferBuilder bb = preShaderDraw(matrices, x - 10.0F, y - 10.0F, width + 20.0F, height + 20.0F);
        HUD_SHADER.setParameters(x, y, width, height, 1.0F, 0.7F, 0.0F);
        HUD_SHADER.use();
        BufferRenderer.drawWithGlobalProgram(bb.end());
    }

    public static void drawOutlineShader(MatrixStack matrices, float x, float y, float width, float height, float externalAlpha, float internalAlpha) {
        BufferBuilder bb = preShaderDraw(matrices, x - 50.0F, y - 50.0F, width + 100.0F, height + 100.0F);
        HUD_SHADER.setParameters(x, y, width, height, 0.0F, externalAlpha, internalAlpha);
        HUD_SHADER.use();
        BufferRenderer.drawWithGlobalProgram(bb.end());
    }

    public static void drawRect(MatrixStack matrices, float x, float y, float width, float height, Color c) {
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        BufferBuilder bufferBuilder = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        setupRender();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        bufferBuilder.vertex(matrix, x, y + height, 0.0F).color(c.getRGB());
        bufferBuilder.vertex(matrix, x + width, y + height, 0.0F).color(c.getRGB());
        bufferBuilder.vertex(matrix, x + width, y, 0.0F).color(c.getRGB());
        bufferBuilder.vertex(matrix, x, y, 0.0F).color(c.getRGB());
        BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
        endRender();
    }

    public static void drawRect(
        MatrixStack matrices, double x, double y, double width, double height, meteordevelopment.meteorclient.utils.render.color.Color color
    ) {
        Color c = new Color(color.r, color.g, color.b, color.a);
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        BufferBuilder bufferBuilder = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        setupRender();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        bufferBuilder.vertex(matrix, (float) x, (float) (y + height), 0.0F).color(c.getRGB());
        bufferBuilder.vertex(matrix, (float) ((float) x + width), (float) ((float) y + height), 0.0F).color(c.getRGB());
        bufferBuilder.vertex(matrix, (float) ((float) x + width), (float) y, 0.0F).color(c.getRGB());
        bufferBuilder.vertex(matrix, (float) x, (float) y, 0.0F).color(c.getRGB());
        BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
        endRender();
    }

    public static void horizontalGradient(MatrixStack matrices, float x1, float y1, float x2, float y2, Color startColor, Color endColor) {
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        BufferBuilder bufferBuilder = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        setupRender();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        bufferBuilder.vertex(matrix, x1, y1, 0.0F).color(startColor.getRGB());
        bufferBuilder.vertex(matrix, x1, y2, 0.0F).color(startColor.getRGB());
        bufferBuilder.vertex(matrix, x2, y2, 0.0F).color(endColor.getRGB());
        bufferBuilder.vertex(matrix, x2, y1, 0.0F).color(endColor.getRGB());
        BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
        endRender();
    }

    public static void verticalGradient(MatrixStack matrices, float left, float top, float right, float bottom, Color startColor, Color endColor) {
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        BufferBuilder bufferBuilder = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        setupRender();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        bufferBuilder.vertex(matrix, left, top, 0.0F).color(startColor.getRGB());
        bufferBuilder.vertex(matrix, left, bottom, 0.0F).color(endColor.getRGB());
        bufferBuilder.vertex(matrix, right, bottom, 0.0F).color(endColor.getRGB());
        bufferBuilder.vertex(matrix, right, top, 0.0F).color(startColor.getRGB());
        BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
        endRender();
    }

    public static Color getAwtColor(meteordevelopment.meteorclient.utils.render.color.Color color) {
        return new Color(color.r, color.g, color.b, color.a);
    }

    @NotNull
    public static Color getColorObject(meteordevelopment.meteorclient.utils.render.color.Color c) {
        int color = c.getPacked();
        int alpha = color >> 24 & 0xFF;
        int red = color >> 16 & 0xFF;
        int green = color >> 8 & 0xFF;
        int blue = color & 0xFF;
        return new Color(red, green, blue, alpha);
    }

    @NotNull
    public static Color getColorObject(Color c) {
        int color = c.getRGB();
        int alpha = color >> 24 & 0xFF;
        int red = color >> 16 & 0xFF;
        int green = color >> 8 & 0xFF;
        int blue = color & 0xFF;
        return new Color(red, green, blue, alpha);
    }

    public static double interpolate(double oldValue, double newValue, double interpolationValue) {
        return oldValue + (newValue - oldValue) * interpolationValue;
    }

    public static Color getAnalogousColor(SettingColor color) {
        float[] hsb = Color.RGBtoHSB(color.r, color.g, color.b, null);
        float degree = 0.84F;
        float newHueSubtracted = hsb[0] - degree;
        return new Color(Color.HSBtoRGB(newHueSubtracted, hsb[1], hsb[2]));
    }

    public static Color TwoColoreffect(Color cl1, Color cl2, double speed, double count) {
        int angle = (int) ((System.currentTimeMillis() / speed + count) % 360.0);
        angle = (angle >= 180 ? 360 - angle : angle) * 2;
        return interpolateColorC(cl1, cl2, angle / 360.0F);
    }

    public static Color TwoColoreffect(SettingColor cl1, SettingColor cl2, double speed, double count) {
        int angle = (int) ((System.currentTimeMillis() / speed + count) % 360.0);
        angle = (angle >= 180 ? 360 - angle : angle) * 2;
        return interpolateColorC(cl1, cl2, angle / 360.0F);
    }

    public static Color interpolateColorC(Color color1, Color color2, float amount) {
        amount = Math.min(1.0F, Math.max(0.0F, amount));
        return new Color(
            interpolateInt(color1.getRed(), color2.getRed(), amount),
            interpolateInt(color1.getGreen(), color2.getGreen(), amount),
            interpolateInt(color1.getBlue(), color2.getBlue(), amount),
            interpolateInt(color1.getAlpha(), color2.getAlpha(), amount)
        );
    }

    public static Color interpolateColorC(SettingColor color1, SettingColor color2, float amount) {
        amount = Math.min(1.0F, Math.max(0.0F, amount));
        return new Color(
            interpolateInt(color1.r, color2.r, amount),
            interpolateInt(color1.g, color2.g, amount),
            interpolateInt(color1.b, color2.b, amount),
            interpolateInt(color1.a, color2.a, amount)
        );
    }

    public static Color interpolateColorsBackAndForth(int speed, int index, Color start, Color end, boolean trueColor) {
        int angle = (int) ((System.currentTimeMillis() / speed + index) % 360L);
        angle = (angle >= 180 ? 360 - angle : angle) * 2;
        return trueColor ? interpolateColorHue(start, end, angle / 360.0F) : interpolateColorC(start, end, angle / 360.0F);
    }

    public static Color interpolateColorHue(Color color1, Color color2, float amount) {
        amount = Math.min(1.0F, Math.max(0.0F, amount));
        float[] color1HSB = Color.RGBtoHSB(color1.getRed(), color1.getGreen(), color1.getBlue(), null);
        float[] color2HSB = Color.RGBtoHSB(color2.getRed(), color2.getGreen(), color2.getBlue(), null);
        Color resultColor = Color.getHSBColor(
            interpolateFloat(color1HSB[0], color2HSB[0], amount),
            interpolateFloat(color1HSB[1], color2HSB[1], amount),
            interpolateFloat(color1HSB[2], color2HSB[2], amount)
        );
        return new Color(resultColor.getRed(), resultColor.getGreen(), resultColor.getBlue(), interpolateInt(color1.getAlpha(), color2.getAlpha(), amount));
    }

    public static Color interpolateColorHue(SettingColor color1, SettingColor color2, float amount) {
        amount = Math.min(1.0F, Math.max(0.0F, amount));
        float[] color1HSB = Color.RGBtoHSB(color1.r, color1.g, color1.b, null);
        float[] color2HSB = Color.RGBtoHSB(color2.r, color2.g, color2.b, null);
        Color resultColor = Color.getHSBColor(
            interpolateFloat(color1HSB[0], color2HSB[0], amount),
            interpolateFloat(color1HSB[1], color2HSB[1], amount),
            interpolateFloat(color1HSB[2], color2HSB[2], amount)
        );
        return new Color(resultColor.getRed(), resultColor.getGreen(), resultColor.getBlue(), interpolateInt(color1.a, color2.a, amount));
    }

    public static Color fade(int speed, int index, Color color, float alpha) {
        float[] hsb = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
        int angle = (int) ((System.currentTimeMillis() / speed + index) % 360L);
        angle = (angle > 180 ? 360 - angle : angle) + 180;
        Color colorHSB = new Color(Color.HSBtoRGB(hsb[0], hsb[1], angle / 360.0F));
        return new Color(colorHSB.getRed(), colorHSB.getGreen(), colorHSB.getBlue(), Math.max(0, Math.min(255, (int) (alpha * 255.0F))));
    }

    public static Color fade(int speed, int index, SettingColor color, float alpha) {
        float[] hsb = Color.RGBtoHSB(color.r, color.g, color.b, null);
        int angle = (int) ((System.currentTimeMillis() / speed + index) % 360L);
        angle = (angle > 180 ? 360 - angle : angle) + 180;
        Color colorHSB = new Color(Color.HSBtoRGB(hsb[0], hsb[1], angle / 360.0F));
        return new Color(colorHSB.getRed(), colorHSB.getGreen(), colorHSB.getBlue(), Math.max(0, Math.min(255, (int) (alpha * 255.0F))));
    }

    public static Color astolfo(boolean clickgui, int yOffset) {
        float speed = clickgui ? 3500.0F : 3000.0F;
        float hue = (float) (System.currentTimeMillis() % (int) speed + yOffset);
        if (hue > speed) {
            hue -= speed;
        }

        float var4 = hue / speed;
        if (var4 > 0.5F) {
            var4 = 0.5F - (var4 - 0.5F);
        }

        hue = var4 + 0.5F;
        return Color.getHSBColor(hue, 0.4F, 1.0F);
    }

    public static Color rainbow(int delay, float saturation, float brightness) {
        double rainbow = Math.ceil((float) (System.currentTimeMillis() + delay) / 16.0F);
        rainbow %= 360.0;
        return Color.getHSBColor((float) (rainbow / 360.0), saturation, brightness);
    }

    public static Color skyRainbow(int speed, int index) {
        int angle = (int) ((System.currentTimeMillis() / speed + index) % 360L);
        int var3;
        return Color.getHSBColor((float) ((var3 = angle % 360) / 360.0) < 0.5 ? -((float) (var3 / 360.0)) : (float) (var3 / 360.0), 0.5F, 1.0F);
    }

    public static Color rainbow(int speed, int index, float saturation, float brightness, float opacity) {
        int angle = (int) ((System.currentTimeMillis() / speed + index) % 360L);
        float hue = angle / 360.0F;
        Color color = new Color(Color.HSBtoRGB(hue, saturation, brightness));
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), Math.max(0, Math.min(255, (int) (opacity * 255.0F))));
    }

    public static float interpolateFloat(float oldValue, float newValue, double interpolationValue) {
        return (float) interpolate(oldValue, newValue, (float) interpolationValue);
    }

    public static int interpolateInt(int oldValue, int newValue, double interpolationValue) {
        return (int) interpolate(oldValue, newValue, (float) interpolationValue);
    }

    public static void drawBlurredShadow(
        MatrixStack matrices, float x, float y, float width, float height, int blurRadius, meteordevelopment.meteorclient.utils.render.color.Color color
    ) {
        float var14 = width + blurRadius * 2;
        float var15 = height + blurRadius * 2;
        float var12 = x - blurRadius;
        float var13 = y - blurRadius;
        int identifier = (int) (var14 * var15 + var14 * blurRadius);
        if (shadowCache.containsKey(identifier)) {
            shadowCache.get(identifier).bind();
            setupRender();
            RenderSystem.setShaderColor(color.r / 255.0F, color.g / 255.0F, color.b / 255.0F, color.a / 255.0F);
            renderTexture(matrices, var12, var13, var14, var15, 0.0F, 0.0F, var14, var15, var14, var15);
            endRender();
        } else {
            BufferedImage original = new BufferedImage((int) var14, (int) var15, 2);
            Graphics g = original.getGraphics();
            g.setColor(new Color(-1));
            g.fillRect(blurRadius, blurRadius, (int) (var14 - blurRadius * 2), (int) (var15 - blurRadius * 2));
            g.dispose();
            GaussianFilter op = new GaussianFilter(blurRadius);
            BufferedImage blurred = op.filter(original, null);
            shadowCache.put(identifier, new BlurredShadow(blurred));
        }
    }

    public static void drawBlurredShadowMasked(
        MatrixStack matrices,
        float x,
        float y,
        float width,
        float height,
        int blurRadius,
        meteordevelopment.meteorclient.utils.render.color.Color color,
        float maskX,
        float maskY,
        float maskWidth,
        float maskHeight
    ) {
        if (!(width <= 0.0F) && !(height <= 0.0F) && blurRadius >= 0) {
            float var18 = width + blurRadius * 2;
            float var19 = height + blurRadius * 2;
            float var16 = x - blurRadius;
            float var17 = y - blurRadius;
            int identifier = (int) (var18 * var19 + var18 * blurRadius);
            if (shadowCache.containsKey(identifier)) {
                shadowCache.get(identifier).bind();
                setupRender();
                RenderSystem.setShaderColor(color.r / 255.0F, color.g / 255.0F, color.b / 255.0F, color.a / 255.0F);
                renderTexture(matrices, var16, var17, var18, var19, 0.0F, 0.0F, var18, var19, var18, var19);
                endRender();
            } else {
                BufferedImage original = new BufferedImage((int) var18, (int) var19, 2);
                Graphics2D g2d = original.createGraphics();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g2d.setColor(new Color(color.getPacked(), true));
                g2d.fillRect(blurRadius, blurRadius, (int) (var18 - blurRadius * 2), (int) (var19 - blurRadius * 2));
                g2d.dispose();
                GaussianFilter op = new GaussianFilter(blurRadius);
                BufferedImage blurred = op.filter(original, null);
                shadowCache.put(identifier, new BlurredShadow(blurred));
            }
        }
    }

    public static void drawDirectedBlurredShadow(
        MatrixStack matrices,
        double x,
        double y,
        double width,
        double height,
        int blurRadius,
        meteordevelopment.meteorclient.utils.render.color.Color color,
        BlurDirection direction
    ) {
        if (!(width <= 0.0) && !(height <= 0.0)) {
            switch (direction) {
                case UP:
                    height += blurRadius;
                    y -= blurRadius;
                    break;
                case DOWN:
                    height += blurRadius;
                    break;
                case LEFT:
                    width += blurRadius;
                    x -= blurRadius;
                    break;
                case RIGHT:
                    width += blurRadius;
            }

            int identifier = (int) (width * height + width * blurRadius + direction.ordinal());
            if (shadowCache.containsKey(identifier)) {
                shadowCache.get(identifier).bind();
                setupRender();
                RenderSystem.setShaderColor(color.r / 255.0F, color.g / 255.0F, color.b / 255.0F, color.a / 255.0F);
                renderTexture(matrices, x, y, width, height, 0.0F, 0.0F, width, height, width, height);
                endRender();
            } else {
                BufferedImage original = new BufferedImage((int) width, (int) height, 2);
                Graphics g = original.getGraphics();
                g.setColor(new Color(-1));
                switch (direction) {
                    case UP:
                        g.fillRect(0, blurRadius, (int) width, (int) (height - blurRadius));
                        break;
                    case DOWN:
                        g.fillRect(0, 0, (int) width, (int) (height - blurRadius));
                        break;
                    case LEFT:
                        g.fillRect(blurRadius, 0, (int) (width - blurRadius), (int) height);
                        break;
                    case RIGHT:
                        g.fillRect(0, 0, (int) (width - blurRadius), (int) height);
                }

                g.dispose();
                GaussianFilter op = new GaussianFilter(blurRadius);
                BufferedImage blurred = op.filter(original, null);
                shadowCache.put(identifier, new BlurredShadow(blurred));
            }
        }
    }

    public static void drawDirectedBlurredShadow(
        MatrixStack matrices,
        float x,
        float y,
        float width,
        float height,
        int blurRadius,
        meteordevelopment.meteorclient.utils.render.color.Color color,
        BlurDirection direction
    ) {
        if (!(width <= 0.0F) && !(height <= 0.0F)) {
            switch (direction) {
                case UP:
                    height += blurRadius;
                    y -= blurRadius;
                    break;
                case DOWN:
                    height += blurRadius;
                    break;
                case LEFT:
                    width += blurRadius;
                    x -= blurRadius;
                    break;
                case RIGHT:
                    width += blurRadius;
            }

            int identifier = (int) (width * height + width * blurRadius + direction.ordinal());
            if (shadowCache.containsKey(identifier)) {
                shadowCache.get(identifier).bind();
                setupRender();
                RenderSystem.setShaderColor(color.r / 255.0F, color.g / 255.0F, color.b / 255.0F, color.a / 255.0F);
                renderTexture(matrices, x, y, width, height, 0.0F, 0.0F, width, height, width, height);
                endRender();
            } else {
                BufferedImage original = new BufferedImage((int) width, (int) height, 2);
                Graphics g = original.getGraphics();
                g.setColor(new Color(-1));
                switch (direction) {
                    case UP:
                        g.fillRect(0, blurRadius, (int) width, (int) (height - blurRadius));
                        break;
                    case DOWN:
                        g.fillRect(0, 0, (int) width, (int) (height - blurRadius));
                        break;
                    case LEFT:
                        g.fillRect(blurRadius, 0, (int) (width - blurRadius), (int) height);
                        break;
                    case RIGHT:
                        g.fillRect(0, 0, (int) (width - blurRadius), (int) height);
                }

                g.dispose();
                GaussianFilter op = new GaussianFilter(blurRadius);
                BufferedImage blurred = op.filter(original, null);
                shadowCache.put(identifier, new BlurredShadow(blurred));
            }
        }
    }

    public static void drawRound(MatrixStack matrices, float x, float y, float width, float height, float radius, Color color) {
        renderRoundedQuad(matrices, color, x, y, (width + x), (height + y), radius, 4.0);
    }

    public static void drawRound(
        MatrixStack matrices, float x, float y, float width, float height, float radius, meteordevelopment.meteorclient.utils.render.color.Color color
    ) {
        renderRoundedQuad(matrices, color, x, y, (width + x), (height + y), radius, 4.0);
    }

    public static void drawRound(
        MatrixStack matrices, int x, int y, int width, int height, int radius, meteordevelopment.meteorclient.utils.render.color.Color color
    ) {
        renderRoundedQuad(matrices, color, x, y, (width + x), (height + y), radius, 4.0);
    }

    public static void renderRoundedQuad(MatrixStack matrices, Color c, double fromX, double fromY, double toX, double toY, double radius, double samples) {
        setupRender();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        renderRoundedQuadInternal(
            matrices.peek().getPositionMatrix(),
            c.getRed() / 255.0F,
            c.getGreen() / 255.0F,
            c.getBlue() / 255.0F,
            c.getAlpha() / 255.0F,
            fromX,
            fromY,
            toX,
            toY,
            radius,
            samples
        );
        endRender();
    }

    public static void renderRoundedQuad(
        MatrixStack matrices,
        meteordevelopment.meteorclient.utils.render.color.Color c,
        double fromX,
        double fromY,
        double toX,
        double toY,
        double radius,
        double samples
    ) {
        setupRender();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        renderRoundedQuadInternal(
            matrices.peek().getPositionMatrix(), c.r / 255.0F, c.g / 255.0F, c.b / 255.0F, c.a / 255.0F, fromX, fromY, toX, toY, radius, samples
        );
        endRender();
    }

    public static void renderRoundedQuad2(
        MatrixStack matrices, Color c, Color c2, Color c3, Color c4, double fromX, double fromY, double toX, double toY, double radius
    ) {
        setupRender();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        renderRoundedQuadInternal2(
            matrices.peek().getPositionMatrix(),
            c.getRed() / 255.0F,
            c.getGreen() / 255.0F,
            c.getBlue() / 255.0F,
            c.getAlpha() / 255.0F,
            c2.getRed() / 255.0F,
            c2.getGreen() / 255.0F,
            c2.getBlue() / 255.0F,
            c2.getAlpha() / 255.0F,
            c3.getRed() / 255.0F,
            c3.getGreen() / 255.0F,
            c3.getBlue() / 255.0F,
            c3.getAlpha() / 255.0F,
            c4.getRed() / 255.0F,
            c4.getGreen() / 255.0F,
            c4.getBlue() / 255.0F,
            c4.getAlpha() / 255.0F,
            fromX,
            fromY,
            toX,
            toY,
            radius
        );
        endRender();
    }

    public static void renderRoundedQuadInternal(
        Matrix4f matrix, float cr, float cg, float cb, float ca, double fromX, double fromY, double toX, double toY, double radius, double samples
    ) {
        BufferBuilder bufferBuilder = Tessellator.getInstance().begin(DrawMode.TRIANGLE_FAN, VertexFormats.POSITION_COLOR);
        double[][] map = new double[][]{
            {toX - radius, toY - radius, radius},
            {toX - radius, fromY + radius, radius},
            {fromX + radius, fromY + radius, radius},
            {fromX + radius, toY - radius, radius}
        };

        for (int i = 0; i < 4; i++) {
            double[] current = map[i];
            double rad = current[2];
            double r = i * 90.0;

            while (r < 90.0 + i * 90.0) {
                float rad1 = (float) Math.toRadians(r);
                float sin = (float) (Math.sin(rad1) * rad);
                float cos = (float) (Math.cos(rad1) * rad);
                bufferBuilder.vertex(matrix, (float) current[0] + sin, (float) current[1] + cos, 0.0F).color(cr, cg, cb, ca);
                r += 90.0 / samples;
            }

            float rad1 = (float) Math.toRadians(90.0 + i * 90.0);
            float sin = (float) (Math.sin(rad1) * rad);
            float cos = (float) (Math.cos(rad1) * rad);
            bufferBuilder.vertex(matrix, (float) current[0] + sin, (float) current[1] + cos, 0.0F).color(cr, cg, cb, ca);
        }

        BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
    }

    public static void renderRoundedQuadInternal2(
        Matrix4f matrix,
        float cr,
        float cg,
        float cb,
        float ca,
        float cr1,
        float cg1,
        float cb1,
        float ca1,
        float cr2,
        float cg2,
        float cb2,
        float ca2,
        float cr3,
        float cg3,
        float cb3,
        float ca3,
        double fromX,
        double fromY,
        double toX,
        double toY,
        double radC1
    ) {
        BufferBuilder bufferBuilder = Tessellator.getInstance().begin(DrawMode.TRIANGLE_FAN, VertexFormats.POSITION_COLOR);
        double[][] map = new double[][]{
            {toX - radC1, toY - radC1, radC1}, {toX - radC1, fromY + radC1, radC1}, {fromX + radC1, fromY + radC1, radC1}, {fromX + radC1, toY - radC1, radC1}
        };

        for (int i = 0; i < 4; i++) {
            double[] current = map[i];
            double rad = current[2];

            for (double r = i * 90; r < 90 + i * 90; r += 10.0) {
                float rad1 = (float) Math.toRadians(r);
                float sin = (float) (Math.sin(rad1) * rad);
                float cos = (float) (Math.cos(rad1) * rad);
                switch (i) {
                    case 0:
                        bufferBuilder.vertex(matrix, (float) current[0] + sin, (float) current[1] + cos, 0.0F).color(cr1, cg1, cb1, ca1);
                        break;
                    case 1:
                        bufferBuilder.vertex(matrix, (float) current[0] + sin, (float) current[1] + cos, 0.0F).color(cr, cg, cb, ca);
                        break;
                    case 2:
                        bufferBuilder.vertex(matrix, (float) current[0] + sin, (float) current[1] + cos, 0.0F).color(cr2, cg2, cb2, ca2);
                        break;
                    default:
                        bufferBuilder.vertex(matrix, (float) current[0] + sin, (float) current[1] + cos, 0.0F).color(cr3, cg3, cb3, ca3);
                }
            }
        }

        BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
    }

    public static void draw2DGradientRect(
        MatrixStack matrices,
        float left,
        float top,
        float right,
        float bottom,
        Color leftBottomColor,
        Color leftTopColor,
        Color rightBottomColor,
        Color rightTopColor
    ) {
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        BufferBuilder bufferBuilder = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        setupRender();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        bufferBuilder.vertex(matrix, right, top, 0.0F).color(rightTopColor.getRGB());
        bufferBuilder.vertex(matrix, left, top, 0.0F).color(leftTopColor.getRGB());
        bufferBuilder.vertex(matrix, left, bottom, 0.0F).color(leftBottomColor.getRGB());
        bufferBuilder.vertex(matrix, right, bottom, 0.0F).color(rightBottomColor.getRGB());
        BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
        endRender();
    }

    public static Color injectAlpha(Color color, int alpha) {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), MathHelper.clamp(alpha, 0, 255));
    }

    public static void drawGradientBlurredShadow(
        MatrixStack matrices, float x, float y, float width, float height, int blurRadius, Color color1, Color color2, Color color3, Color color4
    ) {
        float var17 = width + blurRadius * 2;
        float var18 = height + blurRadius * 2;
        float var15 = x - blurRadius;
        float var16 = y - blurRadius;
        int identifier = (int) (var17 * var18 + var17 * blurRadius);
        if (shadowCache.containsKey(identifier)) {
            shadowCache.get(identifier).bind();
            setupRender();
            renderGradientTexture(
                matrices,
                var15,
                var16,
                var17,
                var18,
                0.0F,
                0.0F,
                var17,
                var18,
                var17,
                var18,
                color1,
                color2,
                color3,
                color4
            );
            endRender();
        } else {
            BufferedImage original = new BufferedImage((int) var17, (int) var18, 2);
            Graphics g = original.getGraphics();
            g.setColor(new Color(-1));
            g.fillRect(blurRadius, blurRadius, (int) (var17 - blurRadius * 2), (int) (var18 - blurRadius * 2));
            g.dispose();
            GaussianFilter op = new GaussianFilter(blurRadius);
            BufferedImage blurred = op.filter(original, null);
            shadowCache.put(identifier, new BlurredShadow(blurred));
        }
    }

    public static void drawGradientBlurredShadow(
        MatrixStack matrices,
        float x,
        float y,
        float width,
        float height,
        int blurRadius,
        SettingColor color1,
        SettingColor color2,
        SettingColor color3,
        SettingColor color4
    ) {
        float var17 = width + blurRadius * 2;
        float var18 = height + blurRadius * 2;
        float var15 = x - blurRadius;
        float var16 = y - blurRadius;
        int identifier = (int) (var17 * var18 + var17 * blurRadius);
        if (shadowCache.containsKey(identifier)) {
            shadowCache.get(identifier).bind();
            setupRender();
            renderGradientTexture(
                matrices,
                var15,
                var16,
                var17,
                var18,
                0.0F,
                0.0F,
                var17,
                var18,
                var17,
                var18,
                color1,
                color2,
                color3,
                color4
            );
            endRender();
        } else {
            BufferedImage original = new BufferedImage((int) var17, (int) var18, 2);
            Graphics g = original.getGraphics();
            g.setColor(new Color(-1));
            g.fillRect(blurRadius, blurRadius, (int) (var17 - blurRadius * 2), (int) (var18 - blurRadius * 2));
            g.dispose();
            GaussianFilter op = new GaussianFilter(blurRadius);
            BufferedImage blurred = op.filter(original, null);
            shadowCache.put(identifier, new BlurredShadow(blurred));
        }
    }

    public static void drawGradientBlurredShadow1(
        MatrixStack matrices, float x, float y, float width, float height, int blurRadius, Color color1, Color color2, Color color3, Color color4
    ) {
        float var20 = width + blurRadius * 2;
        float var21 = height + blurRadius * 2;
        float var18 = x - blurRadius;
        float var19 = y - blurRadius;
        int identifier = (int) (var20 * var21 + var20 * blurRadius);
        if (shadowCache1.containsKey(identifier)) {
            shadowCache1.get(identifier).bind();
            setupRender();
            RenderSystem.blendFunc(SrcFactor.SRC_ALPHA, DstFactor.ONE);
            renderGradientTexture(
                matrices,
                var18,
                var19,
                var20,
                var21,
                0.0F,
                0.0F,
                var20,
                var21,
                var20,
                var21,
                color1,
                color2,
                color3,
                color4
            );
            endRender();
        } else {
            BufferedImage original = new BufferedImage((int) var20, (int) var21, 2);
            Graphics g = original.getGraphics();
            g.setColor(new Color(-1));
            g.fillRect(blurRadius, blurRadius, (int) (var20 - blurRadius * 2), (int) (var21 - blurRadius * 2));
            g.dispose();
            BufferedImage blurred = new GaussianFilter(blurRadius).filter(original, null);
            BufferedImage black = new BufferedImage((int) var20 + blurRadius * 2, (int) var21 + blurRadius * 2, 2);
            Graphics g2 = black.getGraphics();
            g2.setColor(new Color(0));
            g2.fillRect(0, 0, (int) var20 + blurRadius * 2, (int) var21 + blurRadius * 2);
            g2.dispose();
            BufferedImage combined = new BufferedImage((int) var20, (int) var21, 2);
            Graphics g1 = combined.getGraphics();
            g1.drawImage(black, -blurRadius, -blurRadius, null);
            g1.drawImage(blurred, 0, 0, null);
            g1.dispose();
            shadowCache1.put(identifier, new BlurredShadow(combined));
        }
    }

    public static void renderGradientTexture(
        MatrixStack matrices,
        double x0,
        double y0,
        double width,
        double height,
        float u,
        float v,
        double regionWidth,
        double regionHeight,
        double textureWidth,
        double textureHeight,
        Color c1,
        Color c2,
        Color c3,
        Color c4
    ) {
        RenderSystem.setShader(GameRenderer::getPositionTexColorProgram);
        BufferBuilder bufferBuilder = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
        renderGradientTextureInternal(bufferBuilder, matrices, x0, y0, width, height, u, v, regionWidth, regionHeight, textureWidth, textureHeight, c1, c2, c3, c4);
        BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
    }

    public static void renderGradientTexture(
        MatrixStack matrices,
        double x0,
        double y0,
        double width,
        double height,
        float u,
        float v,
        double regionWidth,
        double regionHeight,
        double textureWidth,
        double textureHeight,
        SettingColor c1,
        SettingColor c2,
        SettingColor c3,
        SettingColor c4
    ) {
        RenderSystem.setShader(GameRenderer::getPositionTexColorProgram);
        BufferBuilder bufferBuilder = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
        renderGradientTextureInternal(bufferBuilder, matrices, x0, y0, width, height, u, v, regionWidth, regionHeight, textureWidth, textureHeight, c1, c2, c3, c4);
        BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
    }

    public static void renderGradientTextureInternal(
        BufferBuilder buff,
        MatrixStack matrices,
        double x0,
        double y0,
        double width,
        double height,
        float u,
        float v,
        double regionWidth,
        double regionHeight,
        double textureWidth,
        double textureHeight,
        Color c1,
        Color c2,
        Color c3,
        Color c4
    ) {
        double x1 = x0 + width;
        double y1 = y0 + height;
        double z = 0.0;
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        buff.vertex(matrix, (float) x0, (float) y1, (float) z).texture(u / (float) textureWidth, (v + (float) regionHeight) / (float) textureHeight).color(c1.getRGB());
        buff.vertex(matrix, (float) x1, (float) y1, (float) z)
            .texture((u + (float) regionWidth) / (float) textureWidth, (v + (float) regionHeight) / (float) textureHeight)
            .color(c2.getRGB());
        buff.vertex(matrix, (float) x1, (float) y0, (float) z).texture((u + (float) regionWidth) / (float) textureWidth, v / (float) textureHeight).color(c3.getRGB());
        buff.vertex(matrix, (float) x0, (float) y0, (float) z).texture(u / (float) textureWidth, (v + 0.0F) / (float) textureHeight).color(c4.getRGB());
    }

    public static void renderGradientTextureInternal(
        BufferBuilder buff,
        MatrixStack matrices,
        double x0,
        double y0,
        double width,
        double height,
        float u,
        float v,
        double regionWidth,
        double regionHeight,
        double textureWidth,
        double textureHeight,
        SettingColor c1,
        SettingColor c2,
        SettingColor c3,
        SettingColor c4
    ) {
        double x1 = x0 + width;
        double y1 = y0 + height;
        double z = 0.0;
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        buff.vertex(matrix, (float) x0, (float) y1, (float) z)
            .texture(u / (float) textureWidth, (v + (float) regionHeight) / (float) textureHeight)
            .color(c1.getPacked());
        buff.vertex(matrix, (float) x1, (float) y1, (float) z)
            .texture((u + (float) regionWidth) / (float) textureWidth, (v + (float) regionHeight) / (float) textureHeight)
            .color(c2.getPacked());
        buff.vertex(matrix, (float) x1, (float) y0, (float) z).texture((u + (float) regionWidth) / (float) textureWidth, v / (float) textureHeight).color(c3.getPacked());
        buff.vertex(matrix, (float) x0, (float) y0, (float) z).texture(u / (float) textureWidth, (v + 0.0F) / (float) textureHeight).color(c4.getPacked());
    }

    public static void drawOrbiz(MatrixStack matrices, float z, double r, Color c) {
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        setupRender();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        BufferBuilder bufferBuilder = Tessellator.getInstance().begin(DrawMode.TRIANGLE_FAN, VertexFormats.POSITION_COLOR);

        for (int i = 0; i <= 20; i++) {
            float x2 = (float) (Math.sin(i * 56.548656F / 180.0F) * r);
            float y2 = (float) (Math.cos(i * 56.548656F / 180.0F) * r);
            bufferBuilder.vertex(matrix, x2, y2, z).color(c.getRed() / 255.0F, c.getGreen() / 255.0F, c.getBlue() / 255.0F, 0.4F);
        }

        BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
        endRender();
    }

    public static void drawStar(MatrixStack matrices, Color c, float scale) {
        setupRender();
        RenderSystem.blendFunc(SrcFactor.SRC_ALPHA, DstFactor.ONE);
        RenderSystem.setShaderTexture(0, star);
        RenderSystem.setShaderColor(c.getRed() / 255.0F, c.getGreen() / 255.0F, c.getBlue() / 255.0F, c.getAlpha() / 255.0F);
        renderGradientTexture(matrices, 0.0, 0.0, scale, scale, 0.0F, 0.0F, 128.0, 128.0, 128.0, 128.0, c, c, c, c);
        endRender();
    }

    public static void drawHeart(MatrixStack matrices, Color c, float scale) {
        setupRender();
        RenderSystem.blendFunc(SrcFactor.SRC_ALPHA, DstFactor.ONE);
        RenderSystem.setShaderTexture(0, heart);
        RenderSystem.setShaderColor(c.getRed() / 255.0F, c.getGreen() / 255.0F, c.getBlue() / 255.0F, c.getAlpha() / 255.0F);
        renderGradientTexture(matrices, 0.0, 0.0, scale, scale, 0.0F, 0.0F, 128.0, 128.0, 128.0, 128.0, c, c, c, c);
        endRender();
    }

    public static void drawBloom(MatrixStack matrices, Color c, float scale) {
        setupRender();
        RenderSystem.blendFunc(SrcFactor.SRC_ALPHA, DstFactor.ONE);
        RenderSystem.setShaderTexture(0, firefly);
        RenderSystem.setShaderColor(c.getRed() / 255.0F, c.getGreen() / 255.0F, c.getBlue() / 255.0F, c.getAlpha() / 255.0F);
        renderGradientTexture(matrices, 0.0, 0.0, scale, scale, 0.0F, 0.0F, 128.0, 128.0, 128.0, 128.0, c, c, c, c);
        endRender();
    }

    public static void drawPure(MatrixStack matrices, Color c, float scale) {
        setupRender();
        RenderSystem.blendFunc(SrcFactor.SRC_ALPHA, DstFactor.ONE);
        RenderSystem.setShaderTexture(0, pure);
        RenderSystem.setShaderColor(c.getRed() / 255.0F, c.getGreen() / 255.0F, c.getBlue() / 255.0F, c.getAlpha() / 255.0F);
        renderGradientTexture(matrices, 0.0, 0.0, scale, scale, 0.0F, 0.0F, 114.0, 108.0, 114.0, 128108.0, c, c, c, c);
        endRender();
    }

    public static void drawBubble(MatrixStack matrices, float angle, float factor) {
        setupRender();
        RenderSystem.blendFunc(SrcFactor.SRC_ALPHA, DstFactor.ONE);
        RenderSystem.setShaderTexture(0, bubble);
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(angle));
        float scale = factor * 2.0F;
        renderGradientTexture(
            matrices,
            (-scale / 2.0F),
            (-scale / 2.0F),
            scale,
            scale,
            0.0F,
            0.0F,
            128.0,
            128.0,
            128.0,
            128.0,
            applyOpacity(CaptureMark.getColor(270), 1.0F - factor),
            applyOpacity(CaptureMark.getColor(0), 1.0F - factor),
            applyOpacity(CaptureMark.getColor(180), 1.0F - factor),
            applyOpacity(CaptureMark.getColor(90), 1.0F - factor)
        );
        endRender();
    }

    public static void drawBlurredShadow(MatrixStack matrices, float x, float y, float width, float height, int blurRadius, Color color) {
        float var14 = width + blurRadius * 2;
        float var15 = height + blurRadius * 2;
        float var12 = x - blurRadius;
        float var13 = y - blurRadius;
        int identifier = (int) (var14 * var15 + var14 * blurRadius);
        if (shadowCache.containsKey(identifier)) {
            shadowCache.get(identifier).bind();
            setupRender();
            RenderSystem.setShaderColor(color.getRed() / 255.0F, color.getGreen() / 255.0F, color.getBlue() / 255.0F, color.getAlpha() / 255.0F);
            renderTexture(matrices, var12, var13, var14, var15, 0.0F, 0.0F, var14, var15, var14, var15);
            endRender();
        } else {
            BufferedImage original = new BufferedImage((int) var14, (int) var15, 2);
            Graphics g = original.getGraphics();
            g.setColor(new Color(-1));
            g.fillRect(blurRadius, blurRadius, (int) (var14 - blurRadius * 2), (int) (var15 - blurRadius * 2));
            g.dispose();
            GaussianFilter op = new GaussianFilter(blurRadius);
            BufferedImage blurred = op.filter(original, null);
            shadowCache.put(identifier, new BlurredShadow(blurred));
        }
    }

    public static void drawBlurredShadowMasked2(
        MatrixStack matrices,
        float x,
        float y,
        float width,
        float height,
        int blurRadius,
        meteordevelopment.meteorclient.utils.render.color.Color color,
        float maskWidth,
        float maskHeight,
        Side side
    ) {
        if (!(width <= 0.0F) && !(height <= 0.0F) && blurRadius >= 0) {
            float var19 = width + blurRadius * 2;
            float var20 = height + blurRadius * 2;
            float var17 = x - blurRadius;
            float var18 = y - blurRadius;
            int identifier = (int) (var19 * var20 + var19 * blurRadius);
            if (shadowCache.containsKey(identifier)) {
                shadowCache.get(identifier).bind();
                setupRender();
                RenderSystem.setShaderColor(color.r / 255.0F, color.g / 255.0F, color.b / 255.0F, color.a / 255.0F);
                switch (side) {
                    case Top:
                        renderTexture(
                            matrices, var17, var18 - height / 2.0F, width, height, width, -blurRadius * 2.0F, (var19 - maskWidth) / 2.0F, var19 / 2.0F, width, height
                        );
                        break;
                    case Bottom:
                        renderTexture(
                            matrices, var17, var18 + height / 2.0F, width, height, width, -blurRadius * 2.0F, (var19 + maskWidth) / 2.0F, var19 / 2.0F, width, height
                        );
                }

                endRender();
            } else {
                BufferedImage original = new BufferedImage((int) var19, (int) var20, 2);
                Graphics2D g2d = original.createGraphics();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g2d.setColor(new Color(color.getPacked(), true));
                g2d.fillRect(blurRadius, blurRadius, (int) (var19 - blurRadius * 2), (int) (var20 - blurRadius * 2));
                g2d.dispose();
                GaussianFilter op = new GaussianFilter(blurRadius);
                BufferedImage blurred = op.filter(original, null);
                shadowCache.put(identifier, new BlurredShadow(blurred));
            }
        }
    }

    public static void RdrawBlurredShadow(MatrixStack matrices, float x, float y, float width, float height, int blurRadius, Color color) {
        float var14 = width + blurRadius * 2;
        float var15 = height + blurRadius * 2;
        float var12 = x - blurRadius;
        float var13 = y - blurRadius;
        int identifier = (int) (var14 * var15 + var14 * blurRadius);
        if (shadowCache.containsKey(identifier)) {
            shadowCache.get(identifier).bind();
            setupRender();
            RenderSystem.setShaderColor(color.getRed() / 255.0F, color.getGreen() / 255.0F, color.getBlue() / 255.0F, color.getAlpha() / 255.0F);
            matrices.push();
            matrices.translate(var12 + var14 / 2.0F, var13 + var15 / 2.0F, 0.0F);
            Quaternionf var16 = new Quaternionf().rotateZ((float) Math.toRadians(45.0));
            matrices.multiply(var16);
            matrices.translate(-(var14 / 2.0F), -(var15 / 2.0F), 0.0F);
            renderTexture(matrices, 0.0, 0.0, var14, var15, 0.0F, 0.0F, var14, var15, var14, var15);
            matrices.pop();
            endRender();
        } else {
            BufferedImage original = new BufferedImage((int) var14, (int) var15, 2);
            Graphics g = original.getGraphics();
            g.setColor(new Color(-1));
            g.fillRect(blurRadius, blurRadius, (int) (var14 - blurRadius * 2), (int) (var15 - blurRadius * 2));
            g.dispose();
            GaussianFilter op = new GaussianFilter(blurRadius);
            BufferedImage blurred = op.filter(original, null);
            shadowCache.put(identifier, new BlurredShadow(blurred));
        }
    }

    public static void endRender() {
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    public static void registerBufferedImageTexture(Texture i, BufferedImage bi) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(bi, "png", baos);
            byte[] bytes = baos.toByteArray();
            registerTexture(i, bytes);
        } catch (Exception var4) {
        }
    }

    static float bicubicWeight(float t) {
        float var1 = Math.abs(t);
        if (var1 <= 1.0) {
            return 1.5F * var1 * var1 * var1 - 2.5F * var1 * var1 + 1.0F;
        } else {
            return var1 <= 2.0 ? -0.5F * var1 * var1 * var1 + 2.5F * var1 * var1 - 4.0F * var1 + 2.0F : 0.0F;
        }
    }

    public static void renderTextureX(
        MatrixStack matrices,
        double x0,
        double y0,
        double width,
        double height,
        float u,
        float v,
        double regionWidth,
        double regionHeight,
        double textureWidth,
        double textureHeight
    ) {
        double x1 = x0 + width;
        double y1 = y0 + height;
        double z = 0.0;
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(770, 771);
        RenderSystem.enableDepthTest();
        GL13.glEnable(32925);
        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        float maxAniso = GL11.glGetFloat(34047);
        RenderSystem.texParameter(3553, 34046, (int) Math.min(16.0F, maxAniso));
        RenderSystem.texParameter(3553, 10241, 9987);
        RenderSystem.texParameter(3553, 10240, 9729);
        BufferBuilder bufferBuilder = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
        bufferBuilder.vertex(matrix, (float) x0, (float) y1, (float) z).texture(u / (float) textureWidth, (v + (float) regionHeight) / (float) textureHeight);
        bufferBuilder.vertex(matrix, (float) x1, (float) y1, (float) z)
            .texture((u + (float) regionWidth) / (float) textureWidth, (v + (float) regionHeight) / (float) textureHeight);
        bufferBuilder.vertex(matrix, (float) x1, (float) y0, (float) z).texture((u + (float) regionWidth) / (float) textureWidth, v / (float) textureHeight);
        bufferBuilder.vertex(matrix, (float) x0, (float) y0, (float) z).texture(u / (float) textureWidth, (v + 0.0F) / (float) textureHeight);
        BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
        GL13.glDisable(32925);
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
        RenderSystem.disableDepthTest();
    }

    public static void renderTexture(
        MatrixStack matrices,
        double x0,
        double y0,
        double width,
        double height,
        float u,
        float v,
        double regionWidth,
        double regionHeight,
        double textureWidth,
        double textureHeight
    ) {
        double x1 = x0 + width;
        double y1 = y0 + height;
        double z = 0.0;
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        BufferBuilder bufferBuilder = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
        bufferBuilder.vertex(matrix, (float) x0, (float) y1, (float) z).texture(u / (float) textureWidth, (v + (float) regionHeight) / (float) textureHeight);
        bufferBuilder.vertex(matrix, (float) x1, (float) y1, (float) z)
            .texture((u + (float) regionWidth) / (float) textureWidth, (v + (float) regionHeight) / (float) textureHeight);
        bufferBuilder.vertex(matrix, (float) x1, (float) y0, (float) z).texture((u + (float) regionWidth) / (float) textureWidth, v / (float) textureHeight);
        bufferBuilder.vertex(matrix, (float) x0, (float) y0, (float) z).texture(u / (float) textureWidth, (v + 0.0F) / (float) textureHeight);
        BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
    }

    public static void renderTextureA(
        MatrixStack matrices,
        double x0,
        double y0,
        double width,
        double height,
        float u,
        float v,
        double regionWidth,
        double regionHeight,
        double textureWidth,
        double textureHeight
    ) {
        double x1 = x0 + width;
        double y1 = y0 + height;
        double z = 0.0;
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        BufferBuilder bufferBuilder = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
        bufferBuilder.vertex(matrix, (float) x0, (float) y1, (float) z).texture(u / (float) textureWidth, (v + (float) regionHeight) / (float) textureHeight);
        bufferBuilder.vertex(matrix, (float) x1, (float) y1, (float) z)
            .texture((u + (float) regionWidth) / (float) textureWidth, (v + (float) regionHeight) / (float) textureHeight);
        bufferBuilder.vertex(matrix, (float) x1, (float) y0, (float) z).texture((u + (float) regionWidth) / (float) textureWidth, v / (float) textureHeight);
        bufferBuilder.vertex(matrix, (float) x0, (float) y0, (float) z).texture(u / (float) textureWidth, (v + 0.0F) / (float) textureHeight);
        BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
        RenderSystem.disableBlend();
    }

    public static void renderTexture(
        MatrixStack matrices,
        double x0,
        double y0,
        double width,
        double height,
        float u,
        float v,
        double regionWidth,
        double regionHeight,
        double textureWidth,
        double textureHeight,
        boolean antiAliasing
    ) {
        double x1 = x0 + width;
        double y1 = y0 + height;
        double z = 0.0;
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
        if (antiAliasing) {
            GL13.glEnable(32925);
        }

        Matrix4f matrix = matrices.peek().getPositionMatrix();
        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        BufferBuilder bufferBuilder = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
        bufferBuilder.vertex(matrix, (float) x0, (float) y1, (float) z).texture(u / (float) textureWidth, (v + (float) regionHeight) / (float) textureHeight);
        bufferBuilder.vertex(matrix, (float) x1, (float) y1, (float) z)
            .texture((u + (float) regionWidth) / (float) textureWidth, (v + (float) regionHeight) / (float) textureHeight);
        bufferBuilder.vertex(matrix, (float) x1, (float) y0, (float) z).texture((u + (float) regionWidth) / (float) textureWidth, v / (float) textureHeight);
        bufferBuilder.vertex(matrix, (float) x0, (float) y0, (float) z).texture(u / (float) textureWidth, (v + 0.0F) / (float) textureHeight);
        BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
        RenderSystem.disableBlend();
        RenderSystem.disableDepthTest();
        if (antiAliasing) {
            GL13.glDisable(32925);
        }
    }

    private static void registerTexture(Texture i, byte[] content) {
        try {
            ByteBuffer data = BufferUtils.createByteBuffer(content.length).put(content);
            data.flip();
            NativeImageBackedTexture tex = new NativeImageBackedTexture(NativeImage.read(data));
            MeteorClient.mc.execute(() -> MeteorClient.mc.getTextureManager().registerTexture(i.getId(), tex));
        } catch (Exception var4) {
        }
    }

    public static enum BlurDirection {
        UP,
        DOWN,
        LEFT,
        RIGHT;
    }

    public static enum Side {
        Left,
        Right,
        Top,
        Bottom;
    }

    public static class BlurredShadow {
        Texture id = new Texture("texture/remote/" + RandomStringUtils.randomAlphanumeric(16));

        public BlurredShadow(BufferedImage bufferedImage) {
            Render2DEngine.registerBufferedImageTexture(this.id, bufferedImage);
        }

        public void bind() {
            RenderSystem.setShaderTexture(0, this.id.getId());
        }
    }

    public record Rectangle(float x, float y, float x1, float y1) {
        public boolean contains(double x, double y) {
            return x >= this.x && x <= this.x1 && y >= this.y && y <= this.y1;
        }
    }
}
