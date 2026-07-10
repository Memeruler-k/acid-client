package dev.abstr3act.addon.modules.Seraphim;

import dev.abstr3act.addon.Compassion;
import dev.abstr3act.addon.module.SeraphimModule;
import dev.abstr3act.addon.utils.abnormally.AChatUtils;
import meteordevelopment.meteorclient.events.game.GameLeftEvent;
import meteordevelopment.meteorclient.events.packets.PacketEvent.Receive;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class FlagLogger extends SeraphimModule {
    int count;

    public FlagLogger() {
        super(Compassion.SERAPHIM, "FlagLogger", "FlagLogger");
    }

    @EventHandler(
        priority = 201
    )
    private void onReceivePacket(Receive event) {
        if (event.packet instanceof PlayerPositionLookS2CPacket) {
            this.count++;
            AChatUtils.sendMsgSeraphim(Text.of(Formatting.WHITE + "Flag detected: " + Formatting.GRAY + "[" + Formatting.RED + this.count + Formatting.GRAY + "]"));
        }
    }

    @EventHandler
    private void onGameLeft(GameLeftEvent event) {
        this.count = 0;
    }

    public void onDeactivate() {
        this.count = 0;
    }
}
