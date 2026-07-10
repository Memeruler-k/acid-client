package dev.abstr3act.addon.utils.compassion.impl;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.NotNull;

public final class PlaceData {
    @NotNull
    private final BlockPos placePos;
    @NotNull
    private final Direction placeFacing;
    @NotNull
    private final BlockPos targetPos;

    public PlaceData(@NotNull BlockPos placePos, @NotNull Direction placeFacing, @NotNull BlockPos targetPos) {
        if (placePos == null) {
            throw new IllegalArgumentException("placePos cannot be null");
        } else if (placeFacing == null) {
            throw new IllegalArgumentException("placeFacing cannot be null");
        } else if (targetPos == null) {
            throw new IllegalArgumentException("targetPos cannot be null");
        } else {
            this.placePos = placePos;
            this.placeFacing = placeFacing;
            this.targetPos = targetPos;
        }
    }

    @NotNull
    public BlockPos getPlacePos() {
        return this.placePos;
    }

    @NotNull
    public Direction getPlaceFacing() {
        return this.placeFacing;
    }

    @NotNull
    public BlockPos getTargetPos() {
        return this.targetPos;
    }
}
