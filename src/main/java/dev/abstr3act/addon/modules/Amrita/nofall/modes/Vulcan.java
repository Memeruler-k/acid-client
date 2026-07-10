package dev.abstr3act.addon.modules.Amrita.nofall.modes;

import dev.abstr3act.addon.modules.Amrita.nofall.NoFallMode;
import dev.abstr3act.addon.modules.Amrita.nofall.NoFallModes;
import dev.abstr3act.addon.utils.seraphim.movement.MovementUtils;
import meteordevelopment.meteorclient.events.packets.PacketEvent.Send;
import meteordevelopment.meteorclient.events.world.TickEvent.Pre;
import meteordevelopment.meteorclient.mixin.PlayerMoveC2SPacketAccessor;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

public class Vulcan extends NoFallMode {
    private boolean vulCanNoFall = false;
    private boolean vulCantNoFall = false;
    private boolean nextSpoof = false;
    private boolean doSpoof = false;

    public Vulcan() {
        super(NoFallModes.Vulcan);
    }

    @Override
    public void onActivate() {
        this.vulCanNoFall = false;
        this.vulCantNoFall = false;
        this.nextSpoof = false;
        this.doSpoof = false;
    }

    @Override
    public void onTickEventPre(Pre event) {
        if (!this.vulCanNoFall && this.mc.player.fallDistance > 3.25) {
            this.vulCanNoFall = true;
        }

        if (this.vulCanNoFall && this.mc.player.isOnGround() && this.vulCantNoFall) {
            this.vulCantNoFall = false;
        }

        if (!this.vulCantNoFall) {
            if (this.nextSpoof) {
                this.mc.player.getVelocity().add(0.0, -0.1, 0.0);
                this.mc.player.fallDistance = -0.1F;
                MovementUtils.strafe(0.3F);
                this.nextSpoof = false;
            }

            if (this.mc.player.fallDistance > 3.5625F) {
                this.mc.player.fallDistance = 0.0F;
                this.doSpoof = true;
                this.nextSpoof = true;
            }
        }
    }

    @Override
    public void onSendPacket(Send event) {
        if (event.packet instanceof PlayerMoveC2SPacket packet) {
            PlayerMoveC2SPacketAccessor accessor = (PlayerMoveC2SPacketAccessor) packet;
            accessor.setOnGround(true);
            this.doSpoof = false;
            accessor.setY(Math.round(this.mc.player.getPos().y * 2.0) / 2.0);
            this.mc.player.setPosition(this.mc.player.getPos().x, ((PlayerMoveC2SPacket) event.packet).getY(this.mc.player.getPos().y), this.mc.player.getPos().z);
        }
    }
}
