package dev.abstr3act.addon.modules.Seraphim;

import dev.abstr3act.addon.Compassion;
import dev.abstr3act.addon.events.EventInteractBlock;
import dev.abstr3act.addon.module.SeraphimModule;
import dev.abstr3act.addon.modules.Seraphim.InvTotem.InvTotem;
import meteordevelopment.meteorclient.settings.BoolSetting.Builder;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.state.property.Properties;

public class AnchorAssist extends SeraphimModule {
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
    private final Setting<Boolean> switchSlot = this.sgGeneral
        .add(((Builder) ((Builder) ((Builder) new Builder().name("switchSlot")).description(".")).defaultValue(false)).build());
    private final Setting<Integer> slot = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("SwitchSlot"))
                .description("."))
                .defaultValue(2))
                .range(1, 9)
                .min(1)
                .max(9)
                .sliderRange(1, 9)
                .visible(this.switchSlot::get))
                .build()
        );
    private final Setting<Boolean> swapBack = this.sgGeneral
        .add(((Builder) ((Builder) ((Builder) ((Builder) new Builder().name("swapBack")).description(".")).defaultValue(false)).visible(this.switchSlot::get)).build());
    private final Setting<Integer> swapBackDelay = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("swapBackDelay (ms)"))
                .description("."))
                .defaultValue(100))
                .range(1, 2000)
                .sliderRange(1, 2000)
                .visible(this.swapBack::get))
                .build()
        );
    private final Setting<Integer> swapBackSlot = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("swapBackSlot"))
                .description("."))
                .defaultValue(2))
                .range(1, 9)
                .min(1)
                .max(9)
                .sliderRange(1, 9)
                .visible(this.swapBack::get))
                .build()
        );
    boolean swapped = false;
    int t = 0;

    public AnchorAssist() {
        super(Compassion.SERAPHIM, "AnchorAssist", ".");
    }

    public static int getTotemSlot(PlayerEntity player) {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = player.getInventory().getStack(i);
            if (stack.getItem().equals(Items.TOTEM_OF_UNDYING)) {
                return i;
            }
        }

        return -1;
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
        InvTotem invTotem = (InvTotem) Modules.get().get(InvTotem.class);
        if (invTotem.isActive()) {
            invTotem.targetBlock = this.mc.world.getBlockState(event.getHitResult().getBlockPos()).getBlock();
            invTotem.targetBlockPos = event.getHitResult().getBlockPos();
        }

        Block block = this.mc.world.getBlockState(event.getHitResult().getBlockPos()).getBlock();
        if (block.equals(Blocks.RESPAWN_ANCHOR)) {
            if (this.mc.world.getBlockState(event.getHitResult().getBlockPos()).get(Properties.CHARGES) <= 0) {
                InvUtils.swap(getGlowStoneSlot(this.mc.player), false);
            } else if (this.switchSlot.get()) {
                InvUtils.swap(this.slot.get() - 1, true);
                wait(() -> InvUtils.swap(this.swapBackSlot.get() - 1, false), (this.swapBackDelay.get()).longValue());
            } else {
                InvUtils.swap(getAnchorSlot(this.mc.player), false);
            }
        } else if (block.equals(Blocks.GLOWSTONE) && this.mc.player.getMainHandStack().getItem().equals(Items.GLOWSTONE)) {
            event.cancel();
        }
    }
}
