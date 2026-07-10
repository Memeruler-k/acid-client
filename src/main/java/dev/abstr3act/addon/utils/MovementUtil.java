package dev.abstr3act.addon.utils;

import meteordevelopment.meteorclient.MeteorClient;
import net.minecraft.client.input.Input;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.util.math.MathHelper;

public class MovementUtil {
    public static int fallTicks = 0;

    public static boolean hasMotion() {
        return MeteorClient.mc.player != null
            && (MeteorClient.mc.player.getVelocity().x != 0.0 || MeteorClient.mc.player.getVelocity().y != 0.0 || MeteorClient.mc.player.getVelocity().z != 0.0);
    }

    public static boolean isMoving() {
        return MeteorClient.mc.player != null && (MeteorClient.mc.player.input.movementForward != 0.0 || MeteorClient.mc.player.input.movementSideways != 0.0);
    }

    public static double getBaseMoveSpeed(double customSpeed) {
        double baseSpeed = customSpeed;
        if (MeteorClient.mc.player != null && MeteorClient.mc.player.hasStatusEffect(StatusEffects.SPEED)) {
            StatusEffectInstance effect = MeteorClient.mc.player.getStatusEffect(StatusEffects.SPEED);
            if (effect != null) {
                int amplifier = effect.getAmplifier();
                baseSpeed = customSpeed * (1.0 + 0.2 * (amplifier + 1));
            }
        }

        return baseSpeed;
    }

    public static int getSpeedEffect() {
        if (MeteorClient.mc.player != null && MeteorClient.mc.player.hasStatusEffect(StatusEffects.SPEED)) {
            StatusEffectInstance effect = MeteorClient.mc.player.getStatusEffect(StatusEffects.SPEED);
            if (effect != null) {
                return effect.getAmplifier() + 1;
            }
        }

        return 0;
    }

    public static void stopMoving() {
        if (MeteorClient.mc.player != null) {
            MeteorClient.mc.player.setVelocity(0.0, MeteorClient.mc.player.getVelocity().y, 0.0);
            MeteorClient.mc.player.input.movementForward = 0.0F;
            MeteorClient.mc.player.input.movementSideways = 0.0F;
        }
    }

    public static boolean isDiagonal(float threshold) {
        float yaw = getPlayerDirection();
        yaw = Math.abs((yaw + 360.0F) % 360.0F);
        boolean isNorth = Math.abs(yaw) < threshold || Math.abs(yaw - 360.0F) < threshold;
        boolean isSouth = Math.abs(yaw - 180.0F) < threshold;
        boolean isEast = Math.abs(yaw - 90.0F) < threshold;
        boolean isWest = Math.abs(yaw - 270.0F) < threshold;
        return !isNorth && !isSouth && !isEast && !isWest;
    }

    public static float getPlayerDirection() {
        if (MeteorClient.mc.player == null) {
            return 0.0F;
        } else {
            float yaw = MeteorClient.mc.player.getYaw();
            float strafe = 45.0F;
            Input input = MeteorClient.mc.player.input;
            if (input.movementForward < 0.0F) {
                strafe = -45.0F;
                yaw += 180.0F;
            }

            if (input.pressingLeft) {
                yaw -= strafe;
                if (input.movementForward == 0.0F) {
                    yaw -= 45.0F;
                }
            } else if (input.pressingRight) {
                yaw += strafe;
                if (input.movementForward == 0.0F) {
                    yaw += 45.0F;
                }
            }

            return (yaw % 360.0F + 360.0F) % 360.0F;
        }
    }

    public static void setVelocityY(Double y) {
        if (MeteorClient.mc.player != null) {
            MeteorClient.mc.player.setVelocity(MeteorClient.mc.player.getVelocity().x, y, MeteorClient.mc.player.getVelocity().z);
        }
    }

    public static void smoothStrafe(Float speed) {
        if (MeteorClient.mc.player != null) {
            if (isMoving()) {
                double yaw = Math.toRadians(getPlayerDirection());
                MeteorClient.mc
                    .player
                    .setVelocity(
                        MeteorClient.mc.player.getVelocity().x - Math.sin(yaw) * (speed / 4.0F),
                        MeteorClient.mc.player.getVelocity().y,
                        MeteorClient.mc.player.getVelocity().z + Math.cos(yaw) * (speed / 4.0F)
                    );
            }
        }
    }

    public static float distanceToWithoutY(Entity entity1, Entity entity2) {
        float f = (float) (entity1.getX() - entity2.getX());
        float h = (float) (entity1.getZ() - entity2.getZ());
        return MathHelper.sqrt(f * f + h * h);
    }

    public static void strafe(Float speed) {
        if (MeteorClient.mc.player != null) {
            if (isMoving()) {
                double yaw = Math.toRadians(getPlayerDirection());
                double xSpeed = -Math.sin(yaw);
                double zSpeed = Math.cos(yaw);
                double magnitude = Math.sqrt(xSpeed * xSpeed + zSpeed * zSpeed);
                if (magnitude > 0.0) {
                    xSpeed /= magnitude;
                    zSpeed /= magnitude;
                }

                MeteorClient.mc.player.setVelocity(xSpeed * speed.floatValue(), MeteorClient.mc.player.getVelocity().y, zSpeed * speed.floatValue());
            } else {
                stopMoving();
            }
        }
    }
}
