package dev.abstr3act.addon.utils;

import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;

import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;

public class TextColorParser {
    public static Map<String, Color> parseText(Text text) {
        Map<String, Color> result = new LinkedHashMap<>();
        parseRecursive(text, result);
        return result;
    }

    private static void parseRecursive(Text text, Map<String, Color> result) {
        Style style = text.getStyle();
        TextColor textColor = style.getColor();
        Color color = textColor != null ? new Color(textColor.getRgb()) : Color.WHITE;
        result.put(text.getString(), color);

        for (Text sibling : text.getSiblings()) {
            parseRecursive(sibling, result);
        }
    }
}
