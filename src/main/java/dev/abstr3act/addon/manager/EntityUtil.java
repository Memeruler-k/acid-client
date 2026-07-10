package dev.abstr3act.addon.manager;

import dev.abstr3act.addon.modules.Compassion.MoveFix;
import dev.abstr3act.addon.utils.compassion.BlockPosX;
import meteordevelopment.meteorclient.MeteorClient;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.RaycastContext.FluidHandling;
import net.minecraft.world.RaycastContext.ShapeType;

public class EntityUtil {
    public static boolean isHoldingWeapon(PlayerEntity player) {
        return player.getMainHandStack().getItem() instanceof SwordItem || player.getMainHandStack().getItem() instanceof AxeItem;
    }

    public static boolean isInsideBlock() {
        return BlockUtil.getBlock(getPlayerPos(true)) == Blocks.ENDER_CHEST
            ? true
            : MeteorClient.mc.world.canCollide(MeteorClient.mc.player, MeteorClient.mc.player.getBoundingBox());
    }

    public static int getDamagePercent(ItemStack stack) {
        return stack.getDamage() == stack.getMaxDamage()
            ? 100
            : (int) ((stack.getMaxDamage() - stack.getDamage()) / Math.max(0.1, stack.getMaxDamage()) * 100.0);
    }

    public static boolean isArmorLow(PlayerEntity player, int durability) {
        for (ItemStack piece : player.getArmorItems()) {
            if (piece == null || piece.isEmpty()) {
                return true;
            }

            if (getDamagePercent(piece) < durability) {
                return true;
            }
        }

        return false;
    }

    public static float getHealth(Entity entity) {
        if (entity.isLiving()) {
            LivingEntity livingBase = (LivingEntity) entity;
            return livingBase.getHealth() + livingBase.getAbsorptionAmount();
        } else {
            return 0.0F;
        }
    }

    public static BlockPos getEntityPos(Entity entity) {
        return new BlockPosX(entity.getPos());
    }

    public static BlockPos getPlayerPos(boolean fix) {
        return new BlockPosX(MeteorClient.mc.player.getPos(), fix);
    }

    public static BlockPos getEntityPos(Entity entity, boolean fix) {
        return new BlockPosX(entity.getPos(), fix);
    }

    public static Vec3d getEyesPos() {
        return MeteorClient.mc.player.getEyePos();
    }

    public static boolean canSee(BlockPos pos, Direction side) {
        Vec3d testVec = pos.toCenterPos().add(side.getVector().getX() * 0.5, side.getVector().getY() * 0.5, side.getVector().getZ() * 0.5);
        HitResult result = MeteorClient.mc.world.raycast(new RaycastContext(getEyesPos(), testVec, ShapeType.COLLIDER, FluidHandling.NONE, MeteorClient.mc.player));
        return result == null || result.getType() == Type.MISS;
    }

    public static void swingHand(Hand hand, SwingSide side) {
        switch (side) {
            case All:
                MeteorClient.mc.player.swingHand(hand);
                break;
            case Client:
                MeteorClient.mc.player.swingHand(hand, false);
                break;
            case Server:
                MeteorClient.mc.getNetworkHandler().sendPacket(new HandSwingC2SPacket(hand));
        }
    }

    public static void syncInventory() {
        if (MoveFix.INSTANCE.inventorySync.get()) {
            MeteorClient.mc.getNetworkHandler().sendPacket(new CloseHandledScreenC2SPacket(MeteorClient.mc.player.currentScreenHandler.syncId));
        }
    }
}
