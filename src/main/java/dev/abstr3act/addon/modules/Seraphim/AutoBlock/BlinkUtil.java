package dev.abstr3act.addon.modules.Seraphim.AutoBlock;

import dev.abstr3act.addon.utils.fragment.blinkUtils.PacketUtil;
import meteordevelopment.meteorclient.MeteorClient;
import net.minecraft.entity.EntityPose;
import net.minecraft.network.packet.Packet;
import net.minecraft.util.math.Vec3d;

import java.util.LinkedList;
import java.util.List;

public class BlinkUtil {
    private static final List<Packet<?>> packets = new LinkedList<>();
    private static final List<Vec3d> positions = new LinkedList<>();
    public static boolean limiter = false;
    public static boolean blinking = false;
    private static Double prevYMotion = null;
    private static boolean isStarted = false;

    public static void addPacket(Packet<?> packet) {
        packets.add(packet);
    }

    public static void doBlink() {
        blinking = true;
        if (prevYMotion == null) {
            prevYMotion = MeteorClient.mc.player.getVelocity().y;
        }

        if (!isStarted) {
            synchronized (positions) {
                positions.add(
                    new Vec3d(
                        MeteorClient.mc.player.getX(),
                        MeteorClient.mc.player.getBoundingBox().minY + MeteorClient.mc.player.getEyeHeight(EntityPose.STANDING) / 2.0F,
                        MeteorClient.mc.player.getZ()
                    )
                );
                positions.add(new Vec3d(MeteorClient.mc.player.getX(), MeteorClient.mc.player.getBoundingBox().minY, MeteorClient.mc.player.getZ()));
            }

            isStarted = true;
        } else {
            synchronized (positions) {
                positions.add(new Vec3d(MeteorClient.mc.player.getX(), MeteorClient.mc.player.getY(), MeteorClient.mc.player.getZ()));
            }
        }
    }

    public static void sync(boolean blinkSync, boolean noSyncResetPos) {
        if (blinkSync) {
            try {
                limiter = true;

                while (!packets.isEmpty()) {
                    PacketUtil.sendPacket(packets.remove(0));
                }
            } catch (Exception var19) {
                limiter = false;
            } finally {
                limiter = false;
            }

            synchronized (positions) {
                positions.clear();
            }
        } else {
            try {
                limiter = true;
                packets.clear();
            } catch (Exception var16) {
                limiter = false;
            } finally {
                limiter = false;
            }

            if (noSyncResetPos) {
                if (!positions.isEmpty() && positions.size() > 1) {
                    MeteorClient.mc.player.setPosition(positions.get(1));
                }

                if (prevYMotion != null) {
                    setVelocityY(prevYMotion);
                }
            }
        }
    }

    public static void setVelocityY(Double y) {
        if (MeteorClient.mc.player != null) {
            MeteorClient.mc.player.setVelocity(MeteorClient.mc.player.getVelocity().x, y, MeteorClient.mc.player.getVelocity().z);
        }
    }

    public static void stopBlink() {
        positions.clear();
        prevYMotion = null;
        isStarted = false;
        blinking = false;
    }
}
