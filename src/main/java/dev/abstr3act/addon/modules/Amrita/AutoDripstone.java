package dev.abstr3act.addon.modules.Amrita;

import dev.abstr3act.addon.Compassion;
import dev.abstr3act.addon.module.AmritaModule;
import meteordevelopment.meteorclient.events.world.TickEvent.Pre;
import meteordevelopment.meteorclient.settings.DoubleSetting.Builder;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.TrapdoorBlock;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.hit.BlockHitResult;

public class AutoDripstone extends AmritaModule {
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
    private final Setting<Double> range = this.sgGeneral
        .add(
            ((Builder) ((Builder) new Builder().name("target-range")).description("The radius in which players get targeted."))
                .defaultValue(4.0)
                .min(0.0)
                .sliderMax(5.0)
                .build()
        );

    public AutoDripstone() {
        super(Compassion.AMRITA, "AutoDripstone", "Automatically places anvils above players to destroy helmets.");
    }

    public void onActivate() {
    }

    @EventHandler
    private void onTick(Pre event) {
        if (this.mc.player.raycast(this.range.get(), 0.05F, false) instanceof BlockHitResult bhr
            && this.mc.world.getBlockState(bhr.getBlockPos()).getBlock() instanceof TrapdoorBlock) {
            FindItemResult dripStone = InvUtils.findInHotbar(new Item[]{Items.POINTED_DRIPSTONE});
            BlockUtils.place(bhr.getBlockPos().down(), dripStone, false, 0);
        }
    }
}
