package dev.abstr3act.addon.modules.Seraphim;

import dev.abstr3act.addon.Compassion;
import dev.abstr3act.addon.module.SeraphimModule;
import dev.abstr3act.addon.utils.abnormally.AChatUtils;
import meteordevelopment.meteorclient.events.world.TickEvent.Pre;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.StringSetting.Builder;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.collection.DefaultedList;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

public class AutoShop extends SeraphimModule {
    private static final int[] operations = new int[]{64, 10, 1, -64, -10, -1};
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
    private final SettingGroup sgShop = this.settings.createGroup("Shop");
    private final Setting<String> shop_command = this.sgShop
        .add(((Builder) ((Builder) ((Builder) new Builder().name("Shop Command")).description(".")).defaultValue("/shop")).build());
    private final Setting<Item> end_shop = this.sgShop
        .add(
            ((meteordevelopment.meteorclient.settings.ItemSetting.Builder) ((meteordevelopment.meteorclient.settings.ItemSetting.Builder) ((meteordevelopment.meteorclient.settings.ItemSetting.Builder) new meteordevelopment.meteorclient.settings.ItemSetting.Builder()
                .name("End Shop"))
                .description("."))
                .defaultValue(Items.AIR))
                .build()
        );
    private final Setting<Item> nether_shop = this.sgShop
        .add(
            ((meteordevelopment.meteorclient.settings.ItemSetting.Builder) ((meteordevelopment.meteorclient.settings.ItemSetting.Builder) ((meteordevelopment.meteorclient.settings.ItemSetting.Builder) new meteordevelopment.meteorclient.settings.ItemSetting.Builder()
                .name("Nether Shop"))
                .description("."))
                .defaultValue(Items.AIR))
                .build()
        );
    private final Setting<Item> gear_shop = this.sgShop
        .add(
            ((meteordevelopment.meteorclient.settings.ItemSetting.Builder) ((meteordevelopment.meteorclient.settings.ItemSetting.Builder) ((meteordevelopment.meteorclient.settings.ItemSetting.Builder) new meteordevelopment.meteorclient.settings.ItemSetting.Builder()
                .name("Gear Shop"))
                .description("."))
                .defaultValue(Items.AIR))
                .build()
        );
    private final Setting<Item> food_shop = this.sgShop
        .add(
            ((meteordevelopment.meteorclient.settings.ItemSetting.Builder) ((meteordevelopment.meteorclient.settings.ItemSetting.Builder) ((meteordevelopment.meteorclient.settings.ItemSetting.Builder) new meteordevelopment.meteorclient.settings.ItemSetting.Builder()
                .name("Food Shop"))
                .description("."))
                .defaultValue(Items.AIR))
                .build()
        );
    private final Setting<Item> shard_shop = this.sgShop
        .add(
            ((meteordevelopment.meteorclient.settings.ItemSetting.Builder) ((meteordevelopment.meteorclient.settings.ItemSetting.Builder) ((meteordevelopment.meteorclient.settings.ItemSetting.Builder) new meteordevelopment.meteorclient.settings.ItemSetting.Builder()
                .name("Shard Shop"))
                .description("."))
                .defaultValue(Items.AIR))
                .build()
        );
    private final Setting<TargetShop> targetShop = this.sgShop
        .add(
            ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) new meteordevelopment.meteorclient.settings.EnumSetting.Builder()
                .name("TargetShop"))
                .description("."))
                .defaultValue(TargetShop.Gear))
                .build()
        );
    private final Setting<Item> confirm_item = this.sgShop
        .add(
            ((meteordevelopment.meteorclient.settings.ItemSetting.Builder) ((meteordevelopment.meteorclient.settings.ItemSetting.Builder) ((meteordevelopment.meteorclient.settings.ItemSetting.Builder) new meteordevelopment.meteorclient.settings.ItemSetting.Builder()
                .name("Confirm Item"))
                .description("."))
                .defaultValue(Items.AIR))
                .build()
        );
    private final Setting<Item> exit_item = this.sgShop
        .add(
            ((meteordevelopment.meteorclient.settings.ItemSetting.Builder) ((meteordevelopment.meteorclient.settings.ItemSetting.Builder) ((meteordevelopment.meteorclient.settings.ItemSetting.Builder) new meteordevelopment.meteorclient.settings.ItemSetting.Builder()
                .name("Exit Item"))
                .description("."))
                .defaultValue(Items.AIR))
                .build()
        );
    private final Setting<String> shard_key_string = this.sgShop
        .add(((Builder) ((Builder) ((Builder) new Builder().name("Target Shard Key Name")).description(".")).defaultValue("Common Key")).build());
    private final Setting<Item> target_item = this.sgShop
        .add(
            ((meteordevelopment.meteorclient.settings.ItemSetting.Builder) ((meteordevelopment.meteorclient.settings.ItemSetting.Builder) ((meteordevelopment.meteorclient.settings.ItemSetting.Builder) new meteordevelopment.meteorclient.settings.ItemSetting.Builder()
                .name("Target Item"))
                .description("."))
                .defaultValue(Items.AIR))
                .build()
        );
    private final Setting<Boolean> totem_fill = this.sgShop
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("TotemRefill"))
                .description("Refill totems"))
                .defaultValue(false))
                .visible(() -> ((Item) this.target_item.get()).equals(Items.TOTEM_OF_UNDYING)))
                .build()
        );
    private final Setting<Boolean> silent = this.sgShop
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("Silent"))
                .description("."))
                .defaultValue(false))
                .build()
        );
    private final Setting<Integer> delay = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("Delay (ms)"))
                .description("."))
                .defaultValue(20))
                .min(1)
                .sliderRange(1, 5000)
                .build()
        );
    boolean processed = false;
    Progress progress = Progress.Open;
    int count = 0;

    public AutoShop() {
        super(Compassion.SERAPHIM, "AutoShop", "donut-smp autoRe-gear.");
    }

    public static int getEmptySlots(PlayerEntity player) {
        DefaultedList<ItemStack> mainInventory = player.getInventory().main;
        int emptySlots = 0;

        for (ItemStack stack : mainInventory) {
            if (stack.isEmpty()) {
                emptySlots++;
            }
        }

        return emptySlots;
    }

    public static Result findStep(int target) {
        Queue<State> q = new LinkedList<>();
        Set<Integer> v = new HashSet<>();
        q.offer(new State(0, 0, new int[operations.length]));
        v.add(0);

        while (!q.isEmpty()) {
            State state = q.poll();
            int cv = state.value;
            int cs = state.steps;
            int[] co = state.operationCounts;
            if (cv == target) {
                return new Result(cs, co);
            }

            for (int i = 0; i < operations.length; i++) {
                int nextValue = cv + operations[i];
                if (!v.contains(nextValue) && Math.abs(nextValue) <= Math.abs(target) + 64) {
                    int[] nextOperationCounts = (int[]) co.clone();
                    nextOperationCounts[i]++;
                    v.add(nextValue);
                    q.offer(new State(nextValue, cs + 1, nextOperationCounts));
                }
            }
        }

        return null;
    }

    public void onActivate() {
        this.progress = Progress.Open;
    }

    @EventHandler
    private void onTickEvent(Pre event) {
        if (this.mc.currentScreen instanceof GenericContainerScreen screen) {
            AChatUtils.sendMsgSeraphim(screen.getTitle());
        }

        switch (this.progress) {
            case Open:
                this.mc.player.networkHandler.sendCommand((String) this.shop_command.get());
                this.progress = Progress.Select;
                break;
            case Select:
                if (this.mc.player.currentScreenHandler instanceof GenericContainerScreenHandler shopx && this.mc.currentScreen.getTitle().getString().contains("ѕʜᴏᴘ")
                ) {
                    for (int ixxxxx = 0; ixxxxx < shopx.getInventory().size(); ixxxxx++) {
                        Slot slot = shopx.getSlot(ixxxxx);
                        if (slot.hasStack() && slot.getStack().getItem().equals(this.getShop((TargetShop) this.targetShop.get()))) {
                            this.mc.interactionManager.clickSlot(this.mc.player.currentScreenHandler.syncId, ixxxxx, 0, SlotActionType.PICKUP, this.mc.player);
                            this.progress = Progress.Item;
                        }
                    }
                }
                break;
            case Item:
                if (this.mc.player.currentScreenHandler instanceof GenericContainerScreenHandler shopx && this.mc.currentScreen.getTitle().getString().contains("ѕʜᴏᴘ")
                ) {
                    for (int ixxxx = 0; ixxxx < shopx.getInventory().size(); ixxxx++) {
                        Slot slot = shopx.getSlot(ixxxx);
                        if (slot.hasStack() && slot.getStack().getItem().equals(this.target_item.get())) {
                            this.mc.interactionManager.clickSlot(this.mc.player.currentScreenHandler.syncId, ixxxx, 0, SlotActionType.PICKUP, this.mc.player);
                            this.progress = Progress.Buy;
                        }
                    }
                }
                break;
            case Buy:
                if (this.mc.player.currentScreenHandler instanceof GenericContainerScreenHandler shop
                    && this.mc.currentScreen.getTitle().getString().contains("ʙᴜʏɪɴɢ")) {
                    if (((Item) this.target_item.get()).getDefaultStack().isStackable()) {
                        for (int ix = 0; ix < shop.getInventory().size(); ix++) {
                            Slot slot = shop.getSlot(ix);
                            if (slot.hasStack() && slot.getStack().getItem().equals(this.confirm_item.get()) && slot.getStack().getName().getString().contains("Set to")) {
                                this.mc.interactionManager.clickSlot(this.mc.player.currentScreenHandler.syncId, ix, 0, SlotActionType.PICKUP, this.mc.player);
                                this.progress = Progress.Add;
                            }
                        }
                    } else if (this.totem_fill.get() && ((Item) this.target_item.get()).equals(Items.TOTEM_OF_UNDYING)) {
                        if (getEmptySlots(this.mc.player) > 0) {
                            for (int ixx = 0; ixx < shop.getInventory().size(); ixx++) {
                                Slot slot = shop.getSlot(ixx);
                                if (slot.hasStack()
                                    && slot.getStack().getItem().equals(this.confirm_item.get())
                                    && slot.getStack().getCount() == 1
                                    && !slot.getStack().getName().getString().contains("Add")) {
                                    this.mc.interactionManager.clickSlot(this.mc.player.currentScreenHandler.syncId, ixx, 0, SlotActionType.PICKUP, this.mc.player);
                                    this.progress = Progress.Buy;
                                }
                            }
                        } else {
                            this.progress = Progress.Open;
                            this.mc.currentScreen.close();
                            this.sendPacket(new CloseHandledScreenC2SPacket(this.mc.player.currentScreenHandler.syncId));
                            this.toggle();
                        }
                    } else {
                        for (int ixxx = 0; ixxx < shop.getInventory().size(); ixxx++) {
                            Slot slot = shop.getSlot(ixxx);
                            if (slot.hasStack()
                                && slot.getStack().getItem().equals(this.confirm_item.get())
                                && slot.getStack().getCount() == 1
                                && !slot.getStack().getName().getString().contains("Add")) {
                                this.mc.interactionManager.clickSlot(this.mc.player.currentScreenHandler.syncId, ixxx, 0, SlotActionType.PICKUP, this.mc.player);
                                this.progress = Progress.Open;
                                this.mc.currentScreen.close();
                                this.sendPacket(new CloseHandledScreenC2SPacket(this.mc.player.currentScreenHandler.syncId));
                                this.toggle();
                            }
                        }
                    }
                }
                break;
            case Add:
                if (this.mc.player.currentScreenHandler instanceof GenericContainerScreenHandler shop
                    && this.mc.currentScreen.getTitle().getString().contains("ʙᴜʏɪɴɢ")) {
                    for (int i = 0; i < shop.getInventory().size(); i++) {
                        Slot slot = shop.getSlot(i);
                        if (slot.hasStack()
                            && slot.getStack().getItem().equals(this.confirm_item.get())
                            && slot.getStack().getCount() == 1
                            && !slot.getStack().getName().getString().contains("Add")) {
                            this.mc.interactionManager.clickSlot(this.mc.player.currentScreenHandler.syncId, i, 0, SlotActionType.PICKUP, this.mc.player);
                            this.progress = Progress.Open;
                            this.mc.currentScreen.close();
                            this.sendPacket(new CloseHandledScreenC2SPacket(this.mc.player.currentScreenHandler.syncId));
                            this.toggle();
                        }
                    }
                }
        }
    }

    private Item getShop(TargetShop targetShop) {
        switch (targetShop) {
            case End:
                return (Item) this.end_shop.get();
            case Nether:
                return (Item) this.nether_shop.get();
            case Gear:
                return (Item) this.gear_shop.get();
            case Food:
                return (Item) this.food_shop.get();
            case Shard:
                return (Item) this.shard_shop.get();
            default:
                return (Item) this.gear_shop.get();
        }
    }

    public String getInfoString() {
        return "DonutSMP";
    }

    static enum Progress {
        Open,
        Select,
        Item,
        Buy,
        Add;
    }

    static enum TargetShop {
        End,
        Nether,
        Gear,
        Food,
        Shard;
    }

    static class Result {
        int totalSteps;
        int[] operationCounts;

        Result(int totalSteps, int[] operationCounts) {
            this.totalSteps = totalSteps;
            this.operationCounts = operationCounts;
        }
    }

    static class State {
        int value;
        int steps;
        int[] operationCounts;

        State(int value, int steps, int[] operationCounts) {
            this.value = value;
            this.steps = steps;
            this.operationCounts = operationCounts;
        }
    }
}
