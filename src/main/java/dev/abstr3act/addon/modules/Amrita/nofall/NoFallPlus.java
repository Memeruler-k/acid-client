package dev.abstr3act.addon.modules.Amrita.nofall;

import dev.abstr3act.addon.Compassion;
import dev.abstr3act.addon.module.AmritaModule;
import dev.abstr3act.addon.modules.Amrita.nofall.modes.*;
import meteordevelopment.meteorclient.events.packets.PacketEvent.Send;
import meteordevelopment.meteorclient.events.packets.PacketEvent.Sent;
import meteordevelopment.meteorclient.events.world.TickEvent.Post;
import meteordevelopment.meteorclient.events.world.TickEvent.Pre;
import meteordevelopment.meteorclient.settings.EnumSetting.Builder;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.orbit.EventHandler;

public class NoFallPlus extends AmritaModule {
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
    private NoFallMode currentMode;
    public final Setting<NoFallModes> mode = this.sgGeneral
        .add(
            new Builder<NoFallModes>().name("mode").description("The method of applying nofall.")
                .defaultValue(NoFallModes.Elytra_Clip)
                .onModuleActivated(spiderModesSetting -> this.onModeChanged((NoFallModes) spiderModesSetting.get()))
                .onChanged(this::onModeChanged)
                .build()
        );

    public NoFallPlus() {
        super(Compassion.AMRITA, "NoFallV2", "Bypass fall damage or reduce fall damage");
        this.onModeChanged((NoFallModes) this.mode.get());
    }

    public void onActivate() {
        this.currentMode.onActivate();
    }

    public void onDeactivate() {
        this.currentMode.onDeactivate();
    }

    @EventHandler
    private void onPreTick(Pre event) {
        this.currentMode.onTickEventPre(event);
    }

    @EventHandler
    private void onPostTick(Post event) {
        this.currentMode.onTickEventPost(event);
    }

    @EventHandler
    public void onSendPacket(Send event) {
        this.currentMode.onSendPacket(event);
    }

    @EventHandler
    public void onSentPacket(Sent event) {
        this.currentMode.onSentPacket(event);
    }

    public String getInfoString() {
        return ((NoFallModes) this.mode.get()).toString();
    }

    private void onModeChanged(NoFallModes mode) {
        switch (mode) {
            case Elytra_Fly:
                this.currentMode = new ElytraFly();
                break;
            case Elytra_Clip:
                this.currentMode = new Eclip();
                break;
            case Matrix_New:
                this.currentMode = new MatrixNew();
                break;
            case Verus:
                this.currentMode = new Verus();
                break;
            case Vulcan:
                this.currentMode = new Vulcan();
                break;
            case Vulcan_2dot7dot7:
                this.currentMode = new Vulcan277();
        }
    }
}
