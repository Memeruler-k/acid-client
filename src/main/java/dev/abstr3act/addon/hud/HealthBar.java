package dev.abstr3act.addon.hud;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.abstr3act.addon.font.FontRenderers;
import dev.abstr3act.addon.utils.notifications.DrawUtils;
import dev.abstr3act.addon.utils.render.Render2DEngine;
import dev.abstr3act.addon.utils.render.shaders.AnimationUtility;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.settings.IntSetting.Builder;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import net.minecraft.client.gui.DrawContext;

public class HealthBar extends HudElement {
    public static HealthBar INSTANCE;    public static final HudElementInfo<HealthBar> INFO = new HudElementInfo(dev.abstr3act.addon.Compassion.HUD_GROUP, "HealthBar", ".", HealthBar::new);
    public final SettingGroup sgGeneral = this.settings.getDefaultGroup();
    public final Setting<Boolean> cancel = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("Cancel HUD"))
                .description(""))
                .defaultValue(true))
                .build()
        );
    private final Setting<Integer> sizeX = this.sgGeneral
        .add(((Builder) ((Builder) ((Builder) new Builder().name("sizeX")).description("")).defaultValue(100)).sliderMin(-1000).sliderMax(1000).build());
    private final Setting<Integer> sizeY = this.sgGeneral
        .add(((Builder) ((Builder) ((Builder) new Builder().name("sizeY")).description("")).defaultValue(15)).sliderMin(-1000).sliderMax(1000).build());
    private final Setting<Integer> oX = this.sgGeneral
        .add(((Builder) ((Builder) ((Builder) new Builder().name("oX")).description("")).defaultValue(0)).sliderMin(-1000).sliderMax(1000).build());
    private final Setting<Integer> oY = this.sgGeneral
        .add(((Builder) ((Builder) ((Builder) new Builder().name("oY")).description("")).defaultValue(0)).sliderMin(-1000).sliderMax(1000).build());
    private final Setting<Integer> oX1 = this.sgGeneral
        .add(((Builder) ((Builder) ((Builder) new Builder().name("oX1")).description("")).defaultValue(0)).sliderMin(-1000).sliderMax(1000).build());
    private final Setting<Integer> oY1 = this.sgGeneral
        .add(((Builder) ((Builder) ((Builder) new Builder().name("oY1")).description("")).defaultValue(0)).sliderMin(-1000).sliderMax(1000).build());
    private final Setting<Integer> oX2 = this.sgGeneral
        .add(((Builder) ((Builder) ((Builder) new Builder().name("oX2")).description("")).defaultValue(0)).sliderMin(-1000).sliderMax(1000).build());
    private final Setting<Integer> oY2 = this.sgGeneral
        .add(((Builder) ((Builder) ((Builder) new Builder().name("oY2")).description("")).defaultValue(0)).sliderMin(-1000).sliderMax(1000).build());
    private final Setting<Integer> oX3 = this.sgGeneral
        .add(((Builder) ((Builder) ((Builder) new Builder().name("oX3")).description("")).defaultValue(0)).sliderMin(-1000).sliderMax(1000).build());
    private final Setting<Integer> oY3 = this.sgGeneral
        .add(((Builder) ((Builder) ((Builder) new Builder().name("oY3")).description("")).defaultValue(0)).sliderMin(-1000).sliderMax(1000).build());
    private final Setting<Integer> oX4 = this.sgGeneral
        .add(((Builder) ((Builder) ((Builder) new Builder().name("oX4")).description("")).defaultValue(0)).sliderMin(-1000).sliderMax(1000).build());
    private final Setting<Integer> oY4 = this.sgGeneral
        .add(((Builder) ((Builder) ((Builder) new Builder().name("oY4")).description("")).defaultValue(0)).sliderMin(-1000).sliderMax(1000).build());
    private final Setting<Integer> oX5 = this.sgGeneral
        .add(((Builder) ((Builder) ((Builder) new Builder().name("oX5")).description("")).defaultValue(0)).sliderMin(-1000).sliderMax(1000).build());
    private final Setting<Integer> oY5 = this.sgGeneral
        .add(((Builder) ((Builder) ((Builder) new Builder().name("oY5")).description("")).defaultValue(0)).sliderMin(-1000).sliderMax(1000).build());
    private final Setting<Integer> oX6 = this.sgGeneral
        .add(((Builder) ((Builder) ((Builder) new Builder().name("oX6")).description("")).defaultValue(0)).sliderMin(-1000).sliderMax(1000).build());
    private final Setting<Integer> oY6 = this.sgGeneral
        .add(((Builder) ((Builder) ((Builder) new Builder().name("oY6")).description("")).defaultValue(0)).sliderMin(-1000).sliderMax(1000).build());
    private final Setting<Double> radius = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("radius"))
                .description(""))
                .defaultValue(0.0)
                .sliderMin(0.0)
                .sliderMax(50.0)
                .build()
        );
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
    private final Setting<SettingColor> absColor = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                .name("absColor"))
                .description(""))
                .defaultValue(new Color(255, 255, 255, 255))
                .build()
        );
    private final Setting<SettingColor> oxyColor = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                .name("oxyColor"))
                .description(""))
                .defaultValue(new Color(255, 255, 255, 255))
                .build()
        );
    private final Setting<SettingColor> hungColor = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                .name("hungColor"))
                .description(""))
                .defaultValue(new Color(255, 255, 255, 255))
                .build()
        );
    private final Setting<SettingColor> armColor = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                .name("armColor"))
                .description(""))
                .defaultValue(new Color(255, 255, 255, 255))
                .build()
        );
    private final Setting<SettingColor> fontColor = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                .name("fontColor"))
                .description(""))
                .defaultValue(new Color(0, 0, 0, 255))
                .build()
        );
    private final Setting<Double> animationFactor = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("AnimationFactor"))
                .description(""))
                .defaultValue(2.0)
                .sliderMin(0.01)
                .sliderMax(10.0)
                .build()
        );
    private final Setting<Double> factor1 = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("Factor1"))
                .description(""))
                .defaultValue(183.0)
                .sliderMin(0.0)
                .sliderMax(1000.0)
                .build()
        );
    private final Setting<Double> factor2 = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("Factor2"))
                .description(""))
                .defaultValue(182.0)
                .sliderMin(0.0)
                .sliderMax(1000.0)
                .build()
        );
    private final Setting<Double> factor3 = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("Factor3"))
                .description(""))
                .defaultValue(5.0)
                .sliderMin(0.0)
                .sliderMax(1000.0)
                .build()
        );
    private float hAnimation;
    private float hAnimation1;
    private float hAnimation2;
    private float hAnimation3;
    private float hAnimation4;
    private float hAnimation5;
    private float hAnimation6;
    private float varAnimation;
    private float varAnimation1;
    private float varAnimation2;
    private float varAnimation3;
    private float varAnimation4;
    private float varAnimation5;
    private float xpAnimation;
    private float xpBarAnimation;
    public HealthBar() {
        super(INFO);
        INSTANCE = this;
    }

    public void render(HudRenderer renderer) {
        this.setSize((this.sizeX.get()).intValue(), (this.sizeY.get()).intValue());
        if (MeteorClient.mc.player != null) {
            renderer.post(
                () -> {
                    DrawUtils.renderer.begin();
                    float health = MeteorClient.mc.player.getHealth();
                    float maxHealth = MeteorClient.mc.player.getMaxHealth();
                    float healthRatio = health / maxHealth;
                    float adjustedWidth = (this.sizeX.get()).intValue() * healthRatio;
                    this.hAnimation = AnimationUtility.ease(this.hAnimation, adjustedWidth, (this.animationFactor.get()).floatValue());
                    DrawUtils.drawRoundedQuad(
                        this.x,
                        this.y,
                        (this.sizeX.get()).intValue(),
                        (this.sizeY.get()).intValue(),
                        this.radius.get(),
                        (Color) this.bgColor.get()
                    );
                    DrawUtils.drawRoundedQuad(
                        this.x, this.y, this.hAnimation, (this.sizeY.get()).intValue(), this.radius.get(), (Color) this.barColor.get()
                    );
                    float health2 = MeteorClient.mc.player.getAbsorptionAmount();
                    float maxHealth2 = 16.0F;
                    float healthRatio2 = health2 / maxHealth2;
                    float adjustedWidth2 = (this.sizeX.get()).intValue() * healthRatio2;
                    if (health2 != 0.0F) {
                        this.hAnimation2 = AnimationUtility.ease(this.hAnimation2, adjustedWidth2, (this.animationFactor.get()).floatValue());
                        this.hAnimation1 = AnimationUtility.ease(
                            this.hAnimation1, (this.sizeX.get()).intValue(), (this.animationFactor.get()).floatValue()
                        );
                        DrawUtils.drawRoundedQuad(
                            this.x + this.oX.get(),
                            this.y + this.oY.get(),
                            this.hAnimation1,
                            (this.sizeY.get()).intValue(),
                            this.radius.get(),
                            (Color) this.bgColor.get()
                        );
                        DrawUtils.drawRoundedQuad(
                            this.x + this.oX.get(),
                            this.y + this.oY.get(),
                            this.hAnimation2,
                            (this.sizeY.get()).intValue(),
                            this.radius.get(),
                            (Color) this.absColor.get()
                        );
                    }

                    float health3 = MeteorClient.mc.player.getHungerManager().getFoodLevel();
                    float maxHealth3 = 20.0F;
                    float healthRatio3 = health3 / maxHealth3;
                    float adjustedWidth3 = (this.sizeX.get()).intValue() * healthRatio3;
                    this.hAnimation3 = AnimationUtility.ease(this.hAnimation3, adjustedWidth3, (this.animationFactor.get()).floatValue());
                    DrawUtils.drawRoundedQuad(
                        this.x + this.oX1.get(),
                        this.y + this.oY1.get(),
                        (this.sizeX.get()).intValue(),
                        (this.sizeY.get()).intValue(),
                        this.radius.get(),
                        (Color) this.bgColor.get()
                    );
                    DrawUtils.drawRoundedQuad(
                        this.x + this.oX1.get(),
                        this.y + this.oY1.get(),
                        this.hAnimation3,
                        (this.sizeY.get()).intValue(),
                        this.radius.get(),
                        (Color) this.hungColor.get()
                    );
                    float health4 = MeteorClient.mc.player.getAir();
                    float maxHealth4 = MeteorClient.mc.player.getMaxAir();
                    float healthRatio4 = health4 / maxHealth4;
                    float adjustedWidth4 = (this.sizeX.get()).intValue() * healthRatio4;
                    if (MeteorClient.mc.player.isSubmergedInWater()) {
                        this.hAnimation4 = AnimationUtility.ease(this.hAnimation4, adjustedWidth4, (this.animationFactor.get()).floatValue());
                        DrawUtils.drawRoundedQuad(
                            this.x + this.oX2.get(),
                            this.y + this.oY2.get(),
                            (this.sizeX.get()).intValue(),
                            (this.sizeY.get()).intValue(),
                            this.radius.get(),
                            (Color) this.bgColor.get()
                        );
                        DrawUtils.drawRoundedQuad(
                            this.x + this.oX2.get(),
                            this.y + this.oY2.get(),
                            this.hAnimation4,
                            (this.sizeY.get()).intValue(),
                            this.radius.get(),
                            (Color) this.oxyColor.get()
                        );
                    }

                    float health5 = MeteorClient.mc.player.getArmor();
                    float maxHealth5 = 20.0F;
                    float healthRatio5 = Math.min(health5 / maxHealth5, 1.0F);
                    float adjustedWidth5 = (this.sizeX.get()).intValue() * healthRatio5;
                    if (health5 > 0.0F) {
                        this.hAnimation5 = AnimationUtility.ease(this.hAnimation5, adjustedWidth5, (this.animationFactor.get()).floatValue());
                        DrawUtils.drawRoundedQuad(
                            this.x + this.oX3.get(),
                            this.y + this.oY3.get(),
                            (this.sizeX.get()).intValue(),
                            (this.sizeY.get()).intValue(),
                            this.radius.get(),
                            (Color) this.bgColor.get()
                        );
                        DrawUtils.drawRoundedQuad(
                            this.x + this.oX3.get(),
                            this.y + this.oY3.get(),
                            this.hAnimation5,
                            (this.sizeY.get()).intValue(),
                            this.radius.get(),
                            (Color) this.armColor.get()
                        );
                    }

                    DrawUtils.renderer.render(null);
                    if (health5 > 0.0F) {
                        this.varAnimation5 = AnimationUtility.ease(this.varAnimation5, health5, (this.animationFactor.get()).floatValue());
                        FontRenderers.monsterrat_16
                            .drawString(
                                renderer.drawContext.getMatrices(),
                                String.valueOf((int) Math.ceil(this.varAnimation5)),
                                this.x + this.oX3.get() + 5,
                                this.y + this.oY3.get(),
                                ((SettingColor) this.fontColor.get()).getPacked()
                            );
                    }

                    if (MeteorClient.mc.player.isSubmergedInWater()) {
                        this.varAnimation4 = AnimationUtility.ease(this.varAnimation4, health4, (this.animationFactor.get()).floatValue());
                        FontRenderers.monsterrat_16
                            .drawString(
                                renderer.drawContext.getMatrices(),
                                String.valueOf((int) Math.ceil(this.varAnimation4)),
                                this.x + this.oX2.get() + 5,
                                this.y + this.oY2.get(),
                                ((SettingColor) this.fontColor.get()).getPacked()
                            );
                    }

                    this.varAnimation3 = AnimationUtility.ease(this.varAnimation3, health3, (this.animationFactor.get()).floatValue());
                    FontRenderers.monsterrat_16
                        .drawString(
                            renderer.drawContext.getMatrices(),
                            String.valueOf((int) Math.ceil(this.varAnimation3)),
                            this.x + this.oX1.get() + 5,
                            this.y + this.oY1.get(),
                            ((SettingColor) this.fontColor.get()).getPacked()
                        );
                    if (health2 != 0.0F) {
                        this.varAnimation2 = AnimationUtility.ease(this.varAnimation2, health2, (this.animationFactor.get()).floatValue());
                        FontRenderers.monsterrat_16
                            .drawString(
                                renderer.drawContext.getMatrices(),
                                "♥ " + (int) Math.ceil(this.varAnimation2),
                                this.x + this.oX.get() + 5,
                                this.y + this.oY.get(),
                                ((SettingColor) this.fontColor.get()).getPacked()
                            );
                    }

                    this.varAnimation = AnimationUtility.ease(this.varAnimation, health, (this.animationFactor.get()).floatValue());
                    FontRenderers.monsterrat_16
                        .drawString(
                            renderer.drawContext.getMatrices(),
                            "♥ " + (int) Math.ceil(this.varAnimation),
                            this.x + 5,
                            this.y,
                            ((SettingColor) this.fontColor.get()).getPacked()
                        );
                    int color = 16777215;
                    if (MeteorClient.mc.player != null
                        && !MeteorClient.mc.player.getMainHandStack().isEmpty()
                        && MeteorClient.mc.player.getMainHandStack().getRarity().getFormatting().getColorValue() != null) {
                        color = MeteorClient.mc.player.getMainHandStack().getRarity().getFormatting().getColorValue();
                    }

                    String name = MeteorClient.mc.player.getMainHandStack().getName().getString();
                    this.varAnimation1 = AnimationUtility.ease(this.varAnimation1, FontRenderers.monsterrat_16.getStringWidth(name), 3.0F);
                    this.drawCentredQuad(
                        renderer,
                        this.x + this.oX4.get(),
                        this.y + this.oY4.get(),
                        this.varAnimation1 + 10.0F,
                        FontRenderers.monsterrat_16.getFontHeight(name),
                        (Color) this.bgColor.get()
                    );
                    FontRenderers.monsterrat_16
                        .drawCenteredString(
                            renderer.drawContext.getMatrices(), name, this.x + this.oX4.get(), this.y + this.oY4.get() + 2, new java.awt.Color(color)
                        );
                    this.renderExperienceLevel(renderer.drawContext);
                    this.renderExperienceBar(renderer.drawContext);
                }
            );
        }
    }

    public void renderExperienceLevel(DrawContext context) {
        this.xpAnimation = AnimationUtility.ease(this.xpAnimation, MeteorClient.mc.player.experienceLevel, (this.animationFactor.get()).floatValue());
        int i = (int) this.xpAnimation;
        if (i > 0) {
            MeteorClient.mc.getProfiler().push("expLevel");
            String string = i + "";
            int j = this.getX() + this.oX6.get();
            int k = this.getY() + this.oY6.get();
            FontRenderers.monsterrat_16.drawCenteredString(context.getMatrices(), string, j, k, new java.awt.Color(72, 255, 60, 255));
            MeteorClient.mc.getProfiler().pop();
        }
    }

    public void renderExperienceBar(DrawContext context) {
        MeteorClient.mc.getProfiler().push("expBar");
        int i = MeteorClient.mc.player.getNextLevelExperience();
        if (i > 0) {
            this.xpBarAnimation = AnimationUtility.ease(
                this.xpBarAnimation, (int) (MeteorClient.mc.player.experienceProgress * this.factor1.get()), (this.animationFactor.get()).floatValue()
            );
            int k = (int) this.xpBarAnimation;
            int l = this.y + this.oY5.get();
            int x = this.x + this.oX5.get();
            RenderSystem.enableBlend();
            Render2DEngine.drawRect(context.getMatrices(), x, l, this.factor2.get(), this.factor3.get(), (Color) this.bgColor.get());
            if (k > 0) {
                Render2DEngine.drawRect(
                    context.getMatrices(), (float) x, (float) l, (float) k, (this.factor3.get()).floatValue(), new java.awt.Color(72, 255, 60, 150)
                );
            }

            RenderSystem.disableBlend();
        }

        MeteorClient.mc.getProfiler().pop();
    }

    private void drawCentredQuad(HudRenderer renderer, double x, double y, double width, double height, Color color) {
        Render2DEngine.drawRect(renderer.drawContext.getMatrices(), x - width / 2.0, y, width, height, color);
    }


}
