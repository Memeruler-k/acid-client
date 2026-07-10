package dev.abstr3act.addon.modules.Seraphim;

import dev.abstr3act.addon.Compassion;
import dev.abstr3act.addon.module.SeraphimModule;
import meteordevelopment.meteorclient.events.world.TickEvent.Post;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket.Mode;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class AutoWallJump extends SeraphimModule {
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
    Stage stage = Stage.RELEASE;

    public AutoWallJump() {
        super(Compassion.SERAPHIM, "AutoWallJump", "KitPVP Assist");
    }

    public static boolean checkCollidingBlock(ClientPlayerEntity player, World world, BlockPos pos) {
        Box playerBoundingBox = player.getBoundingBox();
        BlockState blockState = world.getBlockState(pos);
        if (blockState.getCollisionShape(world, pos).isEmpty()) {
            return false;
        } else {
            Box blockBoundingBox = blockState.getCollisionShape(world, pos).getBoundingBox().offset(pos);
            return playerBoundingBox.intersects(blockBoundingBox);
        }
    }

    public static boolean isPlayerBlocked(ClientPlayerEntity player, World world) {
        Vec3d velocity = player.getVelocity();
        if (velocity.x == 0.0 && velocity.z == 0.0) {
            return false;
        } else {
            Box playerBoundingBox = player.getBoundingBox().offset(velocity);

            for (BlockPos pos : BlockPos.iterate(
                (int) Math.floor(playerBoundingBox.minX),
                (int) Math.floor(playerBoundingBox.minY),
                (int) Math.floor(playerBoundingBox.minZ),
                (int) Math.ceil(playerBoundingBox.maxX),
                (int) Math.ceil(playerBoundingBox.maxY),
                (int) Math.ceil(playerBoundingBox.maxZ)
            )) {
                BlockState blockState = world.getBlockState(pos);
                if (!blockState.getCollisionShape(world, pos).isEmpty()) {
                    Box blockBoundingBox = blockState.getCollisionShape(world, pos).getBoundingBox().offset(pos);
                    if (playerBoundingBox.intersects(blockBoundingBox)) {
                        return true;
                    }
                }
            }

            return false;
        }
    }

    private static boolean isFullBlock(World world, BlockPos pos) {
        BlockState blockState = world.getBlockState(pos);
        return blockState.isOpaque() && Block.isShapeFullCube(blockState.getCollisionShape(world, pos));
    }

    @EventHandler
    public void onTickEvent(Post event) {
        if (this.mc.player != null) {
            if (this.canJump(this.mc.player)) {
                if (this.mc.player.fallDistance > 0.0F) {
                    this.mc.options.sneakKey.setPressed(true);
                    this.sendPacket(new ClientCommandC2SPacket(this.mc.player, Mode.PRESS_SHIFT_KEY));
                } else if (this.mc.player.isSneaking()) {
                    this.mc.options.sneakKey.setPressed(false);
                    this.sendPacket(new ClientCommandC2SPacket(this.mc.player, Mode.RELEASE_SHIFT_KEY));
                }
            }
        }
    }

    public void onDeactivate() {
        this.mc.options.sneakKey.setPressed(false);
        this.stage = Stage.RELEASE;
    }

    public boolean isMoving() {
        return this.mc.player != null
            && this.mc.world != null
            && this.mc.player.input != null
            && (this.mc.player.input.movementForward != 0.0 || this.mc.player.input.movementSideways != 0.0);
    }

    public boolean canJump(ClientPlayerEntity player) {
        World world = player.getWorld();
        Vec3d playerPos = player.getPos();
        Vec3d playerLook = player.getRotationVec(1.0F);
        Vec3d forwardPos = playerPos.add(playerLook.normalize().multiply(1.0));
        BlockPos baseBlockPos = new BlockPos((int) forwardPos.x, (int) forwardPos.y, (int) forwardPos.z);
        return isPlayerBlocked(this.mc.player, this.mc.world) ? true : isFullBlock(world, baseBlockPos) && isFullBlock(world, baseBlockPos.up());
    }

    static enum Stage {
        STORE,
        RELEASE;
    }
}
