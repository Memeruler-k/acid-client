package dev.abstr3act.addon.modules.Amrita.velocity;

import dev.abstr3act.addon.Compassion;
import dev.abstr3act.addon.modules.Amrita.velocity.modes.GrimCancel;
import dev.abstr3act.addon.modules.Amrita.velocity.modes.GrimCancel_v2;
import dev.abstr3act.addon.modules.Amrita.velocity.modes.GrimSkip;
import meteordevelopment.meteorclient.events.packets.PacketEvent.Receive;
import meteordevelopment.meteorclient.events.packets.PacketEvent.Send;
import meteordevelopment.meteorclient.events.packets.PacketEvent.Sent;
import meteordevelopment.meteorclient.events.world.TickEvent.Post;
import meteordevelopment.meteorclient.events.world.TickEvent.Pre;
import meteordevelopment.meteorclient.settings.EnumSetting.Builder;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;

public class VelocityPlus extends Module {
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
    private VelocityMode currentMode;
    private final Setting<VelocityModes> mode = this.sgGeneral
        .add(
            new Builder<VelocityModes>().name("mode").description("Velocity mode.").defaultValue(VelocityModes.Grim_Cancel)
                .onModuleActivated(timerModesSetting -> this.onTimerModeChanged((VelocityModes) timerModesSetting.get()))
                .onChanged(this::onTimerModeChanged)
                .build()
        );

    public VelocityPlus() {
        super(Compassion.AMRITA, "VelocityV3", "Bypass velocity.");
    }

    private void onTimerModeChanged(VelocityModes mode) {
        switch (mode) {
            case Grim_Cancel:
                this.currentMode = new GrimCancel();
                break;
            case Grim_Cancel_v2:
                this.currentMode = new GrimCancel_v2();
                break;
            case Grim_Skip:
                this.currentMode = new GrimSkip();
        }
    }

    public void onActivate() {
        this.currentMode.onActivate();
    }

    public void onDeactivate() {
        this.currentMode.onDeactivate();
    }

    public String getInfoString() {
        return ((VelocityModes) this.mode.get()).toString();
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

    @EventHandler
    private void onRecivePacket(Receive event) {
        this.currentMode.onReceivePacket(event);
    }
}
