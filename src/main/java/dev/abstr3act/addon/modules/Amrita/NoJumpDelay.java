package dev.abstr3act.addon.modules.Amrita;

import dev.abstr3act.addon.Compassion;
import dev.abstr3act.addon.events.EventPlayerUpdate;
import dev.abstr3act.addon.mixin.accessor.ILivingEntity;
import dev.abstr3act.addon.module.AmritaModule;
import meteordevelopment.meteorclient.settings.IntSetting.Builder;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.orbit.EventHandler;

public class NoJumpDelay extends AmritaModule {
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
    private final Setting<Integer> delay = this.sgGeneral
        .add(((Builder) ((Builder) ((Builder) new Builder().name("Delay")).description("")).sliderRange(0, 4).defaultValue(1)).build());

    public NoJumpDelay() {
        super(Compassion.AMRITA, "NoJumpDelay", ".");
    }

    @EventHandler
    public void onUpdate(EventPlayerUpdate event) {
        if (this.mc.player != null) {
            if (((ILivingEntity) this.mc.player).getLastJumpCooldown() > this.delay.get()) {
                ((ILivingEntity) this.mc.player).setLastJumpCooldown(this.delay.get());
            }
        }
    }
}
