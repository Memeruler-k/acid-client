package dev.abstr3act.addon.modules.Compassion;

import dev.abstr3act.addon.Compassion;
import dev.abstr3act.addon.module.CompassionModule;
import meteordevelopment.meteorclient.events.game.GameLeftEvent;
import meteordevelopment.meteorclient.events.world.TickEvent.Post;
import meteordevelopment.meteorclient.settings.IntSetting.Builder;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.orbit.EventHandler;

public class ConsoleSpammer extends CompassionModule {
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
    private final Setting<Integer> packets = this.sgGeneral
        .add(((Builder) ((Builder) ((Builder) new Builder().name("Packets")).description(".")).defaultValue(1)).min(1).sliderRange(1, 100).build());
    byte[] data = new byte[]{7, 0, -49, -24, 11, 6, 0, 0};

    public ConsoleSpammer() {
        super(Compassion.COMPASSION, "ConsoleSpammer", ".");
    }

    @EventHandler
    private void onGameLeft(GameLeftEvent event) {
        this.toggle();
    }

    @EventHandler
    private void onTick(Post event) {
//        for (int i = 0; i < this.packets.get(); i++) {
//            this.mc.player.networkHandler.getConnection().channel.pipeline().firstContext().writeAndFlush(Unpooled.wrappedBuffer(this.data));
//        }
    }
}
