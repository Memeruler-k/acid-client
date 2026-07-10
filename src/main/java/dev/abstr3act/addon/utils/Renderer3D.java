package dev.abstr3act.addon.utils;

import meteordevelopment.meteorclient.renderer.*;
import meteordevelopment.meteorclient.renderer.Mesh.Attrib;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.world.Dir;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;

public class Renderer3D {
    public static final Mesh lines = new ShaderMesh(Shaders.POS_COLOR, DrawMode.Lines, new Attrib[]{Attrib.Vec3, Attrib.Color});
    public final Mesh triangles = new ShaderMesh(Shaders.POS_COLOR, DrawMode.Triangles, new Attrib[]{Attrib.Vec3, Attrib.Color});

    public static void line(double x1, double y1, double z1, double x2, double y2, double z2, Color color1, Color color2) {
        lines.line(lines.vec3(x1, y1, z1).color(color1).next(), lines.vec3(x2, y2, z2).color(color2).next());
    }

    public static void rainbowCircle(MatrixStack matrices, double x, double z, double y, double radius) {
        matrices.push();
        int segments = 360;

        for (int i = 0; i <= segments; i++) {
            double currentAngle = i * Math.PI / 180.0;
            double nextAngle = (i + 1) * Math.PI / 180.0;
            double x1 = x - Math.sin(currentAngle) * radius;
            double z1 = z + Math.cos(currentAngle) * radius;
            double x2 = x - Math.sin(nextAngle) * radius;
            double z2 = z + Math.cos(nextAngle) * radius;
            Color color1 = generateRainbowColor(i, segments);
            Color color2 = generateRainbowColor(i + 1, segments);
            line(x1, y, z1, x2, y, z2, color1, color2);
        }

        matrices.pop();
    }

    public static Color generateRainbowColor(int step, int totalSteps) {
        float normalizedStep = (float) step / totalSteps;
        float red = (float) (Math.sin(normalizedStep * 2.0F * Math.PI) * 0.5 + 0.5);
        float green = (float) (Math.sin(normalizedStep * 2.0F * Math.PI + (Math.PI * 2.0 / 3.0)) * 0.5 + 0.5);
        float blue = (float) (Math.sin(normalizedStep * 2.0F * Math.PI + (Math.PI * 4.0 / 3.0)) * 0.5 + 0.5);
        return new Color((int) (red * 255.0F), (int) (green * 255.0F), (int) (blue * 255.0F), 255);
    }

    public void begin() {
        lines.begin();
        this.triangles.begin();
    }

    public void end() {
        lines.end();
        this.triangles.end();
    }

    public void render(MatrixStack matrices, float lineWidth) {
        lines.render(matrices);
        this.triangles.render(matrices);
    }

    public void line(@NotNull Vec3d vec1, @NotNull Vec3d vec2, Color color) {
        this.line(vec1.x, vec1.y, vec1.z, vec2.x, vec2.y, vec2.z, color);
    }

    public void line(double x1, double y1, double z1, double x2, double y2, double z2, Color color) {
        line(x1, y1, z1, x2, y2, z2, color, color);
    }

