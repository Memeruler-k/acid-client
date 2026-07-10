package dev.abstr3act.addon.hud;

import dev.abstr3act.addon.modules.Compassion.BlurSetting;
import dev.abstr3act.addon.utils.render.Render2DEngine;
import dev.abstr3act.addon.utils.render.shaders.AnimationUtility;
import kotlin.Pair;
import meteordevelopment.meteorclient.renderer.Framebuffer;
import meteordevelopment.meteorclient.renderer.Renderer2D;
import meteordevelopment.meteorclient.renderer.Shader;
import meteordevelopment.meteorclient.settings.ModuleListSetting.Builder;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.hud.Alignment;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import net.minecraft.client.gui.DrawContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NewArrayList extends NewHudElement {
    private static final Color WHITE = new Color();    public static final HudElementInfo<NewArrayList> INFO = new HudElementInfo(
        dev.abstr3act.addon.Compassion.HUD_GROUP, "new-array-list", "Displays your active modules.", NewArrayList::new
    );
    private final Framebuffer[] fbos = new Framebuffer[6];
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
    public final Setting<Double> blend = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("blend"))
                .description("Rainbow spread of rainbow color mode."))
                .defaultValue(10.0)
                .sliderMin(1.0)
                .sliderMax(15.0)
                .build()
        );
    public final Setting<Integer> blurRadius = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("blurRadius"))
                .description("."))
                .defaultValue(1))
                .sliderMin(0)
                .sliderMax(100)
                .build()
        );
    public final Setting<Double> blurOpacity = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("blurOpacity"))
                .description("."))
                .defaultValue(0.55F)
                .sliderMin(0.0)
                .sliderMax(1.0)
                .build()
        );
    public final Setting<Double> blurStrength = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("blurStrength"))
                .description("."))
                .defaultValue(20.0)
                .sliderMin(5.0)
                .sliderMax(50.0)
                .build()
        );
    public final Setting<Double> blurStrength2test = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("blurStrength2test"))
                .description("."))
                .defaultValue(20.0)
                .sliderMin(5.0)
                .sliderMax(50.0)
                .build()
        );
    private final Setting<List<Module>> hiddenModules = this.sgGeneral
        .add(((Builder) ((Builder) new Builder().name("hidden-modules")).description("Which modules not to show in the list.")).build());
    private final Setting<Sort> sort = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) new meteordevelopment.meteorclient.settings.EnumSetting.Builder()
                .name("sort"))
                .description("How to sort active modules."))
                .defaultValue(Sort.Biggest))
                .build()
        );
    private final Setting<Boolean> activeInfo = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("additional-info"))
                .description("Shows additional info from the module next to the name in the active modules list."))
                .defaultValue(true))
                .build()
        );
    private final Setting<SettingColor> moduleInfoColor = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                .name("module-info-color"))
                .description("Color of module info text."))
                .defaultValue(new SettingColor(175, 175, 175))
                .visible(this.activeInfo::get))
                .build()
        );
    private final Setting<ColorMode> colorMode = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) new meteordevelopment.meteorclient.settings.EnumSetting.Builder()
                .name("color-mode"))
                .description("What color to use for active modules."))
                .defaultValue(ColorMode.Rainbow))
                .build()
        );
    private final Setting<SettingColor> flatColor = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                .name("flat-color"))
                .description("Color for flat color mode."))
                .defaultValue(new SettingColor(225, 25, 25))
                .visible(() -> this.colorMode.get() == ColorMode.Flat))
                .build()
        );
    private final Setting<Double> rainbowSpeed = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("rainbow-speed"))
                .description("Rainbow speed of rainbow color mode."))
                .defaultValue(0.05)
                .sliderMin(0.01)
                .sliderMax(0.2)
                .decimalPlaces(4)
                .visible(() -> this.colorMode.get() == ColorMode.Rainbow))
                .build()
        );
    public final Setting<Double> rainbowSpread = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("rainbow-spread"))
                .description("Rainbow spread of rainbow color mode."))
                .defaultValue(0.01)
                .sliderMin(0.001)
                .sliderMax(0.05)
                .decimalPlaces(4)
                .visible(() -> this.colorMode.get() == ColorMode.Rainbow))
                .build()
        );
    private final Setting<Double> rainbowSaturation = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("rainbow-saturation"))
                .defaultValue(1.0)
                .sliderRange(0.0, 1.0)
                .visible(() -> this.colorMode.get() == ColorMode.Rainbow))
                .build()
        );
    private final Setting<Double> rainbowBrightness = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("rainbow-brightness"))
                .defaultValue(1.0)
                .sliderRange(0.0, 1.0)
                .visible(() -> this.colorMode.get() == ColorMode.Rainbow))
                .build()
        );
    private final Setting<Double> doubleColorSpread = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("double-spread"))
                .description("Double spread of rainbow color mode."))
                .defaultValue(0.01)
                .sliderMin(0.001)
                .sliderMax(0.05)
                .decimalPlaces(4)
                .visible(() -> this.colorMode.get() == ColorMode.DoubleColor))
                .build()
        );
    private final Setting<SettingColor> flatColor2 = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                .name("flat-color-2"))
                .description("Color for flat color mode."))
                .defaultValue(new SettingColor(225, 25, 25))
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
        );
    private final Setting<Boolean> shadow = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("shadow"))
                .description("Renders shadow behind text."))
                .defaultValue(true))
                .build()
        );
    private final Setting<Boolean> bgs = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("BG-Shadow"))
                .description("Renders shadow behind background."))
                .defaultValue(true))
                .build()
        );
    private final Setting<Alignment> alignment = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) new meteordevelopment.meteorclient.settings.EnumSetting.Builder()
                .name("alignment"))
                .description("Horizontal alignment."))
                .defaultValue(Alignment.Auto))
                .build()
        );
    private final Setting<Boolean> outlines = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("outlines"))
                .description("Whether or not to render outlines"))
                .defaultValue(false))
                .build()
        );
    private final Setting<Integer> outlineWidth = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("outline-width"))
                .description("Outline width"))
                .defaultValue(2))
                .min(-5)
                .sliderMin(-5)
                .visible(this.outlines::get))
                .build()
        );
    private final Setting<Integer> blurWidth = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("blurWidth"))
                .description("Outline width"))
                .defaultValue(0))
                .min(-5)
                .sliderMin(-5)
                .visible(this.outlines::get))
                .build()
        );
    private final Setting<Integer> ow = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("outline-width-2"))
                .description("Outline width"))
                .defaultValue(0))
                .sliderRange(-1000, 1000)
                .visible(this.outlines::get))
                .build()
        );
    private final Setting<Integer> hw = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("outline-height-2"))
                .description("Outline width"))
                .defaultValue(0))
                .sliderRange(-1000, 1000)
                .visible(this.outlines::get))
                .build()
        );
    private final Setting<Integer> ow1 = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("outline-width-2"))
                .description("Outline width"))
                .defaultValue(0))
                .sliderRange(-1000, 1000)
                .visible(this.outlines::get))
                .build()
        );
    private final Setting<Integer> hw1 = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("outline-height-2"))
                .description("Outline width"))
                .defaultValue(0))
                .sliderRange(-1000, 1000)
                .visible(this.outlines::get))
                .build()
        );
    private final Setting<Integer> outlineHeight = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("outline-Height"))
                .description("Outline wHeight"))
                .defaultValue(2))
                .min(-5)
                .sliderMin(-5)
                .visible(this.outlines::get))
                .build()
        );
    private final Setting<Integer> t1 = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("offset-1"))
                .description("Outline width"))
                .defaultValue(0))
                .sliderRange(-10, 10)
                .visible(this.outlines::get))
                .build()
        );
    private final Setting<Integer> t2 = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("offset-2"))
                .description("Outline width"))
                .defaultValue(0))
                .sliderRange(-10, 10)
                .visible(this.outlines::get))
                .build()
        );
    private final Setting<Integer> t3 = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("offset-3"))
                .description("Outline width"))
                .defaultValue(0))
                .sliderRange(-10, 10)
                .visible(this.outlines::get))
                .build()
        );
    private final Setting<Integer> t4 = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("offset-4"))
                .description("Outline width"))
                .defaultValue(0))
                .sliderRange(-10, 10)
                .visible(this.outlines::get))
                .build()
        );
    private final Setting<Integer> t5 = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("offset-5"))
                .description("Outline width"))
                .defaultValue(0))
                .sliderRange(-100, 100)
                .visible(this.outlines::get))
                .build()
        );
    private final Setting<Integer> t6 = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("offset-6"))
                .description("Outline width"))
                .defaultValue(0))
                .sliderRange(-100, 100)
                .visible(this.outlines::get))
                .build()
        );
    private final Setting<Integer> t7 = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("offset-7"))
                .description("Outline width"))
                .defaultValue(0))
                .sliderRange(-100, 100)
                .visible(this.outlines::get))
                .build()
        );
    private final Setting<Integer> t8 = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("space"))
                .description("space height"))
                .defaultValue(0))
                .sliderRange(-100, 100)
                .visible(this.outlines::get))
                .build()
        );
    private final Setting<Integer> radius = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("radius"))
                .description("."))
                .defaultValue(0))
                .sliderRange(0, 50)
                .visible(this.outlines::get))
                .build()
        );
    private final Setting<Integer> sX = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("shadow-offset-x"))
                .description("Outline width"))
                .defaultValue(0))
                .sliderRange(-100, 100)
                .visible(this.outlines::get))
                .build()
        );
    private final Setting<Integer> sY = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("shadow-offset-y"))
                .description("Outline width"))
                .defaultValue(0))
                .sliderRange(-100, 100)
                .visible(this.outlines::get))
                .build()
        );
    private final Setting<Integer> wX = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("shadow-offset-width"))
                .description("Outline width"))
                .defaultValue(0))
                .sliderRange(-100, 100)
                .visible(this.outlines::get))
                .build()
        );
    private final Setting<Integer> wY = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("shadow-offset-height"))
                .description("Outline width"))
                .defaultValue(0))
                .sliderRange(-100, 100)
                .visible(this.outlines::get))
                .build()
        );
    private final Setting<Boolean> strip = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("strip"))
                .description("Whether or not to render outlines"))
                .defaultValue(false))
                .build()
        );
    private final Setting<Boolean> right = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("right"))
                .description("Whether or not to render outlines"))
                .defaultValue(false))
                .build()
        );
    private final Setting<SettingColor> shadowColor = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                .name("ShadowColor"))
                .description("."))
                .defaultValue(new SettingColor(0, 0, 0))
                .build()
        );
    private final List<Module> modules = new ArrayList<>();
    private final Color rainbow = new Color(255, 255, 255);
    private final Color doubleColor = new Color(255, 255, 255);
    private final Map<Module, Pair<Float, Float>> modulePositions = new HashMap<>();
    double textLength = 0.0;
    double textHeight = 0.0;
    private Shader shaderDown;
    private Shader shaderUp;
    private DrawContext context;
    private boolean enabled;
    private long fadeEndAt;
    private double rainbowHue1;
    private double doubleColor1;
    private double rainbowHue2;
    private double doubleColor2;
    private float hAnimation;
    private float vAnimation;
    private double prevX;
    private double prevTextLength;
    private Color prevColor = new Color();
    public NewArrayList() {
        super(INFO);
    }

    public void tick(HudRenderer renderer) {
        this.modules.clear();

        for (Module module : Modules.get().getActive()) {
            if (!(this.hiddenModules.get()).contains(module)) {
                this.modules.add(module);
            }
        }

        if (this.modules.isEmpty()) {
            if (this.isInEditor()) {
                this.setSize(
                    renderer.textWidth("New ArrayList", this.shadow.get(), this.getScale()), renderer.textHeight(this.shadow.get(), this.getScale())
                );
            }
        } else {
            this.modules.sort((e1, e2) -> {
                return switch ((Sort) this.sort.get()) {
                    case Alphabetical -> e1.title.compareTo(e2.title);
                    case Biggest ->
                        Double.compare(this.getModuleWidth(renderer, e2), this.getModuleWidth(renderer, e1));
                    case Smallest ->
                        Double.compare(this.getModuleWidth(renderer, e1), this.getModuleWidth(renderer, e2));
                };
            });
            double width = 0.0;
            double height = 0.0;

            for (int i = 0; i < this.modules.size(); i++) {
                Module modulex = this.modules.get(i);
                width = Math.max(width, this.getModuleWidth(renderer, modulex));
                height += renderer.textHeight(this.shadow.get(), this.getScale());
                if (i > 0) {
                    height += 2.0;
                }
            }

            this.setSize(width, height);
        }
    }

    public void render(HudRenderer renderer) {
        if (renderer != null) {
            double x = this.x;
            double y = this.y;
            if (this.modules.isEmpty()) {
                if (this.isInEditor()) {
                    renderer.text("Active Modules", x, y, WHITE, this.shadow.get(), this.getScale());
                    this.setSize(50.0, 50.0);
                }
            } else {
                this.rainbowHue1 = this.rainbowHue1 + this.rainbowSpeed.get() * renderer.delta;
                if (this.rainbowHue1 > 1.0) {
                    this.rainbowHue1--;
                } else if (this.rainbowHue1 < 0.0) {
                    this.rainbowHue1++;
                }

                this.rainbowHue2 = this.rainbowHue1;
                this.prevX = x;
                Color color = new Color(255, 255, 255, 255);

                for (int i = 0; i < this.modules.size(); i++) {
                    double offset = this.alignX(this.getModuleWidth(renderer, this.modules.get(i)), (Alignment) this.alignment.get());
                    this.modulePositions.keySet().removeIf(module -> !this.modules.contains(module));
                    switch ((ColorMode) this.colorMode.get()) {
                        case Random:
                            color = this.modules.get(i).color;
                            break;
                        case Rainbow:
                            this.rainbowHue2 = this.rainbowHue2 + this.rainbowSpread.get();
                            int c = java.awt.Color.HSBtoRGB(
                                (float) this.rainbowHue2, (this.rainbowSaturation.get()).floatValue(), (this.rainbowBrightness.get()).floatValue()
                            );
                            this.rainbow.r = Color.toRGBAR(c);
                            this.rainbow.g = Color.toRGBAG(c);
                            this.rainbow.b = Color.toRGBAB(c);
                            color = this.rainbow;
                            break;
                        case DoubleColor:
                            this.rainbowHue1 = this.rainbowHue1 + this.rainbowSpeed.get() * renderer.delta;
                            if (this.rainbowHue1 > 1.0) {
                                this.rainbowHue1--;
                            } else if (this.rainbowHue1 < 0.0) {
                                this.rainbowHue1++;
                            }

                            double progress = Math.sin((this.rainbowHue1 + i / this.modules.size()) * Math.PI * 2.0);
                            progress = (progress + 1.0) / 2.0;
                            Color redColor = (Color) this.dp1.get();
                            Color whiteColor = (Color) this.dp2.get();
                            int red = (int) (redColor.r * (1.0 - progress) + whiteColor.r * progress);
                            int green = (int) (redColor.g * (1.0 - progress) + whiteColor.g * progress);
                            int blue = (int) (redColor.b * (1.0 - progress) + whiteColor.b * progress);
                            this.rainbow.set(red, green, blue, 255);
                            color = this.rainbow;
                    }

                    float targetX = (float) (x + offset);
                    float targetY = (float) y;
                    Pair<Float, Float> prevPos = this.modulePositions.get(this.modules.get(i));
                    if (prevPos == null) {
                        prevPos = new Pair(targetX + 50.0F, targetY);
                    }

                    float newX = AnimationUtility.ease((Float) prevPos.getFirst(), targetX, 5.0F);
                    float newY = AnimationUtility.ease((Float) prevPos.getSecond(), targetY, 5.0F);
                    this.modulePositions.put(this.modules.get(i), new Pair(newX, newY));
                    this.renderModule(renderer, this.modules, i, newX, newY, color);
                    y += 2.0 + renderer.textHeight(this.shadow.get(), this.getScale());
                }
            }
        }
    }

    private void renderModule(HudRenderer renderer, List<Module> modules, int index, double x, double y, Color c) {
        Module module = modules.get(index);
        Color color;
        if (((ColorMode) this.colorMode.get()).equals(ColorMode.Flat)) {
            color = (Color) this.flatColor.get();
        } else {
            color = c;
        }

        Color color1 = (Color) this.flatColor2.get();
        renderer.text(module.title, x, y, color, this.shadow.get(), this.getScale());
        double emptySpace = renderer.textWidth(" ", this.shadow.get(), this.getScale());
        double textHeight = renderer.textHeight(this.shadow.get(), this.getScale());
        double textLength = renderer.textWidth(module.title, this.shadow.get(), this.getScale());
        this.textLength = textLength;
        this.textHeight = textHeight;
        if (this.activeInfo.get()) {
            String info = module.getInfoString() == null ? null : "[" + module.getInfoString() + "]";
            if (info != null) {
                renderer.text(info, x + emptySpace + textLength, y, (Color) this.moduleInfoColor.get(), this.shadow.get(), this.getScale());
                textLength += emptySpace + renderer.textWidth(info, this.shadow.get(), this.getScale());
            }
        }

        if (this.outlines.get()) {
            double rectHeight = textHeight + 4.0 + this.outlineWidth.get() * 2 + (this.t5.get()).intValue();
            double rectWidth = textLength + 4.0 + this.outlineWidth.get() * 2 + (this.t7.get()).intValue();
            double adjustedX = x - (this.outlineWidth.get()).intValue() - 4.0 + (this.t6.get()).intValue();
            if (this.strip.get()) {
                if (!this.right.get()) {
                    Render2DEngine.drawBlurredShadow(
                        renderer.drawContext.getMatrices(),
                        (float) (adjustedX - 3.0),
                        (float) (y - (this.outlineWidth.get()).intValue()),
                        3.0F,
                        (float) rectHeight,
                        ((BlurSetting) Modules.get().get(BlurSetting.class)).radius.get(),
                        this.rainbow
                    );
                    Renderer2D.COLOR
                        .quad(adjustedX - 3.0, y - (this.outlineWidth.get()).intValue(), 3.0, rectHeight, this.rainbow, this.rainbow, this.rainbow, this.rainbow);
                } else {
                    Render2DEngine.drawBlurredShadow(
                        renderer.drawContext.getMatrices(),
                        (float) (adjustedX + rectWidth),
                        (float) (y - (this.outlineWidth.get()).intValue()),
                        3.0F,
                        (float) rectHeight,
                        ((BlurSetting) Modules.get().get(BlurSetting.class)).radius.get(),
                        this.rainbow
                    );
                    Renderer2D.COLOR
                        .quad(
                            adjustedX + rectWidth, y - (this.outlineWidth.get()).intValue(), 3.0, rectHeight, this.rainbow, this.rainbow, this.rainbow, this.rainbow
                        );
                }
            }

            Render2DEngine.drawRoundedBlur(
                renderer.drawContext.getMatrices(),
                (float) (x - (this.outlineWidth.get()).intValue() + (this.t1.get()).intValue()),
                (float) (y - (this.outlineWidth.get()).intValue() + (this.t2.get()).intValue()),
                (float) (textLength - 4.0 + this.outlineWidth.get() * 2) + (this.t3.get()).intValue(),
                (float) (textHeight + 4.0 + this.outlineHeight.get() * 2) + (this.t4.get()).intValue(),
                1.0F,
                new java.awt.Color(((SettingColor) this.flatColor2.get()).getPacked()),
                (this.blurStrength.get()).floatValue(),
                (this.blurOpacity.get()).floatValue()
            );
        }

        this.prevTextLength = textLength;
        this.prevColor = color1;
    }

    private double getModuleWidth(HudRenderer renderer, Module module) {
        double width = renderer.textWidth(module.title, this.shadow.get(), this.getScale());
        if (this.activeInfo.get()) {
            String info = module.getInfoString() == null ? null : "[" + module.getInfoString() + "]";
            if (info != null) {
                width += renderer.textWidth(" ", this.shadow.get(), this.getScale()) + renderer.textWidth(info, this.shadow.get(), this.getScale());
            }
        }

        return width;
    }

    private double getScale() {
        return -1.0;
    }

    @Override
    public void onRender2D(DrawContext context) {
    }

    public static enum ColorMode {
        Flat,
        Random,
        Rainbow,
        DoubleColor;
    }

    public static enum Sort {
        Alphabetical,
        Biggest,
        Smallest;
    }


}
