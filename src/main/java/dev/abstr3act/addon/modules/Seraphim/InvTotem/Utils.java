package dev.abstr3act.addon.modules.Seraphim.InvTotem;

import meteordevelopment.meteorclient.MeteorClient;

import java.util.List;
import java.util.Random;

public class Utils {
    private static final int[] P = new int[512];
    private static final int[] perm = new int[256];
    private static final Random random;

    static {
        int i = 0;

        while (i < 256) {
            perm[i] = i++;
        }

        for (int ix = 0; ix < 256; ix++) {
            int j = (int) (Math.random() * 256.0);
            int temp = perm[ix];
            perm[ix] = perm[j];
            perm[j] = temp;
        }

        System.arraycopy(perm, 0, P, 0, 256);
        System.arraycopy(perm, 0, P, 256, 256);
        random = new Random((long) (System.currentTimeMillis() * noise(MeteorClient.mc.player.getX(), MeteorClient.mc.player.getY())));
    }

    private static double grad(int hash, double x, double y) {
        int h = hash & 15;
        double u = h < 8 ? x : y;
        double v = h < 4 ? y : (h != 12 && h != 14 ? 0.0 : x);
        return ((h & 1) == 0 ? u : -u) + ((h & 2) == 0 ? v : -v);
    }

    private static double lerp(double t, double a, double b) {
        return a + t * (b - a);
    }

    private static double fade(double t) {
        return t * t * t * (t * (t * 6.0 - 15.0) + 10.0);
    }

    private static double noise(double x, double y) {
        int X = (int) Math.floor(x) & 0xFF;
        int Y = (int) Math.floor(y) & 0xFF;
        double xf = x - Math.floor(x);
        double yf = y - Math.floor(y);
        double u = fade(xf);
        double v = fade(yf);
        int a = P[X] + Y;
        int aa = P[a];
        int ab = P[a + 1];
        int b = P[X + 1] + Y;
        int ba = P[b];
        int bb = P[b + 1];
        double gradAA = grad(P[aa], xf, yf);
        double gradBA = grad(P[ba], xf - 1.0, yf);
        double gradAB = grad(P[ab], xf, yf - 1.0);
        double gradBB = grad(P[bb], xf - 1.0, yf - 1.0);
        double x1 = lerp(u, gradAA, gradBA);
        double x2 = lerp(u, gradAB, gradBB);
        return lerp(v, x1, x2);
    }

    public static int[] getVar(List<Integer> list) {
        if (list.isEmpty()) {
            return new int[]{-1, -1};
        } else if (list.size() == 1) {
            return new int[]{list.get(0), -1};
        } else {
            int index1 = random.nextInt(list.size());
            int retries = 0;

            int index2;
            do {
                index2 = random.nextInt(list.size());
            } while (index1 == index2 && ++retries < 10);

            if (index1 == index2) {
                index2 = (index1 + 1) % list.size();
            }

            return new int[]{list.get(index1), list.get(index2)};
        }
    }
}
