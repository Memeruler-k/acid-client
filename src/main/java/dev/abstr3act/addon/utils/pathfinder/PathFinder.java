package dev.abstr3act.addon.utils.pathfinder;

import dev.abstr3act.addon.Global;
import net.minecraft.block.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Objects;

public class PathFinder {
    private static final Vec3d[] directions = new Vec3d[]{
        new Vec3d(1.0, 0.0, 0.0),
        new Vec3d(-1.0, 0.0, 0.0),
        new Vec3d(0.0, 0.0, 1.0),
        new Vec3d(0.0, 0.0, -1.0),
        new Vec3d(0.0, 1.0, 0.0),
        new Vec3d(0.0, -1.0, 0.0)
    };
    private final Vec3d startVec3Path;
    private final Vec3d endVec3Path;
    private final ArrayList<PathHub> pathHubs = new ArrayList<>();
    private final ArrayList<PathHub> workingPathHubList = new ArrayList<>();
    private ArrayList<Vec3d> path = new ArrayList<>();

    public PathFinder(Vec3d startVec3Path, Vec3d endVec3Path) {
        this.startVec3Path = this.floorVec3d(startVec3Path);
        this.endVec3Path = this.floorVec3d(endVec3Path);
    }

    public static boolean isValid(Vec3d loc, boolean checkGround) {
        return isValid((int) loc.x, (int) loc.y, (int) loc.z, checkGround);
    }

    public static boolean isValid(int x, int y, int z, boolean checkGround) {
        BlockPos block1 = new BlockPos(x, y, z);
        BlockPos block2 = new BlockPos(x, y + 1, z);
        BlockPos block3 = new BlockPos(x, y - 1, z);
        return !isNotPassable(block1) && !isNotPassable(block2) && (isNotPassable(block3) || !checkGround) && canWalkOn(block3);
    }

    private static boolean isNotPassable(BlockPos block) {
        Block b = Objects.requireNonNull(Global.mc.world).getBlockState(block).getBlock();
        return b.getDefaultState().isSolidBlock(Global.mc.world, block)
            || b == Blocks.GLASS
            || b == Blocks.GRAY_STAINED_GLASS
            || b == Blocks.GREEN_STAINED_GLASS
            || b == Blocks.BLACK_STAINED_GLASS
            || b == Blocks.BLUE_STAINED_GLASS
            || b == Blocks.BROWN_STAINED_GLASS
            || b == Blocks.CYAN_STAINED_GLASS
            || b == Blocks.LIGHT_BLUE_STAINED_GLASS
            || b == Blocks.LIGHT_GRAY_STAINED_GLASS
            || b == Blocks.LIME_STAINED_GLASS
            || b == Blocks.MAGENTA_STAINED_GLASS
            || b == Blocks.ORANGE_STAINED_GLASS
            || b == Blocks.PINK_STAINED_GLASS
            || b == Blocks.WHITE_STAINED_GLASS
            || b == Blocks.YELLOW_STAINED_GLASS
            || b == Blocks.PURPLE_STAINED_GLASS
            || b == Blocks.RED_STAINED_GLASS
            || b == Blocks.GLASS_PANE
            || b == Blocks.GRAY_STAINED_GLASS_PANE
            || b == Blocks.GREEN_STAINED_GLASS_PANE
            || b == Blocks.BLACK_STAINED_GLASS_PANE
            || b == Blocks.BLUE_STAINED_GLASS_PANE
            || b == Blocks.BROWN_STAINED_GLASS_PANE
            || b == Blocks.CYAN_STAINED_GLASS_PANE
            || b == Blocks.LIGHT_BLUE_STAINED_GLASS_PANE
            || b == Blocks.LIGHT_GRAY_STAINED_GLASS_PANE
            || b == Blocks.LIME_STAINED_GLASS_PANE
            || b == Blocks.MAGENTA_STAINED_GLASS_PANE
            || b == Blocks.ORANGE_STAINED_GLASS_PANE
            || b == Blocks.PINK_STAINED_GLASS_PANE
            || b == Blocks.WHITE_STAINED_GLASS_PANE
            || b == Blocks.YELLOW_STAINED_GLASS_PANE
            || b == Blocks.PURPLE_STAINED_GLASS_PANE
            || b == Blocks.RED_STAINED_GLASS_PANE
            || b instanceof AbstractSkullBlock
            || b instanceof SnowBlock
            || b instanceof SnowyBlock
            || b instanceof DoorBlock
            || b instanceof LeavesBlock
            || b instanceof SlabBlock
            || b instanceof StairsBlock
            || b instanceof CactusBlock
            || b instanceof ChestBlock
            || b instanceof EnderChestBlock
            || b instanceof SkullBlock
            || b instanceof PaneBlock
            || b instanceof FenceBlock
            || b instanceof WallBlock
            || b instanceof TintedGlassBlock
            || b instanceof StainedGlassBlock
            || b instanceof PistonBlock
            || b instanceof PistonExtensionBlock
            || b instanceof PistonHeadBlock
            || b instanceof TrapdoorBlock
            || b instanceof EndPortalFrameBlock
            || b instanceof EndPortalBlock
            || b instanceof BedBlock
            || b instanceof CobwebBlock
            || b instanceof BarrierBlock
            || b instanceof LadderBlock
            || b instanceof CarpetBlock;
    }

