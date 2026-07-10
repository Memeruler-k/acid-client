package dev.abstr3act.addon.modules.Amrita.velocity.modes;

import dev.abstr3act.addon.modules.Amrita.velocity.VelocityMode;
import dev.abstr3act.addon.modules.Amrita.velocity.VelocityModes;
import meteordevelopment.meteorclient.events.packets.PacketEvent.Receive;
import meteordevelopment.meteorclient.events.packets.PacketEvent.Send;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket.Action;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket.Full;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ExplosionS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.util.math.Direction;

public class GrimCancel_v2 extends VelocityMode {
    private int skip = 0;
    private boolean canCancel = false;

    public GrimCancel_v2() {
        super(VelocityModes.Grim_Cancel_v2);
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
            event.cancel();
            this.canCancel = true;
        } else if (packet instanceof PlayerPositionLookS2CPacket) {
            this.skip = 3;
        }
    }

    @Override
    public void onSendPacket(Send event) {
        Packet<?> packet = event.packet;
        if (packet instanceof PlayerMoveC2SPacket) {
            this.skip--;
            if (this.canCancel && this.skip <= 0) {
                this.mc
                    .getNetworkHandler()
                    .sendPacket(
                        new Full(
                            this.mc.player.getX(),
                            this.mc.player.getY(),
                            this.mc.player.getZ(),
                            this.mc.player.getYaw(),
                            this.mc.player.getPitch(),
                            this.mc.player.isOnGround()
                        )
                    );
                this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.STOP_DESTROY_BLOCK, this.mc.player.getBlockPos(), Direction.UP));
            }
        }
    }
}
