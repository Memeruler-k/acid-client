package dev.abstr3act.addon.modules.Seraphim.fly.modes;

import dev.abstr3act.addon.modules.Seraphim.fly.FlyMode;
import dev.abstr3act.addon.modules.Seraphim.fly.FlyModes;
import meteordevelopment.meteorclient.events.entity.player.SendMovementPacketsEvent.Pre;
import meteordevelopment.meteorclient.events.packets.PacketEvent.Receive;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.world.Timer;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket.PositionAndOnGround;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.util.math.Vec3d;

public class VulcanClip extends FlyMode {
    private boolean waitFlag = false;
    private boolean canGlide = false;
    private int ticks = 0;
    private Timer timer;

    public VulcanClip() {
        super(FlyModes.Vulcan_Clip);
    }

    @Override
    public void onDeactivate() {
        this.timer.setOverride(1.0);
    }

    @Override
    public void onActivate() {
        this.timer = (Timer) Modules.get().get(Timer.class);
        if (this.mc.player.isOnGround() && this.settings.canClip.get()) {
            this.clip(0.0F, -0.1F);
            this.waitFlag = true;
            this.canGlide = false;
            this.ticks = 0;
            this.timer.setOverride(0.1F);
        } else {
            this.waitFlag = false;
            this.canGlide = true;
        }
    }

    @Override
    public void onPlayerMoveSendPre(Pre event) {
        if (this.canGlide) {
            this.timer.setOverride(1.0);
            Vec3d velocity = this.mc.player.getVelocity();
            velocity.add(0.0, -(this.ticks % 2 == 0 ? 0.17 : 0.1), 0.0);
            if (this.ticks == 0) {
                velocity.add(0.0, -0.07, 0.0);
            }

            this.mc.player.setVelocity(velocity);
            this.ticks++;
        }
    }

    @Override
    public void onRecivePacket(Receive event) {
        super.onRecivePacket(event);
        if (event.packet instanceof PlayerPositionLookS2CPacket && this.waitFlag) {
            PlayerPositionLookS2CPacket packet = (PlayerPositionLookS2CPacket) event.packet;
            Vec3d playerPos = this.mc.player.getPos();
            this.waitFlag = false;
            this.mc.player.setPosition(packet.getX(), packet.getY(), packet.getZ());
            this.mc.player.networkHandler.sendPacket(new PositionAndOnGround(playerPos.x, playerPos.y, playerPos.z, false));
            event.cancel();
            this.mc.player.jump();
            this.clip(0.127318F, 0.0F);
            this.clip(3.425559F, 3.7F);
            this.clip(3.14285F, 3.54F);
            this.clip(2.88522F, 3.4F);
            this.canGlide = true;
        }
    }

    private void clip(float dist, float y) {
        float tickDelta = this.mc.getRenderTickCounter().getTickDelta(true);
        double yaw = Math.toRadians(this.mc.player.getYaw(tickDelta));
        double x = -Math.sin(yaw) * dist;
        double z = Math.cos(yaw) * dist;
        this.mc.player.setPosition(this.mc.player.getPos().x + x, this.mc.player.getPos().y + y, this.mc.player.getPos().z + z);
        this.mc.player.networkHandler.sendPacket(new PositionAndOnGround(this.mc.player.getX(), this.mc.player.getY(), this.mc.player.getZ(), false));
    }
}
