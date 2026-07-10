package dev.abstr3act.addon.gui;

import dev.abstr3act.addon.font.FontRenderers;
import dev.abstr3act.addon.utils.render.Render2DEngine;
import meteordevelopment.meteorclient.MeteorClient;
import net.minecraft.client.gui.DrawContext;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

public class MainMenuButton {
    public static boolean locked = false;
    private final float posX;
    private final float posY;
    private final float width;
    private final float height;
    private final String name;
    private final Runnable action;
    private final boolean isExit;
    private final float animationSpeed = 0.05F;
    private float animationProgress = 0.0F;

    public MainMenuButton(float posX, float posY, @NotNull String name, Runnable action, boolean isExit) {
        this.name = name;
        this.posX = posX;
        this.posY = posY;
        this.action = action;
        this.width = 214.0F;
        this.height = 38.0F;
        this.isExit = isExit;
    }

    public MainMenuButton(float posX, float posY, float width, float height, @NotNull String name, Runnable action, boolean isExit) {
        this.name = name;
        this.posX = posX;
        this.posY = posY;
        this.action = action;
        this.width = width;
        this.height = height;
        this.isExit = isExit;
    }

    public MainMenuButton(float posX, float posY, @NotNull String name, Runnable action) {
        this(posX, posY, name, action, false);
    }

    public void onRender(DrawContext context, float mouseX, float mouseY) {
        float halfOfWidth = MeteorClient.mc.getWindow().getScaledWidth() / 2.0F;
        float halfOfHeight = MeteorClient.mc.getWindow().getScaledHeight() / 2.0F;
        boolean hovered = Render2DEngine.isHovered(mouseX, mouseY, halfOfWidth + this.posX, halfOfHeight + this.posY, this.width, this.height);
        this.animationProgress = hovered ? Math.min(1.0F, this.animationProgress + 0.05F) : Math.max(0.0F, this.animationProgress - 0.05F);
        Color startColor = new Color(0);
        Color endColor = this.isExit ? new Color(15025477) : new Color(12237498);
        int r = (int) (startColor.getRed() + (endColor.getRed() - startColor.getRed()) * this.animationProgress);
        int g = (int) (startColor.getGreen() + (endColor.getGreen() - startColor.getGreen()) * this.animationProgress);
        int b = (int) (startColor.getBlue() + (endColor.getBlue() - startColor.getBlue()) * this.animationProgress);
        Color currentColor = new Color(r, g, b);
        FontRenderers.modules
            .drawCenteredString(
                context.getMatrices(),
                "",
                halfOfWidth + this.posX + this.width / 2.0F,
                halfOfHeight + this.posY + this.height / 2.0F - 3.0F - this.animationProgress * 3.0F,
                new Color(hovered ? -1 : Render2DEngine.applyOpacity(-1, 0.7F))
            );
        Render2DEngine.drawOutlineShader(context.getMatrices(), halfOfWidth + this.posX, halfOfHeight + this.posY, this.width, this.height);
        Render2DEngine.drawRoundedBlur(
            context.getMatrices(), halfOfWidth + this.posX, halfOfHeight + this.posY, this.width, this.height, 0.0F, currentColor, 20.0F, 0.55F
        );
        FontRenderers.modules
            .drawCenteredString(
                context.getMatrices(),
                this.name,
                halfOfWidth + this.posX + this.width / 2.0F,
                halfOfHeight + this.posY + this.height / 2.0F - 3.0F - this.animationProgress * 3.0F,
                new Color(hovered ? -1 : Render2DEngine.applyOpacity(-1, 0.7F))
            );
    }

    public void onClick(int mouseX, int mouseY) {
        float halfOfWidth = MeteorClient.mc.getWindow().getScaledWidth() / 2.0F;
        float halfOfHeight = MeteorClient.mc.getWindow().getScaledHeight() / 2.0F;
        boolean hovered = Render2DEngine.isHovered(mouseX, mouseY, halfOfWidth + this.posX, halfOfHeight + this.posY, this.width, this.height);
        if (hovered) {
            this.action.run();
        }
    }
}
