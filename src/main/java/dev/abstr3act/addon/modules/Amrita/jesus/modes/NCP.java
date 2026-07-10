package dev.abstr3act.addon.modules.Amrita.jesus.modes;

import dev.abstr3act.addon.modules.Amrita.jesus.JesusMode;
import dev.abstr3act.addon.modules.Amrita.jesus.JesusModes;
import meteordevelopment.meteorclient.events.entity.player.PlayerMoveEvent;
import meteordevelopment.meteorclient.mixininterface.IVec3d;
import net.minecraft.block.AirBlock;
import net.minecraft.util.math.Vec3d;

public class NCP extends JesusMode {
    float newSpeed = 0.0F;

    public NCP() {
        super(JesusModes.NCP);
    }

    @Override
    public void onPlayerMoveEvent(PlayerMoveEvent event) {
        this.mc.player.setSprinting(false);
        if (this.mc.player.isInFluid()) {
            Vec3d velocity = this.mc.player.getVelocity();
            if (this.mc.options.jumpKey.isPressed()
                && !this.mc.player.isSneaking()
                && !(this.mc.world.getBlockState(this.mc.player.getBlockPos().add(0, 1, 0)).getBlock() instanceof AirBlock)) {
                this.mc.player.setVelocity(velocity.x, 0.12, velocity.z);
            }

            velocity = this.mc.player.getVelocity();
            if (this.mc.options.sneakKey.isPressed()) {
                this.mc.player.setVelocity(velocity.x, -0.12, velocity.z);
            }

            velocity = this.mc.player.getVelocity();
            if (this.mc.world.getBlockState(this.mc.player.getBlockPos().add(0, 1, 0)).getBlock() instanceof AirBlock && this.mc.options.jumpKey.isPressed()) {
                this.mc.player.setSneaking(true);
                this.mc.player.setVelocity(velocity.x, 0.12, velocity.z);
            }

            float yaw = this.mc.player.getYaw();
            Vec3d forward = Vec3d.fromPolar(0.0F, yaw);
            Vec3d right = Vec3d.fromPolar(0.0F, yaw + 90.0F);
            double velX = 0.0;
            double velZ = 0.0;
            double s = 0.5;
            double speedValue = this.settings.speed.get();
            if (this.mc.options.forwardKey.isPressed()) {
                velX += forward.x * s * speedValue;
                velZ += forward.z * s * speedValue;
            }

            if (this.mc.options.backKey.isPressed()) {
                velX -= forward.x * s * speedValue;
                velZ -= forward.z * s * speedValue;
            }

            if (this.mc.options.rightKey.isPressed()) {
                velX += right.x * s * speedValue;
                velZ += right.z * s * speedValue;
            }

            if (this.mc.options.leftKey.isPressed()) {
                velX -= right.x * s * speedValue;
                velZ -= right.z * s * speedValue;
            }

            if (velX >= (this.settings.limit_speed.get()).floatValue()) {
                velX = (this.settings.limit_speed.get()).floatValue();
            }

            if (velZ >= (this.settings.limit_speed.get()).floatValue()) {
                velZ = (this.settings.limit_speed.get()).floatValue();
            }

            ((IVec3d) this.mc.player.getVelocity()).set(velX, 0.0, velZ);
            this.mc.player.setSneaking(true);
        }
    }

    @Override
    public void onDeactivate() {
        this.newSpeed = 0.6F;
        super.onDeactivate();
    }

    @Override
    public void onActivate() {
        this.newSpeed = 0.6F;
        this.newSpeed = this.newSpeed + (this.settings.speed.get()).floatValue();
        super.onActivate();
    }

    public void setMotion(double motion) {
        float forward = this.mc.player.forwardSpeed;
        float yaw = this.mc.player.getYaw();
        if (forward == 0.0F) {
            ((IVec3d) this.mc.player.getVelocity()).set(0.0, this.mc.player.getVelocity().y, 0.0);
        } else {
            double x = forward * motion * Math.cos(Math.toRadians(yaw + 90.0F)) * motion * Math.sin(Math.toRadians(yaw + 90.0F));
            double z = forward * motion * Math.sin(Math.toRadians(yaw + 90.0F)) * motion * Math.cos(Math.toRadians(yaw + 90.0F));
            ((IVec3d) this.mc.player.getVelocity()).set(x, this.mc.player.getVelocity().y, z);
        }
    }
}
