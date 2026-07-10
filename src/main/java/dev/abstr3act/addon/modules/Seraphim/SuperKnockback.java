package dev.abstr3act.addon.modules.Seraphim;

import dev.abstr3act.addon.Compassion;
import dev.abstr3act.addon.module.SeraphimModule;
import meteordevelopment.meteorclient.events.packets.PacketEvent.Send;
import meteordevelopment.meteorclient.mixininterface.IPlayerInteractEntityC2SPacket;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket.Mode;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket.InteractType;

public class SuperKnockback extends SeraphimModule {
    public SuperKnockback() {
        super(Compassion.SERAPHIM, "SuperKnockback", "Performs more KB when you hit your target.");
    }

    @EventHandler
    private void onSendPacket(Send event) {
        if (event.packet instanceof IPlayerInteractEntityC2SPacket packet && packet.getType() == InteractType.ATTACK) {
            this.mc.player.networkHandler.sendPacket(new ClientCommandC2SPacket(this.mc.player, Mode.START_SPRINTING));
        }
    }
}
