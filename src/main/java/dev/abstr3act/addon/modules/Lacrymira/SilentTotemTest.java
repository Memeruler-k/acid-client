package dev.abstr3act.addon.modules.Lacrymira;

import dev.abstr3act.addon.Compassion;
import dev.abstr3act.addon.module.LacrymiraModule;
import dev.abstr3act.addon.utils.math.InventoryUtility;
import meteordevelopment.meteorclient.events.world.TickEvent.Post;
import meteordevelopment.meteorclient.settings.EnumSetting.Builder;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.slot.SlotActionType;

public class SilentTotemTest extends LacrymiraModule {
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
    private final Setting<Mode> mode = this.sgGeneral
        .add(((Builder) ((Builder) ((Builder) new Builder().name("Mode")).description("mode")).defaultValue(Mode.Normal)).build());

    public SilentTotemTest() {
        super(Compassion.LACRYMIRA, "AntiMissV2", " ");
    }

    public void onDeactivate() {
        if (((Mode) this.mode.get()).equals(Mode.Silent)) {
            this.sendPacket(new UpdateSelectedSlotC2SPacket(this.mc.player.getInventory().selectedSlot));
        }
    }

    public String getInfoString() {
        return ((Mode) this.mode.get()).name();
    }

    @EventHandler
    public void onUpdate(Post e) {
        if (!(this.mc.currentScreen instanceof InventoryScreen) && !(this.mc.player.currentScreenHandler instanceof GenericContainerScreenHandler)) {
            int totemSlot = this.findTotemSlot();
            int emptyHotbarSlot = this.findEmptyHotbarSlot();
            if (totemSlot != -1 && emptyHotbarSlot != -1) {
                this.moveItem(totemSlot, emptyHotbarSlot);
            }

            if (InventoryUtility.findItemInHotBar(Items.TOTEM_OF_UNDYING).found()) {
                if (((Mode) this.mode.get()).equals(Mode.Silent)) {
                    this.sendPacket(new UpdateSelectedSlotC2SPacket(InventoryUtility.findItemInHotBar(Items.TOTEM_OF_UNDYING).slot()));
                } else {
                    InventoryUtility.findItemInHotBar(Items.TOTEM_OF_UNDYING).switchTo();
                }
            }
        }
    }

    private int findTotemSlot() {
        for (int i = 9; i < 36; i++) {
            ItemStack stack = this.mc.player.getInventory().getStack(i);
            if (stack.getItem() == Items.TOTEM_OF_UNDYING) {
                return i;
            }
        }

        return -1;
    }

    private int findEmptyHotbarSlot() {
        for (int i = 0; i < 9; i++) {
            if (this.mc.player.getInventory().getStack(i).isEmpty()) {
                return i;
            }
        }

        return -1;
    }

    private void moveItem(int fromSlot, int toSlot) {
        this.clickSlot(fromSlot, toSlot, SlotActionType.SWAP);
    }

    static enum Mode {
        Silent,
        Normal;
    }
}
