package dev.abstr3act.addon.utils;

import meteordevelopment.meteorclient.MeteorClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket.PositionAndOnGround;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class PositionUtils {
    public static boolean sendFakePosB(double x, double y, double z, boolean onGround) {
        MeteorClient.mc.player.networkHandler.sendPacket(new PositionAndOnGround(x, y, z, onGround));
        return true;
    }

    public static void sendFakePos(double x, double y, double z, boolean onGround) {
        MeteorClient.mc.player.networkHandler.sendPacket(new PositionAndOnGround(x, y, z, onGround));
    }

    public static void sendFakePos(Vec3d vec3d, boolean onGround) {
        MeteorClient.mc.player.networkHandler.sendPacket(new PositionAndOnGround(vec3d.x, vec3d.y, vec3d.z, onGround));
    }

    public static void sendFakePos(BlockPos blockPos, boolean onGround) {
        MeteorClient.mc.player.networkHandler.sendPacket(new PositionAndOnGround(blockPos.getX(), blockPos.getY(), blockPos.getZ(), onGround));
    }

    public static void sendFakeY(double y, boolean onGround) {
        MeteorClient.mc.player.networkHandler.sendPacket(new PositionAndOnGround(MeteorClient.mc.player.getX(), y, MeteorClient.mc.player.getZ(), onGround));
    }

    public static void sendBack(boolean onGround) {
        MeteorClient.mc
            .player
            .networkHandler
            .sendPacket(new PositionAndOnGround(MeteorClient.mc.player.getX(), MeteorClient.mc.player.getY(), MeteorClient.mc.player.getZ(), onGround));
    }

    public static void syncClientPosInt() {
        sendClientPos(MeteorClient.mc.player.getBlockPos());
    }

    public static void syncAccurateClientPosInt() {
        MeteorClient.mc.player.setPos(MeteorClient.mc.player.getX(), MeteorClient.mc.player.getY(), MeteorClient.mc.player.getZ());
    }

    public static void sendClientPos(BlockPos playerPos) {
        MeteorClient.mc.player.setPos(playerPos.getX(), playerPos.getY(), playerPos.getZ());
    }

    public static void sendClientPos(Double playerPos) {
        MeteorClient.mc.player.setPos(playerPos, playerPos, playerPos);
    }

    public static BlockPos getTargetXOffset(PlayerEntity target, int x) {
        return new BlockPos(target.getBlockX() + x, target.getBlockY(), target.getBlockZ());
    }

    public static BlockPos getTargetYOffset(PlayerEntity target, int y) {
        return new BlockPos(target.getBlockX(), target.getBlockY() + y, target.getBlockZ());
    }

    public static BlockPos getTargetZOffset(PlayerEntity target, int z) {
        return new BlockPos(target.getBlockX(), target.getBlockY(), target.getBlockZ() + z);
    }

    public static BlockPos getTargetOffset(PlayerEntity target, int x, int y, int z) {
        return new BlockPos(target.getBlockX() + x, target.getBlockY() + y, target.getBlockZ() + z);
    }
}
