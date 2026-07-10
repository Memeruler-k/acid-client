package dev.abstr3act.addon.modules.Seraphim;

import dev.abstr3act.addon.Compassion;
import dev.abstr3act.addon.events.EventInteractBlock;
import dev.abstr3act.addon.module.SeraphimModule;
import meteordevelopment.meteorclient.events.entity.player.InteractEntityEvent;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Block;
import net.minecraft.block.RailBlock;
import net.minecraft.entity.vehicle.TntMinecartEntity;
import net.minecraft.item.BowItem;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.Item;
import net.minecraft.item.Items;

public class CartAssist extends SeraphimModule {
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();

    public CartAssist() {
        super(Compassion.SERAPHIM, "CartAssist", ".");
    }

    @EventHandler
    public void onInteractBlock(EventInteractBlock event) {
        Block block = this.mc.world.getBlockState(event.getHitResult().getBlockPos()).getBlock();
        if (!(this.mc.player.getMainHandStack().getItem() instanceof BowItem) && !(this.mc.player.getMainHandStack().getItem() instanceof CrossbowItem)) {
            if (block instanceof RailBlock) {
                FindItemResult cart = InvUtils.find(new Item[]{Items.TNT_MINECART});
                if (cart.found()) {
                    InvUtils.swap(cart.slot(), false);
                }
            }
        }
    }

    @EventHandler
    public void onInteractEntity(InteractEntityEvent event) {
        if (event.entity instanceof TntMinecartEntity) {
            FindItemResult bow = InvUtils.find(new Item[]{Items.BOW});
            if (bow.found()) {
                InvUtils.swap(bow.slot(), false);
            }
        }
    }
}
