package dev.abstr3act.addon.utils.fragment.blinkUtils;

import meteordevelopment.meteorclient.MeteorClient;
import net.minecraft.network.packet.Packet;

import java.util.ArrayList;

public class PacketUtil {
    public static ArrayList<Packet<?>> silentPackets = new ArrayList<>();

    public static void sendPacket(Packet<?> packet) {
        if (MeteorClient.mc.getNetworkHandler() != null) {
            MeteorClient.mc.getNetworkHandler().sendPacket(packet);
        }
    }

    public static void sendPacketAsSilent(Packet<?> packet) {
        if (MeteorClient.mc.getNetworkHandler() != null) {
            silentPackets.add(packet);
            MeteorClient.mc.getNetworkHandler().sendPacket(packet);
        }
    }
}
