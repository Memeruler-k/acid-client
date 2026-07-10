package dev.abstr3act.addon.utils.luna;

import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public class StringUtils {
    public static boolean hasMatchingSubstring(String str, ArrayList<String> substrings) {
        for (String s : substrings) {
            if (str.contains(s)) {
                return true;
            }
        }

        return false;
    }

    public static boolean hasMatchingSubstring(String str, List<String> substrings) {
        for (String s : substrings) {
            if (str.contains(s)) {
                return true;
            }
        }

        return false;
    }

    public static String getPlayerName(Text message) {
        String[] parts = message.getString().split("> ", 2);
        return parts.length < 2 ? "" : parts[0].substring(parts[0].indexOf(60) + 1);
    }

    public static String getPlayerName(String message) {
        String[] parts = message.split("> ", 2);
        return parts.length < 2 ? "" : parts[0].substring(parts[0].indexOf(60) + 1);
    }

    public static String getMessageContent(Text message) {
        String[] parts = message.getString().split("> ", 2);
        return parts.length < 2 ? "" : parts[1];
    }

    public static String getMessageContent(String message) {
        String[] parts = message.split("> ", 2);
        return parts.length < 2 ? "" : parts[1];
    }

    public static String[] getParts(String message) {
        return message.split("> ", 2);
    }

    public static String[] getParts(Text message) {
        return message.getString().split("> ", 2);
    }
}
