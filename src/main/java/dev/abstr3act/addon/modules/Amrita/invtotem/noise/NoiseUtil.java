package dev.abstr3act.addon.modules.Amrita.invtotem.noise;

public class NoiseUtil {
    private static final int[] PERM = new int[512];
    private static final int[] PERM_MOD12 = new int[512];
    private static final double SQRT_3 = Math.sqrt(3.0);
    private static final double F2 = 0.5 * (SQRT_3 - 1.0);
    private static final double G2 = (3.0 - SQRT_3) / 6.0;

    public NoiseUtil(long seed) {
        int[] p = new int[256];
        int i = 0;

        while (i < 256) {
            p[i] = i++;
        }

        for (int ix = 255; ix > 0; ix--) {
            int j = (int) ((seed & 4294967295L) % (ix + 1));
            seed = seed * 6364136223846793005L + 1442695040888963407L;
            int swap = p[ix];
            p[ix] = p[j];
            p[j] = swap;
        }

        for (int ix = 0; ix < 512; ix++) {
            PERM[ix] = p[ix & 0xFF];
            PERM_MOD12[ix] = PERM[ix] % 12;
        }
    }

    private static int fastFloor(double x) {
        int xi = (int) x;
        return x < xi ? xi - 1 : xi;
    }

    private static double grad(int hash, double x, double y) {
        int h = hash & 7;
        double u = h < 4 ? x : y;
        double v = h < 4 ? y : x;
        return ((h & 1) == 0 ? u : -u) + ((h & 2) == 0 ? 2.0 * v : -2.0 * v);
    }

    public double eval(double x, double y) {
        double s = (x + y) * F2;
        int i = fastFloor(x + s);
        int j = fastFloor(y + s);
        double t = (i + j) * G2;
        double X0 = i - t;
        double Y0 = j - t;
        double x0 = x - X0;
        double y0 = y - Y0;
        int i1;
        int j1;
        if (x0 > y0) {
            i1 = 1;
            j1 = 0;
        } else {
            i1 = 0;
            j1 = 1;
        }

        double x1 = x0 - i1 + G2;
        double y1 = y0 - j1 + G2;
        double x2 = x0 - 1.0 + 2.0 * G2;
        double y2 = y0 - 1.0 + 2.0 * G2;
        int ii = i & 0xFF;
        int jj = j & 0xFF;
        int gi0 = PERM_MOD12[ii + PERM[jj]];
        int gi1 = PERM_MOD12[ii + i1 + PERM[jj + j1]];
        int gi2 = PERM_MOD12[ii + 1 + PERM[jj + 1]];
        double n0 = grad(gi0, x0, y0);
        double n1 = grad(gi1, x1, y1);
        double n2 = grad(gi2, x2, y2);
        return 70.0 * (n0 + n1 + n2);
    }
}
