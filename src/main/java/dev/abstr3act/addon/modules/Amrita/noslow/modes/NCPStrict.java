package dev.abstr3act.addon.modules.Amrita.noslow.modes;

import dev.abstr3act.addon.events.PlayerUseMultiplierEvent;
import dev.abstr3act.addon.modules.Amrita.noslow.NoSlowMode;
import dev.abstr3act.addon.modules.Amrita.noslow.NoSlowModes;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket.Action;
import net.minecraft.util.math.Direction;

public class NCPStrict extends NoSlowMode {
    public NCPStrict() {
        super(NoSlowModes.NCP_Strict);
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
            network.sendPacket(new PlayerActionC2SPacket(Action.ABORT_DESTROY_BLOCK, this.mc.player.getBlockPos(), Direction.DOWN));
        }
    }
}
