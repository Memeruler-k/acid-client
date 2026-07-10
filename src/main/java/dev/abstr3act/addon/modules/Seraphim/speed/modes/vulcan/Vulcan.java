package dev.abstr3act.addon.modules.Seraphim.speed.modes.vulcan;

import dev.abstr3act.addon.modules.Seraphim.speed.SpeedMode;
import dev.abstr3act.addon.modules.Seraphim.speed.SpeedModes;
import dev.abstr3act.addon.utils.seraphim.movement.CustomSpeedUtils;
import meteordevelopment.meteorclient.events.entity.player.PlayerMoveEvent;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.player.SlotUtils;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.Item;
import net.minecraft.item.Items;

import java.util.Objects;

public class Vulcan extends SpeedMode {
    public Item chestPlate;

    public Vulcan() {
        super(SpeedModes.Vulcan);
    }

    @Override
    public void onDeactivate() {
        FindItemResult chest = InvUtils.find(new Item[]{this.chestPlate});
        if (chest.found() && this.mc.player.getEquippedStack(EquipmentSlot.CHEST).getItem() == Items.ELYTRA && this.settings.autoSwapVulcan.get()) {
            InvUtils.move().from(chest.slot()).toArmor(2);
        }
    }

    @Override
    public void onActivate() {
        FindItemResult elytra = InvUtils.find(new Item[]{Items.ELYTRA});
        if (!elytra.found()) {
            this.settings.error("Elytra not found", new Object[0]);
            this.settings.toggle();
        } else if (!SlotUtils.isArmor(elytra.slot())
            && this.settings.autoSwapVulcan.get()
            && this.mc.player.getEquippedStack(EquipmentSlot.CHEST).getItem() != Items.ELYTRA) {
            this.chestPlate = this.mc.player.getEquippedStack(EquipmentSlot.CHEST).getItem();
            InvUtils.move().from(elytra.slot()).toArmor(2);
        }
    }

    @Override
    public void onPlayerMoveEvent(PlayerMoveEvent event) {
        if (this.mc.player != null && this.mc.player.getEquippedStack(EquipmentSlot.CHEST).getItem() == Items.ELYTRA) {
            if (this.mc.player.hasStatusEffect(StatusEffects.SPEED) && this.mc.player.getStatusEffect(StatusEffects.SPEED) != null) {
                if (Objects.requireNonNull(this.mc.player.getStatusEffect(StatusEffects.SPEED)).getAmplifier() == 1) {
                    CustomSpeedUtils.applySpeed(event, this.settings.speedVulcanef2.get());
                } else if (Objects.requireNonNull(this.mc.player.getStatusEffect(StatusEffects.SPEED)).getAmplifier() == 0) {
                    CustomSpeedUtils.applySpeed(event, this.settings.speedVulcanef1.get());
                }
            } else {
                CustomSpeedUtils.applySpeed(event, this.settings.speedVulcanef0.get());
            }
        }
    }
}
