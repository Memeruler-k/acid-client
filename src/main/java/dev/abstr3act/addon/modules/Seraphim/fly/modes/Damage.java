package dev.abstr3act.addon.modules.Seraphim.fly.modes;

import dev.abstr3act.addon.modules.Seraphim.fly.FlyMode;
import dev.abstr3act.addon.modules.Seraphim.fly.FlyModes;
import meteordevelopment.meteorclient.events.entity.DamageEvent;
import meteordevelopment.meteorclient.events.world.TickEvent.Pre;
import meteordevelopment.meteorclient.mixininterface.IVec3d;
import net.minecraft.util.math.Vec3d;

public class Damage extends FlyMode {
    public static int workingTicks = 15;
    public static int workingUpTicks = 0;
    public static double speed = 0.0;
    public static double speedUp = 0.0;
    private int ticks = 0;
    private int ticks_up = 0;
    private boolean damaged = false;

    public Damage() {
        super(FlyModes.Damage);
    }

    @Override
    public void onActivate() {
        this.damaged = false;
        this.ticks = 0;
        this.ticks_up = 0;
    }

    @Override
    public void onTickEventPre(Pre event) {
        if (this.damaged && this.ticks != workingTicks) {
            float yaw = this.mc.player.getYaw();
            Vec3d forward = Vec3d.fromPolar(0.0F, yaw);
            double velX = 0.0;
            double velZ = 0.0;
            double s = speed;
            if (this.mc.options.forwardKey.isPressed()) {
                velX += forward.x * s;
                velZ += forward.z * s;
            }

            if (this.mc.options.backKey.isPressed()) {
                velX -= forward.x * s;
                velZ -= forward.z * s;
            }

            if (this.ticks_up < workingUpTicks) {
                ((IVec3d) this.mc.player.getVelocity()).set(velX, speedUp, velZ);
            } else {
                ((IVec3d) this.mc.player.getVelocity()).set(velX, 0.0, velZ);
            }

            this.ticks++;
            this.ticks_up++;
        } else if (this.damaged) {
            this.damaged = false;
            this.ticks = 0;
            this.ticks_up = 0;
        }
    }

    @Override
    public void onDamage(DamageEvent event) {
        if (event.entity == this.mc.player) {
            this.damaged = true;
            this.ticks = 0;
            this.ticks_up = 0;
        }
    }
}