    public void boxLines(double x1, double y1, double z1, double x2, double y2, double z2, Color color, int excludeDir) {
        int blb = lines.vec3(x1, y1, z1).color(color).next();
        int blf = lines.vec3(x1, y1, z2).color(color).next();
        int brb = lines.vec3(x2, y1, z1).color(color).next();
        int brf = lines.vec3(x2, y1, z2).color(color).next();
        int tlb = lines.vec3(x1, y2, z1).color(color).next();
        int tlf = lines.vec3(x1, y2, z2).color(color).next();
        int trb = lines.vec3(x2, y2, z1).color(color).next();
        int trf = lines.vec3(x2, y2, z2).color(color).next();
        if (excludeDir == 0) {
            lines.line(blb, tlb);
            lines.line(blf, tlf);
            lines.line(brb, trb);
            lines.line(brf, trf);
            lines.line(blb, blf);
            lines.line(brb, brf);
            lines.line(blb, brb);
            lines.line(blf, brf);
            lines.line(tlb, tlf);
            lines.line(trb, trf);
            lines.line(tlb, trb);
            lines.line(tlf, trf);
        } else {
            if (Dir.isNot(excludeDir, (byte) 32) && Dir.isNot(excludeDir, (byte) 8)) {
                lines.line(blb, tlb);
            }

            if (Dir.isNot(excludeDir, (byte) 32) && Dir.isNot(excludeDir, (byte) 16)) {
                lines.line(blf, tlf);
            }

            if (Dir.isNot(excludeDir, (byte) 64) && Dir.isNot(excludeDir, (byte) 8)) {
                lines.line(brb, trb);
            }

            if (Dir.isNot(excludeDir, (byte) 64) && Dir.isNot(excludeDir, (byte) 16)) {
                lines.line(brf, trf);
            }

            if (Dir.isNot(excludeDir, (byte) 32) && Dir.isNot(excludeDir, (byte) 4)) {
                lines.line(blb, blf);
            }

            if (Dir.isNot(excludeDir, (byte) 64) && Dir.isNot(excludeDir, (byte) 4)) {
                lines.line(brb, brf);
            }

            if (Dir.isNot(excludeDir, (byte) 8) && Dir.isNot(excludeDir, (byte) 4)) {
                lines.line(blb, brb);
            }

            if (Dir.isNot(excludeDir, (byte) 16) && Dir.isNot(excludeDir, (byte) 4)) {
                lines.line(blf, brf);
            }

            if (Dir.isNot(excludeDir, (byte) 32) && Dir.isNot(excludeDir, (byte) 2)) {
                lines.line(tlb, tlf);
            }

            if (Dir.isNot(excludeDir, (byte) 64) && Dir.isNot(excludeDir, (byte) 2)) {
                lines.line(trb, trf);
            }

            if (Dir.isNot(excludeDir, (byte) 8) && Dir.isNot(excludeDir, (byte) 2)) {
                lines.line(tlb, trb);
            }

            if (Dir.isNot(excludeDir, (byte) 16) && Dir.isNot(excludeDir, (byte) 2)) {
                lines.line(tlf, trf);
            }
        }

        lines.growIfNeeded();
    }

    public void blockLines(int x, int y, int z, Color color, int excludeDir) {
        this.boxLines(x, y, z, x + 1, y + 1, z + 1, color, excludeDir);
    }

    public void quad(
        double x1,
        double y1,
        double z1,
        double x2,
        double y2,
        double z2,
        double x3,
        double y3,
        double z3,
        double x4,
        double y4,
        double z4,
        Color topLeft,
        Color topRight,
        Color bottomRight,
        Color bottomLeft
    ) {
        this.triangles
            .quad(
                this.triangles.vec3(x1, y1, z1).color(bottomLeft).next(),
                this.triangles.vec3(x2, y2, z2).color(topLeft).next(),
                this.triangles.vec3(x3, y3, z3).color(topRight).next(),
                this.triangles.vec3(x4, y4, z4).color(bottomRight).next()
            );
    }

    public void quad(
        double x1, double y1, double z1, double x2, double y2, double z2, double x3, double y3, double z3, double x4, double y4, double z4, Color color
    ) {
        this.quad(x1, y1, z1, x2, y2, z2, x3, y3, z3, x4, y4, z4, color, color, color, color);
    }

    public void quadVertical(double x1, double y1, double z1, double x2, double y2, double z2, Color color) {
        this.quad(x1, y1, z1, x1, y2, z1, x2, y2, z2, x2, y1, z2, color);
    }

    public void quadHorizontal(double x1, double y, double z1, double x2, double z2, Color color) {
        this.quad(x1, y, z1, x1, y, z2, x2, y, z2, x2, y, z1, color);
    }

    public void gradientQuadVertical(double x1, double y1, double z1, double x2, double y2, double z2, Color topColor, Color bottomColor) {
        this.quad(x1, y1, z1, x1, y2, z1, x2, y2, z2, x2, y1, z2, topColor, topColor, bottomColor, bottomColor);
    }

    public void side(
        double x1,
        double y1,
        double z1,
        double x2,
        double y2,
        double z2,
        double x3,
        double y3,
        double z3,
        double x4,
        double y4,
        double z4,
        Color sideColor,
        Color lineColor,
        ShapeMode mode
    ) {
        if (mode.lines()) {
            int i1 = lines.vec3(x1, y1, z1).color(lineColor).next();
            int i2 = lines.vec3(x2, y2, z2).color(lineColor).next();
            int i3 = lines.vec3(x3, y3, z3).color(lineColor).next();
            int i4 = lines.vec3(x4, y4, z4).color(lineColor).next();
            lines.line(i1, i2);
            lines.line(i2, i3);
            lines.line(i3, i4);
            lines.line(i4, i1);
        }

        if (mode.sides()) {
            this.quad(x1, y1, z1, x2, y2, z2, x3, y3, z3, x4, y4, z4, sideColor);
        }
    }

