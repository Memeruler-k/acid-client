package dev.abstr3act.addon.hud;

import dev.abstr3act.addon.utils.render.Render2DEngine;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.world.TickEvent.Pre;
import meteordevelopment.meteorclient.renderer.text.TextRenderer;
import meteordevelopment.meteorclient.settings.DoubleSetting.Builder;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BowItem;

public class CooldownHUD extends HudElement {
    private static final int MAX_DRAW_TIME = 20;
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
    private final Setting<Double> scale = this.sgGeneral
        .add(((Builder) ((Builder) new Builder().name("scale")).description("The scale.")).defaultValue(2.0).min(1.0).sliderRange(1.0, 5.0).build());
    private final Setting<Integer> x1 = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("x"))
                .description("."))
                .defaultValue(0))
                .sliderRange(-100, 100)
                .build()
        );
    private final Setting<Integer> y1 = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("y"))
                .description("."))
                .defaultValue(0))
                .sliderRange(-100, 100)
                .build()
        );
    private final Setting<Integer> x2 = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("x2"))
                .description("."))
                .defaultValue(0))
                .sliderRange(-100, 100)
                .build()
        );
    private final Setting<Integer> y2 = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("y2"))
                .description("."))
                .defaultValue(0))
                .sliderRange(-100, 100)
                .build()
        );
    private final Setting<Integer> x3 = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("x3"))
                .description("."))
                .defaultValue(0))
                .sliderRange(-100, 100)
                .build()
        );
    private final Setting<Integer> y3 = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("y3"))
                .description("."))
                .defaultValue(0))
                .sliderRange(-100, 100)
                .build()
        );
    private final Setting<Integer> x4 = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("x4"))
                .description("."))
                .defaultValue(0))
                .sliderRange(-100, 100)
                .build()
        );
    private final Setting<Integer> y4 = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("y4"))
                .description("."))
                .defaultValue(0))
                .sliderRange(-100, 100)
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
        );    public static final HudElementInfo<CooldownHUD> INFO = new HudElementInfo(
        dev.abstr3act.addon.Compassion.HUD_GROUP, "CoolDown", "It's a Cat girl what do you want", CooldownHUD::new
    );
    private final Color rainbow = new Color(255, 255, 255);
    float cooldown = 0.0F;
    private double rainbowHue2;
    public CooldownHUD() {
        super(INFO);
        MeteorClient.EVENT_BUS.subscribe(this);
    }

    public static float getBowChargeProgress(PlayerEntity player) {
        if (player.isUsingItem() && player.getActiveItem().getItem() instanceof BowItem) {
            int useTime = player.getItemUseTime();
            float chargeProgress = Math.min((20 - useTime) / 20.0F, 1.0F);
            return Math.max(chargeProgress, 0.0F);
        } else {
            return 0.0F;
        }
    }

    @EventHandler
    private void onTickEvent(Pre event) {
    }

    public void render(HudRenderer renderer) {
        this.setSize(10.0, 10.0);
        if (MeteorClient.mc.player != null && MeteorClient.mc.player.isUsingItem() && MeteorClient.mc.player.getActiveItem().getItem() instanceof BowItem) {
            this.cooldown = 1.0F - getBowChargeProgress(MeteorClient.mc.player);
        } else if (MeteorClient.mc.player != null
            && MeteorClient.mc.player.getItemCooldownManager().isCoolingDown(MeteorClient.mc.player.getMainHandStack().getItem())) {
            this.cooldown = 1.0F - MeteorClient.mc.player.getItemCooldownManager().getCooldownProgress(MeteorClient.mc.player.getMainHandStack().getItem(), 0.0F);
        } else {
            if (MeteorClient.mc.player == null) {
                this.cooldown = 0.5F;
                return;
            }

            this.cooldown = MeteorClient.mc.player.getAttackCooldownProgress(0.5F);
        }

        float r = this.cooldown * 100.0F;
        String timeText = String.format("%.1f", r) + "%";
        Render2DEngine.drawRoundedBlur(
            renderer.drawContext.getMatrices(), this.x, this.y, 200 + this.x3.get(), 4 + this.y3.get(), 50.0F, java.awt.Color.BLACK
        );
        renderer.quad(
            this.x + this.x2.get(), this.y + this.y2.get(), (200 + this.x4.get()) * this.cooldown, 4 + this.y4.get(), Color.WHITE
        );
        TextRenderer.get().begin(this.scale.get());
        TextRenderer.get()
            .render(timeText, this.x + this.x1.get() - TextRenderer.get().getWidth(timeText) / 2.0, this.y + this.y1.get(), Color.WHITE, false);
        TextRenderer.get().end();
    }


}
