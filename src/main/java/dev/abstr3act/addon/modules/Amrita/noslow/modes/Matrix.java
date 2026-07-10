package dev.abstr3act.addon.modules.Amrita.noslow.modes;

import dev.abstr3act.addon.modules.Amrita.noslow.NoSlowMode;
import dev.abstr3act.addon.modules.Amrita.noslow.NoSlowModes;
import meteordevelopment.meteorclient.events.world.TickEvent.Pre;
import net.minecraft.util.math.Vec3d;

public class Matrix extends NoSlowMode {
    private int ticks = 0;

    public Matrix() {
        super(NoSlowModes.Matrix);
    }

    @Override
    public void onActivate() {
        this.ticks = 0;
    }

    @Override
    public void onTickEventPre(Pre event) {
        if (this.mc.player.isUsingItem() && this.mc.player.isOnGround() && this.ticks % 2 == 0) {
            float speed = 0.4F;
            Vec3d vel = this.mc.player.getVelocity();
            double x = vel.getX() * speed;
            double z = vel.getZ() * speed;
            this.mc.player.setVelocity(x, vel.getY(), z);
        }

        this.ticks++;
    }
}
