package dev.abstr3act.addon.modules.Amrita;

import dev.abstr3act.addon.Compassion;
import dev.abstr3act.addon.module.CompassionModule;
import meteordevelopment.meteorclient.events.render.Render2DEvent;
import meteordevelopment.meteorclient.settings.BoolSetting.Builder;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket.Mode;

public final class SneakDerp extends CompassionModule {
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
    private final Setting<Boolean> client = this.sgGeneral
        .add(((Builder) ((Builder) ((Builder) new Builder().name("Client")).description(".")).defaultValue(true)).build());
    private final Setting<Integer> delay = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("Delay"))
                .description("."))
                .defaultValue(1))
                .min(0)
                .max(2000)
                .build()
        );
    int i;
    int j;

    public SneakDerp() {
        super(Compassion.AMRITA, "SneakDerp", ".");
    }

    @EventHandler
    public void onSync(Render2DEvent event) {
        if (this.i <= 0) {
            this.sendPacket(new ClientCommandC2SPacket(this.mc.player, Mode.PRESS_SHIFT_KEY));
            this.i = this.delay.get();
            this.j--;
        } else {
            this.i--;
        }

        if (this.j <= 0) {
            this.sendPacket(new ClientCommandC2SPacket(this.mc.player, Mode.RELEASE_SHIFT_KEY));
            this.j = this.delay.get();
        }
    }

    public void onDeactivate() {
    }
}
