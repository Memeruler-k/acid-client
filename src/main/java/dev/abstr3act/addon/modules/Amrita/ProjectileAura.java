package dev.abstr3act.addon.modules.Amrita;

import dev.abstr3act.addon.Compassion;
import dev.abstr3act.addon.events.EventSync;
import dev.abstr3act.addon.module.AmritaModule;
import dev.abstr3act.addon.modules.Fragment.AntiBot;
import dev.abstr3act.addon.utils.PredictUtility;
import dev.abstr3act.addon.utils.RotationUtil;
import dev.abstr3act.addon.utils.TargetUtils;
import dev.abstr3act.addon.utils.math.InventoryUtility;
import dev.abstr3act.addon.utils.math.MathUtility;
import dev.abstr3act.addon.utils.seraphim.movement.MovementUtils;
import meteordevelopment.meteorclient.settings.BoolSetting.Builder;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.utils.entity.SortPriority;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket.Full;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;

public final class ProjectileAura extends AmritaModule {
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
    private final Setting<Boolean> ignoreWalls = this.sgGeneral
        .add(((Builder) ((Builder) ((Builder) new Builder().name("IgnoreWalls")).description("Ignore walls while aiming.")).defaultValue(true)).build());
    private final Setting<Boolean> prediction = this.sgGeneral
        .add(((Builder) ((Builder) ((Builder) new Builder().name("Prediction")).description(".")).defaultValue(true)).build());
    private final Setting<Integer> ticks = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("PredictTicks"))
                .description("."))
                .defaultValue(5))
                .visible(this.prediction::get))
                .min(1)
                .max(20)
                .build()
        );
    private final Setting<Double> factor = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("PredictFactor"))
                .description("Prediction value for aiming."))
                .defaultValue(0.3F)
                .min(0.01F)
                .max(8.0)
                .build()
        );
    private final Setting<Double> aimRange = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("MaxRange"))
                .description("Maximum range for aiming."))
                .defaultValue(20.0)
                .min(1.0)
                .max(30.0)
                .sliderRange(1.0, 30.0)
                .build()
        );
    private final Setting<Double> minRange = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("MinRange"))
                .description("Maximum range for aiming."))
                .defaultValue(3.0)
                .min(1.0)
                .max(30.0)
                .sliderRange(1.0, 30.0)
                .build()
        );
    private final Setting<Double> predict = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("AimPredict"))
                .description("Prediction value for aiming."))
                .defaultValue(0.5)
                .min(0.5)
                .max(8.0)
                .build()
        );
    private final Setting<Integer> delay = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("ShootDelay"))
                .description("Delay before shooting."))
                .defaultValue(5))
                .min(1)
                .max(10)
                .build()
        );
    private final Setting<Integer> fov = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("FOV"))
                .description("Field of view for aiming."))
                .defaultValue(65))
                .min(10)
                .max(360)
                .build()
        );
    private final Setting<Double> rotationSpeed = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("RotationSpeed"))
                .description("."))
                .sliderRange(1.0, 180.0)
                .defaultValue(40.0)
                .build()
        );
    private final Setting<Boolean> randomizedRotation = this.sgGeneral
        .add(((Builder) ((Builder) ((Builder) new Builder().name("Randomized Rotation")).description(".")).defaultValue(true)).build());
    private final Setting<Double> randomTurnSpeed = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("Random Turn Value"))
                .description("."))
                .sliderRange(1.0, 20.0)
                .defaultValue(15.0)
                .visible(this.randomizedRotation::get))
                .build()
        );
    private final Setting<Double> baseOffset = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("BaseOffset"))
                .description("."))
                .sliderRange(0.0, 20.0)
                .defaultValue(0.0)
                .visible(this.randomizedRotation::get))
                .build()
        );
    private final Setting<Double> jitterAmount = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("JitterAmount"))
                .description("."))
                .sliderRange(0.0, 20.0)
                .defaultValue(0.2)
                .visible(this.randomizedRotation::get))
                .build()
        );
    private final Setting<Integer> baseYaw = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("baseYaw"))
                .description(""))
                .defaultValue(180))
                .min(10)
                .max(360)
                .build()
        );
    private final Setting<Integer> baseYaw2 = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("baseYaw2"))
                .description(""))
                .defaultValue(180))
                .min(10)
                .max(360)
                .build()
        );
    private final Setting<Integer> deltaYaw = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("deltaYaw"))
                .description(""))
                .defaultValue(40))
                .min(0)
                .max(360)
                .build()
        );
    private final Setting<Integer> deltaYaw2 = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("deltaYaw2"))
                .description(""))
                .defaultValue(60))
                .min(0)
                .max(360)
                .build()
        );
    private final Setting<Double> rotYawRandom = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("YawRandom"))
                .description("Randomize yaw while aiming."))
                .defaultValue(0.0)
                .min(0.0)
                .max(3.0)
                .build()
        );
    private final Setting<Double> rotPitchRandom = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("PitchRandom"))
                .description("Randomize pitch while aiming."))
                .defaultValue(0.0)
                .min(0.0)
                .max(3.0)
                .build()
        );
    Vec3d targetVec;
    private Entity target;
    private float rotationYaw;
    private float rotationPitch;

    public ProjectileAura() {
        super(Compassion.AMRITA, "ProjectileAura", ".");
    }

    @EventHandler
    public void onSync(EventSync event) {
        this.calcThread();
    }

    public void onDeactivate() {
        this.target = null;
        RotationUtil.reset();
    }

    private void calcThread() {
        if (this.target == null) {
            this.target = TargetUtils.getPlayerTarget(this.aimRange.get(), SortPriority.LowestDistance);
        } else if (this.skipEntity(this.target)) {
            this.target = null;
        } else if (!(this.mc.player.distanceTo(this.target) < this.minRange.get())) {
            this.targetVec = this.getResolvedPos(this.target).add(0.0, 1.3F, 0.0);
            if (this.prediction.get() && MovementUtils.isMoving(this.target)) {
                this.targetVec = PredictUtility.predictPosition(this.target, this.ticks.get(), (this.factor.get()).floatValue()).add(0.0, 1.3F, 0.0);
            }

            if (this.targetVec != null) {
                float delta_yaw = MathHelper.wrapDegrees(
                    (float) MathHelper.wrapDegrees(Math.toDegrees(Math.atan2(this.targetVec.z - this.mc.player.getZ(), this.targetVec.x - this.mc.player.getX())) - 90.0)
                        - this.rotationYaw
                );
                float delta_pitch = (float) (
                    -Math.toDegrees(
                        Math.atan2(
                            this.targetVec.y - (this.mc.player.getPos().y + this.mc.player.getEyeHeight(this.mc.player.getPose())),
                            Math.sqrt(Math.pow(this.targetVec.x - this.mc.player.getX(), 2.0) + Math.pow(this.targetVec.z - this.mc.player.getZ(), 2.0))
                        )
                    )
                )
                    - this.rotationPitch;
                if (delta_yaw > (this.baseYaw.get()).intValue()) {
                    delta_yaw -= (this.baseYaw2.get()).intValue();
                }

                float deltaYaw = MathHelper.clamp(
                    MathHelper.abs(delta_yaw),
                    MathUtility.random((float) (-this.deltaYaw.get()), (float) (-this.deltaYaw2.get())),
                    MathUtility.random((float) (this.deltaYaw.get()).intValue(), (float) (this.deltaYaw2.get()).intValue())
                );
                float newYaw = (float) (
                    this.rotationYaw + (delta_yaw > 0.0F ? deltaYaw : -deltaYaw) + MathUtility.random(-this.rotYawRandom.get(), this.rotYawRandom.get())
                );
                float newPitch = (float) (
                    MathHelper.clamp(
                        this.rotationPitch + MathHelper.clamp(delta_pitch, MathUtility.random(-10.0F, -20.0F), MathUtility.random(10.0F, 20.0F)), -90.0F, 90.0F
                    )
                        + MathUtility.random(-this.rotPitchRandom.get(), this.rotPitchRandom.get())
                );
                double gcdFix = Math.pow(this.mc.options.getMouseSensitivity().getValue() * 0.6 + 0.2, 3.0) * 8.0 * 0.15F;
                this.rotationYaw = (float) (newYaw - (newYaw - this.rotationYaw) % gcdFix);
                this.rotationPitch = (float) (newPitch - (newPitch - this.rotationPitch) % gcdFix);
                RotationUtil.aimAtEntity(
                    this.target,
                    (this.rotationSpeed.get()).floatValue(),
                    this.randomizedRotation.get(),
                    (this.randomTurnSpeed.get()).floatValue(),
                    (this.baseOffset.get()).floatValue(),
                    (this.jitterAmount.get()).floatValue()
                );
                if (this.target != null
                    && (this.mc.player.canSee(this.target) || this.ignoreWalls.get())
                    && this.mc.player.age % this.delay.get() == 0) {
                    this.sendPacket(new UpdateSelectedSlotC2SPacket(InventoryUtility.findProjectileInHotBar().slot()));
                    this.sendPacket(
                        new Full(this.mc.player.getX(), this.mc.player.getY(), this.mc.player.getZ(), this.rotationYaw, this.rotationPitch, this.mc.player.isOnGround())
                    );
                    this.sendSequencedPacket(id -> new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, id, this.mc.player.getYaw(), this.mc.player.getPitch()));
                    this.sendPacket(new UpdateSelectedSlotC2SPacket(this.mc.player.getInventory().selectedSlot));
                }
            }
        }
    }

    private boolean skipEntity(Entity entity) {
        if (entity instanceof LivingEntity ent) {
            if (ent.isDead()) {
                return true;
            } else if (!entity.isAlive()) {
                return true;
            } else if (entity instanceof ArmorStandEntity) {
                return true;
            } else if (entity instanceof PlayerEntity pl) {
                if (entity == this.mc.player) {
                    return true;
                } else {
                    if (entity instanceof PlayerEntity) {
                        if (AntiBot.INSTANCE.inBotList((LivingEntity) entity)) {
                            return true;
                        }

                        if (Friends.get().isFriend((PlayerEntity) entity)) {
                            return true;
                        }
                    }

                    return Math.abs(this.getYawToEntityNew(entity)) > (this.fov.get()).intValue()
                        ? true
                        : this.mc.player.squaredDistanceTo(this.getResolvedPos(entity)) > Math.pow(this.aimRange.get(), 2.0);
                }
            } else {
                return true;
            }
        } else {
            return true;
        }
    }

    public float getYawToEntityNew(@NotNull Entity entity) {
        return this.getYawBetween(this.mc.player.getYaw(), this.mc.player.getX(), this.mc.player.getZ(), entity.getX(), entity.getZ());
    }

    public float getYawBetween(float yaw, double srcX, double srcZ, double destX, double destZ) {
        double xDist = destX - srcX;
        double zDist = destZ - srcZ;
        float yaw1 = (float) (StrictMath.atan2(zDist, xDist) * 180.0 / Math.PI) - 90.0F;
        return yaw + MathHelper.wrapDegrees(yaw1 - yaw);
    }

    private Vec3d getResolvedPos(@NotNull Entity pl) {
        return new Vec3d(
            pl.getX() + (pl.getX() - pl.prevX) * this.predict.get(), pl.getY(), pl.getZ() + (pl.getZ() - pl.prevZ) * this.predict.get()
        );
    }
}
