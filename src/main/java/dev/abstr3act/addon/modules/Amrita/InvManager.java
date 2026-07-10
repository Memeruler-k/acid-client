package dev.abstr3act.addon.modules.Amrita;

import dev.abstr3act.addon.Compassion;
import dev.abstr3act.addon.events.EventUpdate;
import dev.abstr3act.addon.module.AmritaModule;
import dev.abstr3act.addon.utils.ToolUtil;
import kotlin.Pair;
import meteordevelopment.meteorclient.settings.IntSetting.Builder;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.screen.ingame.FurnaceScreen;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.*;
import net.minecraft.screen.slot.SlotActionType;

import java.util.*;
import java.util.Map.Entry;

public class InvManager extends AmritaModule {
    public static boolean cleaning = false;
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
    private final Setting<Integer> delay = this.sgGeneral
        .add(((Builder) ((Builder) ((Builder) new Builder().name("Delay")).description("")).defaultValue(0)).sliderRange(0, 10).build());
    private final Setting<Integer> armorDelay = this.sgGeneral
        .add(((Builder) ((Builder) ((Builder) new Builder().name("Armor Delay")).description("")).defaultValue(0)).sliderRange(0, 10).build());
    private final Setting<Boolean> cleaner = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("Cleaner"))
                .description("."))
                .defaultValue(true))
                .build()
        );
    private final Setting<Boolean> onlyInventory = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("Only Inventory"))
                .description("."))
                .defaultValue(true))
                .build()
        );
    private final Map<Item, Integer> itemSlots = new HashMap<Item, Integer>() {
        {
            this.put(Items.NETHERITE_SWORD, 0);
            this.put(Items.DIAMOND_SWORD, 0);
            this.put(Items.IRON_SWORD, 0);
            this.put(Items.GOLDEN_SWORD, 0);
            this.put(Items.STONE_SWORD, 0);
            this.put(Items.WOODEN_SWORD, 0);
            this.put(Items.NETHERITE_PICKAXE, 1);
            this.put(Items.DIAMOND_PICKAXE, 1);
            this.put(Items.IRON_PICKAXE, 1);
            this.put(Items.GOLDEN_PICKAXE, 1);
            this.put(Items.STONE_PICKAXE, 1);
            this.put(Items.WOODEN_PICKAXE, 1);
            this.put(Items.NETHERITE_AXE, 2);
            this.put(Items.DIAMOND_AXE, 2);
            this.put(Items.IRON_AXE, 2);
            this.put(Items.GOLDEN_AXE, 2);
            this.put(Items.STONE_AXE, 2);
            this.put(Items.WOODEN_AXE, 2);
            this.put(Items.NETHERITE_SHOVEL, 3);
            this.put(Items.DIAMOND_SHOVEL, 3);
            this.put(Items.IRON_SHOVEL, 3);
            this.put(Items.GOLDEN_SHOVEL, 3);
            this.put(Items.STONE_SHOVEL, 3);
            this.put(Items.WOODEN_SHOVEL, 3);
        }
    };
    private final Map<Item, Integer> swordMaterialRank = new HashMap<Item, Integer>() {
        {
            this.put(Items.NETHERITE_SWORD, 6);
            this.put(Items.DIAMOND_SWORD, 5);
            this.put(Items.IRON_SWORD, 4);
            this.put(Items.GOLDEN_SWORD, 3);
            this.put(Items.STONE_SWORD, 2);
            this.put(Items.WOODEN_SWORD, 1);
        }
    };
    private final Map<Item, Integer> pickaxeMaterialRank = new HashMap<Item, Integer>() {
        {
            this.put(Items.NETHERITE_PICKAXE, 6);
            this.put(Items.DIAMOND_PICKAXE, 5);
            this.put(Items.IRON_PICKAXE, 4);
            this.put(Items.GOLDEN_PICKAXE, 3);
            this.put(Items.STONE_PICKAXE, 2);
            this.put(Items.WOODEN_PICKAXE, 1);
        }
    };
    private final Map<Item, Integer> axeMaterialItem = new HashMap<Item, Integer>() {
        {
            this.put(Items.NETHERITE_AXE, 6);
            this.put(Items.DIAMOND_AXE, 5);
            this.put(Items.IRON_AXE, 4);
            this.put(Items.GOLDEN_AXE, 3);
            this.put(Items.STONE_AXE, 2);
            this.put(Items.WOODEN_AXE, 1);
        }
    };
    private final Map<Item, Integer> shovelMaterialItem = new HashMap<Item, Integer>() {
        {
            this.put(Items.NETHERITE_SHOVEL, 6);
            this.put(Items.DIAMOND_SHOVEL, 5);
            this.put(Items.IRON_SHOVEL, 4);
            this.put(Items.GOLDEN_SHOVEL, 3);
            this.put(Items.STONE_SHOVEL, 2);
            this.put(Items.WOODEN_SHOVEL, 1);
        }
    };
    private int tickCounter = 0;

    public InvManager() {
        super(Compassion.AMRITA, "InvManager", "");
    }

    public void onDeactivate() {
        this.tickCounter = 0;
        cleaning = false;
    }

    public void onActivate() {
    }

    @EventHandler
    public void onUpdate(EventUpdate event) {
        PlayerEntity player = this.mc.player;
        if (player != null) {
            if ((!this.onlyInventory.get() || this.mc.currentScreen instanceof InventoryScreen)
                && !(this.mc.currentScreen instanceof GenericContainerScreen)
                && !(this.mc.currentScreen instanceof FurnaceScreen)) {
                if (this.tickCounter > 0) {
                    cleaning = true;
                    this.tickCounter--;
                } else {
                    PlayerInventory inventory = player.getInventory();
                    ItemStack[] hotbarTargets = new ItemStack[9];

                    for (int i = 0; i < inventory.main.size(); i++) {
                        ItemStack stack = inventory.getStack(i);
                        Integer slot = this.itemSlots.get(stack.getItem());
                        if (slot != null) {
                            ItemStack targetStack = hotbarTargets[slot];
                            if (targetStack == null || this.compareItems(stack, targetStack) > 0) {
                                hotbarTargets[slot] = stack;
                            }
                        }
                    }

                    for (int slot = 0; slot < hotbarTargets.length; slot++) {
                        ItemStack targetStack = hotbarTargets[slot];
                        if (targetStack != null) {
                            int hotbarIndex = this.findItemInHotbar(inventory, targetStack.getItem());
                            if (hotbarIndex != slot) {
                                int inventoryIndex = -1;

                                for (int ix = 0; ix < inventory.main.size(); ix++) {
                                    ItemStack stack = inventory.getStack(ix);
                                    if (stack.getItem() == targetStack.getItem() && this.compareItems(stack, targetStack) == 0) {
                                        inventoryIndex = ix;
                                        break;
                                    }
                                }

                                if (inventoryIndex != -1) {
                                    cleaning = true;
                                    this.swapItems(inventoryIndex, slot);
                                    this.tickCounter = this.delay.get();
                                    return;
                                }
                            }
                        }
                    }

                    Map<EquipmentSlot, Pair<ItemStack, Integer>> armorCandidates = new HashMap<>();

                    for (int ixx = 0; ixx <= 35; ixx++) {
                        ItemStack stack = inventory.getStack(ixx);
                        if (stack.getItem() instanceof ArmorItem) {
                            EquipmentSlot armorEquipSlot = ToolUtil.getArmorEquipmentSlot(stack);
                            Pair<ItemStack, Integer> currentBest = armorCandidates.get(armorEquipSlot);
                            if (currentBest == null || ToolUtil.isBetterArmor(stack, (ItemStack) currentBest.getFirst())) {
                                armorCandidates.put(armorEquipSlot, new Pair(stack, ixx));
                            }
                        }
                    }

                    for (Entry<EquipmentSlot, Pair<ItemStack, Integer>> entry : armorCandidates.entrySet()) {
                        EquipmentSlot slotx = entry.getKey();
                        Pair<ItemStack, Integer> candidate = entry.getValue();
                        ItemStack bestArmor = (ItemStack) candidate.getFirst();
                        int inventoryIndex = candidate.getSecond();
                        ItemStack currentArmor = player.getEquippedStack(slotx);
                        if (currentArmor.isEmpty() || ToolUtil.isBetterArmor(bestArmor, currentArmor)) {
                            if (!currentArmor.isEmpty()) {
                                cleaning = true;
                                int armorSlot = ToolUtil.getItemSlotId(currentArmor);
                                this.mc.interactionManager.clickSlot(player.currentScreenHandler.syncId, armorSlot, 0, SlotActionType.THROW, player);
                                this.tickCounter = this.armorDelay.get();
                                return;
                            } else {
                                cleaning = true;
                                int slotIndex = inventoryIndex < 9 ? inventoryIndex + 36 : inventoryIndex;
                                this.mc.interactionManager.clickSlot(player.currentScreenHandler.syncId, slotIndex, 0, SlotActionType.QUICK_MOVE, player);
                                this.tickCounter = this.delay.get();
                                return;
                            }
                        }
                    }

                    if (this.cleaner.get()) {
                        ItemStack[] cleanerTargets = new ItemStack[9];
                        Set<Item> allowedItems = new HashSet<>(
                            Arrays.asList(
                                Items.ENDER_PEARL,
                                Items.ENDER_EYE,
                                Items.TRIDENT,
                                Items.MACE,
                                Items.BOW,
                                Items.CROSSBOW,
                                Items.ARROW,
                                Items.GOLDEN_APPLE,
                                Items.ENCHANTED_GOLDEN_APPLE,
                                Items.APPLE,
                                Items.MUSHROOM_STEW,
                                Items.BREAD,
                                Items.PORKCHOP,
                                Items.COOKED_PORKCHOP,
                                Items.GOLDEN_CARROT,
                                Items.CARROT,
                                Items.POTATO,
                                Items.BAKED_POTATO,
                                Items.COOKED_BEEF,
                                Items.BEEF,
                                Items.COOKED_CHICKEN,
                                Items.CHICKEN,
                                Items.COOKED_MUTTON,
                                Items.MUTTON,
                                Items.COOKED_RABBIT,
                                Items.RABBIT,
                                Items.RABBIT_STEW,
                                Items.BEETROOT,
                                Items.BEETROOT_SOUP,
                                Items.MELON_SLICE,
                                Items.PUMPKIN_PIE,
                                Items.COOKIE,
                                Items.SWEET_BERRIES,
                                Items.COD,
                                Items.COOKED_COD,
                                Items.SALMON,
                                Items.COOKED_SALMON,
                                Items.TROPICAL_FISH,
                                Items.PUFFERFISH,
                                Items.HONEY_BOTTLE,
                                Items.GLOW_BERRIES,
                                Items.DRIED_KELP,
                                Items.ROTTEN_FLESH,
                                Items.POISONOUS_POTATO,
                                Items.COMPASS,
                                Items.RECOVERY_COMPASS,
                                Items.WATER_BUCKET,
                                Items.ELYTRA,
                                Items.IRON_INGOT,
                                Items.DIAMOND,
                                Items.EMERALD,
                                Items.GOLD_INGOT,
                                Items.NETHERITE_INGOT
                            )
                        );

                        for (int ixxx = 0; ixxx <= 35; ixxx++) {
                            ItemStack stack = inventory.getStack(ixxx);
                            Integer slotx = this.itemSlots.get(stack.getItem());
                            if (slotx != null) {
                                ItemStack targetStack = cleanerTargets[slotx];
                                if (targetStack == null || this.compareItems(stack, targetStack) > 0) {
                                    cleanerTargets[slotx] = stack;
                                }
                            }
                        }

                        for (int ixxxx = 0; ixxxx <= 35; ixxxx++) {
                            ItemStack stack = inventory.getStack(ixxxx);
                            if (!stack.isEmpty()
                                && !(stack.getItem() instanceof BlockItem)
                                && !(stack.getItem() instanceof SpawnEggItem)
                                && !(stack.getItem() instanceof PotionItem)
                                && !(stack.getItem() instanceof SplashPotionItem)
                                && !(stack.getItem() instanceof LingeringPotionItem)
                                && !allowedItems.contains(stack.getItem())
                                && !Arrays.asList(cleanerTargets).contains(stack)) {
                                cleaning = true;
                                int button = stack.getCount() > 1 ? 1 : 0;
                                int slotx = ixxxx < 9 ? ixxxx + 36 : ixxxx;
                                this.mc.interactionManager.clickSlot(player.currentScreenHandler.syncId, slotx, button, SlotActionType.THROW, player);
                                this.tickCounter = this.delay.get();
                                return;
                            }
                        }
                    }

                    cleaning = false;
                }
            } else {
                cleaning = false;
                this.tickCounter = 0;
            }
        }
    }

    private int findItemInHotbar(PlayerInventory inventory, Item item) {
        for (int i = 0; i <= 8; i++) {
            if (inventory.getStack(i).getItem() == item) {
                return i;
            }
        }

        return -1;
    }

    private int compareItems(ItemStack stack1, ItemStack stack2) {
        Item item1 = stack1.getItem();
        Item item2 = stack2.getItem();
        int rank1 = 0;
        int rank2 = 0;
        if (item1 instanceof SwordItem) {
            rank1 = this.swordMaterialRank.get(item1);
            rank2 = this.swordMaterialRank.get(item2);
        }

        if (item1 instanceof MiningToolItem && item2 instanceof MiningToolItem) {
            if (item1 instanceof PickaxeItem && item2 instanceof PickaxeItem) {
                rank1 = this.pickaxeMaterialRank.get(item1);
                rank2 = this.pickaxeMaterialRank.get(item2);
            } else if (item1 instanceof AxeItem && item2 instanceof AxeItem) {
                rank1 = this.axeMaterialItem.get(item1);
                rank2 = this.axeMaterialItem.get(item2);
            } else if (item1 instanceof ShovelItem && item2 instanceof ShovelItem) {
                rank1 = this.shovelMaterialItem.get(item1);
                rank2 = this.shovelMaterialItem.get(item2);
            }
        }

        if (rank1 != rank2) {
            return Integer.compare(rank1, rank2);
        } else {
            if (item1 == item2) {
                int enchantments1 = stack1.getEnchantments().getEnchantments().size();
                int enchantments2 = stack2.getEnchantments().getEnchantments().size();
                if (enchantments1 != enchantments2) {
                    return Integer.compare(enchantments1, enchantments2);
                }
            }

            return 0;
        }
    }

    private void swapItems(int from, int to) {
        int syncId = this.mc.player.currentScreenHandler.syncId;
        int fromSlot = from < 9 ? from + 36 : from;
        if (fromSlot != to + 36) {
            this.mc.interactionManager.clickSlot(syncId, fromSlot, to, SlotActionType.SWAP, this.mc.player);
        }
    }
}
