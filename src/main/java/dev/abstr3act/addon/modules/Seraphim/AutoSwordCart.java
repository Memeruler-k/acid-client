package dev.abstr3act.addon.modules.Seraphim;

import dev.abstr3act.addon.Compassion;
import dev.abstr3act.addon.module.SeraphimModule;
import meteordevelopment.meteorclient.events.entity.player.AttackEntityEvent;
import meteordevelopment.meteorclient.settings.IntSetting.Builder;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.vehicle.TntMinecartEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;

public class AutoSwordCart extends SeraphimModule {
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
    public final Setting<Integer> knockBackAttackCount = this.sgGeneral
        .add(((Builder) ((Builder) ((Builder) new Builder().name("KnockBackAttackCount")).description(".")).defaultValue(3)).sliderMax(10).min(0).build());
    public final Setting<Integer> igniteAttackCount = this.sgGeneral
        .add(((Builder) ((Builder) ((Builder) new Builder().name("IgniteAttackCount")).description(".")).defaultValue(3)).sliderMax(10).min(0).build());
    int t = 0;

    public AutoSwordCart() {
        super(Compassion.SERAPHIM, "AutoSwordCart", ".");
    }

    @EventHandler
    public void onAttackEntity(AttackEntityEvent event) {
        if (event.entity instanceof TntMinecartEntity) {
            if (this.t >= this.knockBackAttackCount.get()) {
                InvUtils.swap(InvUtils.findEmpty().slot(), false);
            }

            if (this.t >= this.knockBackAttackCount.get() + this.igniteAttackCount.get()) {
                InvUtils.swap(this.getKnockbackSwordSlot(), false);
            }

            this.t++;
        }
    }

    public int getKnockbackSwordSlot() {
        for (int i = 0; i < this.mc.player.getInventory().size(); i++) {
            ItemStack stack = this.mc.player.getInventory().getStack(i);
            if (!stack.isEmpty() && stack.getItem() instanceof SwordItem) {
                int knockbackLevel = 2;
                if (knockbackLevel > 0) {
                    return i;
                }
            }
        }

        return -1;
    }
}
