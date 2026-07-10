package dev.abstr3act.addon.mixin;

import dev.abstr3act.addon.events.EventPacket;
import dev.abstr3act.addon.events.TransferOrigin;
import dev.abstr3act.addon.modules.Amrita.AntiExceptions;
import dev.abstr3act.addon.utils.abnormally.AChatUtils;
import io.netty.channel.ChannelHandlerContext;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.OffThreadException;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BundleS2CPacket;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({ClientConnection.class})
public class MixinClientConnection {
    @Shadow
    protected static <T extends PacketListener> void handlePacket(Packet<T> packet, PacketListener listener) {
    }

    @Inject(
        method = {"handlePacket"},
        at = {@At("HEAD")},
        cancellable = true,
        require = 1
    )
    private static void hookReceivingPacket(Packet<?> packet, PacketListener listener, CallbackInfo ci) {
        if (!(packet instanceof BundleS2CPacket bundleS2CPacket)) {
            EventPacket event = new EventPacket(TransferOrigin.RECEIVE, packet, true);
            MeteorClient.EVENT_BUS.post(event);
            if (event.isCancelled()) {
                ci.cancel();
            }
        } else {
            ci.cancel();

            for (Packet<?> packetInBundle : bundleS2CPacket.getPackets()) {
                try {
                    handlePacket(packetInBundle, listener);
                } catch (OffThreadException var7) {
                }
            }
        }
    }

    @Inject(
        method = {"exceptionCaught"},
        at = {@At("HEAD")},
        cancellable = true
    )
    private void exceptionCaughtHook(ChannelHandlerContext context, Throwable t, CallbackInfo ci) {
        if (((AntiExceptions) Modules.get().get(AntiExceptions.class)).isActive()) {
            AChatUtils.sendMsgAmrita(Text.of("Exception detected: " + t.getMessage()));
            ci.cancel();
        }
    }

    @Inject(
        method = {"send(Lnet/minecraft/network/packet/Packet;)V"},
        at = {@At("HEAD")},
        cancellable = true
    )
    private void hookSendingPacket(Packet<?> packet, CallbackInfo callbackInfo) {
        EventPacket event = new EventPacket(TransferOrigin.SEND, packet, true);
        MeteorClient.EVENT_BUS.post(event);
        if (event.isCancelled()) {
            callbackInfo.cancel();
        }
    }
}
