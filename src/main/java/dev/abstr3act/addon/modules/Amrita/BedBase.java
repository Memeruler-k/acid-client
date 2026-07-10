package dev.abstr3act.addon.modules.Amrita;

import dev.abstr3act.addon.Compassion;
import dev.abstr3act.addon.modules.Amrita.autoweb.InteractionUtility;
import dev.abstr3act.addon.utils.math.InventoryUtility;
import meteordevelopment.meteorclient.events.world.TickEvent.Post;
import meteordevelopment.meteorclient.settings.DoubleSetting.Builder;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.entity.SortPriority;
import meteordevelopment.meteorclient.utils.entity.TargetUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class BedBase extends Module {
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
    private final Setting<InteractionUtility.Interact> interact = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) new meteordevelopment.meteorclient.settings.EnumSetting.Builder()
                .name("Interact"))
                .description("Interaction type."))
                .defaultValue(InteractionUtility.Interact.Strict))
                .build()
        );
    private final Setting<InteractionUtility.Rotate> rotate = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) new meteordevelopment.meteorclient.settings.EnumSetting.Builder()
                .name("Rotate"))
                .description("Rotation mode for placement."))
                .defaultValue(InteractionUtility.Rotate.None))
                .build()
        );
    private final Setting<InteractionUtility.PlaceMode> placeMode = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) new meteordevelopment.meteorclient.settings.EnumSetting.Builder()
                .name("Place Mode"))
                .description("Mode for placing blocks."))
                .defaultValue(InteractionUtility.PlaceMode.Normal))
                .build()
        );
    int i;
    private PlayerEntity target = null;

    public BedBase() {
        super(Compassion.AMRITA, "BedBase", "Automatically places webs on other players.");
    }

    @EventHandler
    private void onTick(Post event) {
        this.target = TargetUtils.getPlayerTarget(this.range.get(), (SortPriority) this.priority.get());
        if (this.target != null) {
            if (InventoryUtility.findBed().found() && InventoryUtility.findBlock().found()) {
                if (InteractionUtility.canPlaceBlock(this.getDirection(this.target.getBlockPos()), (InteractionUtility.Interact) this.interact.get(), true)
                    && this.i <= 0) {
                    InventoryUtility.switchTo(InventoryUtility.findBlock().slot());
                    InteractionUtility.placeBlock(
                        this.getDirection(this.target.getBlockPos()),
                        (InteractionUtility.Rotate) this.rotate.get(),
                        (InteractionUtility.Interact) this.interact.get(),
                        (InteractionUtility.PlaceMode) this.placeMode.get(),
                        true
                    );
                    this.i++;
                }

                if (InteractionUtility.canPlaceBlock(this.getDirection(this.target.getBlockPos()).add(0, 1, 0), (InteractionUtility.Interact) this.interact.get(), true)
                    && this.i >= 2) {
                    InventoryUtility.switchTo(InventoryUtility.findBed().slot());
                    InteractionUtility.placeBlock(
                        this.getDirection(this.target.getBlockPos()).add(0, 1, 0),
                        (InteractionUtility.Rotate) this.rotate.get(),
                        (InteractionUtility.Interact) this.interact.get(),
                        (InteractionUtility.PlaceMode) this.placeMode.get(),
                        true
                    );
                    InventoryUtility.switchTo(this.mc.player.getInventory().selectedSlot);
                    this.i = 0;
                }
            }
        }
    }

    private BlockPos getDirection(BlockPos pos) {
        if (this.mc.player.getFacing() == Direction.EAST) {
            return pos.west();
        } else if (this.mc.player.getFacing() == Direction.WEST) {
            return pos.east();
        } else if (this.mc.player.getFacing() == Direction.NORTH) {
            return pos.south();
        } else {
            return this.mc.player.getFacing() == Direction.SOUTH ? pos.north() : pos;
        }
    }

    public void onActivate() {
        this.i = 0;
    }
}
