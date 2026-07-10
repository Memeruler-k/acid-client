package dev.abstr3act.addon.modules.Amrita.noslow;

import dev.abstr3act.addon.Compassion;
import dev.abstr3act.addon.events.PlayerUseMultiplierEvent;
import dev.abstr3act.addon.module.AmritaModule;
import dev.abstr3act.addon.modules.Amrita.noslow.modes.*;
import meteordevelopment.meteorclient.events.world.TickEvent.Pre;
import meteordevelopment.meteorclient.settings.EnumSetting.Builder;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.orbit.EventHandler;

public class NoSlowPlus extends AmritaModule {
    public SettingGroup defaultGroup = this.settings.getDefaultGroup();
    public SettingGroup usingItemGroup = this.settings.createGroup("Using item");
    public SettingGroup sneakGroup = this.settings.createGroup("Sneak");
    public SettingGroup otherGroup = this.settings.createGroup("Other");
    private NoSlowMode currentMode;
    public final Setting<NoSlowModes> mode = this.defaultGroup
        .add(
            new Builder<NoSlowModes>().name("mode").description("The method of applying no slow.")
                .defaultValue(NoSlowModes.Vanilla)
                .onModuleActivated(spiderModesSetting -> this.onModeChanged((NoSlowModes) spiderModesSetting.get()))
                .onChanged(this::onModeChanged)
                .build()
        );
    public final Setting<Double> usingForward = this.usingItemGroup
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("forward-multiplier"))
                .defaultValue(1.0)
                .min(0.2)
                .sliderRange(0.2, 1.0)
                .visible(() -> this.mode.get() != NoSlowModes.Matrix))
                .build()
        );
    public final Setting<Double> usingSideways = this.usingItemGroup
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("sideways-multiplier"))
                .defaultValue(1.0)
                .min(0.2)
                .sliderRange(0.2, 1.0)
                .visible(() -> this.mode.get() != NoSlowModes.Matrix))
                .build()
        );
    public final Setting<Double> sneakForward = this.sneakGroup
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("forward-multiplier"))
                .defaultValue(1.0)
                .min(0.2)
                .sliderRange(0.2, 1.0)
                .visible(() -> this.mode.get() != NoSlowModes.Matrix))
                .build()
        );
    public final Setting<Double> sneakSideways = this.sneakGroup
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("sideways-multiplier"))
                .defaultValue(1.0)
                .min(0.2)
                .sliderRange(0.2, 1.0)
                .visible(() -> this.mode.get() != NoSlowModes.Matrix))
                .build()
        );
    public final Setting<Double> otherForward = this.otherGroup
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("forward-multiplier"))
                .defaultValue(1.0)
                .min(0.2)
                .sliderRange(0.2, 1.0)
                .visible(() -> this.mode.get() != NoSlowModes.Matrix))
                .build()
        );
    public final Setting<Double> otherSideways = this.otherGroup
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("sideways-multiplier"))
                .defaultValue(1.0)
                .min(0.2)
                .sliderRange(0.2, 1.0)
                .visible(() -> this.mode.get() != NoSlowModes.Matrix))
                .build()
        );

    public NoSlowPlus() {
        super(Compassion.AMRITA, "NoSlowV3", "Remove or increase slowness.");
    }

    private void onModeChanged(NoSlowModes mode) {
        switch (mode) {
            case Vanilla:
                this.currentMode = new Vanilla();
                break;
            case NCP_Strict:
                this.currentMode = new NCPStrict();
                break;
            case Grim_1dot8:
                this.currentMode = new Grim();
                break;
            case Grim_New:
                this.currentMode = new GrimNew();
                break;
            case Matrix:
                this.currentMode = new Matrix();
        }
    }

    @EventHandler
    private void onUse(PlayerUseMultiplierEvent event) {
        this.currentMode.onUse(event);
    }

    @EventHandler
    private void onTickEventPre(Pre event) {
        this.currentMode.onTickEventPre(event);
    }

    public void onActivate() {
        this.currentMode.onActivate();
    }

    public String getInfoString() {
        return ((NoSlowModes) this.mode.get()).toString();
    }
}
