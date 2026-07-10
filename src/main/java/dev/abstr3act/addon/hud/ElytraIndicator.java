package dev.abstr3act.addon.hud;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.abstr3act.addon.utils.render.Render2DEngine;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.settings.IntSetting.Builder;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import net.minecraft.item.ElytraItem;
import net.minecraft.util.Identifier;

public class ElytraIndicator extends HudElement {
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();    public static final HudElementInfo<ElytraIndicator> INFO = new HudElementInfo(
        dev.abstr3act.addon.Compassion.HUD_GROUP, "ElytraIndicator", "Displays information about your combat target.", ElytraIndicator::new
    );
    private final Setting<Integer> size = this.sgGeneral.add(((Builder) ((Builder) new Builder().name("size")).defaultValue(80)).min(10).max(300).build());
    private final Setting<SettingColor> color3 = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) new meteordevelopment.meteorclient.settings.ColorSetting.Builder().name("color"))
                .defaultValue(new Color(-979657829))
                .build()
        );
    public ElytraIndicator() {
        super(INFO);
    }

    public static float getTickDelta() {
        return MeteorClient.mc.getRenderTickCounter().getTickDelta(true);
    }

    public void render(HudRenderer renderer) {
        if (MeteorClient.mc.player.getInventory().getArmorStack(2).getItem() instanceof ElytraItem) {
            RenderSystem.setShaderTexture(0, Identifier.of("acid", "gui/elytra.png"));
            Render2DEngine.renderTexture(
                renderer.drawContext.getMatrices(),
                this.getX(),
                this.getY(),
                (this.size.get()).intValue(),
                (this.size.get()).intValue(),
                0.0F,
                0.0F,
                16.0,
                16.0,
                16.0,
                16.0
            );
        } else {
            RenderSystem.setShaderTexture(0, Identifier.of("acid", "gui/elytra_outline.png"));
            RenderSystem.setShaderColor(0.8F, 0.8F, 0.8F, 1.0F);
            Render2DEngine.renderTexture(
                renderer.drawContext.getMatrices(),
                this.getX(),
                this.getY(),
                (this.size.get()).intValue(),
                (this.size.get()).intValue(),
                0.0F,
                0.0F,
                16.0,
                16.0,
                16.0,
                16.0
            );
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        }

        this.setSize((this.size.get()).intValue(), (this.size.get()).intValue());
    }


}
