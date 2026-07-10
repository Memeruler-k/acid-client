package dev.abstr3act.addon.utils;

import dev.abstr3act.addon.utils.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import net.minecraft.util.math.MathHelper;

public class Render2DUtils {
    public static Color injectAlpha(SettingColor color, int alpha) {
        return new Color(color.r, color.g, color.b, MathHelper.clamp(alpha, 0, 255));
    }

    public static Color injectAlpha(meteordevelopment.meteorclient.utils.render.color.Color color, int alpha) {
        return new Color(color.r, color.g, color.b, MathHelper.clamp(alpha, 0, 255));
    }

    public static Color injectAlpha(java.awt.Color color, int alpha) {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), MathHelper.clamp(alpha, 0, 255));
    }
}
