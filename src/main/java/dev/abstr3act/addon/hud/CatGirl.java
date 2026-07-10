package dev.abstr3act.addon.hud;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.abstr3act.addon.hud.storage.Chars;
import dev.abstr3act.addon.utils.render.Render2DEngine;
import meteordevelopment.meteorclient.settings.DoubleSetting.Builder;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import net.minecraft.util.Identifier;
import net.minecraft.util.InvalidIdentifierException;
import org.lwjgl.opengl.GL13;

public class CatGirl extends HudElement {
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
    private final Setting<Double> girlScale = this.sgGeneral
        .add(
            ((Builder) ((Builder) new Builder().name("Catgirl Scale")).description("Modify the size of the Catgirl."))
                .defaultValue(1.0)
                .min(0.0)
                .sliderRange(0.0, 10.0)
                .build()
        );
    private final Setting<SideMode> side = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) new meteordevelopment.meteorclient.settings.EnumSetting.Builder()
                .name("Kill Message Mode"))
                .description("What kind of messages to send."))
                .defaultValue(SideMode.Right))
                .build()
        );
    private final Setting<chars> image = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) new meteordevelopment.meteorclient.settings.EnumSetting.Builder()
                .name("Image"))
                .description("idk"))
                .defaultValue(chars.no_underwear))
                .build()
        );
    private final Setting<Integer> shadowOffsetX = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("Shadow Offset X"))
                .description("."))
                .defaultValue(0))
                .sliderRange(-100, 100)
                .build()
        );
    private final Setting<Integer> shadowOffsetY = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("Shadow Offset Y"))
                .description("."))
                .defaultValue(0))
                .sliderRange(-100, 100)
                .build()
        );
    private final Setting<Integer> renderOffsetX = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("renderOffsetX"))
                .description("."))
                .defaultValue(0))
                .sliderRange(-1000, 1000)
                .build()
        );
    private final Setting<Integer> renderX = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("renderX"))
                .description("."))
                .defaultValue(200))
                .sliderRange(-1000, 1000)
                .build()
        );
    private final Setting<Integer> renderX2 = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("renderX_2"))
                .description("."))
                .defaultValue(360))
                .sliderRange(-1000, 1000)
                .build()
        );
    private final Setting<SettingColor> shadowColor = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                .name("Shadow Color"))
                .description("."))
                .defaultValue(new Color(0, 0, 0, 0))
                .build()
        );
    private final Setting<Boolean> onlyInClickGUI = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("onlyInClickGUI"))
                .description("."))
                .defaultValue(false))
                .build()
        );
    private final long animationDuration = 500L;    public static final HudElementInfo<CatGirl> INFO = new HudElementInfo(
        dev.abstr3act.addon.Compassion.HUD_GROUP, "catgirl", "It's a Cat girl what do you want", CatGirl::new
    );
    private boolean animationStarted = false;
    private long animationStart = 0L;
    private float renderXOffset;
    public CatGirl() {
        super(INFO);
    }

    public void render(HudRenderer renderer) {
        this.setSize(50.0, 50.0);
        if (this.image.get() != null) {
            try {
                if (!Utils.canCloseGui()) {
                    this.animationStarted = false;
                    this.animationStart = 0L;
                    this.renderXOffset = (this.renderOffsetX.get()).intValue();
                }

                if (Utils.canCloseGui() && !this.animationStarted) {
                    this.animationStarted = true;
                    this.animationStart = System.currentTimeMillis();
                }

                float progress = this.animationStarted ? Math.min(1.0F, (float) (System.currentTimeMillis() - this.animationStart) / 500.0F) : 0.0F;
                float easedProgress = (float) (1.0 - Math.cos(progress * Math.PI / 2.0));
                this.renderXOffset = (this.renderOffsetX.get()).intValue() * (1.0F - easedProgress);
                float renderX = (float) (
                    this.x - this.renderX.get()
                        + (this.side.get() == SideMode.Left ? this.girlScale.get() * (this.renderX2.get()).intValue() : 0.0)
                        + this.renderXOffset
                );
                if (Utils.canCloseGui()) {
                    this.renderIconAt(renderer, renderX);
                }
            } catch (InvalidIdentifierException var5) {
                var5.printStackTrace();
            }
        }
    }

    private void renderIconAt(HudRenderer renderer, float renderX) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
        GL13.glEnable(32925);
        RenderSystem.setShaderTexture(0, ((chars) this.image.get()).icons);
        RenderSystem.setShaderColor(
            ((SettingColor) this.shadowColor.get()).r,
            ((SettingColor) this.shadowColor.get()).g,
            ((SettingColor) this.shadowColor.get()).b,
            ((SettingColor) this.shadowColor.get()).a / 255.0F
        );
        Render2DEngine.renderTextureX(
            renderer.drawContext.getMatrices(),
            renderX + (this.shadowOffsetX.get()).intValue(),
            this.y + this.shadowOffsetY.get() - 180,
            this.girlScale.get() * (this.side.get() == SideMode.Left ? this.girlScale.get() * -360.0 : 360.0),
            this.girlScale.get() * 360.0,
            0.0F,
            0.0F,
            360.0,
            360.0,
            360.0,
            360.0
        );
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        Render2DEngine.renderTextureX(
            renderer.drawContext.getMatrices(),
            renderX,
            this.y - 180,
            this.girlScale.get() * (this.side.get() == SideMode.Left ? this.girlScale.get() * -360.0 : 360.0),
            this.girlScale.get() * 360.0,
            0.0F,
            0.0F,
            360.0,
            360.0,
            360.0,
            360.0
        );
        GL13.glDisable(32925);
        RenderSystem.disableBlend();
        RenderSystem.disableDepthTest();
    }

    public static enum SideMode {
        Right,
        Left;
    }

    static enum chars {
        ayu(Chars.a, "Ayu"),
        eto(Chars.b, "Eto"),
        eto_hoppe(Chars.c, "Eto & Hoppe"),
        hikari_fantasia(Chars.d, "Hikari (Fantasia)"),
        hikari_fatalis(Chars.e, "Hikari (Fantasia)"),
        hikari_zero(Chars.f, "Hikari (Zero)"),
        ilith_ivy(Chars.g, "Ilith & Ivy"),
        kanae(Chars.h, "Kanae"),
        kou(Chars.i, "Kou"),
        kou_winter(Chars.j, "Kou (Winter)"),
        lagrange(Chars.k, "Lagrange"),
        luna(Chars.l, "Luna"),
        luna_ilot(Chars.m, "Luna & Ilot"),
        nami_twilight(Chars.n, "Nami (Twilight)"),
        no_underwear(Chars.o, "No Underwear"),
        sapphire(Chars.p, "Sapphire"),
        shirahime(Chars.q, "Shirahime"),
        tairitsu_axium(Chars.r, "Tairitsu (Axium)"),
        tairitsu_elegy(Chars.s, "Tairitsu (Elegy)"),
        tairitsu_g(Chars.t, "Tairitsu (Grievous Lady)"),
        tairitsu_hikari(Chars.u, "Tairitsu & Hikari"),
        tairitsu_sonata(Chars.v, "Tairitsu (Sonata)"),
        tairitsu_tempest(Chars.w, "Tairitsu (Tempest)"),
        salt(Chars.x1, "Salt"),
        acid(Chars.x, "Acid");

        private final String name;
        private final Identifier icons;

        private chars(Identifier icons, String name) {
            this.icons = icons;
            this.name = name;
        }

        @Override
        public String toString() {
            return this.name;
        }

        public Identifier getIdentifier() {
            return this.icons;
        }
    }


}
