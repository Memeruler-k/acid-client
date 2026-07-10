package dev.abstr3act.addon.utils.math;

import dev.abstr3act.addon.mixin.accessor.IInteractionManager;
import dev.abstr3act.addon.utils.math.inv.SearchInvResult;
import meteordevelopment.meteorclient.MeteorClient;
import net.minecraft.block.Block;
import net.minecraft.item.*;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.registry.Registries;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

public final class InventoryUtility {
    private static int cachedSlot = -1;

    public static int getItemCount(Item item) {
        if (MeteorClient.mc.player == null) {
            return 0;
        } else {
            int counter = 0;

            for (int i = 0; i <= 44; i++) {
                ItemStack itemStack = MeteorClient.mc.player.getInventory().getStack(i);
                if (itemStack.getItem() == item) {
                    counter += itemStack.getCount();
                }
            }

            return counter;
        }
    }

    public static SearchInvResult getPickAxeHotbar() {
        if (MeteorClient.mc.player == null) {
            return SearchInvResult.notFound();
        } else {
            int slot = -1;
            float f = 1.0F;

            for (int b1 = 0; b1 < 9; b1++) {
                ItemStack itemStack = MeteorClient.mc.player.getInventory().getStack(b1);
                if (itemStack != null && itemStack.getItem() instanceof PickaxeItem) {
                    slot = b1;
                }
            }

            return slot == -1 ? SearchInvResult.notFound() : new SearchInvResult(slot, true, MeteorClient.mc.player.getInventory().getStack(slot));
        }
    }

    public static SearchInvResult getSkull() {
        if (MeteorClient.mc.player == null) {
            return SearchInvResult.notFound();
        } else {
            int slot = -1;

            for (int b1 = 0; b1 < 9; b1++) {
                ItemStack itemStack = MeteorClient.mc.player.getInventory().getStack(b1);
                if (itemStack != null
                    && (
                    itemStack.getItem().equals(Items.SKELETON_SKULL)
                        || itemStack.getItem().equals(Items.WITHER_SKELETON_SKULL)
                        || itemStack.getItem().equals(Items.CREEPER_HEAD)
                        || itemStack.getItem().equals(Items.PLAYER_HEAD)
                        || itemStack.getItem().equals(Items.ZOMBIE_HEAD)
                )) {
                    slot = b1;
                    break;
                }
            }

            return slot == -1 ? SearchInvResult.notFound() : new SearchInvResult(slot, true, MeteorClient.mc.player.getInventory().getStack(slot));
        }
    }

    public static int getElytra() {
        for (ItemStack stack : MeteorClient.mc.player.getInventory().armor) {
            if (stack.getItem() == Items.ELYTRA && stack.getDamage() < 430) {
                return -2;
            }
        }

        int slot = -1;

        for (int i = 0; i < 36; i++) {
            ItemStack s = MeteorClient.mc.player.getInventory().getStack(i);
            if (s.getItem() == Items.ELYTRA && s.getDamage() < 430) {
                slot = i;
                break;
            }
        }

        if (slot < 9 && slot != -1) {
            slot += 36;
        }

        return slot;
    }

    public static SearchInvResult findInHotBar(Searcher searcher) {
        if (MeteorClient.mc.player != null) {
            for (int i = 0; i < 9; i++) {
                ItemStack stack = MeteorClient.mc.player.getInventory().getStack(i);
                if (searcher.isValid(stack)) {
                    return new SearchInvResult(i, true, stack);
                }
            }
        }

        return SearchInvResult.notFound();
    }

    public static SearchInvResult findItemInHotBar(List<Item> items) {
        return findInHotBar(stack -> items.contains(stack.getItem()));
    }

    public static SearchInvResult findItemInHotBar(Item... items) {
        return findItemInHotBar(Arrays.asList(items));
    }

    public static SearchInvResult findInInventory(Searcher searcher) {
        if (MeteorClient.mc.player != null) {
            for (int i = 36; i >= 0; i--) {
                ItemStack stack = MeteorClient.mc.player.getInventory().getStack(i);
                if (searcher.isValid(stack)) {
                    if (i < 9) {
                        i += 36;
                    }

                    return new SearchInvResult(i, true, stack);
                }
            }
        }

        return SearchInvResult.notFound();
    }

    public static SearchInvResult findItemInInventory(List<Item> items) {
        return findInInventory(stack -> items.contains(stack.getItem()));
    }

    public static SearchInvResult findItemInInventory(Item... items) {
        return findItemInInventory(Arrays.asList(items));
    }

