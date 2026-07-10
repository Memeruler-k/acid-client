package dev.abstr3act.addon.utils.compassion;

import net.minecraft.block.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.PriorityQueue;

public final class AStarCustomPathFinder {
    private static final Vec3[] flatCardinalDirections = new Vec3[]{
        new Vec3(1.0, 0.0, 0.0), new Vec3(-1.0, 0.0, 0.0), new Vec3(0.0, 0.0, 1.0), new Vec3(0.0, 0.0, -1.0)
    };
    static MinecraftClient mc = MinecraftClient.getInstance();
    private final Vec3 startVec3;
    private final Vec3 endVec3;
    private final ArrayList<Hub> hubs = new ArrayList<>();
    private final PriorityQueue<Hub> hubsToWork = new PriorityQueue<>(new CompareHub());
    private ArrayList<Vec3> path = new ArrayList<>();

    public AStarCustomPathFinder(Vec3 startVec3, Vec3 endVec3) {
        this.startVec3 = startVec3.addVector(0.0, 0.0, 0.0).floor();
        this.endVec3 = endVec3.addVector(0.0, 0.0, 0.0).floor();
    }

    public static boolean checkPositionValidity(Vec3 loc, boolean checkGround) {
        return checkPositionValidity((int) loc.getX(), (int) loc.getY(), (int) loc.getZ(), checkGround);
    }

    public static boolean checkPositionValidity(int x, int y, int z, boolean checkGround) {
        BlockPos block1 = new BlockPos(x, y, z);
        BlockPos block2 = new BlockPos(x, y + 1, z);
        BlockPos block3 = new BlockPos(x, y - 1, z);
        return !isBlockSolid(block1) && !isBlockSolid(block2) && (isBlockSolid(block3) || !checkGround) && isSafeToWalkOn(block3);
    }

    private static boolean isBlockSolid(BlockPos block) {
        return mc.world.getBlockState(block).shapeCache != null && mc.world.getBlockState(block).shapeCache.isFullCube
            || mc.world.getBlockState(block).getBlock() instanceof SlabBlock
            || mc.world.getBlockState(block).getBlock() instanceof StairsBlock
            || mc.world.getBlockState(block).getBlock() instanceof CactusBlock
            || mc.world.getBlockState(block).getBlock() instanceof ChestBlock
            || mc.world.getBlockState(block).getBlock() instanceof EnderChestBlock
            || mc.world.getBlockState(block).getBlock() instanceof SkullBlock
            || mc.world.getBlockState(block).getBlock() instanceof PaneBlock
            || mc.world.getBlockState(block).getBlock() instanceof FenceBlock
            || mc.world.getBlockState(block).getBlock() instanceof WallBlock
            || mc.world.getBlockState(block).getBlock() instanceof StainedGlassBlock
            || mc.world.getBlockState(block).getBlock() instanceof PistonBlock
            || mc.world.getBlockState(block).getBlock() instanceof PistonExtensionBlock
            || mc.world.getBlockState(block).getBlock() instanceof PistonHeadBlock
            || mc.world.getBlockState(block).getBlock() instanceof StainedGlassBlock
            || mc.world.getBlockState(block).getBlock() instanceof TrapdoorBlock;
    }

    private static boolean isSafeToWalkOn(BlockPos block) {
        return !(mc.world.getBlockState(block).getBlock() instanceof FenceBlock) && !(mc.world.getBlockState(block).getBlock() instanceof WallBlock);
    }

    public ArrayList<Vec3> getPath() {
        return this.path;
    }

