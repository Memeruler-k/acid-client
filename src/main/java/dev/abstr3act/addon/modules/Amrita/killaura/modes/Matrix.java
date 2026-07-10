package dev.abstr3act.addon.modules.Amrita.killaura.modes;

import dev.abstr3act.addon.modules.Amrita.criticals.CriticalsPlus;
import dev.abstr3act.addon.modules.Amrita.killaura.KillAuraPlusMode;
import dev.abstr3act.addon.modules.Amrita.killaura.KillAuraPlusModes;
import dev.abstr3act.addon.modules.Amrita.killaura.sb.GameSensitivityUtils;
import dev.abstr3act.addon.modules.Amrita.killaura.sb.RaycastUtils;
import meteordevelopment.meteorclient.events.world.TickEvent.Pre;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.systems.modules.combat.KillAura.ShieldMode;
import meteordevelopment.meteorclient.utils.entity.SortPriority;
import meteordevelopment.meteorclient.utils.entity.TargetUtils;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.player.Rotations;
import net.minecraft.client.util.math.Vector2f;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Tameable;
import net.minecraft.entity.mob.EndermanEntity;
import net.minecraft.entity.mob.ZombifiedPiglinEntity;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.apache.commons.lang3.time.StopWatch;

import java.util.ArrayList;
import java.util.Set;

public class Matrix extends KillAuraPlusMode {
    private final ArrayList<Entity> targets = new ArrayList<>();
    private final StopWatch stopWatch = new StopWatch();
    float lastYaw;
    float lastPitch;
    int ticks = 0;
    boolean isRotated;
    private Vector2f rotateVector = new Vector2f(0.0F, 0.0F);
    private LivingEntity target;
    private Entity selected;

    public Matrix() {
        super(KillAuraPlusModes.Matrix);
    }

    @Override
    public void onTickPre(Pre event) {
        if (this.target == null || !this.entityCheck(this.target)) {
            TargetUtils.getList(this.targets, this::entityCheck, (SortPriority) this.settings.priority.get(), 15);
            if (!this.targets.isEmpty() && this.targets.get(0) instanceof LivingEntity livingEntity) {
                this.target = livingEntity;
            }
        }

        if (this.target != null && this.target.isAlive()) {
            this.isRotated = false;
            EntityHitResult result = RaycastUtils.raycastEntity(this.settings.range.get(), this.rotateVector.getX(), this.rotateVector.getY(), 0.0);
            if ((!this.settings.onlyCrits.get() || CriticalsPlus.allowCrit() || !CriticalsPlus.needCrit(this.target))
                && this.delayCheck()
                && result != null
                && result.getType() == net.minecraft.util.hit.HitResult.Type.ENTITY) {
                this.attack(this.target);
                this.ticks = 2;
            }

            if (this.settings.rotationType.get() == Type.Fast) {
                if (this.ticks > 0) {
                    this.updateRotation(true, 180.0F, 90.0F);
                    Rotations.rotate(this.rotateVector.getX(), this.rotateVector.getY());
                    this.ticks--;
                } else {
                    this.reset();
                }
            } else if (!this.isRotated) {
                this.updateRotation(false, 80.0F, 35.0F);
                Rotations.rotate(this.rotateVector.getX(), this.rotateVector.getY());
            }
        } else {
            this.reset();
        }
    }

    private boolean delayCheck() {
        return this.mc.player.getAttackCooldownProgress(0.5F) >= 1.0F;
    }

    private void attack(Entity target) {
        this.mc.interactionManager.attackEntity(this.mc.player, target);
        this.mc.player.swingHand(Hand.MAIN_HAND);
    }

