package dev.abstr3act.addon.events.legacy;

import meteordevelopment.meteorclient.events.Cancellable;
import net.minecraft.network.packet.Packet;

public class ModifyPacketEvent extends Cancellable {
    public Packet<?> packet;

    public ModifyPacketEvent(Packet<?> packet) {
        this.packet = packet;
    }
}
