package dev.abstr3act.addon.modules.Seraphim;

import dev.abstr3act.addon.Compassion;
import dev.abstr3act.addon.module.SeraphimModule;
import dev.abstr3act.addon.utils.TargetUtils;
import meteordevelopment.meteorclient.events.entity.player.AttackEntityEvent;
import meteordevelopment.meteorclient.events.world.TickEvent.Pre;
import meteordevelopment.meteorclient.settings.IntSetting.Builder;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.utils.entity.SortPriority;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.util.Hand;

public class AutoKitPVP extends SeraphimModule {
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
    private final SettingGroup sgHeal = this.settings.createGroup("OnLowHealth");
    private final SettingGroup sgOnAttack = this.settings.createGroup("OnAttack");
    private final SettingGroup sgOnTimer = this.settings.createGroup("OnTimer");
    private final SettingGroup sgInRange = this.settings.createGroup("InRange");
    private final Setting<Integer> delayHeal = this.sgHeal
        .add(((Builder) ((Builder) ((Builder) new Builder().name("HealingDelay")).description(".")).defaultValue(200)).sliderRange(1, 2000).build());
    private final Setting<String> healItem = this.sgHeal
        .add(
            ((meteordevelopment.meteorclient.settings.StringSetting.Builder) ((meteordevelopment.meteorclient.settings.StringSetting.Builder) ((meteordevelopment.meteorclient.settings.StringSetting.Builder) new meteordevelopment.meteorclient.settings.StringSetting.Builder()
                .name("AttackItemName"))
                .description("Item name"))
                .defaultValue(""))
                .build()
        );
    private final Setting<Integer> health = this.sgHeal
        .add(((Builder) ((Builder) ((Builder) new Builder().name("Health")).description(".")).defaultValue(20)).sliderRange(0, 100).build());
    private final Setting<Integer> delayAttack = this.sgOnAttack
        .add(((Builder) ((Builder) ((Builder) new Builder().name("AttackDelay")).description(".")).defaultValue(15)).sliderRange(1, 2000).build());
    private final Setting<String> attackItem = this.sgOnAttack
        .add(
            ((meteordevelopment.meteorclient.settings.StringSetting.Builder) ((meteordevelopment.meteorclient.settings.StringSetting.Builder) ((meteordevelopment.meteorclient.settings.StringSetting.Builder) new meteordevelopment.meteorclient.settings.StringSetting.Builder()
                .name("AttackItemName"))
                .description("Item name"))
                .defaultValue(""))
                .build()
        );
    private final Setting<Integer> timerDelay = this.sgOnTimer
        .add(((Builder) ((Builder) ((Builder) new Builder().name("TimerDelay")).description(".")).defaultValue(15)).sliderRange(1, 2000).build());
    private final Setting<String> timerItem = this.sgOnTimer
        .add(
            ((meteordevelopment.meteorclient.settings.StringSetting.Builder) ((meteordevelopment.meteorclient.settings.StringSetting.Builder) ((meteordevelopment.meteorclient.settings.StringSetting.Builder) new meteordevelopment.meteorclient.settings.StringSetting.Builder()
                .name("TimerItemName"))
                .description("Item name"))
                .defaultValue(""))
                .build()
        );
    private final Setting<Integer> delayInRange = this.sgInRange
        .add(((Builder) ((Builder) ((Builder) new Builder().name("RangeDelay")).description(".")).defaultValue(15)).sliderRange(1, 2000).build());
    private final Setting<Double> range = this.sgInRange
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("Range"))
                .description("."))
                .defaultValue(6.0)
                .sliderRange(1.0, 10.0)
                .build()
        );
    private final Setting<String> rangeItem = this.sgInRange
        .add(
            ((meteordevelopment.meteorclient.settings.StringSetting.Builder) ((meteordevelopment.meteorclient.settings.StringSetting.Builder) ((meteordevelopment.meteorclient.settings.StringSetting.Builder) new meteordevelopment.meteorclient.settings.StringSetting.Builder()
                .name("RangeItemName"))
                .description("Item name"))
                .defaultValue(""))
                .build()
        );
    private int attackTimer = 0;
    private int delayTimer = 0;
    private int rangeTimer = 0;
    private int healTimer = 0;

    public AutoKitPVP() {
        super(Compassion.SERAPHIM, "AutoKitPVP", ".");
    }

    public static int patchItem(PlayerEntity player, String itemName, boolean exactMatch) {
        PlayerInventory inventory = player.getInventory();

        for (int i = 0; i < 9; i++) {
            ItemStack stack = (ItemStack) inventory.main.get(i);
            if (!stack.isEmpty()) {
                String currentItemName = stack.getName().getString();
                if (exactMatch && currentItemName.equals(itemName) || !exactMatch && currentItemName.contains(itemName)) {
                    return i;
                }
            }
        }

        return -1;
    }

    @EventHandler(
        priority = 200
    )
    private void onTick(Pre event) {
        if (!fullNullCheck()) {
            if (this.attackTimer < this.delayAttack.get()) {
                this.attackTimer++;
            }

            if (this.delayTimer < this.timerDelay.get()) {
                this.delayTimer++;
            }

            if (this.rangeTimer < this.delayInRange.get()) {
                this.rangeTimer++;
            }

            if (this.healTimer < this.delayHeal.get()) {
                this.healTimer++;
            }

            if (this.mc.player.getHealth() + this.mc.player.getAbsorptionAmount() <= (this.health.get()).intValue()
                && this.healTimer >= this.delayHeal.get()) {
                this.release((String) this.healItem.get(), true);
                this.healTimer = 0;
            }

            Entity target = TargetUtils.getPlayerTarget(this.range.get(), SortPriority.LowestDistance);
            if (target instanceof PlayerEntity && this.rangeTimer >= this.timerDelay.get()) {
                this.release((String) this.rangeItem.get(), true);
                this.rangeTimer = 0;
            }

            if (this.delayTimer >= this.timerDelay.get()) {
                this.release((String) this.timerItem.get(), true);
                this.delayTimer = 0;
            }
        }
    }

    @EventHandler
    private void onAttack(AttackEntityEvent event) {
        if (event.entity instanceof PlayerEntity && this.attackTimer >= this.delayAttack.get()) {
            this.release((String) this.attackItem.get(), false);
            this.attackTimer = 0;
        }
    }

    private void release(String name, boolean swapBack) {
        if (patchItem(this.mc.player, name, false) != -1) {
            InvUtils.swap(patchItem(this.mc.player, name, false), swapBack);
            this.sendSequencedPacket(id -> new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, id, this.mc.player.getYaw(), this.mc.player.getPitch()));
            if (swapBack) {
                InvUtils.swapBack();
            }
        }
    }

    public String getInfoString() {
        return "[" + this.attackTimer + " | " + this.rangeTimer + " | " + this.healTimer + "]";
    }
}
