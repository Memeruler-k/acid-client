package dev.abstr3act.addon.utils.player;

import dev.abstr3act.addon.utils.Wrapper;
import dev.abstr3act.addon.utils.world.BlockHelper;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.utils.player.Rotations;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket.Action;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

import java.util.ArrayList;

public class AutomationUtils {
    public static ArrayList<Vec3i> surroundPositions = new ArrayList<Vec3i>() {
        {
            this.add(new Vec3i(1, 0, 0));
            this.add(new Vec3i(-1, 0, 0));
            this.add(new Vec3i(0, 0, 1));
            this.add(new Vec3i(0, 0, -1));
        }
    };

    public static boolean isAnvilBlock(BlockPos pos) {
        return BlockHelper.getBlock(pos) == Blocks.ANVIL || BlockHelper.getBlock(pos) == Blocks.CHIPPED_ANVIL || BlockHelper.getBlock(pos) == Blocks.DAMAGED_ANVIL;
    }

    public static boolean isWeb(BlockPos pos) {
        return BlockHelper.getBlock(pos) == Blocks.COBWEB || BlockHelper.getBlock(pos) == Block.getBlockFromItem(Items.STRING);
    }

    public static boolean isBurrowed(PlayerEntity p, boolean holeCheck) {
        BlockPos pos = p.getBlockPos();
        return holeCheck && !Wrapper.isInHole(p)
            ? false
            : BlockHelper.getBlock(pos) == Blocks.ENDER_CHEST || BlockHelper.getBlock(pos) == Blocks.OBSIDIAN || isAnvilBlock(pos);
    }

    public static boolean isWebbed(PlayerEntity p) {
        BlockPos pos = p.getBlockPos();
        return isWeb(pos) ? true : isWeb(pos.up());
    }

    public static boolean isTrapBlock(BlockPos pos) {
        return BlockHelper.getBlock(pos) == Blocks.OBSIDIAN || BlockHelper.getBlock(pos) == Blocks.ENDER_CHEST;
    }

    public static boolean isSurroundBlock(BlockPos pos) {
        return BlockHelper.getBlock(pos) == Blocks.OBSIDIAN
            || BlockHelper.getBlock(pos) == Blocks.ENDER_CHEST
            || BlockHelper.getBlock(pos) == Blocks.RESPAWN_ANCHOR;
    }

    public static boolean canCrystal(PlayerEntity p) {
        BlockPos tpos = p.getBlockPos();

        for (Vec3i sp : surroundPositions) {
            BlockPos sb = tpos.add(sp.getX(), sp.getY(), sp.getZ());
            if (BlockHelper.getBlock(sb) == Blocks.AIR) {
                return true;
            }
        }

        return false;
    }

    public static void mineWeb(PlayerEntity p, int swordSlot) {
        if (p != null && swordSlot != -1) {
            BlockPos pos = p.getBlockPos();
            BlockPos webPos = null;
            if (isWeb(pos)) {
                webPos = pos;
            }

            if (isWeb(pos.up())) {
                webPos = pos.up();
            }

            if (isWeb(pos.up(2))) {
                webPos = pos.up(2);
            }

            if (webPos != null) {
                Wrapper.updateSlot(swordSlot);
                doRegularMine(webPos);
            }
        }
    }

    public static void doPacketMine(BlockPos targetPos) {
        MeteorClient.mc.player.networkHandler.sendPacket(new PlayerActionC2SPacket(Action.START_DESTROY_BLOCK, targetPos, Direction.UP));
        Wrapper.swingHand(false);
        MeteorClient.mc.player.networkHandler.sendPacket(new PlayerActionC2SPacket(Action.STOP_DESTROY_BLOCK, targetPos, Direction.UP));
    }

    public static void doRegularMine(BlockPos targetPos) {
        MeteorClient.mc.interactionManager.updateBlockBreakingProgress(targetPos, Direction.UP);
        Vec3d hitPos = new Vec3d(targetPos.getX() + 0.5, targetPos.getY() + 0.5, targetPos.getZ() + 0.5);
        Rotations.rotate(Rotations.getYaw(hitPos), Rotations.getPitch(hitPos), 50, () -> Wrapper.swingHand(false));
    }
}
