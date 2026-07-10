package dev.abstr3act.addon.utils;

import dev.abstr3act.addon.utils.math.inv.SearchInvResult;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.systems.modules.combat.Criticals;
import net.minecraft.block.Block;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.DamageUtil;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.*;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public final class InventoryUtility {
    private static int cachedSlot = -1;

    public static SearchInvResult getAxe() {
        if (MeteorClient.mc.player == null) {
            return SearchInvResult.notFound();
        } else {
            int slot = -1;
            float f = 1.0F;

            for (int b1 = 9; b1 < 45; b1++) {
                ItemStack itemStack = MeteorClient.mc.player.getInventory().getStack(b1 >= 36 ? b1 - 36 : b1);
                if (itemStack != null && itemStack.getItem() instanceof AxeItem axe) {
                    float f1 = (axe.getComponents().get(DataComponentTypes.MAX_DAMAGE)).intValue();
                    float var7 = f1
                        + EnchantmentHelper.getLevel(
                        (RegistryEntry) MeteorClient.mc.world.getRegistryManager().get(Enchantments.SHARPNESS.getRegistryRef()).getEntry(Enchantments.SHARPNESS).get(),
                        itemStack
                    );
                    if (var7 > f) {
                        f = var7;
                        slot = b1;
                    }
                }
            }

            if (slot >= 36) {
                slot -= 36;
            }

            return slot == -1 ? SearchInvResult.notFound() : new SearchInvResult(slot, true, MeteorClient.mc.player.getInventory().getStack(slot));
        }
    }

    public static float getHitDamage(@NotNull ItemStack weapon, PlayerEntity ent) {
        if (MeteorClient.mc.player == null) {
            return 0.0F;
        } else {
            float baseDamage = 1.0F;
            if (weapon.getItem() instanceof SwordItem swordItem) {
                baseDamage = 7.0F;
            }

            if (weapon.getItem() instanceof AxeItem axeItem) {
                baseDamage = 9.0F;
            }

            if (weapon.getItem() instanceof MaceItem) {
                baseDamage = getBonusAttackDamage(MeteorClient.mc.player) + 7.0F;
            }

            Criticals criticals = new Criticals();
            if (MeteorClient.mc.player.fallDistance > 0.0F || criticals.isActive()) {
                baseDamage += baseDamage / 2.0F;
            }

            if (MeteorClient.mc.player.hasStatusEffect(StatusEffects.STRENGTH)) {
                int strength = Objects.requireNonNull(MeteorClient.mc.player.getStatusEffect(StatusEffects.STRENGTH)).getAmplifier() + 1;
                baseDamage += 3 * strength;
            }

            return DamageUtil.getDamageLeft(
                ent,
                baseDamage,
                MeteorClient.mc.world.getDamageSources().generic(),
                ent.getArmor(),
                (float) ent.getAttributeInstance(EntityAttributes.GENERIC_ARMOR_TOUGHNESS).getValue()
            );
        }
    }

    public static float getBonusAttackDamage(PlayerEntity player) {
        return 1.0F;
    }

    public static boolean shouldDealAdditionalDamage(PlayerEntity attacker) {
        return attacker.fallDistance > 1.5F && !attacker.isFallFlying() && !attacker.isOnGround() && !attacker.isInFluid() && !attacker.isInLava();
    }

    public static Integer findMace() {
        for (int slot = 0; slot < 9; slot++) {
            ItemStack stack = MeteorClient.mc.player.getInventory().getStack(slot);
            if (stack.getItem() == Items.MACE && stack.hasEnchantments() && checkBreach(stack)) {
                return slot;
            }
        }

        return -1;
    }

    public static boolean checkBreach(ItemStack stack) {
        return stack.getItem().equals(Items.MACE)
            && stack.hasEnchantments()
            && stack.getEnchantments()
            .getLevel((RegistryEntry) MeteorClient.mc.world.getRegistryManager().get(Enchantments.BREACH.getRegistryRef()).getEntry(Enchantments.BREACH).get())
            != 0;
    }

    public static Item getItem(String Name) {
        if (Name == null) {
            return Items.AIR;
        } else {
            for (Block block : Registries.BLOCK) {
                if (block.getTranslationKey().replace("block.minecraft.", "").equals(Name.toLowerCase())) {
                    return Item.fromBlock(block);
                }
            }

            for (Item item : Registries.ITEM) {
                if (item.getTranslationKey().replace("item.minecraft.", "").equals(Name.toLowerCase())) {
                    return item;
                }
            }

            return Items.DIRT;
        }
    }

    public static int getBedsCount() {
        if (MeteorClient.mc.player == null) {
            return 0;
        } else {
            int counter = 0;

            for (int i = 0; i <= 44; i++) {
                ItemStack itemStack = MeteorClient.mc.player.getInventory().getStack(i);
                if (itemStack.getItem() instanceof BedItem) {
                    counter += itemStack.getCount();
                }
            }

            return counter;
        }
    }

    public static int getRemainingArmorCount(PlayerEntity player) {
        PlayerInventory inventory = player.getInventory();
        int remainingArmorCount = 0;

        for (int i = 0; i < 4; i++) {
            ItemStack armorItem = (ItemStack) inventory.armor.get(i);
            if (!armorItem.isEmpty()) {
                remainingArmorCount++;
            }
        }

        return remainingArmorCount;
    }

    public static int findBedItem(PlayerInventory inventory) {
        for (int i = 9; i < inventory.main.size(); i++) {
            ItemStack stack = inventory.getStack(i);
            if (stack.getItem() instanceof BedItem) {
                return i;
            }
        }

        return -1;
    }

    public static int findAxeItem(PlayerInventory inventory) {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = inventory.getStack(i);
            if (stack.getItem() instanceof AxeItem) {
                return i;
            }
        }

        return -1;
    }

    public static int findSwordItem(PlayerInventory inventory) {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = inventory.getStack(i);
            if (stack.getItem() instanceof SwordItem) {
                return i;
            }
        }

        return -1;
    }

    public static int getSlotInHotBar(PlayerEntity player, Item item) {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = player.getInventory().getStack(i);
            if (stack.getItem().equals(item)) {
                return i;
            }
        }

        return -1;
    }

    public interface Searcher {
        boolean isValid(ItemStack var1);
    }
}
