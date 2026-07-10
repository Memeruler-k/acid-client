package dev.abstr3act.addon.modules.Abnormally;

import dev.abstr3act.addon.Compassion;
import dev.abstr3act.addon.mixin.accessor.PlayerMoveC2SPacketAccessor;
import dev.abstr3act.addon.module.AbnormallyModule;
import meteordevelopment.meteorclient.events.packets.PacketEvent.Send;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

public class NoGround extends AbnormallyModule {
    public NoGround() {
        super(Compassion.ABNORMALLY, "NoGround", "Skid from CCBlueX");
    }

    @EventHandler
    private void onSentPacket(Send event) {
        if (event.packet instanceof PlayerMoveC2SPacket) {
            ((PlayerMoveC2SPacketAccessor) event.packet).setOnGround(false);
        }
    }
}
