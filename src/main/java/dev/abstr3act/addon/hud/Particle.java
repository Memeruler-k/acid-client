package dev.abstr3act.addon.hud;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.abstr3act.addon.events.EventAttack;
import dev.abstr3act.addon.hud.storage.TextureStorage;
import dev.abstr3act.addon.utils.render.Render2DEngine;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.world.TickEvent.Post;
import meteordevelopment.meteorclient.settings.DoubleSetting.Builder;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.EntityHitResult;

import java.util.ArrayList;
import java.util.List;

public class Particle extends HudElement {
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
    private final Setting<Double> hitOffsetX = this.sgGeneral
        .add(((Builder) ((Builder) new Builder().name("hitOffsetX")).description(".")).defaultValue(0.0).sliderRange(-100.0, 100.0).build());
    private final Setting<Double> hitOffsetY = this.sgGeneral
        .add(((Builder) ((Builder) new Builder().name("hitOffsetY")).description(".")).defaultValue(0.0).sliderRange(-100.0, 100.0).build());
    private final Setting<Double> animationStep = this.sgGeneral
        .add(((Builder) ((Builder) new Builder().name("animationStep")).description(".")).defaultValue(2.0).sliderRange(-100.0, 100.0).build());
    private final Setting<Double> hitScale = this.sgGeneral
        .add(((Builder) ((Builder) new Builder().name("hitScale")).description(".")).defaultValue(1.5).sliderRange(0.01, 10.0).build());
    int i = 0;    public static final HudElementInfo<Particle> INFO = new HudElementInfo(
        dev.abstr3act.addon.Compassion.HUD_GROUP, "Particles", "Arcaea hit particle", Particle::new
    );
    Identifier texture = TextureStorage.pure;
    boolean clicked;
    private int currentIndex = 0;
    private List<RenderRegion> regions = new ArrayList<>();
    public Particle() {
        super(INFO);
        this.initializeRegions();
        MeteorClient.EVENT_BUS.subscribe(this);
    }

    @EventHandler
    private void onTickEvent(Post tick) {
        if (this.currentIndex >= 15) {
            this.clicked = false;
            this.currentIndex = 0;
        }

        this.currentIndex++;
    }

    @EventHandler
    private void onAttackEvent(EventAttack event) {
        if (!(MeteorClient.mc.crosshairTarget instanceof EntityHitResult) && event.isPre()) {
            this.texture = TextureStorage.lost;
        } else {
            this.texture = MeteorClient.mc.player.fallDistance > 0.0F ? TextureStorage.pure : TextureStorage.far;
        }

        this.currentIndex = 0;
        this.clicked = true;
    }

    private void initializeRegions() {
        int gridSize = 4;
        int regionSize = 256;
        double textureWidth = 1024.0;
        double textureHeight = 1024.0;
        double regionWidth = regionSize / textureWidth;
        double regionHeight = regionSize / textureHeight;
        double x0 = 0.0;
        double y0 = 0.0;

        for (int row = 0; row < gridSize; row++) {
            for (int col = 0; col < gridSize; col++) {
                double x = x0 + col * regionSize;
                double y = y0 + row * regionSize;
                float u = (float) (col * regionWidth);
                float v = (float) (row * regionHeight);
                this.regions.add(new RenderRegion(x, y, 256.0, 256.0, u, v, regionWidth, regionHeight, textureWidth, textureHeight));
            }
        }
    }

    public void render(HudRenderer renderer) {
        this.setSize(180.0, 180.0);
        if (MeteorClient.mc.player != null && this.clicked) {
            Identifier i = Identifier.of("acid", "hud/particle/note_light.png");
            RenderSystem.setShaderTexture(0, i);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            int textureX = this.currentIndex % 4 * 256;
            int textureY = this.currentIndex / 4 * 256;
            if (!this.texture.equals(TextureStorage.lost)) {
                Render2DEngine.renderTextureA(
                    renderer.drawContext.getMatrices(), this.getX(), this.getY(), 256.0, 256.0, textureX, textureY, 256.0, 256.0, 1024.0, 1024.0
                );
            }

            RenderSystem.setShaderTexture(0, this.texture);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F - this.currentIndex / 10.0F);
            Render2DEngine.renderTextureA(
                renderer.drawContext.getMatrices(),
                this.getX() + this.hitOffsetX.get(),
                this.getY() + this.hitOffsetY.get() - this.currentIndex * this.animationStep.get(),
                114.0 * this.hitScale.get(),
                108.0 * this.hitScale.get(),
                0.0F,
                0.0F,
                114.0 * this.hitScale.get(),
                108.0 * this.hitScale.get(),
                114.0 * this.hitScale.get(),
                108.0 * this.hitScale.get()
            );
        }
    }


}
