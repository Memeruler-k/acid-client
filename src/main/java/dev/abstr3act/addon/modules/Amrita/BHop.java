package dev.abstr3act.addon.modules.Amrita;

import dev.abstr3act.addon.Compassion;
import dev.abstr3act.addon.module.AmritaModule;
import meteordevelopment.meteorclient.events.world.TickEvent.Pre;
import meteordevelopment.meteorclient.settings.DoubleSetting.Builder;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.math.Vec3d;

public class BHop extends AmritaModule {
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
    private final Setting<Double> AIR_ACCEL = this.sgGeneral
        .add(((Builder) ((Builder) new Builder().name("Air Accel")).sliderRange(0.0, 5.0).description("")).defaultValue(0.02).build());
    private final Setting<Double> MAX_SPEED = this.sgGeneral
        .add(((Builder) ((Builder) new Builder().name("MaxSpeed")).sliderRange(0.0, 5.0).description("")).defaultValue(0.5).build());

    public BHop() {
        super(Compassion.AMRITA, "BHop", "CSGO BHop");
    }

    @EventHandler
    private void onTickEvent(Pre event) {
        this.applyBHop(this.mc.player);
    }

    private void applyBHop(ClientPlayerEntity player) {
        boolean isMoving = player.input.pressingForward || player.input.pressingBack || player.input.pressingLeft || player.input.pressingRight;
        if (player.isOnGround() && player.input.pressingForward) {
            player.jump();
        }

        if (!player.isOnGround() && isMoving) {
            this.applyAirAcceleration();
        }
    }

    private void applyAirAcceleration() {
        Vec3d velocity = this.mc.player.getVelocity();
        double speed = Math.sqrt(velocity.x * velocity.x + velocity.z * velocity.z);
        double moveX = 0.0;
        double moveZ = 0.0;
        if (this.mc.player.input.pressingForward) {
            moveZ++;
        }

        if (this.mc.player.input.pressingBack) {
            moveZ--;
        }

        if (this.mc.player.input.pressingLeft) {
            moveX--;
        }

        if (this.mc.player.input.pressingRight) {
            moveX++;
        }

        if (moveX != 0.0 || moveZ != 0.0) {
            double yaw = Math.toRadians(this.mc.player.getYaw());
            double cosYaw = Math.cos(yaw);
            double sinYaw = Math.sin(yaw);
            double accelX = (moveZ * -sinYaw + moveX * -cosYaw) * this.AIR_ACCEL.get();
            double accelZ = (moveZ * cosYaw + moveX * -sinYaw) * this.AIR_ACCEL.get();
            if (speed < this.MAX_SPEED.get()) {
                this.mc.player.addVelocity(accelX, 0.0, accelZ);
            }
        }
    }
}
