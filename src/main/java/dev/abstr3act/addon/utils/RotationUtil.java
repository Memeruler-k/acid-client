package dev.abstr3act.addon.utils;

import dev.abstr3act.addon.events.legacy.MotionEvent;
import dev.abstr3act.addon.utils.abnormally.AChatUtils;
import kotlin.Pair;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class RotationUtil {
    public static final List<Module> modules = new ArrayList<>();
    private static final Random RANDOM = new Random();
    public static float baseYaw = 0.0F;
    public static float basePitch = 0.0F;
    public static Float currentYaw = null;
    public static Float currentPitch = null;
    public static boolean isRotating = false;
    public static float easeYaw = 0.0F;
    public static float easePitch = 0.0F;
    public static boolean isDebugRendering = false;
    public static Float interpolatedYaw = null;
    public static Float interpolatedPitch = null;
    public static int pitchTick = 0;
    public static Long lastTime = 0L;
    public static DecimalFormat decimalFormat = new DecimalFormat("#.##");
    public static String bps = "0.00";
    public static float[] currentRotation = null;
    public static float[] serverRotation = new float[0];
    public static float[] previousRotation = null;
    public static MovementCorrection currentCorrection = MovementCorrection.OFF;
    static Random random = new Random();
    private Vec3d lastPosition = null;
    private double accumulatedDistance = 0.0;

    public static void reset() {
        currentYaw = null;
        currentPitch = null;
        interpolatedYaw = null;
        interpolatedPitch = null;
        pitchTick = 0;
        isRotating = false;
    }

    public static Pair<Float, Float> getRotationsEntity(LivingEntity entity) {
        return getRotations(entity.getX(), entity.getY() + entity.getStandingEyeHeight() - 0.4, entity.getZ());
    }

    public static void aimAtEntity(Entity target, float speed, boolean random, float randomSpeed, float baseMaxOffset, float jitterAmount) {
        new Thread(() -> {
            float adjustedMaxOffset = baseMaxOffset * (speed / 10.0F);
            double targetCenterX = target.getX() + getRandomOffset(adjustedMaxOffset);
            double targetCenterY = target.getY() + target.getHeight() / 2.0F + getRandomOffset(adjustedMaxOffset);
            double targetCenterZ = target.getZ() + getRandomOffset(adjustedMaxOffset);
            double playerEyeY = MeteorClient.mc.player.getY() + MeteorClient.mc.player.getStandingEyeHeight();
            double xDiff = targetCenterX - MeteorClient.mc.player.getX();
            double yDiff = targetCenterY - playerEyeY;
            double zDiff = targetCenterZ - MeteorClient.mc.player.getZ();
            double distance = Math.sqrt(xDiff * xDiff + zDiff * zDiff);
            float targetYaw = (float) (Math.toDegrees(Math.atan2(zDiff, xDiff)) - 90.0);
            float targetPitch = (float) (-Math.toDegrees(Math.atan2(yDiff, distance)));
            isRotating = true;
            if (currentYaw == null) {
                currentYaw = baseYaw;
            }

            if (currentPitch == null) {
                currentPitch = basePitch;
            }

            if (currentPitch > 90.0F) {
                currentPitch = 90.0F;
            } else if (currentPitch < -90.0F) {
                currentPitch = -90.0F;
            } else {
                float rotSpeed = random ? speed + new Random().nextFloat() * randomSpeed : speed;
                if (rotSpeed <= 0.0F) {
                    rotSpeed = 0.0F;
                }

                float[] newRotation = humanizeRotation(currentYaw, currentPitch, targetYaw, targetPitch, rotSpeed, jitterAmount);
                currentYaw = newRotation[0];
                currentPitch = newRotation[1];
                applyHumanLikeRotation(currentYaw, currentPitch);
                applyTimingDelay(50L, 100L);
            }
        }).start();
    }

    private static float[] smoothHumanRotation(
        float currentYaw,
        float currentPitch,
        float targetYaw,
        float targetPitch,
        float speed,
        float jitter,
        float yawOffset,
        float pitchOffset,
        float yawFactor,
        float pitchFactor
    ) {
        float yawDiff = MathHelper.wrapDegrees(targetYaw - currentYaw);
        float pitchDiff = targetPitch - currentPitch;
        float yawChange = yawDiff / Math.abs(yawDiff) * Math.min(speed, Math.abs(yawDiff));
        float pitchChange = pitchDiff / Math.abs(pitchDiff) * Math.min(speed, Math.abs(pitchDiff));
        Random rand = new Random();
        yawChange += (rand.nextFloat() - yawOffset) * jitter;
        pitchChange += (rand.nextFloat() - pitchOffset) * jitter;
        currentYaw += yawChange * yawFactor;
        currentPitch += pitchChange * pitchFactor;
        return new float[]{currentYaw, currentPitch};
    }

    public static void aimAtEntity2(
        Entity target,
        float maxSpeed,
        float minSpeed,
        float speedFactor,
        boolean random,
        float baseMaxOffset,
        float baseMinOffset,
        float jitterAmount,
        float maxOffset,
        float minOffset,
        long maxDelay,
        long minDelay,
        float targetHeightFactor,
        float randomAngleMax,
        float randomAngleMin,
        float randomChanceMax,
        float randomChanceMin,
        boolean closeRotateFaster,
        boolean farRotateFaster,
        float yawOffset,
        float pitchOffset,
        float yawFactor,
        float pitchFactor
    ) {
        new Thread(
            () -> {
                try {
                    float adjustedMaxOffset = baseMaxOffset * (maxSpeed / speedFactor);
                    float adjustedMinOffset = baseMinOffset * (minSpeed / speedFactor);
                    double targetCenterX = target.getX() + getRandomOffset(adjustedMaxOffset, adjustedMinOffset);
                    double targetCenterY = target.getY() + target.getHeight() / targetHeightFactor + getRandomOffset(adjustedMaxOffset, adjustedMinOffset);
                    double targetCenterZ = target.getZ() + getRandomOffset(adjustedMaxOffset, adjustedMinOffset);
                    double playerEyeY = MeteorClient.mc.player.getY() + MeteorClient.mc.player.getStandingEyeHeight();
                    double xDiff = targetCenterX - MeteorClient.mc.player.getX();
                    double yDiff = targetCenterY - playerEyeY;
                    double zDiff = targetCenterZ - MeteorClient.mc.player.getZ();
                    double distance = Math.sqrt(xDiff * xDiff + zDiff * zDiff);
                    float targetYaw = (float) (Math.toDegrees(Math.atan2(zDiff, xDiff)) - 90.0);
                    float targetPitch = (float) (-Math.toDegrees(Math.atan2(yDiff, distance)));
                    isRotating = true;
                    if (currentYaw == null) {
                        currentYaw = baseYaw;
                    }

                    if (currentPitch == null) {
                        currentPitch = basePitch;
                    }

                    currentPitch = Math.max(-90.0F, Math.min(90.0F, currentPitch));
                    float dynamicSpeed = minSpeed + (maxSpeed - minSpeed) * (float) Math.sqrt(distance) / speedFactor;
                    if (closeRotateFaster) {
                        dynamicSpeed = maxSpeed - (maxSpeed - minSpeed) * (float) Math.sqrt(distance) / speedFactor;
                    }

                    if (farRotateFaster) {
                        dynamicSpeed = minSpeed + (maxSpeed - minSpeed) * (float) Math.sqrt(distance) / speedFactor;
                    }

                    float var53 = Math.max(minSpeed, Math.min(maxSpeed, dynamicSpeed));
                    Random rand = new Random();
                    float randomChance = randomChanceMin + rand.nextFloat() * (randomChanceMax - randomChanceMin);
                    boolean shouldRotateRandomly = rand.nextFloat() < randomChance;
                    float rotSpeed = var53;
                    if (random && shouldRotateRandomly) {
                        boolean reverseRotation = rand.nextBoolean();
                        float randomAngle = randomAngleMin + rand.nextFloat() * (randomAngleMax - randomAngleMin);
                        rotSpeed = var53 + (reverseRotation ? -randomAngle : randomAngle);
                    }

                    float[] smoothRotation = smoothHumanRotation(
                        currentYaw, currentPitch, targetYaw, targetPitch, rotSpeed, jitterAmount, yawOffset, pitchOffset, yawFactor, pitchFactor
                    );
                    currentYaw = smoothRotation[0];
                    currentPitch = smoothRotation[1];
                    applyHumanLikeRotation(currentYaw, currentPitch, maxOffset, minOffset);
                    applyTimingDelay(maxDelay, minDelay);
                } catch (Exception var52) {
                    AChatUtils.sendMsgAmrita(Text.of(var52.getMessage()));
                }
            }
        )
            .start();
    }

    public static void aimAtEntity(
        Entity target,
        float maxSpeed,
        float minSpeed,
        float speedFactor,
        boolean random,
        float baseMaxOffset,
        float baseMinOffset,
        float jitterAmount,
        float maxOffset,
        float minOffset,
        long maxDelay,
        long minDelay,
        float targetHeightFactor,
        float randomAngleMax,
        float randomAngleMin,
        float randomChanceMax,
        float randomChanceMin,
        boolean closeRotateFaster,
        boolean farRotateFaster
    ) {
        new Thread(() -> {
            try {
                float adjustedMaxOffset = baseMaxOffset * (maxSpeed / speedFactor);
                float adjustedMinOffset = baseMinOffset * (minSpeed / speedFactor);
                double targetCenterX = target.getX() + getRandomOffset(adjustedMaxOffset, adjustedMinOffset);
                double targetCenterY = target.getY() + target.getHeight() / targetHeightFactor + getRandomOffset(adjustedMaxOffset, adjustedMinOffset);
                double targetCenterZ = target.getZ() + getRandomOffset(adjustedMaxOffset, adjustedMinOffset);
                double playerEyeY = MeteorClient.mc.player.getY() + MeteorClient.mc.player.getStandingEyeHeight();
                double xDiff = targetCenterX - MeteorClient.mc.player.getX();
                double yDiff = targetCenterY - playerEyeY;
                double zDiff = targetCenterZ - MeteorClient.mc.player.getZ();
                double distance = Math.sqrt(xDiff * xDiff + zDiff * zDiff);
                float targetYaw = (float) (Math.toDegrees(Math.atan2(zDiff, xDiff)) - 90.0);
                float targetPitch = (float) (-Math.toDegrees(Math.atan2(yDiff, distance)));
                isRotating = true;
                if (currentYaw == null) {
                    currentYaw = baseYaw;
                }

                if (currentPitch == null) {
                    currentPitch = basePitch;
                }

                if (currentPitch > 90.0F) {
                    currentPitch = 90.0F;
                    return;
                }

                if (currentPitch < -90.0F) {
                    currentPitch = -90.0F;
                    return;
                }

                float dynamicSpeed = minSpeed + (maxSpeed - minSpeed) * (float) Math.sqrt(distance) / speedFactor;
                if (closeRotateFaster) {
                    dynamicSpeed = maxSpeed - (maxSpeed - minSpeed) * (float) Math.sqrt(distance) / speedFactor;
                }

                if (farRotateFaster) {
                    dynamicSpeed = minSpeed + (maxSpeed - minSpeed) * (float) Math.sqrt(distance) / speedFactor;
                }

                if (dynamicSpeed > maxSpeed) {
                    dynamicSpeed = maxSpeed;
                }

                if (dynamicSpeed < minSpeed) {
                    dynamicSpeed = minSpeed;
                }

                Random rand = new Random();
                float randomChance = randomChanceMin + rand.nextFloat() * (randomChanceMax - randomChanceMin);
                boolean shouldRotateRandomly = rand.nextFloat() < randomChance;
                float rotSpeed;
                if (random && shouldRotateRandomly) {
                    boolean reverseRotation = rand.nextBoolean();
                    float randomAngle = randomAngleMin + rand.nextFloat() * (randomAngleMax - randomAngleMin);
                    rotSpeed = dynamicSpeed + (reverseRotation ? -randomAngle : randomAngle);
                } else {
                    rotSpeed = dynamicSpeed;
                }

                if (rotSpeed <= 0.0F) {
                    rotSpeed = 0.0F;
                }

                float[] newRotation = humanizeRotation(currentYaw, currentPitch, targetYaw, targetPitch, rotSpeed, jitterAmount);
                currentYaw = newRotation[0];
                currentPitch = newRotation[1];
                applyHumanLikeRotation(currentYaw, currentPitch, maxOffset, minOffset);
                applyTimingDelay(maxDelay, minDelay);
            } catch (Exception var48) {
                AChatUtils.sendMsgAmrita(Text.of(var48.getMessage()));
            }
        }).start();
    }

    public static void aimAtEntity(
        Entity target,
        float maxSpeed,
        float minSpeed,
        float speedFactor,
        boolean random,
        float randomSpeed,
        float baseMaxOffset,
        float baseMinOffset,
        float jitterAmount,
        float maxOffset,
        float minOffset,
        long maxDelay,
        long minDelay,
        float targetHeightFactor
    ) {
        new Thread(() -> {
            try {
                float adjustedMaxOffset = baseMaxOffset * (maxSpeed / speedFactor);
                float adjustedMinOffset = baseMinOffset * (minSpeed / speedFactor);
                double targetCenterX = target.getX() + getRandomOffset(adjustedMaxOffset, adjustedMinOffset);
                double targetCenterY = target.getY() + target.getHeight() / targetHeightFactor + getRandomOffset(adjustedMaxOffset, adjustedMinOffset);
                double targetCenterZ = target.getZ() + getRandomOffset(adjustedMaxOffset, adjustedMinOffset);
                double playerEyeY = MeteorClient.mc.player.getY() + MeteorClient.mc.player.getStandingEyeHeight();
                double xDiff = targetCenterX - MeteorClient.mc.player.getX();
                double yDiff = targetCenterY - playerEyeY;
                double zDiff = targetCenterZ - MeteorClient.mc.player.getZ();
                double distance = Math.sqrt(xDiff * xDiff + zDiff * zDiff);
                float targetYaw = (float) (Math.toDegrees(Math.atan2(zDiff, xDiff)) - 90.0);
                float targetPitch = (float) (-Math.toDegrees(Math.atan2(yDiff, distance)));
                isRotating = true;
                if (currentYaw == null) {
                    currentYaw = baseYaw;
                }

                if (currentPitch == null) {
                    currentPitch = basePitch;
                }

                if (currentPitch > 90.0F) {
                    currentPitch = 90.0F;
                    return;
                }

                if (currentPitch < -90.0F) {
                    currentPitch = -90.0F;
                    return;
                }

                float rotSpeed = random ? getRandomOffset(maxSpeed, minSpeed) + new Random().nextFloat() * randomSpeed : maxSpeed;
                if (rotSpeed <= 0.0F) {
                    rotSpeed = 0.0F;
                }

                float[] newRotation = humanizeRotation(currentYaw, currentPitch, targetYaw, targetPitch, rotSpeed, jitterAmount);
                currentYaw = newRotation[0];
                currentPitch = newRotation[1];
                applyHumanLikeRotation(currentYaw, currentPitch, maxOffset, minOffset);
                applyTimingDelay(maxDelay, minDelay);
            } catch (Exception var38) {
                AChatUtils.sendMsgAmrita(Text.of(var38.getMessage()));
            }
        }).start();
    }

    public static void fixMovement(float yaw) {
        float forward = MeteorClient.mc.player.forwardSpeed;
        float strafe = MeteorClient.mc.player.sidewaysSpeed;
        double angle = wrapAngleTo180_double(Math.toDegrees(direction(MeteorClient.mc.player.getYaw(), forward, strafe)));
        if (forward != 0.0F || strafe != 0.0F) {
            float closestForward = 0.0F;
            float closestStrafe = 0.0F;
            float closestDifference = Float.MAX_VALUE;

            for (float predictedForward = -1.0F; predictedForward <= 1.0F; predictedForward++) {
                for (float predictedStrafe = -1.0F; predictedStrafe <= 1.0F; predictedStrafe++) {
                    if (predictedStrafe != 0.0F || predictedForward != 0.0F) {
                        double predictedAngle = wrapAngleTo180_double(Math.toDegrees(direction(yaw, predictedForward, predictedStrafe)));
                        double difference = Math.abs(angle - predictedAngle);
                        if (difference < closestDifference) {
                            closestDifference = (float) difference;
                            closestForward = predictedForward;
                            closestStrafe = predictedStrafe;
                        }
                    }
                }
            }

            MeteorClient.mc.player.forwardSpeed = closestForward;
            MeteorClient.mc.player.sidewaysSpeed = closestStrafe;
        }
    }

    public static double direction(float rotationYaw, double moveForward, double moveStrafing) {
        if (moveForward < 0.0) {
            rotationYaw += 180.0F;
        }

        float forward = 1.0F;
        if (moveForward < 0.0) {
            forward = -0.5F;
        } else if (moveForward > 0.0) {
            forward = 0.5F;
        }

        if (moveStrafing > 0.0) {
            rotationYaw -= 90.0F * forward;
        }

        if (moveStrafing < 0.0) {
            rotationYaw += 90.0F * forward;
        }

        return Math.toRadians(rotationYaw);
    }

    public static double wrapAngleTo180_double(double value) {
        double var2 = value % 360.0;
        if (var2 >= 180.0) {
            var2 -= 360.0;
        }

        if (var2 < -180.0) {
            var2 += 360.0;
        }

        return var2;
    }

    public static void aimAtPos(Vec3d target, float speed, boolean random, float randomSpeed, float baseMaxOffset, float jitterAmount) {
        new Thread(() -> {
            float adjustedMaxOffset = baseMaxOffset * (speed / 10.0F);
            double targetCenterX = target.getX() + getRandomOffset(adjustedMaxOffset);
            double targetCenterY = target.getY() + getRandomOffset(adjustedMaxOffset);
            double targetCenterZ = target.getZ() + getRandomOffset(adjustedMaxOffset);
            double playerEyeY = MeteorClient.mc.player.getY() + MeteorClient.mc.player.getStandingEyeHeight();
            double xDiff = targetCenterX - MeteorClient.mc.player.getX();
            double yDiff = targetCenterY - playerEyeY;
            double zDiff = targetCenterZ - MeteorClient.mc.player.getZ();
            double distance = Math.sqrt(xDiff * xDiff + zDiff * zDiff);
            float targetYaw = (float) (Math.toDegrees(Math.atan2(zDiff, xDiff)) - 90.0);
            float targetPitch = (float) (-Math.toDegrees(Math.atan2(yDiff, distance)));
            isRotating = true;
            if (currentYaw == null) {
                currentYaw = baseYaw;
            }

            if (currentPitch == null) {
                currentPitch = basePitch;
            }

            if (currentPitch > 90.0F) {
                currentPitch = 90.0F;
            } else if (currentPitch < -90.0F) {
                currentPitch = -90.0F;
            } else {
                float rotSpeed = random ? speed + new Random().nextFloat() * randomSpeed : speed;
                if (rotSpeed <= 0.0F) {
                    rotSpeed = 0.0F;
                }

                float[] newRotation = humanizeRotation(currentYaw, currentPitch, targetYaw, targetPitch, rotSpeed, jitterAmount);
                currentYaw = newRotation[0];
                currentPitch = newRotation[1];
                applyHumanLikeRotation(currentYaw, currentPitch);
                applyTimingDelay(50L, 100L);
            }
        }).start();
    }

    private static float[] humanizeRotation(float currentYaw, float currentPitch, float targetYaw, float targetPitch, float speed, float jitterAmount) {
        float newYaw = smoothRotation(currentYaw, addJitter(targetYaw, jitterAmount), speed);
        float newPitch = smoothRotation(currentPitch, addJitter(targetPitch, jitterAmount), speed);
        return new float[]{newYaw, newPitch};
    }

    private static float smoothRotation(float current, float target, float speed) {
        float diff = (target - current + 540.0F) % 360.0F - 180.0F;
        float maxStep = Math.min(Math.abs(diff), speed);
        return (current + maxStep * (diff / Math.abs(diff))) % 360.0F;
    }

    private static float addJitter(float value, float jitterAmount) {
        return value + (new Random().nextFloat() * jitterAmount - jitterAmount / 2.0F);
    }

    private static void applyTimingDelay(long minDelay, long maxDelay) {
        long delay = ThreadLocalRandom.current().nextLong(minDelay, maxDelay);

        try {
            Thread.sleep(delay);
        } catch (InterruptedException var7) {
            Thread.currentThread().interrupt();
        }
    }

    private static float normalizeAngle(float angle, boolean pitchMode) {
        if (pitchMode) {
            float normalized = angle % 360.0F;
            if (normalized <= -180.0F) {
                normalized += 360.0F;
            }

            if (normalized > 180.0F) {
                normalized -= 360.0F;
            }

            return normalized;
        } else {
            return (angle % 360.0F + 360.0F) % 360.0F;
        }
    }

    public static float interpolateAngle(float current, float target, float maxSpeed, float interpolationStep, boolean pitchMode) {
        float normalizedCurrent = normalizeAngle(current, pitchMode);
        float normalizedTarget = normalizeAngle(target, pitchMode);
        float delta = normalizedTarget - normalizedCurrent;
        if (delta > 180.0F) {
            delta -= 360.0F;
        }

        if (delta < -180.0F) {
            delta += 360.0F;
        }

        float clampedDelta = Math.max(-maxSpeed * interpolationStep, Math.min(delta, maxSpeed * interpolationStep));
        float interpolatedAngle = normalizedCurrent + clampedDelta;
        return normalizeAngle(interpolatedAngle, pitchMode);
    }

    private static Pair<Float, Float> getRotations(double posX, double posY, double posZ) {
        ClientPlayerEntity player = MeteorClient.mc.player;
        double x = posX - player.getX();
        double y = posY - (player.getY() + player.getStandingEyeHeight());
        double z = posZ - player.getZ();
        double dist = MathHelper.sqrt((float) (x * x + z * z));
        float yaw = (float) (Math.toDegrees(Math.atan2(z, x)) - 90.0);
        float pitch = (float) (-Math.toDegrees(Math.atan2(y, dist)));
        return new Pair(yaw, pitch);
    }

    public static Vec3d getCameraRotationVec() {
        return getRotationVector(MeteorClient.mc.player.getYaw(), MeteorClient.mc.player.getPitch());
    }

    private static Vec3d getRotationVector(float pitch, float yaw) {
        float f = pitch * (float) (Math.PI / 180.0);
        float g = -yaw * (float) (Math.PI / 180.0);
        float h = MathHelper.cos(g);
        float i = MathHelper.sin(g);
        float j = MathHelper.cos(f);
        float k = MathHelper.sin(f);
        return new Vec3d(i * j, -k, h * j);
    }

    private static float smoothRotate(float current, float target, float speed) {
        float delta = target - current;
        float step = MathHelper.clamp(delta, -speed, speed);
        return current + step;
    }

    private static float wrapDegrees(float angle) {
        float var1 = angle % 360.0F;
        if (var1 >= 180.0F) {
            var1 -= 360.0F;
        }

        if (var1 < -180.0F) {
            var1 += 360.0F;
        }

        return var1;
    }

    private static float getRandomOffset(float maxOffset) {
        return RANDOM.nextFloat() * maxOffset - maxOffset / 2.0F;
    }

    private static float getRandomOffset(float maxOffset, float minOffset) {
        return minOffset + RANDOM.nextFloat() * (maxOffset - minOffset);
    }

    private static void applyHumanLikeRotation(float yaw, float pitch) {
        currentYaw = yaw + getRandomOffset(0.1F);
        currentPitch = pitch + getRandomOffset(0.1F);
    }

    private static void applyHumanLikeRotation(float yaw, float pitch, float maxOffset, float minOffset) {
        currentYaw = yaw + getRandomOffset(maxOffset, minOffset);
        currentPitch = pitch + getRandomOffset(maxOffset, minOffset);
    }

    public static void scaffoldRotation(boolean hypixelMode, Vec3d pos, float speed, boolean useRandom, float randomSpeed, long minDelay, long maxDelay) {
        new Thread(() -> {
            BlockPos playerPos = MeteorClient.mc.player.getBlockPos();
            BlockPos closestBlock = null;
            double closestDistance = Double.MAX_VALUE;

            for (int x = -3; x <= 3; x++) {
                for (int y = -3; y <= 3; y++) {
                    for (int z = -3; z <= 3; z++) {
                        BlockPos currentPos = playerPos.add(x, y, z);
                        if (!MeteorClient.mc.world.isAir(currentPos)) {
                            double distance = pos.squaredDistanceTo(Vec3d.ofCenter(currentPos));
                            if (distance < closestDistance) {
                                closestDistance = distance;
                                closestBlock = currentPos;
                            }
                        }
                    }
                }
            }

            if (closestBlock != null) {
                Vec3d targetPos = Vec3d.ofCenter(closestBlock);
                Vec3d playerEyePos = MeteorClient.mc.player.getPos().add(0.0, MeteorClient.mc.player.getStandingEyeHeight(), 0.0);
                double xDiff = targetPos.x - playerEyePos.x;
                double yDiff = targetPos.y - playerEyePos.y - 2.0;
                double zDiff = targetPos.z - playerEyePos.z;
                double distance = Math.sqrt(xDiff * xDiff + zDiff * zDiff);
                float targetYaw = (float) Math.toDegrees(Math.atan2(zDiff, xDiff)) - 90.0F;
                float targetPitch = -((float) Math.toDegrees(Math.atan2(yDiff, distance)));
                if (targetPitch >= 89.0F) {
                    targetPitch = 89.0F;
                }

                isRotating = true;
                if (currentYaw == null) {
                    currentYaw = baseYaw;
                }

                if (currentPitch == null) {
                    currentPitch = basePitch;
                }

                if (currentPitch > 90.0F) {
                    currentPitch = 90.0F;
                } else if (currentPitch < -90.0F) {
                    currentPitch = -90.0F;
                } else {
                    float rotSpeed = useRandom ? speed + random.nextFloat() * randomSpeed : speed;
                    if (rotSpeed <= 0.0F) {
                        rotSpeed = 0.0F;
                    }

                    float[] newRotations = humanizeRotation(currentYaw, currentPitch, targetYaw, targetPitch, rotSpeed, 0.1F);
                    float newYaw = newRotations[0];
                    float newPitch = newRotations[1];
                    if (hypixelMode) {
                        if (MovementUtil.isDiagonal(37.0F)) {
                            newYaw += 76.0F;
                        } else {
                            newYaw = MovementUtil.getPlayerDirection() - 114.0F;
                        }
                    }

                    currentYaw = newYaw;
                    currentPitch = newPitch;
                    applyHumanLikeRotation(currentYaw, currentPitch);
                    applyTimingDelay(minDelay, maxDelay);

                    try {
                        float yawDiff = Math.abs(currentYaw - targetYaw);
                        float var30 = Math.abs(currentPitch - targetPitch);
                    } catch (Exception var31) {
                    }
                }
            }
        }).start();
    }

    public static void setRotation(float targetYaw, float targetPitch, float speed, boolean setRotationStatus) {
        if (currentYaw == null) {
            currentYaw = baseYaw + 0.001F;
        }

        if (currentPitch == null) {
            currentPitch = basePitch + 0.001F;
        }

        if (setRotationStatus) {
            isRotating = true;
        }

        float rotYaw = smoothRotate(currentYaw, targetYaw, speed);
        float rotPitch = smoothRotate(currentPitch, targetPitch, speed);
        applyHumanLikeRotation(rotYaw, rotPitch);
    }

    public static void setRotation(float targetYaw, float targetPitch, float speed) {
        if (currentYaw == null) {
            currentYaw = baseYaw + 0.001F;
        }

        if (currentPitch == null) {
            currentPitch = basePitch + 0.001F;
        }

        float rotYaw = smoothRotate(currentYaw, targetYaw, speed);
        float rotPitch = smoothRotate(currentPitch, targetPitch, speed);
        applyHumanLikeRotation(rotYaw, rotPitch);
    }

    public void init() {
        MeteorClient.EVENT_BUS.subscribe(this);
    }

    @EventHandler
    public void onMotion(MotionEvent event) {
        if (MeteorClient.mc.player != null) {
            long currentTime = System.nanoTime();
            if (lastTime == 0L) {
                lastTime = currentTime;
            }

            double deltaTime = (currentTime - lastTime) / 1.0E9;
            lastTime = currentTime;
            if (baseYaw >= 180.0F) {
                baseYaw = -180.0F;
            } else if (baseYaw <= -180.0F) {
                baseYaw = 180.0F;
            }

            if (currentYaw != null && currentPitch != null) {
                interpolatedYaw = interpolateAngle(event.yaw, currentYaw, 180.0F, (float) (20.0 * deltaTime), false);
                interpolatedPitch = interpolateAngle(event.pitch, currentPitch, 180.0F, (float) (20.0 * deltaTime), true);
                if (Float.isNaN(interpolatedYaw) || Float.isNaN(interpolatedPitch) || Float.isInfinite(interpolatedYaw) || Float.isInfinite(interpolatedPitch)) {
                    reset();
                    return;
                }

                event.yaw = interpolatedYaw;
                event.pitch = interpolatedPitch;
                MeteorClient.mc.player.bodyYaw = MeteorClient.mc.player.getYaw();
                pitchTick++;
            }
        }
    }
}
