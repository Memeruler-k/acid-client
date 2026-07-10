package dev.abstr3act.addon.utils.seraphim;

import meteordevelopment.meteorclient.MeteorClient;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;

public final class BlockUtils {
    public static ActionResult interact(Vec3d vec) {
        return interact(vec, Direction.UP);
    }

    public static ActionResult interact(BlockPos pos, Direction dir) {
        Vec3d vec = new Vec3d(pos.getX(), pos.getY(), pos.getZ());
        return interact(vec, dir);
    }

    public static ActionResult interact(Vec3d vec3d, Direction dir) {
        if (PlayerUtils.invalid()) {
            return ActionResult.FAIL;
        } else {
            Vec3i vec3i = new Vec3i((int) vec3d.x, (int) vec3d.y, (int) vec3d.z);
            BlockPos pos = new BlockPos(vec3i);
            BlockHitResult result = new BlockHitResult(vec3d, dir, pos, false);
            ClientPlayerEntity p = PlayerUtils.player();
            return PlayerUtils.getInteractions().interactBlock(p, p.getActiveHand(), result);
        }
    }

    public static ActionResult interact(BlockHitResult result) {
        if (PlayerUtils.invalid()) {
            return ActionResult.FAIL;
        } else {
            ClientPlayerInteractionManager im = PlayerUtils.getInteractions();
            ClientPlayerEntity p = PlayerUtils.player();
            return im.interactBlock(p, p.getActiveHand(), result);
        }
    }

    public static boolean matchTargetBlock(Block block) {
        if (MeteorClient.mc.crosshairTarget == null) {
            return false;
        } else {
            Vec3d vec3d = MeteorClient.mc.crosshairTarget.getPos();
            Vec3i vec3i = new Vec3i((int) vec3d.x, (int) vec3d.y, (int) vec3d.z);
            BlockPos pos = new BlockPos(vec3i);
            return matchBlock(pos, block);
        }
    }

    public static boolean matchBlock(BlockPos pos, Block block) {
        if (PlayerUtils.invalid()) {
            return false;
        } else {
            World world = PlayerUtils.getWorld();
            BlockState state = world.getBlockState(pos);
            return state != null && state.isOf(block);
        }
    }

    public static boolean canCrystalOn(BlockPos pos) {
        return matchBlock(pos, Blocks.OBSIDIAN) || matchBlock(pos, Blocks.BEDROCK);
    }
}
