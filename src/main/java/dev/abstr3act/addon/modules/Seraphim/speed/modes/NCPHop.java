package dev.abstr3act.addon.modules.Seraphim.speed.modes;

import dev.abstr3act.addon.modules.Seraphim.speed.SpeedMode;
import dev.abstr3act.addon.modules.Seraphim.speed.SpeedModes;
import meteordevelopment.meteorclient.events.world.TickEvent.Pre;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.world.Timer;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;

public class NCPHop extends SpeedMode {
    public NCPHop() {
        super(SpeedModes.NCP_Hop);
    }

    @Override
    public void onActivate() {
        ((Timer) Modules.get().get(Timer.class)).setOverride(1.0865F);
    }

    @Override
    public void onDeactivate() {
        ((Timer) Modules.get().get(Timer.class)).setOverride(1.0);
        this.mc.player.getAbilities().setFlySpeed(0.02F);
    }

    @Override
    public void onTickEventPre(Pre event) {
        if (!this.mc.player.isTouchingWater() && !this.mc.player.isInLava() && !this.mc.player.isClimbing() && !this.mc.player.isRiding()) {
            Timer timer = (Timer) Modules.get().get(Timer.class);
            if (PlayerUtils.isMoving() && this.mc.player.isOnGround()) {
                this.mc.player.jump();
                this.mc.player.getAbilities().setFlySpeed(0.0223F);
            } else {
                timer.setOverride(1.0);
            }
        }
    }
}
