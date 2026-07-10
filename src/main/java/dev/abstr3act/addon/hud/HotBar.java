package dev.abstr3act.addon.hud;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.abstr3act.addon.modules.Lacrymira.SilentTotemTest;
import dev.abstr3act.addon.utils.math.InventoryUtility;
import dev.abstr3act.addon.utils.render.Render2DEngine;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.settings.EnumSetting.Builder;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

public class HotBar extends HudElement {
    public static HotBar INSTANCE;
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
    public final Setting<Mode> lmode = this.sgGeneral
        .add(((Builder) ((Builder) ((Builder) new Builder().name("Mode")).description(".")).defaultValue(Mode.Merged)).build());
    public final Setting<Boolean> cancel = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("Cancel HUD"))
                .description(""))
                .defaultValue(true))
                .build()
        );
    private final Setting<SettingColor> color1 = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                .name("Color1"))
                .description("."))
                .defaultValue(new Color(255, 255, 255, 255))
                .build()
        );
    private final Setting<SettingColor> color2 = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                .name("Color2"))
                .description("."))
                .defaultValue(new Color(255, 255, 255, 255))
                .build()
        );
    private final Setting<SettingColor> color3 = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                .name("Color3"))
                .description("."))
                .defaultValue(new Color(255, 255, 255, 255))
                .build()
        );
    private final Setting<SettingColor> color4 = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                .name("Color4"))
                .description("."))
                .defaultValue(new Color(255, 255, 255, 255))
                .build()
        );
    private final Setting<SettingColor> color5 = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                .name("Color5"))
                .description("."))
                .defaultValue(new Color(255, 255, 255, 255))
                .build()
        );
    private final Setting<SettingColor> color6 = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                .name("Color6"))
                .description("."))
                .defaultValue(new Color(255, 255, 255, 255))
                .build()
        );
    private final Setting<SettingColor> color7 = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                .name("Color7"))
                .description("."))
                .defaultValue(new Color(255, 255, 255, 255))
                .build()
        );
    private final Setting<SettingColor> color8 = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                .name("Color8"))
                .description("."))
                .defaultValue(new Color(255, 255, 255, 255))
                .build()
        );
    private final Setting<Integer> offsetX1 = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("OffsetX1"))
                .description("."))
                .defaultValue(0))
                .sliderRange(-1000, 1000)
                .build()
        );
    private final Setting<Integer> offsetY1 = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("OffsetY1"))
                .description("."))
                .defaultValue(0))
                .sliderRange(-1000, 1000)
                .build()
        );
    private final Setting<Integer> offsetX2 = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("OffsetX2"))
                .description("."))
                .defaultValue(0))
                .sliderRange(-1000, 1000)
                .build()
        );
    private final Setting<Integer> offsetY2 = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("OffsetY2"))
                .description("."))
                .defaultValue(0))
                .sliderRange(-1000, 1000)
                .build()
        );
    private final Setting<Integer> offsetX3 = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("OffsetX3"))
                .description("."))
                .defaultValue(0))
                .sliderRange(-1000, 1000)
                .build()
        );
    private final Setting<Integer> offsetY3 = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("OffsetY3"))
                .description("."))
                .defaultValue(0))
                .sliderRange(-1000, 1000)
                .build()
        );
    private final Setting<Integer> offsetX4 = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("OffsetX4"))
                .description("."))
                .defaultValue(0))
                .sliderRange(-1000, 1000)
                .build()
        );
    private final Setting<Integer> offsetY4 = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("OffsetY4"))
                .description("."))
                .defaultValue(0))
                .sliderRange(-1000, 1000)
                .build()
        );
    private final Setting<Integer> offsetX5 = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("OffsetX5"))
                .description("."))
                .defaultValue(0))
                .sliderRange(-1000, 1000)
                .build()
        );
    private final Setting<Integer> offsetY5 = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("OffsetY5"))
                .description("."))
                .defaultValue(0))
                .sliderRange(-1000, 1000)
                .build()
        );
    private final Setting<Integer> offsetX6 = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("OffsetX6"))
                .description("."))
                .defaultValue(0))
                .sliderRange(-1000, 1000)
                .build()
        );
    private final Setting<Integer> offsetY6 = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("OffsetY6"))
                .description("."))
                .defaultValue(0))
                .sliderRange(-1000, 1000)
                .build()
        );
    private final Setting<Integer> var1 = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("Var1"))
                .description("."))
                .defaultValue(0))
                .sliderRange(-1000, 1000)
                .build()
        );
    private final Setting<Integer> offsetX7 = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("OffsetX7"))
                .description("."))
                .defaultValue(0))
                .sliderRange(-1000, 1000)
                .build()
        );
    private final Setting<Integer> offsetY7 = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("OffsetY7"))
                .description("."))
                .defaultValue(0))
                .sliderRange(-1000, 1000)
                .build()
        );
    private final Setting<Integer> offsetX8 = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("OffsetX8"))
                .description("."))
                .defaultValue(0))
                .sliderRange(-1000, 1000)
                .build()
        );
    private final Setting<Integer> offsetY8 = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("OffsetY8"))
                .description("."))
                .defaultValue(0))
                .sliderRange(-1000, 1000)
                .build()
        );
    private final Setting<Integer> var2 = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("Var2"))
                .description("."))
                .defaultValue(0))
                .sliderRange(-1000, 1000)
                .build()
        );
    private final Setting<Integer> var3 = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("Var3"))
                .description("."))
                .defaultValue(0))
                .sliderRange(-1000, 1000)
                .build()
        );
    private final Setting<Integer> var4 = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("Var4"))
                .description("."))
                .defaultValue(0))
                .sliderRange(-1000, 1000)
                .build()
        );
    private final Setting<Integer> var5 = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("Var5"))
                .description("."))
                .defaultValue(0))
                .sliderRange(-1000, 1000)
                .build()
        );
    private final Setting<Integer> var6 = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("Var6"))
                .description("."))
                .defaultValue(0))
                .sliderRange(-1000, 1000)
                .build()
        );
    private final Setting<Integer> offsetX9 = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("OffsetX9"))
                .description("."))
                .defaultValue(0))
                .sliderRange(-1000, 1000)
                .build()
        );
    private final Setting<Integer> offsetY9 = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("OffsetY9"))
                .description("."))
                .defaultValue(0))
                .sliderRange(-1000, 1000)
                .build()
        );
    private final Setting<Integer> offsetX10 = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("OffsetX10"))
                .description("."))
                .defaultValue(0))
                .sliderRange(-1000, 1000)
                .build()
        );
    private final Setting<Integer> offsetY10 = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("OffsetY10"))
                .description("."))
                .defaultValue(0))
                .sliderRange(-1000, 1000)
                .build()
        );
    private final Setting<Double> matrixX = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("matrixX"))
                .description("."))
                .defaultValue(0.0)
                .sliderRange(-1000.0, 1000.0)
                .build()
        );
    private final Setting<Double> matrixY = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("matrixY"))
                .description("."))
                .defaultValue(0.0)
                .sliderRange(-1000.0, 1000.0)
                .build()
        );
    private final Setting<Double> matrixZ = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("matrixZ"))
                .description("."))
                .defaultValue(0.0)
                .sliderRange(-1000.0, 1000.0)
                .build()
        );
    private final Setting<Double> translateX1 = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("translateX1"))
                .description("."))
                .defaultValue(0.0)
                .sliderRange(-1000.0, 1000.0)
                .build()
        );
    private final Setting<Double> translateY1 = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("translateY1"))
                .description("."))
                .defaultValue(0.0)
                .sliderRange(-1000.0, 1000.0)
                .build()
        );
    private final Setting<Double> translateZ1 = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("translateZ1"))
                .description("."))
                .defaultValue(0.0)
                .sliderRange(-1000.0, 1000.0)
                .build()
        );
    private final Setting<Double> translateX2 = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("translateX2"))
                .description("."))
                .defaultValue(0.0)
                .sliderRange(-1000.0, 1000.0)
                .build()
        );
    private final Setting<Double> translateY2 = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("translateY2"))
                .description("."))
                .defaultValue(0.0)
                .sliderRange(-1000.0, 1000.0)
                .build()
        );
    private final Setting<Double> translateZ2 = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("translateZ2"))
                .description("."))
                .defaultValue(0.0)
                .sliderRange(-1000.0, 1000.0)
                .build()
        );
    private final Setting<Integer> itemX = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("itemX"))
                .description("."))
                .defaultValue(0))
                .sliderRange(-1000, 1000)
                .build()
        );
    private final Setting<Integer> itemY = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("itemY"))
                .description("."))
                .defaultValue(0))
                .sliderRange(-1000, 1000)
                .build()
        );
    private final Setting<Double> factor1 = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("factor1"))
                .description("."))
                .defaultValue(19.8)
                .sliderRange(-1000.0, 1000.0)
                .build()
        );
    private final Setting<Integer> width1 = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("width1"))
                .description("."))
                .defaultValue(0))
                .sliderRange(-1000, 1000)
                .build()
        );
    private final Setting<Integer> height1 = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("height1"))
                .description("."))
                .defaultValue(0))
                .sliderRange(-1000, 1000)
                .build()
        );
    private final Setting<Integer> width2 = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("width2"))
                .description("."))
                .defaultValue(0))
                .sliderRange(-1000, 1000)
                .build()
        );
    private final Setting<Integer> height2 = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("height2"))
                .description("."))
                .defaultValue(0))
                .sliderRange(-1000, 1000)
                .build()
        );
    private final Setting<Integer> width3 = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("width3"))
                .description("."))
                .defaultValue(0))
                .sliderRange(-1000, 1000)
                .build()
        );
    private final Setting<Integer> height3 = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("height3"))
                .description("."))
                .defaultValue(0))
                .sliderRange(-1000, 1000)
                .build()
        );
    private final Setting<Integer> width4 = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("width4"))
                .description("."))
                .defaultValue(0))
                .sliderRange(-1000, 1000)
                .build()
        );
    private final Setting<Integer> height4 = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("height4"))
                .description("."))
                .defaultValue(0))
                .sliderRange(-1000, 1000)
                .build()
        );
    private final Setting<Integer> width5 = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("width5"))
                .description("."))
                .defaultValue(0))
                .sliderRange(-1000, 1000)
                .build()
        );
    private final Setting<Integer> height5 = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("height5"))
                .description("."))
                .defaultValue(0))
                .sliderRange(-1000, 1000)
                .build()
        );
    private final Setting<Integer> width6 = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("width6"))
                .description("."))
                .defaultValue(0))
                .sliderRange(-1000, 1000)
                .build()
        );
    private final Setting<Integer> height6 = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("height6"))
                .description("."))
                .defaultValue(0))
                .sliderRange(-1000, 1000)
                .build()
        );
    private final Setting<Integer> width7 = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("width7"))
                .description("."))
                .defaultValue(0))
                .sliderRange(-1000, 1000)
                .build()
        );
    private final Setting<Integer> height7 = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("height7"))
                .description("."))
                .defaultValue(1))
                .sliderRange(-1000, 1000)
                .build()
        );    public static final HudElementInfo<HotBar> INFO = new HudElementInfo(dev.abstr3act.addon.Compassion.HUD_GROUP, "HotBar", "ClientBoard", HotBar::new);
    public HotBar() {
        super(INFO);
        INSTANCE = this;
        MeteorClient.EVENT_BUS.subscribe(this);
    }

    public void render(HudRenderer renderer) {
        this.setSize(201.0, 20.0);
        if (MeteorClient.mc.player != null) {
            MatrixStack matrices = renderer.drawContext.getMatrices();
            int i = this.getX();
            int j = this.getY();
            if (MeteorClient.mc.player.getOffHandStack().isEmpty()) {
                Render2DEngine.drawRoundedBlur(
                    matrices,
                    i - 180 + this.offsetX1.get(),
                    j - 50 + this.offsetY1.get(),
                    360 + this.width1.get(),
                    40 + this.height1.get(),
                    1.0F,
                    new java.awt.Color(((SettingColor) this.color1.get()).getPacked(), true)
                );
            } else if (this.lmode.get() == Mode.Merged) {
                Render2DEngine.drawRoundedBlur(
                    matrices,
                    i - 222 + this.offsetX2.get(),
                    j - 50 + this.offsetY2.get(),
                    402 + this.width2.get(),
                    40 + this.height2.get(),
                    1.0F,
                    new java.awt.Color(((SettingColor) this.color2.get()).getPacked(), true)
                );
                Render2DEngine.drawRect(
                    renderer.drawContext.getMatrices(),
                    (float) (i - 254 + this.offsetX3.get()),
                    (float) (j - 46 + this.offsetY3.get()),
                    (float) (2 + this.width3.get()),
                    (float) (30 + this.height3.get()),
                    new java.awt.Color(1157627903, true)
                );
            } else {
                Render2DEngine.drawRoundedBlur(
                    matrices,
                    i - 180 + this.offsetX4.get(),
                    j - 50 + this.offsetY4.get(),
                    360 + this.width4.get(),
                    40 + this.height4.get(),
                    1.0F,
                    new java.awt.Color(((SettingColor) this.color3.get()).getPacked(), true)
                );
                Render2DEngine.drawRoundedBlur(
                    matrices,
                    i - 225.0F + (this.offsetX5.get()).intValue(),
                    j - 50 + this.offsetY5.get(),
                    40 + this.width5.get(),
                    40 + this.height5.get(),
                    1.0F,
                    new java.awt.Color(((SettingColor) this.color4.get()).getPacked(), true)
                );
            }

            java.awt.Color c = new java.awt.Color(((SettingColor) this.color5.get()).getPacked(), true);
            Render2DEngine.drawRect(
                matrices,
                i - 176
                    + MeteorClient.mc.player.getInventory().selectedSlot * (this.factor1.get()).floatValue() * 2.0F
                    + (this.offsetX6.get()).intValue(),
                (float) (j - 48 + this.offsetY6.get()),
                (float) (34 + this.width6.get()),
                (float) (34 + this.height6.get()),
                c
            );
            Render2DEngine.drawRect(
                renderer.drawContext.getMatrices(),
                i - 176
                    + MeteorClient.mc.player.getInventory().selectedSlot * (this.factor1.get()).floatValue() * 2.0F
                    + (this.offsetX6.get()).intValue()
                    + (this.offsetX10.get()).intValue(),
                (float) (j - 48 + this.offsetY6.get() + this.offsetY10.get()),
                (float) (34 + this.width6.get() + this.width7.get()),
                (float) (this.height7.get()).intValue(),
                new java.awt.Color(((SettingColor) this.color6.get()).getPacked(), true)
            );
            if (((SilentTotemTest) Modules.get().get(SilentTotemTest.class)).isActive() && InventoryUtility.findItemInHotBar(Items.TOTEM_OF_UNDYING).found()) {
                this.drawSlot(matrices, i, j, (Color) this.color7.get(), InventoryUtility.findItemInHotBar(Items.TOTEM_OF_UNDYING).slot());
            }

            this.renderHotBarItems(renderer.drawContext);
        }
    }

    private void drawSlot(MatrixStack matrices, int i, int j, Color c, int slot) {
        Render2DEngine.drawRect(
            matrices,
            (i - 176 + slot * (this.factor1.get()).floatValue() * 2.0F + (this.offsetX6.get()).intValue()),
            (j - 48 + this.offsetY6.get()),
            (34 + this.width6.get()),
            (34 + this.height6.get()),
            (Color) this.color7.get()
        );
        Render2DEngine.drawRect(
            matrices,
            i - 176
                + slot * (this.factor1.get()).floatValue() * 2.0F
                + (this.offsetX6.get()).intValue()
                + (this.offsetX10.get()).intValue(),
            (float) (j - 48 + this.offsetY6.get() + this.offsetY10.get()),
            (float) (34 + this.width6.get() + this.width7.get()),
            (float) (this.height7.get()).intValue(),
            new java.awt.Color(((SettingColor) this.color8.get()).getPacked(), true)
        );
    }

    public void renderHotBarItems(DrawContext context) {
        PlayerEntity playerEntity = MeteorClient.mc.player;
        if (playerEntity != null) {
            int i = this.getX();
            int o = this.getY() - 32 - 6 + this.var1.get();
            if (!MeteorClient.mc.player.getOffHandStack().isEmpty()) {
                if (this.lmode.get() == Mode.Merged) {
                    this.renderHotbarItem(context, i - 218 + this.offsetX7.get(), o - 10 + this.offsetY7.get(), playerEntity.getOffHandStack());
                } else {
                    this.renderHotbarItem(context, i - 222 + this.offsetX8.get(), o - 10 + this.offsetY8.get(), playerEntity.getOffHandStack());
                }
            }

            for (int m = 0; m < 9; m++) {
                int n = i - (180 + this.var2.get()) + m * (40 + this.var3.get()) + 4 + this.var4.get();
                if (m == MeteorClient.mc.player.getInventory().selectedSlot) {
                    this.renderHotbarItem(context, n, o - 14 + this.var5.get(), (ItemStack) playerEntity.getInventory().main.get(m));
                } else {
                    this.renderHotbarItem(context, n, o - 10 + this.var6.get(), (ItemStack) playerEntity.getInventory().main.get(m));
                }
            }
        }
    }

    private void renderHotbarItem(DrawContext context, int i, int j, ItemStack itemStack) {
        if (!itemStack.isEmpty()) {
            context.getMatrices().push();
            context.getMatrices().translate(i + 16 + this.translateX1.get(), j + 24 + this.translateY1.get(), 0.0 + this.translateZ1.get());
            context.getMatrices()
                .scale(
                    1.8F + (this.matrixX.get()).floatValue(), 1.8F + (this.matrixY.get()).floatValue(), 2.0F + (this.matrixZ.get()).floatValue()
                );
            context.getMatrices()
                .translate(
                    (float) (-(i + 16) + this.translateX2.get()), (float) (-(j + 24) + this.translateY2.get()), 0.0 + this.translateZ2.get()
                );
            context.drawItem(itemStack, i + this.itemX.get(), j + this.itemY.get());
            context.drawItemInSlot(MeteorClient.mc.textRenderer, itemStack, i + this.itemX.get(), j + this.itemY.get());
            context.getMatrices().pop();
        }
    }

    private void renderExperienceBar(DrawContext context, int x) {
        MeteorClient.mc.getProfiler().push("expBar");
        int i = MeteorClient.mc.player.getNextLevelExperience();
        if (i > 0) {
            int k = (int) (MeteorClient.mc.player.experienceProgress * 183.0F);
            int l = context.getScaledWindowHeight() - 32 + 3;
            RenderSystem.enableBlend();
            Render2DEngine.drawRect(context.getMatrices(), (float) x, (float) l, 182.0F, 5.0F, new java.awt.Color(0, 0, 0, 150));
            if (k > 0) {
                Render2DEngine.drawRect(context.getMatrices(), (float) x, (float) l, (float) k, 5.0F, new java.awt.Color(72, 255, 60, 150));
            }

            RenderSystem.disableBlend();
        }

        MeteorClient.mc.getProfiler().pop();
    }

    public static enum Mode {
        Merged,
        Separately;
    }


}
