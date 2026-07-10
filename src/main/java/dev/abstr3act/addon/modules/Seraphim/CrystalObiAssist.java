package dev.abstr3act.addon.modules.Seraphim;

import dev.abstr3act.addon.Compassion;
import dev.abstr3act.addon.events.EventAttackBlock;
import dev.abstr3act.addon.events.EventInteractBlock;
import dev.abstr3act.addon.module.SeraphimModule;
import dev.abstr3act.addon.utils.seraphim.BlockUtils;
import meteordevelopment.meteorclient.settings.EnumSetting.Builder;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Blocks;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.item.SwordItem;
import net.minecraft.util.math.BlockPos;

import java.util.Objects;

public class CrystalObiAssist extends SeraphimModule {
    private final SettingGroup sgObsidian = this.settings.createGroup("Obsidian");
    private final SettingGroup sgCrystal = this.settings.createGroup("Crystal");
    private final SettingGroup sgAttack = this.settings.createGroup("Attack");
    private final Setting<Mode> mode_obi = this.sgObsidian
        .add(((Builder) ((Builder) ((Builder) new Builder().name("SwitchMode (Obsidian)")).description(".")).defaultValue(Mode.Post)).build());
    private final Setting<Integer> swapDelay_obi = this.sgObsidian
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("swapDelayObsidian (ms)"))
                .description("."))
                .defaultValue(100))
                .range(1, 2000)
                .sliderRange(1, 2000)
                .build()
        );
    private final Setting<Mode> mode_cys = this.sgCrystal
        .add(((Builder) ((Builder) ((Builder) new Builder().name("SwitchMode (Crystal)")).description(".")).defaultValue(Mode.Post)).build());
    private final Setting<Integer> swapDelay_cys = this.sgCrystal
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("swapDelayCrystal (ms)"))
                .description("."))
                .defaultValue(100))
                .range(1, 2000)
                .sliderRange(1, 2000)
                .build()
        );
    private final Setting<Mode> mode_click = this.sgAttack
        .add(((Builder) ((Builder) ((Builder) new Builder().name("SwitchMode (Attack)")).description(".")).defaultValue(Mode.Post)).build());
    private final Setting<Integer> swapDelay_attack = this.sgAttack
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("swapDelayAttack (ms)"))
                .description("."))
                .defaultValue(100))
                .range(1, 2000)
                .sliderRange(1, 2000)
                .build()
        );

    public CrystalObiAssist() {
        super(Compassion.SERAPHIM, "CrystalAssist", ".");
    }

    @EventHandler
    public void onInteractBlock(EventAttackBlock event) {
        if (InvUtils.find(new Item[]{Items.END_CRYSTAL}).found() && InvUtils.find(new Item[]{Items.OBSIDIAN}).found()) {
            if (this.switchable(this.mc.player.getMainHandStack().getItem()) && this.isAble2Crystal(event.getBlockPos())) {
                if (Objects.requireNonNull((Mode) this.mode_cys.get()) == Mode.Pre) {
                    wait(event::cancel, (this.swapDelay_cys.get()).longValue());
                }

                InvUtils.swap(InvUtils.find(new Item[]{Items.END_CRYSTAL}).slot(), false);
                BlockUtils.interact(event.getBlockPos(), event.getEnumFacing());
                if (Objects.requireNonNull((Mode) this.mode_cys.get()) == Mode.Post) {
                    wait(event::cancel, (this.swapDelay_cys.get()).longValue());
                }
            }
        }
    }

    @EventHandler
    public void onCrystal(EventAttackBlock event) {
        if (InvUtils.find(new Item[]{Items.END_CRYSTAL}).found() && InvUtils.find(new Item[]{Items.OBSIDIAN}).found()) {
            if (this.switchable(this.mc.player.getMainHandStack().getItem()) && !this.isAble2Crystal(event.getBlockPos())) {
                if (Objects.requireNonNull((Mode) this.mode_obi.get()) == Mode.Pre) {
                    wait(event::cancel, (this.swapDelay_obi.get()).longValue());
                }

                InvUtils.swap(InvUtils.find(new Item[]{Items.OBSIDIAN}).slot(), false);
                BlockUtils.interact(event.getBlockPos(), event.getEnumFacing());
                if (Objects.requireNonNull((Mode) this.mode_obi.get()) == Mode.Pre) {
                    wait(event::cancel, (this.swapDelay_obi.get()).longValue());
                }
            }
        }
    }

    @EventHandler
    public void onObsidian(EventInteractBlock event) {
        if (InvUtils.find(new Item[]{Items.END_CRYSTAL}).found() && InvUtils.find(new Item[]{Items.OBSIDIAN}).found()) {
            if (this.isAble2Crystal(event.getHitResult().getBlockPos())
                && this.mc.options.attackKey.isPressed()
                && this.mc.player.getMainHandStack().getItem().equals(Items.OBSIDIAN)) {
                wait(event::cancel, (this.swapDelay_attack.get()).longValue());
            }
        }
    }

    public boolean isAble2Crystal(BlockPos pos) {
        ClientWorld world = this.mc.world;
        return world.getBlockState(pos).getBlock().equals(Blocks.OBSIDIAN) || world.getBlockState(pos).getBlock().equals(Blocks.BEDROCK);
    }

    public boolean switchable(Item item) {
        return item instanceof SwordItem
            || item.equals(Items.TOTEM_OF_UNDYING)
            || item.equals(Items.END_CRYSTAL)
            || item.equals(Items.OBSIDIAN)
            || item.equals(Items.AIR);
    }

    static enum Mode {
        Pre,
        Post;
    }
}
