package dev.abstr3act.addon.modules.Amrita.noslow.modes;

import dev.abstr3act.addon.events.PlayerUseMultiplierEvent;
import dev.abstr3act.addon.modules.Amrita.noslow.NoSlowMode;
import dev.abstr3act.addon.modules.Amrita.noslow.NoSlowModes;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.util.Hand;

public class GrimNew extends NoSlowMode {
    public GrimNew() {
        super(NoSlowModes.Grim_New);
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

        Hand hand = this.mc.player.getActiveHand();
        ClientPlayNetworkHandler network = this.mc.getNetworkHandler();

        assert network != null;

        if (hand == Hand.MAIN_HAND) {
            network.sendPacket(new PlayerInteractItemC2SPacket(Hand.OFF_HAND, 0, this.mc.player.getYaw(), this.mc.player.getPitch()));
        } else if (hand == Hand.OFF_HAND) {
            network.sendPacket(new UpdateSelectedSlotC2SPacket(this.mc.player.getInventory().selectedSlot % 8 + 1));
            network.sendPacket(new UpdateSelectedSlotC2SPacket(this.mc.player.getInventory().selectedSlot));
        }
    }
}
