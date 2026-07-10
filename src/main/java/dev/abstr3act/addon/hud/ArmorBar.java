package dev.abstr3act.addon.hud;

import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.settings.IntSetting.Builder;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;

public class ArmorBar extends HudElement {
    public static ArmorBar INSTANCE;    public static final HudElementInfo<ArmorBar> INFO = new HudElementInfo(dev.abstr3act.addon.Compassion.HUD_GROUP, "ArmorBar", ".", ArmorBar::new);
    public final SettingGroup sgGeneral = this.settings.getDefaultGroup();
    private final Setting<Integer> sizeX = this.sgGeneral
        .add(((Builder) ((Builder) ((Builder) new Builder().name("sizeX")).description("")).defaultValue(100)).sliderMin(-1000).sliderMax(1000).build());
    private final Setting<Integer> sizeY = this.sgGeneral
        .add(((Builder) ((Builder) ((Builder) new Builder().name("sizeY")).description("")).defaultValue(15)).sliderMin(-1000).sliderMax(1000).build());
    private final Setting<SettingColor> bgColor = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                .name("BGColor"))
                .description(""))
                .defaultValue(new Color(0, 0, 0, 60))
                .build()
        );
    private final Setting<SettingColor> barColor = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                .name("barColor"))
                .description(""))
                .defaultValue(new Color(255, 255, 255, 255))
                .build()
        );
    public ArmorBar() {
        super(INFO);
        INSTANCE = this;
    }

    public void render(HudRenderer renderer) {
        this.setSize((this.sizeX.get()).intValue(), (this.sizeY.get()).intValue());
        if (MeteorClient.mc.player != null) {
            float health = 20.0F;
            float maxHealth = MeteorClient.mc.player.getArmor();
            float healthRatio = health / maxHealth;
            float adjustedWidth = (this.sizeX.get()).intValue() * healthRatio;
            renderer.drawContext.getMatrices().push();
            renderer.quad(this.x, this.y, (this.sizeX.get()).intValue(), (this.sizeY.get()).intValue(), (Color) this.bgColor.get());
            renderer.quad(this.x, this.y, adjustedWidth, (this.sizeY.get()).intValue(), (Color) this.barColor.get());
            renderer.drawContext.getMatrices().pop();
        }
    }


}
