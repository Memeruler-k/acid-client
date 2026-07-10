package dev.abstr3act.addon.utils.render;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.function.Function;

public class Blur {
    public static BufferedImage ProcessImage(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        int[] pixels = image.getRGB(0, 0, width, height, null, 0, width);
        int[] changedPixels = new int[pixels.length];
        FastGaussianBlur(pixels, changedPixels, width, height, 7);
        BufferedImage newImage = new BufferedImage(width, height, image.getType());
        newImage.setRGB(0, 0, width, height, changedPixels, 0, width);
        return newImage;
    }

    private static void FastGaussianBlur(int[] source, int[] output, int width, int height, int radius) {
        ArrayList<Integer> gaussianBoxes = CreateGausianBoxes(radius, 3);
        BoxBlur(source, output, width, height, (gaussianBoxes.get(0) - 1) / 2);
        BoxBlur(output, source, width, height, (gaussianBoxes.get(1) - 1) / 2);
        BoxBlur(source, output, width, height, (gaussianBoxes.get(2) - 1) / 2);
    }

    private static ArrayList<Integer> CreateGausianBoxes(double sigma, int n) {
        double idealFilterWidth = Math.sqrt(12.0 * sigma * sigma / n + 1.0);
        int filterWidth = (int) Math.floor(idealFilterWidth);
        if (filterWidth % 2 == 0) {
            filterWidth--;
        }

        int filterWidthU = filterWidth + 2;
        double mIdeal = (12.0 * sigma * sigma - n * filterWidth * filterWidth - 4 * n * filterWidth - 3 * n) / (-4 * filterWidth - 4);
        double m = Math.round(mIdeal);
        ArrayList<Integer> result = new ArrayList<>();

        for (int i = 0; i < n; i++) {
            result.add(i < m ? filterWidth : filterWidthU);
        }

        return result;
    }

    private static void BoxBlur(int[] source, int[] output, int width, int height, int radius) {
        System.arraycopy(source, 0, output, 0, source.length);
        BoxBlurHorizontal(output, source, width, height, radius);
        BoxBlurVertical(source, output, width, height, radius);
    }

    private static void BoxBlurHorizontal(int[] sourcePixels, int[] outputPixels, int width, int height, int radius) {
        int[] resultingColorPixel = new int[3];
        float iarr = 1.0F / (radius + radius);
        float[] val = new float[3];
        int[] fv = new int[3];
        int[] lv = new int[3];
        int[] tmp = new int[3];

        for (int i = 0; i < height; i++) {
            int outputIndex = i * width;
            int li = outputIndex;
            int sourceIndex = outputIndex + radius;
            asBytes(sourcePixels[outputIndex], fv);
            asBytes(sourcePixels[outputIndex + width - 1], lv);
            apply(fv, val, ifv -> (float) (radius * ifv));

            for (int j = 0; j < radius; j++) {
                componentAdd(val, asBytes(sourcePixels[outputIndex + j], tmp));
            }

            for (int j = 0; j < radius; j++) {
                componentAdd(val, asBytes(sourcePixels[sourceIndex++], tmp));
                componentSub(val, fv);
                componentMulAndRoundTo(val, iarr, resultingColorPixel);
                outputPixels[outputIndex++] = asInteger(resultingColorPixel);
            }

            for (int j = radius + 1; j < width - radius; j++) {
                componentAdd(val, asBytes(sourcePixels[sourceIndex++], tmp));
                componentSub(val, asBytes(sourcePixels[li++], tmp));
                componentMulAndRoundTo(val, iarr, resultingColorPixel);
                outputPixels[outputIndex++] = asInteger(resultingColorPixel);
            }

            for (int j = width - radius; j < width; j++) {
                componentAdd(val, lv);
                componentSub(val, asBytes(sourcePixels[li++], tmp));
                componentMulAndRoundTo(val, iarr, resultingColorPixel);
                outputPixels[outputIndex++] = asInteger(resultingColorPixel);
            }
        }
    }

    private static void BoxBlurVertical(int[] sourcePixels, int[] outputPixels, int width, int height, int radius) {
        int[] resultingColorPixel = new int[3];
        float iarr = 1.0F / (radius + radius + 1);
        float[] val = new float[3];
        int[] fv = new int[3];
        int[] lv = new int[3];
        int[] tmp = new int[3];

        for (int i = 0; i < width; i++) {
            int outputIndex = i;
            int li = i;
            int sourceIndex = i + radius * width;
            asBytes(sourcePixels[i], fv);
            asBytes(sourcePixels[i + width * (height - 1)], lv);
            apply(fv, val, ifv -> (float) (radius + 1) * ifv.intValue());

            for (int j = 0; j < radius; j++) {
                componentAdd(val, asBytes(sourcePixels[outputIndex + j * width], tmp));
            }

            for (int j = 0; j <= radius; j++) {
                componentAdd(val, asBytes(sourcePixels[sourceIndex], tmp));
                componentSub(val, fv);
                componentMulAndRoundTo(val, iarr, resultingColorPixel);
                outputPixels[outputIndex] = asInteger(resultingColorPixel);
                sourceIndex += width;
                outputIndex += width;
            }

            for (int j = radius + 1; j < height - radius; j++) {
                componentAdd(val, asBytes(sourcePixels[sourceIndex], tmp));
                componentSub(val, asBytes(sourcePixels[li], tmp));
                componentMulAndRoundTo(val, iarr, resultingColorPixel);
                outputPixels[outputIndex] = asInteger(resultingColorPixel);
                li += width;
                sourceIndex += width;
                outputIndex += width;
            }

            for (int j = height - radius; j < height; j++) {
                componentAdd(val, lv);
                componentSub(val, asBytes(sourcePixels[li], tmp));
                componentMulAndRoundTo(val, iarr, resultingColorPixel);
                outputPixels[outputIndex] = asInteger(resultingColorPixel);
                li += width;
                outputIndex += width;
            }
        }
    }

    private static int[] asBytes(int i, int[] result) {
        result[0] = i >> 16 & 0xFF;
        result[1] = i >> 8 & 0xFF;
        result[2] = i & 0xFF;
        return result;
    }

    private static void apply(int[] input, float[] result, Function<Integer, Float> f) {
        for (int i = 0; i < input.length; i++) {
            result[i] = f.apply(input[i]);
        }
    }

    private static void componentAdd(float[] input, int[] add) {
        for (int i = 0; i < input.length; i++) {
            input[i] += add[i];
        }
    }

    private static void componentSub(float[] input, int[] sub) {
        for (int i = 0; i < input.length; i++) {
            input[i] -= sub[i];
        }
    }

    private static void componentMulAndRoundTo(float[] input, float mul, int[] result) {
        for (int i = 0; i < input.length; i++) {
            result[i] = Math.round(input[i] * mul);
        }
    }

    private static int asInteger(int[] array) {
        return 0xFF000000 | array[0] << 16 | array[1] << 8 | array[2];
    }
}
