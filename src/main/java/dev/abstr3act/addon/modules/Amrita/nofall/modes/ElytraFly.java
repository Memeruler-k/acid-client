package dev.abstr3act.addon.modules.Amrita.nofall.modes;

import dev.abstr3act.addon.modules.Amrita.nofall.NoFallMode;
import dev.abstr3act.addon.modules.Amrita.nofall.NoFallModes;
import meteordevelopment.meteorclient.events.world.TickEvent.Pre;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket.Mode;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket.OnGroundOnly;
import net.minecraft.util.math.Vec3d;

public class ElytraFly extends NoFallMode {
    public ElytraFly() {
        super(NoFallModes.Elytra_Fly);
    }

    @Override
    public void onTickEventPre(Pre event) {
        if (this.mc.player.fallDistance > 2.0F) {
            FindItemResult elytra = InvUtils.find(new Item[]{Items.ELYTRA});
            if (elytra.found()) {
                int slot = elytra.slot();
                if (this.mc.player.getInventory().getArmorStack(2).getItem() != Items.ELYTRA) {
                    InvUtils.move().from(slot).toArmor(2);
                }
            }

            if (this.mc.player.fallDistance > 2.7) {
                this.mc.player.networkHandler.sendPacket(new ClientCommandC2SPacket(this.mc.player, Mode.START_FALL_FLYING));
                this.mc.player.networkHandler.sendPacket(new OnGroundOnly(true));
                Vec3d vel = this.mc.player.getVelocity();
                this.mc.player.setVelocity(vel.x, 0.0, vel.z);
                this.mc.player.fallDistance = 0.0F;
                this.mc.player.setOnGround(true);
            }
        }
    }
}
