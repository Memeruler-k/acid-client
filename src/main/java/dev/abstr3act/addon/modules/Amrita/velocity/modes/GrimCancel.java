package dev.abstr3act.addon.modules.Amrita.velocity.modes;

import dev.abstr3act.addon.modules.Amrita.velocity.VelocityMode;
import dev.abstr3act.addon.modules.Amrita.velocity.VelocityModes;
import meteordevelopment.meteorclient.events.packets.PacketEvent.Receive;
import meteordevelopment.meteorclient.utils.network.MeteorExecutor;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket.Action;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket.Full;
import net.minecraft.network.packet.s2c.play.EntityDamageS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ExplosionS2CPacket;

public class GrimCancel extends VelocityMode {
    private boolean canCancel = false;

    public GrimCancel() {
        super(VelocityModes.Grim_Cancel);
    }

    @Override
    public void onActivate() {
        this.canCancel = false;
    }

    @Override
    public void onDeactivate() {
        this.canCancel = false;
    }

    @Override
    public void onReceivePacket(Receive event) {
        Packet<?> packet = event.packet;
        if (packet instanceof EntityDamageS2CPacket && ((EntityDamageS2CPacket) packet).entityId() == this.mc.player.getId()) {
            this.canCancel = true;
        }

        if ((
            packet instanceof EntityVelocityUpdateS2CPacket && ((EntityVelocityUpdateS2CPacket) packet).getEntityId() == this.mc.player.getId()
                || packet instanceof ExplosionS2CPacket
        )
            && this.canCancel) {
            event.cancel();
            MeteorExecutor.execute(
                () -> {
                    try {
                        Thread.sleep(20L);
                    } catch (Exception var2x) {
                    }

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
                    this.mc
                        .getNetworkHandler()
                        .sendPacket(new PlayerActionC2SPacket(Action.STOP_DESTROY_BLOCK, this.mc.player.getBlockPos(), this.mc.player.getHorizontalFacing().getOpposite()));
                    this.canCancel = false;
                }
            );
        }
    }
}
