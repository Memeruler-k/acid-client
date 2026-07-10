package dev.abstr3act.addon.modules.Seraphim;

import dev.abstr3act.addon.Compassion;
import dev.abstr3act.addon.events.EventPlayerUpdate;
import dev.abstr3act.addon.module.SeraphimModule;
import meteordevelopment.meteorclient.settings.IntSetting.Builder;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.item.*;

public class RapidFire extends SeraphimModule {
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
    private final Setting<Integer> swapDelay = this.sgGeneral
        .add(((Builder) ((Builder) ((Builder) new Builder().name("swapDelay (ms)")).description(".")).defaultValue(100)).range(1, 2000).sliderRange(1, 2000).build());
    private final Setting<Integer> delay = this.sgGeneral
        .add(((Builder) ((Builder) ((Builder) new Builder().name("delay (tick)")).description(".")).defaultValue(5)).range(1, 60).sliderRange(1, 60).build());
    String status = "Standby";
    int i = 0;

    public RapidFire() {
        super(Compassion.SERAPHIM, "RapidFire", ".");
    }

    @EventHandler
    public void onUpdate(EventPlayerUpdate event) {
        if (this.i < this.delay.get()) {
            this.i++;
        } else {
            if (this.mc.options.useKey.isPressed()
                && this.mc.player.getMainHandStack().getItem() instanceof ToolItem
                && !(this.mc.player.getMainHandStack().getItem() instanceof AxeItem)
                && !(this.mc.player.getMainHandStack().getItem() instanceof SwordItem)
                && !this.mc.player.getMainHandStack().isEmpty()) {
                if (this.mc.player.getMainHandStack().getDamage() > 0) {
                    this.status = "Reloading";
                    return;
                }

                this.status = "Standby";
                wait(() -> InvUtils.swap(InvUtils.find(new Item[]{Items.AIR}).slot(), true), (this.swapDelay.get()).longValue())
                    .thenRun(() -> wait(InvUtils::swapBack, (this.swapDelay.get()).longValue()));
            }

            this.i = 0;
        }
    }

    public String getInfoString() {
        return this.status;
    }
}
