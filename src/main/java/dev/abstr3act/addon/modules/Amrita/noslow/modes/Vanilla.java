package dev.abstr3act.addon.modules.Amrita.noslow.modes;

import dev.abstr3act.addon.events.PlayerUseMultiplierEvent;
import dev.abstr3act.addon.modules.Amrita.noslow.NoSlowMode;
import dev.abstr3act.addon.modules.Amrita.noslow.NoSlowModes;

public class Vanilla extends NoSlowMode {
    public Vanilla() {
        super(NoSlowModes.Vanilla);
    }

    @Override
    public void onUse(PlayerUseMultiplierEvent event) {
        if (this.mc.player.isSneaking()) {
            event.setForward((this.settings.sneakForward.get()).floatValue());
            event.setSideways((this.settings.sneakSideways.get()).floatValue());
        } else if (this.mc.player.isUsingItem()) {
            event.setForward((this.settings.usingForward.get()).floatValue());
            event.setSideways((this.settings.usingSideways.get()).floatValue());
        } else {
            event.setForward((this.settings.otherForward.get()).floatValue());
            event.setSideways((this.settings.otherSideways.get()).floatValue());
        }
    }
}
