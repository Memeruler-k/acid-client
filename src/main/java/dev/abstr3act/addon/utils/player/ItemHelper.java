package dev.abstr3act.addon.utils.player;

import meteordevelopment.meteorclient.utils.misc.Names;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.EnderChestBlock;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.item.*;

import java.util.ArrayList;

public class ItemHelper {
    public static ArrayList<Item> wools = new ArrayList<Item>() {
        {
            this.add(Items.WHITE_WOOL);
            this.add(Items.ORANGE_WOOL);
            this.add(Items.MAGENTA_WOOL);
            this.add(Items.LIGHT_BLUE_WOOL);
            this.add(Items.YELLOW_WOOL);
            this.add(Items.LIME_WOOL);
            this.add(Items.PINK_WOOL);
            this.add(Items.GRAY_WOOL);
            this.add(Items.LIGHT_GRAY_WOOL);
            this.add(Items.CYAN_WOOL);
            this.add(Items.PURPLE_WOOL);
            this.add(Items.BLUE_WOOL);
            this.add(Items.BROWN_WOOL);
            this.add(Items.GREEN_WOOL);
            this.add(Items.RED_WOOL);
            this.add(Items.BLACK_WOOL);
        }
    };
    public static ArrayList<Item> planks = new ArrayList<Item>() {
        {
            this.add(Items.OAK_PLANKS);
            this.add(Items.SPRUCE_PLANKS);
            this.add(Items.BIRCH_PLANKS);
            this.add(Items.JUNGLE_PLANKS);
            this.add(Items.ACACIA_PLANKS);
            this.add(Items.DARK_OAK_PLANKS);
        }
    };
    public static ArrayList<Item> shulkers = new ArrayList<Item>() {
        {
            this.add(Items.SHULKER_BOX);
            this.add(Items.BLACK_SHULKER_BOX);
            this.add(Items.BLUE_SHULKER_BOX);
            this.add(Items.BROWN_SHULKER_BOX);
            this.add(Items.GREEN_SHULKER_BOX);
            this.add(Items.RED_SHULKER_BOX);
            this.add(Items.WHITE_SHULKER_BOX);
            this.add(Items.LIGHT_BLUE_SHULKER_BOX);
            this.add(Items.LIGHT_GRAY_SHULKER_BOX);
            this.add(Items.LIME_SHULKER_BOX);
            this.add(Items.MAGENTA_SHULKER_BOX);
            this.add(Items.ORANGE_SHULKER_BOX);
            this.add(Items.PINK_SHULKER_BOX);
            this.add(Items.CYAN_SHULKER_BOX);
            this.add(Items.GRAY_SHULKER_BOX);
            this.add(Items.PURPLE_SHULKER_BOX);
            this.add(Items.YELLOW_SHULKER_BOX);
        }
    };

    public static FindItemResult findShulker(boolean inventory) {
        return inventory
            ? InvUtils.find(itemStack -> Block.getBlockFromItem(itemStack.getItem()) instanceof ShulkerBoxBlock)
            : InvUtils.findInHotbar(itemStack -> Block.getBlockFromItem(itemStack.getItem()) instanceof ShulkerBoxBlock);
    }

    public static FindItemResult findPick() {
        return InvUtils.findInHotbar(itemStack -> itemStack.getItem() instanceof PickaxeItem);
    }

    public static FindItemResult findSword() {
        return InvUtils.findInHotbar(itemStack -> itemStack.getItem() instanceof SwordItem);
    }

    public static FindItemResult findAxe() {
        return InvUtils.findInHotbar(itemStack -> itemStack.getItem() instanceof AxeItem);
    }

    public static FindItemResult findChorus() {
        return InvUtils.findInHotbar(new Item[]{Items.CHORUS_FRUIT});
    }

    public static FindItemResult findEgap() {
        return InvUtils.findInHotbar(new Item[]{Items.ENCHANTED_GOLDEN_APPLE});
    }

    public static FindItemResult findObby() {
        return InvUtils.findInHotbar(new Item[]{Blocks.OBSIDIAN.asItem()});
    }

    public static FindItemResult findCraftTable() {
        return InvUtils.findInHotbar(new Item[]{Blocks.CRAFTING_TABLE.asItem()});
    }

    public static FindItemResult findXP() {
        return InvUtils.findInHotbar(new Item[]{Items.EXPERIENCE_BOTTLE});
    }

    public static FindItemResult findXPinAll() {
        return InvUtils.find(new Item[]{Items.EXPERIENCE_BOTTLE});
    }

    public static String getCommonName(Item item) {
        if (item instanceof BedItem) {
            return "Beds";
        } else if (item instanceof ExperienceBottleItem) {
            return "XP";
        } else if (item instanceof EndCrystalItem) {
            return "Crystals";
        } else if (item.equals(Items.ENCHANTED_GOLDEN_APPLE)) {
            return "EGaps";
        } else if (item instanceof EnderPearlItem) {
            return "Pearls";
        } else if (item.equals(Items.TOTEM_OF_UNDYING)) {
            return "Totems";
        } else if (Block.getBlockFromItem(item) == Blocks.OBSIDIAN) {
            return "Obby";
        } else {
            return Block.getBlockFromItem(item) instanceof EnderChestBlock ? "Echests" : Names.get(item);
        }
    }
}
