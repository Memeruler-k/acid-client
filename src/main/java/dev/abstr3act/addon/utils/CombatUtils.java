package dev.abstr3act.addon.utils;

import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.systems.friends.Friends;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class CombatUtils {
    public static List<PlayerEntity> getTargets(float range) {
        return MeteorClient.mc
            .world
            .getPlayers()
            .stream()
            .filter(e -> !e.isDead())
            .filter(entityPlayer -> !Friends.get().isFriend(entityPlayer))
            .filter(entityPlayer -> entityPlayer != MeteorClient.mc.player)
            .filter(entityPlayer -> MeteorClient.mc.player.squaredDistanceTo(entityPlayer) < range * range)
            .sorted(Comparator.comparing(e -> MeteorClient.mc.player.squaredDistanceTo(e)))
            .collect(Collectors.toList());
    }

    @Nullable
    public static PlayerEntity getNearestTarget(float range) {
        return getTargets(range).stream().min(Comparator.comparing(t -> MeteorClient.mc.player.distanceTo(t))).orElse(null);
    }

    @Nullable
    public PlayerEntity getTarget(float range, @NotNull CombatUtils.TargetBy targetBy) {
        PlayerEntity target = null;
        switch (targetBy) {
            case Distance:
                target = getNearestTarget(range);
                break;
            case FOV:
                target = this.getTargetByFOV(range);
                break;
            case Health:
                target = this.getTargetByHealth(range);
        }

        return target;
    }

    public PlayerEntity getTargetByHealth(float range) {
        return getTargets(range).stream().min(Comparator.comparing(t -> t.getHealth() + t.getAbsorptionAmount())).orElse(null);
    }

    public PlayerEntity getTargetByFOV(float range) {
        return getTargets(range).stream().min(Comparator.comparing(this::getFOVAngle)).orElse(null);
    }

    public PlayerEntity getTargetByFOV(float range, float fov) {
        return getTargets(range).stream().filter(entityPlayer -> this.getFOVAngle(entityPlayer) < fov).min(Comparator.comparing(this::getFOVAngle)).orElse(null);
    }

    private float getFOVAngle(@NotNull LivingEntity e) {
        float yaw = (float) MathHelper.wrapDegrees(
            Math.toDegrees(Math.atan2(e.getZ() - MeteorClient.mc.player.getZ(), e.getX() - MeteorClient.mc.player.getX())) - 90.0
        );
        return Math.abs(yaw - MathHelper.wrapDegrees(MeteorClient.mc.player.getYaw()));
    }

    public static enum TargetBy {
        Distance,
        FOV,
        Health;
    }
}