    private boolean entityCheck(Entity entity) {
        if (!entity.equals(this.mc.player) && !entity.equals(this.mc.cameraEntity)) {
            if (!(entity instanceof LivingEntity livingEntity && livingEntity.isDead()) && entity.isAlive()) {
                Box hitbox = entity.getBoundingBox();
                if (!PlayerUtils.isWithin(
                    MathHelper.clamp(this.mc.player.getX(), hitbox.minX, hitbox.maxX),
                    MathHelper.clamp(this.mc.player.getY(), hitbox.minY, hitbox.maxY),
                    MathHelper.clamp(this.mc.player.getZ(), hitbox.minZ, hitbox.maxZ),
                    this.settings.range.get()
                )) {
                    return false;
                } else if (!((Set) this.settings.entities.get()).contains(entity.getType())) {
                    return false;
                } else if (!PlayerUtils.canSeeEntity(entity) && !PlayerUtils.isWithin(entity, this.settings.wallsRange.get())) {
                    return false;
                } else if (this.settings.ignoreTamed.get()
                    && entity instanceof Tameable tameable
                    && tameable.getOwnerUuid() != null
                    && tameable.getOwnerUuid().equals(this.mc.player.getUuid())) {
                    return false;
                } else {
                    if (this.settings.ignorePassive.get()) {
                        if (entity instanceof EndermanEntity enderman && !enderman.isAngry()) {
                            return false;
                        }

                        if (entity instanceof ZombifiedPiglinEntity piglin && !piglin.isAttacking()) {
                            return false;
                        }

                        if (entity instanceof WolfEntity wolf && !wolf.isAttacking()) {
                            return false;
                        }
                    }

                    if (entity instanceof PlayerEntity player) {
                        if (player.isCreative()) {
                            return false;
                        }

                        if (!Friends.get().shouldAttack(player)) {
                            return false;
                        }

                        if (this.settings.shieldMode.get() == ShieldMode.Ignore && player.blockedByShield(this.mc.world.getDamageSources().playerAttack(this.mc.player))) {
                            return false;
                        }
                    }

                    return true;
                }
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    private void updateRotation(boolean attack, float rotationYawSpeed, float rotationPitchSpeed) {
        Vec3d vec = this.target
            .getPos()
            .add(
                0.0,
                MathHelper.clamp(
                    this.mc.player.getEyeHeight(this.mc.player.getPose()) - this.target.getY(),
                    0.0,
                    this.target.getHeight() * (this.mc.player.distanceTo(this.target) / this.settings.range.get())
                ),
                0.0
            )
            .subtract(this.mc.player.getEyePos());
        this.isRotated = true;
        float yawToTarget = (float) MathHelper.wrapDegrees(Math.toDegrees(Math.atan2(vec.z, vec.x)) - 90.0);
        float pitchToTarget = (float) (-Math.toDegrees(Math.atan2(vec.y, MathHelper.hypot(vec.x, vec.z))));
        float yawDelta = MathHelper.wrapDegrees(yawToTarget - this.rotateVector.getX());
        float pitchDelta = MathHelper.wrapDegrees(pitchToTarget - this.rotateVector.getY());
        int roundedYaw = (int) yawDelta;
        switch ((Type) this.settings.rotationType.get()) {
            case Smooth: {
                float clampedYaw = Math.min(Math.max(Math.abs(yawDelta), 1.0F), rotationYawSpeed);
                float clampedPitch = Math.min(Math.max(Math.abs(pitchDelta), 1.0F), rotationPitchSpeed);
                if (attack && this.selected != this.target && this.settings.speedUpRotationWhenAttacking.get()) {
                    clampedPitch = Math.max(Math.abs(pitchDelta), 1.0F);
                } else {
                    clampedPitch /= 3.0F;
                }

                if (Math.abs(clampedYaw - this.lastYaw) <= 3.0F) {
                    clampedYaw = this.lastYaw + 3.1F;
                }

                float yaw = this.rotateVector.getX() + (yawDelta > 0.0F ? clampedYaw : -clampedYaw);
                float pitch = MathHelper.clamp(this.rotateVector.getY() + (pitchDelta > 0.0F ? clampedPitch : -clampedPitch), -89.0F, 89.0F);
                float gcd = GameSensitivityUtils.getGCDValue();
                yaw -= (yaw - this.rotateVector.getX()) % gcd;
                pitch -= (pitch - this.rotateVector.getY()) % gcd;
                this.rotateVector = new Vector2f(yaw, pitch);
                this.lastYaw = clampedYaw;
                this.lastPitch = clampedPitch;
                break;
            }
            case Fast: {
                float yaw = this.rotateVector.getX() + roundedYaw;
                float pitch = MathHelper.clamp(this.rotateVector.getY() + pitchDelta, -90.0F, 90.0F);
                float gcd = GameSensitivityUtils.getGCDValue();
                yaw -= (yaw - this.rotateVector.getX()) % gcd;
                pitch -= (pitch - this.rotateVector.getY()) % gcd;
                this.rotateVector = new Vector2f(yaw, pitch);
            }
        }
    }

    private void reset() {
        this.rotateVector = new Vector2f(this.mc.player.getYaw(), this.mc.player.getPitch());
    }

    public static enum Type {
        Smooth,
        Fast;
    }
}