    private static boolean canWalkOn(BlockPos block) {
        Block b = Objects.requireNonNull(Global.mc.world).getBlockState(block).getBlock();
        return !(b instanceof FenceBlock) && !(b instanceof FenceGateBlock) && !(b instanceof WallBlock) && b != Blocks.BARRIER;
    }

    private Vec3d floorVec3d(Vec3d vec) {
        return new Vec3d(Math.floor(vec.x), Math.floor(vec.y), Math.floor(vec.z));
    }

    public ArrayList<Vec3d> getPath() {
        return this.path;
    }

    public void compute() {
        this.compute(100, 4);
    }

    public void compute(int loops, int depth) {
        this.path.clear();
        this.workingPathHubList.clear();
        ArrayList<Vec3d> initPath = new ArrayList<>();
        initPath.add(this.startVec3Path);
        this.workingPathHubList.add(new PathHub(this.startVec3Path, null, initPath, this.startVec3Path.squaredDistanceTo(this.endVec3Path), 0.0, 0.0));

        label53:
        for (int i = 0; i < loops; i++) {
            this.workingPathHubList.sort(new CompareHub());
            if (!this.workingPathHubList.isEmpty()) {
                int j = 0;
                Iterator var6 = new ArrayList<>(this.workingPathHubList).iterator();

                while (true) {
                    if (var6.hasNext()) {
                        PathHub pathHub = (PathHub) var6.next();
                        if (++j <= depth) {
                            this.workingPathHubList.remove(pathHub);
                            this.pathHubs.add(pathHub);

                            for (Vec3d direction : directions) {
                                Vec3d loc = this.floorVec3d(pathHub.getLoc().add(direction));
                                if (isValid(loc, false) && this.putHub(pathHub, loc, 0.0)) {
                                    return;
                                }
                            }

                            Vec3d loc1 = this.floorVec3d(pathHub.getLoc().add(0.0, 1.0, 0.0));
                            Vec3d loc2 = this.floorVec3d(pathHub.getLoc().add(0.0, -1.0, 0.0));
                            if ((!isValid(loc1, false) || !this.putHub(pathHub, loc1, 0.0)) && (!isValid(loc2, false) || !this.putHub(pathHub, loc2, 0.0))) {
                                continue;
                            }

                            return;
                        }
                    }
                    continue label53;
                }
            }
            break;
        }

        this.pathHubs.sort(new CompareHub());
        this.path = this.pathHubs.getFirst().getPathway();
    }

    public boolean putHub(PathHub parent, Vec3d loc, double cost) {
        PathHub existingPathHub = this.doesHubExistAt(loc);
        double totalCost = cost;
        if (parent != null) {
            totalCost = cost + parent.getMaxCost();
        }

        if (existingPathHub == null) {
            if (loc.squaredDistanceTo(this.endVec3Path) <= 1.0) {
                this.path.clear();
                this.path = new ArrayList<>(Objects.requireNonNull(parent).getPathway());
                this.path.add(loc);
                return true;
            }

            ArrayList<Vec3d> newPath = new ArrayList<>(Objects.requireNonNull(parent).getPathway());
            newPath.add(loc);
            this.workingPathHubList.add(new PathHub(loc, parent, newPath, loc.squaredDistanceTo(this.endVec3Path), cost, totalCost));
        }

        return false;
    }

    public PathHub doesHubExistAt(Vec3d loc) {
        for (PathHub pathHub : this.pathHubs) {
            if (pathHub.getLoc().equals(loc)) {
                return pathHub;
            }
        }

        return null;
    }

    public static class CompareHub implements Comparator<PathHub> {
        public int compare(PathHub o1, PathHub o2) {
            return Double.compare(o1.getSqDist() + o1.getMaxCost(), o2.getSqDist() + o2.getMaxCost());
        }
    }
}
