package dev.abstr3act.addon.modules.Seraphim;

import dev.abstr3act.addon.Compassion;
import dev.abstr3act.addon.module.SeraphimModule;
import meteordevelopment.meteorclient.events.packets.PacketEvent.Receive;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.network.packet.s2c.play.UpdateSelectedSlotS2CPacket;

public class NoServerSlot extends SeraphimModule {
    public NoServerSlot() {
        super(Compassion.SERAPHIM, "NoServerSlot", ".");
    }

    @EventHandler
    public void onPacketReceive(Receive event) {
        if (event.packet instanceof UpdateSelectedSlotS2CPacket) {
            event.cancel();
            this.sendPacket(new UpdateSelectedSlotC2SPacket(this.mc.player.getInventory().selectedSlot));
        }
    }
}
