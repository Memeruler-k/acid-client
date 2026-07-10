package dev.abstr3act.addon.hud;

import dev.abstr3act.addon.font.FontRenderers;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.settings.IntSetting.Builder;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;

import java.awt.*;

public class FontDebugger extends HudElement {
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
    private final Setting<Integer> space = this.sgGeneral
        .add(((Builder) ((Builder) ((Builder) new Builder().name("space")).description("The scale.")).defaultValue(10)).min(1).sliderRange(1, 100).build());
    public FontDebugger() {
        super(INFO);
        MeteorClient.EVENT_BUS.subscribe(this);
    }    public static final HudElementInfo<FontDebugger> INFO = new HudElementInfo(
        dev.abstr3act.addon.Compassion.HUD_GROUP, "FontDebugger", "Arcaea", FontDebugger::new
    );

    public void render(HudRenderer renderer) {
        this.setSize(100.0, 100.0);
        renderer.post(() -> {
            int x1 = this.getX();
            int y1 = this.getY();
            renderer.drawContext.getMatrices().push();
            y1 += this.space.get();
            FontRenderers.exo_regular_16.drawStringFix(renderer.drawContext.getMatrices(), "exo_regular_16", x1, y1, Color.WHITE);
            y1 += this.space.get();
            FontRenderers.exo_regular_32.drawStringFix(renderer.drawContext.getMatrices(), "exo_regular_32", x1, y1, Color.WHITE);
            y1 += this.space.get();
            FontRenderers.exo_light_16.drawStringFix(renderer.drawContext.getMatrices(), "exo_light_16", x1, y1, Color.WHITE);
            y1 += this.space.get();
            FontRenderers.exo_light_32.drawStringFix(renderer.drawContext.getMatrices(), "exo_light_32", x1, y1, Color.WHITE);
            y1 += this.space.get();
            FontRenderers.exo_medium_16.drawStringFix(renderer.drawContext.getMatrices(), "exo_medium_16", x1, y1, Color.WHITE);
            y1 += this.space.get();
            FontRenderers.exo_medium_32.drawStringFix(renderer.drawContext.getMatrices(), "exo_medium_32", x1, y1, Color.WHITE);
            y1 += this.space.get();
            FontRenderers.exo_semibold_16.drawStringFix(renderer.drawContext.getMatrices(), "exo_semibold_16", x1, y1, Color.WHITE);
            y1 += this.space.get();
            FontRenderers.exo_semibold_32.drawStringFix(renderer.drawContext.getMatrices(), "exo_semibold_32", x1, y1, Color.WHITE);
            y1 += this.space.get();
            FontRenderers.exo_regular_16.drawStringFix(renderer.drawContext.getMatrices(), "exo_regular_16", x1, y1, Color.WHITE);
            y1 += this.space.get();
            FontRenderers.exo_regular_16.drawStringFix(renderer.drawContext.getMatrices(), "exo_regular_16", x1, y1, Color.WHITE);
            y1 += this.space.get();
            FontRenderers.geosans_light_oblique_16.drawStringFix(renderer.drawContext.getMatrices(), "geosans_light_oblique_16", x1, y1, Color.WHITE);
            y1 += this.space.get();
            FontRenderers.geosans_light_oblique_32.drawStringFix(renderer.drawContext.getMatrices(), "geosans_light_oblique_32", x1, y1, Color.WHITE);
            y1 += this.space.get();
            FontRenderers.geosans_light_16.drawStringFix(renderer.drawContext.getMatrices(), "geosans_light_16", x1, y1, Color.WHITE);
            y1 += this.space.get();
            FontRenderers.geosans_light_32.drawStringFix(renderer.drawContext.getMatrices(), "geosans_light_32", x1, y1, Color.WHITE);
            renderer.drawContext.getMatrices().pop();
        });
    }


}
