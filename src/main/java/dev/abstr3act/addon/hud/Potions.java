package dev.abstr3act.addon.hud;

import dev.abstr3act.addon.font.FontRenderers;
import dev.abstr3act.addon.utils.render.Render2DEngine;
import dev.abstr3act.addon.utils.render.shaders.AnimationUtility;
import kotlin.Pair;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.settings.ColorSetting.Builder;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.util.Formatting;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class Potions extends HudElement {
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
    private final Setting<SettingColor> bg1 = this.sgGeneral
        .add(((Builder) ((Builder) new Builder().name("bg-color-1")).description("Color for flat color mode.")).defaultValue(new SettingColor(225, 25, 25)).build());
    private final Setting<SettingColor> bg2 = this.sgGeneral
        .add(((Builder) ((Builder) new Builder().name("bg-color-2")).description("Color for flat color mode.")).defaultValue(new SettingColor(225, 25, 25)).build());
    private final Setting<SettingColor> dp1 = this.sgGeneral
        .add(
            ((Builder) ((Builder) new Builder().name("double-color-1")).description("Color for flat color mode.")).defaultValue(new SettingColor(225, 25, 25)).build()
        );
    private final Setting<SettingColor> dp2 = this.sgGeneral
        .add(
            ((Builder) ((Builder) new Builder().name("double-color-2")).description("Color for flat color mode.")).defaultValue(new SettingColor(225, 25, 25)).build()
        );
    private final Setting<SettingColor> dp3 = this.sgGeneral
        .add(((Builder) ((Builder) new Builder().name("bg-color-1a")).description("Color for flat color mode.")).defaultValue(new SettingColor(225, 25, 25)).build());
    private final Setting<SettingColor> dp4 = this.sgGeneral
        .add(((Builder) ((Builder) new Builder().name("bg-color-2a")).description("Color for flat color mode.")).defaultValue(new SettingColor(225, 25, 25)).build());
    private final Setting<Integer> size = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("Size"))
                .defaultValue(32))
                .min(1)
                .sliderRange(1, 800)
                .build()
        );
    private final Setting<Boolean> potionColor = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("potionColor"))
                .defaultValue(true))
                .build()
        );
    private final Setting<Integer> y_offset_1 = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("y_offset_1"))
                .defaultValue(18))
                .sliderRange(-1000, 1000)
                .build()
        );
    private final Setting<Integer> y_offset_2 = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("y_offset_2"))
                .defaultValue(8))
                .sliderRange(-1000, 1000)
                .build()
        );
    private final Setting<Integer> x_offset_1 = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("x_offset_1"))
                .defaultValue(0))
                .sliderRange(-1000, 1000)
                .build()
        );
    private final Setting<Integer> x_offset_3 = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("x_offset_3"))
                .defaultValue(0))
                .sliderRange(-1000, 1000)
                .build()
        );
    private final Setting<Integer> y_offset_3 = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("y_offset_3"))
                .defaultValue(0))
                .sliderRange(-1000, 1000)
                .build()
        );
    private final Setting<Integer> max_width = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("max_width"))
                .defaultValue(50))
                .sliderRange(-1000, 1000)
                .build()
        );
    private final Setting<Double> factor_1 = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder().name("factor_1"))
                .defaultValue(1.4)
                .sliderRange(-5.0, 5.0)
                .build()
        );
    private final Setting<Double> factor_2 = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder().name("factor_2"))
                .defaultValue(5.0)
                .sliderRange(-50.0, 50.0)
                .build()
        );
    private final Setting<Integer> var_1 = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("var_1"))
                .defaultValue(3))
                .sliderRange(-1000, 1000)
                .build()
        );
    private final Setting<Integer> var_2 = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("var_2"))
                .defaultValue(13))
                .sliderRange(-1000, 1000)
                .build()
        );
    private final Setting<Integer> var_3 = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("var_3"))
                .defaultValue(38))
                .sliderRange(-1000, 1000)
                .build()
        );
    private final Setting<Integer> var_4 = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("var_4"))
                .defaultValue(0))
                .sliderRange(-1000, 1000)
                .build()
        );
    private final Setting<Integer> alpha = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("alpha"))
                .defaultValue(0))
                .sliderRange(0, 255)
                .build()
        );
    private final Setting<Integer> var_6 = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("var_6"))
                .defaultValue(0))
                .sliderRange(-1000, 1000)
                .build()
        );
    private final Setting<Integer> var_7 = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("var_7"))
                .defaultValue(0))
                .sliderRange(-1000, 1000)
                .build()
        );
    private final Setting<Integer> var_8 = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("var_8"))
                .defaultValue(0))
                .sliderRange(-1000, 1000)
                .build()
        );
    private final Setting<Integer> var_9 = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("var_9"))
                .defaultValue(0))
                .sliderRange(-1000, 1000)
                .build()
        );
    private final Setting<Integer> var_10 = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("var_10"))
                .defaultValue(0))
                .sliderRange(-1000, 1000)
                .build()
        );
    private final Setting<Integer> var_11 = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("var_11"))
                .defaultValue(0))
                .sliderRange(-1000, 1000)
                .build()
        );
    private final Setting<Integer> var_12 = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("var_12"))
                .defaultValue(0))
                .sliderRange(-1000, 1000)
                .build()
        );
    private final Setting<Integer> var_13 = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("var_13"))
                .defaultValue(0))
                .sliderRange(-1000, 1000)
                .build()
        );
    private final Setting<Integer> var_14 = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("var_14"))
                .defaultValue(0))
                .sliderRange(-1000, 1000)
                .build()
        );
    private final Setting<Integer> maxEffectDuration = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("maxEffectDuration"))
                .defaultValue(1200))
                .sliderRange(0, 1000)
                .build()
        );
    private final Map<StatusEffect, Pair<Float, Float>> potionPositions = new HashMap<>();    public static final HudElementInfo<Potions> INFO = new HudElementInfo(dev.abstr3act.addon.Compassion.HUD_GROUP, "Potions", "Potions", Potions::new);
    float maxEffectDurationV;
    private float vAnimation;
    private float hAnimation;
    private float vAnimation2;
    private float hAnimation2;
    private float vAnimation3;
    private float hAnimation3;
    private Map<StatusEffect, Integer> potionMaxDurations = new HashMap<>();
    private Map<StatusEffectInstance, Float> potionAnimations = new HashMap<>();
    public Potions() {
        super(INFO);
        MeteorClient.EVENT_BUS.subscribe(this);
    }

    public static String getDuration(StatusEffectInstance pe) {
        if (pe.isInfinite()) {
            return "*:*";
        } else {
            int var1 = pe.getDuration();
            int mins = var1 / 1200;
            String sec = String.format("%02d", var1 % 1200 / 20);
            return mins + ":" + sec;
        }
    }

    public Color getColor(StatusEffect effect, int alpha) {
        if (this.potionColor.get()) {
            if (effect == StatusEffects.HEALTH_BOOST.value()) {
                return new Color(255, 99, 99, alpha);
            }

            if (effect == StatusEffects.INSTANT_HEALTH.value()) {
                return new Color(255, 99, 99, alpha);
            }

            if (effect == StatusEffects.REGENERATION.value()) {
                return new Color(255, 99, 99, alpha);
            }

            if (effect == StatusEffects.HASTE.value()) {
                return new Color(255, 229, 99, alpha);
            }

            if (effect == StatusEffects.ABSORPTION.value()) {
                return new Color(255, 229, 99, alpha);
            }

            if (effect == StatusEffects.STRENGTH.value()) {
                return new Color(147, 66, 66, alpha);
            }

            if (effect == StatusEffects.RESISTANCE.value()) {
                return new Color(102, 172, 227, alpha);
            }

            if (effect == StatusEffects.POISON.value()) {
                return new Color(107, 255, 116, alpha);
            }

            if (effect == StatusEffects.SLOWNESS.value()) {
                return new Color(96, 117, 134, alpha);
            }

            if (effect == StatusEffects.SLOW_FALLING.value()) {
                return new Color(204, 190, 171, alpha);
            }

            if (effect == StatusEffects.WATER_BREATHING.value()) {
                return new Color(170, 220, 255, alpha);
            }

            if (effect == StatusEffects.FIRE_RESISTANCE.value()) {
                return new Color(255, 180, 135, alpha);
            }

            if (effect == StatusEffects.SPEED.value()) {
                return new Color(111, 238, 255, alpha);
            }
        }

        return new Color(255, 255, 255, alpha);
    }

    public void render(HudRenderer renderer) {
        DrawContext context = renderer.drawContext;
        this.setSize(100.0, 100.0);
        renderer.post(
            () -> {
                int y_offset1 = 0;
                float max_width = (this.max_width.get()).intValue();
                float pointerX = 0.0F;

                for (StatusEffectInstance potionEffect : MeteorClient.mc.player.getStatusEffects()) {
                    if (y_offset1 == 0) {
                        y_offset1 += this.y_offset_2.get();
                    }

                    y_offset1 += this.y_offset_1.get();
                    float timeWidth = FontRenderers.sf_bold_mini.getStringWidth(getDuration(potionEffect));
                    if (timeWidth > pointerX) {
                        pointerX = timeWidth;
                    }
                }

                this.vAnimation = AnimationUtility.ease(this.vAnimation, 28 + y_offset1, (this.factor_2.get()).floatValue());
                this.hAnimation = AnimationUtility.ease(this.hAnimation, max_width, (this.factor_2.get()).floatValue());
                renderer.drawContext.getMatrices().push();
                double x = this.x;
                double y = this.y;
                Set<StatusEffect> activeEffects = MeteorClient.mc
                    .player
                    .getStatusEffects()
                    .stream()
                    .map(effectx -> (StatusEffect) effectx.getEffectType().value())
                    .collect(Collectors.toSet());
                List<StatusEffect> toRemove = new ArrayList<>();

                for (StatusEffect potion : this.potionPositions.keySet()) {
                    if (!activeEffects.contains(potion)) {
                        toRemove.add(potion);
                    }
                }

                toRemove.forEach(this.potionPositions::remove);

                for (StatusEffectInstance effectInstance : MeteorClient.mc.player.getStatusEffects()) {
                    StatusEffect effect = (StatusEffect) effectInstance.getEffectType().value();
                    float targetX = (float) x;
                    float targetY = (float) y;
                    Pair<Float, Float> prevPos = this.potionPositions.getOrDefault(effect, new Pair(targetX - 50.0F, targetY));
                    float newX = AnimationUtility.ease((Float) prevPos.getFirst(), targetX, 5.0F);
                    float newY = AnimationUtility.ease((Float) prevPos.getSecond(), targetY, 5.0F);
                    this.potionPositions.put(effect, new Pair(newX, newY));
                    this.drawPotionEffect(context, newX, newY, effectInstance);
                    y += (this.var_14.get()).intValue();
                }
            }
        );
        renderer.drawContext.getMatrices().pop();
    }

    public void drawPotionEffect(DrawContext context, float x, float y, StatusEffectInstance potionEffect) {
        float max_width = (this.max_width.get()).intValue();
        StatusEffect potion = (StatusEffect) potionEffect.getEffectType().value();
        float px = x + (this.x_offset_1.get()).intValue();
        FontRenderers.sf_bold_mini
            .drawCenteredString(
                context.getMatrices(),
                "",
                px + (x + max_width - px) / 2.0F + (this.var_4.get()).intValue(),
                y + 38.0F + (this.var_10.get()).intValue(),
                Color.WHITE
            );
        Render2DEngine.drawRect(new MatrixStack(), 0.0F, 0.0F, 0.0F, 0.0F, new Color(-1));
        Render2DEngine.drawRoundedBlur2(
            new MatrixStack(),
            px + (this.var_7.get()).intValue(),
            y + (this.var_3.get()).intValue() + (this.var_8.get()).intValue(),
            max_width + (this.var_9.get()).intValue(),
            5 + this.var_6.get(),
            0.0F,
            new Color(((SettingColor) this.dp4.get()).getPacked(), true)
        );
        Render2DEngine.drawRect(
            new MatrixStack(),
            px + (this.var_7.get()).intValue(),
            y + (this.var_3.get()).intValue() + (this.var_8.get()).intValue(),
            5.0F,
            (float) (5 + this.var_6.get()),
            this.getColor(potion, 255)
        );
        FontRenderers.sf_bold_mini
            .drawStringFix(
                context.getMatrices(), potion.getName().getString() + " " + Formatting.WHITE + (potionEffect.getAmplifier() + 1), x + 24.0F, y + 38.0F, Color.WHITE
            );
        FontRenderers.sf_bold_mini
            .drawCenteredString(
                context.getMatrices(),
                getDuration(potionEffect),
                px + (x + max_width - px) / 2.0F + (this.var_4.get()).intValue(),
                y + 38.0F + (this.var_10.get()).intValue(),
                Color.WHITE
            );
        Render2DEngine.drawRect(new MatrixStack(), 0.0F, 0.0F, 0.0F, 0.0F, new Color(-1));
    }


}
