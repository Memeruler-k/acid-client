package dev.abstr3act.addon.modules.Seraphim.speed.modes;

import dev.abstr3act.addon.modules.Seraphim.speed.SpeedMode;
import dev.abstr3act.addon.modules.Seraphim.speed.SpeedModes;
import meteordevelopment.meteorclient.events.world.TickEvent.Pre;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.world.Timer;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;

public class AACHop438 extends SpeedMode {
    public AACHop438() {
        super(SpeedModes.AAC_Hop_4dot3dot8);
    }

    @Override
    public void onDeactivate() {
        ((Timer) Modules.get().get(Timer.class)).setOverride(1.0);
    }

    @Override
    public void onTickEventPre(Pre event) {
        Timer timer = (Timer) Modules.get().get(Timer.class);
        timer.setOverride(1.0);
        if (PlayerUtils.isMoving() && !this.mc.player.isTouchingWater() && !this.mc.player.isInLava() && !this.mc.player.isClimbing() && !this.mc.player.isRiding()
        ) {
            if (this.mc.player.isOnGround()) {
                this.mc.player.jump();
            } else if (this.mc.player.fallDistance <= 0.1) {
                timer.setOverride(1.5);
            } else if (this.mc.player.fallDistance < 1.3) {
                timer.setOverride(0.7);
            } else {
                timer.setOverride(1.0);
            }
        }
    }
}
