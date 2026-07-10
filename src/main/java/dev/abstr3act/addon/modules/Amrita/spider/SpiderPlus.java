package dev.abstr3act.addon.modules.Amrita.spider;

import dev.abstr3act.addon.Compassion;
import dev.abstr3act.addon.module.AmritaModule;
import dev.abstr3act.addon.modules.Amrita.spider.modes.Eclip;
import dev.abstr3act.addon.modules.Amrita.spider.modes.Matrix;
import dev.abstr3act.addon.modules.Amrita.spider.modes.Vulcan;
import meteordevelopment.meteorclient.events.packets.PacketEvent.Send;
import meteordevelopment.meteorclient.events.packets.PacketEvent.Sent;
import meteordevelopment.meteorclient.events.world.TickEvent.Post;
import meteordevelopment.meteorclient.events.world.TickEvent.Pre;
import meteordevelopment.meteorclient.settings.EnumSetting.Builder;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.orbit.EventHandler;

public class SpiderPlus extends AmritaModule {
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
    private SpiderMode currentMode;
    public final Setting<SpiderModes> spiderMode = this.sgGeneral
        .add(
            new Builder<SpiderModes>().name("mode").description("The method of applying spider.")
                .defaultValue(SpiderModes.Matrix)
                .onModuleActivated(spiderModesSetting -> this.onSpiderModeChanged((SpiderModes) spiderModesSetting.get()))
                .onChanged(this::onSpiderModeChanged)
                .build()
        );
    public final Setting<Integer> Blocks = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("blocks"))
                .defaultValue(3))
                .description("Don't touch if you don't know what it does."))
                .visible(() -> this.spiderMode.get() == SpiderModes.Elytra_clip))
                .range(0, 10)
                .build()
        );

    public SpiderPlus() {
        super(Compassion.AMRITA, "SpiderV2", "Bypass spider");
        this.onSpiderModeChanged((SpiderModes) this.spiderMode.get());
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

    private void onSpiderModeChanged(SpiderModes mode) {
        switch (mode) {
            case Matrix:
                this.currentMode = new Matrix();
                break;
            case Vulcan:
                this.currentMode = new Vulcan();
                break;
            case Elytra_clip:
                this.currentMode = new Eclip();
        }
    }

    public String getInfoString() {
        return ((SpiderModes) this.spiderMode.get()).toString();
    }
}
