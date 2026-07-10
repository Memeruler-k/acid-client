package dev.abstr3act.addon.setting;

import meteordevelopment.meteorclient.settings.IVisible;
import meteordevelopment.meteorclient.settings.Setting;
import net.minecraft.nbt.NbtCompound;

import java.util.function.Consumer;

public class FloatSetting extends Setting<Float> {
    public final Float min;
    public final Float max;
    public final Float sliderMin;
    public final Float sliderMax;
    public final boolean onSliderRelease;
    public final int decimalPlaces;
    public final boolean noSlider;

    private FloatSetting(
        String name,
        String description,
        Float defaultValue,
        Consumer<Float> onChanged,
        Consumer<Setting<Float>> onModuleActivated,
        IVisible visible,
        Float min,
        Float max,
        Float sliderMin,
        Float sliderMax,
        boolean onSliderRelease,
        int decimalPlaces,
        boolean noSlider
    ) {
        super(name, description, defaultValue, onChanged, onModuleActivated, visible);
        this.min = min;
        this.max = max;
        this.sliderMin = sliderMin;
        this.sliderMax = sliderMax;
        this.decimalPlaces = decimalPlaces;
        this.onSliderRelease = onSliderRelease;
        this.noSlider = noSlider;
    }

    protected Float parseImpl(String str) {
        try {
            return Float.parseFloat(str.trim());
        } catch (NumberFormatException var3) {
            return null;
        }
    }

    protected boolean isValueValid(Float value) {
        return value >= this.min && value <= this.max;
    }

    protected NbtCompound save(NbtCompound tag) {
        tag.putFloat("value", (Float) this.get());
        return tag;
    }

    public Float load(NbtCompound tag) {
        this.set(tag.getFloat("value"));
        return (Float) this.get();
    }

    public static class Builder extends SettingBuilder<Builder, Float, FloatSetting> {
        public Float min = Float.NEGATIVE_INFINITY;
        public Float max = Float.POSITIVE_INFINITY;
        public Float sliderMin = 0.0F;
        public Float sliderMax = 10.0F;
        public boolean onSliderRelease = false;
        public int decimalPlaces = 3;
        public boolean noSlider = false;

        public Builder() {
            super(0.0F);
        }

        public Builder defaultValue(Float defaultValue) {
            this.defaultValue = defaultValue;
            return this;
        }

        public Builder min(Float min) {
            this.min = min;
            return this;
        }

        public Builder max(Float max) {
            this.max = max;
            return this;
        }

        public Builder range(Float min, Float max) {
            this.min = Math.min(min, max);
            this.max = Math.max(min, max);
            return this;
        }

        public Builder sliderMin(Float min) {
            this.sliderMin = min;
            return this;
        }

        public Builder sliderMax(Float max) {
            this.sliderMax = max;
            return this;
        }

        public Builder sliderRange(Float min, Float max) {
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

        public FloatSetting build() {
            return new FloatSetting(
                this.name,
                this.description,
                (Float) this.defaultValue,
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
