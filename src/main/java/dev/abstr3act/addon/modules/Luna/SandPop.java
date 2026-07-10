package dev.abstr3act.addon.modules.Luna;

import dev.abstr3act.addon.Compassion;
import dev.abstr3act.addon.module.LunaModule;
import dev.abstr3act.addon.utils.TargetUtils;
import meteordevelopment.meteorclient.events.world.TickEvent.Pre;
import meteordevelopment.meteorclient.settings.DoubleSetting.Builder;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.utils.entity.SortPriority;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;

public class SandPop extends LunaModule {
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
    private final Setting<Double> range = this.sgGeneral
        .add(
            ((Builder) ((Builder) new Builder().name("target-range")).description("The maximum distance to target players."))
                .defaultValue(4.0)
                .range(0.0, 5.0)
                .sliderMax(5.0)
                .build()
        );
    private final Setting<SortPriority> priority = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) new meteordevelopment.meteorclient.settings.EnumSetting.Builder()
                .name("target-priority"))
                .description("How to filter targets within range."))
                .defaultValue(SortPriority.LowestDistance))
                .build()
        );
    private final Setting<Boolean> PlaceSand = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("Putting sand"))
                .description("Puts sands on head."))
                .defaultValue(false))
                .build()
        );
    private final Setting<Boolean> selfToggle = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("Auto toggle"))
                .description("Auto toggle."))
                .defaultValue(false))
                .build()
        );
    private final Setting<Boolean> rotate = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("rotate"))
                .description("Rotates towards the sands when placing."))
                .defaultValue(true))
                .build()
        );
    private PlayerEntity target = null;

    public SandPop() {
        super(Compassion.LUNA, "SandPop", "Automatically places sands to your opponent (rofl module).");
    }

    @EventHandler
    private void onTick(Pre event) {
        if (TargetUtils.isBadTarget(this.target, this.range.get())) {
            this.target = TargetUtils.getPlayerTarget(this.range.get(), (SortPriority) this.priority.get());
            if (TargetUtils.isBadTarget(this.target, this.range.get())) {
                return;
            }
        }

        if (this.PlaceSand.get()) {
            BlockUtils.place(this.target.getBlockPos().add(0, 2, 0), InvUtils.findInHotbar(new Item[]{Items.SAND}), this.rotate.get(), 0, false);
        }

        if (this.selfToggle.get()) {
            this.toggle();
        }
    }
}