    public void compute(int loops) {
        this.path.clear();
        this.hubsToWork.clear();
        ArrayList<Vec3> initPath = new ArrayList<>();
        initPath.add(this.startVec3);
        this.hubsToWork.add(new Hub(this.startVec3, null, initPath, this.startVec3.squareDistanceTo(this.endVec3), 0.0, 0.0));

        for (int exploredNodes = 0; !this.hubsToWork.isEmpty() && exploredNodes < loops; exploredNodes++) {
            Hub currentHub = this.hubsToWork.poll();
            this.hubs.add(currentHub);
            if (currentHub.getLoc().squareDistanceTo(this.endVec3) <= 0.0) {
                this.path = currentHub.getPath();
                return;
            }

            for (Vec3 direction : flatCardinalDirections) {
                Vec3 neighbor = currentHub.getLoc().add(direction).floor();
                if (checkPositionValidity(neighbor, false) && this.addHub(currentHub, neighbor, 0.0)) {
                    return;
                }
            }

            Vec3 above = currentHub.getLoc().addVector(0.0, 1.0, 0.0).floor();
            Vec3 below = currentHub.getLoc().addVector(0.0, -1.0, 0.0).floor();
            if (checkPositionValidity(above, false) && this.addHub(currentHub, above, 0.0)) {
                return;
            }

            if (checkPositionValidity(below, false) && this.addHub(currentHub, below, 0.0)) {
                return;
            }
        }

        this.path.clear();
    }

    public boolean addHub(Hub parent, Vec3 loc, double cost) {
        Hub existingHub = this.isHubExisting(loc);
        double totalCost = cost + (parent != null ? parent.getTotalCost() : 0.0);
        if (existingHub == null) {
            if (loc.squareDistanceTo(this.endVec3) < 1.0) {
                this.path.clear();
                this.path = parent.getPath();
                this.path.add(loc);
                return true;
            }

            ArrayList<Vec3> newPath = new ArrayList<>(parent.getPath());
            newPath.add(loc);
            this.hubsToWork.add(new Hub(loc, parent, newPath, loc.squareDistanceTo(this.endVec3), cost, totalCost));
        } else if (existingHub.getCost() > cost) {
            existingHub.updateHub(parent, loc, cost, totalCost);
        }

        return false;
    }

    public Hub isHubExisting(Vec3 loc) {
        for (Hub hub : this.hubs) {
            if (hub.matchesLocation(loc)) {
                return hub;
            }
        }

        for (Hub hubx : this.hubsToWork) {
            if (hubx.matchesLocation(loc)) {
                return hubx;
            }
        }

        return null;
    }

    public static class CompareHub implements Comparator<Hub> {
        public int compare(Hub o1, Hub o2) {
            return Double.compare(o1.getTotalCost() + o1.squareDistanceToFromTarget, o2.getTotalCost() + o2.squareDistanceToFromTarget);
        }
    }

    private static class Hub {
        private Vec3 loc;
        private Hub parent;
        private ArrayList<Vec3> path;
        private double squareDistanceToFromTarget;
        private double cost;
        private double totalCost;

        public Hub(Vec3 loc, Hub parent, ArrayList<Vec3> path, double distanceToTarget, double cost, double totalCost) {
            this.loc = loc;
            this.parent = parent;
            this.path = path;
            this.squareDistanceToFromTarget = distanceToTarget;
            this.cost = cost;
            this.totalCost = totalCost;
        }

        public Vec3 getLoc() {
            return this.loc;
        }

        public Hub getParent() {
            return this.parent;
        }

        public double getTotalCost() {
            return this.totalCost;
        }

        public double getCost() {
            return this.cost;
        }

        public ArrayList<Vec3> getPath() {
            return this.path;
        }

        public boolean matchesLocation(Vec3 loc) {
            return this.loc.getX() == loc.getX() && this.loc.getY() == loc.getY() && this.loc.getZ() == loc.getZ();
        }

        public void updateHub(Hub parent, Vec3 loc, double cost, double totalCost) {
            this.parent = parent;
            this.loc = loc;
            this.cost = cost;
            this.totalCost = totalCost;
            this.path = new ArrayList<>(parent.getPath());
            this.path.add(loc);
        }
    }
}
