package dev.abstr3act.addon.hud;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.abstr3act.addon.utils.render.Render2DEngine;
import meteordevelopment.meteorclient.settings.DoubleSetting.Builder;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import net.minecraft.util.Identifier;
import net.minecraft.util.InvalidIdentifierException;
import org.lwjgl.opengl.GL13;

public class STFUIcon extends HudElement {
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
    private final Setting<Double> scale = this.sgGeneral
        .add(((Builder) ((Builder) new Builder().name("scale")).description("The scale.")).defaultValue(2.0).min(0.01).sliderRange(0.01, 5.0).build());
    private final Setting<icons> icon = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) new meteordevelopment.meteorclient.settings.EnumSetting.Builder()
                .name("Icon"))
                .description("The icon"))
                .defaultValue(icons.arcaea))
                .build()
        );
    private final Setting<SideMode> side = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) new meteordevelopment.meteorclient.settings.EnumSetting.Builder()
                .name("Rotate Mode"))
                .description("idk."))
                .defaultValue(SideMode.Right))
                .build()
        );
    private Identifier i = Identifier.of("acid", "icons/arcaea");
    public STFUIcon() {
        super(INFO);
    }    public static final HudElementInfo<STFUIcon> INFO = new HudElementInfo(
        dev.abstr3act.addon.Compassion.HUD_GROUP, "UselessIcon", "It's a Cat girl what do you want", STFUIcon::new
    );

    public void render(HudRenderer renderer) {
        if (this.i != null) {
            try {
                this.setSize(180.0, 180.0);
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
                RenderSystem.enableDepthTest();
                GL13.glEnable(32925);
                this.i = Identifier.of("acid", "icons/" + ((icons) this.icon.get()).icons);
                RenderSystem.setShaderTexture(0, this.i);
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                Render2DEngine.renderTextureX(
                    renderer.drawContext.getMatrices(),
                    this.x + (this.side.get() == SideMode.Left ? this.scale.get() * ((icons) this.icon.get()).width : 0.0),
                    this.y,
                    this.scale.get()
                        * (
                        this.side.get() == SideMode.Left
                            ? this.scale.get() * -((icons) this.icon.get()).width
                            : ((icons) this.icon.get()).width
                    ),
                    this.scale.get() * ((icons) this.icon.get()).height,
                    0.0F,
                    0.0F,
                    ((icons) this.icon.get()).width,
                    ((icons) this.icon.get()).height,
                    ((icons) this.icon.get()).width,
                    ((icons) this.icon.get()).height
                );
                GL13.glDisable(32925);
                RenderSystem.disableDepthTest();
                RenderSystem.defaultBlendFunc();
                RenderSystem.disableBlend();
            } catch (InvalidIdentifierException var3) {
                var3.printStackTrace();
            }
        }
    }

    public static enum SideMode {
        Right,
        Left;
    }

    public static enum icons {
        arcaea("arcaea.png", "Arcaea", 726, 195),
        ddlc("ddlc.png", "DDLC", 512, 512),
        sb616("lowiro.png", "SB616", 440, 192),
        blueArchive("bluearchive.png", "BlueArchive", 665, 250),
        phantomShield("ps.png", "PhantomShield", 512, 512),
        lb("liquidbounce.png", "LiquidBounce", 502, 146),
        compassion("compassion2.png", "Compassion", 512, 512),
        new_compassion("compassion_new.png", "Compassion (New)", 512, 512),
        acid("acid.png", "Acid", 1200, 470),
        acid_white("acid_white.png", "Acid White", 1200, 470),
        acid_shadered("acid_white_shadered.png", "Acid Shadered", 1200, 470),
        sigma("sigma.png", "JelloSigma", 616, 144);

        private final String icons;
        private final String name;
        private final int width;
        private final int height;

        private icons(String icons, String name, int width, int height) {
            this.icons = icons;
            this.name = name;
            this.height = height;
            this.width = width;
        }

        @Override
        public String toString() {
            return this.name;
        }

        public String getName() {
            return this.icons;
        }

        public int getWidth() {
            return this.width;
        }

        public int getHeight() {
            return this.height;
        }
    }


}
