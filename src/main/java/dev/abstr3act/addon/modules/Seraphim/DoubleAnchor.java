package dev.abstr3act.addon.modules.Seraphim;

import dev.abstr3act.addon.Compassion;
import dev.abstr3act.addon.events.EventInteractBlock;
import dev.abstr3act.addon.module.SeraphimModule;
import meteordevelopment.meteorclient.events.world.TickEvent.Pre;
import meteordevelopment.meteorclient.settings.IntSetting.Builder;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.RespawnAnchorBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Hand;

public class DoubleAnchor extends SeraphimModule {
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
    private final Setting<Integer> delay = this.sgGeneral
        .add(((Builder) ((Builder) ((Builder) new Builder().name("Delay (ms)")).description(".")).defaultValue(100)).range(1, 2000).sliderRange(1, 2000).build());
    boolean interacted = false;
    int stage = 0;

    public DoubleAnchor() {
        super(Compassion.SERAPHIM, "DoubleAnchor", ".");
    }

    public static int getAnchorSlot(PlayerEntity player) {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = player.getInventory().getStack(i);
            if (stack.getItem().equals(Items.RESPAWN_ANCHOR)) {
                return i;
            }
        }

        return -1;
    }

    public static int getGlowStoneSlot(PlayerEntity player) {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = player.getInventory().getStack(i);
            if (stack.getItem().equals(Items.GLOWSTONE)) {
                return i;
            }
        }

        return -1;
    }

    @EventHandler
    public void onInteractBlock(EventInteractBlock event) {
        if (this.mc.world.getBlockState(event.getHitResult().getBlockPos()).getBlock() instanceof RespawnAnchorBlock block
            && this.mc.world.getBlockState(event.getHitResult().getBlockPos()).get(Properties.CHARGES) > 0) {
            if (this.stage == 0) {
                wait(() -> {
                    InvUtils.swap(getAnchorSlot(this.mc.player), false);
                    this.mc.interactionManager.interactBlock(this.mc.player, Hand.MAIN_HAND, event.getHitResult());
                }, (this.delay.get()).longValue());
            }

            this.stage++;
        }
    }

    @EventHandler
    public void onTickEvent(Pre event) {
        if (this.stage >= 2) {
            this.stage = 0;
        }
    }
}
