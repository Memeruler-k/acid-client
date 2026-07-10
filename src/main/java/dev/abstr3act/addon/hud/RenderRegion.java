package dev.abstr3act.addon.hud;

public record RenderRegion(
    double x, double y, double width, double height, float u, float v, double regionWidth, double regionHeight,
    double textureWidth, double textureHeight
) {
}
