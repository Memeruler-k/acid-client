package dev.abstr3act.addon.modules.Seraphim;

import dev.abstr3act.addon.Compassion;
import meteordevelopment.meteorclient.events.world.TickEvent.Pre;
import meteordevelopment.meteorclient.settings.IntSetting.Builder;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;

public class AutoJumpReset extends Module {
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
    private final Setting<Integer> damageTicks = this.sgGeneral
        .add(((Builder) ((Builder) ((Builder) new Builder().name("damageTicks")).description(".")).defaultValue(20)).min(0).sliderRange(1, 100).build());
    private final Setting<JumpMode> jumpMode = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) new meteordevelopment.meteorclient.settings.EnumSetting.Builder()
                .name("JumpMode"))
                .description("."))
                .defaultValue(JumpMode.Jump))
                .build()
        );
    private final Setting<Boolean> fail = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("SmartFail"))
                .description("."))
                .defaultValue(true))
                .build()
        );
    private final Setting<Double> failRate = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("FailRate"))
                .description("."))
                .defaultValue(0.3F)
                .sliderRange(0.0, 1.0)
                .build()
        );
    private final Setting<Double> jumpRate = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("FailJumpRate"))
                .description("."))
                .defaultValue(0.25)
                .sliderRange(0.0, 1.0)
                .build()
        );
    private final Setting<Double> motion = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("Motion"))
                .description("."))
                .defaultValue(0.42F)
                .sliderRange(0.4F, 0.5)
                .build()
        );
    int i = 0;
    int j = 0;
    private boolean doJump;
    private boolean failJump;
    private boolean skip;
    private boolean flag;
    private int level;

    public AutoJumpReset() {
        super(Compassion.SERAPHIM, "JumpReset", "LegitVelocity");
    }

    public void onActivate() {
        this.level = this.mc.player.getBlockPos().getY();
    }

    @EventHandler
    private void onTick(Pre event) {
        if ((this.failJump || this.mc.player.hurtTime > this.damageTicks.get()) && this.mc.player.isOnGround()) {
            if (this.failJump) {
                this.failJump = false;
            }

            if (!this.doJump) {
                this.skip = true;
            }

            if (Math.random() <= this.failRate.get() && this.fail.get()) {
                if (Math.random() <= this.jumpRate.get()) {
                    this.doJump = true;
                    this.failJump = true;
                } else {
                    this.doJump = false;
                    this.failJump = false;
                }
            } else {
                this.doJump = true;
                this.failJump = false;
            }

            if (this.skip) {
                this.skip = false;
                return;
            }

            switch ((JumpMode) this.jumpMode.get()) {
                case Jump:
                    this.mc.player.jump();
                    break;
                case Motion:
                    this.mc.player.setVelocity(this.mc.player.getVelocity().getX(), this.motion.get(), this.mc.player.getVelocity().getZ());
                    break;
                case Both:
                    this.mc.player.jump();
                    this.mc.player.setVelocity(this.mc.player.getVelocity().getX(), this.motion.get(), this.mc.player.getVelocity().getZ());
            }
        }
    }

    public String getInfoString() {
        return ((JumpMode) this.jumpMode.get()).name();
    }

    static enum JumpMode {
        Jump,
        Motion,
        Both;
    }
}
