package dev.abstr3act.addon.hud.targethud;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.abstr3act.addon.Compassion;
import dev.abstr3act.addon.hud.storage.TextureStorage;
import dev.abstr3act.addon.modules.Lacrymira.Media;
import dev.abstr3act.addon.modules.Seraphim.KillAura;
import dev.abstr3act.addon.utils.HashUtils;
import dev.abstr3act.addon.utils.TargetUtils;
import dev.abstr3act.addon.utils.notifications.DrawUtils;
import dev.abstr3act.addon.utils.render.Render2DEngine;
import dev.abstr3act.addon.utils.render.shaders.AnimationUtility;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.renderer.Renderer2D;
import meteordevelopment.meteorclient.renderer.text.TextRenderer;
import meteordevelopment.meteorclient.settings.DoubleSetting.Builder;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.config.Config;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.systems.hud.elements.TextHud;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.entity.EntityUtils;
import meteordevelopment.meteorclient.utils.entity.SortPriority;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BedItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.SwordItem;
import net.minecraft.util.Identifier;
import org.joml.Matrix4fStack;
import org.lwjgl.opengl.GL30;

public class NewTargetHud extends HudElement {
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();    public static final HudElementInfo<NewTargetHud> INFO = new HudElementInfo(
        Compassion.HUD_GROUP, "TargetHUD", "Displays information about your combat target.", NewTargetHud::new
    );
    private final Setting<Double> range = this.sgGeneral
        .add(((Builder) ((Builder) new Builder().name("range")).description("The range to target players.")).defaultValue(100.0).min(1.0).sliderMax(200.0).build());
    private final Setting<Double> test = this.sgGeneral
        .add(((Builder) ((Builder) new Builder().name("test")).description("The range to target players.")).defaultValue(0.0).min(1.0).sliderMax(200.0).build());
    private final Setting<Double> test2 = this.sgGeneral
        .add(((Builder) ((Builder) new Builder().name("test-2")).description("The range to target players.")).defaultValue(190.0).min(1.0).sliderMax(1000.0).build());    private final Setting<Double> scale = this.sgGeneral
        .add(
            ((Builder) ((Builder) ((Builder) new Builder().name("scale")).description("The scale."))
                .defaultValue(2.0)
                .min(1.0)
                .sliderRange(1.0, 5.0)
                .onChanged(aDouble -> this.calculateSize()))
                .build()
        );
    private final Setting<Double> test3 = this.sgGeneral
        .add(((Builder) ((Builder) new Builder().name("HUD Height")).description("The width of hud.")).defaultValue(190.0).min(1.0).sliderMax(1000.0).build());    private final Setting<Double> armorScale = this.sgGeneral
        .add(
            ((Builder) ((Builder) ((Builder) new Builder().name("ArmorScale")).description("The scale."))
                .defaultValue(2.0)
                .min(1.0)
                .sliderRange(1.0, 5.0)
                .onChanged(aDouble -> this.calculateSize()))
                .build()
        );
    private final Setting<Double> test4 = this.sgGeneral
        .add(((Builder) ((Builder) new Builder().name("HUD Width")).description("The height of hud.")).defaultValue(190.0).min(1.0).sliderMax(1000.0).build());
    private final Setting<Boolean> displayPing = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("ping"))
                .description("Shows the player's ping."))
                .defaultValue(true))
                .build()
        );
    private final Setting<SettingColor> pingColor1 = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                .name("ping-stage-1"))
                .description("Color of ping text when under 75."))
                .defaultValue(new SettingColor(255, 255, 255))
                .visible(this.displayPing::get))
                .build()
        );
    private final Setting<SettingColor> pingColor2 = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                .name("ping-stage-2"))
                .description("Color of ping text when between 75 and 200."))
                .defaultValue(new SettingColor(255, 255, 255))
                .visible(this.displayPing::get))
                .build()
        );
    private final Setting<SettingColor> pingColor3 = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                .name("ping-stage-3"))
                .description("Color of ping text when over 200."))
                .defaultValue(new SettingColor(255, 255, 255))
                .visible(this.displayPing::get))
                .build()
        );
    private final Setting<Boolean> displayDistance = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("getDistance"))
                .description("Shows the distance between you and the player."))
                .defaultValue(true))
                .build()
        );
    private final Setting<SettingColor> distColor1 = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                .name("distance-stage-1"))
                .description("The color when a player is within 10 blocks of you."))
                .defaultValue(new SettingColor(255, 255, 255))
                .visible(this.displayDistance::get))
                .build()
        );
    private final Setting<SettingColor> distColor2 = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                .name("distance-stage-2"))
                .description("The color when a player is within 50 blocks of you."))
                .defaultValue(new SettingColor(255, 255, 255))
                .visible(this.displayDistance::get))
                .build()
        );
    private final Setting<SettingColor> distColor3 = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                .name("distance-stage-3"))
                .description("The color when a player is greater then 50 blocks away from you."))
                .defaultValue(new SettingColor(255, 255, 255))
                .visible(this.displayDistance::get))
                .build()
        );
    private final Setting<SettingColor> backgroundColor = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                .name("background-color"))
                .description("Color of background."))
                .defaultValue(new SettingColor(0, 0, 0, 150))
                .build()
        );
    private final Setting<SettingColor> healthColor1 = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                .name("health-stage-1"))
                .description("The color on the left of the health gradient."))
                .defaultValue(new SettingColor(255, 255, 255))
                .build()
        );
    private final Setting<SettingColor> healthColor3 = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                .name("health-stage-3"))
                .description("The color on the right of the health gradient."))
                .defaultValue(new SettingColor(255, 255, 255))
                .build()
        );
    private final Setting<SettingColor> absorptionColor = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                .name("Absorption-Color"))
                .description("The color on the absorption."))
                .defaultValue(new SettingColor(255, 255, 255))
                .build()
        );
    private final Setting<background> bg = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) new meteordevelopment.meteorclient.settings.EnumSetting.Builder()
                .name("Background"))
                .description("The background of TargetHUD"))
                .defaultValue(background.thud))
                .build()
        );
    private final Setting<Double> phx = this.sgGeneral
        .add(((Builder) ((Builder) new Builder().name("Player head offset-X")).description("offset")).defaultValue(0.0).min(1.0).sliderMax(1000.0).build());
    private final Setting<Double> phy = this.sgGeneral
        .add(((Builder) ((Builder) new Builder().name("Player head offset-y")).description("offset")).defaultValue(0.0).min(1.0).sliderMax(1000.0).build());
    private final Setting<Double> scales = this.sgGeneral
        .add(((Builder) ((Builder) new Builder().name("Player head scale")).description("offset")).defaultValue(1.0).min(0.0).sliderMax(10.0).build());
    private final Setting<Boolean> gradient = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("gradient"))
                .description("gradient"))
                .defaultValue(true))
                .build()
        );
    private final Setting<SettingColor> gradient1 = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                .name("gradient-color-1"))
                .description("."))
                .defaultValue(new SettingColor(255, 255, 255))
                .build()
        );
    private final Setting<SettingColor> gradient2 = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                .name("gradient-color-2"))
                .description("."))
                .defaultValue(new SettingColor(255, 255, 255))
                .build()
        );
    private final Setting<SettingColor> gradient3 = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                .name("gradient-color-3"))
                .description("."))
                .defaultValue(new SettingColor(255, 255, 255))
                .build()
        );
    private final Setting<SettingColor> gradient4 = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                .name("gradient-color-4"))
                .description("."))
                .defaultValue(new SettingColor(255, 255, 255))
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
    private final Setting<Double> ab = this.sgGeneral
        .add(((Builder) ((Builder) new Builder().name("ab1")).description(".")).defaultValue(0.0).sliderRange(-100.0, 100.0).build());
    private final Setting<Double> ab2 = this.sgGeneral
        .add(((Builder) ((Builder) new Builder().name("ab2")).description(".")).defaultValue(0.0).sliderRange(-100.0, 100.0).build());
    private final Setting<Double> af1 = this.sgGeneral
        .add(((Builder) ((Builder) new Builder().name("af1")).description(".")).defaultValue(0.0).sliderRange(-100.0, 100.0).build());
    private final Setting<Double> af2 = this.sgGeneral
        .add(((Builder) ((Builder) new Builder().name("af2")).description(".")).defaultValue(0.0).sliderRange(-100.0, 100.0).build());
    private final Setting<Double> af3 = this.sgGeneral
        .add(((Builder) ((Builder) new Builder().name("af3")).description(".")).defaultValue(0.0).sliderRange(-100.0, 100.0).build());
    private final Setting<Double> af4 = this.sgGeneral
        .add(((Builder) ((Builder) new Builder().name("af4")).description(".")).defaultValue(0.0).sliderRange(-100.0, 100.0).build());
    private final Setting<Double> af5 = this.sgGeneral
        .add(((Builder) ((Builder) new Builder().name("af5")).description(".")).defaultValue(0.0).sliderRange(-100.0, 100.0).build());
    private final Setting<Double> af6 = this.sgGeneral
        .add(((Builder) ((Builder) new Builder().name("af6")).description(".")).defaultValue(0.0).sliderRange(-100.0, 100.0).build());
    private final Setting<Double> rainbowSpeed = this.sgGeneral
        .add(
            ((Builder) ((Builder) new Builder().name("rainbow-speed")).description("Rainbow speed of rainbow color mode."))
                .defaultValue(0.05)
                .sliderMin(0.01)
                .sliderMax(0.2)
                .decimalPlaces(4)
                .build()
        );
    private final Setting<Double> amAlpha = this.sgGeneral
        .add(((Builder) ((Builder) new Builder().name("Am Alpha")).description(".")).defaultValue(0.5).sliderMin(0.01).sliderMax(1.0).max(1.0).min(0.0).build());
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
    private final Setting<Double> animationFactor = this.sgGeneral
        .add(
            ((Builder) ((Builder) new Builder().name("AnimationFactor")).description(".")).defaultValue(0.5).sliderMin(0.01).sliderMax(5.0).max(5.0).min(0.0).build()
        );
    private final Color rainbow = new Color(255, 255, 255);
    private final Color rainbow2 = new Color(255, 255, 255);
    public float animation;
    Color color;
    Color color1;
    private PlayerEntity lastTarget;
    private PlayerEntity playerEntity;
    private double rainbowHue1;
    private double rainbowHue2;
    private double t1;
    private double t2;
    private float vAnimation;
    private float hAnimation;
    private float vAnimation2;
    private float hAnimation2;
    private float vAnimation3;
    private float hAnimation3;
    private float vAnimation4;
    private float hAnimation4;
    private float xAnimation;
    private float yAnimation;
    private float xAnimation2;
    private float yAnimation2;
    private float xAnimation3;
    private float yAnimation3;
    private float xAnimation4;
    private float yAnimation4;
    private float xAnimation5;
    private float yAnimation5;
    private float fAnimation4;
    private float gAnimation4;
    private float fAnimation;
    private float gAnimation;
    private float varAnimation;
    private float cosAnimation;
    private float value;
    private float value2;
    public NewTargetHud() {
        super(INFO);
        this.calculateSize();
    }

    private static void drawPlayerHead(MatrixStack matrixStack, PlayerEntity target, double x0, double y0, double width, double height) {
        if (target != null) {
            float hurtPercent = target.hurtTime / 6.0F;
            if (target instanceof PlayerEntity) {
                RenderSystem.setShaderTexture(0, ((AbstractClientPlayerEntity) target).getSkinTextures().texture());
            } else {
                RenderSystem.setShaderTexture(0, MeteorClient.mc.getEntityRenderDispatcher().getRenderer(target).getTexture(target));
            }

            RenderSystem.setShaderColor(1.0F, 1.0F - hurtPercent, 1.0F - hurtPercent, 1.0F);
            Render2DEngine.renderTexture(matrixStack, x0, y0, width, height, 8.0F, 8.0F, 8.0, 8.0, 64.0, 64.0);
            Render2DEngine.renderTexture(matrixStack, x0, y0, width, height, 40.0F, 8.0F, 8.0, 8.0, 64.0, 64.0);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        }
    }

    private void calculateSize() {
        this.setSize(this.test4.get() * this.scale.get(), this.test3.get() * this.scale.get());
    }

    private void reset() {
        this.vAnimation = 0.0F;
        this.vAnimation2 = 0.0F;
        this.vAnimation3 = 0.0F;
        this.vAnimation4 = 0.0F;
        this.hAnimation = 0.0F;
        this.hAnimation2 = 0.0F;
        this.hAnimation3 = 0.0F;
        this.hAnimation4 = 0.0F;
        this.xAnimation = this.getX();
        this.xAnimation2 = this.getX();
        this.xAnimation3 = this.getX();
        this.xAnimation4 = this.getX();
        this.yAnimation = this.getY();
        this.yAnimation2 = this.getY();
        this.yAnimation3 = this.getY();
        this.yAnimation4 = this.getY();
        this.gAnimation4 = 0.0F;
        this.fAnimation4 = 0.0F;
        this.gAnimation = 0.0F;
        this.fAnimation = 0.0F;
        this.varAnimation = 0.0F;
        this.cosAnimation = 0.0F;
        this.value2 = 0.0F;
        this.value = 0.0F;
    }

    public void render(HudRenderer renderer) {
        super.render(renderer);
        renderer.drawContext.getMatrices().push();
        this.animation = AnimationUtility.ease(this.animation, 1.0F, (this.animationFactor.get()).floatValue());
        renderer.drawContext.getMatrices().scale(this.animation, this.animation, this.animation);
        this.t1 = this.t1 + this.rainbowSpeed.get() * renderer.delta;
        if (this.t1 > 1.0) {
            this.t1--;
        } else if (this.t1 < 0.0) {
            this.t1++;
        }

        this.t2 = this.t1;
        this.rainbowHue1 = this.rainbowHue1 + this.rainbowSpeed.get() * renderer.delta;
        if (this.rainbowHue1 > 1.0) {
            this.rainbowHue1--;
        } else if (this.rainbowHue1 < 0.0) {
            this.rainbowHue1++;
        }

        this.rainbowHue2 = this.rainbowHue1;
        MatrixStack context = renderer.drawContext.getMatrices();
        this.calculateSize();
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
        this.color = this.rainbow;
        this.t1 = this.t1 + this.rainbowSpeed.get() * renderer.delta;
        if (this.t1 > 1.0) {
            this.t1--;
        } else if (this.t1 < 0.0) {
            this.t1++;
        }

        double p = Math.sin((this.t1 + 1.0) * Math.PI * 2.0);
        p = (p + 1.0) / 2.0;
        Color w = (Color) this.dp1.get();
        Color r1 = (Color) this.dp2.get();
        int r = (int) (r1.r * (1.0 - p) + w.r * p);
        int g = (int) (r1.g * (1.0 - p) + w.g * p);
        int b = (int) (r1.b * (1.0 - p) + w.b * p);
        this.rainbow2.set(r, g, b, 255);
        this.color1 = this.rainbow2;
        renderer.post(
            () -> {
                double x = this.x;
                double y = this.y;
                Color primaryColor = TextHud.getSectionColor(0);
                Color secondaryColor = TextHud.getSectionColor(1);
                if (this.isInEditor()) {
                    this.playerEntity = MeteorClient.mc.player;
                } else {
                    this.playerEntity = TargetUtils.getPlayerTarget(this.range.get(), SortPriority.LowestDistance);
                }

                if (((KillAura) Modules.get().get(KillAura.class)).isActive() && KillAura.closestEntity != null && KillAura.closestEntity instanceof PlayerEntity) {
                    this.playerEntity = (PlayerEntity) KillAura.closestEntity;
                }

                if (this.playerEntity == null && !this.isInEditor()) {
                    this.reset();
                } else if (this.lastTarget != this.playerEntity) {
                    this.reset();
                    this.lastTarget = this.playerEntity;
                } else {
                    Renderer2D.COLOR.begin();
                    DrawUtils.renderer.begin();
                    this.hAnimation = AnimationUtility.ease(
                        this.hAnimation, this.getWidth() + (this.tx2.get()).floatValue(), (this.animationFactor.get()).floatValue()
                    );
                    this.vAnimation = AnimationUtility.ease(
                        this.vAnimation, this.getHeight() + (this.ty2.get()).floatValue(), (this.animationFactor.get()).floatValue()
                    );
                    Render2DEngine.drawRoundedBlur(
                        context,
                        (float) x + (this.tx1.get()).floatValue(),
                        (float) y + (this.ty1.get()).floatValue(),
                        this.hAnimation,
                        this.getHeight() + (this.ty2.get()).floatValue(),
                        0.0F,
                        new java.awt.Color(((SettingColor) this.backgroundColor.get()).getPacked())
                    );
                    DrawUtils.renderer.render(null);
                    context.push();
                    RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                    RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                    context.pop();
                    RenderSystem.setShaderTexture(0, ((background) this.bg.get()).identifier);
                    context.push();
                    RenderSystem.enableBlend();
                    RenderSystem.defaultBlendFunc();
                    RenderSystem.disableBlend();
                    RenderSystem.setShaderColor(0.3F, 0.3F, 0.3F, (this.amAlpha.get()).floatValue());
                    GL30.glEnable(32925);
                    this.hAnimation2 = AnimationUtility.ease(this.hAnimation2, this.getWidth(), (this.animationFactor.get()).floatValue());
                    this.vAnimation2 = AnimationUtility.ease(this.vAnimation2, this.getHeight(), (this.animationFactor.get()).floatValue());
                    this.hAnimation3 = AnimationUtility.ease(
                        this.hAnimation3, (float) (64.0 * this.scale.get() * this.scales.get()), (this.animationFactor.get()).floatValue()
                    );
                    this.vAnimation3 = AnimationUtility.ease(
                        this.vAnimation3, (float) (64.0 * this.scale.get() * this.scales.get()), (this.animationFactor.get()).floatValue()
                    );
                    Render2DEngine.renderTextureX(context, x, y, this.hAnimation2, this.getHeight(), 0.0F, 0.0F, 100.0, 50.0, 100.0, 50.0);
                    RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                    context.push();
                    this.xAnimation = AnimationUtility.ease(this.xAnimation, (float) (x + this.phx.get()), (this.animationFactor.get()).floatValue());
                    this.yAnimation = AnimationUtility.ease(this.yAnimation, (float) (y + this.phy.get()), (this.animationFactor.get()).floatValue());
                    drawPlayerHead(renderer.drawContext.getMatrices(), this.playerEntity, this.xAnimation, y + this.phy.get(), this.hAnimation3, this.vAnimation3);
                    if (this.playerEntity == null) {
                        if (this.isInEditor()) {
                            renderer.line(x, y, x + this.getWidth(), y + this.getHeight(), Color.GRAY);
                            renderer.line(x + this.getWidth(), y, x, y + this.getHeight(), Color.GRAY);
                            Renderer2D.COLOR.render(null);
                        }
                    } else {
                        Renderer2D.COLOR.render(null);
                        double var37 = x + 5.0 * this.scale.get();
                        double var40 = y + 5.0 * this.scale.get();
                        String breakText = " ";
                        String hash = HashUtils.hashSHA256(this.playerEntity.getUuidAsString());
                        String string = hash.substring(0, Math.min(5, hash.length()));
                        String nameText = ((Media) Modules.get().get(Media.class)).isActive()
                            ? (String) ((Media) Modules.get().get(Media.class)).protectedString.get() + "_" + string
                            : this.playerEntity.getName().getString();
                        Color nameColor = PlayerUtils.getPlayerColor(this.playerEntity, primaryColor);
                        int ping = EntityUtils.getPing(this.playerEntity);
                        String pingText = ping + "ms";
                        Color pingColor;
                        if (ping <= 75) {
                            pingColor = (Color) this.pingColor1.get();
                        } else if (ping <= 200) {
                            pingColor = (Color) this.pingColor2.get();
                        } else {
                            pingColor = (Color) this.pingColor3.get();
                        }

                        double dist = 0.0;
                        if (!this.isInEditor()) {
                            dist = Math.round(MeteorClient.mc.player.distanceTo(this.playerEntity) * 100.0) / 100.0;
                        }

                        String distText = dist + "m";
                        Color distColor;
                        if (dist <= 10.0) {
                            distColor = (Color) this.distColor1.get();
                        } else if (dist <= 50.0) {
                            distColor = (Color) this.distColor2.get();
                        } else {
                            distColor = (Color) this.distColor3.get();
                        }

                        String friendText = "Unknown";
                        Color friendColor = primaryColor;
                        if (Friends.get().isFriend(this.playerEntity)) {
                            friendText = "Friend";
                            friendColor = (Color) Config.get().friendColor.get();
                        } else {
                            boolean naked = true;

                            for (int position = 3; position >= 0; position--) {
                                ItemStack itemStack = this.getItem(position);
                                if (!itemStack.isEmpty()) {
                                    naked = false;
                                }
                            }

                            if (naked) {
                                friendText = "Naked";
                                friendColor = Color.WHITE;
                            } else {
                                boolean threat = false;

                                for (int positionx = 5; positionx >= 0; positionx--) {
                                    ItemStack itemStack = this.getItem(positionx);
                                    if (itemStack.getItem() instanceof SwordItem
                                        || itemStack.getItem() == Items.END_CRYSTAL
                                        || itemStack.getItem() == Items.RESPAWN_ANCHOR
                                        || itemStack.getItem() instanceof BedItem) {
                                        threat = true;
                                    }
                                }

                                if (threat) {
                                    friendText = "Threat";
                                    friendColor = Color.WHITE;
                                }
                            }
                        }

                        this.fAnimation4 = AnimationUtility.ease(
                            this.fAnimation4, (float) (0.7 * this.scale.get()), (this.animationFactor.get()).floatValue()
                        );
                        TextRenderer.get().begin(this.fAnimation4, false, true);
                        double breakWidth = TextRenderer.get().getWidth(breakText);
                        double pingWidth = TextRenderer.get().getWidth(pingText);
                        double friendWidth = TextRenderer.get().getWidth(friendText);
                        TextRenderer.get().render(nameText, var37, var40, nameColor != null ? nameColor : primaryColor);
                        TextRenderer.get().end();
                        this.gAnimation4 = AnimationUtility.ease(
                            this.gAnimation4, (float) (0.5 * this.scale.get()), (this.animationFactor.get()).floatValue()
                        );
                        TextRenderer.get().begin(this.gAnimation4, false, true);
                        y = var40 + (TextRenderer.get().getHeight() + 10.0);
                        TextRenderer.get().render(friendText, var37, y, friendColor);
                        if (this.displayPing.get()) {
                            TextRenderer.get().render(breakText, var37 + friendWidth, y, secondaryColor);
                            TextRenderer.get().render(pingText, var37 + friendWidth + breakWidth, y, pingColor);
                            if (this.displayDistance.get()) {
                                TextRenderer.get().render(breakText, var37 + friendWidth + breakWidth + pingWidth, y, secondaryColor);
                                TextRenderer.get().render(distText, var37 + friendWidth + breakWidth + pingWidth + breakWidth, y, distColor);
                            }
                        } else if (this.displayDistance.get()) {
                            TextRenderer.get().render(breakText, var37 + friendWidth, y, secondaryColor);
                            TextRenderer.get().render(distText, var37 + friendWidth + breakWidth, y, distColor);
                        }

                        TextRenderer.get().end();
                        Matrix4fStack matrices = RenderSystem.getModelViewStack();
                        matrices.pushMatrix();
                        matrices.scale((this.scale.get()).floatValue(), (this.scale.get()).floatValue(), 1.0F);
                        this.gAnimation = AnimationUtility.ease(this.gAnimation, 0.35F, (this.animationFactor.get()).floatValue());
                        TextRenderer.get().begin(this.gAnimation, false, true);
                        TextRenderer.get().end();
                        this.xAnimation4 = AnimationUtility.ease(
                            this.xAnimation4, (float) (var37 + this.af3.get()), (this.animationFactor.get()).floatValue()
                        );
                        this.yAnimation4 = AnimationUtility.ease(this.yAnimation4, (float) (y + this.af4.get()), (this.animationFactor.get()).floatValue());
                        this.cosAnimation = AnimationUtility.ease(
                            this.cosAnimation, (this.armorScale.get()).floatValue(), (this.animationFactor.get()).floatValue()
                        );
                        this.drawArmor(renderer, (this.armorScale.get()).floatValue(), var37 + this.af3.get(), this.yAnimation);
                        y = (int) (this.y + this.test.get() * this.scale.get());
                        x = this.x;
                        y += 5.0;
                        x /= this.scale.get();
                        y /= this.scale.get();
                        this.fAnimation = AnimationUtility.ease(
                            this.fAnimation, (float) (0.35F * this.scale.get()), (this.animationFactor.get()).floatValue()
                        );
                        TextRenderer.get().begin(this.fAnimation, false, true);
                        this.xAnimation3 = AnimationUtility.ease(this.xAnimation3, (float) (x + 3.0), (this.animationFactor.get()).floatValue());
                        this.yAnimation3 = AnimationUtility.ease(this.yAnimation3, (float) (y - 10.0), (this.animationFactor.get()).floatValue());
                        this.value = AnimationUtility.fast(this.value, this.playerEntity.getHealth(), (this.animationFactor.get()).floatValue());
                        TextRenderer.get()
                            .render(this.value > 1000.0F ? "[Spoofed]" : String.valueOf(String.format("%.1f", this.value)), x + 3.0, y - 10.0, Color.WHITE, false);
                        TextRenderer.get().end();
                        this.varAnimation = AnimationUtility.ease(
                            this.varAnimation, (float) (0.25 * this.scale.get()), (this.animationFactor.get()).floatValue()
                        );
                        TextRenderer.get().begin(this.varAnimation, false, true);
                        this.value2 = AnimationUtility.fast(this.value2, this.playerEntity.getAbsorptionAmount(), (this.animationFactor.get()).floatValue());
                        this.xAnimation2 = AnimationUtility.ease(
                            this.xAnimation2,
                            (float) (x + 15.0 + TextRenderer.get().getWidth(this.value > 1000.0F ? "[Spoofed]" : String.valueOf(String.format("%.1f", this.value)))),
                            (this.animationFactor.get()).floatValue()
                        );
                        this.yAnimation2 = AnimationUtility.ease(this.yAnimation2, (float) (y - 7.0), (this.animationFactor.get()).floatValue());
                        TextRenderer.get()
                            .render(
                                this.value2 > 1000.0F ? "[Spoofed]" : String.valueOf(String.format("%.1f", this.value2)),
                                x + 15.0 + TextRenderer.get().getWidth(this.value > 1000.0F ? "[Spoofed]" : String.valueOf(String.format("%.1f", this.value))),
                                y - 7.0,
                                (Color) this.absorptionColor.get(),
                                false
                            );
                        TextRenderer.get().end();
                        Renderer2D.COLOR.begin();
                        Renderer2D.COLOR.render(null);
                        y += 2.0;
                        float health = this.playerEntity.getHealth();
                        float maxHealth = this.playerEntity.getMaxHealth();
                        int healthWidth = (int) (health / maxHealth * this.test2.get());
                        Renderer2D.COLOR.begin();
                        this.vAnimation4 = AnimationUtility.ease(this.vAnimation4, 3.0F, (this.animationFactor.get()).floatValue());
                        this.hAnimation4 = AnimationUtility.ease(
                            this.hAnimation4, (float) (healthWidth + this.ab.get()), (this.animationFactor.get()).floatValue()
                        );
                        this.xAnimation4 = AnimationUtility.ease(this.xAnimation4, (float) x, (this.animationFactor.get()).floatValue());
                        this.yAnimation4 = AnimationUtility.ease(this.yAnimation4, (float) y, (this.animationFactor.get()).floatValue());
                        Renderer2D.COLOR.quad(x, y, this.hAnimation4, this.vAnimation4, this.color1, this.color, this.color, this.color1);
                        Renderer2D.COLOR.render(null);
                        matrices.popMatrix();
                    }
                }
            }
        );
        renderer.drawContext.getMatrices().pop();
    }

    private float interpolate(float startValue, float endValue, float time) {
        return startValue + (endValue - startValue) * time;
    }

    private void drawArmor(HudRenderer renderer, float scales, double x, double y) {
        int slot = 5;
        Matrix4fStack matrices = RenderSystem.getModelViewStack();
        matrices.pushMatrix();
        matrices.scale(scales, scales, 1.0F);
        x /= scales;
        y /= scales;
        double var14 = x + this.af5.get();
        double var16 = y + this.af6.get();

        for (int position = 0; position < 6; position++) {
            double armorX = var14 + position * 20;
            ItemStack itemStack = this.getItem(slot);
            renderer.item(itemStack, (int) (armorX * this.scale.get()), (int) (var16 * this.scale.get()), (this.scale.get()).floatValue(), true);
            slot--;
        }

        matrices.popMatrix();
    }

    private ItemStack getItem(int i) {
        if (this.isInEditor()) {
            return switch (i) {
                case 0 -> Items.END_CRYSTAL.getDefaultStack();
                case 1 -> Items.NETHERITE_BOOTS.getDefaultStack();
                case 2 -> Items.NETHERITE_LEGGINGS.getDefaultStack();
                case 3 -> Items.NETHERITE_CHESTPLATE.getDefaultStack();
                case 4 -> Items.NETHERITE_HELMET.getDefaultStack();
                case 5 -> Items.TOTEM_OF_UNDYING.getDefaultStack();
                default -> ItemStack.EMPTY;
            };
        } else if (this.playerEntity == null) {
            return ItemStack.EMPTY;
        } else {
            return switch (i) {
                case 4 -> this.playerEntity.getOffHandStack();
                case 5 -> this.playerEntity.getMainHandStack();
                default -> this.playerEntity.getInventory().getArmorStack(i);
            };
        }
    }

    public static enum background {
        hikari_fatalis("Hikari (Fatalis)", TextureStorage.hikari_fatalis),
        tairitsu_hikari("Tairitsu & Hikari", TextureStorage.tairitsu_hikari),
        kou_winter("Kou (Winter)", TextureStorage.kou_winter),
        eto_hoppe("Eto & Hoppe", TextureStorage.eto_hoppe),
        luna_ilot("Luna & Ilot", TextureStorage.luna_ilot),
        hikari_zero("Hikari (Zero)", TextureStorage.hikari_zero),
        luna("Luna", TextureStorage.luna),
        acid("Acid", TextureStorage.acid),
        tairitsu_latest("Tairitsu (Latest)", TextureStorage.tairitsu_latest),
        hikari_latest("Hikari (Latest)", TextureStorage.hikari_latest),
        compassion("Compassion", TextureStorage.compassion),
        kotone_fujita("Kotone Fujita", TextureStorage.kotone_fujita),
        catgirl("Cat Girl", TextureStorage.catgirl),
        mila_legacy("Mila", TextureStorage.mila_legacy),
        mita_kind_legacy("KindMita", TextureStorage.mita_kind_legacy),
        mita_hat("CapMita", TextureStorage.mita_hat),
        mita_short_legacy("ShortHairMita", TextureStorage.mita_short_legacy),
        mita_sleepy_legacy("SleepyMita", TextureStorage.mita_sleepy_legacy),
        mita_normal("Mita", TextureStorage.mita_normal),
        mita_normal_legacy("Mita (Official)", TextureStorage.mita_normal_legacy),
        thud("Default", TextureStorage.thud);

        private final String name;
        private final Identifier identifier;

        private background(String name, Identifier identifier) {
            this.name = name;
            this.identifier = identifier;
        }

        @Override
        public String toString() {
            return this.name;
        }

        public Identifier getPath() {
            return this.identifier;
        }
    }






}
