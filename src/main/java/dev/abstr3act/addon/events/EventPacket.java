package dev.abstr3act.addon.events;

import meteordevelopment.meteorclient.events.Cancellable;
import net.minecraft.network.packet.Packet;

public class EventPacket extends Cancellable {
    private final TransferOrigin origin;
    private final Packet<?> packet;
    private final boolean original;

    public EventPacket(TransferOrigin origin, Packet<?> packet, boolean original) {
        this.origin = origin;
        this.packet = packet;
        this.original = original;
    }

    public TransferOrigin getOrigin() {
        return this.origin;
    }

    public Packet<?> getPacket() {
        return this.packet;
    }

    public boolean isOriginal() {
        return this.original;
    }
}
