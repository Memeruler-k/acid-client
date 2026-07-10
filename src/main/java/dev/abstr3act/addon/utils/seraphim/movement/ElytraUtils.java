package dev.abstr3act.addon.utils.seraphim.movement;

import meteordevelopment.meteorclient.MeteorClient;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket.Mode;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;

public class ElytraUtils {
    public static void startFly() {
        if (MeteorClient.mc.player != null && MeteorClient.mc.player.networkHandler != null) {
            MeteorClient.mc.player.networkHandler.sendPacket(new ClientCommandC2SPacket(MeteorClient.mc.player, Mode.START_FALL_FLYING));
        }
    }

    public static void fakeInventoryOpen(boolean open) {
        if (MeteorClient.mc.player != null && MeteorClient.mc.player.networkHandler != null) {
            if (open) {
                MeteorClient.mc.player.networkHandler.sendPacket(new ClientCommandC2SPacket(MeteorClient.mc.player, Mode.OPEN_INVENTORY));
            } else {
                MeteorClient.mc.player.networkHandler.sendPacket(new CloseHandledScreenC2SPacket(0));
            }
        }
    }
}
