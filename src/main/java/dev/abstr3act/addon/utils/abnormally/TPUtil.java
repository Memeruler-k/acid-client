package dev.abstr3act.addon.utils.abnormally;

import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.utils.Utils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.render.DimensionEffects.SkyType;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket.OnGroundOnly;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket.PositionAndOnGround;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;

public class TPUtil {
    public static void sendMovePacket(double x, double y, double z, boolean onGround) {
        ClientPlayNetworkHandler networkHandler = MeteorClient.mc.player.networkHandler;
        networkHandler.sendPacket(new PositionAndOnGround(x, y, z, onGround));
    }

    public static void sendClientMovePacket(double x, double y, double z, boolean onGround) {
        MeteorClient.mc.player.setPos(x, y, z);
        MeteorClient.mc.player.setOnGround(onGround);
    }

    public static void warp(double x1, double y1, double z1, double x2, double y2, double z2) {
        double distance = calculateDistance(x1, y1, z1, x2, y2, z2);
        int packetsRequired = (int) Math.ceil(Math.abs(distance / 10.0));

        for (int packetNumber = 0; packetNumber < packetsRequired - 1; packetNumber++) {
            MeteorClient.mc.player.networkHandler.sendPacket(new OnGroundOnly(true));
        }

        MeteorClient.mc.player.networkHandler.sendPacket(new PositionAndOnGround(x2, y2, z2, true));
    }

    private static double calculateDistance(double x1, double y1, double z1, double x2, double y2, double z2) {
        double dx = x2 - x1;
        double dy = y2 - y1;
        double dz = z2 - z1;
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    public static void sendMovePacket(Vec3d vec3d, boolean onGround) {
        ClientPlayNetworkHandler networkHandler = MeteorClient.mc.player.networkHandler;
        networkHandler.sendPacket(new PositionAndOnGround(vec3d.x, vec3d.y, vec3d.z, onGround));
    }

    public static final void send(@NotNull Packet<?> packet) {
        MinecraftClient minecraftClient = MinecraftClient.getInstance();
        ClientPlayNetworkHandler clientPlayNetworkHandler = minecraftClient.getNetworkHandler();
        clientPlayNetworkHandler.sendPacket(packet);
    }

    public static void doTpXY(double fromX, double fromZ, double toX, double toZ, double moveDistance, boolean onGround) {
        double squareDis = Utils.squaredDistance(fromX, MeteorClient.mc.player.getY(), fromZ, toX, MeteorClient.mc.player.getY(), toZ);
        int step = (int) Math.ceil(Math.sqrt(squareDis) / moveDistance);

        for (int i = 1; i <= step; i++) {
            sendMovePacket(fromX, MeteorClient.mc.player.getY(), fromZ, onGround);
        }

        sendMovePacket(toX, MeteorClient.mc.player.getY(), toZ, onGround);
    }

    public static void doTp(double fromX, double fromY, double fromZ, double toX, double toY, double toZ, double moveDistance, boolean onGround) {
        double squareDis = Utils.squaredDistance(fromX, fromY, fromZ, toX, toY, toZ);
        int step = (int) Math.ceil(Math.sqrt(squareDis) / moveDistance);

        for (int i = 1; i <= step; i++) {
            sendMovePacket(fromX, fromY, fromZ, onGround);
        }

        sendMovePacket(toX, toY, toZ, onGround);
    }

    public static void doTpClient(double fromX, double fromY, double fromZ, double toX, double toY, double toZ, double moveDistance, boolean onGround) {
        double squareDis = Utils.squaredDistance(fromX, fromY, fromZ, toX, toY, toZ);
        int step = (int) Math.ceil(Math.sqrt(squareDis) / moveDistance);

        for (int i = 1; i <= step; i++) {
            sendClientMovePacket(fromX, fromY, fromZ, onGround);
        }

        sendClientMovePacket(toX, toY, toZ, onGround);
    }

    public static void doTpClient(Vec3d fromVec, Vec3d toVec, double moveDistance, boolean onGround) {
        doTpClient(fromVec.x, fromVec.y, fromVec.z, toVec.x, toVec.y, toVec.z, moveDistance, onGround);
    }

    public static void doTpClient(Vec3d vec3d, Vec3d vec3d2, double d, boolean bl, int n, Object object) {
        if ((n & 8) != 0) {
            bl = true;
        }

        doTpClient(vec3d, vec3d2, d, bl);
    }

    public static void doTp(Vec3d fromVec, Vec3d toVec, double moveDistance, boolean onGround) {
        doTp(fromVec.x, fromVec.y, fromVec.z, toVec.x, toVec.y, toVec.z, moveDistance, onGround);
    }

    public static void doTp(Vec3d vec3d, Vec3d vec3d2, double d, boolean bl, int n, Object object) {
        if ((n & 8) != 0) {
            bl = true;
        }

        doTp(vec3d, vec3d2, d, bl);
    }

    public static Vec3d findVClipVecToMove(Vec3d fromVec, Vec3d toVec, double searchStep, boolean allowVoid) {
        Vec3d findVec = null;
        double step = 0.0;
        SkyType skyType = MeteorClient.mc.world.getDimensionEffects().getSkyType();
        int voidY = skyType == null ? -1 : (skyType == SkyType.NORMAL ? -64 : 0);

        while (step <= 200.0) {
            Vec3d vec = new Vec3d(fromVec.x, fromVec.y + step, fromVec.z);
            if (!allowVoid || !(fromVec.y <= voidY)) {
                if (BlockUtil.isSafeBlock(BlockPos.ofFloored(vec))) {
                    return vec;
                }

                vec = new Vec3d(fromVec.x, fromVec.y - step, fromVec.z);
                if (!allowVoid || !(fromVec.y <= voidY)) {
                    if (BlockUtil.isSafeBlock(BlockPos.ofFloored(vec))) {
                        return vec;
                    }

                    step += searchStep;
                }
            }
        }

        return findVec;
    }
}
