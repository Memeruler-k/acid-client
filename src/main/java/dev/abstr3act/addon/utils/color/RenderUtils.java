package dev.abstr3act.addon.utils.color;

import meteordevelopment.meteorclient.renderer.Renderer2D;

public class RenderUtils {
    public static void drawSolidQuad(double x, double y, double width, double height, meteordevelopment.meteorclient.utils.render.color.Color color) {
        Renderer2D.COLOR.quad(x, y, width, height, color);
    }

    public static void drawGradientQuad(
        double x,
        double y,
        double width,
        double height,
        meteordevelopment.meteorclient.utils.render.color.Color topLeft,
        meteordevelopment.meteorclient.utils.render.color.Color topRight,
        meteordevelopment.meteorclient.utils.render.color.Color bottomRight,
        meteordevelopment.meteorclient.utils.render.color.Color bottomLeft
    ) {
        Renderer2D.COLOR.quad(x, y, width, height, topLeft, topRight, bottomRight, bottomLeft);
    }
}