    public static SearchInvResult findBlockInHotBar(@NotNull List<Block> blocks) {
        return findItemInHotBar(blocks.stream().<Item>map(Block::asItem).toList());
    }

    public static SearchInvResult findBlockInHotBar(Block... blocks) {
        return findItemInHotBar(Arrays.stream(blocks).<Item>map(Block::asItem).toList());
    }

    public static SearchInvResult findBlockInInventory(@NotNull List<Block> blocks) {
        return findItemInInventory(blocks.stream().<Item>map(Block::asItem).toList());
    }

    public static SearchInvResult findBlockInInventory(Block... blocks) {
        return findItemInInventory(Arrays.stream(blocks).<Item>map(Block::asItem).toList());
    }

    public static void saveSlot() {
        cachedSlot = MeteorClient.mc.player.getInventory().selectedSlot;
    }

    public static void returnSlot() {
        if (cachedSlot != -1) {
            switchTo(cachedSlot);
        }

        cachedSlot = -1;
    }

    public static void saveAndSwitchTo(int slot) {
        saveSlot();
        if (MeteorClient.mc.player != null && MeteorClient.mc.getNetworkHandler() != null) {
            if (MeteorClient.mc.player.getInventory().selectedSlot != slot) {
                MeteorClient.mc.player.getInventory().selectedSlot = slot;
                ((IInteractionManager) MeteorClient.mc.interactionManager).syncSlot();
            }
        }
    }

    public static void switchTo(int slot) {
        if (MeteorClient.mc.player != null && MeteorClient.mc.getNetworkHandler() != null) {
            if (MeteorClient.mc.player.getInventory().selectedSlot != slot) {
                MeteorClient.mc.player.getInventory().selectedSlot = slot;
                ((IInteractionManager) MeteorClient.mc.interactionManager).syncSlot();
            }
        }
    }

    public static void switchToSilent(int slot) {
        if (MeteorClient.mc.player != null && MeteorClient.mc.getNetworkHandler() != null) {
            MeteorClient.mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(slot));
        }
    }

    public static SearchInvResult getAntiWeaknessItem() {
        if (MeteorClient.mc.player == null) {
            return SearchInvResult.notFound();
        } else {
            Item mainHand = MeteorClient.mc.player.getMainHandStack().getItem();
            return !(mainHand instanceof SwordItem) && !(mainHand instanceof PickaxeItem) && !(mainHand instanceof AxeItem) && !(mainHand instanceof ShovelItem)
                ? findInHotBar(
                itemStack -> itemStack.getItem() instanceof SwordItem
                    || itemStack.getItem() instanceof PickaxeItem
                    || itemStack.getItem() instanceof AxeItem
                    || itemStack.getItem() instanceof ShovelItem
            )
                : new SearchInvResult(MeteorClient.mc.player.getInventory().selectedSlot, true, MeteorClient.mc.player.getMainHandStack());
        }
    }

    public static SearchInvResult findProjectileInHotBar() {
        if (MeteorClient.mc.player == null) {
            return SearchInvResult.notFound();
        } else {
            for (int b1 = 0; b1 < 9; b1++) {
                ItemStack itemStack = MeteorClient.mc.player.getInventory().getStack(b1);
                if (itemStack != null && itemStack.getItem() instanceof ProjectileItem) {
                    return new SearchInvResult(b1, true, MeteorClient.mc.player.getInventory().getStack(b1));
                }
            }

            return SearchInvResult.notFound();
        }
    }

    public static SearchInvResult findBed() {
        if (MeteorClient.mc.player == null) {
            return SearchInvResult.notFound();
        } else {
            for (int b1 = 0; b1 < 9; b1++) {
                ItemStack itemStack = MeteorClient.mc.player.getInventory().getStack(b1);
                if (itemStack != null && itemStack.getItem() instanceof BedItem) {
                    return new SearchInvResult(b1, true, MeteorClient.mc.player.getInventory().getStack(b1));
                }
            }

            return SearchInvResult.notFound();
        }
    }

    public static SearchInvResult findBlock() {
        if (MeteorClient.mc.player == null) {
            return SearchInvResult.notFound();
        } else {
            for (int b1 = 0; b1 < 9; b1++) {
                ItemStack itemStack = MeteorClient.mc.player.getInventory().getStack(b1);
                if (itemStack != null && itemStack.getItem() instanceof BlockItem && !(itemStack.getItem() instanceof BedItem)) {
                    return new SearchInvResult(b1, true, MeteorClient.mc.player.getInventory().getStack(b1));
                }
            }

            return SearchInvResult.notFound();
        }
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

    public interface Searcher {
        boolean isValid(ItemStack var1);
    }
}
