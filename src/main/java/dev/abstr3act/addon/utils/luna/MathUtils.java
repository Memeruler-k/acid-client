package dev.abstr3act.addon.utils.luna;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;

public class MathUtils {
    public static float getTargetToPlayerPitch(PlayerEntity player, Entity target) {
        Vec3d playerPos = player.getPos();
        Vec3d targetPos = target.getPos();
        double deltaX = targetPos.x - playerPos.x;
        double deltaY = targetPos.y + target.getHeight() / 2.0F - (playerPos.y + player.getEyeHeight(player.getPose()));
        double deltaZ = targetPos.z - playerPos.z;
        double distanceXZ = MathHelper.sqrt((float) (deltaX * deltaX + deltaZ * deltaZ));
        float pitch = (float) (-(MathHelper.atan2(deltaY, distanceXZ) * (180.0 / Math.PI)));
        return MathHelper.wrapDegrees(pitch);
    }

    public static float getTargetToPlayerYaw(PlayerEntity player, Entity target) {
        Vec3d playerPos = player.getPos();
        Vec3d targetPos = target.getPos();
        double deltaX = targetPos.x - playerPos.x;
        double deltaZ = targetPos.z - playerPos.z;
        float yaw = (float) (MathHelper.atan2(deltaZ, deltaX) * (180.0 / Math.PI)) - 90.0F;
        return MathHelper.wrapDegrees(yaw);
    }

    public static Direction getFacingOrder(float yaw, float pitch) {
        float f = pitch * (float) (Math.PI / 180.0);
        float g = -yaw * (float) (Math.PI / 180.0);
        float h = MathHelper.sin(f);
        float i = MathHelper.cos(f);
        float j = MathHelper.sin(g);
        float k = MathHelper.cos(g);
        boolean bl = j > 0.0F;
        boolean bl2 = h < 0.0F;
        boolean bl3 = k > 0.0F;
        float l = bl ? j : -j;
        float m = bl2 ? -h : h;
        float n = bl3 ? k : -k;
        float o = l * i;
        float p = n * i;
        Direction direction = bl ? Direction.EAST : Direction.WEST;
        Direction direction2 = bl2 ? Direction.UP : Direction.DOWN;
        Direction direction3 = bl3 ? Direction.SOUTH : Direction.NORTH;
        if (l > n) {
            return m > o ? direction2 : direction;
        } else {
            return m > p ? direction2 : direction3;
        }
    }

    public static float clamp(float num, float min, float max) {
        return num < min ? min : Math.min(num, max);
    }

    public static double clamp(double value, double min, double max) {
        return value < min ? min : Math.min(value, max);
    }

    public static double square(double input) {
        return input * input;
    }

    public static float random(float min, float max) {
        return (float) (Math.random() * (max - min) + min);
    }

    public static double random(double min, double max) {
        return (float) (Math.random() * (max - min) + min);
    }

    public static float rad(float angle) {
        return (float) (angle * Math.PI / 180.0);
    }

    public static double interpolate(double previous, double current, float delta) {
        return previous + (current - previous) * delta;
    }

    public static float round(float value, int places) {
        if (places < 0) {
            throw new IllegalArgumentException();
        } else {
            BigDecimal bd = BigDecimal.valueOf(value);
            bd = bd.setScale(places, RoundingMode.FLOOR);
            return bd.floatValue();
        }
    }

    public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map, boolean descending) {
        LinkedList<Entry<K, V>> list = new LinkedList<>(map.entrySet());
        if (descending) {
            list.sort(Entry.comparingByValue(Comparator.reverseOrder()));
        } else {
            list.sort(Entry.comparingByValue());
        }

        LinkedHashMap result = new LinkedHashMap();

        for (Entry entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }

        return result;
    }
}
