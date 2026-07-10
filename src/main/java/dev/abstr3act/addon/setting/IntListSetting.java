package dev.abstr3act.addon.setting;

import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.themes.meteor.MeteorGuiTheme;
import meteordevelopment.meteorclient.gui.widgets.containers.WTable;
import meteordevelopment.meteorclient.gui.widgets.input.WIntEdit;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import meteordevelopment.meteorclient.gui.widgets.pressable.WMinus;
import meteordevelopment.meteorclient.settings.IVisible;
import meteordevelopment.meteorclient.settings.Setting;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtInt;
import net.minecraft.nbt.NbtList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class IntListSetting extends Setting<List<Integer>> {
    public final boolean onSliderRelease;
    public final boolean noSlider;
    public int min;
    public int max;
    public int sliderMin;
    public int sliderMax;

    public IntListSetting(
        String name,
        String description,
        List<Integer> defaultValue,
        Consumer<List<Integer>> onChanged,
        Consumer<Setting<List<Integer>>> onModuleActivated,
        IVisible visible,
        int min,
        int max,
        int sliderMin,
        int sliderMax,
        boolean onSliderRelease,
        boolean noSlider
    ) {
        super(name, description, defaultValue, onChanged, onModuleActivated, visible);
        this.min = min;
        this.max = max;
        this.sliderMin = sliderMin;
        this.sliderMax = sliderMax;
        this.onSliderRelease = onSliderRelease;
        this.noSlider = noSlider;
    }

    public static void fillTable(GuiTheme theme, WTable table, IntListSetting setting) {
        table.clear();
        ArrayList<Integer> values = new ArrayList<>((Collection<? extends Integer>) setting.get());

        for (int i = 0; i < (setting.get()).size(); i++) {
            int index = i;
            WIntEdit edit = theme.intEdit(values.get(index), setting.min, setting.max, setting.sliderMin, setting.sliderMax, setting.noSlider);
            table.add(edit).expandX();
            Runnable action = () -> {
                try {
                    int newValue = edit.get();
                    values.set(index, newValue);
                    setting.set(values);
                } catch (NumberFormatException var5x) {
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

    public static void intListW(WTable table, IntListSetting setting) {
        GuiTheme theme = new MeteorGuiTheme();
        WTable wtable = (WTable) table.add(theme.table()).expandX().widget();
        fillTable(theme, wtable, setting);
    }

    protected List<Integer> parseImpl(String str) {
        try {
            return Arrays.stream(str.split(",")).map(String::trim).map(Integer::parseInt).collect(Collectors.toList());
        } catch (NumberFormatException var3) {
            return null;
        }
    }

    protected boolean isValueValid(List<Integer> value) {
        return value.stream().allMatch(v -> v >= this.min && v <= this.max);
    }

    public NbtCompound save(NbtCompound tag) {
        NbtList valueTag = new NbtList();

        for (Integer val : this.get()) {
            valueTag.add(NbtInt.of(val));
        }

        tag.put("value", valueTag);
        return tag;
    }

    public List<Integer> load(NbtCompound tag) {
        (this.get()).clear();

        for (NbtElement element : tag.getList("value", 6)) {
            (this.get()).add(((NbtInt) element).intValue());
        }

        return (List<Integer>) this.get();
    }

    public void resetImpl() {
        this.value = new ArrayList((Collection) this.defaultValue);
    }

    public static class Builder extends SettingBuilder<Builder, List<Integer>, IntListSetting> {
        private int min = Integer.MIN_VALUE;
        private int max = Integer.MAX_VALUE;
        private int sliderMin = 0;
        private int sliderMax = 10;
        private boolean onSliderRelease = false;
        private int decimalPlaces = 0;
        private boolean noSlider = false;

        public Builder() {
            super(new ArrayList(0));
        }

        public Builder defaultValue(Integer... defaults) {
            this.defaultValue = defaults != null ? Arrays.asList(defaults) : new ArrayList();
            return this;
        }

        public Builder min(int min) {
            this.min = min;
            return this;
        }

        public Builder max(int max) {
            this.max = max;
            return this;
        }

        public Builder range(int min, int max) {
            this.min = Math.min(min, max);
            this.max = Math.max(min, max);
            return this;
        }

        public Builder sliderMin(int min) {
            this.sliderMin = min;
            return this;
        }

        public Builder sliderMax(int max) {
            this.sliderMax = max;
            return this;
        }

        public Builder sliderRange(int min, int max) {
            this.sliderMin = min;
            this.sliderMax = max;
            return this;
        }

        public Builder onSliderRelease() {
            this.onSliderRelease = true;
            return this;
        }

        public Builder noSlider() {
            this.noSlider = true;
            return this;
        }

        public IntListSetting build() {
            return new IntListSetting(
                this.name,
                this.description,
                (List<Integer>) this.defaultValue,
                this.onChanged,
                this.onModuleActivated,
                this.visible,
                this.min,
                this.max,
                Math.max(this.sliderMin, this.min),
                Math.min(this.sliderMax, this.max),
                this.onSliderRelease,
                this.noSlider
            );
        }
    }
}
