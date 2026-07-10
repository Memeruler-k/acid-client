package dev.abstr3act.addon.utils.notifications;

import dev.abstr3act.addon.mixin.accessor.MeshMixin;
import meteordevelopment.meteorclient.renderer.Mesh;
import meteordevelopment.meteorclient.renderer.Renderer2D;
import meteordevelopment.meteorclient.utils.render.color.Color;
import org.lwjgl.system.MemoryUtil;

public class Renderer2DQuad extends Renderer2D {
    private final double circleNone = 0.0;
    private final double circleQuarter = Math.PI / 2;
    private final double circleHalf = Math.PI;
    private final double circleThreeQuarter = Math.PI * 3.0 / 2.0;

    public Renderer2DQuad(boolean texture) {
        super(texture);
    }

    public static void triangle(int i1, int i2, int i3, Mesh entity) {
        MeshMixin accessor = (MeshMixin) entity;
        long p = accessor.getIndicesPointer() + accessor.getIndicesCount() * 4L;
        MemoryUtil.memPutInt(p, i1);
        MemoryUtil.memPutInt(p + 4L, i2);
        MemoryUtil.memPutInt(p + 8L, i3);
        accessor.setIndicesCount(accessor.getIndicesCount() + 3);
        accessor.growIfNeeded();
    }

    public void quadRoundedOutline(double x, double y, double width, double height, Color color, double r, double s) {
        double var14 = this.getR(r, width, height);
        if (var14 <= 0.0) {
            this.quad(x, y, width, s, color);
            this.quad(x, y + height - s, width, s, color);
            this.quad(x, y + s, s, height - s * 2.0, color);
            this.quad(x + width - s, y + s, s, height - s * 2.0, color);
        } else {
            this.circlePartOutline(x + var14, y + var14, var14, Math.PI * 3.0 / 2.0, Math.PI / 2, color, s);
            this.quad(x + var14, y, width - var14 * 2.0, s, color);
            this.circlePartOutline(x + width - var14, y + var14, var14, 0.0, Math.PI / 2, color, s);
            this.quad(x, y + var14, s, height - var14 * 2.0, color);
            this.quad(x + width - s, y + var14, s, height - var14 * 2.0, color);
            this.circlePartOutline(x + width - var14, y + height - var14, var14, Math.PI / 2, Math.PI / 2, color, s);
            this.quad(x + var14, y + height - s, width - var14 * 2.0, s, color);
            this.circlePartOutline(x + var14, y + height - var14, var14, Math.PI, Math.PI / 2, color, s);
        }
    }

    public void quadRounded(double x, double y, double width, double height, Color color, double r, boolean roundTop) {
        double var13 = this.getR(r, width, height);
        if (var13 <= 0.0) {
            this.quad(x, y, width, height, color);
        } else {
            if (roundTop) {
                this.circlePart(x + var13, y + var13, var13, Math.PI * 3.0 / 2.0, Math.PI / 2, color);
                this.quad(x + var13, y, width - 2.0 * var13, var13, color);
                this.circlePart(x + width - var13, y + var13, var13, 0.0, Math.PI / 2, color);
                this.quad(x, y + var13, width, height - 2.0 * var13, color);
            } else {
                this.quad(x, y, width, height - var13, color);
            }

            this.circlePart(x + width - var13, y + height - var13, var13, Math.PI / 2, Math.PI / 2, color);
            this.quad(x + var13, y + height - var13, width - 2.0 * var13, var13, color);
            this.circlePart(x + var13, y + height - var13, var13, Math.PI, Math.PI / 2, color);
        }
    }

    public void quadRoundedSide(double x, double y, double width, double height, Color color, double r, boolean right) {
        double var13 = this.getR(r, width, height);
        if (var13 <= 0.0) {
            this.quad(x, y, width, height, color);
        } else if (right) {
            this.circlePart(x + width - var13, y + var13, var13, 0.0, Math.PI / 2, color);
            this.circlePart(x + width - var13, y + height - var13, var13, Math.PI / 2, Math.PI / 2, color);
            this.quad(x, y, width - var13, height, color);
            this.quad(x + width - var13, y + var13, var13, height - var13 * 2.0, color);
        } else {
            this.circlePart(x + var13, y + var13, var13, Math.PI * 3.0 / 2.0, Math.PI / 2, color);
            this.circlePart(x + var13, y + height - var13, var13, Math.PI, Math.PI / 2, color);
            this.quad(x + var13, y, width - var13, height, color);
            this.quad(x, y + var13, var13, height - var13 * 2.0, color);
        }
    }

    private double getR(double r, double w, double h) {
        if (r * 2.0 > h) {
            r = h / 2.0;
        }

        if (r * 2.0 > w) {
            r = w / 2.0;
        }

        return r;
    }

    private int getCirDepth(double r, double angle) {
        return Math.max(1, (int) (angle * r / (Math.PI / 2)));
    }

    public void circlePart(double x, double y, double r, double startAngle, double angle, Color color) {
        int cirDepth = this.getCirDepth(r, angle);
        double cirPart = angle / cirDepth;
        int center = this.triangles.vec2(x, y).color(color).next();
        int prev = this.vecOnCircle(x, y, r, startAngle, color);

        for (int i = 1; i < cirDepth + 1; i++) {
            int next = this.vecOnCircle(x, y, r, startAngle + cirPart * i, color);
            triangle(prev, center, next, this.triangles);
            prev = next;
        }
    }

    public void circlePartOutline(double x, double y, double r, double startAngle, double angle, Color color, double outlineWidth) {
        if (outlineWidth >= r) {
            this.circlePart(x, y, r, startAngle, angle, color);
        } else {
            int cirDepth = this.getCirDepth(r, angle);
            double cirPart = angle / cirDepth;
            int innerPrev = this.vecOnCircle(x, y, r - outlineWidth, startAngle, color);
            int outerPrev = this.vecOnCircle(x, y, r, startAngle, color);

            for (int i = 1; i < cirDepth + 1; i++) {
                int inner = this.vecOnCircle(x, y, r - outlineWidth, startAngle + cirPart * i, color);
                int outer = this.vecOnCircle(x, y, r, startAngle + cirPart * i, color);
                this.triangles.quad(inner, innerPrev, outerPrev, outer);
                innerPrev = inner;
                outerPrev = outer;
            }
        }
    }

    private int vecOnCircle(double x, double y, double r, double angle, Color color) {
        return this.triangles.vec2(x + Math.sin(angle) * r, y - Math.cos(angle) * r).color(color).next();
    }
}
