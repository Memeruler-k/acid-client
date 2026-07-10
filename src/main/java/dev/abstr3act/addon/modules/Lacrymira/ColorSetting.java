package dev.abstr3act.addon.modules.Lacrymira;

import dev.abstr3act.addon.Compassion;
import dev.abstr3act.addon.module.LacrymiraModule;
import dev.abstr3act.addon.utils.render.CaptureMark;
import meteordevelopment.meteorclient.settings.EnumSetting.Builder;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;

public class ColorSetting extends LacrymiraModule {
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
    public final Setting<CaptureMark.colorModeEn> mode = this.sgGeneral
        .add(((Builder) ((Builder) ((Builder) new Builder().name("Mode")).description("Mode")).defaultValue(CaptureMark.colorModeEn.Sky)).build());
    public final Setting<Integer> colorSpeed = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("colorSpeed"))
                .description("speed of color"))
                .defaultValue(8))
                .sliderMin(2)
                .sliderMax(54)
                .build()
        );
    public final Setting<SettingColor> hcolor1 = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                .name("Color"))
                .description("Color1"))
                .defaultValue(new Color(255, 255, 255))
                .build()
        );
    public final Setting<SettingColor> acolor = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                .name("Color2"))
                .description("Color2"))
                .defaultValue(new Color(255, 255, 255))
                .build()
        );

    public ColorSetting() {
        super(Compassion.CLIENT, "ColorSetting", "Setting");
    }
}
