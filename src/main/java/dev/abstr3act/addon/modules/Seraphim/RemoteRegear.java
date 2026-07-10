package dev.abstr3act.addon.modules.Seraphim;

import dev.abstr3act.addon.Compassion;
import dev.abstr3act.addon.command.TestCommand;
import dev.abstr3act.addon.module.SeraphimModule;
import dev.abstr3act.addon.modules.Seraphim.clicker.Timer;
import dev.abstr3act.addon.utils.abnormally.AChatUtils;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.settings.ItemListSetting.Builder;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;

import java.util.List;
import java.util.Random;

public class RemoteRegear extends SeraphimModule {
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
    private final Setting<Sort> sort = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) new meteordevelopment.meteorclient.settings.EnumSetting.Builder()
                .name("Sort"))
                .description("."))
                .defaultValue(Sort.None))
                .build()
        );
    private final Timer autoMystDelay = new Timer();
    private final Timer timer = new Timer();
    private final Random rnd = new Random();

    public RemoteRegear() {
        super(Compassion.SERAPHIM, "RemoteRegear", ".");
    }

    @EventHandler
    public void onRender3D(Render3DEvent event) {
        if (TestCommand.savedScreenHandler == null) {
            AChatUtils.sendMsgSeraphim(Text.of("Please save screen handler."));
            this.toggle();
        } else {
            if (this.mc.player.currentScreenHandler instanceof GenericContainerScreenHandler chest) {
                for (int i = 0; i < chest.getInventory().size(); i++) {
                    Slot slot = chest.getSlot(i);
                    if (slot.hasStack() && this.isAllowed(slot.getStack())) {
                        this.mc.interactionManager.clickSlot(this.mc.player.currentScreenHandler.syncId, i, 0, SlotActionType.QUICK_MOVE, this.mc.player);
                        this.autoMystDelay.reset();
                    }
                }

                AChatUtils.sendMsgSeraphim(Text.of("Successfully regeared."));
                this.toggle();
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
