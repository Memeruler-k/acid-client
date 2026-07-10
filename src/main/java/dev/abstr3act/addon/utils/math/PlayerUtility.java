package dev.abstr3act.addon.utils.math;

import meteordevelopment.meteorclient.MeteorClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public final class PlayerUtility {
    public static boolean isInHell() {
        return MeteorClient.mc.world == null ? false : Objects.equals(MeteorClient.mc.world.getRegistryKey().getValue().getPath(), "the_nether");
    }

    public static boolean isInEnd() {
        return MeteorClient.mc.world == null ? false : Objects.equals(MeteorClient.mc.world.getRegistryKey().getValue().getPath(), "the_end");
    }

    public static boolean isInOver() {
        return MeteorClient.mc.world == null ? false : Objects.equals(MeteorClient.mc.world.getRegistryKey().getValue().getPath(), "overworld");
    }

    public static boolean isEating() {
        return MeteorClient.mc.player == null
            ? false
            : (
            MeteorClient.mc.player.getMainHandStack().getComponents().contains(DataComponentTypes.FOOD)
                || MeteorClient.mc.player.getOffHandStack().getComponents().contains(DataComponentTypes.FOOD)
        )
            && MeteorClient.mc.player.isUsingItem();
    }

    public static boolean isMining() {
        return MeteorClient.mc.interactionManager == null ? false : MeteorClient.mc.interactionManager.isBreakingBlock();
    }

    public static float squaredDistanceFromEyes(@NotNull Vec3d targetPos) {
        if (MeteorClient.mc.player == null) {
            return 0.0F;
        } else {
            double dx = targetPos.x - MeteorClient.mc.player.getX();
            double dy = targetPos.y - (MeteorClient.mc.player.getY() + MeteorClient.mc.player.getEyeHeight(MeteorClient.mc.player.getPose()));
            double dz = targetPos.z - MeteorClient.mc.player.getZ();
            return (float) (dx * dx + dy * dy + dz * dz);
        }
    }

    public static float squaredDistance2d(@NotNull Vec2f point) {
        if (MeteorClient.mc.player == null) {
            return 0.0F;
        } else {
            double d = MeteorClient.mc.player.getX() - point.x;
            double f = MeteorClient.mc.player.getZ() - point.y;
            return (float) (d * d + f * f);
        }
    }

    public static ClientPlayerEntity getPlayer() {
        return MeteorClient.mc.player;
    }

    public static float calculatePercentage(@NotNull ItemStack stack) {
        float durability = stack.getMaxDamage() - stack.getDamage();
        return durability / stack.getMaxDamage() * 100.0F;
    }

    public static float fixAngle(float angle) {
        return Math.round(angle / (float) (getGCD() * 0.15)) * (float) (getGCD() * 0.15);
    }

    public static float getGCD() {
        double sensitivity = MeteorClient.mc.options.getMouseSensitivity().getValue();
        double value = sensitivity * 0.6 + 0.2;
        double result = Math.pow(value, 3.0) * 8.0;
        return (float) result;
    }

    public static float squaredDistance2d(double x, double z) {
        if (MeteorClient.mc.player == null) {
            return 0.0F;
        } else {
            double d = MeteorClient.mc.player.getX() - x;
            double f = MeteorClient.mc.player.getZ() - z;
            return (float) (d * d + f * f);
        }
    }

    public static float getSquaredDistance2D(Vec3d vec) {
        double d0 = MeteorClient.mc.player.getX() - vec.getX();
        double d2 = MeteorClient.mc.player.getZ() - vec.getZ();
        return (float) (d0 * d0 + d2 * d2);
    }

    public static boolean canSee(Vec3d pos) {
        Vec3d vec3d = new Vec3d(MeteorClient.mc.player.getX(), MeteorClient.mc.player.getEyeY(), MeteorClient.mc.player.getZ());
        return pos.distanceTo(vec3d) > 128.0 ? false : ExplosionUtility.raycast(vec3d, pos, false) == Type.MISS;
    }

    public static boolean isFalling() {
        return MeteorClient.mc.player == null
            ? false
            : !MeteorClient.mc.player.isOnGround() && !MeteorClient.mc.player.isCreative() && MeteorClient.mc.player.getVelocity().y < 0.0;
    }
}
