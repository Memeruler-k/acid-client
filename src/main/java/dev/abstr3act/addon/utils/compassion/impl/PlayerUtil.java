package dev.abstr3act.addon.utils.compassion.impl;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;

public final class PlayerUtil {
    private PlayerUtil() {
    }

    @NotNull
    public static PlayerUtil.PlayerState getStateFromPlayer(@NotNull PlayerEntity player) {
        if (player == null) {
            throw new IllegalArgumentException("player cannot be null");
        } else {
            return !player.isSneaking() && !player.isSwimming()
                ? (player.isOnGround() ? PlayerState.Sneak : PlayerState.Normal)
                : PlayerState.Creep;
        }
    }

    public static enum PlayerState {
        Normal,
        Sneak,
        Creep;

        @NotNull
        public Box getBBox(@NotNull Vec3d vec) {
            if (vec == null) {
                throw new IllegalArgumentException("vec cannot be null");
            } else {
                return this.getBBox(vec.x, vec.y, vec.z);
            }
        }

        @NotNull
        public Box getBBox(double x, double y, double z) {
            switch (this) {
                case Normal:
                    return new Box(x + 0.3, y + 1.8, z + 0.3, x - 0.3, y, z - 0.3);
                case Sneak:
                    return new Box(x + 0.3, y + 1.5, z + 0.3, x - 0.3, y, z - 0.3);
                case Creep:
                    return new Box(x + 0.3, y + 0.6, z + 0.3, x - 0.3, y, z - 0.3);
                default:
                    throw new IllegalStateException("Unexpected value: " + this);
            }
        }

        public double getEyeY() {
            switch (this) {
                case Normal:
                    return 1.62;
                case Sneak:
                    return 1.27;
                case Creep:
                    return 0.4;
                default:
                    throw new IllegalStateException("Unexpected value: " + this);
            }
        }
    }
}
