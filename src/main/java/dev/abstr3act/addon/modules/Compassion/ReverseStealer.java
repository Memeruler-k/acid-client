package dev.abstr3act.addon.modules.Compassion;

import dev.abstr3act.addon.Compassion;
import dev.abstr3act.addon.module.CompassionModule;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.settings.BoolSetting.Builder;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;

public class ReverseStealer extends CompassionModule {
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
    public final Setting<Boolean> close = this.sgGeneral
        .add(((Builder) ((Builder) ((Builder) new Builder().name("AutoClose")).description(".")).defaultValue(true)).build());

    public ReverseStealer() {
        super(Compassion.COMPASSION, "ReverseStealer", ".");
    }

    @EventHandler
    public void onRender3D(Render3DEvent stack) {
        if (this.mc.player.currentScreenHandler instanceof GenericContainerScreenHandler chest) {
            for (int i = 0; i < this.mc.player.getInventory().size(); i++) {
                Slot slot = (Slot) this.mc.player.currentScreenHandler.slots.get(i);
                if (slot.hasStack() && !this.mc.currentScreen.getTitle().getString().contains("Kit") && !this.mc.currentScreen.getTitle().getString().contains("Shop")) {
                    this.mc.interactionManager.clickSlot(this.mc.player.currentScreenHandler.syncId, i, 0, SlotActionType.QUICK_MOVE, this.mc.player);
                }
            }

            for (int ix = 0; ix < chest.getInventory().size(); ix++) {
                Slot slot = chest.getSlot(ix);
                if (!slot.getStack().isEmpty() && this.close.get()) {
                    this.mc.player.closeHandledScreen();
                }
            }

            if (this.isPlayerInventoryEmpty() && this.close.get()) {
                this.mc.player.closeHandledScreen();
            }
        }
    }

    private boolean isPlayerInventoryEmpty() {
        for (int i = 0; i < this.mc.player.getInventory().size(); i++) {
            if (!this.mc.player.getInventory().getStack(i).isEmpty()) {
                return false;
            }
        }

        return true;
    }
}
