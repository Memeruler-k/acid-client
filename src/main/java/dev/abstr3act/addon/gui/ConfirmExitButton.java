package dev.abstr3act.addon.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.abstr3act.addon.utils.render.Render2DEngine;
import meteordevelopment.meteorclient.MeteorClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL13;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ConfirmExitButton extends Screen {
    public static int ticksActive;
    private static ConfirmExitButton INSTANCE = new ConfirmExitButton();
    private final List<MainMenuButton> buttons = new ArrayList<>();
    private final List<MainMenuText> texts = new ArrayList<>();
    public boolean confirm = false;

    protected ConfirmExitButton() {
        super(Text.of("CExitConfirm"));
        INSTANCE = this;
        this.texts.add(new MainMenuText(-100.0F, 10.0F, "Do you really want to quit the game?"));
        this.buttons.add(new MainMenuButton(-110.0F, 50.0F, "Confirm".toUpperCase(Locale.ROOT), MeteorClient.mc::scheduleStop, true));
        this.buttons.add(new MainMenuButton(-110.0F, 100.0F, "Cancel".toUpperCase(Locale.ROOT), () -> MeteorClient.mc.setScreen(new MainMenuScreen())));
    }

    public static ConfirmExitButton getInstance() {
        ticksActive = 0;
        if (INSTANCE == null) {
            INSTANCE = new ConfirmExitButton();
        }

        return INSTANCE;
    }

    public void tick() {
        ticksActive++;
        if (ticksActive > 400) {
            ticksActive = 0;
        }
    }

    public void render(@NotNull DrawContext context, int mouseX, int mouseY, float delta) {
        float screenWidth = MeteorClient.mc.getWindow().getScaledWidth();
        float screenHeight = MeteorClient.mc.getWindow().getScaledHeight();
        float halfOfWidth = screenWidth / 2.0F;
        float halfOfHeight = screenHeight / 2.0F;
        float scaleFactor = 1.2F;
        float scaledWidth = screenWidth * scaleFactor;
        float scaledHeight = screenHeight * scaleFactor;
        float maxOffsetX = (scaledWidth - screenWidth) / 2.0F;
        float maxOffsetY = (scaledHeight - screenHeight) / 2.0F;
        float normalizedMouseX = (mouseX - halfOfWidth) / halfOfWidth;
        float normalizedMouseY = (mouseY - halfOfHeight) / halfOfHeight;
        float offsetX = normalizedMouseX * maxOffsetX;
        float offsetY = normalizedMouseY * maxOffsetY;
        offsetX = Math.max(-maxOffsetX, Math.min(maxOffsetX, offsetX));
        offsetY = Math.max(-maxOffsetY, Math.min(maxOffsetY, offsetY));
        Identifier background = Identifier.of("acid", "gui/bg_7.png");
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
        GL13.glEnable(32925);
        RenderSystem.setShaderTexture(0, background);
        Render2DEngine.renderTextureX(
            context.getMatrices(), -maxOffsetX + offsetX, -maxOffsetY + offsetY, scaledWidth, scaledHeight, 0.0F, 0.0F, 1920.0, 1080.0, 1920.0, 1080.0
        );
        RenderSystem.resetTextureMatrix();
        GL13.glDisable(32925);
        RenderSystem.disableBlend();
        RenderSystem.disableDepthTest();
        RenderSystem.defaultBlendFunc();
        Color black = new Color(0, true);
        Render2DEngine.draw2DGradientRect(
            context.getMatrices(),
            0.0F,
            0.0F,
            MeteorClient.mc.getWindow().getScaledWidth(),
            MeteorClient.mc.getWindow().getScaledHeight(),
            Render2DEngine.injectAlpha(black, 170),
            black,
            Render2DEngine.injectAlpha(black, 170),
            black
        );
        this.texts.forEach(a -> a.onRender(context));
        this.buttons.forEach(b -> b.onRender(context, mouseX, mouseY));
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        float halfOfWidth = MeteorClient.mc.getWindow().getScaledWidth() / 2.0F;
        float halfOfHeight = MeteorClient.mc.getWindow().getScaledHeight() / 2.0F;
        this.buttons.forEach(b -> b.onClick((int) mouseX, (int) mouseY));
        if (Render2DEngine.isHovered(mouseX, mouseY, halfOfWidth - 50.0F, halfOfHeight + 70.0F, 100.0, 10.0)) {
            this.confirm = true;
            MeteorClient.mc.setScreen(new TitleScreen());
            this.confirm = false;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }
}
