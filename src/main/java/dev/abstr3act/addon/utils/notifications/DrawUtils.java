package dev.abstr3act.addon.utils.notifications;

import meteordevelopment.meteorclient.renderer.text.TextRenderer;

import java.awt.*;
import java.util.regex.Pattern;

public class DrawUtils {
    public static final Pattern COLOR_CODE_PATTERN = Pattern.compile("[§|&][0123456789abcdefklmnor]");
    private static final int[] COLOR_CODES = new int[]{
        -16777216, -16777046, -16733696, -16733526, -5636096, -5635926, -22016, -5592406, -11184811, -11184641, -11141291, -11141121, -43691, -43521, -171, -1
    };
    public static Renderer2DQuad renderer;

    public static double render(String text, double x, double y, Color color, boolean shadow) {
        TextRenderer renderer = TextRenderer.get();
        double width = 0.0;
        int currentColor = color.getRGB();
        char[] characters = text.toCharArray();
        float originalX = (float) x;
        String[] parts = COLOR_CODE_PATTERN.split(text);
        int index = 0;

        for (String s : parts) {
            for (String s2 : s.split("\n")) {
                for (String s3 : s2.split("\r")) {
                    renderer.render(s3, x, y, new meteordevelopment.meteorclient.utils.render.color.Color(currentColor));
                    x += renderer.getWidth(s3);
                    index += s3.length();
                    if (index < characters.length && characters[index] == '\r') {
                        x = originalX;
                        index++;
                    }
                }

                if (index < characters.length && characters[index] == '\n') {
                    x = originalX;
                    y += renderer.getHeight(shadow) * 2.0;
                    index++;
                }
            }

            if (index < characters.length) {
                char colorCode = characters[index];
                if (colorCode == 167 || colorCode == '&') {
                    char colorChar = characters[index + 1];
                    int codeIndex = "0123456789abcdef".indexOf(colorChar);
                    if (codeIndex < 0) {
                        if (colorChar == 'r') {
                            currentColor = color.getRGB();
                        }
                    } else {
                        currentColor = COLOR_CODES[codeIndex];
                    }

                    index += 2;
                }
            }
        }

        return width;
    }

    public static void init() {
        renderer = new Renderer2DQuad(false);
    }

    public static void drawRoundedQuad(
        double x, double y, double width, double height, double radius, meteordevelopment.meteorclient.utils.render.color.Color color
    ) {
        renderer.quadRounded(x, y, width, height, color, radius, true);
    }

    public static void drawRoundedQuad(
        double x, double y, double width, double height, double radius, meteordevelopment.meteorclient.utils.render.color.Color color, boolean roundTop
    ) {
        renderer.quadRounded(x, y, width, height, color, radius, roundTop);
    }

    public static void drawQuad(double x, double y, double width, double height, meteordevelopment.meteorclient.utils.render.color.Color color) {
        renderer.quad(x, y, width, height, color);
    }

    public static double render(String text, double x, double y, Color color) {
        return render(text, x, y, color, false);
    }

    public static double getWidth(String text) {
        return TextRenderer.get().getWidth(text.replaceAll(COLOR_CODE_PATTERN.pattern(), ""));
    }
}
