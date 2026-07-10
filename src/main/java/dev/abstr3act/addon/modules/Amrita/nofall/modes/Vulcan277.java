package dev.abstr3act.addon.modules.Amrita.nofall.modes;

import dev.abstr3act.addon.modules.Amrita.nofall.NoFallMode;
import dev.abstr3act.addon.modules.Amrita.nofall.NoFallModes;
import meteordevelopment.meteorclient.events.packets.PacketEvent.Send;
import meteordevelopment.meteorclient.mixin.PlayerMoveC2SPacketAccessor;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.Vec3d;

public class Vulcan277 extends NoFallMode {
    public Vulcan277() {
        super(NoFallModes.Vulcan_2dot7dot7);
    }

    @Override
    public void onSendPacket(Send event) {
        if (event.packet instanceof PlayerMoveC2SPacket packet) {
            PlayerMoveC2SPacketAccessor accessor = (PlayerMoveC2SPacketAccessor) packet;
            if (this.mc.player.fallDistance > 7.0) {
                accessor.setOnGround(true);
                this.mc.player.fallDistance = 0.0F;
                Vec3d vel = this.mc.player.getVelocity();
                this.mc.player.setVelocity(vel.x, 0.0, vel.z);
            }
        }
    }
}
