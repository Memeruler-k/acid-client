package dev.abstr3act.addon.modules.Amrita.velocity.modes;

import dev.abstr3act.addon.modules.Amrita.velocity.VelocityMode;
import dev.abstr3act.addon.modules.Amrita.velocity.VelocityModes;
import meteordevelopment.meteorclient.events.packets.PacketEvent.Receive;
import meteordevelopment.meteorclient.events.packets.PacketEvent.Send;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ExplosionS2CPacket;

public class GrimSkip extends VelocityMode {
    private int skip = 0;
    private boolean canCancel = false;

    public GrimSkip() {
        super(VelocityModes.Grim_Skip);
    }

    @Override
    public void onActivate() {
        this.canCancel = false;
        this.skip = 0;
    }

    @Override
    public void onDeactivate() {
        this.canCancel = false;
        this.skip = 0;
    }

    @Override
    public void onReceivePacket(Receive event) {
        Packet<?> packet = event.packet;
        if ((
            packet instanceof EntityVelocityUpdateS2CPacket && ((EntityVelocityUpdateS2CPacket) packet).getEntityId() == this.mc.player.getId()
                || packet instanceof ExplosionS2CPacket
        )
            && this.canCancel) {
            this.skip = 6;
            event.cancel();
        }
    }

    @Override
    public void onSendPacket(Send event) {
        Packet<?> packet = event.packet;
        if (packet instanceof PlayerMoveC2SPacket && this.skip > 0) {
            this.skip--;
            event.cancel();
        }
    }
}
