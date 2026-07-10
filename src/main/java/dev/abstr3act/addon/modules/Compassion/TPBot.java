package dev.abstr3act.addon.modules.Compassion;

import dev.abstr3act.addon.Compassion;
import dev.abstr3act.addon.events.EventPlayerMove;
import dev.abstr3act.addon.events.EventRender3D;
import dev.abstr3act.addon.mixin.accessor.IEntity;
import dev.abstr3act.addon.module.CompassionModule;
import dev.abstr3act.addon.modules.Fragment.AntiBot;
import dev.abstr3act.addon.utils.PredictUtility;
import dev.abstr3act.addon.utils.TargetUtils;
import dev.abstr3act.addon.utils.abnormally.BlockUtil;
import dev.abstr3act.addon.utils.compassion.PathUtils;
import dev.abstr3act.addon.utils.compassion.Vec3;
import dev.abstr3act.addon.utils.compassion.impl.PlayerUtil;
import meteordevelopment.meteorclient.events.world.TickEvent.Post;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.EnumSetting.Builder;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.world.Timer;
import meteordevelopment.meteorclient.utils.entity.SortPriority;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket.PositionAndOnGround;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;

public class TPBot extends CompassionModule {
    public Vec3 path;
    private SettingGroup sgGeneral = this.settings.getDefaultGroup();
    private final Setting<Mode> mode = this.sgGeneral
        .add(((Builder) ((Builder) ((Builder) new Builder().name("Mode")).description(".")).defaultValue(Mode.RandomXZ)).build());
    public final Setting<Double> randomSpeed = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("RandomXZSpeed"))
                .description("."))
                .defaultValue(1.0)
                .min(0.1)
                .sliderRange(0.1, 15.0)
                .visible(() -> ((Mode) this.mode.get()).equals(Mode.RandomXZ)))
                .build()
        );
    public final Setting<Double> precisionSpeed = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("PrecisionXZSpeed"))
                .description("."))
                .defaultValue(1.0)
                .min(0.1)
                .sliderRange(0.1, 15.0)
                .visible(() -> ((Mode) this.mode.get()).equals(Mode.Precision)))
                .build()
        );
    public final Setting<Double> closeDistanceThreshold = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("CloseDistanceThreshold"))
                .description("."))
                .defaultValue(0.2)
                .min(0.1)
                .sliderRange(0.1, 2.0)
                .visible(() -> ((Mode) this.mode.get()).equals(Mode.Precision)))
                .build()
        );
    public final Setting<Double> minDistance = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("MinDistance"))
                .description("."))
                .defaultValue(2.0)
                .sliderRange(1.0, 5.0)
                .visible(() -> ((Mode) this.mode.get()).equals(Mode.RandomXZ)))
                .build()
        );
    public final Setting<Double> maxDistance = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("MaxDistance"))
                .description("."))
                .defaultValue(4.0)
                .sliderRange(1.0, 5.0)
                .visible(() -> ((Mode) this.mode.get()).equals(Mode.RandomXZ)))
                .build()
        );
    public final Setting<SortPriority> sortPriority = this.sgGeneral
        .add(((Builder) ((Builder) ((Builder) new Builder().name("Priority")).description(".")).defaultValue(SortPriority.LowestDistance)).build());
    public final Setting<Double> targetRange = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("TargetRange"))
                .description("."))
                .defaultValue(100.0)
                .min(0.1)
                .sliderRange(0.1, 500.0)
                .build()
        );
    public final Setting<Double> ySpeed = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("YSpeed"))
                .description("."))
                .defaultValue(2.0)
                .min(0.1)
                .sliderRange(0.1, 30.0)
                .build()
        );
    public final Setting<Double> xOffset = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("XOffset"))
                .description("."))
                .defaultValue(0.0)
                .sliderRange(-5.0, 5.0)
                .build()
        );
    public final Setting<Double> yOffset = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("YOffset"))
                .description("."))
                .defaultValue(2.0)
                .sliderRange(-5.0, 5.0)
                .build()
        );
    public final Setting<Double> zOffset = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("ZOffset"))
                .description("."))
                .defaultValue(0.0)
                .sliderRange(-5.0, 5.0)
                .build()
        );
    public final Setting<Boolean> predict = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("Predict"))
                .description("."))
                .defaultValue(false))
                .build()
        );
    public final Setting<Integer> predictTicks = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("PredictTicks"))
                .description("."))
                .defaultValue(4))
                .min(1)
                .sliderRange(1, 20)
                .visible(this.predict::get))
                .build()
        );
    public final Setting<Boolean> wallMode = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("WallMode"))
                .description("."))
                .defaultValue(false))
                .build()
        );
    public final Setting<Boolean> pathMode = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("PathMode"))
                .description("."))
                .defaultValue(false))
                .build()
        );
    public final Setting<Boolean> useTimer = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("UseTimer"))
                .description("."))
                .defaultValue(false))
                .build()
        );
    public final Setting<Double> timerValue = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("TimerValue"))
                .description("."))
                .defaultValue(1.0)
                .sliderRange(1.0, 20.0)
                .visible(this.useTimer::get))
                .build()
        );
    public final Setting<Double> maxMoveDistance = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("MaxMoveDistance"))
                .description("."))
                .defaultValue(10.0)
                .sliderRange(0.0, 20.0)
                .visible(this.useTimer::get))
                .build()
        );
    public final Setting<Boolean> pauseWhileUsing = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("PauseWhileUsing"))
                .description("."))
                .defaultValue(false))
                .build()
        );
    public final Setting<Double> tp = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("TP"))
                .description("."))
                .defaultValue(50.0)
                .min(0.0)
                .sliderRange(0.0, 300.0)
                .build()
        );
    public final Setting<Integer> loops = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("Loops"))
                .description("."))
                .defaultValue(1000))
                .min(1)
                .sliderRange(1, 2000)
                .build()
        );
    public final Setting<ShapeMode> shapeMode = this.sgGeneral
        .add(((Builder) ((Builder) ((Builder) new Builder().name("shape-mode")).description("How the shapes are rendered.")).defaultValue(ShapeMode.Both)).build());
    private final Setting<SettingColor> sideColor = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                .name("sideColor"))
                .description("."))
                .defaultValue(new SettingColor(255, 255, 255, 150))
                .build()
        );
    private final Setting<SettingColor> lineColor = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                .name("lineColor"))
                .description("."))
                .defaultValue(new SettingColor(255, 255, 255, 255))
                .build()
        );
    private final Setting<Boolean> ignoreBots = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("IgnoreBots"))
                .description("IgnoreBots."))
                .defaultValue(false))
                .build()
        );
    private final Setting<Boolean> ignoreFriends = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("IgnoreFriends"))
                .description("."))
                .defaultValue(false))
                .build()
        );
    private PlayerEntity target;
    private ArrayList<Vec3> lastPath;

    public TPBot() {
        super(Compassion.COMPASSION, "TPBot", "Auto trace target");
    }

    public void onDeactivate() {
        if (this.useTimer.get()) {
            Timer timer = new Timer();
            timer.setOverride(1.0);
        }
    }

    @EventHandler
    public void onSync(Post e) {
        if (this.mc.player != null && this.mc.world != null) {
            if (this.minDistance.get() > this.maxDistance.get()) {
                this.minDistance.set(this.maxDistance.get());
            }

            this.target = TargetUtils.getPlayerTarget(this.targetRange.get(), (SortPriority) this.sortPriority.get());
        }
    }

    @EventHandler
    public void onRender(EventRender3D event) {
        if (this.target == null) {
            if (this.useTimer.get()) {
                Timer timer = new Timer();
                timer.setOverride(1.0);
            }
        } else {
            if (this.useTimer.get()) {
                Timer timer = new Timer();
                timer.setOverride(this.timerValue.get());
            } else if (!this.useTimer.get()) {
                Timer timer = new Timer();
                timer.setOverride(1.0);
            }

            if (this.lastPath != null && this.pathMode.get()) {
                for (Vec3 vec3 : this.lastPath) {
                    event.renderer
                        .box(
                            ((IEntity) this.mc.player).getDimensions().getBoxAt(vec3.mc()),
                            (Color) this.sideColor.get(),
                            (Color) this.lineColor.get(),
                            (ShapeMode) this.shapeMode.get(),
                            0
                        );
                }
            }
        }
    }

    @EventHandler
    public void doTpMove(EventPlayerMove event) {
        if (this.target != null) {
            if (!(this.target instanceof PlayerEntity) || !AntiBot.INSTANCE.inBotList(this.target) || !this.ignoreBots.get()) {
                PlayerEntity predict;
                if (this.predict.get()) {
                    predict = PredictUtility.predictPlayer(this.target, this.predictTicks.get());
                } else {
                    predict = this.target;
                }

                if (this.mc.player != null && this.mc.world != null) {
                    if (!this.mc.player.isUsingItem() || !this.pauseWhileUsing.get()) {
                        if (predict != null) {
                            if (((Mode) this.mode.get()).equals(Mode.Precision)) {
                                if (this.pathMode.get()) {
                                    ArrayList<Vec3> t = PathUtils.computePath(predict.getPos(), this.loops.get(), this.tp.get());
                                    this.lastPath = t;

                                    for (Vec3 v : t) {
                                        boolean isBlocked = this.wallMode.get()
                                            && !BlockUtil.checkCanMove(
                                            this.mc.player.getPos(), new Vec3d(v.getX(), v.getY(), v.getZ()), BlockUtil.getBufferMap(), PlayerUtil.PlayerState.Normal
                                        );
                                        if (isBlocked) {
                                            this.teleport(v.getX(), v.getY(), v.getZ());
                                        } else {
                                            double pathX = v.getX() + this.xOffset.get();
                                            double pathY = v.getY() + this.yOffset.get();
                                            double pathZ = v.getZ() + this.zOffset.get();
                                            double deltaX = predict.getX() + this.xOffset.get() - this.mc.player.getX();
                                            double deltaZ = predict.getZ() + this.zOffset.get() - this.mc.player.getZ();
                                            double distance = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
                                            double targetVelX = this.precisionSpeed.get() * -Math.sin(Math.toRadians(this.wrapDS(pathX, pathZ)));
                                            double targetVelZ = this.precisionSpeed.get() * Math.cos(Math.toRadians(this.wrapDS(pathX, pathZ)));
                                            double heightDifference = pathY - this.mc.player.getY();
                                            double targetVelY = 0.0;
                                            if (Math.abs(heightDifference) > 0.1) {
                                                targetVelY = Math.min(this.ySpeed.get(), Math.abs(heightDifference));
                                                targetVelY *= Math.signum(heightDifference);
                                            }

                                            double speedMultiplier = distance < this.closeDistanceThreshold.get() ? distance / this.closeDistanceThreshold.get() : 1.0;
                                            if (Math.abs(heightDifference) > 0.1) {
                                                targetVelY = Math.min(this.ySpeed.get(), Math.abs(heightDifference));
                                                targetVelY *= Math.signum(heightDifference);
                                            }

                                            event.setX(targetVelX * speedMultiplier);
                                            event.setZ(targetVelZ * speedMultiplier);
                                            event.setY(targetVelY);
                                            event.cancel();
                                        }
                                    }
                                } else {
                                    Vec3d vx = predict.getPos();
                                    double targetX = vx.getX() + this.xOffset.get();
                                    double targetY = vx.getY() + this.yOffset.get();
                                    double targetZ = vx.getZ() + this.zOffset.get();
                                    double deltaXx = targetX - this.mc.player.getX();
                                    double deltaZx = targetZ - this.mc.player.getZ();
                                    double distancex = Math.sqrt(deltaXx * deltaXx + deltaZx * deltaZx);
                                    double targetVelXx = this.precisionSpeed.get() * -Math.sin(Math.toRadians(this.wrapDS(targetX, targetZ)));
                                    double targetVelZx = this.precisionSpeed.get() * Math.cos(Math.toRadians(this.wrapDS(targetX, targetZ)));
                                    double heightDifferencex = targetY - this.mc.player.getY();
                                    double targetVelYx = 0.0;
                                    double speedMultiplier = distancex < this.closeDistanceThreshold.get() ? distancex / this.closeDistanceThreshold.get() : 1.0;
                                    if (Math.abs(heightDifferencex) > 0.1) {
                                        targetVelYx = Math.min(this.ySpeed.get(), Math.abs(heightDifferencex));
                                        targetVelYx *= Math.signum(heightDifferencex);
                                    }

                                    event.setX(targetVelXx * speedMultiplier);
                                    event.setZ(targetVelZx * speedMultiplier);
                                    event.setY(targetVelYx);
                                    event.cancel();
                                }
                            } else if (((Mode) this.mode.get()).equals(Mode.RandomXZ)) {
                                double wrap = Math.atan2(this.mc.player.getZ() - predict.getZ(), this.mc.player.getX() - predict.getX());
                                wrap += this.randomSpeed.get() / Math.sqrt(this.mc.player.squaredDistanceTo(predict));
                                double targetX = predict.getX() + this.randomOffset() * Math.cos(wrap);
                                double targetY = predict.getY();
                                double targetZ = predict.getZ() + this.randomOffset() * Math.cos(wrap);
                                double heightDifferencex = targetY - this.mc.player.getY();
                                double targetVelYx = 0.0;
                                if (Math.abs(heightDifferencex) > 0.1) {
                                    targetVelYx = Math.min(this.ySpeed.get(), Math.abs(heightDifferencex));
                                    targetVelYx *= Math.signum(heightDifferencex);
                                }

                                event.setX(this.randomSpeed.get() * -Math.sin(Math.toRadians(this.wrapDS(targetX, targetZ))));
                                event.setY(targetVelYx);
                                event.setZ(this.randomSpeed.get() * Math.cos(Math.toRadians(this.wrapDS(targetX, targetZ))));
                                event.cancel();
                            }
                        }
                    }
                }
            }
        }
    }

    private void teleport(double targetX, double targetY, double targetZ) {
        if (this.mc.player.squaredDistanceTo(targetX, targetY, targetZ) <= this.maxMoveDistance.get() * this.maxMoveDistance.get()) {
            this.mc.player.networkHandler.sendPacket(new PositionAndOnGround(targetX, targetY, targetZ, false));
            this.mc.player.setPosition(targetX, targetY, targetZ);
        }
    }

    private double randomOffset() {
        return Math.random() * (this.maxDistance.get() - this.minDistance.get()) + this.minDistance.get();
    }

    private double wrapDS(double x, double z) {
        double diffX = x - this.mc.player.getX();
        double diffZ = z - this.mc.player.getZ();
        return Math.toDegrees(Math.atan2(diffZ, diffX)) - 90.0;
    }

    public static enum Mode {
        RandomXZ,
        Precision;
    }
}
