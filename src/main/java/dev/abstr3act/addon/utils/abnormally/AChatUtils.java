package dev.abstr3act.addon.utils.abnormally;

import dev.abstr3act.addon.Compassion;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.mixininterface.IChatHud;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.utils.render.color.Color;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;
import java.util.Objects;

public class AChatUtils {
    public static List<int[]> getColor(Category mode) {
        if (mode == Compassion.ABNORMALLY) {
            int[] startColor = new int[]{167, 189, 255};
            int[] endColor = new int[]{255, 167, 167};
            return List.of(startColor, endColor);
        } else if (mode == Compassion.LUNA) {
            int[] startColor = new int[]{155, 89, 182};
            int[] endColor = new int[]{235, 222, 240};
            return List.of(startColor, endColor);
        } else if (mode == Compassion.SELENA) {
            int[] startColor = new int[]{228, 90, 90};
            int[] endColor = new int[]{228, 213, 90};
            return List.of(startColor, endColor);
        } else if (mode == Compassion.COMPASSION) {
            int[] startColor = new int[]{148, 185, 255};
            int[] endColor = new int[]{255, 187, 237};
            return List.of(startColor, endColor);
        } else {
            return List.of(new int[]{0, 0, 0}, new int[]{0, 0, 0});
        }
    }

    public static int[] getColor(Category mode, int index) {
        if (mode == Compassion.ABNORMALLY) {
            int[] startColor = new int[]{167, 189, 255};
            int[] endColor = new int[]{255, 167, 167};
            List<int[]> l = List.of(startColor, endColor);
            return l.get(index);
        } else if (mode == Compassion.LUNA) {
            int[] startColor = new int[]{155, 89, 182};
            int[] endColor = new int[]{235, 222, 240};
            List<int[]> l = List.of(startColor, endColor);
            return l.get(index);
        } else if (mode == Compassion.SELENA) {
            int[] startColor = new int[]{228, 90, 90};
            int[] endColor = new int[]{228, 213, 90};
            List<int[]> l = List.of(startColor, endColor);
            return l.get(index);
        } else if (mode == Compassion.COMPASSION) {
            int[] startColor = new int[]{255, 120, 140};
            int[] endColor = new int[]{255, 255, 255};
            List<int[]> l = List.of(startColor, endColor);
            return l.get(index);
        } else if (mode == Compassion.SERAPHIM) {
            int[] startColor = new int[]{141, 154, 219};
            int[] endColor = new int[]{255, 255, 255};
            List<int[]> l = List.of(startColor, endColor);
            return l.get(index);
        } else if (mode == Compassion.LACRYMIRA) {
            int[] startColor = new int[]{126, 144, 201};
            int[] endColor = new int[]{191, 195, 213};
            List<int[]> l = List.of(startColor, endColor);
            return l.get(index);
        } else if (mode == Compassion.AMRITA) {
            int[] startColor = new int[]{255, 168, 168};
            int[] endColor = new int[]{251, 168, 255};
            List<int[]> l = List.of(startColor, endColor);
            return l.get(index);
        } else {
            List<int[]> l = List.of(new int[]{0, 0, 0}, new int[]{0, 0, 0});
            return l.get(index);
        }
    }

    public static Text getPrefix() {
        String text = "Abnormally";
        int[] startColor = new int[]{167, 189, 255};
        int[] endColor = new int[]{255, 167, 167};
        MutableText prefix = Text.empty().append("[");

        for (int i = 0; i < text.length(); i++) {
            float ratio = (float) i / (text.length() - 1);
            int red = (int) (startColor[0] + ratio * (endColor[0] - startColor[0]));
            int green = (int) (startColor[1] + ratio * (endColor[1] - startColor[1]));
            int blue = (int) (startColor[2] + ratio * (endColor[2] - startColor[2]));
            prefix = prefix.append(Text.literal(String.valueOf(text.charAt(i))).setStyle(Style.EMPTY.withColor(Color.fromRGBA(red, green, blue, 1))));
        }

        return prefix.append("] ");
    }

    public static Text getPrefixAcid() {
        String text = "AcidClient";
        int[] startColor = new int[]{133, 180, 255};
        int[] endColor = new int[]{255, 255, 255};
        MutableText prefix = Text.empty().append("[");

        for (int i = 0; i < text.length(); i++) {
            float ratio = (float) i / (text.length() - 1);
            int red = (int) (startColor[0] + ratio * (endColor[0] - startColor[0]));
            int green = (int) (startColor[1] + ratio * (endColor[1] - startColor[1]));
            int blue = (int) (startColor[2] + ratio * (endColor[2] - startColor[2]));
            prefix = prefix.append(Text.literal(String.valueOf(text.charAt(i))).setStyle(Style.EMPTY.withColor(Color.fromRGBA(red, green, blue, 1))));
        }

        return prefix.append("] ");
    }

