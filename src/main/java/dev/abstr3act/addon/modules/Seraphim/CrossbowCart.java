package dev.abstr3act.addon.modules.Seraphim;

import dev.abstr3act.addon.Compassion;
import dev.abstr3act.addon.events.EventInteractBlock;
import dev.abstr3act.addon.module.SeraphimModule;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Blocks;
import net.minecraft.block.RailBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.vehicle.TntMinecartEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.List;

public class CrossbowCart extends SeraphimModule {
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();

    public CrossbowCart() {
        super(Compassion.SERAPHIM, "FlintAssist", ".");
    }

    @EventHandler
    public void onInteractBlock(EventInteractBlock event) {
        if (this.checkCart(event.getHitResult().getBlockPos().toCenterPos(), 1.2F)
            && this.mc.world.getBlockState(event.getHitResult().getBlockPos()).getBlock() != Blocks.FIRE
            && this.mc.world.getBlockState(event.getHitResult().getBlockPos()).getBlock() != Blocks.SOUL_FIRE
            && !(this.mc.world.getBlockState(event.getHitResult().getBlockPos()).getBlock() instanceof RailBlock)) {
            FindItemResult flint = InvUtils.find(new Item[]{Items.FLINT_AND_STEEL});
            if (flint.found()) {
                InvUtils.swap(flint.slot(), false);
            }
        }
    }

    public boolean checkCart(Vec3d pos, float range) {
        World world = this.mc.player.getWorld();
        double x = pos.getX();
        double y = pos.getY();
        double z = pos.getZ();
        Box box = new Box(x - range, y - range, z - range, x + range, y + range, z + range);
        List<TntMinecartEntity> tntMinecarts = world.getEntitiesByClass(TntMinecartEntity.class, box, Entity::isAttackable);
        return !tntMinecarts.isEmpty();
    }
}
