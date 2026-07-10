package dev.abstr3act.addon.manager;

import dev.abstr3act.addon.Compassion;
import dev.abstr3act.addon.modules.Compassion.MoveFix;
import dev.abstr3act.addon.utils.compassion.BlockPosX;
import meteordevelopment.meteorclient.MeteorClient;
import net.minecraft.block.BedBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.projectile.thrown.ExperienceBottleEntity;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.*;
import net.minecraft.world.chunk.WorldChunk;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BlockUtil {
    public static final List<Block> shiftBlocks = Arrays.asList(
        Blocks.ENDER_CHEST,
        Blocks.CHEST,
        Blocks.TRAPPED_CHEST,
        Blocks.CRAFTING_TABLE,
        Blocks.BIRCH_TRAPDOOR,
        Blocks.BAMBOO_TRAPDOOR,
        Blocks.DARK_OAK_TRAPDOOR,
        Blocks.CHERRY_TRAPDOOR,
        Blocks.ANVIL,
        Blocks.BREWING_STAND,
        Blocks.HOPPER,
        Blocks.DROPPER,
        Blocks.DISPENSER,
        Blocks.ACACIA_TRAPDOOR,
        Blocks.ENCHANTING_TABLE,
        Blocks.WHITE_SHULKER_BOX,
        Blocks.ORANGE_SHULKER_BOX,
        Blocks.MAGENTA_SHULKER_BOX,
        Blocks.LIGHT_BLUE_SHULKER_BOX,
        Blocks.YELLOW_SHULKER_BOX,
        Blocks.LIME_SHULKER_BOX,
        Blocks.PINK_SHULKER_BOX,
        Blocks.GRAY_SHULKER_BOX,
        Blocks.CYAN_SHULKER_BOX,
        Blocks.PURPLE_SHULKER_BOX,
        Blocks.BLUE_SHULKER_BOX,
        Blocks.BROWN_SHULKER_BOX,
        Blocks.GREEN_SHULKER_BOX,
        Blocks.RED_SHULKER_BOX,
        Blocks.BLACK_SHULKER_BOX
    );
    public static final CopyOnWriteArrayList<BlockPos> placedPos = new CopyOnWriteArrayList<>();

    public static boolean canPlace(BlockPos pos) {
        return canPlace(pos, 1000.0);
    }

    public static boolean canPlace(BlockPos pos, double distance) {
        if (getPlaceSide(pos, distance) == null) {
            return false;
        } else {
            return !canReplace(pos) ? false : !hasEntity(pos, false);
        }
    }

    public static boolean canPlace(BlockPos pos, double distance, boolean ignoreCrystal) {
        if (getPlaceSide(pos, distance) == null) {
            return false;
        } else {
            return !canReplace(pos) ? false : !hasEntity(pos, ignoreCrystal);
        }
    }

    public static boolean clientCanPlace(BlockPos pos) {
        return clientCanPlace(pos, false);
    }

    public static boolean clientCanPlace(BlockPos pos, boolean ignoreCrystal) {
        return !canReplace(pos) ? false : !hasEntity(pos, ignoreCrystal);
    }

    public static List<Entity> getEntities(Box box) {
        List<Entity> list = new ArrayList<>();

        for (Entity entity : MeteorClient.mc.world.getEntities()) {
            if (entity != null && entity.getBoundingBox().intersects(box)) {
                list.add(entity);
            }
        }

        return list;
    }

    public static List<EndCrystalEntity> getEndCrystals(Box box) {
        List<EndCrystalEntity> list = new ArrayList<>();

        for (Entity entity : MeteorClient.mc.world.getEntities()) {
            if (entity instanceof EndCrystalEntity crystal && crystal.getBoundingBox().intersects(box)) {
                list.add(crystal);
            }
        }

        return list;
    }

    public static boolean hasEntity(BlockPos pos, boolean ignoreCrystal) {
        for (Entity entity : getEntities(new Box(pos))) {
            if (entity.isAlive()
                && !(entity instanceof ItemEntity)
                && !(entity instanceof ExperienceOrbEntity)
                && !(entity instanceof ExperienceBottleEntity)
                && !(entity instanceof ArrowEntity)
                && (!ignoreCrystal || !(entity instanceof EndCrystalEntity))
                && !(entity instanceof ArmorStandEntity)) {
                return true;
            }
        }

        return false;
    }

    public static boolean hasCrystal(BlockPos pos) {
        for (Entity entity : getEndCrystals(new Box(pos))) {
            if (entity.isAlive() && entity instanceof EndCrystalEntity) {
                return true;
            }
        }

        return false;
    }

    public static boolean hasEntityBlockCrystal(BlockPos pos, boolean ignoreCrystal) {
        for (Entity entity : getEntities(new Box(pos))) {
            if (entity.isAlive() && (!ignoreCrystal || !(entity instanceof EndCrystalEntity)) && !(entity instanceof ArmorStandEntity)) {
                return true;
            }
        }

        return false;
    }

    public static boolean hasEntityBlockCrystal(BlockPos pos, boolean ignoreCrystal, boolean ignoreItem) {
        for (Entity entity : getEntities(new Box(pos))) {
            if (entity.isAlive()
                && (!ignoreItem || !(entity instanceof ItemEntity))
                && (!ignoreCrystal || !(entity instanceof EndCrystalEntity))
                && !(entity instanceof ArmorStandEntity)) {
                return true;
            }
        }

        return false;
    }

    public static Direction getBestNeighboring(BlockPos pos, Direction facing) {
        for (Direction i : Direction.values()) {
            if ((facing == null || !pos.offset(i).equals(pos.offset(facing, -1))) && i != Direction.DOWN && getPlaceSide(pos, false, true) != null) {
                return i;
            }
        }

        Direction bestFacing = null;
        double distance = 0.0;

        for (Direction ix : Direction.values()) {
            if ((facing == null || !pos.offset(ix).equals(pos.offset(facing, -1)))
                && ix != Direction.DOWN
                && getPlaceSide(pos) != null
                && (bestFacing == null || MeteorClient.mc.player.getEyePos().squaredDistanceTo(pos.offset(ix).toCenterPos()) < distance)) {
                bestFacing = ix;
                distance = MeteorClient.mc.player.getEyePos().squaredDistanceTo(pos.offset(ix).toCenterPos());
            }
        }

        return bestFacing;
    }

    public static boolean canPlaceCrystal(BlockPos pos) {
        BlockPos obsPos = pos.down();
        BlockPos boost = obsPos.up();
        return (getBlock(obsPos) == Blocks.BEDROCK || getBlock(obsPos) == Blocks.OBSIDIAN)
            && getClickSideStrict(obsPos) != null
            && MeteorClient.mc.world.isAir(boost)
            && !hasEntityBlockCrystal(boost, false)
            && !hasEntityBlockCrystal(boost.up(), false);
    }

    public static void placeCrystal(BlockPos pos, boolean rotate) {
        boolean offhand = MeteorClient.mc.player.getOffHandStack().getItem() == Items.END_CRYSTAL;
        BlockPos obsPos = pos.down();
        Direction facing = getClickSide(obsPos);
        Vec3d vec = obsPos.toCenterPos().add(facing.getVector().getX() * 0.5, facing.getVector().getY() * 0.5, facing.getVector().getZ() * 0.5);
        if (rotate) {
            Compassion.ROTATION.lookAt(vec);
        }

        clickBlock(obsPos, facing, false, offhand ? Hand.OFF_HAND : Hand.MAIN_HAND);
    }

    public static void placeBlock(BlockPos pos, boolean rotate) {
        placeBlock(pos, rotate, MoveFix.INSTANCE.packetPlace.get());
    }

    public static void placeBlock(BlockPos pos, boolean rotate, boolean packet) {
        if (airPlace()) {
            placedPos.add(pos);
            clickBlock(pos, Direction.DOWN, rotate, Hand.MAIN_HAND, packet);
        } else {
            Direction side = getPlaceSide(pos);
            if (side != null) {
                placedPos.add(pos);
                clickBlock(pos.offset(side), side.getOpposite(), rotate, Hand.MAIN_HAND, packet);
            }
        }
    }

    public static void clickBlock(BlockPos pos, Direction side, boolean rotate) {
        clickBlock(pos, side, rotate, Hand.MAIN_HAND);
    }

    public static void clickBlock(BlockPos pos, Direction side, boolean rotate, Hand hand) {
        clickBlock(pos, side, rotate, hand, MoveFix.INSTANCE.packetPlace.get());
    }

    public static void clickBlock(BlockPos pos, Direction side, boolean rotate, boolean packet) {
        clickBlock(pos, side, rotate, Hand.MAIN_HAND, packet);
    }

    public static void clickBlock(BlockPos pos, Direction side, boolean rotate, Hand hand, boolean packet) {
        Vec3d directionVec = new Vec3d(
            pos.getX() + 0.5 + side.getVector().getX() * 0.5, pos.getY() + 0.5 + side.getVector().getY() * 0.5, pos.getZ() + 0.5 + side.getVector().getZ() * 0.5
        );
        if (rotate) {
            Compassion.ROTATION.lookAt(directionVec);
        }

        EntityUtil.swingHand(hand, SwingSide.Server);
        BlockHitResult result = new BlockHitResult(directionVec, side, pos, false);
        if (packet) {
            MeteorClient.mc.interactionManager.sendSequencedPacket(MeteorClient.mc.world, id -> new PlayerInteractBlockC2SPacket(hand, result, id));
        } else {
            MeteorClient.mc.interactionManager.interactBlock(MeteorClient.mc.player, hand, result);
        }

        if (rotate) {
            Compassion.ROTATION.snapBack();
        }
    }

    public static void clickBlock(BlockPos pos, Direction side, boolean rotate, Hand hand, SwingSide swingSide) {
        Vec3d directionVec = new Vec3d(
            pos.getX() + 0.5 + side.getVector().getX() * 0.5, pos.getY() + 0.5 + side.getVector().getY() * 0.5, pos.getZ() + 0.5 + side.getVector().getZ() * 0.5
        );
        if (rotate) {
            Compassion.ROTATION.lookAt(directionVec);
        }

        EntityUtil.swingHand(hand, swingSide);
        BlockHitResult result = new BlockHitResult(directionVec, side, pos, false);
        MeteorClient.mc.interactionManager.sendSequencedPacket(MeteorClient.mc.world, id -> new PlayerInteractBlockC2SPacket(hand, result, id));
        if (rotate && MoveFix.INSTANCE.snapBack.get()) {
            Compassion.ROTATION.snapBack();
        }
    }

    public static Direction getPlaceSide(BlockPos pos) {
        return getPlaceSide(pos, MoveFix.INSTANCE.placement.get() == Placement.Strict, MoveFix.INSTANCE.placement.get() == Placement.Legit);
    }

    public static Direction getPlaceSide(BlockPos pos, boolean strict, boolean legit) {
        if (pos == null) {
            return null;
        } else {
            double dis = 114514.0;
            Direction side = null;

            for (Direction i : Direction.values()) {
                if (canClick(pos.offset(i))
                    && !canReplace(pos.offset(i))
                    && (!legit || EntityUtil.canSee(pos.offset(i), i.getOpposite()))
                    && (!strict || isStrictDirection(pos.offset(i), i.getOpposite()))) {
                    double vecDis = MeteorClient.mc
                        .player
                        .getEyePos()
                        .squaredDistanceTo(pos.toCenterPos().add(i.getVector().getX() * 0.5, i.getVector().getY() * 0.5, i.getVector().getZ() * 0.5));
                    if (side == null || vecDis < dis) {
                        side = i;
                        dis = vecDis;
                    }
                }
            }

            return airPlace() ? Direction.DOWN : side;
        }
    }

    public static double distanceToXZ(double x, double z, double x2, double z2) {
        double dx = x2 - x;
        double dz = z2 - z;
        return Math.sqrt(dx * dx + dz * dz);
    }

    public static double distanceToXZ(double x, double z) {
        return distanceToXZ(x, z, MeteorClient.mc.player.getX(), MeteorClient.mc.player.getZ());
    }

    public static Direction getPlaceSide(BlockPos pos, double distance) {
        if (airPlace()) {
            return Direction.DOWN;
        } else {
            double dis = 114514.0;
            Direction side = null;

            for (Direction i : Direction.values()) {
                if (canClick(pos.offset(i))
                    && !canReplace(pos.offset(i))
                    && (
                    MoveFix.INSTANCE.placement.get() == Placement.Legit
                        ? EntityUtil.canSee(pos.offset(i), i.getOpposite())
                        : MoveFix.INSTANCE.placement.get() != Placement.Strict || isStrictDirection(pos.offset(i), i.getOpposite())
                )) {
                    double vecDis = MeteorClient.mc
                        .player
                        .getEyePos()
                        .squaredDistanceTo(pos.toCenterPos().add(i.getVector().getX() * 0.5, i.getVector().getY() * 0.5, i.getVector().getZ() * 0.5));
                    if (!(MathHelper.sqrt((float) vecDis) > distance) && (side == null || vecDis < dis)) {
                        side = i;
                        dis = vecDis;
                    }
                }
            }

            return side;
        }
    }

    public static Direction getClickSide(BlockPos pos) {
        Direction side = null;
        double range = 100.0;

        for (Direction i : Direction.values()) {
            if (EntityUtil.canSee(pos, i) && !(MathHelper.sqrt((float) MeteorClient.mc.player.getEyePos().squaredDistanceTo(pos.offset(i).toCenterPos())) > range)) {
                side = i;
                range = MathHelper.sqrt((float) MeteorClient.mc.player.getEyePos().squaredDistanceTo(pos.offset(i).toCenterPos()));
            }
        }

        if (side != null) {
            return side;
        } else {
            Direction var8 = Direction.UP;

            for (Direction ix : Direction.values()) {
                if ((
                    MoveFix.INSTANCE.placement.get() != Placement.Strict
                        || isStrictDirection(pos, ix) && (!MoveFix.INSTANCE.blockCheck.get() || MeteorClient.mc.world.isAir(pos.offset(ix)))
                )
                    && !(MathHelper.sqrt((float) MeteorClient.mc.player.getEyePos().squaredDistanceTo(pos.offset(ix).toCenterPos())) > range)) {
                    var8 = ix;
                    range = MathHelper.sqrt((float) MeteorClient.mc.player.getEyePos().squaredDistanceTo(pos.offset(ix).toCenterPos()));
                }
            }

            return var8;
        }
    }

    public static Direction getClickSideStrict(BlockPos pos) {
        Direction side = null;
        double range = 100.0;

        for (Direction i : Direction.values()) {
            if (EntityUtil.canSee(pos, i) && !(MathHelper.sqrt((float) MeteorClient.mc.player.getEyePos().squaredDistanceTo(pos.offset(i).toCenterPos())) > range)) {
                side = i;
                range = MathHelper.sqrt((float) MeteorClient.mc.player.getEyePos().squaredDistanceTo(pos.offset(i).toCenterPos()));
            }
        }

        if (side != null) {
            return side;
        } else {
            Direction var8 = null;

            for (Direction ix : Direction.values()) {
                if ((
                    MoveFix.INSTANCE.placement.get() != Placement.Strict
                        || isStrictDirection(pos, ix) && (!MoveFix.INSTANCE.blockCheck.get() || MeteorClient.mc.world.isAir(pos.offset(ix)))
                )
                    && !(MathHelper.sqrt((float) MeteorClient.mc.player.getEyePos().squaredDistanceTo(pos.offset(ix).toCenterPos())) > range)) {
                    var8 = ix;
                    range = MathHelper.sqrt((float) MeteorClient.mc.player.getEyePos().squaredDistanceTo(pos.offset(ix).toCenterPos()));
                }
            }

            return var8;
        }
    }

    public static boolean isStrictDirection(BlockPos pos, Direction side) {
        if (MeteorClient.mc.player.getBlockY() - pos.getY() >= 0 && side == Direction.DOWN) {
            return false;
        } else if (side == Direction.UP && pos.getY() + 1 > MeteorClient.mc.player.getEyePos().getY()) {
            return false;
        } else if (!MoveFix.INSTANCE.blockCheck.get()
            || getBlock(pos.offset(side)) != Blocks.OBSIDIAN && getBlock(pos.offset(side)) != Blocks.BEDROCK && getBlock(pos.offset(side)) != Blocks.RESPAWN_ANCHOR) {
            Vec3d eyePos = EntityUtil.getEyesPos();
            Vec3d blockCenter = pos.toCenterPos();
            ArrayList<Direction> validAxis = new ArrayList<>();
            validAxis.addAll(checkAxis(eyePos.x - blockCenter.x, Direction.WEST, Direction.EAST, false));
            validAxis.addAll(checkAxis(eyePos.y - blockCenter.y, Direction.DOWN, Direction.UP, true));
            validAxis.addAll(checkAxis(eyePos.z - blockCenter.z, Direction.NORTH, Direction.SOUTH, false));
            return validAxis.contains(side);
        } else {
            return false;
        }
    }

    public static ArrayList<Direction> checkAxis(double diff, Direction negativeSide, Direction positiveSide, boolean bothIfInRange) {
        ArrayList<Direction> valid = new ArrayList<>();
        if (diff < -0.5) {
            valid.add(negativeSide);
        }

        if (diff > 0.5) {
            valid.add(positiveSide);
        }

        if (bothIfInRange) {
            if (!valid.contains(negativeSide)) {
                valid.add(negativeSide);
            }

            if (!valid.contains(positiveSide)) {
                valid.add(positiveSide);
            }
        }

        return valid;
    }

    public static ArrayList<BlockEntity> getTileEntities() {
        return getLoadedChunks().flatMap(chunk -> chunk.getBlockEntities().values().stream()).collect(Collectors.toCollection(ArrayList::new));
    }

    public static Stream<WorldChunk> getLoadedChunks() {
        int radius = Math.max(2, MeteorClient.mc.options.getClampedViewDistance()) + 3;
        int diameter = radius * 2 + 1;
        ChunkPos center = MeteorClient.mc.player.getChunkPos();
        ChunkPos min = new ChunkPos(center.x - radius, center.z - radius);
        ChunkPos max = new ChunkPos(center.x + radius, center.z + radius);
        return Stream.<ChunkPos>iterate(min, pos -> {
                int x = pos.x;
                int z = pos.z;
                if (++x > max.x) {
                    x = min.x;
                    z++;
                }

                return new ChunkPos(x, z);
            })
            .limit((long) diameter * diameter)
            .filter(c -> MeteorClient.mc.world.isChunkLoaded(c.x, c.z))
            .map(c -> MeteorClient.mc.world.getChunk(c.x, c.z))
            .filter(Objects::nonNull);
    }

    public static ArrayList<BlockPos> getSphere(float range) {
        return getSphere(range, MeteorClient.mc.player.getEyePos());
    }

    public static ArrayList<BlockPos> getSphere(float range, Vec3d pos) {
        ArrayList<BlockPos> list = new ArrayList<>();

        for (double x = pos.getX() - range; x < pos.getX() + range; x++) {
            for (double z = pos.getZ() - range; z < pos.getZ() + range; z++) {
                for (double y = pos.getY() - range; y < pos.getY() + range; y++) {
                    BlockPos curPos = new BlockPosX(x, y, z);
                    if (!(curPos.toCenterPos().distanceTo(pos) > range) && !list.contains(curPos)) {
                        list.add(curPos);
                    }
                }
            }
        }

        return list;
    }

    public static Block getBlock(BlockPos pos) {
        return MeteorClient.mc.world.getBlockState(pos).getBlock();
    }

    public static boolean canReplace(BlockPos pos) {
        if (pos.getY() >= 320) {
            return false;
        } else {
            return MoveFix.INSTANCE.multiPlace.get() && placedPos.contains(pos) ? true : MeteorClient.mc.world.getBlockState(pos).isReplaceable();
        }
    }

    public static boolean canClick(BlockPos pos) {
        return MoveFix.INSTANCE.multiPlace.get() && placedPos.contains(pos)
            ? true
            : MeteorClient.mc.world.getBlockState(pos).isSolid()
            && (!shiftBlocks.contains(getBlock(pos)) && !(getBlock(pos) instanceof BedBlock) || MeteorClient.mc.player.isSneaking());
    }

    public static boolean airPlace() {
        return MoveFix.INSTANCE.placement.get() == Placement.AirPlace;
    }
}
