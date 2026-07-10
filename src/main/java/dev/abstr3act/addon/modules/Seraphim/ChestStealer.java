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
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.Direction;

import java.util.List;
import java.util.Random;

public class ChestStealer extends SeraphimModule {
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

    public ChestStealer() {
        super(Compassion.SERAPHIM, "ChestStealer", ".");
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

    @EventHandler
    public void onRender3D(Render3DEvent event) {
        if (!this.debug.get()) {
            if (this.mc.player.currentScreenHandler instanceof GenericContainerScreenHandler chest) {
                if (this.mc.currentScreen == null) {
                    return;
                }

                if (containsAny(this.mc.currentScreen.getTitle().getString(), (List<String>) this.ignoredContainer.get())) {
                    return;
                }

                for (int i = 0; i < chest.getInventory().size(); i++) {
                    Slot slot = chest.getSlot(i);
                    if (slot.hasStack()
                        && this.isAllowed(slot.getStack())
                        && this.timer.every(this.delay.get() + (this.random.get() && this.delay.get() != 0 ? this.rnd.nextInt(this.delay.get()) : 0))
                        && !this.mc.currentScreen.getTitle().getString().contains("Аукцион")
                        && !this.mc.currentScreen.getTitle().getString().contains("покупки")) {
                        this.mc.interactionManager.clickSlot(this.mc.player.currentScreenHandler.syncId, i, 0, SlotActionType.QUICK_MOVE, this.mc.player);
                        this.autoMystDelay.reset();
                    }
                }

                if ((this.isContainerEmpty(chest) || !this.hasEmptySlot()) && this.close.get()) {
                    this.mc.player.closeHandledScreen();
                }
            }
        }
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

    private boolean isContainerEmpty(GenericContainerScreenHandler container) {
        for (int i = 0; i < (container.getInventory().size() == 90 ? 54 : 27); i++) {
            if (container.getSlot(i).hasStack()) {
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
