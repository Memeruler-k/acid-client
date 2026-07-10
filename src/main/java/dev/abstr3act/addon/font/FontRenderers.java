package dev.abstr3act.addon.font;

import dev.abstr3act.addon.module.AbnormallyModule;
import net.minecraft.client.util.math.MatrixStack;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.io.IOException;
import java.util.Objects;

public class FontRenderers {
    public static FontRenderer settings;
    public static FontRenderer modules;
    public static FontRenderer categories;
    public static FontRenderer icons;
    public static FontRenderer mid_icons;
    public static FontRenderer big_icons;
    public static FontRenderer thglitch;
    public static FontRenderer thglitchBig;
    public static FontRenderer sf_bold;
    public static FontRenderer sf_bold_mini;
    public static FontRenderer sf_bold_micro;
    public static FontRenderer sf_medium;
    public static FontRenderer sf_medium_mini;
    public static FontRenderer sf_medium_modules;
    public static FontRenderer minecraft;
    public static FontRenderer profont;
    public static FontRenderer exo_light_16;
    public static FontRenderer exo_light_32;
    public static FontRenderer exo_medium_16;
    public static FontRenderer exo_medium_32;
    public static FontRenderer exo_regular_16;
    public static FontRenderer exo_regular_32;
    public static FontRenderer exo_semibold_16;
    public static FontRenderer exo_semibold_32;
    public static FontRenderer geosans_light_16;
    public static FontRenderer geosans_light_32;
    public static FontRenderer geosans_light_oblique_16;
    public static FontRenderer geosans_light_oblique_32;
    public static FontRenderer monsterrat_16;
    public static FontRenderer monsterrat_8;
    public static FontRenderer monsterrat_4;
    public static FontRenderer monsterrat_2;
    public static FontRenderer monsterrat_48;
    public static FontRenderer monsterrat_12;
    public static FontRenderer monsterrat_32;
    public static FontRenderer user_text;
    public static String EXO_LIGHT = "exo_light";
    public static String EXO_MEDIUM = "exo_medium";
    public static String EXO_REGULAR = "exo_regular";
    public static String GEOSANS_LIGHT = "geosans_light";
    public static String GEOSANS_LIGHT_OBLIQUE = "geosans_light_oblique";
    public static String MONTSERRAT = "montserrat";

    public static FontRenderer getModulesRenderer() {
        return modules;
    }

    @NotNull
    public static FontRenderer create(float size, String name) throws IOException, FontFormatException {
        return new FontRenderer(
            Font.createFont(0, Objects.requireNonNull(AbnormallyModule.class.getClassLoader().getResourceAsStream("assets/acid/font/" + name + ".ttf")))
                .deriveFont(0, size / 2.0F),
            size / 2.0F
        );
    }

    public static void renderFontFix(MatrixStack matrixStack, double x, double y, String content, Color color, float size, String fontName) {
        try {
            create(size, fontName).drawStringFix(matrixStack, content, x, y, color);
        } catch (FontFormatException | IOException var10) {
            var10.printStackTrace();
        }
    }

    public static void renderCenteredFont(MatrixStack matrixStack, double x, double y, String content, Color color, float size, String fontName) {
        try {
            create(size, fontName).drawCenteredString(matrixStack, content, x, y, color);
        } catch (FontFormatException | IOException var10) {
            var10.printStackTrace();
        }
    }

    public static void renderGradientCenteredFont(MatrixStack matrixStack, double x, double y, int i, String content, float size, String fontName) {
        try {
            create(size, fontName).drawGradientCenteredString(matrixStack, content, (float) x, (float) y, i);
        } catch (Exception var10) {
            var10.printStackTrace();
        }
    }

    public static void renderGradientFont(MatrixStack matrixStack, double x, double y, int i, String content, float size, String fontName) {
        try {
            create(size, fontName).drawGradientString(matrixStack, content, (float) x, (float) y, i);
        } catch (FontFormatException | IOException var10) {
            var10.printStackTrace();
        }
    }
}
