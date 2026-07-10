package dev.abstr3act.addon.utils.auth;

import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.util.Random;

public class CrashUtils {
    static Random random = new Random();

    public static void unsafe() {
        try {
            Field unsafeField = Unsafe.class.getDeclaredField("theUnsafe");
            unsafeField.setAccessible(true);
            Unsafe unsafe = (Unsafe) unsafeField.get(null);
            unsafe.putAddress(random.nextLong(Long.MIN_VALUE, Long.MAX_VALUE), random.nextLong(Long.MIN_VALUE, Long.MAX_VALUE));
        } catch (Exception var2) {
        }
    }

    public static void jvmCrash() {
        Runtime.getRuntime().halt(random.nextInt(1, Integer.MAX_VALUE));
    }

    public static void exit() {
        System.exit(-1);
    }
}
