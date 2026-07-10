package dev.abstr3act.addon.modules.Amrita;

import dev.abstr3act.addon.Compassion;
import dev.abstr3act.addon.events.EventUpdate;
import dev.abstr3act.addon.module.AmritaModule;
import dev.abstr3act.addon.utils.RotationUtil;
import meteordevelopment.meteorclient.settings.DoubleSetting.Builder;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.orbit.EventHandler;

public class Derp extends AmritaModule {
    private static float spinYaw = 0.0F;
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
    private final Setting<Double> spinSpeed = this.sgGeneral
        .add(((Builder) ((Builder) new Builder().name("SpinSpeed")).description(".")).defaultValue(30.0).sliderRange(-40.0, 40.0).build());
    private final Setting<Double> pitch = this.sgGeneral
        .add(((Builder) ((Builder) new Builder().name("Pitch")).description(".")).defaultValue(30.0).sliderRange(-40.0, 40.0).build());

    public Derp() {
        super(Compassion.AMRITA, "Derp", "SpinBot");
    }

    public void onDeactivate() {
        if (this.mc.player != null) {
            spinYaw = 0.0F;
            RotationUtil.reset();
        }
    }

    @EventHandler
    public void onUpdate(EventUpdate event) {
        if (this.mc.player != null) {
            if (spinYaw >= 360.0F || spinYaw <= -360.0F) {
                spinYaw = 0.0F;
            }

            spinYaw = (float) (spinYaw + this.spinSpeed.get());
            RotationUtil.setRotation(spinYaw, (this.pitch.get()).floatValue(), 180.0F, true);
        }
    }
}
