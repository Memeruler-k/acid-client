package dev.abstr3act.addon.utils.seraphim.movement;

import meteordevelopment.meteorclient.MeteorClient;
import net.minecraft.client.input.Input;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;

public class MovementUtils {
    public static double getSpeed() {
        return Math.sqrt(
            MeteorClient.mc.player.getVelocity().x * MeteorClient.mc.player.getVelocity().x
                + MeteorClient.mc.player.getVelocity().z * MeteorClient.mc.player.getVelocity().z
        );
    }

    public static double getSpeed(PlayerEntity player) {
        return Math.sqrt(player.getVelocity().x * player.getVelocity().x + player.getVelocity().z * player.getVelocity().z);
    }

    public static boolean isMoving(Entity player) {
        return player.getZ() - player.prevZ == 0.0 && player.getX() - player.prevX == 0.0;
    }

    public static void strafe(float yaw, double speed, double strength) {
        MeteorClient.mc.player.setVelocity(strafe(MeteorClient.mc.player.getVelocity(), yaw, speed, strength));
    }

    public static void strafe(double speed) {
        MeteorClient.mc.player.setVelocity(strafe(MeteorClient.mc.player.getVelocity(), getDirectionYaw(), speed, 1.0));
    }

    public static double getSqrtSpeed(Vec3d vec) {
        return Math.sqrt(vec.x * vec.x + vec.z * vec.z);
    }

    public static Vec3d strafe(Vec3d velocity, float yaw, double speed, double strength) {
        double prevX = velocity.x * (1.0 - strength);
        double prevZ = velocity.z * (1.0 - strength);
        double useSpeed = speed * strength;
        double angle = Math.toRadians(yaw);
        double newX = -Math.sin(angle) * useSpeed + prevX;
        double newZ = Math.cos(angle) * useSpeed + prevZ;
        return new Vec3d(newX, velocity.y, newZ);
    }

    public static float getDirectionYaw() {
        return getMovementDirectionOfInput(MeteorClient.mc.player.getYaw(), new DirectionalInput(MeteorClient.mc.player.input));
    }

    private static float getMovementDirectionOfInput(float facingYaw, DirectionalInput input) {
        float actualYaw = facingYaw;
        float forward = 1.0F;
        if (input.backwards) {
            actualYaw = facingYaw + 180.0F;
            forward = -0.5F;
        } else if (input.forwards) {
            forward = 0.5F;
        }

        if (input.left) {
            actualYaw -= 90.0F * forward;
        }

        if (input.right) {
            actualYaw += 90.0F * forward;
        }

        return actualYaw;
    }

    private static boolean isMoving(ClientPlayerEntity player) {
        return player.input.pressingForward || player.input.pressingBack || player.input.pressingLeft || player.input.pressingRight;
    }

    public static double direction() {
        float yaw = MeteorClient.mc.player.getYaw();
        if (MeteorClient.mc.player.input.movementForward < 0.0F) {
            yaw += 180.0F;
        }

        float forward = 1.0F;
        if (MeteorClient.mc.player.input.movementForward < 0.0F) {
            forward = (float) (forward - 0.5);
        } else if (MeteorClient.mc.player.input.movementForward > 0.0F) {
            forward = (float) (forward + 0.5);
        }

        if (MeteorClient.mc.player.input.movementSideways > 0.0F) {
            yaw -= 90.0F * forward;
        }

        if (MeteorClient.mc.player.input.movementSideways < 0.0F) {
            yaw += 90.0F * forward;
        }

        return Math.toRadians(yaw);
    }

    private static class DirectionalInput {
        boolean forwards;
        boolean backwards;
        boolean left;
        boolean right;

        public DirectionalInput(Input input) {
            this.forwards = input.pressingForward;
            this.backwards = input.pressingBack;
            this.left = input.pressingLeft;
            this.right = input.pressingRight;
        }
    }
}