    public static Text getPrefixAmritaAC() {
        String text = "AmritaAC";
        int[] startColor = new int[]{255, 168, 168};
        int[] endColor = new int[]{251, 168, 255};
        MutableText prefix = Text.empty().append("[");

        for (int i = 0; i < text.length(); i++) {
            float ratio = (float) i / (text.length() - 1);
            int red = (int) (startColor[0] + ratio * (endColor[0] - startColor[0]));
            int green = (int) (startColor[1] + ratio * (endColor[1] - startColor[1]));
            int blue = (int) (startColor[2] + ratio * (endColor[2] - startColor[2]));
            prefix = prefix.append(Text.literal(String.valueOf(text.charAt(i))).setStyle(Style.EMPTY.withColor(Color.fromRGBA(red, green, blue, 1))));
        }

        return prefix.append("] ");
    }

    public static Text getPrefix(String text, int[] startColor, int[] endColor) {
        MutableText prefix = Text.empty().append("[");

        for (int i = 0; i < text.length(); i++) {
            float ratio = (float) i / (text.length() - 1);
            int red = (int) (startColor[0] + ratio * (endColor[0] - startColor[0]));
            int green = (int) (startColor[1] + ratio * (endColor[1] - startColor[1]));
            int blue = (int) (startColor[2] + ratio * (endColor[2] - startColor[2]));
            prefix = prefix.append(Text.literal(String.valueOf(text.charAt(i))).setStyle(Style.EMPTY.withColor(Color.fromRGBA(red, green, blue, 1))));
        }

        return prefix.append("] ");
    }

    public static Text getPrefixDebugger(String prefixValue) {
        int[] startColor = new int[]{176, 196, 222};
        int[] endColor = new int[]{135, 206, 250};
        MutableText prefix = Text.empty().append("[");

        for (int i = 0; i < prefixValue.length(); i++) {
            float ratio = (float) i / (prefixValue.length() - 1);
            int red = (int) (startColor[0] + ratio * (endColor[0] - startColor[0]));
            int green = (int) (startColor[1] + ratio * (endColor[1] - startColor[1]));
            int blue = (int) (startColor[2] + ratio * (endColor[2] - startColor[2]));
            prefix = prefix.append(Text.literal(String.valueOf(prefixValue.charAt(i))).setStyle(Style.EMPTY.withColor(Color.fromRGBA(red, green, blue, 1))));
        }

        return prefix.append("] ");
    }

    public static void sendMsg(Text msg, String text, int[] startColor, int[] endColor) {
        if (MeteorClient.mc.world != null) {
            MutableText message = Text.empty();
            message.setStyle(Style.EMPTY.withFormatting(Formatting.GRAY));
            message.append(getPrefix(text, startColor, endColor));
            message.append(msg);
            ((IChatHud) MeteorClient.mc.inGameHud.getChatHud()).meteor$add(message, 0);
        }
    }

    public static void sendMsg(Text msg, Text prefix) {
        if (MeteorClient.mc.world != null) {
            MutableText message = Text.empty();
            message.setStyle(Style.EMPTY.withFormatting(Formatting.GRAY));
            message.append(prefix);
            message.append(msg);
            ((IChatHud) MeteorClient.mc.inGameHud.getChatHud()).meteor$add(message, 0);
        }
    }

    public static void sendMsgLuna(Text msg) {
        if (MeteorClient.mc.world != null) {
            MutableText message = Text.empty();
            message.setStyle(Style.EMPTY.withFormatting(Formatting.GRAY));
            message.append(getPrefix("Luna", getColor(Compassion.LUNA, 0), getColor(Compassion.LUNA, 1)));
            message.append(msg);
            ((IChatHud) MeteorClient.mc.inGameHud.getChatHud()).meteor$add(message, 0);
        }
    }

    public static void sendMsgLuna(String msg) {
        if (MeteorClient.mc.world != null) {
            MutableText message = Text.empty();
            message.setStyle(Style.EMPTY.withFormatting(Formatting.GRAY));
            message.append(getPrefix("Luna", getColor(Compassion.LUNA, 0), getColor(Compassion.LUNA, 1)));
            message.append(msg);
            ((IChatHud) MeteorClient.mc.inGameHud.getChatHud()).meteor$add(message, 0);
        }
    }

    public static void sendMsgSelena(Text msg) {
        if (MeteorClient.mc.world != null) {
            MutableText message = Text.empty();
            message.setStyle(Style.EMPTY.withFormatting(Formatting.GRAY));
            message.append(getPrefix("Selena", getColor(Compassion.SELENA, 0), getColor(Compassion.SELENA, 1)));
            message.append(msg);
            ((IChatHud) MeteorClient.mc.inGameHud.getChatHud()).meteor$add(message, 0);
        }
    }

