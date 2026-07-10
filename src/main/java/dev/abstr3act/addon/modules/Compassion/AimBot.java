package dev.abstr3act.addon.modules.Compassion;

import dev.abstr3act.addon.Compassion;
import dev.abstr3act.addon.events.EventPlayerUpdate;
import dev.abstr3act.addon.events.EventSync;
import dev.abstr3act.addon.module.CompassionModule;
import dev.abstr3act.addon.utils.CombatUtils;
import dev.abstr3act.addon.utils.PredictUtility;
import dev.abstr3act.addon.utils.math.MathUtility;
import dev.abstr3act.addon.utils.render.Render2DEngine;
import dev.abstr3act.addon.utils.timers.Timer;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.settings.EnumSetting.Builder;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.entity.EntityUtils;
import meteordevelopment.meteorclient.utils.entity.fakeplayer.FakePlayerEntity;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BowItem;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket.LookAndOnGround;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public final class AimBot extends CompassionModule {
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
    private final Setting<Mode> mode = this.sgGeneral
        .add(((Builder) ((Builder) ((Builder) new Builder().name("Mode")).description("Choose the aiming mode.")).defaultValue(Mode.BowAim)).build());
    private final Setting<Rotation> rotation = this.sgGeneral
        .add(
            ((Builder) ((Builder) ((Builder) ((Builder) new Builder().name("Rotation")).description("Choose the type of rotation.")).defaultValue(Rotation.Silent))
                .visible(() -> this.mode.get() != Mode.AimAssist))
                .build()
        );
    private final Setting<Double> aimRange = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("Range"))
                .description("Maximum range for aiming."))
                .defaultValue(20.0)
                .min(1.0)
                .max(30.0)
                .sliderRange(1.0, 30.0)
                .visible(() -> this.mode.get() != Mode.AimAssist))
                .build()
        );
    private final Setting<Integer> aimStrength = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("AimStrength"))
                .description("Strength of the aim assist."))
                .defaultValue(30))
                .min(1)
                .max(100)
                .sliderRange(1, 100)
                .visible(() -> this.mode.get() == Mode.AimAssist))
                .build()
        );
    private final Setting<Integer> aimSmooth = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("AimSmooth"))
                .description("Smoothing for the aim assist."))
                .defaultValue(45))
                .min(1)
                .max(180)
                .sliderRange(1, 180)
                .visible(() -> this.mode.get() == Mode.AimAssist))
                .build()
        );
    private final Setting<Integer> aimtime = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("AimTime"))
                .description("Duration of aim assist."))
                .defaultValue(2))
                .min(1)
                .max(10)
                .sliderRange(1, 10)
                .visible(() -> this.mode.get() == Mode.AimAssist))
                .build()
        );
    private final Setting<Boolean> ignoreWalls = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("IgnoreWalls"))
                .description("Ignore walls while aiming."))
                .defaultValue(true))
                .visible(() -> this.mode.get() == Mode.CSAim || ((Mode) this.mode.get()).equals(Mode.AimAssist)))
                .build()
        );
    private final Setting<Integer> reactionTime = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("ReactionTime"))
                .description("Time to react to targets."))
                .defaultValue(80))
                .min(1)
                .max(500)
                .sliderRange(1, 500)
                .visible(() -> this.mode.get() == Mode.AimAssist && !this.ignoreWalls.get()))
                .build()
        );
    private final Setting<Boolean> ignoreTeam = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("IgnoreTeam"))
                .description("Ignore teammates while aiming."))
                .defaultValue(true))
                .visible(() -> this.mode.get() == Mode.CSAim || ((Mode) this.mode.get()).equals(Mode.AimAssist)))
                .build()
        );
    private final Setting<Boolean> ignoreInvisible = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("IgnoreInvis"))
                .description("Ignore invisible entities."))
                .defaultValue(false))
                .visible(() -> ((Mode) this.mode.get()).equals(Mode.AimAssist)))
                .build()
        );
    private final Setting<Double> rotYawRandom = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("YawRandom"))
                .description("Randomize yaw while aiming."))
                .defaultValue(0.0)
                .min(0.0)
                .max(3.0)
                .visible(() -> this.mode.get() == Mode.CSAim))
                .build()
        );
    private final Setting<Double> rotPitchRandom = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("PitchRandom"))
                .description("Randomize pitch while aiming."))
                .defaultValue(0.0)
                .min(0.0)
                .max(3.0)
                .visible(() -> this.mode.get() == Mode.CSAim))
                .build()
        );
    private final Setting<Double> predict = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("AimPredict"))
                .description("Prediction value for aiming."))
                .defaultValue(0.5)
                .min(0.5)
                .max(8.0)
                .visible(() -> this.mode.get() == Mode.CSAim))
                .build()
        );
    private final Setting<Integer> delay = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("ShootDelay"))
                .description("Delay before shooting."))
                .defaultValue(5))
                .min(0)
                .max(10)
                .visible(() -> this.mode.get() == Mode.CSAim))
                .build()
        );
    private final Setting<Integer> fov = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("FOV"))
                .description("Field of view for aiming."))
                .defaultValue(65))
                .min(10)
                .max(360)
                .visible(() -> this.mode.get() == Mode.CSAim))
                .build()
        );
    private final Setting<Integer> predictTicks = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("PredictTicks"))
                .description("Ticks to predict ahead while aiming."))
                .defaultValue(2))
                .min(0)
                .max(20)
                .visible(() -> this.mode.get() == Mode.BowAim))
                .build()
        );
    private final Setting<Bone> part = this.sgGeneral
        .add(
            ((Builder) ((Builder) ((Builder) ((Builder) new Builder().name("Bone")).description("Bone to aim at.")).defaultValue(Bone.Head))
                .visible(() -> this.mode.get() == Mode.CSAim))
                .build()
        );
    private Entity target;
    private float rotationYaw;
    private float rotationPitch;
    private float assistAcceleration;
    private int aimTicks = 0;
    private Timer visibleTime = new Timer();

    public AimBot() {
        super(Compassion.COMPASSION, "AimBot", ".");
    }

    @EventHandler
    public void onPlayerUpdate(EventPlayerUpdate event) {
        if (this.mode.get() == Mode.BowAim) {
            if (!(this.mc.player.getActiveItem().getItem() instanceof BowItem)) {
                return;
            }

            CombatUtils combatUtils = new CombatUtils();
            PlayerEntity nearestTarget = combatUtils.getTargetByFOV(128.0F);
            if (nearestTarget == null) {
                return;
            }

            float currentDuration = (this.mc.player.getActiveItem().getMaxUseTime(this.mc.player) - this.mc.player.getItemUseTime()) / 20.0F;
            float var15 = (currentDuration * currentDuration + currentDuration * 2.0F) / 3.0F;
            if (var15 >= 1.0F) {
                var15 = 1.0F;
            }

            float pitch = (float) (-Math.toDegrees(this.calculateArc(nearestTarget, var15 * 3.0F)));
            if (Float.isNaN(pitch)) {
                return;
            }

            PlayerEntity predictedEntity = PredictUtility.predictPlayer(nearestTarget, this.predictTicks.get());
            double iX = predictedEntity.getX() - predictedEntity.prevX;
            double iZ = predictedEntity.getZ() - predictedEntity.prevZ;
            double distance = this.mc.player.distanceTo(predictedEntity);
            distance -= distance % 2.0;
            iX = distance / 2.0 * iX * (this.mc.player.isSprinting() ? 1.3 : 1.1);
            iZ = distance / 2.0 * iZ * (this.mc.player.isSprinting() ? 1.3 : 1.1);
            this.rotationYaw = (float) Math.toDegrees(
                Math.atan2(predictedEntity.getZ() + iZ - this.mc.player.getZ(), predictedEntity.getX() + iX - this.mc.player.getX())
            )
                - 90.0F;
            this.rotationPitch = pitch;
        } else if (this.mode.get() == Mode.CSAim) {
            this.calcThread();
        } else {
            if (this.mc.crosshairTarget.getType() == Type.ENTITY) {
                this.aimTicks++;
            } else {
                this.aimTicks = 0;
            }

            if (this.aimTicks >= this.aimtime.get()) {
                this.assistAcceleration = 0.0F;
                return;
            }

            PlayerEntity nearestTargetx = CombatUtils.getNearestTarget(5.0F);
            this.assistAcceleration = this.assistAcceleration + (this.aimStrength.get()).intValue() / 10000.0F;
            if (nearestTargetx != null) {
                if (!this.mc.player.canSee(nearestTargetx) && !this.ignoreWalls.get()) {
                    this.visibleTime.reset();
                }

                if (!this.visibleTime.passedMs((long) (this.reactionTime.get()).intValue())) {
                    this.rotationYaw = Float.NaN;
                    return;
                }

                if (Float.isNaN(this.rotationYaw)) {
                    this.rotationYaw = this.mc.player.getYaw();
                }

                float delta_yaw = MathHelper.wrapDegrees(
                    (float) MathHelper.wrapDegrees(
                        Math.toDegrees(Math.atan2(nearestTargetx.getEyePos().z - this.mc.player.getZ(), nearestTargetx.getEyePos().x - this.mc.player.getX())) - 90.0
                    )
                        - this.rotationYaw
                );
                if (delta_yaw > 180.0F) {
                    delta_yaw -= 180.0F;
                }

                float deltaYaw = MathHelper.clamp(MathHelper.abs(delta_yaw), -this.aimSmooth.get(), (this.aimSmooth.get()).intValue());
                float newYaw = this.rotationYaw + (delta_yaw > 0.0F ? deltaYaw : -deltaYaw);
                double gcdFix = Math.pow(this.mc.options.getMouseSensitivity().getValue() * 0.6 + 0.2, 3.0) * 1.2;
                this.rotationYaw = (float) (newYaw - (newYaw - this.rotationYaw) % gcdFix);
            } else {
                this.rotationYaw = Float.NaN;
            }
        }

        if (!Float.isNaN(this.rotationYaw)) {
            ((MoveFix) Modules.get().get(MoveFix.class)).fixRotation = this.rotationYaw;
        }
    }

    @EventHandler
    public void onSync(EventSync event) {
        if (!((Mode) this.mode.get()).equals(Mode.AimAssist)) {
            if (((Mode) this.mode.get()).equals(Mode.CSAim)) {
                if (this.target != null && (this.mc.player.canSee(this.target) || this.ignoreWalls.get())) {
                    if (this.mc.player.age % this.delay.get() == 0) {
                        event.addPostAction(
                            () -> this.sendSequencedPacket(id -> new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, id, this.mc.player.getYaw(), this.mc.player.getPitch()))
                        );
                    }
                } else {
                    this.rotationYaw = this.mc.player.getYaw();
                    this.rotationPitch = this.mc.player.getPitch();
                }
            }

            if ((this.target != null || this.mode.get() == Mode.BowAim && this.mc.player.getActiveItem().getItem() instanceof BowItem)
                && this.rotation.get() == Rotation.Silent) {
                this.sendPacket(new LookAndOnGround(this.rotationYaw, this.rotationPitch, this.mc.player.isOnGround()));
            }
        }
    }

    public void onDeactivate() {
        this.target = null;
        this.rotationYaw = this.mc.player.getYaw();
        this.rotationPitch = this.mc.player.getPitch();
    }

    @EventHandler
    public void onRender3D(Render3DEvent event) {
        if (this.mode.get() == Mode.AimAssist) {
            if (!Float.isNaN(this.rotationYaw)) {
                this.mc.player.setYaw((float) Render2DEngine.interpolate(this.mc.player.getYaw(), this.rotationYaw, this.assistAcceleration));
            }
        } else {
            if (this.target != null && (this.mc.player.canSee(this.target) || this.ignoreWalls.get())) {
                if (this.rotation.get() == Rotation.Client) {
                    this.mc.player.setYaw((float) Render2DEngine.interpolate(this.mc.player.prevYaw, this.rotationYaw, this.mc.getRenderTickCounter().getTickDelta(true)));
                    this.mc
                        .player
                        .setPitch((float) Render2DEngine.interpolate(this.mc.player.prevPitch, this.rotationPitch, this.mc.getRenderTickCounter().getTickDelta(true)));
                }
            } else if (this.mode.get() == Mode.CSAim) {
                this.rotationYaw = this.mc.player.getYaw();
                this.rotationPitch = this.mc.player.getPitch();
            }

            if (this.rotation.get() == Rotation.Client && this.mode.get() == Mode.BowAim && this.mc.player.getActiveItem().getItem() instanceof BowItem
            ) {
                this.mc.player.setYaw((float) Render2DEngine.interpolate(this.mc.player.prevYaw, this.rotationYaw, this.mc.getRenderTickCounter().getTickDelta(true)));
                this.mc
                    .player
                    .setPitch((float) Render2DEngine.interpolate(this.mc.player.prevPitch, this.rotationPitch, this.mc.getRenderTickCounter().getTickDelta(true)));
            }
        }
    }

    private float calculateArc(@NotNull PlayerEntity target, double duration) {
        double yArc = target.getY() + target.getEyeHeight(target.getPose()) - (this.mc.player.getY() + this.mc.player.getEyeHeight(this.mc.player.getPose()));
        double dX = target.getX() - this.mc.player.getX();
        double dZ = target.getZ() - this.mc.player.getZ();
        double dirRoot = Math.sqrt(dX * dX + dZ * dZ);
        return this.calculateArc(duration, dirRoot, yArc);
    }

    private float calculateArc(double d, double dr, double y) {
        y = 2.0 * y * (d * d);
        y = 0.05F * (0.05F * (dr * dr) + y);
        y = Math.sqrt(d * d * d * d - y);
        d = d * d - y;
        y = Math.atan2(d * d + y, 0.05F * dr);
        d = Math.atan2(d, 0.05F * dr);
        return (float) Math.min(y, d);
    }

    private void calcThread() {
        if (this.target == null) {
            this.findTarget();
        } else if (this.skipEntity(this.target)) {
            this.target = null;
        } else {
            Vec3d targetVec = this.getResolvedPos(this.target).add(0.0, ((Bone) this.part.get()).getH(), 0.0);
            if (targetVec != null) {
                float delta_yaw = MathHelper.wrapDegrees(
                    (float) MathHelper.wrapDegrees(Math.toDegrees(Math.atan2(targetVec.z - this.mc.player.getZ(), targetVec.x - this.mc.player.getX())) - 90.0)
                        - this.rotationYaw
                );
                float delta_pitch = (float) (
                    -Math.toDegrees(
                        Math.atan2(
                            targetVec.y - (this.mc.player.getPos().y + this.mc.player.getEyeHeight(this.mc.player.getPose())),
                            Math.sqrt(Math.pow(targetVec.x - this.mc.player.getX(), 2.0) + Math.pow(targetVec.z - this.mc.player.getZ(), 2.0))
                        )
                    )
                )
                    - this.rotationPitch;
                if (delta_yaw > 180.0F) {
                    delta_yaw -= 180.0F;
                }

                float deltaYaw = MathHelper.clamp(MathHelper.abs(delta_yaw), MathUtility.random(-40.0F, -60.0F), MathUtility.random(40.0F, 60.0F));
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
            }
        }
    }

    public void findTarget() {
        List<Entity> first_stage = new CopyOnWriteArrayList<>();

        for (Entity entity : this.mc.world.getEntities()) {
            if (!this.skipEntity(entity)) {
                first_stage.add(entity);
            }
        }

        float best_fov = (this.fov.get()).intValue();
        Entity best_entity = null;

        for (Entity ent : first_stage) {
            float temp_fov = Math.abs(
                (float) MathHelper.wrapDegrees(Math.toDegrees(Math.atan2(ent.getZ() - this.mc.player.getZ(), ent.getX() - this.mc.player.getX())) - 90.0)
                    - MathHelper.wrapDegrees(this.mc.player.getYaw())
            );
            if (temp_fov < best_fov) {
                best_entity = ent;
                best_fov = temp_fov;
            }
        }

        this.target = best_entity;
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
                if (entity instanceof FakePlayerEntity) {
                    return true;
                } else if (entity == this.mc.player) {
                    return true;
                } else if (entity.isInvisible() && this.ignoreInvisible.get()) {
                    return true;
                } else if (entity instanceof PlayerEntity && Friends.get().isFriend((PlayerEntity) entity)) {
                    return true;
                } else if (Math.abs(this.getYawToEntityNew(entity)) > (this.fov.get()).intValue()) {
                    return true;
                } else if (pl.getTeamColorValue() == this.mc.player.getTeamColorValue()
                    && this.ignoreTeam.get()
                    && this.mc.player.getTeamColorValue() != 16777215) {
                    return true;
                } else {
                    GameMode gm = EntityUtils.getGameMode((PlayerEntity) entity);
                    return gm == null ? true : this.mc.player.squaredDistanceTo(this.getResolvedPos(entity)) > Math.pow(this.aimRange.get(), 2.0);
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

    private static enum Bone {
        Head(1.7F),
        Neck(1.5F),
        Torso(1.0F),
        Tights(0.8F),
        Feet(0.25F);

        private final float h;

        private Bone(float h) {
            this.h = h;
        }

        public float getH() {
            return this.h;
        }
    }

    private static enum Mode {
        CSAim,
        AimAssist,
        BowAim;
    }

    private static enum Rotation {
        Client,
        Silent;
    }
}
