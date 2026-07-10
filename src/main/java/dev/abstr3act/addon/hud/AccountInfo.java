package dev.abstr3act.addon.hud;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.abstr3act.addon.hud.storage.Chars;
import dev.abstr3act.addon.module.BaseModule;
import dev.abstr3act.addon.utils.math.MathUtility;
import dev.abstr3act.addon.utils.render.Render2DEngine;
import dev.abstr3act.addon.utils.render.shaders.AnimationUtility;
import dev.abstr3act.addon.utils.seraphim.PlayerUtils;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.game.GameJoinedEvent;
import meteordevelopment.meteorclient.renderer.text.TextRenderer;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.StringSetting.Builder;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.misc.NameProtect;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import org.lwjgl.opengl.GL13;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class AccountInfo extends HudElement {
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
    public final Setting<String> prefix = this.sgGeneral
        .add(((Builder) ((Builder) ((Builder) new Builder().name("Prefix")).description(".")).defaultValue("C")).build());
    public final Setting<String> str = this.sgGeneral
        .add(((Builder) ((Builder) ((Builder) new Builder().name("String")).description(".")).defaultValue("ompassion ")).build());
    public final Setting<String> sux = this.sgGeneral
        .add(((Builder) ((Builder) ((Builder) new Builder().name("Suffix")).description(".")).defaultValue("V7 ")).build());
    private final Setting<Double> sizeX = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("sizeX"))
                .description(""))
                .defaultValue(50.0)
                .sliderMin(-1000.0)
                .sliderMax(1000.0)
                .build()
        );
    private final Setting<Double> sizeY = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("sizeY"))
                .description(""))
                .defaultValue(50.0)
                .sliderMin(-1000.0)
                .sliderMax(1000.0)
                .build()
        );
    private final Setting<Integer> ToffsetX = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("TextOffsetX"))
                .description(""))
                .defaultValue(0))
                .sliderMin(-1000)
                .sliderMax(1000)
                .build()
        );
    private final Setting<Integer> ToffsetX2 = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("TextOffsetX_2"))
                .description(""))
                .defaultValue(0))
                .sliderMin(-1000)
                .sliderMax(1000)
                .build()
        );
    private final Setting<Integer> ToffsetX3 = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("TextOffsetX_3"))
                .description(""))
                .defaultValue(0))
                .sliderMin(-1000)
                .sliderMax(1000)
                .build()
        );
    private final Setting<Integer> ToffsetY = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("TextOffsetY"))
                .description(""))
                .defaultValue(0))
                .sliderMin(-1000)
                .sliderMax(1000)
                .build()
        );
    private final Setting<Integer> offsetX = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("avatarOffsetX"))
                .description(""))
                .defaultValue(0))
                .sliderMin(-1000)
                .sliderMax(1000)
                .build()
        );
    private final Setting<Integer> offsetY = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("avatarOffsetY"))
                .description(""))
                .defaultValue(0))
                .sliderMin(-1000)
                .sliderMax(1000)
                .build()
        );
    private final Setting<Integer> height = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("height"))
                .description(""))
                .defaultValue(50))
                .sliderMin(0)
                .sliderMax(1000)
                .build()
        );
    private final Setting<Integer> width = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("width"))
                .description(""))
                .defaultValue(150))
                .sliderMin(0)
                .sliderMax(1000)
                .build()
        );
    private final Setting<Integer> gX = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("GraphOffsetX"))
                .description(""))
                .defaultValue(0))
                .sliderMin(-1000)
                .sliderMax(1000)
                .build()
        );
    private final Setting<Integer> gY = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("GraphOffsetY"))
                .description(""))
                .defaultValue(0))
                .sliderMin(-1000)
                .sliderMax(1000)
                .build()
        );
    private final Setting<Integer> aX = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("CooldownOffsetX"))
                .description(""))
                .defaultValue(0))
                .sliderMin(-1000)
                .sliderMax(1000)
                .build()
        );
    private final Setting<Integer> aY = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("CooldownOffsetY"))
                .description(""))
                .defaultValue(0))
                .sliderMin(-1000)
                .sliderMax(1000)
                .build()
        );
    private final Setting<Integer> aY2 = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("CooldownOffsetY2"))
                .description(""))
                .defaultValue(0))
                .sliderMin(-1000)
                .sliderMax(1000)
                .build()
        );
    private final Setting<Double> textScale = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("textScale"))
                .description(""))
                .defaultValue(1.0)
                .sliderMin(0.0)
                .sliderMax(2.0)
                .build()
        );
    private final Setting<Double> cooldownFactor = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("cooldownFactor"))
                .description(""))
                .defaultValue(1.0)
                .sliderMin(0.0)
                .sliderMax(5.0)
                .build()
        );
    private final Setting<Double> iconScale = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("iS"))
                .description(""))
                .defaultValue(1.0)
                .sliderMin(0.0)
                .sliderMax(2.0)
                .build()
        );
    private final Setting<Double> yMultiplier = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("yMultiplier"))
                .description(""))
                .defaultValue(7.0)
                .sliderMin(0.0)
                .sliderMax(20.0)
                .build()
        );
    private final Setting<Double> xMultiplier = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("xMultiplier"))
                .description(""))
                .defaultValue(7.0)
                .sliderMin(0.0)
                .sliderMax(20.0)
                .build()
        );
    private final Setting<Boolean> random = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("Random"))
                .description(":P"))
                .defaultValue(false))
                .build()
        );
    private final Setting<Avatar> avatar = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) new meteordevelopment.meteorclient.settings.EnumSetting.Builder()
                .name("Avatar"))
                .description("."))
                .defaultValue(Avatar.acid_1))
                .visible(() -> !this.random.get()))
                .build()
        );
    private final Setting<SettingColor> bg1 = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                .name("bg-color-1"))
                .description("Color for flat color mode."))
                .defaultValue(new SettingColor(225, 25, 25))
                .build()
        );
    private final Setting<SettingColor> bg2 = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                .name("bg-color-2"))
                .description("Color for flat color mode."))
                .defaultValue(new SettingColor(225, 25, 25))
                .build()
        );
    private final Setting<SettingColor> bg3 = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                .name("bg-color-3"))
                .description("Color for flat color mode."))
                .defaultValue(new SettingColor(225, 25, 25))
                .build()
        );
    private final Setting<SettingColor> bg4 = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                .name("bg-color-4"))
                .description("Color for flat color mode."))
                .defaultValue(new SettingColor(225, 25, 25))
                .build()
        );
    private final Setting<SettingColor> bg5 = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                .name("bg-color-5"))
                .description("Color for flat color mode."))
                .defaultValue(new SettingColor(225, 25, 25))
                .build()
        );
    private final Setting<Integer> height2 = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("height2"))
                .description(""))
                .defaultValue(0))
                .sliderMin(0)
                .sliderMax(1000)
                .build()
        );
    private final Setting<Integer> width2 = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("width2"))
                .description(""))
                .defaultValue(0))
                .sliderMin(0)
                .sliderMax(1000)
                .build()
        );
    private final Setting<Integer> infoStringY = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("infoStringY"))
                .description(""))
                .defaultValue(0))
                .sliderMin(0)
                .sliderMax(1000)
                .build()
        );
    private final Setting<Double> infoScale = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("infoScale"))
                .description(""))
                .defaultValue(1.0)
                .sliderMin(0.0)
                .sliderMax(2.0)
                .build()
        );
    private final Setting<Integer> infoX = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("infoOffsetX"))
                .description(""))
                .defaultValue(0))
                .sliderMin(0)
                .sliderMax(1000)
                .build()
        );
    private final Setting<Integer> infoY = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("infoOffsetY"))
                .description(""))
                .defaultValue(0))
                .sliderMin(0)
                .sliderMax(1000)
                .build()
        );
    private final Setting<Boolean> average = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("average"))
                .description(""))
                .defaultValue(true))
                .build()
        );
    private final Setting<Boolean> bps = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("bps"))
                .description(""))
                .defaultValue(false))
                .build()
        );
    private final Setting<SettingColor> dp1 = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                .name("double-color-1"))
                .description("Color for flat color mode."))
                .defaultValue(new SettingColor(225, 25, 25))
                .build()
        );
    private final Setting<SettingColor> dp2 = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                .name("double-color-2"))
                .description("Color for flat color mode."))
                .defaultValue(new SettingColor(225, 25, 25))
                .build()
        );
    private final Setting<Double> rainbowSpeed = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("rainbow-speed"))
                .description("Rainbow speed of rainbow color mode."))
                .defaultValue(0.05)
                .sliderMin(0.01)
                .sliderMax(0.2)
                .decimalPlaces(4)
                .build()
        );
    private final Color rainbow = new Color(255, 255, 255);
    private final List<Double> speedList = new ArrayList<>();
    private final ArrayDeque<Float> speedResult = new ArrayDeque<>(20);    public static final HudElementInfo<AccountInfo> INFO = new HudElementInfo(
        dev.abstr3act.addon.Compassion.HUD_GROUP, "AccountInfo", "ClientBoard", AccountInfo::new
    );
    public float currentPlayerSpeed;
    public float averagePlayerSpeed;
    private Identifier identifier = Chars.a1;
    private int lastTick = -1;
    private float animation1;
    private float animation2;
    private double rainbowHue1;
    public AccountInfo() {
        super(INFO);
        MeteorClient.EVENT_BUS.subscribe(this);
    }

    private Avatar getRandom() {
        int pick = new Random().nextInt(Avatar.values().length);
        return Avatar.values()[pick];
    }

    public void draw(MatrixStack stack, int x, int y, Identifier identifier) {
        Render2DEngine.startAntiAtlas();
        Render2DEngine.doAntiAtlas();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
        GL13.glEnable(32925);
        RenderSystem.setShaderTexture(0, identifier);
        Render2DEngine.renderTextureX(
            stack, x, y, 256.0 * this.iconScale.get(), 256.0 * this.iconScale.get(), 0.0F, 0.0F, 1024.0, 1024.0, 1024.0, 1024.0
        );
        GL13.glDisable(32925);
        RenderSystem.disableBlend();
        RenderSystem.disableDepthTest();
        Render2DEngine.stopAntiAtlas();
    }

    public void drawInfoString(float scale, int x, int y, int space, Color color) {
        TextRenderer.get().begin(scale);
        TextRenderer.get().render("X: " + MathUtility.round(MeteorClient.mc.player.getX()), x, y, color);
        int y1 = y + space;
        TextRenderer.get().render("Y: " + MathUtility.round(MeteorClient.mc.player.getY()), x, y1, color);
        y1 += space;
        TextRenderer.get().render("Z: " + MathUtility.round(MeteorClient.mc.player.getZ()), x, y1, color);
        y1 += space;
        TextRenderer.get().render("Ping: " + PlayerUtils.getPing() + " ms", x, y1, color);
        y1 += space;
        TextRenderer.get()
            .render("Speed: " + (this.bps.get() ? MathUtility.round(this.getSpeedMpS()) + " b/s" : MathUtility.round(this.getSpeedKpH()) + " km/h"), x, y1, color);
        TextRenderer.get().end();
    }

    @EventHandler
    public void onGameJoin(GameJoinedEvent event) {
        this.identifier = this.getRandom().getIdentifier();
    }

    public void render(HudRenderer renderer) {
        if (!BaseModule.fullNullCheck()) {
            long m = Runtime.getRuntime().maxMemory();
            long t = Runtime.getRuntime().totalMemory();
            long f = Runtime.getRuntime().freeMemory();
            long o = t - f;
            this.setSize((this.sizeX.get()).floatValue(), (this.sizeY.get()).floatValue());
            int x1 = this.x;
            int y1 = this.y;
            this.rainbowHue1 = this.rainbowHue1 + this.rainbowSpeed.get() * renderer.delta;
            if (this.rainbowHue1 > 1.0) {
                this.rainbowHue1--;
            } else if (this.rainbowHue1 < 0.0) {
                this.rainbowHue1++;
            }

            double progress = Math.sin((this.rainbowHue1 + 1.0) * Math.PI * 2.0);
            progress = (progress + 1.0) / 2.0;
            Color redColor = (Color) this.dp1.get();
            Color whiteColor = (Color) this.dp2.get();
            int red = (int) (redColor.r * (1.0 - progress) + whiteColor.r * progress);
            int green = (int) (redColor.g * (1.0 - progress) + whiteColor.g * progress);
            int blue = (int) (redColor.b * (1.0 - progress) + whiteColor.b * progress);
            this.rainbow.set(red, green, blue, 255);
            Render2DEngine.drawRoundedBlur(
                renderer.drawContext.getMatrices(),
                x1,
                y1,
                (this.sizeX.get()).floatValue(),
                (this.sizeY.get()).floatValue(),
                50.0F,
                new java.awt.Color(((SettingColor) this.bg1.get()).getPacked())
            );
            this.draw(
                renderer.drawContext.getMatrices(),
                x1 + this.offsetX.get(),
                y1 + this.offsetY.get(),
                this.random.get() ? this.identifier : ((Avatar) this.avatar.get()).getIdentifier()
            );
            x1 += this.ToffsetX.get();
            TextRenderer.get().begin(this.textScale.get());
            String name = ((NameProtect) Modules.get().get(NameProtect.class)).isActive()
                ? ((NameProtect) Modules.get().get(NameProtect.class)).getName(MeteorClient.mc.player.getName().getString())
                : MeteorClient.mc.player.getName().getString();
            TextRenderer.get().render(name, x1, y1, Color.WHITE);
            x1 += (int) TextRenderer.get().getWidth(name) + this.ToffsetX2.get();
            TextRenderer.get().render((String) this.prefix.get(), x1, y1, this.rainbow);
            TextRenderer.get().render((String) this.str.get() + (String) this.sux.get(), x1 + TextRenderer.get().getWidth((String) this.prefix.get()), y1, Color.WHITE);
            x1 -= (int) TextRenderer.get().getWidth(name) + this.ToffsetX2.get();
            y1 += this.ToffsetY.get();
            TextRenderer.get().end();
            this.updateSpeedList();
            this.drawGraph(renderer, x1 + this.gX.get(), y1 + this.gY.get());
            this.animation1 = AnimationUtility.fast(this.animation1, MeteorClient.mc.player.getAttackCooldownProgress(0.5F), 50.0F);
            this.animation2 = AnimationUtility.fast(this.animation2, 1.0F - MeteorClient.mc.player.hurtTime / 10.0F, 50.0F);
            renderer.quad(
                x1 + 30.0F + (this.aX.get()).intValue(),
                y1 + 20.0F + (this.aY.get()).intValue(),
                65.0F * this.animation1 * this.cooldownFactor.get(),
                5.0,
                (Color) this.bg4.get()
            );
            renderer.quad(
                x1 + 30.0F + (this.aX.get()).intValue(),
                y1 + 20.0F + (this.aY.get()).intValue() + (this.aY2.get()).intValue(),
                65.0F * this.animation2 * this.cooldownFactor.get(),
                5.0,
                (Color) this.bg5.get()
            );
            this.drawInfoString(
                (this.infoScale.get()).floatValue(),
                x1 + this.infoX.get(),
                y1 + this.infoY.get(),
                this.infoStringY.get(),
                Color.WHITE
            );
        }
    }

    private void updateSpeedList() {
        this.currentPlayerSpeed = (float) Math.hypot(
            MeteorClient.mc.player.getX() - MeteorClient.mc.player.prevX, MeteorClient.mc.player.getZ() - MeteorClient.mc.player.prevZ
        );
        if (this.speedResult.size() > 20) {
            this.speedResult.poll();
        }

        this.speedResult.add(this.currentPlayerSpeed);
        float average = 0.0F;

        for (Float value : this.speedResult) {
            average += MathUtility.clamp(value, 0.0F, 20.0F);
        }

        this.averagePlayerSpeed = average / this.speedResult.size();
        if (this.lastTick != MeteorClient.mc.player.age) {
            this.lastTick = MeteorClient.mc.player.age;
            double z2 = MeteorClient.mc.player.getZ();
            double z1 = MeteorClient.mc.player.prevZ;
            double x2 = MeteorClient.mc.player.getX();
            double x1 = MeteorClient.mc.player.prevX;
            double speed = Math.sqrt((z2 - z1) * (z2 - z1) + (x2 - x1) * (x2 - x1));
            this.speedList.add(speed);

            while (this.speedList.size() > this.width.get()) {
                this.speedList.remove(0);
            }
        }
    }

    private void drawGraph(HudRenderer context, int x, int y) {
        context.quad(
            x - this.width2.get(),
            y - this.height2.get(),
            this.width.get() + this.width2.get(),
            this.height.get() + this.height2.get(),
            (Color) this.bg3.get()
        );
        int size = this.speedList.size();
        int start = Math.max(0, size - this.width.get());

        for (int i = start; i < size - 1; i++) {
            double yVal = this.speedList.get(i) * 10.0 * this.yMultiplier.get();
            double yVal1 = this.speedList.get(i + 1) * 10.0 * this.yMultiplier.get();
            int x1 = x + Math.min((int) ((i - start) * this.xMultiplier.get()), this.width.get() - 1);
            int y1 = y + this.height.get() + 1 - (int) Math.min(yVal, (this.height.get()).intValue());
            int x2 = x + Math.min((int) ((i + 1 - start) * this.xMultiplier.get()), this.width.get() - 1);
            int y2 = y + this.height.get() + 1 - (int) Math.min(yVal1, (this.height.get()).intValue());
            context.line(x1, y1, x2, y2, (Color) this.bg2.get());
        }
    }

    private long toMiB(long bytes) {
        return bytes / 1024L / 1024L;
    }

    public float getSpeedKpH() {
        return (this.average.get() ? this.averagePlayerSpeed : this.currentPlayerSpeed) * 72.0F;
    }

    public float getSpeedMpS() {
        return (this.average.get() ? this.averagePlayerSpeed : this.currentPlayerSpeed) * 20.0F;
    }

    static enum Avatar {
        acid_1(Chars.a1, "捏捏Acid"),
        acid_2(Chars.a2, "可爱Acid"),
        acid_3(Chars.a3, "哭哭Acid"),
        acid_4(Chars.a4, "羞涩Acid"),
        acid_5(Chars.a4, "发电Acid");

        private final String name;
        private final Identifier icons;

        private Avatar(Identifier icons, String name) {
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
