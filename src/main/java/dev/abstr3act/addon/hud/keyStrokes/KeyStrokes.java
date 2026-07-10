package dev.abstr3act.addon.hud.keyStrokes;

import dev.abstr3act.addon.Compassion;
import dev.abstr3act.addon.font.FontRenderers;
import dev.abstr3act.addon.utils.render.Render2DEngine;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.settings.ColorSetting.Builder;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import net.minecraft.client.util.math.MatrixStack;

import java.awt.*;

public class KeyStrokes extends HudElement {
    public static KeyStrokes INSTANCE;    public static final HudElementInfo<KeyStrokes> INFO = new HudElementInfo(Compassion.HUD_GROUP, "KeyStrokes", "KeyStrokes", KeyStrokes::new);
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
    private final Setting<SettingColor> sOffColor = this.sgGeneral
        .add(((Builder) new Builder().name("stringOffColor")).defaultValue(new SettingColor(255, 255, 255, 255)).build());
    private final Setting<SettingColor> sOnColor = this.sgGeneral
        .add(((Builder) new Builder().name("stringOnColor")).defaultValue(new SettingColor(0, 0, 0, 255)).build());
    private final Setting<SettingColor> bOffColor = this.sgGeneral
        .add(((Builder) new Builder().name("bOffColor")).defaultValue(new SettingColor(40, 40, 40, 116)).build());
    private final Setting<SettingColor> bOnColor = this.sgGeneral
        .add(((Builder) new Builder().name("bOnColor")).defaultValue(new SettingColor(229, 234, 237, 66)).build());
    private final Setting<Integer> offsetX = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("JumpOffsetX"))
                .description("."))
                .sliderRange(-1000, 1000)
                .defaultValue(0))
                .build()
        );
    private final Setting<Integer> offsetY = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("JumpOffsetY"))
                .description("."))
                .sliderRange(-1000, 1000)
                .defaultValue(0))
                .build()
        );
    private final Setting<Integer> offsetX1 = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("WOffsetX"))
                .description("."))
                .sliderRange(-1000, 1000)
                .defaultValue(0))
                .build()
        );
    private final Setting<Integer> offsetY1 = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("WOffsetY"))
                .description("."))
                .sliderRange(-1000, 1000)
                .defaultValue(0))
                .build()
        );
    private final Setting<Integer> offsetX2 = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("AOffsetX"))
                .description("."))
                .sliderRange(-1000, 1000)
                .defaultValue(0))
                .build()
        );
    private final Setting<Integer> offsetY2 = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("AOffsetY"))
                .description("."))
                .sliderRange(-1000, 1000)
                .defaultValue(0))
                .build()
        );
    private final Setting<Integer> offsetX3 = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("SOffsetX"))
                .description("."))
                .sliderRange(-1000, 1000)
                .defaultValue(0))
                .build()
        );
    private final Setting<Integer> offsetY3 = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("SOffsetY"))
                .description("."))
                .sliderRange(-1000, 1000)
                .defaultValue(0))
                .build()
        );
    private final Setting<Integer> offsetX4 = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("DOffsetX"))
                .description("."))
                .sliderRange(-1000, 1000)
                .defaultValue(0))
                .build()
        );
    private final Setting<Integer> offsetY4 = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("DOffsetY"))
                .description("."))
                .sliderRange(-1000, 1000)
                .defaultValue(0))
                .build()
        );
    private final Setting<Integer> offsetX5 = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("LMBOffsetX"))
                .description("."))
                .sliderRange(-1000, 1000)
                .defaultValue(0))
                .build()
        );
    private final Setting<Integer> offsetY5 = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("LMBOffsetY"))
                .description("."))
                .sliderRange(-1000, 1000)
                .defaultValue(0))
                .build()
        );
    private final Setting<Integer> offsetX6 = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("LCPSOffsetX"))
                .description("."))
                .sliderRange(-1000, 1000)
                .defaultValue(0))
                .build()
        );
    private final Setting<Integer> offsetY6 = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("LCPSOffsetY"))
                .description("."))
                .sliderRange(-1000, 1000)
                .defaultValue(0))
                .build()
        );
    private final Setting<Integer> offsetX7 = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("RMBOffsetX"))
                .description("."))
                .sliderRange(-1000, 1000)
                .defaultValue(0))
                .build()
        );
    private final Setting<Integer> offsetY7 = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("RMBOffsetY"))
                .description("."))
                .sliderRange(-1000, 1000)
                .defaultValue(0))
                .build()
        );
    private final Setting<Integer> offsetX8 = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("RCPSOffsetX"))
                .description("."))
                .sliderRange(-1000, 1000)
                .defaultValue(0))
                .build()
        );
    private final Setting<Integer> offsetY8 = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("RCPSOffsetY"))
                .description("."))
                .sliderRange(-1000, 1000)
                .defaultValue(0))
                .build()
        );
    private final Setting<Integer> offsetX9 = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("QuadOffsetX"))
                .description("."))
                .sliderRange(-1000, 1000)
                .defaultValue(0))
                .build()
        );
    private final Setting<Integer> offsetY9 = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("QuadOffsetY"))
                .description("."))
                .sliderRange(-1000, 1000)
                .defaultValue(0))
                .build()
        );
    private final Setting<Integer> width1 = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("QuadWidth"))
                .description("."))
                .sliderRange(-1000, 1000)
                .defaultValue(0))
                .build()
        );
    private final Setting<Integer> height1 = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("QuadHeight"))
                .description("."))
                .sliderRange(-1000, 1000)
                .defaultValue(0))
                .build()
        );
    private final Setting<Double> factor = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("Factor"))
                .description("."))
                .sliderRange(0.0, 100.0)
                .defaultValue(0.5)
                .build()
        );
    private final Setting<Double> animationSpeed = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("animationSpeed"))
                .description("."))
                .sliderRange(0.0, 100.0)
                .defaultValue(0.05)
                .build()
        );
    private final Setting<Integer> offsetX10 = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("offsetX10"))
                .description("."))
                .sliderRange(-1000, 1000)
                .defaultValue(0))
                .build()
        );
    private final Setting<Integer> offsetY10 = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("offsetY10"))
                .description("."))
                .sliderRange(-1000, 1000)
                .defaultValue(0))
                .build()
        );
    private float animationProgressW = 0.0F;
    private float animationProgressA = 0.0F;
    private float animationProgressS = 0.0F;
    private float animationProgressD = 0.0F;
    private float animationProgressJump = 0.0F;
    private float animationProgressAttack = 0.0F;
    private float animationProgressUse = 0.0F;
    public KeyStrokes() {
        super(INFO);
        INSTANCE = this;
        MeteorClient.EVENT_BUS.subscribe(this);
    }

    public void render(HudRenderer render) {
        float posX = this.getX();
        float posY = this.getY();
        SettingColor wColor = this.lerpColor((SettingColor) this.bOffColor.get(), (SettingColor) this.bOnColor.get(), this.animationProgressW);
        this.drawKeyBackground(render.drawContext.getMatrices(), posX + 46.0F, posY + 42.0F, 40.0F, 40.0F, wColor, this.animationProgressW);
        SettingColor aColor = this.lerpColor((SettingColor) this.bOffColor.get(), (SettingColor) this.bOnColor.get(), this.animationProgressA);
        this.drawKeyBackground(render.drawContext.getMatrices(), posX + 4.0F, posY + 84.0F, 40.0F, 40.0F, aColor, this.animationProgressA);
        SettingColor sColor = this.lerpColor((SettingColor) this.bOffColor.get(), (SettingColor) this.bOnColor.get(), this.animationProgressS);
        this.drawKeyBackground(render.drawContext.getMatrices(), posX + 46.0F, posY + 84.0F, 40.0F, 40.0F, sColor, this.animationProgressS);
        SettingColor dColor = this.lerpColor((SettingColor) this.bOffColor.get(), (SettingColor) this.bOnColor.get(), this.animationProgressD);
        this.drawKeyBackground(render.drawContext.getMatrices(), posX + 88.0F, posY + 84.0F, 40.0F, 40.0F, dColor, this.animationProgressD);
        SettingColor jumpColor = this.lerpColor((SettingColor) this.bOffColor.get(), (SettingColor) this.bOnColor.get(), this.animationProgressJump);
        this.drawKeyBackground(render.drawContext.getMatrices(), posX + 4.0F, posY + 168.0F, 124.0F, 40.0F, jumpColor, this.animationProgressJump);
        SettingColor attackColor = this.lerpColor((SettingColor) this.bOffColor.get(), (SettingColor) this.bOnColor.get(), this.animationProgressAttack);
        this.drawKeyBackground(render.drawContext.getMatrices(), posX + 4.0F, posY + 126.0F, 61.0F, 40.0F, attackColor, this.animationProgressAttack);
        SettingColor useColor = this.lerpColor((SettingColor) this.bOffColor.get(), (SettingColor) this.bOnColor.get(), this.animationProgressUse);
        this.drawKeyBackground(render.drawContext.getMatrices(), posX + 67.0F, posY + 126.0F, 61.0F, 40.0F, useColor, this.animationProgressUse);
        this.setSize(120.0, 160.0);
        render.post(
            () -> {
                render.drawContext.getMatrices().push();
                this.updateAnimation(MeteorClient.mc.options.forwardKey.isPressed(), "W");
                this.drawKey(
                    render.drawContext.getMatrices(),
                    "W",
                    posX + 50.0F + (this.offsetX1.get()).intValue(),
                    posY + 50.0F + (this.offsetY1.get()).intValue(),
                    (SettingColor) this.sOffColor.get()
                );
                this.updateAnimation(MeteorClient.mc.options.leftKey.isPressed(), "A");
                this.drawKey(
                    render.drawContext.getMatrices(),
                    "A",
                    posX + 8.0F + (this.offsetX2.get()).intValue(),
                    posY + 90.0F + (this.offsetY2.get()).intValue(),
                    (SettingColor) this.sOffColor.get()
                );
                this.updateAnimation(MeteorClient.mc.options.backKey.isPressed(), "S");
                this.drawKey(
                    render.drawContext.getMatrices(),
                    "S",
                    posX + 50.0F + (this.offsetX3.get()).intValue(),
                    posY + 90.0F + (this.offsetY3.get()).intValue(),
                    (SettingColor) this.sOffColor.get()
                );
                this.updateAnimation(MeteorClient.mc.options.rightKey.isPressed(), "D");
                this.drawKey(
                    render.drawContext.getMatrices(),
                    "D",
                    posX + 92.0F + (this.offsetX4.get()).intValue(),
                    posY + 90.0F + (this.offsetY4.get()).intValue(),
                    (SettingColor) this.sOffColor.get()
                );
                this.updateAnimation(MeteorClient.mc.options.jumpKey.isPressed(), "Jump");
                this.drawKey(
                    render.drawContext.getMatrices(),
                    "———",
                    posX + 30.0F + (this.offsetX.get()).intValue(),
                    posY + 173.0F + (this.offsetY.get()).intValue(),
                    (SettingColor) this.sOffColor.get()
                );
                this.updateAnimation(MeteorClient.mc.options.attackKey.isPressed(), "LMB");
                this.drawMouseButton(
                    render.drawContext.getMatrices(),
                    "LMB",
                    posX + 18.0F + (this.offsetX5.get()).intValue(),
                    posY + 134.0F + (this.offsetY5.get()).intValue(),
                    (SettingColor) this.sOffColor.get()
                );
                this.drawMouseButton12(
                    render.drawContext.getMatrices(),
                    ClickCounter.getLeftCps() + " CPS",
                    posX + 14.0F + (this.offsetX6.get()).intValue(),
                    posY + 150.0F + (this.offsetY6.get()).intValue(),
                    (SettingColor) this.sOffColor.get()
                );
                this.updateAnimation(MeteorClient.mc.options.useKey.isPressed(), "RMB");
                this.drawMouseButton(
                    render.drawContext.getMatrices(),
                    "RMB",
                    posX + 80.0F + (this.offsetX7.get()).intValue(),
                    posY + 134.0F + (this.offsetY7.get()).intValue(),
                    (SettingColor) this.sOffColor.get()
                );
                this.drawMouseButton12(
                    render.drawContext.getMatrices(),
                    ClickCounter.getRightCps() + " CPS",
                    posX + 74.0F + (this.offsetX8.get()).intValue(),
                    posY + 150.0F + (this.offsetY8.get()).intValue(),
                    (SettingColor) this.sOffColor.get()
                );
                render.drawContext.getMatrices().pop();
            }
        );
    }

    private void updateAnimation(boolean keyPressed, String key) {
        switch (key) {
            case "W":
                this.animationProgressW = keyPressed
                    ? Math.min(this.animationProgressW + (this.animationSpeed.get()).floatValue(), 1.0F)
                    : Math.max(this.animationProgressW - (this.animationSpeed.get()).floatValue(), 0.0F);
                break;
            case "A":
                this.animationProgressA = keyPressed
                    ? Math.min(this.animationProgressA + (this.animationSpeed.get()).floatValue(), 1.0F)
                    : Math.max(this.animationProgressA - (this.animationSpeed.get()).floatValue(), 0.0F);
                break;
            case "S":
                this.animationProgressS = keyPressed
                    ? Math.min(this.animationProgressS + (this.animationSpeed.get()).floatValue(), 1.0F)
                    : Math.max(this.animationProgressS - (this.animationSpeed.get()).floatValue(), 0.0F);
                break;
            case "D":
                this.animationProgressD = keyPressed
                    ? Math.min(this.animationProgressD + (this.animationSpeed.get()).floatValue(), 1.0F)
                    : Math.max(this.animationProgressD - (this.animationSpeed.get()).floatValue(), 0.0F);
                break;
            case "Jump":
                this.animationProgressJump = keyPressed
                    ? Math.min(this.animationProgressJump + (this.animationSpeed.get()).floatValue(), 1.0F)
                    : Math.max(this.animationProgressJump - (this.animationSpeed.get()).floatValue(), 0.0F);
                break;
            case "LMB":
                this.animationProgressAttack = keyPressed
                    ? Math.min(this.animationProgressAttack + (this.animationSpeed.get()).floatValue(), 1.0F)
                    : Math.max(this.animationProgressAttack - (this.animationSpeed.get()).floatValue(), 0.0F);
                break;
            case "RMB":
                this.animationProgressUse = keyPressed
                    ? Math.min(this.animationProgressUse + (this.animationSpeed.get()).floatValue(), 1.0F)
                    : Math.max(this.animationProgressUse - (this.animationSpeed.get()).floatValue(), 0.0F);
        }
    }

    private SettingColor lerpColor(SettingColor start, SettingColor end, float t) {
        int r = (int) (start.r + (end.r - start.r) * t);
        int g = (int) (start.g + (end.g - start.g) * t);
        int b = (int) (start.b + (end.b - start.b) * t);
        int a = (int) (start.a + (end.a - start.a) * t);
        return new SettingColor(r, g, b, a);
    }

    private void drawKeyBackground(MatrixStack matrices, float x, float y, float width, float height, SettingColor color, float progress) {
        float scaleFactor = (this.factor.get()).floatValue() * progress;
        float widthScaleFactor = scaleFactor * (height / width);
        float expandedWidth = width + width * widthScaleFactor;
        float expandedHeight = height + height * scaleFactor;
        float offsetX = (expandedWidth - width) / 2.0F;
        float offsetY = (expandedHeight - height) / 2.0F;
        Render2DEngine.drawRoundedBlur(
            matrices,
            x + (this.offsetX9.get()).intValue(),
            y + (this.offsetY9.get()).intValue(),
            width + (this.width1.get()).intValue(),
            height + (this.height1.get()).intValue(),
            1.0F,
            new Color(color.getPacked(), true)
        );
        Render2DEngine.drawRect(
            matrices,
            x - offsetX + (this.offsetX9.get()).intValue(),
            y - offsetY + (this.offsetY9.get()).intValue(),
            expandedWidth + (this.width1.get()).intValue(),
            expandedHeight + (this.height1.get()).intValue(),
            new Color(color.getPacked(), true)
        );
    }

    private void drawKey(MatrixStack matrices, String key, float x, float y, SettingColor color) {
        FontRenderers.monsterrat_16.drawString(matrices, key, x + 9.0F, y + 8.0F, color.getPacked());
    }

    private void drawMouseButton(MatrixStack matrices, String s, float x, float y, SettingColor color) {
        FontRenderers.monsterrat_16.drawString(matrices, s, x, y, color.getPacked());
    }

    private void drawMouseButton12(MatrixStack matrices, String s, float x, float y, SettingColor color) {
        FontRenderers.monsterrat_12.drawCenteredString(matrices, s, x, y, color.getPacked());
    }


}
