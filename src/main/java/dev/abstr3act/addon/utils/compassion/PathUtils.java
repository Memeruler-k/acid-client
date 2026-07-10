package dev.abstr3act.addon.utils.compassion;

import meteordevelopment.meteorclient.MeteorClient;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.PlantBlock;
import net.minecraft.block.WallSignBlock;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;

public final class PathUtils {
    private static boolean canPassThrough(BlockPos pos) {
        Block block = MeteorClient.mc.world.getBlockState(new BlockPos(pos.getX(), pos.getY(), pos.getZ())).getBlock();
        return block == Blocks.AIR
            || block instanceof PlantBlock
            || block == Blocks.VINE
            || block == Blocks.LADDER
            || block == Blocks.WATER
            || block == Blocks.WATER_CAULDRON
            || block instanceof WallSignBlock;
    }

    public static ArrayList<Vec3> computePath(LivingEntity fromEntity, LivingEntity toEntity, int loops, double tp) {
        return computePath(
            new Vec3(fromEntity.getX(), fromEntity.getY(), fromEntity.getZ()), new Vec3(toEntity.getX(), toEntity.getY(), toEntity.getZ()), loops, tp
        );
    }

    public static ArrayList<Vec3> computePath(Vec3d vec3d, int loops, double tp) {
        return computePath(
            new Vec3(MeteorClient.mc.player.getX(), MeteorClient.mc.player.getY(), MeteorClient.mc.player.getZ()), new Vec3(vec3d.x, vec3d.y, vec3d.z), loops, tp
        );
    }

    public static ArrayList<Vec3> computePath(Vec3 topFrom, Vec3 to, int loops, double tp) {
        if (!canPassThrough(new BlockPosX(topFrom.mc()))) {
            topFrom = topFrom.addVector(0.0, 1.0, 0.0);
        }

        AStarCustomPathFinder pathfinder = new AStarCustomPathFinder(topFrom, to);
        pathfinder.compute(loops);
        int i = 0;
        Vec3 lastLoc = null;
        Vec3 lastDashLoc = null;
        ArrayList<Vec3> path = new ArrayList<>();
        ArrayList<Vec3> pathFinderPath = pathfinder.getPath();

        for (Vec3 pathElm : pathFinderPath) {
            if (i != 0 && i != pathFinderPath.size() - 1) {
                boolean canContinue = true;
                if (pathElm.squareDistanceTo(lastDashLoc) > tp) {
                    canContinue = false;
                } else {
                    double smallX = Math.min(lastDashLoc.getX(), pathElm.getX());
                    double smallY = Math.min(lastDashLoc.getY(), pathElm.getY());
                    double smallZ = Math.min(lastDashLoc.getZ(), pathElm.getZ());
                    double bigX = Math.max(lastDashLoc.getX(), pathElm.getX());
                    double bigY = Math.max(lastDashLoc.getY(), pathElm.getY());
                    double bigZ = Math.max(lastDashLoc.getZ(), pathElm.getZ());

                    label55:
                    for (int x = (int) smallX; x <= bigX; x++) {
                        for (int y = (int) smallY; y <= bigY; y++) {
                            for (int z = (int) smallZ; z <= bigZ; z++) {
                                if (!AStarCustomPathFinder.checkPositionValidity(x, y, z, false)) {
                                    canContinue = false;
                                    break label55;
                                }
                            }
                        }
                    }
                }

                if (!canContinue) {
                    path.add(lastLoc.addVector(0.5, 0.0, 0.5));
                    lastDashLoc = lastLoc;
                }
            } else {
                if (lastLoc != null) {
                    path.add(lastLoc.addVector(0.5, 0.0, 0.5));
                }

                path.add(pathElm.addVector(0.5, 0.0, 0.5));
                lastDashLoc = pathElm;
            }

            lastLoc = pathElm;
            i++;
        }

        return path;
    }
}
