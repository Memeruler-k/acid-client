package dev.abstr3act.addon.modules.Amrita;

import dev.abstr3act.addon.Compassion;
import dev.abstr3act.addon.module.AmritaModule;
import dev.abstr3act.addon.modules.Seraphim.clicker.Timer;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.settings.ItemListSetting.Builder;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;

import java.util.List;

public class InventoryCleaner extends AmritaModule {
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
    private final Setting<List<Item>> items = this.sgGeneral.add(((Builder) ((Builder) new Builder().name("Items")).description("")).build());
    private final Setting<DropWhen> dropWhen = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) new meteordevelopment.meteorclient.settings.EnumSetting.Builder()
                .name("DropWhen"))
                .description(""))
                .defaultValue(DropWhen.Inventory))
                .build()
        );
    private final Setting<Boolean> cleanChests = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("CleanChests"))
                .description("."))
                .defaultValue(false))
                .build()
        );
    private final Setting<Integer> delay = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("Delay"))
                .description(""))
                .defaultValue(60))
                .sliderRange(0, 500)
                .build()
        );
    private final Timer delayTimer = new Timer();
    private boolean dirty;

    public InventoryCleaner() {
        super(Compassion.AMRITA, "InventoryCleaner", ".");
    }

    @EventHandler
    public void onRender3D(Render3DEvent stack) {
        boolean inInv = this.mc.currentScreen instanceof InventoryScreen;
        if (this.mc.player.currentScreenHandler instanceof GenericContainerScreenHandler chest && this.cleanChests.get()) {
            for (int i = 0; i < chest.getInventory().size(); i++) {
                Slot slot = chest.getSlot(i);
                if (slot.hasStack()
                    && this.dropThisShit(slot.getStack())
                    && !this.mc.currentScreen.getTitle().getString().contains("Аукцион")
                    && !this.mc.currentScreen.getTitle().getString().contains("покупки")
                    && this.delayTimer.every((this.delay.get()).intValue())) {
                    this.mc.interactionManager.clickSlot(this.mc.player.currentScreenHandler.syncId, i, 1, SlotActionType.THROW, this.mc.player);
                    this.dirty = true;
                }
            }
        }

        if (this.dropWhen.get() != DropWhen.Inventory || inInv) {
            if (this.dropWhen.get() != DropWhen.NotInInventory || !inInv) {
                for (int slot = 0; slot < 36; slot++) {
                    ItemStack itemFromslot = this.mc.player.getInventory().getStack(slot);
                    if (this.dropThisShit(itemFromslot)) {
                        this.drop(slot);
                    }
                }

                if (this.dirty && this.delayTimer.passedMs(this.delay.get() + 100)) {
                    this.sendPacket(new CloseHandledScreenC2SPacket(this.mc.player.currentScreenHandler.syncId));
                    this.dirty = false;
                }
            }
        }
    }

    private void drop(int slot) {
        if (this.delayTimer.every((this.delay.get()).intValue())) {
            this.mc.interactionManager.clickSlot(this.mc.player.currentScreenHandler.syncId, slot < 9 ? slot + 36 : slot, 1, SlotActionType.THROW, this.mc.player);
            this.dirty = true;
        }
    }

    private boolean dropThisShit(ItemStack stack) {
        return (this.items.get()).contains(stack.getItem());
    }

    public static enum DropWhen {
        Inventory,
        Always,
        NotInInventory;
    }
}
