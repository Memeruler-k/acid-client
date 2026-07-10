package dev.abstr3act.addon.utils;

import dev.abstr3act.addon.events.EventMove;
import dev.abstr3act.addon.module.BaseModule;
import meteordevelopment.meteorclient.MeteorClient;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.util.math.MathHelper;

public final class MovementUtility {
    public static boolean isMoving() {
        return MeteorClient.mc.player != null
            && MeteorClient.mc.world != null
            && MeteorClient.mc.player.input != null
            && (MeteorClient.mc.player.input.movementForward != 0.0 || MeteorClient.mc.player.input.movementSideways != 0.0);
    }

    public static double getSpeed() {
        return Math.hypot(MeteorClient.mc.player.getVelocity().x, MeteorClient.mc.player.getVelocity().z);
    }

    public static double[] forward(double d) {
        float f = MeteorClient.mc.player.input.movementForward;
        float f2 = MeteorClient.mc.player.input.movementSideways;
        float f3 = MeteorClient.mc.player.getYaw();
        if (f != 0.0F) {
            if (f2 > 0.0F) {
                f3 += f > 0.0F ? -45 : 45;
            } else if (f2 < 0.0F) {
                f3 += f > 0.0F ? 45 : -45;
            }

            f2 = 0.0F;
            if (f > 0.0F) {
                f = 1.0F;
            } else if (f < 0.0F) {
                f = -1.0F;
            }
        }

        double d2 = Math.sin(Math.toRadians(f3 + 90.0F));
        double d3 = Math.cos(Math.toRadians(f3 + 90.0F));
        double d4 = f * d * d3 + f2 * d * d2;
        double d5 = f * d * d2 - f2 * d * d3;
        return new double[]{d4, d5};
    }

    public static void setMotion(double speed) {
        double forward = MeteorClient.mc.player.input.movementForward;
        double strafe = MeteorClient.mc.player.input.movementSideways;
        float yaw = MeteorClient.mc.player.getYaw();
        if (forward == 0.0 && strafe == 0.0) {
            MeteorClient.mc.player.setVelocity(0.0, MeteorClient.mc.player.getVelocity().y, 0.0);
        } else {
            if (forward != 0.0) {
                if (strafe > 0.0) {
                    yaw += forward > 0.0 ? -45 : 45;
                } else if (strafe < 0.0) {
                    yaw += forward > 0.0 ? 45 : -45;
                }

                strafe = 0.0;
                if (forward > 0.0) {
                    forward = 1.0;
                } else if (forward < 0.0) {
                    forward = -1.0;
                }
            }

            double sin = MathHelper.sin((float) Math.toRadians(yaw + 90.0F));
            double cos = MathHelper.cos((float) Math.toRadians(yaw + 90.0F));
            MeteorClient.mc
                .player
                .setVelocity(forward * speed * cos + strafe * speed * sin, MeteorClient.mc.player.getVelocity().y, forward * speed * sin - strafe * speed * cos);
        }
    }

    public static float getMoveDirection() {
        double forward = MeteorClient.mc.player.input.movementForward;
        double strafe = MeteorClient.mc.player.input.movementSideways;
        if (strafe > 0.0) {
            strafe = 1.0;
        } else if (strafe < 0.0) {
            strafe = -1.0;
        }

        float yaw = MeteorClient.mc.player.getYaw();
        if (forward == 0.0 && strafe == 0.0) {
            return yaw;
        } else {
            if (forward != 0.0) {
                if (strafe > 0.0) {
                    yaw += forward > 0.0 ? -45.0F : -135.0F;
                } else if (strafe < 0.0) {
                    yaw += forward > 0.0 ? 45.0F : 135.0F;
                } else if (forward < 0.0) {
                    yaw += 180.0F;
                }
            }

            if (forward == 0.0) {
                if (strafe > 0.0) {
                    yaw -= 90.0F;
                } else if (strafe < 0.0) {
                    yaw += 90.0F;
                }
            }

            return yaw;
        }
    }

    public static double[] forwardWithoutStrafe(double d) {
        float f3 = MeteorClient.mc.player.getYaw();
        double d4 = d * Math.cos(Math.toRadians(f3 + 90.0F));
        double d5 = d * Math.sin(Math.toRadians(f3 + 90.0F));
        return new double[]{d4, d5};
    }

    public static double getJumpSpeed() {
        double jumpSpeed = 0.39999995F;
        if (MeteorClient.mc.player.hasStatusEffect(StatusEffects.JUMP_BOOST)) {
            double amplifier = MeteorClient.mc.player.getStatusEffect(StatusEffects.JUMP_BOOST).getAmplifier();
            jumpSpeed += (amplifier + 1.0) * 0.1;
        }

        return jumpSpeed;
    }

    public static void modifyEventSpeed(EventMove event, double d) {
        double d2 = MeteorClient.mc.player.input.movementForward;
        double d3 = MeteorClient.mc.player.input.movementSideways;
        float f = MeteorClient.mc.player.getYaw();
        if (d2 == 0.0 && d3 == 0.0) {
            event.setX(0.0);
            event.setZ(0.0);
        } else {
            if (d2 != 0.0) {
                if (d3 > 0.0) {
                    f += d2 > 0.0 ? -45 : 45;
                } else if (d3 < 0.0) {
                    f += d2 > 0.0 ? 45 : -45;
                }

                d3 = 0.0;
                if (d2 > 0.0) {
                    d2 = 1.0;
                } else if (d2 < 0.0) {
                    d2 = -1.0;
                }
            }

            double sin = Math.sin(Math.toRadians(f + 90.0F));
            double cos = Math.cos(Math.toRadians(f + 90.0F));
            event.setX(d2 * d * cos + d3 * d * sin);
            event.setZ(d2 * d * sin - d3 * d * cos);
        }
    }

    public static double getBaseMoveSpeed() {
        double d = 0.2873;
        if (BaseModule.fullNullCheck()) {
            return d;
        } else {
            if (MeteorClient.mc.player.hasStatusEffect(StatusEffects.SPEED)) {
                int n = MeteorClient.mc.player.getStatusEffect(StatusEffects.SPEED).getAmplifier();
                d *= 1.0 + 0.2 * (n + 1);
            }

            if (MeteorClient.mc.player.hasStatusEffect(StatusEffects.JUMP_BOOST)) {
                int n = MeteorClient.mc.player.getStatusEffect(StatusEffects.JUMP_BOOST).getAmplifier();
                d /= 1.0 + 0.2 * (n + 1);
            }

            if (MeteorClient.mc.player.hasStatusEffect(StatusEffects.SLOWNESS)) {
                int n = MeteorClient.mc.player.getStatusEffect(StatusEffects.SLOWNESS).getAmplifier();
                d /= 1.0 + 0.2 * (n + 1);
            }

            return d;
        }
    }
}
