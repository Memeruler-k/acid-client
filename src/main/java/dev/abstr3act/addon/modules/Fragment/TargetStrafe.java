package dev.abstr3act.addon.modules.Fragment;

import dev.abstr3act.addon.Compassion;
import dev.abstr3act.addon.events.legacy.MotionEvent;
import dev.abstr3act.addon.events.legacy.WorldEvent;
import dev.abstr3act.addon.module.LacrymiraModule;
import meteordevelopment.meteorclient.settings.DoubleSetting.Builder;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.combat.KillAura;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.util.InputUtil;

public class TargetStrafe extends LacrymiraModule {
    public static int direction = 1;
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
    public final Setting<Double> getDistance = this.sgGeneral
        .add(((Builder) ((Builder) new Builder().name("Distance")).description(".")).defaultValue(1.5).sliderRange(0.0, 8.0).build());

    public TargetStrafe() {
        super(Compassion.LACRYMIRA, "TargetStrafe2", ".");
    }

    public boolean targetStrafeHook() {
        return this.shouldDoStrafe();
    }

    private boolean shouldDoStrafe() {
        if (this.mc.player != null && ((KillAura) Modules.get().get(KillAura.class)).getTarget() != null) {
            return InputUtil.isKeyPressed(
                this.mc.getWindow().getHandle(), InputUtil.fromTranslationKey(this.mc.options.jumpKey.getBoundKeyTranslationKey()).getCode()
            )
                && !InputUtil.isKeyPressed(this.mc.getWindow().getHandle(), InputUtil.fromTranslationKey(this.mc.options.leftKey.getBoundKeyTranslationKey()).getCode())
                && !InputUtil.isKeyPressed(
                this.mc.getWindow().getHandle(), InputUtil.fromTranslationKey(this.mc.options.rightKey.getBoundKeyTranslationKey()).getCode()
            )
                && !InputUtil.isKeyPressed(this.mc.getWindow().getHandle(), InputUtil.fromTranslationKey(this.mc.options.backKey.getBoundKeyTranslationKey()).getCode());
        } else {
            direction = 1;
            return false;
        }
    }

    public void onActivate() {
        direction = 1;
    }

    public void onDeactivate() {
    }

    @EventHandler
    public void onWorld(WorldEvent event) {
        direction = 1;
    }

    @EventHandler
    public void onMotion(MotionEvent event) {
        if (this.shouldDoStrafe()) {
            if (this.mc.player.horizontalCollision) {
                direction = -direction;
            }
        }
    }
}
