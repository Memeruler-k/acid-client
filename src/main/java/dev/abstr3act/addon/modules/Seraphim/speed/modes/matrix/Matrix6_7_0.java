package dev.abstr3act.addon.modules.Seraphim.speed.modes.matrix;

import dev.abstr3act.addon.modules.Seraphim.speed.SpeedMode;
import dev.abstr3act.addon.modules.Seraphim.speed.SpeedModes;
import dev.abstr3act.addon.utils.seraphim.movement.MovementUtils;
import meteordevelopment.meteorclient.events.packets.PacketEvent.Receive;
import meteordevelopment.meteorclient.events.world.TickEvent.Post;
import meteordevelopment.meteorclient.events.world.TickEvent.Pre;
import meteordevelopment.meteorclient.mixininterface.IVec3d;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;

public class Matrix6_7_0 extends SpeedMode {
    private int noVelocityY = 0;

    public Matrix6_7_0() {
        super(SpeedModes.Matrix_6dot7dot0);
    }

    @Override
    public void onDeactivate() {
        this.mc.player.getAbilities().setFlySpeed(0.02F);
    }

    @Override
    public void onTickEventPre(Pre event) {
        this.work();
    }

    @Override
    public void onTickEventPost(Post event) {
    }

    @Override
    public void onReceivePacket(Receive event) {
        if (event.packet instanceof EntityVelocityUpdateS2CPacket velocity
            && this.mc.player != null
            && this.mc.world != null
            && this.mc.world.getEntityById(velocity.getEntityId()) != null
            && this.mc.player == this.mc.world.getEntityById(velocity.getEntityId())) {
            this.noVelocityY = 10;
        }
    }

    private void work() {
        if (!this.mc.player.isOnGround() && this.noVelocityY <= 0) {
            if (this.mc.player.getVelocity().y > 0.0) {
                this.mc.player.getVelocity().add(0.0, -5.0E-4, 0.0);
            }

            this.mc.player.getVelocity().add(0.0, -0.009400114514191982, 0.0);
        }

        if (!this.mc.player.isOnGround() && this.noVelocityY < 8 && MovementUtils.getSpeed() < 0.2177 && this.noVelocityY < 8) {
            MovementUtils.strafe(0.2177F);
        }

        if (Math.abs(this.mc.player.getAbilities().getFlySpeed()) < 0.1) {
            this.mc.player.getAbilities().setFlySpeed(0.026F);
        } else {
            this.mc.player.getAbilities().setFlySpeed(0.0247F);
        }

        if (this.mc.player.isOnGround() && PlayerUtils.isMoving()) {
            this.mc.options.jumpKey.setPressed(false);
            this.mc.player.jump();
            IVec3d v = (IVec3d) this.mc.player.getVelocity();
            v.setY(0.4105000114514192);
            if (Math.abs(this.mc.player.getAbilities().getFlySpeed()) < 0.1) {
                MovementUtils.strafe(MovementUtils.getSpeed());
            }
        }

        if (!PlayerUtils.isMoving()) {
            IVec3d v = (IVec3d) this.mc.player.getVelocity();
            v.setXZ(0.0, 0.0);
        }
    }
}
