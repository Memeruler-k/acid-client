package dev.abstr3act.addon.modules.Amrita.nofall.modes;

import dev.abstr3act.addon.modules.Amrita.nofall.NoFallMode;
import dev.abstr3act.addon.modules.Amrita.nofall.NoFallModes;
import meteordevelopment.meteorclient.events.packets.PacketEvent.Send;
import meteordevelopment.meteorclient.mixin.PlayerMoveC2SPacketAccessor;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.world.Timer;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.shape.VoxelShape;

import java.util.Iterator;

public class MatrixNew extends NoFallMode {
    private Timer timer;

    public MatrixNew() {
        super(NoFallModes.Matrix_New);
    }

    @Override
    public void onDeactivate() {
        this.timer = (Timer) Modules.get().get(Timer.class);
        this.timer.setOverride(1.0);
    }

    @Override
    public void onSendPacket(Send event) {
        if (event.packet instanceof PlayerMoveC2SPacket packet) {
            PlayerMoveC2SPacketAccessor accessor = (PlayerMoveC2SPacketAccessor) packet;
            this.timer = (Timer) Modules.get().get(Timer.class);
            if (!this.mc.player.isOnGround()) {
                if (this.mc.player.fallDistance > 2.69) {
                    this.timer.setOverride(0.3);
                    accessor.setOnGround(true);
                    this.mc.player.fallDistance = 0.0F;
                }

                if (this.mc.player.fallDistance > 3.5) {
                    this.timer.setOverride(0.3);
                } else {
                    this.timer.setOverride(1.0);
                }
            }

            Iterator<VoxelShape> voxelShapeIterator = this.mc
                .world
                .getCollisions(this.mc.player, this.mc.player.getBoundingBox().offset(0.0, this.mc.player.getVelocity().y, 0.0))
                .iterator();
            boolean isEmpty = true;

            while (voxelShapeIterator.hasNext()) {
                VoxelShape shape = voxelShapeIterator.next();
                isEmpty = shape.isEmpty();
            }

            if (!isEmpty && !((PlayerMoveC2SPacket) event.packet).isOnGround() && this.mc.player.getVelocity().y < -0.6) {
                accessor.setOnGround(true);
            }
        }
    }
}
