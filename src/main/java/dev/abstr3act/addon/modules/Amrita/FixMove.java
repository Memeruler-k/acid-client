package dev.abstr3act.addon.modules.Amrita;

import dev.abstr3act.addon.Compassion;
import dev.abstr3act.addon.events.EventKeyboardInput;
import dev.abstr3act.addon.module.AmritaModule;
import dev.abstr3act.addon.utils.RotationUtil;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.orbit.EventHandler;

public final class FixMove extends AmritaModule {
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();

    public FixMove() {
        super(Compassion.AMRITA, "FixMove", ".");
    }

    @EventHandler
    private void onMove(EventKeyboardInput event) {
        float yaw = this.mc.player.getYaw();
        RotationUtil.fixMovement(yaw);
    }

    public void onDeactivate() {
    }
}
