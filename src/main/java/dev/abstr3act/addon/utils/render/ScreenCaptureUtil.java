package dev.abstr3act.addon.utils.render;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.IntBuffer;

public class ScreenCaptureUtil {
    public static BufferedImage captureArea(int x1, int y1, int x2, int y2) {
        int width = x2 - x1;
        int height = y2 - y1;
        IntBuffer buffer = BufferUtils.createIntBuffer(width * height);
        RenderSystem.bindTexture(0);
        GL11.glReadPixels(x1, MinecraftClient.getInstance().getWindow().getFramebufferHeight() - y2, width, height, 6408, 5121, buffer);
        BufferedImage image = new BufferedImage(width, height, 2);
        int[] pixels = new int[width * height];
        buffer.get(pixels);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = pixels[y * width + x];
                int rgba = (pixel & 0xFF) << 24 | pixel >> 8 & 16777215;
                image.setRGB(x, height - y - 1, rgba);
            }
        }

        return image;
    }

    public static void saveCapture(int x1, int y1, int x2, int y2, String path) {
        BufferedImage image = captureArea(x1, y1, x2, y2);
        File file = new File(path);

        try {
            ImageIO.write(image, "PNG", file);
            System.out.println("Screenshot saved: " + file.getAbsolutePath());
        } catch (IOException var8) {
            var8.printStackTrace();
        }
    }
}
