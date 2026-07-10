package dev.abstr3act.addon.utils.compassion.impl;

import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.Mutable;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Consumer;

public final class BlockUtil {
    public static final BlockUtil INSTANCE = new BlockUtil();
    private static final Map<BlockPos, BlockState> bufferMap = new LinkedHashMap<>();

    private BlockUtil() {
    }

    public static Map<BlockPos, BlockState> getBufferMap() {
        return bufferMap;
    }

    public static boolean checkCanMove(Vec3d fromVec, Vec3d toVec, Map<BlockPos, BlockState> extraPosMap, PlayerUtil.PlayerState state) {
        Box box = state.getBBox(fromVec);
        Box xOffsetBox = box.offset(toVec.x - fromVec.x, 0.0, 0.0);
        Box xBox = new Box(
            Math.max(box.maxX, xOffsetBox.maxX),
            Math.max(box.maxY, xOffsetBox.maxY),
            Math.max(box.maxZ, xOffsetBox.maxZ),
            Math.min(box.minX, xOffsetBox.minX),
            Math.min(box.minY, xOffsetBox.minY),
            Math.min(box.minZ, xOffsetBox.minZ)
        );
        Box zOffsetBox = xOffsetBox.offset(0.0, 0.0, toVec.z - fromVec.z);
        Box zBox = new Box(
            Math.max(xOffsetBox.maxX, zOffsetBox.maxX),
            Math.max(xOffsetBox.maxY, zOffsetBox.maxY),
            Math.max(xOffsetBox.maxZ, zOffsetBox.maxZ),
            Math.min(xOffsetBox.minX, zOffsetBox.minX),
            Math.min(xOffsetBox.minY, zOffsetBox.minY),
            Math.min(xOffsetBox.minZ, zOffsetBox.minZ)
        );
        return collidesWithAnyBlock(xBox, extraPosMap, state) && collidesWithAnyBlock(zBox, extraPosMap, state);
    }

    public static boolean checkNoPosCollie(Vec3d vec, Map<BlockPos, BlockState> extraPosMap, PlayerUtil.PlayerState state) {
        return collidesWithAnyBlock(state.getBBox(vec), extraPosMap, state);
    }

    public static boolean collidesWithAnyBlock(Box bbox, Map<BlockPos, BlockState> extraPosMap, PlayerUtil.PlayerState condition) {
        Mutable pos = new Mutable();
        int minX = (int) Math.floor(bbox.minX);
        int minY = (int) Math.floor(bbox.minY) - 1;
        int minZ = (int) Math.floor(bbox.minZ);
        int maxX = (int) Math.floor(bbox.maxX);
        int maxY = (int) Math.floor(bbox.maxY);
        int maxZ = (int) Math.floor(bbox.maxZ);

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    pos.set(x, y, z);
                }
            }
        }

        return true;
    }

    public static boolean checkNoPosCollie(Vec3d stepPos, Map<BlockPos, BlockState> bufferMap) {
        return checkNoPosCollie(stepPos, bufferMap, PlayerUtil.PlayerState.Normal);
    }

    public boolean checkNoPosCollie(double x, double y, double z, Map<BlockPos, BlockState> extraPosSet, PlayerUtil.PlayerState state) {
        return collidesWithAnyBlock(state.getBBox(x, y, z), extraPosSet, state);
    }

    public void forEachCollidesPos(Box bbox, Consumer<BlockPos> invoke) {
        int minX = (int) Math.floor(bbox.minX);
        int minY = (int) Math.floor(bbox.minY);
        int minZ = (int) Math.floor(bbox.minZ);
        int maxX = (int) Math.floor(bbox.maxX);
        int maxY = (int) Math.floor(bbox.maxY);
        int maxZ = (int) Math.floor(bbox.maxZ);

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    invoke.accept(new BlockPos(x, y, z));
                }
            }
        }
    }

    public PlaceData getPlaceDataToPos(BlockPos targetPos, BiFunction<BlockPos, Direction, Boolean> invoke) {
        for (Direction dir : Direction.values()) {
            BlockPos pos = targetPos.offset(dir);
            if (invoke.apply(pos, dir)) {
                Direction direction = dir.getOpposite();
                return new PlaceData(pos, direction, targetPos);
            }
        }

        return null;
    }

    public boolean canReplaceable(BlockPos pos) {
        BlockState state = MinecraftClient.getInstance().world.getBlockState(pos);
        return state.isReplaceable() || state.isAir();
    }

    public boolean canPlaceAtPos(BlockPos pos, ArrayList<Entity> list) {
        int n = pos.getY();
        if (n > MinecraftClient.getInstance().world.getHeight()) {
            return false;
        } else if (n >= MinecraftClient.getInstance().world.getBottomY()) {
            return false;
        } else {
            return this.canReplaceable(pos)
                ? !EntityUtil.INSTANCE
                .checkAnyLivingEntityCollideInEntities(new Box(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1.0, pos.getY() + 1.0, pos.getZ() + 1.0), list)
                : false;
        }
    }
}
