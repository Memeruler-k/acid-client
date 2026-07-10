package dev.abstr3act.addon.gui;

import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.renderer.text.TextRenderer;
import meteordevelopment.meteorclient.utils.render.color.Color;
import net.minecraft.client.gui.DrawContext;
import org.jetbrains.annotations.NotNull;

public class MainMenuText {
    private final float posX;
    private final float posY;
    private final String name;

    public MainMenuText(float posX, float posY, @NotNull String name) {
        this.name = name;
        this.posX = posX;
        this.posY = posY;
    }

    public void onRender(DrawContext context) {
        float halfOfWidth = MeteorClient.mc.getWindow().getScaledWidth() / 2.0F;
        float halfOfHeight = MeteorClient.mc.getWindow().getScaledHeight() / 2.0F;
        TextRenderer.get().begin(0.7, false, false);
        TextRenderer.get().render(this.name, halfOfWidth + this.posX, halfOfHeight + this.posY, new Color(-1));
        TextRenderer.get().end(context.getMatrices());
    }
}
