package dev.abstr3act.addon.modules.Amrita.jesus.modes;

import dev.abstr3act.addon.modules.Amrita.jesus.JesusMode;
import dev.abstr3act.addon.modules.Amrita.jesus.JesusModes;
import meteordevelopment.meteorclient.events.world.TickEvent.Pre;
import meteordevelopment.meteorclient.mixininterface.IVec3d;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class MatrixZoom2 extends JesusMode {
    private final float range = 0.005F;
    private int tick = 0;

    public MatrixZoom2() {
        super(JesusModes.Matrix_Zoom_2);
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
            .getBlockState(new BlockPos((int) this.mc.player.getPos().x, (int) (this.mc.player.getPos().y + 0.005F), (int) this.mc.player.getPos().z))
            .getBlock()
            == Blocks.WATER
            && !this.mc.player.horizontalCollision) {
            if (this.tick == 0) {
                ((IVec3d) this.mc.player.getVelocity()).set(velX, 0.030091, velZ);
            } else if (this.tick == 1) {
                ((IVec3d) this.mc.player.getVelocity()).set(velX, -0.030091, velZ);
            }
        }
    }
}
