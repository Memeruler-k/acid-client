package dev.abstr3act.addon.setting;

import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.themes.meteor.MeteorGuiTheme;
import meteordevelopment.meteorclient.gui.widgets.containers.WTable;
import meteordevelopment.meteorclient.gui.widgets.input.WDoubleEdit;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import meteordevelopment.meteorclient.gui.widgets.pressable.WMinus;
import meteordevelopment.meteorclient.settings.IVisible;
import meteordevelopment.meteorclient.settings.Setting;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtDouble;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class DoubleListSetting extends Setting<List<Double>> {
    public final double min;
    public final double max;
    public final double sliderMin;
    public final double sliderMax;
    public final boolean onSliderRelease;
    public final int decimalPlaces;
    public final boolean noSlider;

    public DoubleListSetting(
        String name,
        String description,
        List<Double> defaultValue,
        Consumer<List<Double>> onChanged,
        Consumer<Setting<List<Double>>> onModuleActivated,
        IVisible visible,
        double min,
        double max,
        double sliderMin,
        double sliderMax,
        boolean onSliderRelease,
        int decimalPlaces,
        boolean noSlider
    ) {
        super(name, description, defaultValue, onChanged, onModuleActivated, visible);
        this.min = min;
        this.max = max;
        this.sliderMin = sliderMin;
        this.sliderMax = sliderMax;
        this.onSliderRelease = onSliderRelease;
        this.decimalPlaces = decimalPlaces;
        this.noSlider = noSlider;
    }

    public static void fillTable(GuiTheme theme, WTable table, DoubleListSetting setting) {
        table.clear();
        ArrayList<Double> values = new ArrayList<>((Collection<? extends Double>) setting.get());

        for (int i = 0; i < (setting.get()).size(); i++) {
            int index = i;
            WDoubleEdit edit = theme.doubleEdit(
                values.get(index), setting.min, setting.max, setting.sliderMin, setting.sliderMax, setting.decimalPlaces, setting.noSlider
            );
            table.add(edit).expandX();
            Runnable action = () -> {
                try {
                    double newValue = edit.get();
                    values.set(index, newValue);
                    setting.set(values);
                } catch (NumberFormatException var6x) {
                    edit.set(values.get(index));
                }
            };
            if (setting.onSliderRelease) {
                edit.actionOnRelease = action;
            } else {
                edit.action = action;
            }

            WMinus delete = (WMinus) table.add(theme.minus()).widget();
            delete.action = () -> {
                values.remove(index);
                setting.set(values);
                fillTable(theme, table, setting);
            };
            table.row();
        }

        if (!(setting.get()).isEmpty()) {
            table.add(theme.horizontalSeparator()).expandX();
            table.row();
        }

        WButton add = (WButton) table.add(theme.button("Add")).expandX().widget();
        add.action = () -> {
            values.add(setting.sliderMin);
            setting.set(values);
            fillTable(theme, table, setting);
        };
        WButton reset = (WButton) table.add(theme.button(GuiRenderer.RESET)).widget();
        reset.action = () -> {
            setting.reset();
            fillTable(theme, table, setting);
        };
    }

    public static void doubleListW(WTable table, DoubleListSetting setting) {
        GuiTheme theme = new MeteorGuiTheme();
        WTable wtable = (WTable) table.add(theme.table()).expandX().widget();
        fillTable(theme, wtable, setting);
    }

    protected List<Double> parseImpl(String str) {
        try {
            return Arrays.stream(str.split(",")).map(String::trim).map(Double::parseDouble).collect(Collectors.toList());
        } catch (NumberFormatException var3) {
            return null;
        }
    }

    protected boolean isValueValid(List<Double> value) {
        return value.stream().allMatch(v -> v >= this.min && v <= this.max);
    }

    public NbtCompound save(NbtCompound tag) {
        NbtList valueTag = new NbtList();

        for (Double val : this.get()) {
            valueTag.add(NbtDouble.of(val));
        }

        tag.put("value", valueTag);
        return tag;
    }

    public List<Double> load(NbtCompound tag) {
        (this.get()).clear();

        for (NbtElement element : tag.getList("value", 6)) {
            (this.get()).add(((NbtDouble) element).doubleValue());
        }

        return (List<Double>) this.get();
    }

    public void resetImpl() {
        this.value = new ArrayList((Collection) this.defaultValue);
    }

    public static class Builder extends SettingBuilder<Builder, List<Double>, DoubleListSetting> {
        private double min = Double.NEGATIVE_INFINITY;
        private double max = Double.POSITIVE_INFINITY;
        private double sliderMin = 0.0;
        private double sliderMax = 10.0;
        private boolean onSliderRelease = false;
        private int decimalPlaces = 3;
        private boolean noSlider = false;

        public Builder() {
            super(new ArrayList(0));
        }

        public Builder defaultValue(Double... defaults) {
            this.defaultValue = defaults != null ? Arrays.asList(defaults) : new ArrayList();
            return this;
        }

        public Builder min(double min) {
            this.min = min;
            return this;
        }

        public Builder max(double max) {
            this.max = max;
            return this;
        }

        public Builder range(double min, double max) {
            this.min = Math.min(min, max);
            this.max = Math.max(min, max);
            return this;
        }

        public Builder sliderMin(double min) {
            this.sliderMin = min;
            return this;
        }

        public Builder sliderMax(double max) {
            this.sliderMax = max;
            return this;
        }

        public Builder sliderRange(double min, double max) {
            this.sliderMin = min;
            this.sliderMax = max;
            return this;
        }

        public Builder onSliderRelease() {
            this.onSliderRelease = true;
            return this;
        }

        public Builder decimalPlaces(int decimalPlaces) {
            this.decimalPlaces = decimalPlaces;
            return this;
        }

        public Builder noSlider() {
            this.noSlider = true;
            return this;
        }

        public DoubleListSetting build() {
            return new DoubleListSetting(
                this.name,
                this.description,
                (List<Double>) this.defaultValue,
                this.onChanged,
                this.onModuleActivated,
                this.visible,
                this.min,
                this.max,
                Math.max(this.sliderMin, this.min),
                Math.min(this.sliderMax, this.max),
                this.onSliderRelease,
                this.decimalPlaces,
                this.noSlider
            );
        }
    }
}
