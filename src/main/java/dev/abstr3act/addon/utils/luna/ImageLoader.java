package dev.abstr3act.addon.utils.luna;

import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.Resource;
import net.minecraft.util.Identifier;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.InputStream;

public class ImageLoader {
    public static Image loadImageFromIdentifier(Identifier identifier) {
        try {
            MinecraftClient client = MinecraftClient.getInstance();
            InputStream resourceStream = ((Resource) client.getResourceManager().getResource(identifier).get()).getInputStream();
            return ImageIO.read(resourceStream);
        } catch (Exception var3) {
            var3.printStackTrace();
            return null;
        }
    }
}
