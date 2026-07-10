package dev.abstr3act.addon.hud;

import meteordevelopment.meteorclient.settings.IntSetting.Builder;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;

public class ScoreboardHud extends NewHudElement {
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();    public static final HudElementInfo<ScoreboardHud> INFO = new HudElementInfo(
        dev.abstr3act.addon.Compassion.HUD_GROUP, "ScoreboardHud", " ", ScoreboardHud::new
    );
    public final Setting<Integer> offsetX = this.sgGeneral
        .add(((Builder) ((Builder) ((Builder) new Builder().name("OffsetX")).description("")).defaultValue(0)).sliderRange(-1000, 1000).build());
    public ScoreboardHud() {
        super(INFO);
    }

    public void render(HudRenderer renderer) {
        this.setSize(100.0, 100.0);
    }


}
