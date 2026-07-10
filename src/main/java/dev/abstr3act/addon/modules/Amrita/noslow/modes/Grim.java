package dev.abstr3act.addon.modules.Amrita.noslow.modes;

import dev.abstr3act.addon.events.PlayerUseMultiplierEvent;
import dev.abstr3act.addon.modules.Amrita.noslow.NoSlowMode;
import dev.abstr3act.addon.modules.Amrita.noslow.NoSlowModes;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;

public class Grim extends NoSlowMode {
    public Grim() {
        super(NoSlowModes.Grim_1dot8);
    }

    @Override
    public void onUse(PlayerUseMultiplierEvent event) {
        if (this.mc.player.isSneaking()) {
            event.setForward((this.settings.sneakForward.get()).floatValue());
            event.setSideways((this.settings.sneakSideways.get()).floatValue());
        } else if (this.mc.player.isUsingItem()) {
            event.setForward((this.settings.usingForward.get()).floatValue());
            event.setSideways((this.settings.usingSideways.get()).floatValue());
        } else {
            event.setForward((this.settings.otherForward.get()).floatValue());
            event.setSideways((this.settings.otherSideways.get()).floatValue());
        }

        if (this.mc.player.isUsingItem()) {
            ClientPlayNetworkHandler network = this.mc.getNetworkHandler();

            assert network != null;

            network.sendPacket(new UpdateSelectedSlotC2SPacket(this.mc.player.getInventory().selectedSlot % 8 + 1));
            network.sendPacket(new UpdateSelectedSlotC2SPacket(this.mc.player.getInventory().selectedSlot));
        }
    }
}
