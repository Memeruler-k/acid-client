package dev.abstr3act.addon.modules.Amrita.jesus;

import dev.abstr3act.addon.Compassion;
import dev.abstr3act.addon.module.AmritaModule;
import dev.abstr3act.addon.modules.Amrita.jesus.modes.MatrixZoom;
import dev.abstr3act.addon.modules.Amrita.jesus.modes.MatrixZoom2;
import dev.abstr3act.addon.modules.Amrita.jesus.modes.NCP;
import dev.abstr3act.addon.modules.Amrita.jesus.modes.VulcanExploit;
import meteordevelopment.meteorclient.events.entity.player.CanWalkOnFluidEvent;
import meteordevelopment.meteorclient.events.entity.player.PlayerMoveEvent;
import meteordevelopment.meteorclient.events.packets.PacketEvent.Send;
import meteordevelopment.meteorclient.events.packets.PacketEvent.Sent;
import meteordevelopment.meteorclient.events.world.CollisionShapeEvent;
import meteordevelopment.meteorclient.events.world.TickEvent.Post;
import meteordevelopment.meteorclient.events.world.TickEvent.Pre;
import meteordevelopment.meteorclient.settings.EnumSetting.Builder;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.orbit.EventHandler;

public class JesusPlus extends AmritaModule {
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
    public final Setting<Double> speed = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("speed"))
                .description("Jesus speed."))
                .defaultValue(1.25)
                .max(2500.0)
                .sliderRange(0.0, 2500.0)
                .build()
        );
    private JesusMode currentMode;
    public final Setting<JesusModes> jesusMode = this.sgGeneral
        .add(new Builder<JesusModes>()
            .name("mode")
            .description("The method of applying jesus.")
            .defaultValue(JesusModes.Matrix_Zoom)
            .onModuleActivated(spiderModesSetting -> this.onJesusModeChanged(spiderModesSetting.get()))
            .onChanged(this::onJesusModeChanged)
            .build()
        );


    public final Setting<Double> limit_speed = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("limit-speed"))
                .description("Jesus speed."))
                .visible(() -> this.jesusMode.get() == JesusModes.NCP))
                .build()
        );
    public final Setting<Boolean> autoSwapVulcan = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("auto-swap"))
                .description("Auto swap."))
                .defaultValue(true))
                .visible(() -> this.jesusMode.get() == JesusModes.Vulcan_Exploit))
                .build()
        );

    public JesusPlus() {
        super(Compassion.AMRITA, "JesusV2", "Bypass-jesus");
        this.onJesusModeChanged((JesusModes) this.jesusMode.get());
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

    @EventHandler
    public void onCanWalkOnFluid(CanWalkOnFluidEvent event) {
        this.currentMode.onCanWalkOnFluid(event);
    }

    @EventHandler
    public void onCollisionShape(CollisionShapeEvent event) {
        this.currentMode.onCollisionShape(event);
    }

    @EventHandler
    private void onPlayerMoveEvent(PlayerMoveEvent event) {
        this.currentMode.onPlayerMoveEvent(event);
    }

    public String getInfoString() {
        return ((JesusModes) this.jesusMode.get()).toString();
    }

    private void onJesusModeChanged(JesusModes mode) {
        switch (mode) {
            case Matrix_Zoom:
                this.currentMode = new MatrixZoom();
                break;
            case Matrix_Zoom_2:
                this.currentMode = new MatrixZoom2();
                break;
            case Vulcan_Exploit:
                this.currentMode = new VulcanExploit();
                break;
            case NCP:
                this.currentMode = new NCP();
        }
    }
}
