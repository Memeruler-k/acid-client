package dev.abstr3act.addon.utils.render;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.util.math.MatrixStack;
import org.lwjgl.opengl.GL11;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

public class ImageRenderer {
    private static int textureId = -1;

    public static void uploadImageToTexture(BufferedImage image) {
        if (textureId == -1) {
            textureId = GL11.glGenTextures();
        }

        RenderSystem.bindTexture(textureId);
        int width = image.getWidth();
        int height = image.getHeight();
        int[] pixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
        IntBuffer buffer = ByteBuffer.allocateDirect(pixels.length * 4).asIntBuffer();
        buffer.put(pixels).flip();
        GL11.glTexParameteri(3553, 10241, 9728);
        GL11.glTexParameteri(3553, 10240, 9728);
        GL11.glTexImage2D(3553, 0, 6408, width, height, 0, 6408, 5121, buffer);
    }

    public static void drawImage(MatrixStack matrices, int x, int y, int width, int height) {
        RenderSystem.setShaderTexture(0, textureId);
        Render2DEngine.renderTexture(matrices, x, y, width, height, 0.0F, 0.0F, width, height, width, height);
    }
}
