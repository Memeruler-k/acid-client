package dev.abstr3act.addon.hud;

import dev.abstr3act.addon.utils.render.Render2DEngine;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.renderer.text.TextRenderer;
import meteordevelopment.meteorclient.settings.DoubleSetting.Builder;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.misc.NameProtect;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class Icon extends HudElement {
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
    public final Setting<String> prefix = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.StringSetting.Builder) ((meteordevelopment.meteorclient.settings.StringSetting.Builder) ((meteordevelopment.meteorclient.settings.StringSetting.Builder) new meteordevelopment.meteorclient.settings.StringSetting.Builder()
                .name("Prefix"))
                .description("."))
                .defaultValue("C"))
                .build()
        );
    public final Setting<String> string = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.StringSetting.Builder) ((meteordevelopment.meteorclient.settings.StringSetting.Builder) ((meteordevelopment.meteorclient.settings.StringSetting.Builder) new meteordevelopment.meteorclient.settings.StringSetting.Builder()
                .name("String"))
                .description("."))
                .defaultValue("ompassion "))
                .build()
        );
    public final Setting<String> sux = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.StringSetting.Builder) ((meteordevelopment.meteorclient.settings.StringSetting.Builder) ((meteordevelopment.meteorclient.settings.StringSetting.Builder) new meteordevelopment.meteorclient.settings.StringSetting.Builder()
                .name("Suffix"))
                .description("."))
                .defaultValue("V7 | "))
                .build()
        );
    private final Setting<Double> scale = this.sgGeneral
        .add(((Builder) ((Builder) new Builder().name("scale")).description("The scale.")).defaultValue(2.0).min(1.0).sliderRange(1.0, 5.0).build());
    private final Setting<Boolean> strip = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("strip"))
                .description("."))
                .defaultValue(false))
                .build()
        );
    private final Setting<Double> h = this.sgGeneral
        .add(
            ((Builder) ((Builder) new Builder().name("h")).description("How round the rectangles of the notifications are."))
                .defaultValue(0.0)
                .sliderRange(-100.0, 100.0)
                .build()
        );
    private final Setting<Double> w = this.sgGeneral
        .add(
            ((Builder) ((Builder) new Builder().name("w")).description("How round the rectangles of the notifications are."))
                .defaultValue(0.0)
                .sliderRange(-100.0, 100.0)
                .build()
        );
    private final Setting<Double> sx = this.sgGeneral
        .add(
            ((Builder) ((Builder) new Builder().name("sx")).description("How round the rectangles of the notifications are."))
                .defaultValue(0.0)
                .sliderRange(-100.0, 100.0)
                .build()
        );
    private final Setting<Double> sy = this.sgGeneral
        .add(
            ((Builder) ((Builder) new Builder().name("sy")).description("How round the rectangles of the notifications are."))
                .defaultValue(0.0)
                .sliderRange(-100.0, 100.0)
                .build()
        );
    private final Setting<Double> rainbowSpeed = this.sgGeneral
        .add(
            ((Builder) ((Builder) new Builder().name("rainbow-speed")).description("Rainbow speed of rainbow color mode."))
                .defaultValue(0.05)
                .sliderMin(0.01)
                .sliderMax(0.2)
                .decimalPlaces(4)
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
    private final Setting<Double> tx1 = this.sgGeneral
        .add(
            ((Builder) ((Builder) new Builder().name("tx1")).description("How round the rectangles of the notifications are."))
                .defaultValue(0.0)
                .sliderRange(-100.0, 100.0)
                .build()
        );
    private final Setting<Double> tx2 = this.sgGeneral
        .add(
            ((Builder) ((Builder) new Builder().name("tx2")).description("How round the rectangles of the notifications are."))
                .defaultValue(0.0)
                .sliderRange(-100.0, 100.0)
                .build()
        );
    private final Setting<Double> ty1 = this.sgGeneral
        .add(
            ((Builder) ((Builder) new Builder().name("ty1")).description("How round the rectangles of the notifications are."))
                .defaultValue(0.0)
                .sliderRange(-100.0, 100.0)
                .build()
        );
    private final Setting<Double> ty2 = this.sgGeneral
        .add(
            ((Builder) ((Builder) new Builder().name("ty2")).description("How round the rectangles of the notifications are."))
                .defaultValue(0.0)
                .sliderRange(-100.0, 100.0)
                .build()
        );
    private final Color rainbow = new Color(255, 255, 255);    public static final HudElementInfo<Icon> INFO = new HudElementInfo(
        dev.abstr3act.addon.Compassion.HUD_GROUP, "Icon", "It's a Cat girl what do you want", Icon::new
    );
    String greeting = "";
    String time = "";
    private double rainbowHue2;
    private double rainbowHue1;
    public Icon() {
        super(INFO);
    }

    public static String getGreeting(String name) {
        LocalTime now = LocalTime.now();
        int hour = now.getHour();
        if (hour >= 5 && hour < 12) {
            return String.format("Good morning %s", name) + "~";
        } else {
            return hour >= 12 && hour < 18 ? String.format("Good afternoon %s", name) + "~" : String.format("Good night %s", name) + "~";
        }
    }

    public void tick(HudRenderer renderer) {
        if (MeteorClient.mc.player != null) {
            this.greeting = getGreeting(
                ((NameProtect) Modules.get().get(NameProtect.class)).isActive()
                    ? ((NameProtect) Modules.get().get(NameProtect.class)).getName(MeteorClient.mc.player.getName().getString())
                    : MeteorClient.mc.player.getName().getString()
            );
            LocalTime time = LocalTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
            this.time = time.format(formatter);
        }
    }

    public void render(HudRenderer renderer) {
        String prefix = (String) this.prefix.get();
        String middle = (String) this.string.get();
        String suffix = (String) this.sux.get();
        String maximumLength = "00:00:00";
        String total = prefix + middle + suffix + maximumLength + this.greeting + " ";
        Render2DEngine.drawRoundedBlur(
            renderer.drawContext.getMatrices(),
            this.x + (this.tx1.get()).floatValue(),
            this.y + (this.ty1.get()).floatValue(),
            (float) (TextRenderer.get().getWidth(total) * this.scale.get()) + (this.tx2.get()).floatValue(),
            (float) (TextRenderer.get().getHeight() * this.scale.get()) + (this.ty2.get()).floatValue(),
            0.0F,
            java.awt.Color.BLACK
        );
        renderer.quad(
            this.x + (this.tx1.get()).floatValue() + this.sx.get(),
            this.y + (this.ty1.get()).floatValue() + this.sy.get(),
            (float) (TextRenderer.get().getWidth(total) * this.scale.get()) + (this.tx2.get()).floatValue() + this.w.get(),
            this.h.get(),
            Color.WHITE
        );
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
        int x = this.getX();
        int y = this.getY();
        renderer.post(() -> {
            TextRenderer.get().begin(this.scale.get(), false, true);
            TextRenderer.get().render(prefix, x, y, this.rainbow, true);
            TextRenderer.get().render(middle, x + TextRenderer.get().getWidth(prefix), this.getY(), new Color(255, 255, 255), true);
            TextRenderer.get().render(suffix, x + TextRenderer.get().getWidth(prefix + middle), this.getY(), Color.WHITE, true);
            TextRenderer.get().render(this.time, x + TextRenderer.get().getWidth(prefix + middle + suffix), this.getY(), Color.WHITE, true);
            TextRenderer.get().render(" | " + this.greeting, x + TextRenderer.get().getWidth(prefix + middle + suffix + this.time), this.getY(), Color.WHITE, true);
            this.setSize(TextRenderer.get().getWidth(total), 10.0 * this.scale.get());
            TextRenderer.get().end(null);
        });
    }


}
