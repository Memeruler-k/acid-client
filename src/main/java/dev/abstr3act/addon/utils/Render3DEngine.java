package dev.abstr3act.addon.utils;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.abstr3act.addon.utils.render.CaptureMark;
import dev.abstr3act.addon.utils.render.Render2DEngine;
import meteordevelopment.meteorclient.MeteorClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.MatrixStack.Entry;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.*;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Render3DEngine {
    public static final Matrix4f lastProjMat = new Matrix4f();
    public static final Matrix4f lastModMat = new Matrix4f();
    public static final Matrix4f lastWorldSpaceMatrix = new Matrix4f();
    public static List<FillAction> FILLED_QUEUE = new ArrayList<>();
    public static List<OutlineAction> OUTLINE_QUEUE = new ArrayList<>();
    public static List<FadeAction> FADE_QUEUE = new ArrayList<>();
    public static List<FillSideAction> FILLED_SIDE_QUEUE = new ArrayList<>();
    public static List<OutlineSideAction> OUTLINE_SIDE_QUEUE = new ArrayList<>();
    public static List<DebugLineAction> DEBUG_LINE_QUEUE = new ArrayList<>();
    public static List<LineAction> LINE_QUEUE = new ArrayList<>();
    private static float prevCircleStep;
    private static float circleStep;

    public static void drawCircle3D(MatrixStack stack, Entity ent, float radius, int color, int points, boolean hudColor, int colorOffset) {
        setupRender();
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.begin(DrawMode.DEBUG_LINE_STRIP, VertexFormats.POSITION_COLOR);
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        double x = ent.prevX
            + (ent.getX() - ent.prevX) * MeteorClient.mc.getRenderTickCounter().getTickDelta(true)
            - MeteorClient.mc.getEntityRenderDispatcher().camera.getPos().getX();
        double y = ent.prevY
            + (ent.getY() - ent.prevY) * MeteorClient.mc.getRenderTickCounter().getTickDelta(true)
            - MeteorClient.mc.getEntityRenderDispatcher().camera.getPos().getY();
        double z = ent.prevZ
            + (ent.getZ() - ent.prevZ) * MeteorClient.mc.getRenderTickCounter().getTickDelta(true)
            - MeteorClient.mc.getEntityRenderDispatcher().camera.getPos().getZ();
        stack.push();
        stack.translate(x, y, z);
        Matrix4f matrix = stack.peek().getPositionMatrix();

        for (int i = 0; i <= points; i++) {
            if (hudColor) {
                color = CaptureMark.getColor(i * colorOffset).getRGB();
            }

            bufferBuilder.vertex(matrix, (float) (radius * Math.cos(i * 6.28 / points)), 0.0F, (float) (radius * Math.sin(i * 6.28 / points))).color(color);
        }

        BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
        endRender();
        stack.translate(-x, -y, -z);
        stack.pop();
    }

    public static void onRender3D(MatrixStack stack) {
        if (!FILLED_QUEUE.isEmpty() || !FADE_QUEUE.isEmpty() || !FILLED_SIDE_QUEUE.isEmpty()) {
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder bufferBuilder = tessellator.begin(DrawMode.QUADS, VertexFormats.POSITION_COLOR);
            RenderSystem.disableDepthTest();
            setupRender();
            RenderSystem.setShader(GameRenderer::getPositionColorProgram);
            FILLED_QUEUE.forEach(action -> setFilledBoxVertexes(bufferBuilder, stack.peek().getPositionMatrix(), action.box(), action.color()));
            FADE_QUEUE.forEach(action -> setFilledFadePoints(action.box(), bufferBuilder, stack.peek().getPositionMatrix(), action.color(), action.color2()));
            FILLED_SIDE_QUEUE.forEach(action -> setFilledSidePoints(bufferBuilder, stack.peek().getPositionMatrix(), action.box, action.color(), action.side()));
            BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
            endRender();
            RenderSystem.enableDepthTest();
            FADE_QUEUE.clear();
            FILLED_SIDE_QUEUE.clear();
            FILLED_QUEUE.clear();
        }

        if (!OUTLINE_QUEUE.isEmpty() || !OUTLINE_SIDE_QUEUE.isEmpty()) {
            setupRender();
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder buffer = tessellator.begin(DrawMode.LINES, VertexFormats.LINES);
            RenderSystem.disableCull();
            RenderSystem.disableDepthTest();
            RenderSystem.setShader(GameRenderer::getRenderTypeLinesProgram);
            RenderSystem.lineWidth(2.0F);
            OUTLINE_QUEUE.forEach(
                action -> setOutlinePoints(action.box(), matrixFrom(action.box().minX, action.box().minY, action.box().minZ), buffer, action.color())
            );
            OUTLINE_SIDE_QUEUE.forEach(
                action -> setSideOutlinePoints(action.box, matrixFrom(action.box().minX, action.box().minY, action.box().minZ), buffer, action.color(), action.side())
            );
            BufferRenderer.drawWithGlobalProgram(buffer.end());
            RenderSystem.enableCull();
            RenderSystem.enableDepthTest();
            endRender();
            OUTLINE_QUEUE.clear();
            OUTLINE_SIDE_QUEUE.clear();
        }

        if (!DEBUG_LINE_QUEUE.isEmpty()) {
            setupRender();
            RenderSystem.disableDepthTest();
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder buffer = tessellator.begin(DrawMode.DEBUG_LINES, VertexFormats.LINES);
            RenderSystem.disableCull();
            RenderSystem.setShader(GameRenderer::getRenderTypeLinesProgram);
            DEBUG_LINE_QUEUE.forEach(
                action -> {
                    MatrixStack matrices = matrixFrom(action.start.getX(), action.start.getY(), action.start.getZ());
                    vertexLine(
                        matrices,
                        buffer,
                        0.0F,
                        0.0F,
                        0.0F,
                        (float) (action.end.getX() - action.start.getX()),
                        (float) (action.end.getY() - action.start.getY()),
                        (float) (action.end.getZ() - action.start.getZ()),
                        action.color
                    );
                }
            );
            BufferRenderer.drawWithGlobalProgram(buffer.end());
            RenderSystem.enableCull();
            RenderSystem.enableDepthTest();
            endRender();
            DEBUG_LINE_QUEUE.clear();
        }

        if (!LINE_QUEUE.isEmpty()) {
            setupRender();
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder buffer = tessellator.begin(DrawMode.LINES, VertexFormats.LINES);
            RenderSystem.disableCull();
            RenderSystem.setShader(GameRenderer::getRenderTypeLinesProgram);
            RenderSystem.lineWidth(2.0F);
            RenderSystem.disableDepthTest();
            LINE_QUEUE.forEach(
                action -> {
                    MatrixStack matrices = matrixFrom(action.start.getX(), action.start.getY(), action.start.getZ());
                    vertexLine(
                        matrices,
                        buffer,
                        0.0F,
                        0.0F,
                        0.0F,
                        (float) (action.end.getX() - action.start.getX()),
                        (float) (action.end.getY() - action.start.getY()),
                        (float) (action.end.getZ() - action.start.getZ()),
                        action.color
                    );
                }
            );
            BufferRenderer.drawWithGlobalProgram(buffer.end());
            RenderSystem.enableCull();
            RenderSystem.lineWidth(1.0F);
            RenderSystem.enableDepthTest();
            endRender();
            LINE_QUEUE.clear();
        }
    }

    @Deprecated
    public static void drawFilledBox(MatrixStack stack, Box box, Color c) {
        FILLED_QUEUE.add(new FillAction(box, c));
    }

    public static void setFilledBoxVertexes(@NotNull BufferBuilder bufferBuilder, Matrix4f m, @NotNull Box box, @NotNull Color c) {
        float minX = (float) (box.minX - MeteorClient.mc.getEntityRenderDispatcher().camera.getPos().getX());
        float minY = (float) (box.minY - MeteorClient.mc.getEntityRenderDispatcher().camera.getPos().getY());
        float minZ = (float) (box.minZ - MeteorClient.mc.getEntityRenderDispatcher().camera.getPos().getZ());
        float maxX = (float) (box.maxX - MeteorClient.mc.getEntityRenderDispatcher().camera.getPos().getX());
        float maxY = (float) (box.maxY - MeteorClient.mc.getEntityRenderDispatcher().camera.getPos().getY());
        float maxZ = (float) (box.maxZ - MeteorClient.mc.getEntityRenderDispatcher().camera.getPos().getZ());
        bufferBuilder.vertex(m, minX, minY, minZ).color(c.getRGB());
        bufferBuilder.vertex(m, maxX, minY, minZ).color(c.getRGB());
        bufferBuilder.vertex(m, maxX, minY, maxZ).color(c.getRGB());
        bufferBuilder.vertex(m, minX, minY, maxZ).color(c.getRGB());
        bufferBuilder.vertex(m, minX, minY, minZ).color(c.getRGB());
        bufferBuilder.vertex(m, minX, maxY, minZ).color(c.getRGB());
        bufferBuilder.vertex(m, maxX, maxY, minZ).color(c.getRGB());
        bufferBuilder.vertex(m, maxX, minY, minZ).color(c.getRGB());
        bufferBuilder.vertex(m, maxX, minY, minZ).color(c.getRGB());
        bufferBuilder.vertex(m, maxX, maxY, minZ).color(c.getRGB());
        bufferBuilder.vertex(m, maxX, maxY, maxZ).color(c.getRGB());
        bufferBuilder.vertex(m, maxX, minY, maxZ).color(c.getRGB());
        bufferBuilder.vertex(m, minX, minY, maxZ).color(c.getRGB());
        bufferBuilder.vertex(m, maxX, minY, maxZ).color(c.getRGB());
        bufferBuilder.vertex(m, maxX, maxY, maxZ).color(c.getRGB());
        bufferBuilder.vertex(m, minX, maxY, maxZ).color(c.getRGB());
        bufferBuilder.vertex(m, minX, minY, minZ).color(c.getRGB());
        bufferBuilder.vertex(m, minX, minY, maxZ).color(c.getRGB());
        bufferBuilder.vertex(m, minX, maxY, maxZ).color(c.getRGB());
        bufferBuilder.vertex(m, minX, maxY, minZ).color(c.getRGB());
        bufferBuilder.vertex(m, minX, maxY, minZ).color(c.getRGB());
        bufferBuilder.vertex(m, minX, maxY, maxZ).color(c.getRGB());
        bufferBuilder.vertex(m, maxX, maxY, maxZ).color(c.getRGB());
        bufferBuilder.vertex(m, maxX, maxY, minZ).color(c.getRGB());
    }

    @NotNull
    public static Box interpolateBox(@NotNull Box from, @NotNull Box to, float delta) {
        double X = Render2DEngine.interpolate(from.maxX, to.maxX, delta);
        double Y = Render2DEngine.interpolate(from.maxY, to.maxY, delta);
        double Z = Render2DEngine.interpolate(from.maxZ, to.maxZ, delta);
        double X1 = Render2DEngine.interpolate(from.minX, to.minX, delta);
        double Y1 = Render2DEngine.interpolate(from.minY, to.minY, delta);
        double Z1 = Render2DEngine.interpolate(from.minZ, to.minZ, delta);
        return new Box(X1, Y1, Z1, X, Y, Z);
    }

    @Deprecated
    public static void drawFilledSide(MatrixStack stack, @NotNull Box box, Color c, Direction dir) {
        FILLED_SIDE_QUEUE.add(new FillSideAction(box, c, dir));
    }

    public static void setFilledSidePoints(BufferBuilder buffer, Matrix4f matrix, Box box, Color c, Direction dir) {
        float minX = (float) (box.minX - MeteorClient.mc.getEntityRenderDispatcher().camera.getPos().getX());
        float minY = (float) (box.minY - MeteorClient.mc.getEntityRenderDispatcher().camera.getPos().getY());
        float minZ = (float) (box.minZ - MeteorClient.mc.getEntityRenderDispatcher().camera.getPos().getZ());
        float maxX = (float) (box.maxX - MeteorClient.mc.getEntityRenderDispatcher().camera.getPos().getX());
        float maxY = (float) (box.maxY - MeteorClient.mc.getEntityRenderDispatcher().camera.getPos().getY());
        float maxZ = (float) (box.maxZ - MeteorClient.mc.getEntityRenderDispatcher().camera.getPos().getZ());
        if (dir == Direction.DOWN) {
            buffer.vertex(matrix, minX, minY, minZ).color(c.getRGB());
            buffer.vertex(matrix, maxX, minY, minZ).color(c.getRGB());
            buffer.vertex(matrix, maxX, minY, maxZ).color(c.getRGB());
            buffer.vertex(matrix, minX, minY, maxZ).color(c.getRGB());
        }

        if (dir == Direction.NORTH) {
            buffer.vertex(matrix, minX, minY, minZ).color(c.getRGB());
            buffer.vertex(matrix, minX, maxY, minZ).color(c.getRGB());
            buffer.vertex(matrix, maxX, maxY, minZ).color(c.getRGB());
            buffer.vertex(matrix, maxX, minY, minZ).color(c.getRGB());
        }

        if (dir == Direction.EAST) {
            buffer.vertex(matrix, maxX, minY, minZ).color(c.getRGB());
            buffer.vertex(matrix, maxX, maxY, minZ).color(c.getRGB());
            buffer.vertex(matrix, maxX, maxY, maxZ).color(c.getRGB());
            buffer.vertex(matrix, maxX, minY, maxZ).color(c.getRGB());
        }

        if (dir == Direction.SOUTH) {
            buffer.vertex(matrix, minX, minY, maxZ).color(c.getRGB());
            buffer.vertex(matrix, maxX, minY, maxZ).color(c.getRGB());
            buffer.vertex(matrix, maxX, maxY, maxZ).color(c.getRGB());
            buffer.vertex(matrix, minX, maxY, maxZ).color(c.getRGB());
        }

        if (dir == Direction.WEST) {
            buffer.vertex(matrix, minX, minY, minZ).color(c.getRGB());
            buffer.vertex(matrix, minX, minY, maxZ).color(c.getRGB());
            buffer.vertex(matrix, minX, maxY, maxZ).color(c.getRGB());
            buffer.vertex(matrix, minX, maxY, minZ).color(c.getRGB());
        }

        if (dir == Direction.UP) {
            buffer.vertex(matrix, minX, maxY, minZ).color(c.getRGB());
            buffer.vertex(matrix, minX, maxY, maxZ).color(c.getRGB());
            buffer.vertex(matrix, maxX, maxY, maxZ).color(c.getRGB());
            buffer.vertex(matrix, maxX, maxY, minZ).color(c.getRGB());
        }
    }

    @NotNull
    public static Vec3d worldSpaceToScreenSpace(@NotNull Vec3d pos) {
        Camera camera = MeteorClient.mc.getEntityRenderDispatcher().camera;
        int displayHeight = MeteorClient.mc.getWindow().getHeight();
        int[] viewport = new int[4];
        GL11.glGetIntegerv(2978, viewport);
        Vector3f target = new Vector3f();
        double deltaX = pos.x - camera.getPos().x;
        double deltaY = pos.y - camera.getPos().y;
        double deltaZ = pos.z - camera.getPos().z;
        Vector4f transformedCoordinates = new Vector4f((float) deltaX, (float) deltaY, (float) deltaZ, 1.0F).mul(lastWorldSpaceMatrix);
        Matrix4f matrixProj = new Matrix4f(lastProjMat);
        Matrix4f matrixModel = new Matrix4f(lastModMat);
        matrixProj.mul(matrixModel).project(transformedCoordinates.x(), transformedCoordinates.y(), transformedCoordinates.z(), viewport, target);
        return new Vec3d(target.x / getScaleFactor(), (displayHeight - target.y) / getScaleFactor(), target.z);
    }

    public static double getScaleFactor() {
        return MeteorClient.mc.getWindow().getScaleFactor();
    }

    @Deprecated
    public static void drawFilledFadeBox(@NotNull MatrixStack stack, @NotNull Box box, @NotNull Color c, @NotNull Color c1) {
        FADE_QUEUE.add(new FadeAction(box, c, c1));
    }

    public static void setFilledFadePoints(Box box, BufferBuilder buffer, Matrix4f posMatrix, Color c, Color c1) {
        float minX = (float) (box.minX - MeteorClient.mc.getEntityRenderDispatcher().camera.getPos().getX());
        float minY = (float) (box.minY - MeteorClient.mc.getEntityRenderDispatcher().camera.getPos().getY());
        float minZ = (float) (box.minZ - MeteorClient.mc.getEntityRenderDispatcher().camera.getPos().getZ());
        float maxX = (float) (box.maxX - MeteorClient.mc.getEntityRenderDispatcher().camera.getPos().getX());
        float maxY = (float) (box.maxY - MeteorClient.mc.getEntityRenderDispatcher().camera.getPos().getY());
        float maxZ = (float) (box.maxZ - MeteorClient.mc.getEntityRenderDispatcher().camera.getPos().getZ());
        buffer.vertex(posMatrix, minX, minY, minZ).color(c.getRGB());
        buffer.vertex(posMatrix, minX, maxY, minZ).color(c1.getRGB());
        buffer.vertex(posMatrix, maxX, maxY, minZ).color(c1.getRGB());
        buffer.vertex(posMatrix, maxX, minY, minZ).color(c.getRGB());
        buffer.vertex(posMatrix, maxX, minY, minZ).color(c.getRGB());
        buffer.vertex(posMatrix, maxX, maxY, minZ).color(c1.getRGB());
        buffer.vertex(posMatrix, maxX, maxY, maxZ).color(c1.getRGB());
        buffer.vertex(posMatrix, maxX, minY, maxZ).color(c.getRGB());
        buffer.vertex(posMatrix, minX, minY, maxZ).color(c.getRGB());
        buffer.vertex(posMatrix, maxX, minY, maxZ).color(c.getRGB());
        buffer.vertex(posMatrix, maxX, maxY, maxZ).color(c1.getRGB());
        buffer.vertex(posMatrix, minX, maxY, maxZ).color(c1.getRGB());
        buffer.vertex(posMatrix, minX, minY, minZ).color(c.getRGB());
        buffer.vertex(posMatrix, minX, minY, maxZ).color(c.getRGB());
        buffer.vertex(posMatrix, minX, maxY, maxZ).color(c1.getRGB());
        buffer.vertex(posMatrix, minX, maxY, minZ).color(c1.getRGB());
        buffer.vertex(posMatrix, minX, maxY, minZ).color(c1.getRGB());
        buffer.vertex(posMatrix, minX, maxY, maxZ).color(c1.getRGB());
        buffer.vertex(posMatrix, maxX, maxY, maxZ).color(c1.getRGB());
        buffer.vertex(posMatrix, maxX, maxY, minZ).color(c1.getRGB());
    }

    public static void drawLine(@NotNull Vec3d start, @NotNull Vec3d end, @NotNull Color color) {
        LINE_QUEUE.add(new LineAction(start, end, color));
    }

    @Deprecated
    public static void drawBoxOutline(@NotNull Box box, Color color, float lineWidth) {
        OUTLINE_QUEUE.add(new OutlineAction(box, color, lineWidth));
    }

    public static void setOutlinePoints(Box box, MatrixStack matrices, BufferBuilder buffer, Color color) {
        box = box.offset(new Vec3d(box.minX, box.minY, box.minZ).negate());
        float x1 = (float) box.minX;
        float y1 = (float) box.minY;
        float z1 = (float) box.minZ;
        float x2 = (float) box.maxX;
        float y2 = (float) box.maxY;
        float z2 = (float) box.maxZ;
        vertexLine(matrices, buffer, x1, y1, z1, x2, y1, z1, color);
        vertexLine(matrices, buffer, x2, y1, z1, x2, y1, z2, color);
        vertexLine(matrices, buffer, x2, y1, z2, x1, y1, z2, color);
        vertexLine(matrices, buffer, x1, y1, z2, x1, y1, z1, color);
        vertexLine(matrices, buffer, x1, y1, z2, x1, y2, z2, color);
        vertexLine(matrices, buffer, x1, y1, z1, x1, y2, z1, color);
        vertexLine(matrices, buffer, x2, y1, z2, x2, y2, z2, color);
        vertexLine(matrices, buffer, x2, y1, z1, x2, y2, z1, color);
        vertexLine(matrices, buffer, x1, y2, z1, x2, y2, z1, color);
        vertexLine(matrices, buffer, x2, y2, z1, x2, y2, z2, color);
        vertexLine(matrices, buffer, x2, y2, z2, x1, y2, z2, color);
        vertexLine(matrices, buffer, x1, y2, z2, x1, y2, z1, color);
    }

    @Deprecated
    public static void drawSideOutline(@NotNull Box box, Color color, float lineWidth, Direction dir) {
        OUTLINE_SIDE_QUEUE.add(new OutlineSideAction(box, color, lineWidth, dir));
    }

    public static void setSideOutlinePoints(Box box, MatrixStack matrices, BufferBuilder buffer, Color color, Direction dir) {
        box = box.offset(new Vec3d(box.minX, box.minY, box.minZ).negate());
        float x1 = (float) box.minX;
        float y1 = (float) box.minY;
        float z1 = (float) box.minZ;
        float x2 = (float) box.maxX;
        float y2 = (float) box.maxY;
        float z2 = (float) box.maxZ;
        switch (dir) {
            case UP:
                vertexLine(matrices, buffer, x1, y2, z1, x2, y2, z1, color);
                vertexLine(matrices, buffer, x2, y2, z1, x2, y2, z2, color);
                vertexLine(matrices, buffer, x2, y2, z2, x1, y2, z2, color);
                vertexLine(matrices, buffer, x1, y2, z2, x1, y2, z1, color);
                break;
            case DOWN:
                vertexLine(matrices, buffer, x1, y1, z1, x2, y1, z1, color);
                vertexLine(matrices, buffer, x2, y1, z1, x2, y1, z2, color);
                vertexLine(matrices, buffer, x2, y1, z2, x1, y1, z2, color);
                vertexLine(matrices, buffer, x1, y1, z2, x1, y1, z1, color);
                break;
            case EAST:
                vertexLine(matrices, buffer, x2, y1, z1, x2, y2, z1, color);
                vertexLine(matrices, buffer, x2, y1, z2, x2, y2, z2, color);
                vertexLine(matrices, buffer, x2, y2, z2, x2, y2, z1, color);
                vertexLine(matrices, buffer, x2, y1, z2, x2, y1, z1, color);
                break;
            case WEST:
                vertexLine(matrices, buffer, x1, y1, z1, x1, y2, z1, color);
                vertexLine(matrices, buffer, x1, y1, z2, x1, y2, z2, color);
                vertexLine(matrices, buffer, x1, y2, z2, x1, y2, z1, color);
                vertexLine(matrices, buffer, x1, y1, z2, x1, y1, z1, color);
                break;
            case NORTH:
                vertexLine(matrices, buffer, x2, y1, z1, x2, y2, z1, color);
                vertexLine(matrices, buffer, x1, y1, z1, x1, y2, z1, color);
                vertexLine(matrices, buffer, x2, y1, z1, x1, y1, z1, color);
                vertexLine(matrices, buffer, x2, y2, z1, x1, y2, z1, color);
                break;
            case SOUTH:
                vertexLine(matrices, buffer, x1, y1, z2, x1, y2, z2, color);
                vertexLine(matrices, buffer, x2, y1, z2, x2, y2, z2, color);
                vertexLine(matrices, buffer, x1, y1, z2, x2, y1, z2, color);
                vertexLine(matrices, buffer, x1, y2, z2, x2, y2, z2, color);
        }
    }

    public static void vertexLine(
        @NotNull MatrixStack matrices, @NotNull VertexConsumer buffer, float x1, float y1, float z1, float x2, float y2, float z2, @NotNull Color lineColor
    ) {
        Matrix4f model = matrices.peek().getPositionMatrix();
        Entry entry = matrices.peek();
        Vector3f normalVec = getNormal(x1, y1, z1, x2, y2, z2);
        buffer.vertex(model, x1, y1, z1)
            .color(lineColor.getRed(), lineColor.getGreen(), lineColor.getBlue(), lineColor.getAlpha())
            .normal(entry, normalVec.x(), normalVec.y(), normalVec.z());
        buffer.vertex(model, x2, y2, z2)
            .color(lineColor.getRed(), lineColor.getGreen(), lineColor.getBlue(), lineColor.getAlpha())
            .normal(entry, normalVec.x(), normalVec.y(), normalVec.z());
    }

    @NotNull
    public static Vector3f getNormal(float x1, float y1, float z1, float x2, float y2, float z2) {
        float xNormal = x2 - x1;
        float yNormal = y2 - y1;
        float zNormal = z2 - z1;
        float normalSqrt = MathHelper.sqrt(xNormal * xNormal + yNormal * yNormal + zNormal * zNormal);
        return new Vector3f(xNormal / normalSqrt, yNormal / normalSqrt, zNormal / normalSqrt);
    }

    @NotNull
    public static MatrixStack matrixFrom(double x, double y, double z) {
        MatrixStack matrices = new MatrixStack();
        Camera camera = MinecraftClient.getInstance().gameRenderer.getCamera();
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(camera.getYaw() + 180.0F));
        matrices.translate(x - camera.getPos().x, y - camera.getPos().y, z - camera.getPos().z);
        return matrices;
    }

    public static void setupRender() {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
    }

    public static void endRender() {
        RenderSystem.disableBlend();
    }

    public static void drawLineDebug(Vec3d start, Vec3d end, Color color) {
        DEBUG_LINE_QUEUE.add(new DebugLineAction(start, end, color));
    }

    public record DebugLineAction(Vec3d start, Vec3d end, Color color) {
    }

    public record FadeAction(Box box, Color color, Color color2) {
    }

    public record FillAction(Box box, Color color) {
    }

    public record FillSideAction(Box box, Color color, Direction side) {
    }

    public record LineAction(Vec3d start, Vec3d end, Color color) {
    }

    public record OutlineAction(Box box, Color color, float lineWidth) {
    }

    public record OutlineSideAction(Box box, Color color, float lineWidth, Direction side) {
    }
}
