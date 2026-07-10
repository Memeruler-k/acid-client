package dev.abstr3act.addon.modules.Selena;

import dev.abstr3act.addon.Compassion;
import dev.abstr3act.addon.module.SelenaModule;
import meteordevelopment.meteorclient.events.world.TickEvent.Pre;
import meteordevelopment.meteorclient.settings.IntSetting.Builder;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket.Action;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class GrimTridentDisabler extends SelenaModule {
    private final SettingGroup settingsModule = this.settings.getDefaultGroup();
    private final Setting<Integer> tridentDelay = this.settingsModule
        .add(
            ((Builder) ((Builder) ((Builder) new Builder().name("Trident delay")).description("delay (in ticks) between trident uses"))
                .sliderRange(0, 20)
                .range(0, 20)
                .defaultValue(0))
                .build()
        );
    private final Setting<Boolean> pauseOnEat = this.settingsModule
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("Pause on eat"))
                .description("Pauses when eating."))
                .defaultValue(false))
                .build()
        );
    private int currentTick = 0;

    public GrimTridentDisabler() {
        super(Compassion.SELENA, "GrimDisabler", "Disables GrimAC move checks (need a trident with Riptide III) [Patched]");
    }

    public void onActivate() {
        this.currentTick = this.tridentDelay.get();
    }

    @EventHandler
    private void onTick(Pre event) {
        if (this.currentTick >= this.tridentDelay.get()) {
            this.currentTick = 0;

            assert this.mc.player != null;

            int tridentSlot = InvUtils.findInHotbar(new Item[]{Items.TRIDENT}).slot();
            int oldSlot = this.mc.player.getInventory().selectedSlot;
            if (tridentSlot == -1 || this.pauseOnEat.get() && this.mc.player.isUsingItem()) {
                return;
            }

            this.mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(tridentSlot));
            this.mc.player.networkHandler.sendPacket(new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, 0, this.mc.player.getYaw(), this.mc.player.getPitch()));
            this.mc.player.networkHandler.sendPacket(new PlayerActionC2SPacket(Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, Direction.DOWN));
            this.mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(oldSlot));
        } else {
            this.currentTick++;
        }
    }
}
