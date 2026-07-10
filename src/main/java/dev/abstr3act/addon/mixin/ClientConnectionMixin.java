package dev.abstr3act.addon.mixin;

import dev.abstr3act.addon.events.legacy.ModifyPacketEvent;
import meteordevelopment.meteorclient.MeteorClient;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.network.packet.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin({ClientConnection.class})
public abstract class ClientConnectionMixin {
    @Shadow
    private static <T extends PacketListener> void handlePacket(Packet<T> packet, PacketListener listener) {
    }

    @ModifyVariable(
        at = @At("HEAD"),
        method = {"send(Lnet/minecraft/network/packet/Packet;Lnet/minecraft/network/PacketCallbacks;)V"},
        argsOnly = true
    )
    public Packet<?> modifyPacket(Packet<?> packet) {
        ModifyPacketEvent event = new ModifyPacketEvent(packet);
        MeteorClient.EVENT_BUS.post(event);
        return event.isCancelled() ? null : event.packet;
    }
}
