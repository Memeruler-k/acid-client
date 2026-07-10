package dev.abstr3act.addon.modules.Seraphim;

import dev.abstr3act.addon.Compassion;
import dev.abstr3act.addon.events.EventPlayerUpdate;
import dev.abstr3act.addon.module.SeraphimModule;
import dev.abstr3act.addon.modules.Seraphim.clicker.Timer;
import dev.abstr3act.addon.utils.abnormally.AChatUtils;
import dev.abstr3act.addon.utils.math.MathUtility;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent.Post;
import meteordevelopment.meteorclient.settings.ItemListSetting.Builder;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.EnderChestBlockEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.Direction;

import java.util.List;
import java.util.Random;

public class ChestFiller extends SeraphimModule {
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
    private final Setting<List<Item>> items = this.sgGeneral.add(((Builder) ((Builder) new Builder().name("Items")).description(".")).build());
    private final Setting<Integer> delay = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("Delay"))
                .description("."))
                .defaultValue(100))
                .sliderRange(0, 1000)
                .build()
        );
    private final Setting<Boolean> random = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("Random"))
                .description("."))
                .defaultValue(false))
                .build()
        );
    private final Setting<Boolean> close = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("Close"))
                .description("."))
                .defaultValue(false))
                .build()
        );
    private final Setting<Boolean> autoMyst = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("AutoMyst"))
                .description("."))
                .defaultValue(false))
                .build()
        );
    private final Setting<Boolean> debug = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("Debug"))
                .description("."))
                .defaultValue(false))
                .build()
        );
    private final Setting<Sort> sort = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) new meteordevelopment.meteorclient.settings.EnumSetting.Builder()
                .name("Sort"))
                .description("."))
                .defaultValue(Sort.None))
                .build()
        );
    private final Setting<List<String>> ignoredContainer = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.StringListSetting.Builder) ((meteordevelopment.meteorclient.settings.StringListSetting.Builder) ((meteordevelopment.meteorclient.settings.StringListSetting.Builder) new meteordevelopment.meteorclient.settings.StringListSetting.Builder()
                .name("Ignored Container"))
                .description("."))
                .defaultValue(List.of("Shop")))
                .build()
        );
    private final Timer autoMystDelay = new Timer();
    private final Timer timer = new Timer();
    private final Random rnd = new Random();

    public ChestFiller() {
        super(Compassion.SERAPHIM, "ChestFiller", ".");
    }

    public static boolean containsAny(String field, List<String> stringList) {
        for (String str : stringList) {
            if (field.contains(str)) {
                return true;
            }
        }

        return false;
    }

    @EventHandler
    public void onTickEvent(Post event) {
        if (this.mc.player.currentScreenHandler instanceof GenericContainerScreenHandler && this.debug.get()) {
            AChatUtils.sendMsgSeraphim(this.mc.currentScreen.getTitle());
        }
    }

    public void displayGrid() {
        AChatUtils.sendMsgSeraphim(Text.of("[S] ■■■■■■■■■"));

        for (int row = 0; row < this.mc.player.currentScreenHandler.slots.size() / 9; row++) {
            StringBuilder builder = new StringBuilder();

            for (int col = 0; col < 9; col++) {
                int index = row * 9 + col;
                builder.append(this.mc.player.currentScreenHandler.getSlot(index).getStack().getItem().equals(Items.TOTEM_OF_UNDYING) ? "§a■§f" : "§c■§f");
            }

            AChatUtils.sendMsgSeraphim(Text.of("[" + (row == 0 ? "H" : row) + "] " + builder));
        }

        AChatUtils.sendMsgSeraphim(Text.of("[E] ■■■■■■■■■"));
    }

    @EventHandler
    public void onRender3D(Render3DEvent event) {
        if (this.mc.player.currentScreenHandler instanceof GenericContainerScreenHandler chest) {
            if (this.debug.get()) {
                AChatUtils.sendMsgSeraphim(Text.of(String.valueOf(this.mc.player.currentScreenHandler.slots.size())));
                AChatUtils.sendMsgSeraphim(Text.of("ChestSize = " + chest.getInventory().size()));
                AChatUtils.sendMsgSeraphim(Text.of("InventoryMainSize = " + this.mc.player.getInventory().main.size()));
                AChatUtils.sendMsgSeraphim(Text.of("InventorySize = " + this.mc.player.getInventory().size()));
                this.displayGrid();
                return;
            }

            if (containsAny(this.mc.currentScreen.getTitle().getString(), (List<String>) this.ignoredContainer.get())) {
                return;
            }

            if (this.isContainerFull(chest)) {
                if (this.close.get()) {
                    this.mc.player.closeHandledScreen();
                }

                return;
            }

            if (this.isInventoryEmpty()) {
                if (this.close.get()) {
                    this.mc.player.closeHandledScreen();
                }

                return;
            }

            for (int i = chest.getInventory().size(); i < this.mc.player.currentScreenHandler.slots.size(); i++) {
                ItemStack slot = chest.getInventory().getStack(i);
                if (slot.isEmpty()
                    && this.timer.every(this.delay.get() + (this.random.get() && this.delay.get() != 0 ? this.rnd.nextInt(this.delay.get()) : 0))
                    && !this.mc.currentScreen.getTitle().getString().contains("Аукцион")
                    && !this.mc.currentScreen.getTitle().getString().contains("покупки")) {
                    this.mc.interactionManager.clickSlot(this.mc.player.currentScreenHandler.syncId, i, 0, SlotActionType.QUICK_MOVE, this.mc.player);
                    this.autoMystDelay.reset();
                }
            }
        }
    }

    private boolean isContainerFull(GenericContainerScreenHandler chest) {
        for (int i = 0; i < chest.getInventory().size(); i++) {
            ItemStack slot = chest.getInventory().getStack(i);
            if (slot.isEmpty()) {
                return false;
            }
        }

        return true;
    }

    @EventHandler
    public void onPlayerUpdate(EventPlayerUpdate event) {
        if (this.autoMyst.get() && this.mc.currentScreen == null && this.autoMystDelay.passedMs(3000L)) {
            for (BlockEntity be : this.getBlockEntities()) {
                if (be instanceof EnderChestBlockEntity && !(this.mc.player.squaredDistanceTo(be.getPos().toCenterPos()) > 39.0)) {
                    this.mc
                        .interactionManager
                        .interactBlock(
                            this.mc.player,
                            Hand.MAIN_HAND,
                            new BlockHitResult(
                                be.getPos().toCenterPos().add(MathUtility.random(-0.4, 0.4), 0.375, MathUtility.random(-0.4, 0.4)), Direction.UP, be.getPos(), false
                            )
                        );
                    this.mc.player.swingHand(Hand.MAIN_HAND);
                    break;
                }
            }
        }
    }

    private boolean hasEmptySlot() {
        for (int i = 0; i < this.mc.player.getInventory().main.size(); i++) {
            ItemStack stack = this.mc.player.getInventory().getStack(i);
            if (stack.isEmpty()) {
                return true;
            }
        }

        return false;
    }

    private boolean isAllowed(ItemStack stack) {
        boolean allowed = (this.items.get()).contains(stack.getItem());

        return switch ((Sort) this.sort.get()) {
            case None -> true;
            case WhiteList -> allowed;
            default -> !allowed;
        };
    }

    private boolean isInventoryEmpty() {
        for (int i = 0; i < this.mc.player.getInventory().main.size(); i++) {
            if (!((ItemStack) this.mc.player.getInventory().main.get(i)).isEmpty()) {
                return false;
            }
        }

        return true;
    }

    private static enum Sort {
        None,
        WhiteList,
        BlackList;
    }
}
