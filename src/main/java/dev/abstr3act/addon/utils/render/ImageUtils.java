package dev.abstr3act.addon.utils.render;

import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;

public class ImageUtils {
    public static BufferedImage applyGaussianBlur(BufferedImage image) {
        int kernelSize = 5;
        float[] gaussianKernel = new float[]{
            0.0036630037F,
            0.014652015F,
            0.025641026F,
            0.014652015F,
            0.0036630037F,
            0.014652015F,
            0.05860806F,
            0.0952381F,
            0.05860806F,
            0.014652015F,
            0.025641026F,
            0.0952381F,
            0.15018316F,
            0.0952381F,
            0.025641026F,
            0.014652015F,
            0.05860806F,
            0.0952381F,
            0.05860806F,
            0.014652015F,
            0.0036630037F,
            0.014652015F,
            0.025641026F,
            0.014652015F,
            0.0036630037F
        };
        Kernel kernel = new Kernel(kernelSize, kernelSize, gaussianKernel);
        ConvolveOp convolveOp = new ConvolveOp(kernel, 1, null);
        return convolveOp.filter(image, null);
    }
}
