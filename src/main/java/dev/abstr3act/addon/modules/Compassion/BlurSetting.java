package dev.abstr3act.addon.modules.Compassion;

import dev.abstr3act.addon.Compassion;
import dev.abstr3act.addon.module.CompassionModule;
import meteordevelopment.meteorclient.settings.IntSetting.Builder;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;

public class BlurSetting extends CompassionModule {
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
    public final Setting<Integer> radius = this.sgGeneral
        .add(((Builder) ((Builder) ((Builder) new Builder().name("radius")).description(".")).defaultValue(0)).sliderRange(0, 50).build());
    public final Setting<Double> blurOpacity = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("blurOpacity"))
                .description("."))
                .defaultValue(0.55F)
                .sliderMin(0.0)
                .sliderMax(1.0)
                .build()
        );
    public final Setting<Double> blurStrength = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("blurStrength"))
                .description("."))
                .defaultValue(20.0)
                .sliderMin(5.0)
                .sliderMax(50.0)
                .build()
        );
    public final Setting<SettingColor> shadowColor = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                .name("shadowColor"))
                .description("."))
                .defaultValue(new Color(0, 0, 0))
                .build()
        );
    public final Setting<Double> shadowRadius = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("shadowRadius"))
                .description("."))
                .defaultValue(20.0)
                .sliderMin(5.0)
                .sliderMax(50.0)
                .build()
        );

    public BlurSetting() {
        super(Compassion.COMPASSION, "BlurSetting", ".");
    }

    public void onActivate() {
        this.toggle();
    }
}
