package dev.abstr3act.addon.modules.Amrita.autoweb;

import dev.abstr3act.addon.Compassion;
import dev.abstr3act.addon.module.AmritaModule;
import dev.abstr3act.addon.utils.Render3DEngine;
import dev.abstr3act.addon.utils.TargetUtils;
import dev.abstr3act.addon.utils.math.InventoryUtility;
import dev.abstr3act.addon.utils.render.Render2DEngine;
import dev.abstr3act.addon.utils.timers.Timer;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent.Pre;
import meteordevelopment.meteorclient.settings.IntSetting.Builder;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.utils.entity.SortPriority;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.RaycastContext.FluidHandling;
import net.minecraft.world.RaycastContext.ShapeType;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class AutoWeb extends AmritaModule {
    public static Timer inactivityTimer = new Timer();
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
    private final SettingGroup sgSelection = this.settings.createGroup("Selection");
    private final SettingGroup sgRender = this.settings.createGroup("Render");
    private final Setting<Integer> range = this.sgGeneral
        .add(((Builder) ((Builder) ((Builder) new Builder().name("range")).description("The placement range.")).defaultValue(5)).min(1).max(7).build());
    private final Setting<SortPriority> sort = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) new meteordevelopment.meteorclient.settings.EnumSetting.Builder()
                .name("Sort"))
                .description("."))
                .defaultValue(SortPriority.ClosestAngle))
                .build()
        );
    private final Setting<Integer> placeWallRange = this.sgGeneral
        .add(((Builder) ((Builder) ((Builder) new Builder().name("wall-range")).description("The range for wall placements.")).defaultValue(5)).min(1).max(7).build());
    private final Setting<PlaceTiming> placeTiming = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) new meteordevelopment.meteorclient.settings.EnumSetting.Builder()
                .name("place-timing"))
                .description("The timing for placing blocks."))
                .defaultValue(PlaceTiming.Default))
                .build()
        );
    private final Setting<Integer> blocksPerTick = this.sgGeneral
        .add(
            ((Builder) ((Builder) ((Builder) ((Builder) new Builder().name("blocks-per-tick")).description("Number of blocks placed per tick.")).defaultValue(8))
                .min(1)
                .max(12)
                .visible(() -> this.placeTiming.get() == PlaceTiming.Default))
                .build()
        );
    private final Setting<Integer> placeDelay = this.sgGeneral
        .add(((Builder) ((Builder) ((Builder) new Builder().name("place-delay")).description("Delay between placing blocks.")).defaultValue(3)).min(0).max(10).build());
    private final Setting<InteractionUtility.Interact> interact = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) new meteordevelopment.meteorclient.settings.EnumSetting.Builder()
                .name("interact"))
                .description("How interactions should be handled."))
                .defaultValue(InteractionUtility.Interact.Strict))
                .build()
        );
    private final Setting<InteractionUtility.PlaceMode> placeMode = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) new meteordevelopment.meteorclient.settings.EnumSetting.Builder()
                .name("place-mode"))
                .description("The mode used for placing blocks."))
                .defaultValue(InteractionUtility.PlaceMode.Normal))
                .build()
        );
    private final Setting<InteractionUtility.Rotate> rotate = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) new meteordevelopment.meteorclient.settings.EnumSetting.Builder()
                .name("rotate"))
                .description("Rotation settings."))
                .defaultValue(InteractionUtility.Rotate.None))
                .build()
        );
    private final Setting<Boolean> head = this.sgSelection
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("head"))
                .description("Enable head selection."))
                .defaultValue(true))
                .build()
        );
    private final Setting<Boolean> leggs = this.sgSelection
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("leggs"))
                .description("Enable leg selection."))
                .defaultValue(true))
                .build()
        );
    private final Setting<Boolean> surround = this.sgSelection
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("surround"))
                .description("Enable surrounding placement."))
                .defaultValue(true))
                .build()
        );
    private final Setting<Boolean> upperSurround = this.sgSelection
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("upper-surround"))
                .description("Enable upper surrounding placement."))
                .defaultValue(false))
                .build()
        );
    private final Setting<RenderMode> renderMode = this.sgRender
        .add(
            ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) new meteordevelopment.meteorclient.settings.EnumSetting.Builder()
                .name("render-mode"))
                .description("How the render will be displayed."))
                .defaultValue(RenderMode.Fade))
                .build()
        );
    private final Setting<SettingColor> renderFillColor = this.sgRender
        .add(
            ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                .name("render-fill-color"))
                .description("The fill color for rendering."))
                .defaultValue(new SettingColor(255, 255, 255, 100))
                .build()
        );
    private final Setting<SettingColor> renderLineColor = this.sgRender
        .add(
            ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                .name("render-line-color"))
                .description("The line color for rendering."))
                .defaultValue(new SettingColor(255, 255, 255, 100))
                .build()
        );
    private final Setting<Integer> renderLineWidth = this.sgRender
        .add(
            ((Builder) ((Builder) ((Builder) new Builder().name("render-line-width")).description("The width of the render lines.")).defaultValue(2))
                .min(1)
                .max(5)
                .build()
        );
    private final Setting<Integer> effectDurationMs = this.sgRender
        .add(
            ((Builder) ((Builder) ((Builder) new Builder().name("effect-duration-ms")).description("The duration of the render effect in milliseconds."))
                .defaultValue(500))
                .min(0)
                .max(10000)
                .build()
        );
    private final ArrayList<BlockPos> sequentialBlocks = new ArrayList<>();
    private final Map<BlockPos, Long> renderPoses = new ConcurrentHashMap<>();
    private int delay = 0;

    public AutoWeb() {
        super(Compassion.AMRITA, "AutoWebV2", ".");
    }

    @EventHandler
    public void onRender3D(Render3DEvent stack) {
        this.renderPoses
            .forEach(
                (pos, time) -> {
                    if (System.currentTimeMillis() - time > (this.effectDurationMs.get()).intValue()) {
                        this.renderPoses.remove(pos);
                    } else {
                        switch ((RenderMode) this.renderMode.get()) {
                            case Fade:
                                Render3DEngine.drawFilledBox(
                                    stack.matrices,
                                    new Box(pos),
                                    Render2DEngine.injectAlpha(
                                        new Color(((SettingColor) this.renderFillColor.get()).getPacked(), true),
                                        (int) (100.0F * (1.0F - (float) (System.currentTimeMillis() - time) / 500.0F))
                                    )
                                );
                                Render3DEngine.drawBoxOutline(
                                    new Box(pos),
                                    Render2DEngine.injectAlpha(
                                        new Color(((SettingColor) this.renderLineColor.get()).getPacked(), true),
                                        (int) (100.0F * (1.0F - (float) (System.currentTimeMillis() - time) / 500.0F))
                                    ),
                                    (this.renderLineWidth.get()).intValue()
                                );
                                break;
                            case Decrease:
                                float scale = 1.0F - (float) (System.currentTimeMillis() - time) / 500.0F;
                                Box box = new Box(pos.getX(), pos.getY(), pos.getZ(), pos.getX(), pos.getY(), pos.getZ());
                                Render3DEngine.drawFilledBox(
                                    stack.matrices,
                                    box.shrink(scale, scale, scale).offset(0.5 + scale * 0.5, 0.5 + scale * 0.5, 0.5 + scale * 0.5),
                                    Render2DEngine.injectAlpha(
                                        new Color(((SettingColor) this.renderFillColor.get()).getPacked(), true),
                                        (int) (100.0F * (1.0F - (float) (System.currentTimeMillis() - time) / 500.0F))
                                    )
                                );
                                Render3DEngine.drawBoxOutline(
                                    box.shrink(scale, scale, scale).offset(0.5 + scale * 0.5, 0.5 + scale * 0.5, 0.5 + scale * 0.5),
                                    new Color(((SettingColor) this.renderLineColor.get()).getPacked(), true),
                                    (this.renderLineWidth.get()).intValue()
                                );
                        }
                    }
                }
            );
    }

    public void onActivate() {
        this.sequentialBlocks.clear();
        this.renderPoses.clear();
    }

    @EventHandler
    public void onTick(Pre e) {
        BlockPos targetBlock1 = this.getSequentialPos();
        if (targetBlock1 != null) {
            if (this.delay > 0) {
                this.delay--;
            } else {
                InventoryUtility.saveSlot();
                if (this.placeTiming.get() == PlaceTiming.Default) {
                    int placed = 0;

                    while (placed < this.blocksPerTick.get()) {
                        BlockPos targetBlock = this.getSequentialPos();
                        if (targetBlock == null
                            || !InteractionUtility.placeBlock(
                            targetBlock,
                            (InteractionUtility.Rotate) this.rotate.get(),
                            (InteractionUtility.Interact) this.interact.get(),
                            (InteractionUtility.PlaceMode) this.placeMode.get(),
                            this.getSlot(),
                            false,
                            true
                        )) {
                            break;
                        }

                        placed++;
                        this.renderPoses.put(targetBlock, System.currentTimeMillis());
                        this.delay = this.placeDelay.get();
                        inactivityTimer.reset();
                    }
                } else if (this.placeTiming.get() == PlaceTiming.Vanilla) {
                    BlockPos targetBlock = this.getSequentialPos();
                    if (targetBlock == null) {
                        return;
                    }

                    if (InteractionUtility.placeBlock(
                        targetBlock,
                        (InteractionUtility.Rotate) this.rotate.get(),
                        (InteractionUtility.Interact) this.interact.get(),
                        (InteractionUtility.PlaceMode) this.placeMode.get(),
                        this.getSlot(),
                        false,
                        true
                    )) {
                        this.sequentialBlocks.add(targetBlock);
                        this.renderPoses.put(targetBlock, System.currentTimeMillis());
                        this.delay = this.placeDelay.get();
                        inactivityTimer.reset();
                    }
                }

                InventoryUtility.returnSlot();
            }
        }
    }

    private BlockPos getSequentialPos() {
        PlayerEntity target = TargetUtils.getPlayerTarget((this.range.get()).intValue(), (SortPriority) this.sort.get());
        if (target != null) {
            BlockPos targetBp = BlockPos.ofFloored(target.getPos());
            ArrayList<BlockPos> positions = new ArrayList<>();
            if (this.leggs.get()) {
                positions.add(targetBp);
            }

            if (this.head.get()) {
                positions.add(targetBp.up());
            }

            if (this.surround.get()) {
                positions.add(targetBp.east());
                positions.add(targetBp.west());
                positions.add(targetBp.south());
                positions.add(targetBp.north());
            }

            if (this.upperSurround.get()) {
                positions.add(targetBp.east().up());
                positions.add(targetBp.west().up());
                positions.add(targetBp.south().up());
                positions.add(targetBp.north().up());
            }

            for (BlockPos bp : positions) {
                BlockHitResult wallCheck = this.mc
                    .world
                    .raycast(
                        new RaycastContext(
                            InteractionUtility.getEyesPos(this.mc.player), bp.toCenterPos().offset(Direction.UP, 0.5), ShapeType.COLLIDER, FluidHandling.NONE, this.mc.player
                        )
                    );
                if ((
                    wallCheck == null
                        || wallCheck.getType() != Type.BLOCK
                        || wallCheck.getBlockPos() == bp
                        || !(InteractionUtility.squaredDistanceFromEyes(bp.toCenterPos()) > Math.pow((this.placeWallRange.get()).intValue(), 2.0))
                )
                    && InteractionUtility.canPlaceBlock(bp, (InteractionUtility.Interact) this.interact.get(), true)
                    && this.mc.world.getBlockState(bp).isReplaceable()) {
                    return bp;
                }
            }
        }

        return null;
    }

    private int getSlot() {
        List<Block> canUseBlocks = new ArrayList<>();
        canUseBlocks.add(Blocks.COBWEB);
        int slot = -1;
        ItemStack mainhandStack = this.mc.player.getMainHandStack();
        if (mainhandStack != ItemStack.EMPTY && mainhandStack.getItem() instanceof BlockItem) {
            Block blockFromMainhandItem = ((BlockItem) mainhandStack.getItem()).getBlock();
            if (canUseBlocks.contains(blockFromMainhandItem)) {
                slot = this.mc.player.getInventory().selectedSlot;
            }
        }

        if (slot == -1) {
            for (int i = 0; i < 9; i++) {
                ItemStack stack = this.mc.player.getInventory().getStack(i);
                if (stack != ItemStack.EMPTY && stack.getItem() instanceof BlockItem) {
                    Block blockFromItem = ((BlockItem) stack.getItem()).getBlock();
                    if (canUseBlocks.contains(blockFromItem)) {
                        slot = i;
                        break;
                    }
                }
            }
        }

        return slot;
    }

    private static enum PlaceTiming {
        Default,
        Vanilla;
    }

    private static enum RenderMode {
        Fade,
        Decrease;
    }
}
