package dev.abstr3act.addon.utils.abnormally;

import net.minecraft.network.packet.Packet;

import java.lang.reflect.Field;

public class ReflectionUtils {
    public static String getSignature(Object obj) {
        StringBuilder stringBuilder = new StringBuilder();
        if (obj instanceof Packet<?> packet) {
            stringBuilder.append(packet).append(" Packet");
        } else {
            stringBuilder.append(obj.getClass().getName()).append("{");
            Field[] fields = obj.getClass().getDeclaredFields();

            for (Field field : fields) {
                field.setAccessible(true);

                try {
                    stringBuilder.append(field.getName()).append("=").append(field.get(obj).toString()).append(", ");
                } catch (IllegalAccessException var9) {
                    var9.printStackTrace();
                    return "<Error when parsing>";
                }
            }

            stringBuilder.append("}");
        }

        return stringBuilder.toString();
    }
}
