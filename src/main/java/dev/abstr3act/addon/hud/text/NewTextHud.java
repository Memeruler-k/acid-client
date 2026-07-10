package dev.abstr3act.addon.hud.text;

import dev.abstr3act.addon.utils.render.Render2DEngine;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.gui.utils.StarscriptTextBoxRenderer;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.StringSetting.Builder;
import meteordevelopment.meteorclient.systems.hud.Hud;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.utils.misc.MeteorStarscript;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.starscript.Script;
import meteordevelopment.starscript.Section;
import meteordevelopment.starscript.compiler.Compiler;
import meteordevelopment.starscript.compiler.Parser;
import meteordevelopment.starscript.compiler.Parser.Result;
import meteordevelopment.starscript.utils.Error;
import meteordevelopment.starscript.utils.StarscriptError;

import java.util.List;

public class NewTextHud extends HudElement {
    private static final Color WHITE = new Color();
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
    private final SettingGroup sgShown = this.settings.createGroup("Shown");
    private final SettingGroup sgScale = this.settings.createGroup("Scale");
    private final SettingGroup sgBackground = this.settings.createGroup("Background");
    public final Setting<Boolean> background = this.sgBackground
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("background"))
                .description("Displays background."))
                .defaultValue(false))
                .build()
        );
    public final Setting<SettingColor> backgroundColor = this.sgBackground
        .add(
            ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                .name("background-color"))
                .description("Color used for the background."))
                .visible(this.background::get))
                .defaultValue(new SettingColor(25, 25, 25, 50))
                .build()
        );
    private double originalWidth;
    private double originalHeight;
    public final Setting<Integer> border = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("border"))
                .description("How much space to add around the text."))
                .defaultValue(0))
                .onChanged(integer -> super.setSize(this.originalWidth + integer * 2, this.originalHeight + integer * 2)))
                .build()
        );
    private boolean needsCompile;
    private boolean recalculateSize;
    public final Setting<Boolean> shadow = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("shadow"))
                .description("Renders shadow behind text."))
                .defaultValue(true))
                .onChanged(aBoolean -> this.recalculateSize = true))
                .build()
        );
    public final Setting<Boolean> customScale = this.sgScale
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("custom-scale"))
                .description("Applies custom text scale rather than the global one."))
                .defaultValue(false))
                .onChanged(integer -> this.recalculateSize = true))
                .build()
        );
    public final Setting<Double> scale = this.sgScale
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("scale"))
                .description("Custom scale."))
                .visible(this.customScale::get))
                .defaultValue(1.0)
                .onChanged(integer -> this.recalculateSize = true))
                .min(0.5)
                .sliderRange(0.5, 3.0)
                .build()
        );
    private int timer;
    public final Setting<Integer> updateDelay = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("update-delay"))
                .description("Update delay in ticks"))
                .defaultValue(4))
                .onChanged(integer -> {
                    if (this.timer > integer) {
                        this.timer = integer;
                    }
                }))
                .min(0)
                .build()
        );
    private Script script;
    private Script conditionScript;
    private Section section;
    private boolean firstTick = true;
    public final Setting<String> text = this.sgGeneral
        .add(
            ((Builder) ((Builder) ((Builder) ((Builder) new Builder().name("text")).description("Text to display with Starscript.")).defaultValue(MeteorClient.NAME))
                .onChanged(s -> this.recompile()))
                .wide()
                .renderer(StarscriptTextBoxRenderer.class)
                .build()
        );
    public final Setting<Shown> shown = this.sgShown
        .add(
            ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) new meteordevelopment.meteorclient.settings.EnumSetting.Builder()
                .name("shown"))
                .description("When this text element is shown."))
                .defaultValue(Shown.Always))
                .onChanged(s -> this.recompile()))
                .build()
        );
    public final Setting<String> condition = this.sgShown
        .add(
            ((Builder) ((Builder) ((Builder) ((Builder) new Builder().name("condition")).description("Condition to check when shown is not Always."))
                .visible(() -> this.shown.get() != Shown.Always))
                .onChanged(s -> this.recompile()))
                .renderer(StarscriptTextBoxRenderer.class)
                .build()
        );
    private boolean empty = false;
    private boolean visible;

    public NewTextHud(HudElementInfo<NewTextHud> info) {
        super(info);
        this.needsCompile = true;
    }

    public static Color getSectionColor(int i) {
        List<SettingColor> colors = (List<SettingColor>) Hud.get().textColors.get();
        return i >= 0 && i < colors.size() ? (Color) colors.get(i) : WHITE;
    }

    private void recompile() {
        this.firstTick = true;
        this.needsCompile = true;
    }

    public void setSize(double width, double height) {
        this.originalWidth = width;
        this.originalHeight = height;
        super.setSize(width + this.border.get() * 2, height + this.border.get() * 2);
    }

    private void calculateSize(HudRenderer renderer) {
        double width = 0.0;
        if (this.section != null) {
            String str = this.section.toString();
            if (!str.isBlank()) {
                width = renderer.textWidth(str, this.shadow.get(), this.getScale());
            }
        }

        if (width != 0.0) {
            this.setSize(width, renderer.textHeight(this.shadow.get(), this.getScale()));
            this.empty = false;
        } else {
            this.setSize(100.0, renderer.textHeight(this.shadow.get(), this.getScale()));
            this.empty = true;
        }
    }

    public void tick(HudRenderer renderer) {
        if (this.recalculateSize) {
            this.calculateSize(renderer);
            this.recalculateSize = false;
        }

        if (this.timer <= 0) {
            this.runTick(renderer);
            this.timer = this.updateDelay.get();
        } else {
            this.timer--;
        }
    }

    private void runTick(HudRenderer renderer) {
        if (this.needsCompile) {
            Result result = Parser.parse((String) this.text.get());
            if (result.hasErrors()) {
                this.script = null;
                this.section = new Section(0, ((Error) result.errors.getFirst()).toString());
                this.calculateSize(renderer);
            } else {
                this.script = Compiler.compile(result);
            }

            if (this.shown.get() != Shown.Always) {
                this.conditionScript = Compiler.compile(Parser.parse((String) this.condition.get()));
            }

            this.needsCompile = false;
        }

        try {
            if (this.script != null) {
                this.section = MeteorStarscript.ss.run(this.script);
                this.calculateSize(renderer);
            }
        } catch (StarscriptError var3) {
            this.section = new Section(0, var3.getMessage());
            this.calculateSize(renderer);
        }

        if (this.shown.get() != Shown.Always && this.conditionScript != null) {
            String text = MeteorStarscript.run(this.conditionScript);
            if (text == null) {
                this.visible = false;
            } else {
                this.visible = this.shown.get() == Shown.WhenTrue ? text.equalsIgnoreCase("true") : text.equalsIgnoreCase("false");
            }
        }

        this.firstTick = false;
    }

    public void render(HudRenderer renderer) {
        if (this.firstTick) {
            this.runTick(renderer);
        }

        boolean visible = this.shown.get() == Shown.Always || this.visible;
        if ((this.empty || !visible) && this.isInEditor()) {
            renderer.line(this.x, this.y, this.x + this.getWidth(), this.y + this.getHeight(), Color.GRAY);
            renderer.line(this.x, this.y + this.getHeight(), this.x + this.getWidth(), this.y, Color.GRAY);
        }

        if (this.section != null && visible) {
            double x = this.x + this.border.get();

            for (Section s = this.section; s != null; s = s.next) {
                x = renderer.text(s.text, x, this.y + this.border.get(), getSectionColor(s.index), this.shadow.get(), this.getScale());
            }

            if (this.background.get()) {
                Render2DEngine.drawRoundedBlur(
                    renderer.drawContext.getMatrices(),
                    this.x,
                    this.y,
                    this.getWidth(),
                    this.getHeight(),
                    0.0F,
                    new java.awt.Color(((SettingColor) this.backgroundColor.get()).getPacked())
                );
            }
        }
    }

    public void onFontChanged() {
        this.recalculateSize = true;
    }

    private double getScale() {
        return this.customScale.get() ? this.scale.get() : -1.0;
    }

    public static enum Shown {
        Always,
        WhenTrue,
        WhenFalse;

        @Override
        public String toString() {
            return switch (this) {
                case Always -> "Always";
                case WhenTrue -> "When True";
                case WhenFalse -> "When False";
            };
        }
    }
}