    public static void sendMsgCompassion(Text msg) {
        if (MeteorClient.mc.world != null) {
            MutableText message = Text.empty();
            message.setStyle(Style.EMPTY.withFormatting(Formatting.GRAY));
            message.append(getPrefix("Compassion", getColor(Compassion.COMPASSION, 0), getColor(Compassion.COMPASSION, 1)));
            message.append(msg);
            ((IChatHud) MeteorClient.mc.inGameHud.getChatHud()).meteor$add(message, 0);
        }
    }

    public static void sendMsgSeraphim(Text msg) {
        if (MeteorClient.mc.world != null) {
            MutableText message = Text.empty();
            message.setStyle(Style.EMPTY.withFormatting(Formatting.GRAY));
            message.append(getPrefix("Seraphim", getColor(Compassion.SERAPHIM, 0), getColor(Compassion.SERAPHIM, 1)));
            message.append(msg);
            ((IChatHud) MeteorClient.mc.inGameHud.getChatHud()).meteor$add(message, 0);
        }
    }

    public static void sendMsgAmrita(Text msg) {
        if (MeteorClient.mc.world != null) {
            MutableText message = Text.empty();
            message.setStyle(Style.EMPTY.withFormatting(Formatting.GRAY));
            message.append(getPrefix("Amrita", getColor(Compassion.AMRITA, 0), getColor(Compassion.AMRITA, 1)));
            message.append(msg);
            ((IChatHud) MeteorClient.mc.inGameHud.getChatHud()).meteor$add(message, 0);
        }
    }

    public static void sendMsgAmritaAC(Text msg) {
        if (MeteorClient.mc.world != null) {
            MutableText message = Text.empty();
            message.setStyle(Style.EMPTY.withFormatting(Formatting.WHITE));
            message.append(getPrefix("AmritaAC", getColor(Compassion.AMRITA, 0), getColor(Compassion.AMRITA, 1)));
            message.append(msg);
            ((IChatHud) MeteorClient.mc.inGameHud.getChatHud()).meteor$add(message, 0);
        }
    }

    public static void sendMsgLacrymira(Text msg) {
        if (MeteorClient.mc.world != null) {
            MutableText message = Text.empty();
            message.setStyle(Style.EMPTY.withFormatting(Formatting.GRAY));
            message.append(getPrefix("Lacrymira", getColor(Compassion.LACRYMIRA, 0), getColor(Compassion.LACRYMIRA, 1)));
            message.append(msg);
            ((IChatHud) MeteorClient.mc.inGameHud.getChatHud()).meteor$add(message, 0);
        }
    }

    public static void sendDebugMsg(Text msg) {
        if (MeteorClient.mc.world != null) {
            MutableText message = Text.empty();
            message.setStyle(Style.EMPTY.withFormatting(Formatting.GRAY));
            message.append(getPrefixDebugger("Debugger"));
            message.append(msg);
            ((IChatHud) MeteorClient.mc.inGameHud.getChatHud()).meteor$add(message, 0);
        }
    }

    public static void sendDebugMsg(String msg) {
        if (MeteorClient.mc.world != null) {
            MutableText message = Text.empty();
            message.setStyle(Style.EMPTY.withFormatting(Formatting.GRAY));
            message.append(getPrefixDebugger("Debugger"));
            message.append(msg);
            ((IChatHud) MeteorClient.mc.inGameHud.getChatHud()).meteor$add(message, 0);
        }
    }

    public static void sendMsg(String msg) {
        if (MeteorClient.mc.world != null) {
            MutableText message = Text.empty();
            message.setStyle(Style.EMPTY.withFormatting(Formatting.GRAY));
            message.append(getPrefix());
            message.append(msg);
            ((IChatHud) MeteorClient.mc.inGameHud.getChatHud()).meteor$add(message, 0);
        }
    }

    public static void sendMsg(Objects msg) {
        if (MeteorClient.mc.world != null) {
            MutableText message = Text.empty();
            message.setStyle(Style.EMPTY.withFormatting(Formatting.GRAY));
            message.append(getPrefix());
            message.append(String.valueOf(msg));
            ((IChatHud) MeteorClient.mc.inGameHud.getChatHud()).meteor$add(message, 0);
        }
    }

    public static void sendMsg(Text msg) {
        if (MeteorClient.mc.world != null) {
            MutableText message = Text.empty();
            message.setStyle(Style.EMPTY.withFormatting(Formatting.GRAY));
            message.append(getPrefix());
            message.append(msg);
            ((IChatHud) MeteorClient.mc.inGameHud.getChatHud()).meteor$add(message, 0);
        }
    }
}