    public void sideVertical(double x1, double y1, double z1, double x2, double y2, double z2, Color sideColor, Color lineColor, ShapeMode mode) {
        this.side(x1, y1, z1, x1, y2, z1, x2, y2, z2, x2, y1, z2, sideColor, lineColor, mode);
    }

    public void sideHorizontal(double x1, double y, double z1, double x2, double z2, Color sideColor, Color lineColor, ShapeMode mode) {
        this.side(x1, y, z1, x1, y, z2, x2, y, z2, x2, y, z1, sideColor, lineColor, mode);
    }

    public void boxSides(double x1, double y1, double z1, double x2, double y2, double z2, Color color, int excludeDir) {
        int blb = this.triangles.vec3(x1, y1, z1).color(color).next();
        int blf = this.triangles.vec3(x1, y1, z2).color(color).next();
        int brb = this.triangles.vec3(x2, y1, z1).color(color).next();
        int brf = this.triangles.vec3(x2, y1, z2).color(color).next();
        int tlb = this.triangles.vec3(x1, y2, z1).color(color).next();
        int tlf = this.triangles.vec3(x1, y2, z2).color(color).next();
        int trb = this.triangles.vec3(x2, y2, z1).color(color).next();
        int trf = this.triangles.vec3(x2, y2, z2).color(color).next();
        if (excludeDir == 0) {
            this.triangles.quad(blb, blf, tlf, tlb);
            this.triangles.quad(brb, trb, trf, brf);
            this.triangles.quad(blb, tlb, trb, brb);
            this.triangles.quad(blf, brf, trf, tlf);
            this.triangles.quad(blb, brb, brf, blf);
            this.triangles.quad(tlb, tlf, trf, trb);
        } else {
            if (Dir.isNot(excludeDir, (byte) 32)) {
                this.triangles.quad(blb, blf, tlf, tlb);
            }

            if (Dir.isNot(excludeDir, (byte) 64)) {
                this.triangles.quad(brb, trb, trf, brf);
            }

            if (Dir.isNot(excludeDir, (byte) 8)) {
                this.triangles.quad(blb, tlb, trb, brb);
            }

            if (Dir.isNot(excludeDir, (byte) 16)) {
                this.triangles.quad(blf, brf, trf, tlf);
            }

            if (Dir.isNot(excludeDir, (byte) 4)) {
                this.triangles.quad(blb, brb, brf, blf);
            }

            if (Dir.isNot(excludeDir, (byte) 2)) {
                this.triangles.quad(tlb, tlf, trf, trb);
            }
        }

        this.triangles.growIfNeeded();
    }

    public void blockSides(int x, int y, int z, Color color, int excludeDir) {
        this.boxSides(x, y, z, x + 1, y + 1, z + 1, color, excludeDir);
    }

    public void box(double x1, double y1, double z1, double x2, double y2, double z2, Color sideColor, Color lineColor, ShapeMode mode, int excludeDir) {
        if (mode.lines()) {
            this.boxLines(x1, y1, z1, x2, y2, z2, lineColor, excludeDir);
        }

        if (mode.sides()) {
            this.boxSides(x1, y1, z1, x2, y2, z2, sideColor, excludeDir);
        }
    }

    public void box(BlockPos pos, Color sideColor, Color lineColor, ShapeMode mode, int excludeDir) {
        if (mode.lines()) {
            this.boxLines(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1, lineColor, excludeDir);
        }

        if (mode.sides()) {
            this.boxSides(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1, sideColor, excludeDir);
        }
    }

    public void box(Box box, Color sideColor, Color lineColor, ShapeMode mode, int excludeDir) {
        if (mode.lines()) {
            this.boxLines(box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ, lineColor, excludeDir);
        }

        if (mode.sides()) {
            this.boxSides(box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ, sideColor, excludeDir);
        }
    }

    public void circle(MatrixStack matrices, double x, double y, double z, double radius, Color color) {
        this.circle(matrices, x, z, x, z, y, radius, color);
    }

    public void circle(MatrixStack matrices, double x1, double z1, double x2, double z2, double y, double radius, Color color) {
        matrices.push();

        for (int i = 5; i <= 360; i++) {
            double MPI = Math.PI;
            double x = x1 - Math.sin(i * MPI / 180.0) * radius;
            double z = z1 + Math.cos(i * MPI / 180.0) * radius;
            double xx = x2 - Math.sin((i - 5) * MPI / 180.0) * radius;
            double zz = z2 + Math.cos((i - 5) * MPI / 180.0) * radius;
            this.line(x, y, z, xx, y, zz, color);
        }

        matrices.pop();
    }
}
