package dev.abstr3act.addon.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.abstr3act.addon.font.FontRenderers;
import dev.abstr3act.addon.modules.Lacrymira.HUDSetting;
import dev.abstr3act.addon.utils.render.Render2DEngine;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.gui.GuiThemes;
import meteordevelopment.meteorclient.gui.tabs.Tab;
import meteordevelopment.meteorclient.gui.tabs.Tabs;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.Utils;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL13;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainMenuScreen extends Screen {
    public static int ticksActive;
    private static MainMenuScreen INSTANCE = new MainMenuScreen();
    private final List<MainMenuButton> buttons = new ArrayList<>();
    public boolean confirm = false;

    protected MainMenuScreen() {
        super(Text.of("CMainMenuScreen"));
        INSTANCE = this;
        this.buttons
            .add(
                new MainMenuButton(
                    -110.0F,
                    -50.0F,
                    I18n.translate("menu.singleplayer", new Object[0]).toUpperCase(Locale.ROOT),
                    () -> MeteorClient.mc.setScreen(new SelectWorldScreen(this))
                )
            );
        this.buttons
            .add(
                new MainMenuButton(
                    -110.0F,
                    0.0F,
                    I18n.translate("menu.multiplayer", new Object[0]).toUpperCase(Locale.ROOT),
                    () -> MeteorClient.mc.setScreen(new MultiplayerScreen(this))
                )
            );
        this.buttons
            .add(
                new MainMenuButton(
                    -110.0F,
                    50.0F,
                    I18n.translate("menu.options", new Object[0]).toUpperCase(Locale.ROOT).replace(".", ""),
                    () -> MeteorClient.mc.setScreen(new OptionsScreen(this, MeteorClient.mc.options))
                )
            );
        this.buttons.add(new MainMenuButton(-110.0F, 100.0F, "ClickGUI".toUpperCase(Locale.ROOT), this::toggleGui));
        this.buttons
            .add(
                new MainMenuButton(
                    -110.0F, 150.0F, I18n.translate("menu.quit", new Object[0]).toUpperCase(Locale.ROOT), () -> MeteorClient.mc.setScreen(new ConfirmExitButton())
                )
            );
    }

    public static MainMenuScreen getInstance() {
        ticksActive = 0;
        if (INSTANCE == null) {
            INSTANCE = new MainMenuScreen();
        }

        return INSTANCE;
    }

    private void toggleGui() {
        if (Utils.canCloseGui()) {
            MeteorClient.mc.currentScreen.close();
        } else {
            ((Tab) Tabs.get().get(0)).openScreen(GuiThemes.get());
        }
    }

    public void tick() {
        ticksActive++;
        if (ticksActive > 400) {
            ticksActive = 0;
        }
    }

    public void render(@NotNull DrawContext context, int mouseX, int mouseY, float delta) {
        HUDSetting hudSetting = (HUDSetting) Modules.get().get(HUDSetting.class);
        context.getMatrices().scale(1.0F, 1.0F, 1.0F);
        float screenWidth = MeteorClient.mc.getWindow().getScaledWidth();
        float screenHeight = MeteorClient.mc.getWindow().getScaledHeight();
        float halfOfWidth = screenWidth / 2.0F;
        float halfOfHeight = screenHeight / 2.0F;
        float scaleFactor = (float) (((HUDSetting.bg) hudSetting.background.get()).getScale() * hudSetting.globalFactor.get());
        float imgWidth = ((HUDSetting.bg) hudSetting.background.get()).getWidth();
        float imgHeight = ((HUDSetting.bg) hudSetting.background.get()).getHeight();
        float scaledWidth = imgWidth * scaleFactor;
        float scaledHeight = imgHeight * scaleFactor;
        float maxOffsetX = (scaledWidth - screenWidth) / 2.0F;
        float maxOffsetY = (scaledHeight - screenHeight) / 2.0F;
        float normalizedMouseX = (mouseX - halfOfWidth) / halfOfWidth;
        float normalizedMouseY = (mouseY - halfOfHeight) / halfOfHeight;
        float offsetX = normalizedMouseX * maxOffsetX;
        float offsetY = normalizedMouseY * maxOffsetY;
        offsetX = Math.max(-maxOffsetX, Math.min(maxOffsetX, offsetX));
        offsetY = Math.max(-maxOffsetY, Math.min(maxOffsetY, offsetY));
        Identifier background = Identifier.of("acid", "gui/" + ((HUDSetting.bg) hudSetting.background.get()).getName());
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
        GL13.glEnable(32925);
        RenderSystem.setShaderTexture(0, background);
        Render2DEngine.renderTextureX(
            context.getMatrices(),
            -maxOffsetX + offsetX,
            -maxOffsetY + offsetY,
            scaledWidth,
            scaledHeight,
            0.0F,
            0.0F,
            ((HUDSetting.bg) hudSetting.background.get()).getWidth(),
            ((HUDSetting.bg) hudSetting.background.get()).getHeight(),
            ((HUDSetting.bg) hudSetting.background.get()).getWidth(),
            ((HUDSetting.bg) hudSetting.background.get()).getHeight()
        );
        Identifier foreground = Identifier.of("acid", "gui/lg_3.png");
        RenderSystem.setShaderTexture(0, foreground);
        Render2DEngine.renderTextureA(
            context.getMatrices(), halfOfWidth - 115.0F - 37.5F, halfOfHeight - 170.0F + 30.0F, 300.0, 70.0, 0.0F, 0.0F, 600.0, 140.0, 600.0, 140.0
        );
        GL13.glDisable(32925);
        RenderSystem.disableBlend();
        RenderSystem.disableDepthTest();
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
        FontRenderers.categories.drawStringFix(context.getMatrices(), "Compassion V8 Full Version\nDeveloping by Team Compassion\n", 4.0, 5.0, new Color(16777215));
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
