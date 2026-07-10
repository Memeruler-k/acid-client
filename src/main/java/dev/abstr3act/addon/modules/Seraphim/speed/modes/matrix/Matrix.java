package dev.abstr3act.addon.modules.Seraphim.speed.modes.matrix;

import dev.abstr3act.addon.modules.Seraphim.speed.SpeedMode;
import dev.abstr3act.addon.modules.Seraphim.speed.SpeedModes;
import meteordevelopment.meteorclient.events.world.TickEvent.Pre;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.world.Timer;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;

public class Matrix extends SpeedMode {
    public Matrix() {
        super(SpeedModes.Matrix);
    }

    @Override
    public void onDeactivate() {
        ((Timer) Modules.get().get(Timer.class)).setOverride(1.0);
        if (this.mc.player != null) {
            this.mc.player.getAbilities().setFlySpeed(0.02F);
        }
    }

    @Override
    public void onTickEventPre(Pre event) {
        Timer timer = (Timer) Modules.get().get(Timer.class);
        timer.setOverride(1.0);
        if (!this.mc.player.isTouchingWater() && !this.mc.player.isInLava() && !this.mc.player.isClimbing() && !this.mc.player.isRiding()) {
            if (PlayerUtils.isMoving()) {
                if (this.mc.player.isOnGround()) {
                    this.mc.player.jump();
                    this.mc.player.getAbilities().setFlySpeed(0.02098F);
                    timer.setOverride(1.055F);
                }
            } else {
                timer.setOverride(1.0);
            }
        }
    }
}
