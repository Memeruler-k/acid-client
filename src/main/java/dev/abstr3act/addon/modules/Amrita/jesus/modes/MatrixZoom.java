package dev.abstr3act.addon.modules.Amrita.jesus.modes;

import dev.abstr3act.addon.modules.Amrita.jesus.JesusMode;
import dev.abstr3act.addon.modules.Amrita.jesus.JesusModes;
import meteordevelopment.meteorclient.events.world.TickEvent.Pre;
import meteordevelopment.meteorclient.mixininterface.IVec3d;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class MatrixZoom extends JesusMode {
    private final float range = 0.005F;

    public MatrixZoom() {
        super(JesusModes.Matrix_Zoom);
    }

    @Override
    public void onTickEventPre(Pre event) {
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

        if (this.mc
            .world
            .getBlockState(new BlockPos(this.mc.player.getBlockX(), (int) (this.mc.player.getBlockY() + 0.005F), this.mc.player.getBlockZ()))
            .getBlock()
            == Blocks.WATER
            && !this.mc.player.horizontalCollision) {
            ((IVec3d) this.mc.player.getVelocity()).set(velX, 0.0, velZ);
        }
    }
}
