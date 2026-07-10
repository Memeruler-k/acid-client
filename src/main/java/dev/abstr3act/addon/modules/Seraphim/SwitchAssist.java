package dev.abstr3act.addon.modules.Seraphim;

import dev.abstr3act.addon.Compassion;
import dev.abstr3act.addon.module.SeraphimModule;
import dev.abstr3act.addon.utils.InventoryUtility;
import meteordevelopment.meteorclient.events.entity.player.AttackEntityEvent;
import meteordevelopment.meteorclient.settings.IntSetting.Builder;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.item.SwordItem;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;

public class SwitchAssist extends SeraphimModule {
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
    private final Setting<Integer> swapDelay = this.sgGeneral
        .add(((Builder) ((Builder) ((Builder) new Builder().name("SwapDelay (ms)")).description(".")).defaultValue(2)).min(1).sliderRange(1, 2000).build());
    private final Setting<Integer> swapBackDelay = this.sgGeneral
        .add(((Builder) ((Builder) ((Builder) new Builder().name("SwapBackDelay (ms)")).description(".")).defaultValue(2)).min(1).sliderRange(1, 2000).build());
    private final Setting<Boolean> silent = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("Silent"))
                .description("."))
                .defaultValue(true))
                .build()
        );

    public SwitchAssist() {
        super(Compassion.SERAPHIM, "SwitchAssist", ".");
    }

    @EventHandler
    public void onAttackEntity(AttackEntityEvent event) {
        if (event.entity instanceof LivingEntity) {
            FindItemResult mace = InvUtils.find(new Item[]{Items.MACE});
            if (InventoryUtility.findSwordItem(this.mc.player.getInventory()) != -1 && mace.found()) {
                if (InventoryUtility.findMace() >= 0) {
                    if (this.mc.player.getMainHandStack().getItem() instanceof SwordItem) {
                        wait(() -> {
                            if (this.silent.get()) {
                                this.sendPacket(new UpdateSelectedSlotC2SPacket(InventoryUtility.findMace()));
                            } else {
                                InvUtils.swap(InventoryUtility.findMace(), true);
                            }
                        }, (this.swapDelay.get()).longValue()).thenRun(() -> wait(() -> {
                            if (this.silent.get()) {
                                this.sendPacket(new UpdateSelectedSlotC2SPacket(this.mc.player.getInventory().selectedSlot));
                            } else {
                                InvUtils.swapBack();
                            }
                        }, (this.swapBackDelay.get()).longValue()));
                    }
                }
            }
        }
    }
}
