package dev.abstr3act.addon.utils.pathfinder;

import dev.abstr3act.addon.Global;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;

public class LinearPathFinder {
    public static ArrayList<Vec3d> getPaths(BlockPos startPos, BlockPos endPos, int blockPerStep, int maxSteps) {
        ArrayList<Vec3d> path = new ArrayList<>();
        if (Global.mc.player != null && maxSteps > 0) {
            double dx = endPos.getX() - startPos.getX();
            double dy = endPos.getY() - startPos.getY();
            double dz = endPos.getZ() - startPos.getZ();
            double totalDistance = Math.sqrt(dx * dx + dy * dy + dz * dz);
            int totalSteps = Math.min((int) (totalDistance / blockPerStep), maxSteps);

            for (int i = 1; i <= totalSteps; i++) {
                double t = i / totalSteps;
                double px = startPos.getX() + t * dx;
                double py = startPos.getY() + t * dy;
                double pz = startPos.getZ() + t * dz;
                path.add(new Vec3d(px, py, pz));
            }

            return path;
        } else {
            return path;
        }
    }
}
