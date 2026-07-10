package dev.abstr3act.addon.utils;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import meteordevelopment.meteorclient.MeteorClient;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.component.type.AttributeModifiersComponent.Entry;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public final class ToolUtil {
    private static final Map<Item, Integer> materialStrength = new HashMap<Item, Integer>() {
        {
            this.put(Items.NETHERITE_SWORD, 5);
            this.put(Items.DIAMOND_SWORD, 4);
            this.put(Items.IRON_SWORD, 3);
            this.put(Items.GOLDEN_SWORD, 2);
            this.put(Items.STONE_SWORD, 1);
            this.put(Items.WOODEN_SWORD, 0);
        }
    };

    public static int findBestTool(BlockPos pos) {
        if (MeteorClient.mc.player != null && MeteorClient.mc.player.isCreative()) {
            return -1;
        } else {
            PlayerEntity player = MinecraftClient.getInstance().player;
            if (player == null) {
                return -1;
            } else {
                BlockState blockState = player.getWorld().getBlockState(pos);
                int bestSlot = -1;
                double bestSpeed = 0.0;

                for (int i = 0; i <= 8; i++) {
                    ItemStack stack = player.getInventory().getStack(i);
                    if (stack.getItem() instanceof MiningToolItem || stack.getItem() instanceof ShearsItem) {
                        float speed = stack.getMiningSpeedMultiplier(blockState);
                        if (speed > bestSpeed) {
                            bestSpeed = speed;
                            bestSlot = i;
                        }
                    }
                }

                return bestSpeed > 1.0 ? bestSlot : -1;
            }
        }
    }

    public static int findBestSword() {
        PlayerEntity player = MeteorClient.mc.player;
        if (player == null) {
            return -1;
        } else {
            int bestSwordSlot = -1;
            double highestStrength = -1.0;

            for (int i = 0; i <= 8; i++) {
                ItemStack stack = player.getInventory().getStack(i);
                if (stack.getItem() instanceof SwordItem) {
                    int strength = materialStrength.getOrDefault(stack.getItem(), 0);
                    if (strength > highestStrength) {
                        highestStrength = strength;
                        bestSwordSlot = i;
                    }
                }
            }

            return bestSwordSlot;
        }
    }

    public static int getItemSlotId(ItemStack itemStack) {
        if (itemStack.getItem() instanceof ArmorItem armorItem) {
            EquipmentSlot slot = armorItem.getSlotType();
            if (slot == EquipmentSlot.HEAD) {
                return 5;
            }

            if (slot == EquipmentSlot.CHEST) {
                return 6;
            }

            if (slot == EquipmentSlot.LEGS) {
                return 7;
            }

            if (slot == EquipmentSlot.FEET) {
                return 8;
            }
        }

        return -1;
    }

    public static boolean isBetterArmor(ItemStack newArmor, ItemStack currentArmor) {
        if (newArmor.getItem() instanceof ArmorItem && currentArmor.getItem() instanceof ArmorItem) {
            int newArmorValue = getArmorScore(newArmor);
            int currentArmorValue = getArmorScore(currentArmor);
            return newArmorValue > currentArmorValue;
        } else {
            return false;
        }
    }

    @NotNull
    public static EquipmentSlot getArmorEquipmentSlot(ItemStack stack) {
        return stack.getItem() instanceof ArmorItem armorItem ? armorItem.getSlotType() : null;
    }

    private static int getArmorScore(ItemStack itemStack) {
        if (itemStack.isEmpty()) {
            return 0;
        } else {
            int score = 0;
            new Object2IntOpenHashMap();
            int var8 = score + 114514;
            if (itemStack.contains(DataComponentTypes.ATTRIBUTE_MODIFIERS)) {
                AttributeModifiersComponent component = (AttributeModifiersComponent) itemStack.get(DataComponentTypes.ATTRIBUTE_MODIFIERS);

                for (Entry modifier : component.modifiers()) {
                    if (modifier.attribute() == EntityAttributes.GENERIC_ARMOR || modifier.attribute() == EntityAttributes.GENERIC_ARMOR_TOUGHNESS) {
                        double value = modifier.modifier().value();
                        switch (modifier.modifier().operation()) {
                            case ADD_VALUE:
                                var8 += (int) value;
                                break;
                            case ADD_MULTIPLIED_BASE:
                                var8 += (int) (value * MeteorClient.mc.player.getAttributeBaseValue(modifier.attribute()));
                                break;
                            case ADD_MULTIPLIED_TOTAL:
                                var8 += (int) (value * var8);
                        }
                    }
                }
            }

            return var8;
        }
    }
}
