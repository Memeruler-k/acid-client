package dev.abstr3act.addon.hud;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.abstr3act.addon.font.FontRenderers;
import dev.abstr3act.addon.hud.storage.TextureStorage;
import dev.abstr3act.addon.utils.render.Render2DEngine;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.settings.DoubleSetting.Builder;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import net.minecraft.util.Identifier;
import org.lwjgl.opengl.GL13;

import java.awt.*;

public class Welcomer extends HudElement {
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
    private final Setting<Double> scale = this.sgGeneral
        .add(((Builder) ((Builder) new Builder().name("scale")).description("The scale.")).defaultValue(2.0).min(1.0).sliderRange(1.0, 5.0).build());
    private final Setting<Double> scale_char = this.sgGeneral
        .add(((Builder) ((Builder) new Builder().name("Char Scale")).description("The scale.")).defaultValue(2.0).min(1.0).sliderRange(1.0, 5.0).build());
    private final Setting<Double> scale_text = this.sgGeneral
        .add(((Builder) ((Builder) new Builder().name("Text Scale")).description("The scale.")).defaultValue(2.0).min(1.0).sliderRange(1.0, 5.0).build());
    private final Setting<Double> tx = this.sgGeneral
        .add(((Builder) ((Builder) new Builder().name("Text offset X")).description(".")).defaultValue(0.0).sliderRange(-500.0, 500.0).build());
    private final Setting<Double> ty = this.sgGeneral
        .add(((Builder) ((Builder) new Builder().name("Text offset Y")).description(".")).defaultValue(0.0).sliderRange(-500.0, 500.0).build());
    private final Setting<Double> offsetX = this.sgGeneral
        .add(((Builder) ((Builder) new Builder().name("Char offset X")).description(".")).defaultValue(0.0).sliderRange(-500.0, 500.0).build());
    private final Setting<Double> offsetY = this.sgGeneral
        .add(((Builder) ((Builder) new Builder().name("Char offset Y")).description(".")).defaultValue(0.0).sliderRange(-500.0, 500.0).build());
    private final Setting<mode> Mode = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) new meteordevelopment.meteorclient.settings.EnumSetting.Builder()
                .name("Text Mode"))
                .description("."))
                .defaultValue(mode.Username))
                .build()
        );
    private final Setting<SettingColor> textShaderColor = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                .name("Text Shadow Color"))
                .description("."))
                .defaultValue(new SettingColor(0, 0, 0))
                .build()
        );
    private final Setting<Double> ToffsetX = this.sgGeneral
        .add(((Builder) ((Builder) new Builder().name("Text shadow offset X")).description(".")).defaultValue(0.01).sliderRange(-10.0, 10.0).build());
    private final Setting<Double> ToffsetY = this.sgGeneral
        .add(((Builder) ((Builder) new Builder().name("Text shadow offset Y")).description(".")).defaultValue(0.01).sliderRange(-10.0, 10.0).build());
    private final Setting<SettingColor> color = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                .name("Color"))
                .description("."))
                .defaultValue(new SettingColor(255, 255, 255))
                .build()
        );
    private final Setting<SettingColor> shadowColor = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                .name("Shadow Color"))
                .description("."))
                .defaultValue(new SettingColor(255, 255, 255))
                .build()
        );
    private final Setting<Integer> shader_range = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("Shader Range"))
                .description("."))
                .defaultValue(40))
                .sliderRange(0, 100)
                .build()
        );
    private final Setting<Double> SoffsetX = this.sgGeneral
        .add(((Builder) ((Builder) new Builder().name("Shadow size offset X")).description(".")).defaultValue(0.0).sliderRange(-100.0, 100.0).build());
    private final Setting<Double> SoffsetY = this.sgGeneral
        .add(((Builder) ((Builder) new Builder().name("Shadow size offset Y")).description(".")).defaultValue(0.0).sliderRange(-100.0, 100.0).build());
    private final Setting<Double> StoffsetX = this.sgGeneral
        .add(((Builder) ((Builder) new Builder().name("Shadow offset X")).description(".")).defaultValue(0.0).sliderRange(-100.0, 100.0).build());
    private final Setting<Double> StoffsetY = this.sgGeneral
        .add(((Builder) ((Builder) new Builder().name("Shadow offset Y")).description(".")).defaultValue(0.0).sliderRange(-100.0, 100.0).build());
    private final Setting<bg> BG = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) new meteordevelopment.meteorclient.settings.EnumSetting.Builder()
                .name("Background"))
                .description("."))
                .defaultValue(bg.c1))
                .build()
        );
    private final Setting<chars> Chars = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) new meteordevelopment.meteorclient.settings.EnumSetting.Builder()
                .name("Char"))
                .description("."))
                .defaultValue(chars.不穿苦茶子))
                .build()
        );
    private final Setting<rating> ratings = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) new meteordevelopment.meteorclient.settings.EnumSetting.Builder()
                .name("Rating background"))
                .description("."))
                .defaultValue(rating.r7))
                .build()
        );
    private final Setting<Double> rating_scale = this.sgGeneral
        .add(((Builder) ((Builder) new Builder().name("Rating background scale")).description(".")).defaultValue(2.0).min(1.0).sliderRange(1.0, 5.0).build());
    private final Setting<Double> rating_offsetX = this.sgGeneral
        .add(((Builder) ((Builder) new Builder().name("Rating background offset X")).description(".")).defaultValue(0.0).sliderRange(-500.0, 500.0).build());
    private final Setting<Double> rating_offsetY = this.sgGeneral
        .add(((Builder) ((Builder) new Builder().name("Rating background offset Y")).description(".")).defaultValue(0.0).sliderRange(-500.0, 500.0).build());
    private final Setting<String> rating_text = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.StringSetting.Builder) ((meteordevelopment.meteorclient.settings.StringSetting.Builder) ((meteordevelopment.meteorclient.settings.StringSetting.Builder) new meteordevelopment.meteorclient.settings.StringSetting.Builder()
                .name("Rating display text"))
                .description("."))
                .defaultValue("13.0"))
                .build()
        );
    private final Setting<Double> rating_text_offsetX = this.sgGeneral
        .add(((Builder) ((Builder) new Builder().name("Rating display text offset X")).description(".")).defaultValue(0.0).sliderRange(-500.0, 500.0).build());
    private final Setting<Double> rating_text_offsetY = this.sgGeneral
        .add(((Builder) ((Builder) new Builder().name("Rating display text offset Y")).description(".")).defaultValue(0.0).sliderRange(-500.0, 500.0).build());
    private final Setting<Double> rating_text_scale = this.sgGeneral
        .add(((Builder) ((Builder) new Builder().name("Rating display text scale")).description(".")).defaultValue(2.0).min(1.0).sliderRange(1.0, 5.0).build());
    private final Setting<Boolean> rating_text_bold = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("Rating display text bold"))
                .description("."))
                .defaultValue(false))
                .build()
        );
    private final Setting<SettingColor> rating_text_color = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                .name("Rating display text color"))
                .description("."))
                .defaultValue(new SettingColor(255, 255, 255))
                .build()
        );
    private final Setting<Double> rating_text_shader_scale = this.sgGeneral
        .add(((Builder) ((Builder) new Builder().name("Rating display text shader scale")).description(".")).defaultValue(2.0).min(1.0).sliderRange(1.0, 5.0).build());
    private final Setting<Boolean> rating_text_shadow_bold = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("Rating display text shadow bold"))
                .description("."))
                .defaultValue(false))
                .build()
        );
    private final Setting<SettingColor> rating_text_shadow_color = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                .name("Rating display text shadow color"))
                .description("."))
                .defaultValue(new SettingColor(0, 0, 0))
                .build()
        );
    private final Setting<Double> rating_text_shadow_offsetX = this.sgGeneral
        .add(((Builder) ((Builder) new Builder().name("Rating display text shadow offset X")).description(".")).defaultValue(0.0).sliderRange(-500.0, 500.0).build());
    private final Setting<Double> rating_text_shadow_offsetY = this.sgGeneral
        .add(((Builder) ((Builder) new Builder().name("Rating display text shadow offset Y")).description(".")).defaultValue(0.0).sliderRange(-500.0, 500.0).build());
    private final Setting<Integer> rating_text_shadow_global_offset = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("Rating display text shadow global offset"))
                .description("."))
                .defaultValue(1))
                .sliderRange(-10, 10)
                .build()
        );
    public Welcomer() {
        super(INFO);
        MeteorClient.EVENT_BUS.subscribe(this);
    }    public static final HudElementInfo<Welcomer> INFO = new HudElementInfo(dev.abstr3act.addon.Compassion.HUD_GROUP, "Welcome", "Arcaea", Welcomer::new);

    public void render(HudRenderer renderer) {
        this.setSize(564.0 * this.scale.get(), 74.0 * this.scale.get());
        int x = this.getX();
        int y = this.getY();
        Render2DEngine.RdrawBlurredShadow(
            renderer.drawContext.getMatrices(),
            this.getX() + (this.StoffsetX.get()).floatValue() + (this.offsetX.get()).floatValue() + (this.SoffsetX.get()).floatValue(),
            this.getY() + (this.StoffsetY.get()).floatValue() + (this.offsetY.get()).floatValue() + (this.SoffsetY.get()).floatValue(),
            85.0F * (this.scale_char.get()).floatValue() + (this.SoffsetX.get()).floatValue(),
            85.0F * (this.scale_char.get()).floatValue() + (this.SoffsetY.get()).floatValue(),
            this.shader_range.get(),
            new Color(
                ((SettingColor) this.shadowColor.get()).r,
                ((SettingColor) this.shadowColor.get()).g,
                ((SettingColor) this.shadowColor.get()).b,
                ((SettingColor) this.shadowColor.get()).a
            )
        );
        RenderSystem.setShaderTexture(0, ((bg) this.BG.get()).icons);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
        GL13.glEnable(32925);
        Render2DEngine.renderTexture(
            renderer.drawContext.getMatrices(),
            this.getX(),
            this.getY(),
            564.0 * this.scale.get(),
            74.0 * this.scale.get(),
            0.0F,
            0.0F,
            564.0 * this.scale.get(),
            74.0 * this.scale.get(),
            564.0 * this.scale.get(),
            74.0 * this.scale.get(),
            true
        );
        RenderSystem.setShaderTexture(0, ((chars) this.Chars.get()).icons);
        Render2DEngine.renderTextureX(
            renderer.drawContext.getMatrices(),
            this.getX() + this.offsetX.get(),
            this.getY() + this.offsetY.get(),
            85.0 * this.scale_char.get(),
            85.0 * this.scale_char.get(),
            0.0F,
            0.0F,
            85.0 * this.scale_char.get(),
            85.0 * this.scale_char.get(),
            85.0 * this.scale_char.get(),
            85.0 * this.scale_char.get()
        );
        RenderSystem.setShaderTexture(0, ((rating) this.ratings.get()).icons);
        Render2DEngine.renderTextureX(
            renderer.drawContext.getMatrices(),
            this.getX() + this.rating_offsetX.get(),
            this.getY() + this.rating_offsetY.get(),
            59.5 * this.rating_scale.get(),
            59.5 * this.rating_scale.get(),
            0.0F,
            0.0F,
            85.0 * this.rating_scale.get(),
            59.5 * this.rating_scale.get(),
            85.0 * this.rating_scale.get(),
            59.5 * this.rating_scale.get()
        );
        GL13.glDisable(32925);
        RenderSystem.disableBlend();
        RenderSystem.disableDepthTest();
        int global = this.rating_text_shadow_global_offset.get();
        renderer.text(
            (String) this.rating_text.get(),
            x + this.rating_text_offsetX.get() - global,
            y + this.rating_text_offsetY.get() + 1.0,
            (meteordevelopment.meteorclient.utils.render.color.Color) this.rating_text_color.get(),
            false,
            this.rating_text_shader_scale.get()
        );
        renderer.text(
            (String) this.rating_text.get(),
            x + this.rating_text_offsetX.get() + global,
            y + this.rating_text_offsetY.get() - 1.0,
            (meteordevelopment.meteorclient.utils.render.color.Color) this.rating_text_color.get(),
            false,
            this.rating_text_shader_scale.get()
        );
        renderer.text(
            (String) this.rating_text.get(),
            x + this.rating_text_offsetX.get() - global,
            y + this.rating_text_offsetY.get() - 1.0,
            (meteordevelopment.meteorclient.utils.render.color.Color) this.rating_text_color.get(),
            false,
            this.rating_text_shader_scale.get()
        );
        renderer.text(
            (String) this.rating_text.get(),
            x + this.rating_text_offsetX.get() + global,
            y + this.rating_text_offsetY.get() + 1.0,
            (meteordevelopment.meteorclient.utils.render.color.Color) this.rating_text_color.get(),
            false,
            this.rating_text_shader_scale.get()
        );
        renderer.text(
            (String) this.rating_text.get(),
            x + this.rating_text_offsetX.get() + this.rating_text_shadow_offsetX.get(),
            y + this.rating_text_offsetY.get() + this.rating_text_shadow_offsetY.get(),
            (meteordevelopment.meteorclient.utils.render.color.Color) this.rating_text_shadow_color.get(),
            false,
            this.rating_text_scale.get()
        );
        switch ((mode) this.Mode.get()) {
            case ID:
                FontRenderers.user_text
                    .drawCenteredString(
                        renderer.drawContext.getMatrices(),
                        MeteorClient.mc.getSession().getUsername(),
                        x + this.tx.get() + this.ToffsetX.get(),
                        y + this.ty.get() + this.ToffsetY.get(),
                        new Color(((SettingColor) this.textShaderColor.get()).getPacked(), true)
                    );
                FontRenderers.user_text
                    .drawCenteredString(
                        renderer.drawContext.getMatrices(),
                        MeteorClient.mc.getSession().getUsername(),
                        x + this.tx.get(),
                        y + this.ty.get(),
                        new Color(((SettingColor) this.color.get()).getPacked(), true)
                    );
            case Username:
        }
    }

    public static enum bg {
        c1(TextureStorage.c1, "Course Banner 1"),
        c2(TextureStorage.c2, "Course Banner 2"),
        c3(TextureStorage.c3, "Course Banner 3"),
        c4(TextureStorage.c4, "Course Banner 4"),
        c5(TextureStorage.c5, "Course Banner 5"),
        c6(TextureStorage.c6, "Course Banner 6"),
        c7(TextureStorage.c7, "Course Banner 7"),
        c8(TextureStorage.c8, "Course Banner 8"),
        c9(TextureStorage.c9, "Course Banner 9"),
        c10(TextureStorage.c10, "Course Banner 10"),
        c11(TextureStorage.c11, "Course Banner 11");

        private final String name;
        private final Identifier icons;

        private bg(Identifier icons, String name) {
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

    public static enum chars {
        kou(TextureStorage.a, "Kou"),
        sapphire(TextureStorage.b, "Sapphire"),
        hikari_tairitsu(TextureStorage.c, "Hikari & Tairitsu"),
        tairitsu_axium(TextureStorage.d, "Tairitsu (Axium)"),
        tairitsu_grievous(TextureStorage.e, "Tairitsu (Grievous Lady)"),
        luna(TextureStorage.f, "Luna"),
        hikari_zero(TextureStorage.g, "Hikari (Zero)"),
        hikari_fracture(TextureStorage.h, "Hikari (Fracture)"),
        ayu(TextureStorage.i, "Ayu"),
        saya(TextureStorage.k, "Saya"),
        tairitsu_tempest(TextureStorage.m, "Tairitsu (Tempest)"),
        langrage(TextureStorage.n, "Langrage"),
        kanae(TextureStorage.o, "Kanae"),
        vita(TextureStorage.p, "Vita"),
        hikari_fatalis(TextureStorage.q, "Hikari (Fatalis)"),
        setsuna(TextureStorage.r, "Setsuna"),
        kou_winter(TextureStorage.s, "Kou (Winter)"),
        langrage2(TextureStorage.t, "Langrage (Awaken)"),
        不穿苦茶子(TextureStorage.v, "不穿苦茶子");

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

    static enum mode {
        Username,
        ID;
    }

    public static enum rating {
        r0(TextureStorage.r0, "Rating 0"),
        r1(TextureStorage.r1, "Rating 1"),
        r2(TextureStorage.r2, "Rating 2"),
        r3(TextureStorage.r3, "Rating 3"),
        r4(TextureStorage.r4, "Rating 4"),
        r5(TextureStorage.r5, "Rating 5"),
        r6(TextureStorage.r6, "Rating 6"),
        r7(TextureStorage.r7, "Rating 7"),
        r8(TextureStorage.r8, "Rating Off");

        private final String name;
        private final Identifier icons;

        private rating(Identifier icons, String name) {
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
