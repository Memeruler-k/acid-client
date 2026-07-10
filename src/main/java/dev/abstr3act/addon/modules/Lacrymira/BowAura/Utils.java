package dev.abstr3act.addon.modules.Lacrymira.BowAura;

import dev.abstr3act.addon.utils.math.ExplosionUtility;
import meteordevelopment.meteorclient.MeteorClient;
import net.minecraft.block.BlockState;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockView;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class Utils {
    public static Vec3d getVisiblePoint(Vec3d eyePos, Box targetBox) {
        Vec3d closestCorner = null;
        double minDistance = Double.MAX_VALUE;

        for (Vec3d corner : getBoxCorners(targetBox)) {
            if (ExplosionUtility.raycast(eyePos, corner, false) == Type.MISS) {
                double distance = eyePos.squaredDistanceTo(corner);
                if (distance < minDistance) {
                    minDistance = distance;
                    closestCorner = corner;
                }
            }
        }

        return closestCorner != null ? closestCorner : targetBox.getCenter();
    }

    @NotNull
    public static float[] calculateAngle(@NotNull Vec3d from, @NotNull Vec3d to) {
        double difX = to.x - from.x;
        double difY = (to.y - from.y) * -1.0;
        double difZ = to.z - from.z;
        double dist = MathHelper.sqrt((float) (difX * difX + difZ * difZ));
        float yD = (float) MathHelper.wrapDegrees(Math.toDegrees(Math.atan2(difZ, difX)) - 90.0);
        float pD = (float) MathHelper.clamp(MathHelper.wrapDegrees(Math.toDegrees(Math.atan2(difY, dist))), -90.0, 90.0);
        return new float[]{yD, pD};
    }

    public static boolean canSee(Vec3d playerEyes, Box entityBox) {
        if (MeteorClient.mc.player != null && MeteorClient.mc.world != null) {
            for (Vec3d corner : getBoxCorners(entityBox)) {
                if (ExplosionUtility.raycast(playerEyes, corner, false) == Type.MISS) {
                    return true;
                }
            }

            return false;
        } else {
            return false;
        }
    }

    public static Vec3d getVelocity(Vec3d fromVec, Vec3d toVec, double speedPerTick) {
        Vec3d subtract = toVec.subtract(fromVec);
        double dis = Math.sqrt(subtract.x * subtract.x + subtract.y * subtract.y + subtract.z * subtract.z);
        double step = speedPerTick / dis;
        return new Vec3d(subtract.x * step, subtract.y * step, subtract.z * step);
    }

    public static Type raycast(Vec3d start, Vec3d end, boolean ignoreTerrain) {
        return (Type) BlockView.raycast(start, end, null, (innerContext, blockPos) -> {
            BlockState blockState = MeteorClient.mc.world.getBlockState(blockPos);
            if (blockState.getBlock().getBlastResistance() < 600.0F && ignoreTerrain) {
                return null;
            } else {
                BlockHitResult hitResult = blockState.getCollisionShape(MeteorClient.mc.world, blockPos).raycast(start, end, blockPos);
                return hitResult == null ? null : hitResult.getType();
            }
        }, innerContext -> Type.MISS);
    }

    public static Vec3d getStraightVec(Vec3d fromVec, Vec3d toVec, double range) {
        Vec3d vec = getVelocity(fromVec, toVec, range);
        return fromVec.add(vec);
    }

    private static List<Vec3d> getBoxCorners(Box box) {
        List<Vec3d> corners = new ArrayList<>();
        corners.add(new Vec3d(box.maxX, box.minY, box.minZ));
        corners.add(new Vec3d(box.maxX, box.minY, box.maxZ));
        corners.add(new Vec3d(box.maxX, box.maxY, box.minZ));
        corners.add(new Vec3d(box.maxX, box.maxY, box.maxZ));
        corners.add(new Vec3d(box.minX, box.minY, box.minZ));
        corners.add(new Vec3d(box.minX, box.minY, box.maxZ));
        corners.add(new Vec3d(box.minX, box.maxY, box.minZ));
        corners.add(new Vec3d(box.minX, box.maxY, box.maxZ));
        return corners;
    }

    public static ArrayList<Vec3d> getVecSphere(float range, Vec3d pos, double precise) {
        ArrayList<Vec3d> list = new ArrayList<>();
        double x = pos.getX() - range;

        while (x <= pos.getX() + range) {
            for (double y = pos.getY() - range; y <= pos.getY() + range; y += precise) {
                for (double z = pos.getZ() - range; z <= pos.getZ() + range; z += precise) {
                    Vec3d curPos = new Vec3d(x, y, z);
                    if (pos.distanceTo(curPos) <= range && !list.contains(curPos)) {
                        list.add(curPos);
                    }
                }
            }

            x += precise;
        }

        return list;
    }
}
