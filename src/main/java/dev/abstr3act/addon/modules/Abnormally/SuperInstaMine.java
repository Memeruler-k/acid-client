package dev.abstr3act.addon.modules.Abnormally;

import dev.abstr3act.addon.Compassion;
import dev.abstr3act.addon.module.AbnormallyModule;
import meteordevelopment.meteorclient.events.entity.player.StartBreakingBlockEvent;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent.Pre;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.EnumSetting.Builder;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.utils.player.Rotations;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Block;
import net.minecraft.item.Items;
import net.minecraft.item.ToolItem;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket.Action;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.Mutable;
import net.minecraft.util.math.Direction;

import java.util.List;

public class SuperInstaMine extends AbnormallyModule {
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
    private final SettingGroup sgRender = this.settings.createGroup("Render");
    private final Setting<listModes> listmode = this.sgGeneral
        .add(
            ((Builder) ((Builder) ((Builder) new Builder().name("List Mode")).description("Whether to break or not break the block list."))
                .defaultValue(listModes.blacklist))
                .build()
        );
    private final Setting<List<Block>> skippableBlox = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BlockListSetting.Builder) ((meteordevelopment.meteorclient.settings.BlockListSetting.Builder) ((meteordevelopment.meteorclient.settings.BlockListSetting.Builder) new meteordevelopment.meteorclient.settings.BlockListSetting.Builder()
                .name("Blocks to Skip"))
                .description("Skips instamining this block."))
                .visible(() -> this.listmode.get() == listModes.blacklist))
                .build()
        );
    private final Setting<List<Block>> nonskippableBlox = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BlockListSetting.Builder) ((meteordevelopment.meteorclient.settings.BlockListSetting.Builder) ((meteordevelopment.meteorclient.settings.BlockListSetting.Builder) new meteordevelopment.meteorclient.settings.BlockListSetting.Builder()
                .name("Blocks to Break"))
                .description("Only instamine this block."))
                .visible(() -> this.listmode.get() == listModes.whitelist))
                .build()
        );
    private final Setting<Integer> range = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("Break Modes (Range)"))
                .description("The range around the center block to break more blocks"))
                .defaultValue(0))
                .sliderRange(-1, 7)
                .min(-1)
                .max(7)
                .build()
        );
    private final Setting<Boolean> aorient = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("AutoOrientBreakDirection"))
                .description("For Break Mode 3 and 4. Automatically chooses whether to break upright or horizontal."))
                .defaultValue(true))
                .build()
        );
    private final Setting<Modes> mode = this.sgGeneral
        .add(
            ((Builder) ((Builder) ((Builder) ((Builder) new Builder().name("Break Direction Mode"))
                .description("For Break Mode 3 and 4. Choose whether to break upright or horizontal."))
                .defaultValue(Modes.Vertical))
                .visible(() -> !this.aorient.get()))
                .build()
        );
    private final Setting<Integer> tickDelay = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("delay"))
                .description("The delay between breaks."))
                .defaultValue(0))
                .min(0)
                .sliderMax(20)
                .build()
        );
    private final Setting<Boolean> pick = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("only-pick"))
                .description("Only tries to mine the block if you are holding a pickaxe."))
                .defaultValue(false))
                .build()
        );
    private final Setting<Boolean> swing = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("Swing Hand"))
                .description("Do or Do Not swing hand when instamining."))
                .defaultValue(true))
                .build()
        );
    private final Setting<Boolean> rotate = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("rotate"))
                .description("Faces the blocks being mined server side."))
                .defaultValue(true))
                .build()
        );
    private final Setting<Boolean> render = this.sgRender
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("render"))
                .description("Renders a block overlay on the block being broken."))
                .defaultValue(true))
                .build()
        );
    private final Setting<ShapeMode> shapeMode = this.sgRender
        .add(((Builder) ((Builder) ((Builder) new Builder().name("shape-mode")).description("How the shapes are rendered.")).defaultValue(ShapeMode.Both)).build());
    private final Setting<SettingColor> sideColor = this.sgRender
        .add(
            ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                .name("side-color"))
                .description("The color of the sides of the blocks being rendered."))
                .defaultValue(new SettingColor(204, 0, 0, 10))
                .build()
        );
    private final Setting<SettingColor> lineColor = this.sgRender
        .add(
            ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                .name("line-color"))
                .description("The color of the lines of the blocks being rendered."))
                .defaultValue(new SettingColor(204, 0, 0, 255))
                .build()
        );
    private final Mutable blockPos = new Mutable(0, -1, 0);
    private final Mutable blockPos1 = new Mutable(0, -1, 0);
    private final Mutable blockPos2 = new Mutable(0, -1, 0);
    private final Mutable blockPos3 = new Mutable(0, -1, 0);
    private final Mutable blockPos4 = new Mutable(0, -1, 0);
    private final Mutable blockPos5 = new Mutable(0, -1, 0);
    private final Mutable blockPos6 = new Mutable(0, -1, 0);
    private final Mutable blockPos7 = new Mutable(0, -1, 0);
    private final Mutable blockPos8 = new Mutable(0, -1, 0);
    private final Mutable blockPos9 = new Mutable(0, -1, 0);
    private final Mutable blockPos10 = new Mutable(0, -1, 0);
    private final Mutable blockPos11 = new Mutable(0, -1, 0);
    private final Mutable blockPos12 = new Mutable(0, -1, 0);
    private final Mutable blockPos13 = new Mutable(0, -1, 0);
    private final Mutable blockPos14 = new Mutable(0, -1, 0);
    private final Mutable blockPos15 = new Mutable(0, -1, 0);
    private final Mutable blockPos16 = new Mutable(0, -1, 0);
    private final Mutable blockPos17 = new Mutable(0, -1, 0);
    private final Mutable blockPos18 = new Mutable(0, -1, 0);
    private final Mutable blockPos19 = new Mutable(0, -1, 0);
    private final Mutable blockPos20 = new Mutable(0, -1, 0);
    private final Mutable blockPos21 = new Mutable(0, -1, 0);
    private final Mutable blockPos22 = new Mutable(0, -1, 0);
    private final Mutable blockPos23 = new Mutable(0, -1, 0);
    private final Mutable blockPos24 = new Mutable(0, -1, 0);
    private final Mutable blockPos25 = new Mutable(0, -1, 0);
    private final Mutable blockPos26 = new Mutable(0, -1, 0);
    private int ticks;
    private Direction direction;
    private Direction playermovingdirection;
    private int playerpitch;

    public SuperInstaMine() {
        super(Compassion.ABNORMALLY, "SuperInstaMine", "Attempts to instantly mine blocks. Modified to be able to break many blocks at a time.");
    }

    public void onActivate() {
        this.ticks = 0;
        this.blockPos.set(0, -128, 0);
        this.blockPos1.set(0, -128, 0);
        this.blockPos2.set(0, -128, 0);
        this.blockPos3.set(0, -128, 0);
        this.blockPos4.set(0, -128, 0);
        this.blockPos5.set(0, -128, 0);
        this.blockPos6.set(0, -128, 0);
        this.blockPos7.set(0, -128, 0);
        this.blockPos8.set(0, -128, 0);
        this.blockPos9.set(0, -128, 0);
        this.blockPos10.set(0, -128, 0);
        this.blockPos11.set(0, -128, 0);
        this.blockPos12.set(0, -128, 0);
        this.blockPos12.set(0, -128, 0);
        this.blockPos14.set(0, -128, 0);
        this.blockPos15.set(0, -128, 0);
        this.blockPos16.set(0, -128, 0);
        this.blockPos17.set(0, -128, 0);
        this.blockPos18.set(0, -128, 0);
        this.blockPos19.set(0, -128, 0);
        this.blockPos20.set(0, -128, 0);
        this.blockPos21.set(0, -128, 0);
        this.blockPos22.set(0, -128, 0);
        this.blockPos23.set(0, -128, 0);
        this.blockPos24.set(0, -128, 0);
        this.blockPos25.set(0, -128, 0);
        this.blockPos26.set(0, -128, 0);
    }

    @EventHandler
    private void onStartBreakingBlock(StartBreakingBlockEvent event) {
        this.direction = event.direction;
        this.playermovingdirection = this.mc.player.getMovementDirection();
        this.playerpitch = Math.round(this.mc.player.getPitch());
        this.blockPos.set(event.blockPos);
        this.blockPos1.set(new BlockPos(event.blockPos.getX() + 1, event.blockPos.getY(), event.blockPos.getZ()));
        this.blockPos2.set(new BlockPos(event.blockPos.getX() - 1, event.blockPos.getY(), event.blockPos.getZ()));
        this.blockPos3.set(new BlockPos(event.blockPos.getX(), event.blockPos.getY(), event.blockPos.getZ() + 1));
        this.blockPos4.set(new BlockPos(event.blockPos.getX(), event.blockPos.getY(), event.blockPos.getZ() - 1));
        this.blockPos5.set(new BlockPos(event.blockPos.getX() + 1, event.blockPos.getY(), event.blockPos.getZ() + 1));
        this.blockPos6.set(new BlockPos(event.blockPos.getX() - 1, event.blockPos.getY(), event.blockPos.getZ() - 1));
        this.blockPos7.set(new BlockPos(event.blockPos.getX() + 1, event.blockPos.getY(), event.blockPos.getZ() - 1));
        this.blockPos8.set(new BlockPos(event.blockPos.getX() - 1, event.blockPos.getY(), event.blockPos.getZ() + 1));
        this.blockPos9.set(new BlockPos(event.blockPos.getX(), event.blockPos.getY() + 1, event.blockPos.getZ()));
        this.blockPos10.set(new BlockPos(event.blockPos.getX() + 1, event.blockPos.getY() + 1, event.blockPos.getZ()));
        this.blockPos11.set(new BlockPos(event.blockPos.getX() - 1, event.blockPos.getY() + 1, event.blockPos.getZ()));
        this.blockPos12.set(new BlockPos(event.blockPos.getX(), event.blockPos.getY() + 1, event.blockPos.getZ() + 1));
        this.blockPos13.set(new BlockPos(event.blockPos.getX(), event.blockPos.getY() + 1, event.blockPos.getZ() - 1));
        this.blockPos14.set(new BlockPos(event.blockPos.getX() + 1, event.blockPos.getY() + 1, event.blockPos.getZ() + 1));
        this.blockPos15.set(new BlockPos(event.blockPos.getX() - 1, event.blockPos.getY() + 1, event.blockPos.getZ() - 1));
        this.blockPos16.set(new BlockPos(event.blockPos.getX() + 1, event.blockPos.getY() + 1, event.blockPos.getZ() - 1));
        this.blockPos17.set(new BlockPos(event.blockPos.getX() - 1, event.blockPos.getY() + 1, event.blockPos.getZ() + 1));
        this.blockPos18.set(new BlockPos(event.blockPos.getX(), event.blockPos.getY() - 1, event.blockPos.getZ()));
        this.blockPos19.set(new BlockPos(event.blockPos.getX() + 1, event.blockPos.getY() - 1, event.blockPos.getZ()));
        this.blockPos20.set(new BlockPos(event.blockPos.getX() - 1, event.blockPos.getY() - 1, event.blockPos.getZ()));
        this.blockPos21.set(new BlockPos(event.blockPos.getX(), event.blockPos.getY() - 1, event.blockPos.getZ() + 1));
        this.blockPos22.set(new BlockPos(event.blockPos.getX(), event.blockPos.getY() - 1, event.blockPos.getZ() - 1));
        this.blockPos23.set(new BlockPos(event.blockPos.getX() + 1, event.blockPos.getY() - 1, event.blockPos.getZ() + 1));
        this.blockPos24.set(new BlockPos(event.blockPos.getX() - 1, event.blockPos.getY() - 1, event.blockPos.getZ() - 1));
        this.blockPos25.set(new BlockPos(event.blockPos.getX() + 1, event.blockPos.getY() - 1, event.blockPos.getZ() - 1));
        this.blockPos26.set(new BlockPos(event.blockPos.getX() - 1, event.blockPos.getY() - 1, event.blockPos.getZ() + 1));
    }

    @EventHandler
    private void onTick(Pre event) {
        if (this.ticks < this.tickDelay.get()) {
            this.ticks++;
        } else {
            this.ticks = 0;
            if (this.shouldMine() && this.range.get() == -1) {
                if ((
                    this.listmode.get() == listModes.whitelist
                        && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos).getBlock())
                        || this.listmode.get() == listModes.blacklist
                        && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos).getBlock())
                )
                    && this.rotate.get()
                    && BlockUtils.canBreak(this.blockPos)) {
                    Rotations.rotate(
                        Rotations.getYaw(this.blockPos),
                        Rotations.getPitch(this.blockPos),
                        () -> this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.STOP_DESTROY_BLOCK, this.blockPos, this.direction))
                    );
                } else if ((
                    this.listmode.get() == listModes.whitelist
                        && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos).getBlock())
                        || this.listmode.get() == listModes.blacklist
                        && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos).getBlock())
                )
                    && !this.rotate.get()
                    && BlockUtils.canBreak(this.blockPos)) {
                    this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.STOP_DESTROY_BLOCK, this.blockPos, this.direction));
                }

                switch (this.playermovingdirection) {
                    case NORTH:
                        if ((
                            this.listmode.get() == listModes.whitelist
                                && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos2).getBlock())
                                || this.listmode.get() == listModes.blacklist
                                && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos2).getBlock())
                        )
                            && (
                            this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                                && BlockUtils.canBreak(this.blockPos2)
                                || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos2))
                        )) {
                            this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.START_DESTROY_BLOCK, this.blockPos2, this.direction));
                        }

                        if ((
                            this.listmode.get() == listModes.whitelist
                                && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos2).getBlock())
                                || this.listmode.get() == listModes.blacklist
                                && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos2).getBlock())
                        )
                            && (
                            this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                                && BlockUtils.canBreak(this.blockPos2)
                                || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos2))
                        )) {
                            this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.STOP_DESTROY_BLOCK, this.blockPos2, this.direction));
                        }
                        break;
                    case SOUTH:
                        if ((
                            this.listmode.get() == listModes.whitelist
                                && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos1).getBlock())
                                || this.listmode.get() == listModes.blacklist
                                && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos1).getBlock())
                        )
                            && (
                            this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                                && BlockUtils.canBreak(this.blockPos1)
                                || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos1))
                        )) {
                            this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.START_DESTROY_BLOCK, this.blockPos1, this.direction));
                        }

                        if ((
                            this.listmode.get() == listModes.whitelist
                                && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos1).getBlock())
                                || this.listmode.get() == listModes.blacklist
                                && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos1).getBlock())
                        )
                            && (
                            this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                                && BlockUtils.canBreak(this.blockPos1)
                                || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos1))
                        )) {
                            this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.STOP_DESTROY_BLOCK, this.blockPos1, this.direction));
                        }
                        break;
                    case EAST:
                        if ((
                            this.listmode.get() == listModes.whitelist
                                && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos4).getBlock())
                                || this.listmode.get() == listModes.blacklist
                                && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos4).getBlock())
                        )
                            && (
                            this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                                && BlockUtils.canBreak(this.blockPos4)
                                || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos4))
                        )) {
                            this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.START_DESTROY_BLOCK, this.blockPos4, this.direction));
                        }

                        if ((
                            this.listmode.get() == listModes.whitelist
                                && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos4).getBlock())
                                || this.listmode.get() == listModes.blacklist
                                && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos4).getBlock())
                        )
                            && (
                            this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                                && BlockUtils.canBreak(this.blockPos4)
                                || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos4))
                        )) {
                            this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.STOP_DESTROY_BLOCK, this.blockPos4, this.direction));
                        }
                        break;
                    case WEST:
                        if ((
                            this.listmode.get() == listModes.whitelist
                                && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos3).getBlock())
                                || this.listmode.get() == listModes.blacklist
                                && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos3).getBlock())
                        )
                            && (
                            this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                                && BlockUtils.canBreak(this.blockPos3)
                                || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos3))
                        )) {
                            this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.START_DESTROY_BLOCK, this.blockPos3, this.direction));
                        }

                        if ((
                            this.listmode.get() == listModes.whitelist
                                && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos3).getBlock())
                                || this.listmode.get() == listModes.blacklist
                                && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos3).getBlock())
                        )
                            && (
                            this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                                && BlockUtils.canBreak(this.blockPos3)
                                || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos3))
                        )) {
                            this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.STOP_DESTROY_BLOCK, this.blockPos3, this.direction));
                        }
                }

                if ((
                    this.listmode.get() == listModes.whitelist
                        && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos).getBlock())
                        || this.listmode.get() == listModes.blacklist
                        && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos).getBlock())
                )
                    && this.rotate.get()
                    && BlockUtils.canBreak(this.blockPos)) {
                    Rotations.rotate(
                        Rotations.getYaw(this.blockPos),
                        Rotations.getPitch(this.blockPos),
                        () -> this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.START_DESTROY_BLOCK, this.blockPos, this.direction))
                    );
                } else if ((
                    this.listmode.get() == listModes.whitelist
                        && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos).getBlock())
                        || this.listmode.get() == listModes.blacklist
                        && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos).getBlock())
                )
                    && !this.rotate.get()
                    && BlockUtils.canBreak(this.blockPos)) {
                    this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.START_DESTROY_BLOCK, this.blockPos, this.direction));
                }

                if (this.swing.get()) {
                    this.mc.getNetworkHandler().sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
                }
            }

            if (this.shouldMine() && this.range.get() == 0) {
                if ((
                    this.listmode.get() == listModes.whitelist
                        && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos).getBlock())
                        || this.listmode.get() == listModes.blacklist
                        && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos).getBlock())
                )
                    && this.rotate.get()
                    && BlockUtils.canBreak(this.blockPos)) {
                    Rotations.rotate(
                        Rotations.getYaw(this.blockPos),
                        Rotations.getPitch(this.blockPos),
                        () -> this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.STOP_DESTROY_BLOCK, this.blockPos, this.direction))
                    );
                } else if ((
                    this.listmode.get() == listModes.whitelist
                        && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos).getBlock())
                        || this.listmode.get() == listModes.blacklist
                        && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos).getBlock())
                )
                    && !this.rotate.get()
                    && BlockUtils.canBreak(this.blockPos)) {
                    this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.STOP_DESTROY_BLOCK, this.blockPos, this.direction));
                }

                if (this.swing.get()) {
                    this.mc.getNetworkHandler().sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
                }
            }

            if (this.shouldMine() && this.range.get() == 1) {
                if ((
                    this.listmode.get() == listModes.whitelist
                        && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos).getBlock())
                        || this.listmode.get() == listModes.blacklist
                        && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos).getBlock())
                )
                    && this.rotate.get()
                    && BlockUtils.canBreak(this.blockPos)) {
                    Rotations.rotate(
                        Rotations.getYaw(this.blockPos),
                        Rotations.getPitch(this.blockPos),
                        () -> this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.STOP_DESTROY_BLOCK, this.blockPos, this.direction))
                    );
                } else if ((
                    this.listmode.get() == listModes.whitelist
                        && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos).getBlock())
                        || this.listmode.get() == listModes.blacklist
                        && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos).getBlock())
                )
                    && !this.rotate.get()
                    && BlockUtils.canBreak(this.blockPos)) {
                    this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.STOP_DESTROY_BLOCK, this.blockPos, this.direction));
                }

                switch (this.playermovingdirection) {
                    case NORTH:
                        if ((
                            this.listmode.get() == listModes.whitelist
                                && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos1).getBlock())
                                || this.listmode.get() == listModes.blacklist
                                && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos1).getBlock())
                        )
                            && (
                            this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                                && BlockUtils.canBreak(this.blockPos1)
                                || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos1))
                        )) {
                            this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.START_DESTROY_BLOCK, this.blockPos1, this.direction));
                        }

                        if ((
                            this.listmode.get() == listModes.whitelist
                                && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos1).getBlock())
                                || this.listmode.get() == listModes.blacklist
                                && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos1).getBlock())
                        )
                            && (
                            this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                                && BlockUtils.canBreak(this.blockPos1)
                                || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos1))
                        )) {
                            this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.STOP_DESTROY_BLOCK, this.blockPos1, this.direction));
                        }
                        break;
                    case SOUTH:
                        if ((
                            this.listmode.get() == listModes.whitelist
                                && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos2).getBlock())
                                || this.listmode.get() == listModes.blacklist
                                && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos2).getBlock())
                        )
                            && (
                            this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                                && BlockUtils.canBreak(this.blockPos2)
                                || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos2))
                        )) {
                            this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.START_DESTROY_BLOCK, this.blockPos2, this.direction));
                        }

                        if ((
                            this.listmode.get() == listModes.whitelist
                                && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos2).getBlock())
                                || this.listmode.get() == listModes.blacklist
                                && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos2).getBlock())
                        )
                            && (
                            this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                                && BlockUtils.canBreak(this.blockPos2)
                                || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos2))
                        )) {
                            this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.STOP_DESTROY_BLOCK, this.blockPos2, this.direction));
                        }
                        break;
                    case EAST:
                        if ((
                            this.listmode.get() == listModes.whitelist
                                && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos3).getBlock())
                                || this.listmode.get() == listModes.blacklist
                                && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos3).getBlock())
                        )
                            && (
                            this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                                && BlockUtils.canBreak(this.blockPos3)
                                || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos3))
                        )) {
                            this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.START_DESTROY_BLOCK, this.blockPos3, this.direction));
                        }

                        if ((
                            this.listmode.get() == listModes.whitelist
                                && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos3).getBlock())
                                || this.listmode.get() == listModes.blacklist
                                && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos3).getBlock())
                        )
                            && (
                            this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                                && BlockUtils.canBreak(this.blockPos3)
                                || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos3))
                        )) {
                            this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.STOP_DESTROY_BLOCK, this.blockPos3, this.direction));
                        }
                        break;
                    case WEST:
                        if ((
                            this.listmode.get() == listModes.whitelist
                                && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos4).getBlock())
                                || this.listmode.get() == listModes.blacklist
                                && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos4).getBlock())
                        )
                            && (
                            this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                                && BlockUtils.canBreak(this.blockPos4)
                                || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos4))
                        )) {
                            this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.START_DESTROY_BLOCK, this.blockPos4, this.direction));
                        }

                        if ((
                            this.listmode.get() == listModes.whitelist
                                && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos4).getBlock())
                                || this.listmode.get() == listModes.blacklist
                                && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos4).getBlock())
                        )
                            && (
                            this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                                && BlockUtils.canBreak(this.blockPos4)
                                || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos4))
                        )) {
                            this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.STOP_DESTROY_BLOCK, this.blockPos4, this.direction));
                        }
                }

                if ((
                    this.listmode.get() == listModes.whitelist
                        && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos).getBlock())
                        || this.listmode.get() == listModes.blacklist
                        && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos).getBlock())
                )
                    && this.rotate.get()
                    && BlockUtils.canBreak(this.blockPos)) {
                    Rotations.rotate(
                        Rotations.getYaw(this.blockPos),
                        Rotations.getPitch(this.blockPos),
                        () -> this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.START_DESTROY_BLOCK, this.blockPos, this.direction))
                    );
                } else if ((
                    this.listmode.get() == listModes.whitelist
                        && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos).getBlock())
                        || this.listmode.get() == listModes.blacklist
                        && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos).getBlock())
                )
                    && !this.rotate.get()
                    && BlockUtils.canBreak(this.blockPos)) {
                    this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.START_DESTROY_BLOCK, this.blockPos, this.direction));
                }

                if (this.swing.get()) {
                    this.mc.getNetworkHandler().sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
                }
            }

            if (this.shouldMine() && this.range.get() == 2) {
                if ((
                    this.listmode.get() == listModes.whitelist
                        && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos).getBlock())
                        || this.listmode.get() == listModes.blacklist
                        && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos).getBlock())
                )
                    && this.rotate.get()
                    && BlockUtils.canBreak(this.blockPos)) {
                    Rotations.rotate(
                        Rotations.getYaw(this.blockPos),
                        Rotations.getPitch(this.blockPos),
                        () -> this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.STOP_DESTROY_BLOCK, this.blockPos, this.direction))
                    );
                } else if ((
                    this.listmode.get() == listModes.whitelist
                        && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos).getBlock())
                        || this.listmode.get() == listModes.blacklist
                        && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos).getBlock())
                )
                    && !this.rotate.get()
                    && BlockUtils.canBreak(this.blockPos)) {
                    this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.STOP_DESTROY_BLOCK, this.blockPos, this.direction));
                }

                if (this.playermovingdirection == Direction.NORTH || this.playermovingdirection == Direction.SOUTH) {
                    if ((
                        this.listmode.get() == listModes.whitelist
                            && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos2).getBlock())
                            || this.listmode.get() == listModes.blacklist
                            && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos2).getBlock())
                    )
                        && (
                        this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                            && BlockUtils.canBreak(this.blockPos2)
                            || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos2))
                    )) {
                        this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.START_DESTROY_BLOCK, this.blockPos2, this.direction));
                    }

                    if ((
                        this.listmode.get() == listModes.whitelist
                            && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos2).getBlock())
                            || this.listmode.get() == listModes.blacklist
                            && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos2).getBlock())
                    )
                        && (
                        this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                            && BlockUtils.canBreak(this.blockPos2)
                            || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos2))
                    )) {
                        this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.STOP_DESTROY_BLOCK, this.blockPos2, this.direction));
                    }

                    if ((
                        this.listmode.get() == listModes.whitelist
                            && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos1).getBlock())
                            || this.listmode.get() == listModes.blacklist
                            && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos1).getBlock())
                    )
                        && (
                        this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                            && BlockUtils.canBreak(this.blockPos1)
                            || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos1))
                    )) {
                        this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.START_DESTROY_BLOCK, this.blockPos1, this.direction));
                    }

                    if ((
                        this.listmode.get() == listModes.whitelist
                            && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos1).getBlock())
                            || this.listmode.get() == listModes.blacklist
                            && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos1).getBlock())
                    )
                        && (
                        this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                            && BlockUtils.canBreak(this.blockPos1)
                            || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos1))
                    )) {
                        this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.STOP_DESTROY_BLOCK, this.blockPos1, this.direction));
                    }
                }

                if (this.playermovingdirection == Direction.EAST || this.playermovingdirection == Direction.WEST) {
                    if ((
                        this.listmode.get() == listModes.whitelist
                            && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos4).getBlock())
                            || this.listmode.get() == listModes.blacklist
                            && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos4).getBlock())
                    )
                        && (
                        this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                            && BlockUtils.canBreak(this.blockPos4)
                            || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos4))
                    )) {
                        this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.START_DESTROY_BLOCK, this.blockPos4, this.direction));
                    }

                    if ((
                        this.listmode.get() == listModes.whitelist
                            && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos4).getBlock())
                            || this.listmode.get() == listModes.blacklist
                            && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos4).getBlock())
                    )
                        && (
                        this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                            && BlockUtils.canBreak(this.blockPos4)
                            || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos4))
                    )) {
                        this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.STOP_DESTROY_BLOCK, this.blockPos4, this.direction));
                    }

                    if ((
                        this.listmode.get() == listModes.whitelist
                            && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos3).getBlock())
                            || this.listmode.get() == listModes.blacklist
                            && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos3).getBlock())
                    )
                        && (
                        this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                            && BlockUtils.canBreak(this.blockPos3)
                            || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos3))
                    )) {
                        this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.START_DESTROY_BLOCK, this.blockPos3, this.direction));
                    }

                    if ((
                        this.listmode.get() == listModes.whitelist
                            && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos3).getBlock())
                            || this.listmode.get() == listModes.blacklist
                            && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos3).getBlock())
                    )
                        && (
                        this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                            && BlockUtils.canBreak(this.blockPos3)
                            || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos3))
                    )) {
                        this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.STOP_DESTROY_BLOCK, this.blockPos3, this.direction));
                    }
                }

                if ((
                    this.listmode.get() == listModes.whitelist
                        && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos).getBlock())
                        || this.listmode.get() == listModes.blacklist
                        && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos).getBlock())
                )
                    && this.rotate.get()
                    && BlockUtils.canBreak(this.blockPos)) {
                    Rotations.rotate(
                        Rotations.getYaw(this.blockPos),
                        Rotations.getPitch(this.blockPos),
                        () -> this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.START_DESTROY_BLOCK, this.blockPos, this.direction))
                    );
                } else if ((
                    this.listmode.get() == listModes.whitelist
                        && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos).getBlock())
                        || this.listmode.get() == listModes.blacklist
                        && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos).getBlock())
                )
                    && !this.rotate.get()
                    && BlockUtils.canBreak(this.blockPos)) {
                    this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.START_DESTROY_BLOCK, this.blockPos, this.direction));
                }

                if (this.swing.get()) {
                    this.mc.getNetworkHandler().sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
                }
            }

            if (this.shouldMine() && this.range.get() == 3) {
                if ((
                    this.listmode.get() == listModes.whitelist
                        && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos).getBlock())
                        || this.listmode.get() == listModes.blacklist
                        && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos).getBlock())
                )
                    && this.rotate.get()
                    && BlockUtils.canBreak(this.blockPos)) {
                    Rotations.rotate(
                        Rotations.getYaw(this.blockPos),
                        Rotations.getPitch(this.blockPos),
                        () -> this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.STOP_DESTROY_BLOCK, this.blockPos, this.direction))
                    );
                } else if ((
                    this.listmode.get() == listModes.whitelist
                        && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos).getBlock())
                        || this.listmode.get() == listModes.blacklist
                        && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos).getBlock())
                )
                    && !this.rotate.get()
                    && BlockUtils.canBreak(this.blockPos)) {
                    this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.STOP_DESTROY_BLOCK, this.blockPos, this.direction));
                }

                if (this.aorient.get() && this.playerpitch <= 30 && this.playerpitch >= -30
                    || this.mode.get() == Modes.Vertical && !this.aorient.get()) {
                    if (this.playermovingdirection == Direction.NORTH
                        || this.playermovingdirection == Direction.SOUTH && this.playerpitch <= 30 && this.playerpitch >= -30) {
                        if ((
                            this.listmode.get() == listModes.whitelist
                                && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos2).getBlock())
                                || this.listmode.get() == listModes.blacklist
                                && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos2).getBlock())
                        )
                            && (
                            this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                                && BlockUtils.canBreak(this.blockPos2)
                                || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos2))
                        )) {
                            this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.START_DESTROY_BLOCK, this.blockPos2, this.direction));
                        }

                        if ((
                            this.listmode.get() == listModes.whitelist
                                && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos2).getBlock())
                                || this.listmode.get() == listModes.blacklist
                                && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos2).getBlock())
                        )
                            && (
                            this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                                && BlockUtils.canBreak(this.blockPos2)
                                || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos2))
                        )) {
                            this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.STOP_DESTROY_BLOCK, this.blockPos2, this.direction));
                        }

                        if ((
                            this.listmode.get() == listModes.whitelist
                                && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos1).getBlock())
                                || this.listmode.get() == listModes.blacklist
                                && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos1).getBlock())
                        )
                            && (
                            this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                                && BlockUtils.canBreak(this.blockPos1)
                                || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos1))
                        )) {
                            this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.START_DESTROY_BLOCK, this.blockPos1, this.direction));
                        }

                        if ((
                            this.listmode.get() == listModes.whitelist
                                && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos1).getBlock())
                                || this.listmode.get() == listModes.blacklist
                                && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos1).getBlock())
                        )
                            && (
                            this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                                && BlockUtils.canBreak(this.blockPos1)
                                || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos1))
                        )) {
                            this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.STOP_DESTROY_BLOCK, this.blockPos1, this.direction));
                        }

                        if ((
                            this.listmode.get() == listModes.whitelist
                                && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos9).getBlock())
                                || this.listmode.get() == listModes.blacklist
                                && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos9).getBlock())
                        )
                            && (
                            this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                                && BlockUtils.canBreak(this.blockPos9)
                                || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos9))
                        )) {
                            this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.START_DESTROY_BLOCK, this.blockPos9, this.direction));
                        }

                        if ((
                            this.listmode.get() == listModes.whitelist
                                && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos9).getBlock())
                                || this.listmode.get() == listModes.blacklist
                                && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos9).getBlock())
                        )
                            && (
                            this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                                && BlockUtils.canBreak(this.blockPos9)
                                || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos9))
                        )) {
                            this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.STOP_DESTROY_BLOCK, this.blockPos9, this.direction));
                        }

                        if ((
                            this.listmode.get() == listModes.whitelist
                                && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos18).getBlock())
                                || this.listmode.get() == listModes.blacklist
                                && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos18).getBlock())
                        )
                            && (
                            this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                                && BlockUtils.canBreak(this.blockPos18)
                                || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos18))
                        )) {
                            this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.START_DESTROY_BLOCK, this.blockPos18, this.direction));
                        }

                        if ((
                            this.listmode.get() == listModes.whitelist
                                && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos18).getBlock())
                                || this.listmode.get() == listModes.blacklist
                                && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos18).getBlock())
                        )
                            && (
                            this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                                && BlockUtils.canBreak(this.blockPos18)
                                || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos18))
                        )) {
                            this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.STOP_DESTROY_BLOCK, this.blockPos18, this.direction));
                        }
                    }

                    if (this.playermovingdirection == Direction.EAST || this.playermovingdirection == Direction.WEST && this.playerpitch <= 30 && this.playerpitch >= -30
                    ) {
                        if ((
                            this.listmode.get() == listModes.whitelist
                                && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos4).getBlock())
                                || this.listmode.get() == listModes.blacklist
                                && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos4).getBlock())
                        )
                            && (
                            this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                                && BlockUtils.canBreak(this.blockPos4)
                                || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos4))
                        )) {
                            this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.START_DESTROY_BLOCK, this.blockPos4, this.direction));
                        }

                        if ((
                            this.listmode.get() == listModes.whitelist
                                && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos4).getBlock())
                                || this.listmode.get() == listModes.blacklist
                                && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos4).getBlock())
                        )
                            && (
                            this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                                && BlockUtils.canBreak(this.blockPos4)
                                || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos4))
                        )) {
                            this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.STOP_DESTROY_BLOCK, this.blockPos4, this.direction));
                        }

                        if ((
                            this.listmode.get() == listModes.whitelist
                                && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos3).getBlock())
                                || this.listmode.get() == listModes.blacklist
                                && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos3).getBlock())
                        )
                            && (
                            this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                                && BlockUtils.canBreak(this.blockPos3)
                                || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos3))
                        )) {
                            this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.START_DESTROY_BLOCK, this.blockPos3, this.direction));
                        }

                        if ((
                            this.listmode.get() == listModes.whitelist
                                && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos3).getBlock())
                                || this.listmode.get() == listModes.blacklist
                                && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos3).getBlock())
                        )
                            && (
                            this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                                && BlockUtils.canBreak(this.blockPos3)
                                || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos3))
                        )) {
                            this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.STOP_DESTROY_BLOCK, this.blockPos3, this.direction));
                        }

                        if ((
                            this.listmode.get() == listModes.whitelist
                                && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos9).getBlock())
                                || this.listmode.get() == listModes.blacklist
                                && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos9).getBlock())
                        )
                            && (
                            this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                                && BlockUtils.canBreak(this.blockPos9)
                                || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos9))
                        )) {
                            this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.START_DESTROY_BLOCK, this.blockPos9, this.direction));
                        }

                        if ((
                            this.listmode.get() == listModes.whitelist
                                && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos9).getBlock())
                                || this.listmode.get() == listModes.blacklist
                                && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos9).getBlock())
                        )
                            && (
                            this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                                && BlockUtils.canBreak(this.blockPos9)
                                || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos9))
                        )) {
                            this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.STOP_DESTROY_BLOCK, this.blockPos9, this.direction));
                        }

                        if ((
                            this.listmode.get() == listModes.whitelist
                                && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos18).getBlock())
                                || this.listmode.get() == listModes.blacklist
                                && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos18).getBlock())
                        )
                            && (
                            this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                                && BlockUtils.canBreak(this.blockPos18)
                                || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos18))
                        )) {
                            this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.START_DESTROY_BLOCK, this.blockPos18, this.direction));
                        }

                        if ((
                            this.listmode.get() == listModes.whitelist
                                && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos18).getBlock())
                                || this.listmode.get() == listModes.blacklist
                                && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos18).getBlock())
                        )
                            && (
                            this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                                && BlockUtils.canBreak(this.blockPos18)
                                || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos18))
                        )) {
                            this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.STOP_DESTROY_BLOCK, this.blockPos18, this.direction));
                        }
                    }
                }

                if (this.aorient.get() && this.playerpitch > 30 | this.playerpitch < -30
                    || this.mode.get() == Modes.Horizontal && !this.aorient.get()) {
                    if ((
                        this.listmode.get() == listModes.whitelist
                            && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos1).getBlock())
                            || this.listmode.get() == listModes.blacklist
                            && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos1).getBlock())
                    )
                        && (
                        this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                            && BlockUtils.canBreak(this.blockPos1)
                            || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos1))
                    )) {
                        this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.START_DESTROY_BLOCK, this.blockPos1, this.direction));
                    }

                    if ((
                        this.listmode.get() == listModes.whitelist
                            && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos1).getBlock())
                            || this.listmode.get() == listModes.blacklist
                            && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos1).getBlock())
                    )
                        && (
                        this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                            && BlockUtils.canBreak(this.blockPos1)
                            || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos1))
                    )) {
                        this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.STOP_DESTROY_BLOCK, this.blockPos1, this.direction));
                    }

                    if ((
                        this.listmode.get() == listModes.whitelist
                            && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos2).getBlock())
                            || this.listmode.get() == listModes.blacklist
                            && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos2).getBlock())
                    )
                        && (
                        this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                            && BlockUtils.canBreak(this.blockPos2)
                            || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos2))
                    )) {
                        this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.START_DESTROY_BLOCK, this.blockPos2, this.direction));
                    }

                    if ((
                        this.listmode.get() == listModes.whitelist
                            && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos2).getBlock())
                            || this.listmode.get() == listModes.blacklist
                            && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos2).getBlock())
                    )
                        && (
                        this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                            && BlockUtils.canBreak(this.blockPos2)
                            || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos2))
                    )) {
                        this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.STOP_DESTROY_BLOCK, this.blockPos2, this.direction));
                    }

                    if ((
                        this.listmode.get() == listModes.whitelist
                            && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos3).getBlock())
                            || this.listmode.get() == listModes.blacklist
                            && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos3).getBlock())
                    )
                        && (
                        this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                            && BlockUtils.canBreak(this.blockPos3)
                            || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos3))
                    )) {
                        this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.START_DESTROY_BLOCK, this.blockPos3, this.direction));
                    }

                    if ((
                        this.listmode.get() == listModes.whitelist
                            && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos3).getBlock())
                            || this.listmode.get() == listModes.blacklist
                            && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos3).getBlock())
                    )
                        && (
                        this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                            && BlockUtils.canBreak(this.blockPos3)
                            || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos3))
                    )) {
                        this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.STOP_DESTROY_BLOCK, this.blockPos3, this.direction));
                    }

                    if ((
                        this.listmode.get() == listModes.whitelist
                            && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos4).getBlock())
                            || this.listmode.get() == listModes.blacklist
                            && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos4).getBlock())
                    )
                        && (
                        this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                            && BlockUtils.canBreak(this.blockPos4)
                            || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos4))
                    )) {
                        this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.START_DESTROY_BLOCK, this.blockPos4, this.direction));
                    }

                    if ((
                        this.listmode.get() == listModes.whitelist
                            && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos4).getBlock())
                            || this.listmode.get() == listModes.blacklist
                            && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos4).getBlock())
                    )
                        && (
                        this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                            && BlockUtils.canBreak(this.blockPos4)
                            || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos4))
                    )) {
                        this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.STOP_DESTROY_BLOCK, this.blockPos4, this.direction));
                    }
                }

                if ((
                    this.listmode.get() == listModes.whitelist
                        && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos).getBlock())
                        || this.listmode.get() == listModes.blacklist
                        && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos).getBlock())
                )
                    && this.rotate.get()
                    && BlockUtils.canBreak(this.blockPos)) {
                    Rotations.rotate(
                        Rotations.getYaw(this.blockPos),
                        Rotations.getPitch(this.blockPos),
                        () -> this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.START_DESTROY_BLOCK, this.blockPos, this.direction))
                    );
                } else if ((
                    this.listmode.get() == listModes.whitelist
                        && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos).getBlock())
                        || this.listmode.get() == listModes.blacklist
                        && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos).getBlock())
                )
                    && !this.rotate.get()
                    && BlockUtils.canBreak(this.blockPos)) {
                    this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.START_DESTROY_BLOCK, this.blockPos, this.direction));
                }

                if (this.swing.get()) {
                    this.mc.getNetworkHandler().sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
                }
            }

            if (this.shouldMine() && this.range.get() == 4) {
                if ((
                    this.listmode.get() == listModes.whitelist
                        && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos).getBlock())
                        || this.listmode.get() == listModes.blacklist
                        && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos).getBlock())
                )
                    && this.rotate.get()
                    && BlockUtils.canBreak(this.blockPos)) {
                    Rotations.rotate(
                        Rotations.getYaw(this.blockPos),
                        Rotations.getPitch(this.blockPos),
                        () -> this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.STOP_DESTROY_BLOCK, this.blockPos, this.direction))
                    );
                } else if ((
                    this.listmode.get() == listModes.whitelist
                        && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos).getBlock())
                        || this.listmode.get() == listModes.blacklist
                        && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos).getBlock())
                )
                    && !this.rotate.get()
                    && BlockUtils.canBreak(this.blockPos)) {
                    this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.STOP_DESTROY_BLOCK, this.blockPos, this.direction));
                }

                if (this.aorient.get() && this.playerpitch <= 30 && this.playerpitch >= -30
                    || this.mode.get() == Modes.Vertical && !this.aorient.get()) {
                    if (this.playermovingdirection == Direction.NORTH || this.playermovingdirection == Direction.SOUTH) {
                        if ((
                            this.listmode.get() == listModes.whitelist
                                && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos2).getBlock())
                                || this.listmode.get() == listModes.blacklist
                                && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos2).getBlock())
                        )
                            && (
                            this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                                && BlockUtils.canBreak(this.blockPos2)
                                || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos2))
                        )) {
                            this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.START_DESTROY_BLOCK, this.blockPos2, this.direction));
                        }

                        if ((
                            this.listmode.get() == listModes.whitelist
                                && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos2).getBlock())
                                || this.listmode.get() == listModes.blacklist
                                && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos2).getBlock())
                        )
                            && (
                            this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                                && BlockUtils.canBreak(this.blockPos2)
                                || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos2))
                        )) {
                            this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.STOP_DESTROY_BLOCK, this.blockPos2, this.direction));
                        }

                        if ((
                            this.listmode.get() == listModes.whitelist
                                && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos1).getBlock())
                                || this.listmode.get() == listModes.blacklist
                                && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos1).getBlock())
                        )
                            && (
                            this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                                && BlockUtils.canBreak(this.blockPos1)
                                || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos1))
                        )) {
                            this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.START_DESTROY_BLOCK, this.blockPos1, this.direction));
                        }

                        if ((
                            this.listmode.get() == listModes.whitelist
                                && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos1).getBlock())
                                || this.listmode.get() == listModes.blacklist
                                && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos1).getBlock())
                        )
                            && (
                            this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                                && BlockUtils.canBreak(this.blockPos1)
                                || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos1))
                        )) {
                            this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.STOP_DESTROY_BLOCK, this.blockPos1, this.direction));
                        }

                        if ((
                            this.listmode.get() == listModes.whitelist
                                && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos9).getBlock())
                                || this.listmode.get() == listModes.blacklist
                                && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos9).getBlock())
                        )
                            && (
                            this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                                && BlockUtils.canBreak(this.blockPos9)
                                || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos9))
                        )) {
                            this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.START_DESTROY_BLOCK, this.blockPos9, this.direction));
                        }

                        if ((
                            this.listmode.get() == listModes.whitelist
                                && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos9).getBlock())
                                || this.listmode.get() == listModes.blacklist
                                && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos9).getBlock())
                        )
                            && (
                            this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                                && BlockUtils.canBreak(this.blockPos9)
                                || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos9))
                        )) {
                            this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.STOP_DESTROY_BLOCK, this.blockPos9, this.direction));
                        }

                        if ((
                            this.listmode.get() == listModes.whitelist
                                && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos18).getBlock())
                                || this.listmode.get() == listModes.blacklist
                                && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos18).getBlock())
                        )
                            && (
                            this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                                && BlockUtils.canBreak(this.blockPos18)
                                || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos18))
                        )) {
                            this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.START_DESTROY_BLOCK, this.blockPos18, this.direction));
                        }

                        if ((
                            this.listmode.get() == listModes.whitelist
                                && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos18).getBlock())
                                || this.listmode.get() == listModes.blacklist
                                && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos18).getBlock())
                        )
                            && (
                            this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                                && BlockUtils.canBreak(this.blockPos18)
                                || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos18))
                        )) {
                            this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.STOP_DESTROY_BLOCK, this.blockPos18, this.direction));
                        }

                        if ((
                            this.listmode.get() == listModes.whitelist
                                && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos10).getBlock())
                                || this.listmode.get() == listModes.blacklist
                                && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos10).getBlock())
                        )
                            && (
                            this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                                && BlockUtils.canBreak(this.blockPos10)
                                || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos10))
                        )) {
                            this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.START_DESTROY_BLOCK, this.blockPos10, this.direction));
                        }

                        if ((
                            this.listmode.get() == listModes.whitelist
                                && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos10).getBlock())
                                || this.listmode.get() == listModes.blacklist
                                && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos10).getBlock())
                        )
                            && (
                            this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                                && BlockUtils.canBreak(this.blockPos10)
                                || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos10))
                        )) {
                            this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.STOP_DESTROY_BLOCK, this.blockPos10, this.direction));
                        }

                        if ((
                            this.listmode.get() == listModes.whitelist
                                && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos11).getBlock())
                                || this.listmode.get() == listModes.blacklist
                                && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos11).getBlock())
                        )
                            && (
                            this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                                && BlockUtils.canBreak(this.blockPos11)
                                || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos11))
                        )) {
                            this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.START_DESTROY_BLOCK, this.blockPos11, this.direction));
                        }

                        if ((
                            this.listmode.get() == listModes.whitelist
                                && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos11).getBlock())
                                || this.listmode.get() == listModes.blacklist
                                && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos11).getBlock())
                        )
                            && (
                            this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                                && BlockUtils.canBreak(this.blockPos11)
                                || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos11))
                        )) {
                            this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.STOP_DESTROY_BLOCK, this.blockPos11, this.direction));
                        }

                        if ((
                            this.listmode.get() == listModes.whitelist
                                && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos19).getBlock())
                                || this.listmode.get() == listModes.blacklist
                                && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos19).getBlock())
                        )
                            && (
                            this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                                && BlockUtils.canBreak(this.blockPos19)
                                || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos19))
                        )) {
                            this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.START_DESTROY_BLOCK, this.blockPos19, this.direction));
                        }

                        if ((
                            this.listmode.get() == listModes.whitelist
                                && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos19).getBlock())
                                || this.listmode.get() == listModes.blacklist
                                && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos19).getBlock())
                        )
                            && (
                            this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                                && BlockUtils.canBreak(this.blockPos19)
                                || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos19))
                        )) {
                            this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.STOP_DESTROY_BLOCK, this.blockPos19, this.direction));
                        }

                        if ((
                            this.listmode.get() == listModes.whitelist
                                && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos20).getBlock())
                                || this.listmode.get() == listModes.blacklist
                                && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos20).getBlock())
                        )
                            && (
                            this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                                && BlockUtils.canBreak(this.blockPos20)
                                || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos20))
                        )) {
                            this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.START_DESTROY_BLOCK, this.blockPos20, this.direction));
                        }

                        if ((
                            this.listmode.get() == listModes.whitelist
                                && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos20).getBlock())
                                || this.listmode.get() == listModes.blacklist
                                && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos20).getBlock())
                        )
                            && (
                            this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                                && BlockUtils.canBreak(this.blockPos20)
                                || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos20))
                        )) {
                            this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.STOP_DESTROY_BLOCK, this.blockPos20, this.direction));
                        }
                    }

                    if (this.playermovingdirection == Direction.EAST || this.playermovingdirection == Direction.WEST) {
                        if ((
                            this.listmode.get() == listModes.whitelist
                                && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos4).getBlock())
                                || this.listmode.get() == listModes.blacklist
                                && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos4).getBlock())
                        )
                            && (
                            this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                                && BlockUtils.canBreak(this.blockPos4)
                                || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos4))
                        )) {
                            this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.START_DESTROY_BLOCK, this.blockPos4, this.direction));
                        }

                        if ((
                            this.listmode.get() == listModes.whitelist
                                && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos4).getBlock())
                                || this.listmode.get() == listModes.blacklist
                                && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos4).getBlock())
                        )
                            && (
                            this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                                && BlockUtils.canBreak(this.blockPos4)
                                || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos4))
                        )) {
                            this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.STOP_DESTROY_BLOCK, this.blockPos4, this.direction));
                        }

                        if ((
                            this.listmode.get() == listModes.whitelist
                                && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos3).getBlock())
                                || this.listmode.get() == listModes.blacklist
                                && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos3).getBlock())
                        )
                            && (
                            this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                                && BlockUtils.canBreak(this.blockPos3)
                                || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos3))
                        )) {
                            this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.START_DESTROY_BLOCK, this.blockPos3, this.direction));
                        }

                        if ((
                            this.listmode.get() == listModes.whitelist
                                && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos3).getBlock())
                                || this.listmode.get() == listModes.blacklist
                                && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos3).getBlock())
                        )
                            && (
                            this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                                && BlockUtils.canBreak(this.blockPos3)
                                || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos3))
                        )) {
                            this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.STOP_DESTROY_BLOCK, this.blockPos3, this.direction));
                        }

                        if ((
                            this.listmode.get() == listModes.whitelist
                                && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos9).getBlock())
                                || this.listmode.get() == listModes.blacklist
                                && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos9).getBlock())
                        )
                            && (
                            this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                                && BlockUtils.canBreak(this.blockPos9)
                                || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos9))
                        )) {
                            this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.START_DESTROY_BLOCK, this.blockPos9, this.direction));
                        }

                        if ((
                            this.listmode.get() == listModes.whitelist
                                && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos9).getBlock())
                                || this.listmode.get() == listModes.blacklist
                                && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos9).getBlock())
                        )
                            && (
                            this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                                && BlockUtils.canBreak(this.blockPos9)
                                || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos9))
                        )) {
                            this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.STOP_DESTROY_BLOCK, this.blockPos9, this.direction));
                        }

                        if ((
                            this.listmode.get() == listModes.whitelist
                                && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos18).getBlock())
                                || this.listmode.get() == listModes.blacklist
                                && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos18).getBlock())
                        )
                            && (
                            this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                                && BlockUtils.canBreak(this.blockPos18)
                                || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos18))
                        )) {
                            this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.START_DESTROY_BLOCK, this.blockPos18, this.direction));
                        }

                        if ((
                            this.listmode.get() == listModes.whitelist
                                && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos18).getBlock())
                                || this.listmode.get() == listModes.blacklist
                                && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos18).getBlock())
                        )
                            && (
                            this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                                && BlockUtils.canBreak(this.blockPos18)
                                || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos18))
                        )) {
                            this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.STOP_DESTROY_BLOCK, this.blockPos18, this.direction));
                        }

                        if ((
                            this.listmode.get() == listModes.whitelist
                                && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos12).getBlock())
                                || this.listmode.get() == listModes.blacklist
                                && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos12).getBlock())
                        )
                            && (
                            this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                                && BlockUtils.canBreak(this.blockPos12)
                                || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos12))
                        )) {
                            this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.START_DESTROY_BLOCK, this.blockPos12, this.direction));
                        }

                        if ((
                            this.listmode.get() == listModes.whitelist
                                && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos12).getBlock())
                                || this.listmode.get() == listModes.blacklist
                                && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos12).getBlock())
                        )
                            && (
                            this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                                && BlockUtils.canBreak(this.blockPos12)
                                || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos12))
                        )) {
                            this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.STOP_DESTROY_BLOCK, this.blockPos12, this.direction));
                        }

                        if ((
                            this.listmode.get() == listModes.whitelist
                                && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos13).getBlock())
                                || this.listmode.get() == listModes.blacklist
                                && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos13).getBlock())
                        )
                            && (
                            this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                                && BlockUtils.canBreak(this.blockPos13)
                                || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos13))
                        )) {
                            this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.START_DESTROY_BLOCK, this.blockPos13, this.direction));
                        }

                        if ((
                            this.listmode.get() == listModes.whitelist
                                && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos13).getBlock())
                                || this.listmode.get() == listModes.blacklist
                                && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos13).getBlock())
                        )
                            && (
                            this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                                && BlockUtils.canBreak(this.blockPos13)
                                || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos13))
                        )) {
                            this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.STOP_DESTROY_BLOCK, this.blockPos13, this.direction));
                        }

                        if ((
                            this.listmode.get() == listModes.whitelist
                                && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos21).getBlock())
                                || this.listmode.get() == listModes.blacklist
                                && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos21).getBlock())
                        )
                            && (
                            this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                                && BlockUtils.canBreak(this.blockPos21)
                                || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos21))
                        )) {
                            this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.START_DESTROY_BLOCK, this.blockPos21, this.direction));
                        }

                        if ((
                            this.listmode.get() == listModes.whitelist
                                && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos21).getBlock())
                                || this.listmode.get() == listModes.blacklist
                                && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos21).getBlock())
                        )
                            && (
                            this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                                && BlockUtils.canBreak(this.blockPos21)
                                || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos21))
                        )) {
                            this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.STOP_DESTROY_BLOCK, this.blockPos21, this.direction));
                        }

                        if ((
                            this.listmode.get() == listModes.whitelist
                                && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos22).getBlock())
                                || this.listmode.get() == listModes.blacklist
                                && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos22).getBlock())
                        )
                            && (
                            this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                                && BlockUtils.canBreak(this.blockPos22)
                                || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos22))
                        )) {
                            this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.START_DESTROY_BLOCK, this.blockPos22, this.direction));
                        }

                        if ((
                            this.listmode.get() == listModes.whitelist
                                && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos22).getBlock())
                                || this.listmode.get() == listModes.blacklist
                                && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos22).getBlock())
                        )
                            && (
                            this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                                && BlockUtils.canBreak(this.blockPos22)
                                || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos22))
                        )) {
                            this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.STOP_DESTROY_BLOCK, this.blockPos22, this.direction));
                        }
                    }
                }

                if (this.aorient.get() && this.playerpitch > 30 | this.playerpitch < -30
                    || this.mode.get() == Modes.Horizontal && !this.aorient.get()) {
                    if ((
                        this.listmode.get() == listModes.whitelist
                            && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos1).getBlock())
                            || this.listmode.get() == listModes.blacklist
                            && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos1).getBlock())
                    )
                        && (
                        this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                            && BlockUtils.canBreak(this.blockPos1)
                            || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos1))
                    )) {
                        this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.START_DESTROY_BLOCK, this.blockPos1, this.direction));
                    }

                    if ((
                        this.listmode.get() == listModes.whitelist
                            && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos1).getBlock())
                            || this.listmode.get() == listModes.blacklist
                            && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos1).getBlock())
                    )
                        && (
                        this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                            && BlockUtils.canBreak(this.blockPos1)
                            || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos1))
                    )) {
                        this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.STOP_DESTROY_BLOCK, this.blockPos1, this.direction));
                    }

                    if ((
                        this.listmode.get() == listModes.whitelist
                            && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos2).getBlock())
                            || this.listmode.get() == listModes.blacklist
                            && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos2).getBlock())
                    )
                        && (
                        this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                            && BlockUtils.canBreak(this.blockPos2)
                            || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos2))
                    )) {
                        this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.START_DESTROY_BLOCK, this.blockPos2, this.direction));
                    }

                    if ((
                        this.listmode.get() == listModes.whitelist
                            && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos2).getBlock())
                            || this.listmode.get() == listModes.blacklist
                            && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos2).getBlock())
                    )
                        && (
                        this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                            && BlockUtils.canBreak(this.blockPos2)
                            || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos2))
                    )) {
                        this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.STOP_DESTROY_BLOCK, this.blockPos2, this.direction));
                    }

                    if ((
                        this.listmode.get() == listModes.whitelist
                            && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos3).getBlock())
                            || this.listmode.get() == listModes.blacklist
                            && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos3).getBlock())
                    )
                        && (
                        this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                            && BlockUtils.canBreak(this.blockPos3)
                            || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos3))
                    )) {
                        this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.START_DESTROY_BLOCK, this.blockPos3, this.direction));
                    }

                    if ((
                        this.listmode.get() == listModes.whitelist
                            && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos3).getBlock())
                            || this.listmode.get() == listModes.blacklist
                            && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos3).getBlock())
                    )
                        && (
                        this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                            && BlockUtils.canBreak(this.blockPos3)
                            || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos3))
                    )) {
                        this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.STOP_DESTROY_BLOCK, this.blockPos3, this.direction));
                    }

                    if ((
                        this.listmode.get() == listModes.whitelist
                            && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos4).getBlock())
                            || this.listmode.get() == listModes.blacklist
                            && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos4).getBlock())
                    )
                        && (
                        this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                            && BlockUtils.canBreak(this.blockPos4)
                            || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos4))
                    )) {
                        this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.START_DESTROY_BLOCK, this.blockPos4, this.direction));
                    }

                    if ((
                        this.listmode.get() == listModes.whitelist
                            && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos4).getBlock())
                            || this.listmode.get() == listModes.blacklist
                            && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos4).getBlock())
                    )
                        && (
                        this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                            && BlockUtils.canBreak(this.blockPos4)
                            || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos4))
                    )) {
                        this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.STOP_DESTROY_BLOCK, this.blockPos4, this.direction));
                    }

                    if ((
                        this.listmode.get() == listModes.whitelist
                            && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos5).getBlock())
                            || this.listmode.get() == listModes.blacklist
                            && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos5).getBlock())
                    )
                        && (
                        this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                            && BlockUtils.canBreak(this.blockPos5)
                            || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos5))
                    )) {
                        this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.START_DESTROY_BLOCK, this.blockPos5, this.direction));
                    }

                    if ((
                        this.listmode.get() == listModes.whitelist
                            && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos5).getBlock())
                            || this.listmode.get() == listModes.blacklist
                            && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos5).getBlock())
                    )
                        && (
                        this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                            && BlockUtils.canBreak(this.blockPos5)
                            || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos5))
                    )) {
                        this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.STOP_DESTROY_BLOCK, this.blockPos5, this.direction));
                    }

                    if ((
                        this.listmode.get() == listModes.whitelist
                            && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos6).getBlock())
                            || this.listmode.get() == listModes.blacklist
                            && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos6).getBlock())
                    )
                        && (
                        this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                            && BlockUtils.canBreak(this.blockPos6)
                            || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos6))
                    )) {
                        this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.START_DESTROY_BLOCK, this.blockPos6, this.direction));
                    }

                    if ((
                        this.listmode.get() == listModes.whitelist
                            && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos6).getBlock())
                            || this.listmode.get() == listModes.blacklist
                            && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos6).getBlock())
                    )
                        && (
                        this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                            && BlockUtils.canBreak(this.blockPos6)
                            || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos6))
                    )) {
                        this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.STOP_DESTROY_BLOCK, this.blockPos6, this.direction));
                    }

                    if ((
                        this.listmode.get() == listModes.whitelist
                            && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos7).getBlock())
                            || this.listmode.get() == listModes.blacklist
                            && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos7).getBlock())
                    )
                        && (
                        this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                            && BlockUtils.canBreak(this.blockPos7)
                            || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos7))
                    )) {
                        this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.START_DESTROY_BLOCK, this.blockPos7, this.direction));
                    }

                    if ((
                        this.listmode.get() == listModes.whitelist
                            && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos7).getBlock())
                            || this.listmode.get() == listModes.blacklist
                            && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos7).getBlock())
                    )
                        && (
                        this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                            && BlockUtils.canBreak(this.blockPos7)
                            || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos7))
                    )) {
                        this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.STOP_DESTROY_BLOCK, this.blockPos7, this.direction));
                    }

                    if ((
                        this.listmode.get() == listModes.whitelist
                            && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos8).getBlock())
                            || this.listmode.get() == listModes.blacklist
                            && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos8).getBlock())
                    )
                        && (
                        this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                            && BlockUtils.canBreak(this.blockPos8)
                            || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos8))
                    )) {
                        this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.START_DESTROY_BLOCK, this.blockPos8, this.direction));
                    }

                    if ((
                        this.listmode.get() == listModes.whitelist
                            && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos8).getBlock())
                            || this.listmode.get() == listModes.blacklist
                            && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos8).getBlock())
                    )
                        && (
                        this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                            && BlockUtils.canBreak(this.blockPos8)
                            || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos8))
                    )) {
                        this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.STOP_DESTROY_BLOCK, this.blockPos8, this.direction));
                    }
                }

                if ((
                    this.listmode.get() == listModes.whitelist
                        && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos).getBlock())
                        || this.listmode.get() == listModes.blacklist
                        && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos).getBlock())
                )
                    && this.rotate.get()
                    && BlockUtils.canBreak(this.blockPos)) {
                    Rotations.rotate(
                        Rotations.getYaw(this.blockPos),
                        Rotations.getPitch(this.blockPos),
                        () -> this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.START_DESTROY_BLOCK, this.blockPos, this.direction))
                    );
                } else if ((
                    this.listmode.get() == listModes.whitelist
                        && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos).getBlock())
                        || this.listmode.get() == listModes.blacklist
                        && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos).getBlock())
                )
                    && !this.rotate.get()
                    && BlockUtils.canBreak(this.blockPos)) {
                    this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.START_DESTROY_BLOCK, this.blockPos, this.direction));
                }

                if (this.swing.get()) {
                    this.mc.getNetworkHandler().sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
                }
            }

            if (this.shouldMine() && this.range.get() == 5) {
                if ((
                    this.listmode.get() == listModes.whitelist
                        && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos).getBlock())
                        || this.listmode.get() == listModes.blacklist
                        && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos).getBlock())
                )
                    && this.rotate.get()
                    && BlockUtils.canBreak(this.blockPos)) {
                    Rotations.rotate(
                        Rotations.getYaw(this.blockPos),
                        Rotations.getPitch(this.blockPos),
                        () -> this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.STOP_DESTROY_BLOCK, this.blockPos, this.direction))
                    );
                } else if ((
                    this.listmode.get() == listModes.whitelist
                        && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos).getBlock())
                        || this.listmode.get() == listModes.blacklist
                        && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos).getBlock())
                )
                    && !this.rotate.get()
                    && BlockUtils.canBreak(this.blockPos)) {
                    this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.STOP_DESTROY_BLOCK, this.blockPos, this.direction));
                }

                if ((
                    this.listmode.get() == listModes.whitelist
                        && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos1).getBlock())
                        || this.listmode.get() == listModes.blacklist
                        && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos1).getBlock())
                )
                    && (
                    this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                        && BlockUtils.canBreak(this.blockPos1)
                        || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos1))
                )) {
                    this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.START_DESTROY_BLOCK, this.blockPos1, this.direction));
                }

                if ((
                    this.listmode.get() == listModes.whitelist
                        && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos1).getBlock())
                        || this.listmode.get() == listModes.blacklist
                        && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos1).getBlock())
                )
                    && (
                    this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                        && BlockUtils.canBreak(this.blockPos1)
                        || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos1))
                )) {
                    this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.STOP_DESTROY_BLOCK, this.blockPos1, this.direction));
                }

                if ((
                    this.listmode.get() == listModes.whitelist
                        && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos2).getBlock())
                        || this.listmode.get() == listModes.blacklist
                        && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos2).getBlock())
                )
                    && (
                    this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                        && BlockUtils.canBreak(this.blockPos2)
                        || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos2))
                )) {
                    this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.START_DESTROY_BLOCK, this.blockPos2, this.direction));
                }

                if ((
                    this.listmode.get() == listModes.whitelist
                        && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos2).getBlock())
                        || this.listmode.get() == listModes.blacklist
                        && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos2).getBlock())
                )
                    && (
                    this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                        && BlockUtils.canBreak(this.blockPos2)
                        || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos2))
                )) {
                    this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.STOP_DESTROY_BLOCK, this.blockPos2, this.direction));
                }

                if ((
                    this.listmode.get() == listModes.whitelist
                        && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos3).getBlock())
                        || this.listmode.get() == listModes.blacklist
                        && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos3).getBlock())
                )
                    && (
                    this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                        && BlockUtils.canBreak(this.blockPos3)
                        || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos3))
                )) {
                    this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.START_DESTROY_BLOCK, this.blockPos3, this.direction));
                }

                if ((
                    this.listmode.get() == listModes.whitelist
                        && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos3).getBlock())
                        || this.listmode.get() == listModes.blacklist
                        && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos3).getBlock())
                )
                    && (
                    this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                        && BlockUtils.canBreak(this.blockPos3)
                        || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos3))
                )) {
                    this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.STOP_DESTROY_BLOCK, this.blockPos3, this.direction));
                }

                if ((
                    this.listmode.get() == listModes.whitelist
                        && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos4).getBlock())
                        || this.listmode.get() == listModes.blacklist
                        && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos4).getBlock())
                )
                    && (
                    this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                        && BlockUtils.canBreak(this.blockPos4)
                        || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos4))
                )) {
                    this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.START_DESTROY_BLOCK, this.blockPos4, this.direction));
                }

                if ((
                    this.listmode.get() == listModes.whitelist
                        && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos4).getBlock())
                        || this.listmode.get() == listModes.blacklist
                        && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos4).getBlock())
                )
                    && (
                    this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                        && BlockUtils.canBreak(this.blockPos4)
                        || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos4))
                )) {
                    this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.STOP_DESTROY_BLOCK, this.blockPos4, this.direction));
                }

                if ((
                    this.listmode.get() == listModes.whitelist
                        && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos5).getBlock())
                        || this.listmode.get() == listModes.blacklist
                        && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos5).getBlock())
                )
                    && (
                    this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                        && BlockUtils.canBreak(this.blockPos5)
                        || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos5))
                )) {
                    this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.START_DESTROY_BLOCK, this.blockPos5, this.direction));
                }

                if ((
                    this.listmode.get() == listModes.whitelist
                        && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos5).getBlock())
                        || this.listmode.get() == listModes.blacklist
                        && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos5).getBlock())
                )
                    && (
                    this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                        && BlockUtils.canBreak(this.blockPos5)
                        || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos5))
                )) {
                    this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.STOP_DESTROY_BLOCK, this.blockPos5, this.direction));
                }

                if ((
                    this.listmode.get() == listModes.whitelist
                        && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos6).getBlock())
                        || this.listmode.get() == listModes.blacklist
                        && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos6).getBlock())
                )
                    && (
                    this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                        && BlockUtils.canBreak(this.blockPos6)
                        || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos6))
                )) {
                    this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.START_DESTROY_BLOCK, this.blockPos6, this.direction));
                }

                if ((
                    this.listmode.get() == listModes.whitelist
                        && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos6).getBlock())
                        || this.listmode.get() == listModes.blacklist
                        && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos6).getBlock())
                )
                    && (
                    this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                        && BlockUtils.canBreak(this.blockPos6)
                        || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos6))
                )) {
                    this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.STOP_DESTROY_BLOCK, this.blockPos6, this.direction));
                }

                if ((
                    this.listmode.get() == listModes.whitelist
                        && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos7).getBlock())
                        || this.listmode.get() == listModes.blacklist
                        && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos7).getBlock())
                )
                    && (
                    this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                        && BlockUtils.canBreak(this.blockPos7)
                        || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos7))
                )) {
                    this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.START_DESTROY_BLOCK, this.blockPos7, this.direction));
                }

                if ((
                    this.listmode.get() == listModes.whitelist
                        && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos7).getBlock())
                        || this.listmode.get() == listModes.blacklist
                        && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos7).getBlock())
                )
                    && (
                    this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                        && BlockUtils.canBreak(this.blockPos7)
                        || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos7))
                )) {
                    this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.STOP_DESTROY_BLOCK, this.blockPos7, this.direction));
                }

                if ((
                    this.listmode.get() == listModes.whitelist
                        && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos8).getBlock())
                        || this.listmode.get() == listModes.blacklist
                        && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos8).getBlock())
                )
                    && (
                    this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                        && BlockUtils.canBreak(this.blockPos8)
                        || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos8))
                )) {
                    this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.START_DESTROY_BLOCK, this.blockPos8, this.direction));
                }

                if ((
                    this.listmode.get() == listModes.whitelist
                        && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos8).getBlock())
                        || this.listmode.get() == listModes.blacklist
                        && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos8).getBlock())
                )
                    && (
                    this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                        && BlockUtils.canBreak(this.blockPos8)
                        || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos8))
                )) {
                    this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.STOP_DESTROY_BLOCK, this.blockPos8, this.direction));
                }

                if ((
                    this.listmode.get() == listModes.whitelist
                        && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos9).getBlock())
                        || this.listmode.get() == listModes.blacklist
                        && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos9).getBlock())
                )
                    && (
                    this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                        && BlockUtils.canBreak(this.blockPos9)
                        || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos9))
                )) {
                    this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.START_DESTROY_BLOCK, this.blockPos9, this.direction));
                }

                if ((
                    this.listmode.get() == listModes.whitelist
                        && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos9).getBlock())
                        || this.listmode.get() == listModes.blacklist
                        && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos9).getBlock())
                )
                    && (
                    this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                        && BlockUtils.canBreak(this.blockPos9)
                        || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos9))
                )) {
                    this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.STOP_DESTROY_BLOCK, this.blockPos9, this.direction));
                }

                if ((
                    this.listmode.get() == listModes.whitelist
                        && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos18).getBlock())
                        || this.listmode.get() == listModes.blacklist
                        && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos18).getBlock())
                )
                    && (
                    this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                        && BlockUtils.canBreak(this.blockPos18)
                        || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos18))
                )) {
                    this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.START_DESTROY_BLOCK, this.blockPos18, this.direction));
                }

                if ((
                    this.listmode.get() == listModes.whitelist
                        && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos18).getBlock())
                        || this.listmode.get() == listModes.blacklist
                        && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos18).getBlock())
                )
                    && (
                    this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                        && BlockUtils.canBreak(this.blockPos18)
                        || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos18))
                )) {
                    this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.STOP_DESTROY_BLOCK, this.blockPos18, this.direction));
                }

                if ((
                    this.listmode.get() == listModes.whitelist
                        && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos).getBlock())
                        || this.listmode.get() == listModes.blacklist
                        && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos).getBlock())
                )
                    && this.rotate.get()
                    && BlockUtils.canBreak(this.blockPos)) {
                    Rotations.rotate(
                        Rotations.getYaw(this.blockPos),
                        Rotations.getPitch(this.blockPos),
                        () -> this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.START_DESTROY_BLOCK, this.blockPos, this.direction))
                    );
                } else if ((
                    this.listmode.get() == listModes.whitelist
                        && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos).getBlock())
                        || this.listmode.get() == listModes.blacklist
                        && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos).getBlock())
                )
                    && !this.rotate.get()
                    && BlockUtils.canBreak(this.blockPos)) {
                    this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.START_DESTROY_BLOCK, this.blockPos, this.direction));
                }

                if (this.swing.get()) {
                    this.mc.getNetworkHandler().sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
                }
            }

            if (this.shouldMine() && this.range.get() == 6) {
                if ((
                    this.listmode.get() == listModes.whitelist
                        && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos).getBlock())
                        || this.listmode.get() == listModes.blacklist
                        && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos).getBlock())
                )
                    && this.rotate.get()
                    && BlockUtils.canBreak(this.blockPos)) {
                    Rotations.rotate(
                        Rotations.getYaw(this.blockPos),
                        Rotations.getPitch(this.blockPos),
                        () -> this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.STOP_DESTROY_BLOCK, this.blockPos, this.direction))
                    );
                } else if ((
                    this.listmode.get() == listModes.whitelist
                        && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos).getBlock())
                        || this.listmode.get() == listModes.blacklist
                        && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos).getBlock())
                )
                    && !this.rotate.get()
                    && BlockUtils.canBreak(this.blockPos)) {
                    this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.STOP_DESTROY_BLOCK, this.blockPos, this.direction));
                }

                if ((
                    this.listmode.get() == listModes.whitelist
                        && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos1).getBlock())
                        || this.listmode.get() == listModes.blacklist
                        && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos1).getBlock())
                )
                    && (
                    this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                        && BlockUtils.canBreak(this.blockPos1)
                        || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos1))
                )) {
                    this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.START_DESTROY_BLOCK, this.blockPos1, this.direction));
                }

                if ((
                    this.listmode.get() == listModes.whitelist
                        && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos1).getBlock())
                        || this.listmode.get() == listModes.blacklist
                        && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos1).getBlock())
                )
                    && (
                    this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                        && BlockUtils.canBreak(this.blockPos1)
                        || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos1))
                )) {
                    this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.STOP_DESTROY_BLOCK, this.blockPos1, this.direction));
                }

                if ((
                    this.listmode.get() == listModes.whitelist
                        && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos2).getBlock())
                        || this.listmode.get() == listModes.blacklist
                        && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos2).getBlock())
                )
                    && (
                    this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                        && BlockUtils.canBreak(this.blockPos2)
                        || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos2))
                )) {
                    this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.START_DESTROY_BLOCK, this.blockPos2, this.direction));
                }

                if ((
                    this.listmode.get() == listModes.whitelist
                        && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos2).getBlock())
                        || this.listmode.get() == listModes.blacklist
                        && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos2).getBlock())
                )
                    && (
                    this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                        && BlockUtils.canBreak(this.blockPos2)
                        || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos2))
                )) {
                    this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.STOP_DESTROY_BLOCK, this.blockPos2, this.direction));
                }

                if ((
                    this.listmode.get() == listModes.whitelist
                        && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos3).getBlock())
                        || this.listmode.get() == listModes.blacklist
                        && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos3).getBlock())
                )
                    && (
                    this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                        && BlockUtils.canBreak(this.blockPos3)
                        || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos3))
                )) {
                    this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.START_DESTROY_BLOCK, this.blockPos3, this.direction));
                }

                if ((
                    this.listmode.get() == listModes.whitelist
                        && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos3).getBlock())
                        || this.listmode.get() == listModes.blacklist
                        && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos3).getBlock())
                )
                    && (
                    this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                        && BlockUtils.canBreak(this.blockPos3)
                        || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos3))
                )) {
                    this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.STOP_DESTROY_BLOCK, this.blockPos3, this.direction));
                }

                if ((
                    this.listmode.get() == listModes.whitelist
                        && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos4).getBlock())
                        || this.listmode.get() == listModes.blacklist
                        && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos4).getBlock())
                )
                    && (
                    this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                        && BlockUtils.canBreak(this.blockPos4)
                        || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos4))
                )) {
                    this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.START_DESTROY_BLOCK, this.blockPos4, this.direction));
                }

                if ((
                    this.listmode.get() == listModes.whitelist
                        && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos4).getBlock())
                        || this.listmode.get() == listModes.blacklist
                        && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos4).getBlock())
                )
                    && (
                    this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                        && BlockUtils.canBreak(this.blockPos4)
                        || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos4))
                )) {
                    this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.STOP_DESTROY_BLOCK, this.blockPos4, this.direction));
                }

                if ((
                    this.listmode.get() == listModes.whitelist
                        && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos5).getBlock())
                        || this.listmode.get() == listModes.blacklist
                        && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos5).getBlock())
                )
                    && (
                    this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                        && BlockUtils.canBreak(this.blockPos5)
                        || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos5))
                )) {
                    this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.START_DESTROY_BLOCK, this.blockPos5, this.direction));
                }

                if ((
                    this.listmode.get() == listModes.whitelist
                        && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos5).getBlock())
                        || this.listmode.get() == listModes.blacklist
                        && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos5).getBlock())
                )
                    && (
                    this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                        && BlockUtils.canBreak(this.blockPos5)
                        || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos5))
                )) {
                    this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.STOP_DESTROY_BLOCK, this.blockPos5, this.direction));
                }

                if ((
                    this.listmode.get() == listModes.whitelist
                        && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos6).getBlock())
                        || this.listmode.get() == listModes.blacklist
                        && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos6).getBlock())
                )
                    && (
                    this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                        && BlockUtils.canBreak(this.blockPos6)
                        || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos6))
                )) {
                    this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.START_DESTROY_BLOCK, this.blockPos6, this.direction));
                }

                if ((
                    this.listmode.get() == listModes.whitelist
                        && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos6).getBlock())
                        || this.listmode.get() == listModes.blacklist
                        && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos6).getBlock())
                )
                    && (
                    this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                        && BlockUtils.canBreak(this.blockPos6)
                        || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos6))
                )) {
                    this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.STOP_DESTROY_BLOCK, this.blockPos6, this.direction));
                }

                if ((
                    this.listmode.get() == listModes.whitelist
                        && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos7).getBlock())
                        || this.listmode.get() == listModes.blacklist
                        && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos7).getBlock())
                )
                    && (
                    this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                        && BlockUtils.canBreak(this.blockPos7)
                        || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos7))
                )) {
                    this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.START_DESTROY_BLOCK, this.blockPos7, this.direction));
                }

                if ((
                    this.listmode.get() == listModes.whitelist
                        && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos7).getBlock())
                        || this.listmode.get() == listModes.blacklist
                        && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos7).getBlock())
                )
                    && (
                    this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                        && BlockUtils.canBreak(this.blockPos7)
                        || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos7))
                )) {
                    this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.STOP_DESTROY_BLOCK, this.blockPos7, this.direction));
                }

                if ((
                    this.listmode.get() == listModes.whitelist
                        && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos8).getBlock())
                        || this.listmode.get() == listModes.blacklist
                        && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos8).getBlock())
                )
                    && (
                    this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                        && BlockUtils.canBreak(this.blockPos8)
                        || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos8))
                )) {
                    this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.START_DESTROY_BLOCK, this.blockPos8, this.direction));
                }

                if ((
                    this.listmode.get() == listModes.whitelist
                        && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos8).getBlock())
                        || this.listmode.get() == listModes.blacklist
                        && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos8).getBlock())
                )
                    && (
                    this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                        && BlockUtils.canBreak(this.blockPos8)
                        || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos8))
                )) {
                    this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.STOP_DESTROY_BLOCK, this.blockPos8, this.direction));
                }

                if ((
                    this.listmode.get() == listModes.whitelist
                        && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos9).getBlock())
                        || this.listmode.get() == listModes.blacklist
                        && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos9).getBlock())
                )
                    && (
                    this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                        && BlockUtils.canBreak(this.blockPos9)
                        || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos9))
                )) {
                    this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.START_DESTROY_BLOCK, this.blockPos9, this.direction));
                }

                if ((
                    this.listmode.get() == listModes.whitelist
                        && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos9).getBlock())
                        || this.listmode.get() == listModes.blacklist
                        && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos9).getBlock())
                )
                    && (
                    this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                        && BlockUtils.canBreak(this.blockPos9)
                        || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos9))
                )) {
                    this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.STOP_DESTROY_BLOCK, this.blockPos9, this.direction));
                }

                if ((
                    this.listmode.get() == listModes.whitelist
                        && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos10).getBlock())
                        || this.listmode.get() == listModes.blacklist
                        && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos10).getBlock())
                )
                    && (
                    this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                        && BlockUtils.canBreak(this.blockPos10)
                        || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos10))
                )) {
                    this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.START_DESTROY_BLOCK, this.blockPos10, this.direction));
                }

                if ((
                    this.listmode.get() == listModes.whitelist
                        && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos10).getBlock())
                        || this.listmode.get() == listModes.blacklist
                        && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos10).getBlock())
                )
                    && (
                    this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                        && BlockUtils.canBreak(this.blockPos10)
                        || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos10))
                )) {
                    this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.STOP_DESTROY_BLOCK, this.blockPos10, this.direction));
                }

                if ((
                    this.listmode.get() == listModes.whitelist
                        && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos11).getBlock())
                        || this.listmode.get() == listModes.blacklist
                        && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos11).getBlock())
                )
                    && (
                    this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                        && BlockUtils.canBreak(this.blockPos11)
                        || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos11))
                )) {
                    this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.START_DESTROY_BLOCK, this.blockPos11, this.direction));
                }

                if ((
                    this.listmode.get() == listModes.whitelist
                        && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos11).getBlock())
                        || this.listmode.get() == listModes.blacklist
                        && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos11).getBlock())
                )
                    && (
                    this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                        && BlockUtils.canBreak(this.blockPos11)
                        || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos11))
                )) {
                    this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.STOP_DESTROY_BLOCK, this.blockPos11, this.direction));
                }

                if ((
                    this.listmode.get() == listModes.whitelist
                        && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos12).getBlock())
                        || this.listmode.get() == listModes.blacklist
                        && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos12).getBlock())
                )
                    && (
                    this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                        && BlockUtils.canBreak(this.blockPos12)
                        || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos12))
                )) {
                    this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.START_DESTROY_BLOCK, this.blockPos12, this.direction));
                }

                if ((
                    this.listmode.get() == listModes.whitelist
                        && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos12).getBlock())
                        || this.listmode.get() == listModes.blacklist
                        && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos12).getBlock())
                )
                    && (
                    this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                        && BlockUtils.canBreak(this.blockPos12)
                        || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos12))
                )) {
                    this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.STOP_DESTROY_BLOCK, this.blockPos12, this.direction));
                }

                if ((
                    this.listmode.get() == listModes.whitelist
                        && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos13).getBlock())
                        || this.listmode.get() == listModes.blacklist
                        && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos13).getBlock())
                )
                    && (
                    this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                        && BlockUtils.canBreak(this.blockPos13)
                        || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos13))
                )) {
                    this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.START_DESTROY_BLOCK, this.blockPos13, this.direction));
                }

                if ((
                    this.listmode.get() == listModes.whitelist
                        && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos13).getBlock())
                        || this.listmode.get() == listModes.blacklist
                        && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos13).getBlock())
                )
                    && (
                    this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                        && BlockUtils.canBreak(this.blockPos13)
                        || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos13))
                )) {
                    this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.STOP_DESTROY_BLOCK, this.blockPos13, this.direction));
                }

                if ((
                    this.listmode.get() == listModes.whitelist
                        && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos18).getBlock())
                        || this.listmode.get() == listModes.blacklist
                        && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos18).getBlock())
                )
                    && (
                    this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                        && BlockUtils.canBreak(this.blockPos18)
                        || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos18))
                )) {
                    this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.START_DESTROY_BLOCK, this.blockPos18, this.direction));
                }

                if ((
                    this.listmode.get() == listModes.whitelist
                        && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos18).getBlock())
                        || this.listmode.get() == listModes.blacklist
                        && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos18).getBlock())
                )
                    && (
                    this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                        && BlockUtils.canBreak(this.blockPos18)
                        || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos18))
                )) {
                    this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.STOP_DESTROY_BLOCK, this.blockPos18, this.direction));
                }

                if ((
                    this.listmode.get() == listModes.whitelist
                        && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos19).getBlock())
                        || this.listmode.get() == listModes.blacklist
                        && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos19).getBlock())
                )
                    && (
                    this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                        && BlockUtils.canBreak(this.blockPos19)
                        || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos19))
                )) {
                    this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.START_DESTROY_BLOCK, this.blockPos19, this.direction));
                }

                if ((
                    this.listmode.get() == listModes.whitelist
                        && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos19).getBlock())
                        || this.listmode.get() == listModes.blacklist
                        && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos19).getBlock())
                )
                    && (
                    this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                        && BlockUtils.canBreak(this.blockPos19)
                        || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos19))
                )) {
                    this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.STOP_DESTROY_BLOCK, this.blockPos19, this.direction));
                }

                if ((
                    this.listmode.get() == listModes.whitelist
                        && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos20).getBlock())
                        || this.listmode.get() == listModes.blacklist
                        && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos20).getBlock())
                )
                    && (
                    this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                        && BlockUtils.canBreak(this.blockPos20)
                        || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos20))
                )) {
                    this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.START_DESTROY_BLOCK, this.blockPos20, this.direction));
                }

                if ((
                    this.listmode.get() == listModes.whitelist
                        && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos20).getBlock())
                        || this.listmode.get() == listModes.blacklist
                        && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos20).getBlock())
                )
                    && (
                    this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                        && BlockUtils.canBreak(this.blockPos20)
                        || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos20))
                )) {
                    this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.STOP_DESTROY_BLOCK, this.blockPos20, this.direction));
                }

                if ((
                    this.listmode.get() == listModes.whitelist
                        && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos21).getBlock())
                        || this.listmode.get() == listModes.blacklist
                        && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos21).getBlock())
                )
                    && (
                    this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                        && BlockUtils.canBreak(this.blockPos21)
                        || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos21))
                )) {
                    this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.START_DESTROY_BLOCK, this.blockPos21, this.direction));
                }

                if ((
                    this.listmode.get() == listModes.whitelist
                        && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos21).getBlock())
                        || this.listmode.get() == listModes.blacklist
                        && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos21).getBlock())
                )
                    && (
                    this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                        && BlockUtils.canBreak(this.blockPos21)
                        || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos21))
                )) {
                    this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.STOP_DESTROY_BLOCK, this.blockPos21, this.direction));
                }

                if ((
                    this.listmode.get() == listModes.whitelist
                        && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos22).getBlock())
                        || this.listmode.get() == listModes.blacklist
                        && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos22).getBlock())
                )
                    && (
                    this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                        && BlockUtils.canBreak(this.blockPos22)
                        || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos22))
                )) {
                    this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.START_DESTROY_BLOCK, this.blockPos22, this.direction));
                }

                if ((
                    this.listmode.get() == listModes.whitelist
                        && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos22).getBlock())
                        || this.listmode.get() == listModes.blacklist
                        && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos22).getBlock())
                )
                    && (
                    this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                        && BlockUtils.canBreak(this.blockPos22)
                        || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos22))
                )) {
                    this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.STOP_DESTROY_BLOCK, this.blockPos22, this.direction));
                }

                if (this.rotate.get() && BlockUtils.canBreak(this.blockPos)) {
                    Rotations.rotate(
                        Rotations.getYaw(this.blockPos),
                        Rotations.getPitch(this.blockPos),
                        () -> this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.START_DESTROY_BLOCK, this.blockPos, this.direction))
                    );
                } else if ((
                    this.listmode.get() == listModes.whitelist
                        && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos).getBlock())
                        || this.listmode.get() == listModes.blacklist
                        && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos).getBlock())
                )
                    && !this.rotate.get()
                    && BlockUtils.canBreak(this.blockPos)) {
                    this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.START_DESTROY_BLOCK, this.blockPos, this.direction));
                }

                if ((
                    this.listmode.get() == listModes.whitelist
                        && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos).getBlock())
                        || this.listmode.get() == listModes.blacklist
                        && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos).getBlock())
                )
                    && this.swing.get()) {
                    this.mc.getNetworkHandler().sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
                }
            }

            if (this.shouldMine() && this.range.get() == 7) {
                if ((
                    this.listmode.get() == listModes.whitelist
                        && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos).getBlock())
                        || this.listmode.get() == listModes.blacklist
                        && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos).getBlock())
                )
                    && this.rotate.get()
                    && BlockUtils.canBreak(this.blockPos)) {
                    Rotations.rotate(
                        Rotations.getYaw(this.blockPos),
                        Rotations.getPitch(this.blockPos),
                        () -> this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.STOP_DESTROY_BLOCK, this.blockPos, this.direction))
                    );
                } else if ((
                    this.listmode.get() == listModes.whitelist
                        && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos).getBlock())
                        || this.listmode.get() == listModes.blacklist
                        && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos).getBlock())
                )
                    && !this.rotate.get()
                    && BlockUtils.canBreak(this.blockPos)) {
                    this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.STOP_DESTROY_BLOCK, this.blockPos, this.direction));
                }

                if ((
                    this.listmode.get() == listModes.whitelist
                        && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos1).getBlock())
                        || this.listmode.get() == listModes.blacklist
                        && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos1).getBlock())
                )
                    && (
                    this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                        && BlockUtils.canBreak(this.blockPos1)
                        || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos1))
                )) {
                    this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.START_DESTROY_BLOCK, this.blockPos1, this.direction));
                }

                if ((
                    this.listmode.get() == listModes.whitelist
                        && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos1).getBlock())
                        || this.listmode.get() == listModes.blacklist
                        && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos1).getBlock())
                )
                    && (
                    this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                        && BlockUtils.canBreak(this.blockPos1)
                        || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos1))
                )) {
                    this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.STOP_DESTROY_BLOCK, this.blockPos1, this.direction));
                }

                if ((
                    this.listmode.get() == listModes.whitelist
                        && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos2).getBlock())
                        || this.listmode.get() == listModes.blacklist
                        && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos2).getBlock())
                )
                    && (
                    this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                        && BlockUtils.canBreak(this.blockPos2)
                        || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos2))
                )) {
                    this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.START_DESTROY_BLOCK, this.blockPos2, this.direction));
                }

                if ((
                    this.listmode.get() == listModes.whitelist
                        && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos2).getBlock())
                        || this.listmode.get() == listModes.blacklist
                        && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos2).getBlock())
                )
                    && (
                    this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                        && BlockUtils.canBreak(this.blockPos2)
                        || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos2))
                )) {
                    this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.STOP_DESTROY_BLOCK, this.blockPos2, this.direction));
                }

                if ((
                    this.listmode.get() == listModes.whitelist
                        && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos3).getBlock())
                        || this.listmode.get() == listModes.blacklist
                        && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos3).getBlock())
                )
                    && (
                    this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                        && BlockUtils.canBreak(this.blockPos3)
                        || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos3))
                )) {
                    this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.START_DESTROY_BLOCK, this.blockPos3, this.direction));
                }

                if ((
                    this.listmode.get() == listModes.whitelist
                        && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos3).getBlock())
                        || this.listmode.get() == listModes.blacklist
                        && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos3).getBlock())
                )
                    && (
                    this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                        && BlockUtils.canBreak(this.blockPos3)
                        || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos3))
                )) {
                    this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.STOP_DESTROY_BLOCK, this.blockPos3, this.direction));
                }

                if ((
                    this.listmode.get() == listModes.whitelist
                        && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos4).getBlock())
                        || this.listmode.get() == listModes.blacklist
                        && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos4).getBlock())
                )
                    && (
                    this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                        && BlockUtils.canBreak(this.blockPos4)
                        || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos4))
                )) {
                    this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.START_DESTROY_BLOCK, this.blockPos4, this.direction));
                }

                if ((
                    this.listmode.get() == listModes.whitelist
                        && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos4).getBlock())
                        || this.listmode.get() == listModes.blacklist
                        && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos4).getBlock())
                )
                    && (
                    this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                        && BlockUtils.canBreak(this.blockPos4)
                        || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos4))
                )) {
                    this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.STOP_DESTROY_BLOCK, this.blockPos4, this.direction));
                }

                if ((
                    this.listmode.get() == listModes.whitelist
                        && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos5).getBlock())
                        || this.listmode.get() == listModes.blacklist
                        && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos5).getBlock())
                )
                    && (
                    this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                        && BlockUtils.canBreak(this.blockPos5)
                        || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos5))
                )) {
                    this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.START_DESTROY_BLOCK, this.blockPos5, this.direction));
                }

                if ((
                    this.listmode.get() == listModes.whitelist
                        && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos5).getBlock())
                        || this.listmode.get() == listModes.blacklist
                        && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos5).getBlock())
                )
                    && (
                    this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                        && BlockUtils.canBreak(this.blockPos5)
                        || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos5))
                )) {
                    this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.STOP_DESTROY_BLOCK, this.blockPos5, this.direction));
                }

                if ((
                    this.listmode.get() == listModes.whitelist
                        && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos6).getBlock())
                        || this.listmode.get() == listModes.blacklist
                        && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos6).getBlock())
                )
                    && (
                    this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                        && BlockUtils.canBreak(this.blockPos6)
                        || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos6))
                )) {
                    this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.START_DESTROY_BLOCK, this.blockPos6, this.direction));
                }

                if ((
                    this.listmode.get() == listModes.whitelist
                        && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos6).getBlock())
                        || this.listmode.get() == listModes.blacklist
                        && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos6).getBlock())
                )
                    && (
                    this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                        && BlockUtils.canBreak(this.blockPos6)
                        || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos6))
                )) {
                    this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.STOP_DESTROY_BLOCK, this.blockPos6, this.direction));
                }

                if ((
                    this.listmode.get() == listModes.whitelist
                        && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos7).getBlock())
                        || this.listmode.get() == listModes.blacklist
                        && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos7).getBlock())
                )
                    && (
                    this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                        && BlockUtils.canBreak(this.blockPos7)
                        || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos7))
                )) {
                    this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.START_DESTROY_BLOCK, this.blockPos7, this.direction));
                }

                if ((
                    this.listmode.get() == listModes.whitelist
                        && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos7).getBlock())
                        || this.listmode.get() == listModes.blacklist
                        && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos7).getBlock())
                )
                    && (
                    this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                        && BlockUtils.canBreak(this.blockPos7)
                        || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos7))
                )) {
                    this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.STOP_DESTROY_BLOCK, this.blockPos7, this.direction));
                }

                if ((
                    this.listmode.get() == listModes.whitelist
                        && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos8).getBlock())
                        || this.listmode.get() == listModes.blacklist
                        && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos8).getBlock())
                )
                    && (
                    this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                        && BlockUtils.canBreak(this.blockPos8)
                        || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos8))
                )) {
                    this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.START_DESTROY_BLOCK, this.blockPos8, this.direction));
                }

                if ((
                    this.listmode.get() == listModes.whitelist
                        && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos8).getBlock())
                        || this.listmode.get() == listModes.blacklist
                        && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos8).getBlock())
                )
                    && (
                    this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                        && BlockUtils.canBreak(this.blockPos8)
                        || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos8))
                )) {
                    this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.STOP_DESTROY_BLOCK, this.blockPos8, this.direction));
                }

                if ((
                    this.listmode.get() == listModes.whitelist
                        && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos9).getBlock())
                        || this.listmode.get() == listModes.blacklist
                        && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos9).getBlock())
                )
                    && (
                    this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                        && BlockUtils.canBreak(this.blockPos9)
                        || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos9))
                )) {
                    this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.START_DESTROY_BLOCK, this.blockPos9, this.direction));
                }

                if ((
                    this.listmode.get() == listModes.whitelist
                        && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos9).getBlock())
                        || this.listmode.get() == listModes.blacklist
                        && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos9).getBlock())
                )
                    && (
                    this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                        && BlockUtils.canBreak(this.blockPos9)
                        || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos9))
                )) {
                    this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.STOP_DESTROY_BLOCK, this.blockPos9, this.direction));
                }

                if ((
                    this.listmode.get() == listModes.whitelist
                        && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos10).getBlock())
                        || this.listmode.get() == listModes.blacklist
                        && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos10).getBlock())
                )
                    && (
                    this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                        && BlockUtils.canBreak(this.blockPos10)
                        || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos10))
                )) {
                    this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.START_DESTROY_BLOCK, this.blockPos10, this.direction));
                }

                if ((
                    this.listmode.get() == listModes.whitelist
                        && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos10).getBlock())
                        || this.listmode.get() == listModes.blacklist
                        && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos10).getBlock())
                )
                    && (
                    this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                        && BlockUtils.canBreak(this.blockPos10)
                        || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos10))
                )) {
                    this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.STOP_DESTROY_BLOCK, this.blockPos10, this.direction));
                }

                if ((
                    this.listmode.get() == listModes.whitelist
                        && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos11).getBlock())
                        || this.listmode.get() == listModes.blacklist
                        && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos11).getBlock())
                )
                    && (
                    this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                        && BlockUtils.canBreak(this.blockPos11)
                        || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos11))
                )) {
                    this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.START_DESTROY_BLOCK, this.blockPos11, this.direction));
                }

                if ((
                    this.listmode.get() == listModes.whitelist
                        && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos11).getBlock())
                        || this.listmode.get() == listModes.blacklist
                        && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos11).getBlock())
                )
                    && (
                    this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                        && BlockUtils.canBreak(this.blockPos11)
                        || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos11))
                )) {
                    this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.STOP_DESTROY_BLOCK, this.blockPos11, this.direction));
                }

                if ((
                    this.listmode.get() == listModes.whitelist
                        && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos12).getBlock())
                        || this.listmode.get() == listModes.blacklist
                        && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos12).getBlock())
                )
                    && (
                    this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                        && BlockUtils.canBreak(this.blockPos12)
                        || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos12))
                )) {
                    this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.START_DESTROY_BLOCK, this.blockPos12, this.direction));
                }

                if ((
                    this.listmode.get() == listModes.whitelist
                        && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos12).getBlock())
                        || this.listmode.get() == listModes.blacklist
                        && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos12).getBlock())
                )
                    && (
                    this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                        && BlockUtils.canBreak(this.blockPos12)
                        || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos12))
                )) {
                    this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.STOP_DESTROY_BLOCK, this.blockPos12, this.direction));
                }

                if ((
                    this.listmode.get() == listModes.whitelist
                        && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos13).getBlock())
                        || this.listmode.get() == listModes.blacklist
                        && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos13).getBlock())
                )
                    && (
                    this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                        && BlockUtils.canBreak(this.blockPos13)
                        || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos13))
                )) {
                    this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.START_DESTROY_BLOCK, this.blockPos13, this.direction));
                }

                if ((
                    this.listmode.get() == listModes.whitelist
                        && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos13).getBlock())
                        || this.listmode.get() == listModes.blacklist
                        && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos13).getBlock())
                )
                    && (
                    this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                        && BlockUtils.canBreak(this.blockPos13)
                        || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos13))
                )) {
                    this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.STOP_DESTROY_BLOCK, this.blockPos13, this.direction));
                }

                if ((
                    this.listmode.get() == listModes.whitelist
                        && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos14).getBlock())
                        || this.listmode.get() == listModes.blacklist
                        && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos14).getBlock())
                )
                    && (
                    this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                        && BlockUtils.canBreak(this.blockPos14)
                        || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos14))
                )) {
                    this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.START_DESTROY_BLOCK, this.blockPos14, this.direction));
                }

                if ((
                    this.listmode.get() == listModes.whitelist
                        && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos14).getBlock())
                        || this.listmode.get() == listModes.blacklist
                        && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos14).getBlock())
                )
                    && (
                    this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                        && BlockUtils.canBreak(this.blockPos14)
                        || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos14))
                )) {
                    this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.STOP_DESTROY_BLOCK, this.blockPos14, this.direction));
                }

                if ((
                    this.listmode.get() == listModes.whitelist
                        && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos15).getBlock())
                        || this.listmode.get() == listModes.blacklist
                        && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos15).getBlock())
                )
                    && (
                    this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                        && BlockUtils.canBreak(this.blockPos15)
                        || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos15))
                )) {
                    this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.START_DESTROY_BLOCK, this.blockPos15, this.direction));
                }

                if ((
                    this.listmode.get() == listModes.whitelist
                        && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos15).getBlock())
                        || this.listmode.get() == listModes.blacklist
                        && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos15).getBlock())
                )
                    && (
                    this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                        && BlockUtils.canBreak(this.blockPos15)
                        || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos15))
                )) {
                    this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.STOP_DESTROY_BLOCK, this.blockPos15, this.direction));
                }

                if ((
                    this.listmode.get() == listModes.whitelist
                        && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos16).getBlock())
                        || this.listmode.get() == listModes.blacklist
                        && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos16).getBlock())
                )
                    && (
                    this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                        && BlockUtils.canBreak(this.blockPos16)
                        || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos16))
                )) {
                    this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.START_DESTROY_BLOCK, this.blockPos16, this.direction));
                }

                if ((
                    this.listmode.get() == listModes.whitelist
                        && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos16).getBlock())
                        || this.listmode.get() == listModes.blacklist
                        && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos16).getBlock())
                )
                    && (
                    this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                        && BlockUtils.canBreak(this.blockPos16)
                        || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos16))
                )) {
                    this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.STOP_DESTROY_BLOCK, this.blockPos16, this.direction));
                }

                if ((
                    this.listmode.get() == listModes.whitelist
                        && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos17).getBlock())
                        || this.listmode.get() == listModes.blacklist
                        && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos17).getBlock())
                )
                    && (
                    this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                        && BlockUtils.canBreak(this.blockPos17)
                        || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos17))
                )) {
                    this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.START_DESTROY_BLOCK, this.blockPos17, this.direction));
                }

                if ((
                    this.listmode.get() == listModes.whitelist
                        && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos17).getBlock())
                        || this.listmode.get() == listModes.blacklist
                        && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos17).getBlock())
                )
                    && (
                    this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                        && BlockUtils.canBreak(this.blockPos17)
                        || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos17))
                )) {
                    this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.STOP_DESTROY_BLOCK, this.blockPos17, this.direction));
                }

                if ((
                    this.listmode.get() == listModes.whitelist
                        && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos18).getBlock())
                        || this.listmode.get() == listModes.blacklist
                        && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos18).getBlock())
                )
                    && (
                    this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                        && BlockUtils.canBreak(this.blockPos18)
                        || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos18))
                )) {
                    this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.START_DESTROY_BLOCK, this.blockPos18, this.direction));
                }

                if ((
                    this.listmode.get() == listModes.whitelist
                        && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos18).getBlock())
                        || this.listmode.get() == listModes.blacklist
                        && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos18).getBlock())
                )
                    && (
                    this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                        && BlockUtils.canBreak(this.blockPos18)
                        || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos18))
                )) {
                    this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.STOP_DESTROY_BLOCK, this.blockPos18, this.direction));
                }

                if ((
                    this.listmode.get() == listModes.whitelist
                        && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos19).getBlock())
                        || this.listmode.get() == listModes.blacklist
                        && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos19).getBlock())
                )
                    && (
                    this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                        && BlockUtils.canBreak(this.blockPos19)
                        || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos19))
                )) {
                    this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.START_DESTROY_BLOCK, this.blockPos19, this.direction));
                }

                if ((
                    this.listmode.get() == listModes.whitelist
                        && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos19).getBlock())
                        || this.listmode.get() == listModes.blacklist
                        && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos19).getBlock())
                )
                    && (
                    this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                        && BlockUtils.canBreak(this.blockPos19)
                        || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos19))
                )) {
                    this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.STOP_DESTROY_BLOCK, this.blockPos19, this.direction));
                }

                if ((
                    this.listmode.get() == listModes.whitelist
                        && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos20).getBlock())
                        || this.listmode.get() == listModes.blacklist
                        && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos20).getBlock())
                )
                    && (
                    this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                        && BlockUtils.canBreak(this.blockPos20)
                        || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos20))
                )) {
                    this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.START_DESTROY_BLOCK, this.blockPos20, this.direction));
                }

                if ((
                    this.listmode.get() == listModes.whitelist
                        && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos20).getBlock())
                        || this.listmode.get() == listModes.blacklist
                        && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos20).getBlock())
                )
                    && (
                    this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                        && BlockUtils.canBreak(this.blockPos20)
                        || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos20))
                )) {
                    this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.STOP_DESTROY_BLOCK, this.blockPos20, this.direction));
                }

                if ((
                    this.listmode.get() == listModes.whitelist
                        && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos21).getBlock())
                        || this.listmode.get() == listModes.blacklist
                        && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos21).getBlock())
                )
                    && (
                    this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                        && BlockUtils.canBreak(this.blockPos21)
                        || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos21))
                )) {
                    this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.START_DESTROY_BLOCK, this.blockPos21, this.direction));
                }

                if ((
                    this.listmode.get() == listModes.whitelist
                        && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos21).getBlock())
                        || this.listmode.get() == listModes.blacklist
                        && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos21).getBlock())
                )
                    && (
                    this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                        && BlockUtils.canBreak(this.blockPos21)
                        || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos21))
                )) {
                    this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.STOP_DESTROY_BLOCK, this.blockPos21, this.direction));
                }

                if ((
                    this.listmode.get() == listModes.whitelist
                        && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos22).getBlock())
                        || this.listmode.get() == listModes.blacklist
                        && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos22).getBlock())
                )
                    && (
                    this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                        && BlockUtils.canBreak(this.blockPos22)
                        || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos22))
                )) {
                    this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.START_DESTROY_BLOCK, this.blockPos22, this.direction));
                }

                if ((
                    this.listmode.get() == listModes.whitelist
                        && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos22).getBlock())
                        || this.listmode.get() == listModes.blacklist
                        && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos22).getBlock())
                )
                    && (
                    this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                        && BlockUtils.canBreak(this.blockPos22)
                        || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos22))
                )) {
                    this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.STOP_DESTROY_BLOCK, this.blockPos22, this.direction));
                }

                if ((
                    this.listmode.get() == listModes.whitelist
                        && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos23).getBlock())
                        || this.listmode.get() == listModes.blacklist
                        && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos23).getBlock())
                )
                    && (
                    this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                        && BlockUtils.canBreak(this.blockPos23)
                        || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos23))
                )) {
                    this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.START_DESTROY_BLOCK, this.blockPos23, this.direction));
                }

                if ((
                    this.listmode.get() == listModes.whitelist
                        && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos23).getBlock())
                        || this.listmode.get() == listModes.blacklist
                        && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos23).getBlock())
                )
                    && (
                    this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                        && BlockUtils.canBreak(this.blockPos23)
                        || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos23))
                )) {
                    this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.STOP_DESTROY_BLOCK, this.blockPos23, this.direction));
                }

                if ((
                    this.listmode.get() == listModes.whitelist
                        && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos24).getBlock())
                        || this.listmode.get() == listModes.blacklist
                        && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos24).getBlock())
                )
                    && (
                    this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                        && BlockUtils.canBreak(this.blockPos24)
                        || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos24))
                )) {
                    this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.START_DESTROY_BLOCK, this.blockPos24, this.direction));
                }

                if ((
                    this.listmode.get() == listModes.whitelist
                        && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos24).getBlock())
                        || this.listmode.get() == listModes.blacklist
                        && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos24).getBlock())
                )
                    && (
                    this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                        && BlockUtils.canBreak(this.blockPos24)
                        || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos24))
                )) {
                    this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.STOP_DESTROY_BLOCK, this.blockPos24, this.direction));
                }

                if ((
                    this.listmode.get() == listModes.whitelist
                        && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos25).getBlock())
                        || this.listmode.get() == listModes.blacklist
                        && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos25).getBlock())
                )
                    && (
                    this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                        && BlockUtils.canBreak(this.blockPos25)
                        || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos25))
                )) {
                    this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.START_DESTROY_BLOCK, this.blockPos25, this.direction));
                }

                if ((
                    this.listmode.get() == listModes.whitelist
                        && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos25).getBlock())
                        || this.listmode.get() == listModes.blacklist
                        && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos25).getBlock())
                )
                    && (
                    this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                        && BlockUtils.canBreak(this.blockPos25)
                        || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos25))
                )) {
                    this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.STOP_DESTROY_BLOCK, this.blockPos25, this.direction));
                }

                if ((
                    this.listmode.get() == listModes.whitelist
                        && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos26).getBlock())
                        || this.listmode.get() == listModes.blacklist
                        && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos26).getBlock())
                )
                    && (
                    this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                        && BlockUtils.canBreak(this.blockPos26)
                        || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos26))
                )) {
                    this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.START_DESTROY_BLOCK, this.blockPos26, this.direction));
                }

                if ((
                    this.listmode.get() == listModes.whitelist
                        && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos26).getBlock())
                        || this.listmode.get() == listModes.blacklist
                        && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos26).getBlock())
                )
                    && (
                    this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                        && BlockUtils.canBreak(this.blockPos26)
                        || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos26))
                )) {
                    this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.STOP_DESTROY_BLOCK, this.blockPos26, this.direction));
                }

                if (this.rotate.get() && BlockUtils.canBreak(this.blockPos)) {
                    Rotations.rotate(
                        Rotations.getYaw(this.blockPos),
                        Rotations.getPitch(this.blockPos),
                        () -> this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.START_DESTROY_BLOCK, this.blockPos, this.direction))
                    );
                } else if ((
                    this.listmode.get() == listModes.whitelist
                        && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos).getBlock())
                        || this.listmode.get() == listModes.blacklist
                        && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos).getBlock())
                )
                    && !this.rotate.get()
                    && BlockUtils.canBreak(this.blockPos)) {
                    this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.START_DESTROY_BLOCK, this.blockPos, this.direction));
                }

                if ((
                    this.listmode.get() == listModes.whitelist
                        && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos).getBlock())
                        || this.listmode.get() == listModes.blacklist
                        && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos).getBlock())
                )
                    && this.swing.get()) {
                    this.mc.getNetworkHandler().sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
                }
            }
        }
    }

    private boolean shouldMine() {
        return this.blockPos.getY() == -128
            ? false
            : !this.pick.get()
            || this.mc.player.getMainHandStack().getItem() == Items.DIAMOND_PICKAXE
            || this.mc.player.getMainHandStack().getItem() == Items.NETHERITE_PICKAXE;
    }

    @EventHandler
    private void onRender(Render3DEvent event) {
        if (this.render.get() && this.shouldMine()) {
            if ((
                this.listmode.get() == listModes.whitelist
                    && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos).getBlock())
                    || this.listmode.get() == listModes.blacklist
                    && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos).getBlock())
            )
                && BlockUtils.canBreak(this.blockPos)) {
                event.renderer.box(this.blockPos, (Color) this.sideColor.get(), (Color) this.lineColor.get(), (ShapeMode) this.shapeMode.get(), 0);
            }

            if ((
                this.listmode.get() == listModes.whitelist
                    && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos1).getBlock())
                    || this.listmode.get() == listModes.blacklist
                    && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos1).getBlock())
            )
                && (
                this.range.get() == -1 && this.playermovingdirection == Direction.SOUTH
                    || this.range.get() == 1 && this.playermovingdirection == Direction.NORTH
                    || this.range.get() == 2 && this.playermovingdirection == Direction.NORTH | this.playermovingdirection == Direction.SOUTH
            )
                && (
                this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                    && BlockUtils.canBreak(this.blockPos1)
                    || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos1))
            )) {
                event.renderer.box(this.blockPos1, (Color) this.sideColor.get(), (Color) this.lineColor.get(), (ShapeMode) this.shapeMode.get(), 0);
            }

            if ((
                this.listmode.get() == listModes.whitelist
                    && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos2).getBlock())
                    || this.listmode.get() == listModes.blacklist
                    && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos2).getBlock())
            )
                && (
                this.range.get() == -1 && this.playermovingdirection == Direction.NORTH
                    || this.range.get() == 1 && this.playermovingdirection == Direction.SOUTH
                    || this.range.get() == 2 && this.playermovingdirection == Direction.NORTH | this.playermovingdirection == Direction.SOUTH
            )
                && (
                this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                    && BlockUtils.canBreak(this.blockPos2)
                    || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos2))
            )) {
                event.renderer.box(this.blockPos2, (Color) this.sideColor.get(), (Color) this.lineColor.get(), (ShapeMode) this.shapeMode.get(), 0);
            }

            if ((
                this.listmode.get() == listModes.whitelist
                    && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos3).getBlock())
                    || this.listmode.get() == listModes.blacklist
                    && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos3).getBlock())
            )
                && (
                this.range.get() == -1 && this.playermovingdirection == Direction.WEST
                    || this.range.get() == 1 && this.playermovingdirection == Direction.EAST
                    || this.range.get() == 2 && this.playermovingdirection == Direction.EAST | this.playermovingdirection == Direction.WEST
            )
                && (
                this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                    && BlockUtils.canBreak(this.blockPos3)
                    || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos3))
            )) {
                event.renderer.box(this.blockPos3, (Color) this.sideColor.get(), (Color) this.lineColor.get(), (ShapeMode) this.shapeMode.get(), 0);
            }

            if ((
                this.listmode.get() == listModes.whitelist
                    && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos4).getBlock())
                    || this.listmode.get() == listModes.blacklist
                    && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos4).getBlock())
            )
                && (
                this.range.get() == -1 && this.playermovingdirection == Direction.EAST
                    || this.range.get() == 1 && this.playermovingdirection == Direction.WEST
                    || this.range.get() == 2 && this.playermovingdirection == Direction.EAST | this.playermovingdirection == Direction.WEST
            )
                && (
                this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                    && BlockUtils.canBreak(this.blockPos4)
                    || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos4))
            )) {
                event.renderer.box(this.blockPos4, (Color) this.sideColor.get(), (Color) this.lineColor.get(), (ShapeMode) this.shapeMode.get(), 0);
            }

            if (this.range.get() == 3) {
                if (this.aorient.get() && this.playerpitch <= 30 && this.playerpitch >= -30
                    || this.mode.get() == Modes.Vertical && !this.aorient.get()) {
                    if (this.playermovingdirection == Direction.NORTH || this.playermovingdirection == Direction.SOUTH) {
                        if ((
                            this.listmode.get() == listModes.whitelist
                                && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos1).getBlock())
                                || this.listmode.get() == listModes.blacklist
                                && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos1).getBlock())
                        )
                            && (
                            this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                                && BlockUtils.canBreak(this.blockPos1)
                                || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos1))
                        )) {
                            event.renderer.box(this.blockPos1, (Color) this.sideColor.get(), (Color) this.lineColor.get(), (ShapeMode) this.shapeMode.get(), 0);
                        }

                        if ((
                            this.listmode.get() == listModes.whitelist
                                && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos2).getBlock())
                                || this.listmode.get() == listModes.blacklist
                                && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos2).getBlock())
                        )
                            && (
                            this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                                && BlockUtils.canBreak(this.blockPos2)
                                || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos2))
                        )) {
                            event.renderer.box(this.blockPos2, (Color) this.sideColor.get(), (Color) this.lineColor.get(), (ShapeMode) this.shapeMode.get(), 0);
                        }

                        if ((
                            this.listmode.get() == listModes.whitelist
                                && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos9).getBlock())
                                || this.listmode.get() == listModes.blacklist
                                && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos9).getBlock())
                        )
                            && (
                            this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                                && BlockUtils.canBreak(this.blockPos9)
                                || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos9))
                        )) {
                            event.renderer.box(this.blockPos9, (Color) this.sideColor.get(), (Color) this.lineColor.get(), (ShapeMode) this.shapeMode.get(), 0);
                        }

                        if ((
                            this.listmode.get() == listModes.whitelist
                                && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos10).getBlock())
                                || this.listmode.get() == listModes.blacklist
                                && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos10).getBlock())
                        )
                            && (
                            this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                                && BlockUtils.canBreak(this.blockPos10)
                                || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos18))
                        )) {
                            event.renderer.box(this.blockPos18, (Color) this.sideColor.get(), (Color) this.lineColor.get(), (ShapeMode) this.shapeMode.get(), 0);
                        }
                    }

                    if (this.playermovingdirection == Direction.EAST || this.playermovingdirection == Direction.WEST) {
                        if ((
                            this.listmode.get() == listModes.whitelist
                                && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos9).getBlock())
                                || this.listmode.get() == listModes.blacklist
                                && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos9).getBlock())
                        )
                            && (
                            this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                                && BlockUtils.canBreak(this.blockPos9)
                                || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos9))
                        )) {
                            event.renderer.box(this.blockPos9, (Color) this.sideColor.get(), (Color) this.lineColor.get(), (ShapeMode) this.shapeMode.get(), 0);
                        }

                        if ((
                            this.listmode.get() == listModes.whitelist
                                && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos18).getBlock())
                                || this.listmode.get() == listModes.blacklist
                                && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos18).getBlock())
                        )
                            && (
                            this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                                && BlockUtils.canBreak(this.blockPos18)
                                || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos18))
                        )) {
                            event.renderer.box(this.blockPos18, (Color) this.sideColor.get(), (Color) this.lineColor.get(), (ShapeMode) this.shapeMode.get(), 0);
                        }

                        if ((
                            this.listmode.get() == listModes.whitelist
                                && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos3).getBlock())
                                || this.listmode.get() == listModes.blacklist
                                && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos3).getBlock())
                        )
                            && (
                            this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                                && BlockUtils.canBreak(this.blockPos3)
                                || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos3))
                        )) {
                            event.renderer.box(this.blockPos3, (Color) this.sideColor.get(), (Color) this.lineColor.get(), (ShapeMode) this.shapeMode.get(), 0);
                        }

                        if ((
                            this.listmode.get() == listModes.whitelist
                                && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos4).getBlock())
                                || this.listmode.get() == listModes.blacklist
                                && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos4).getBlock())
                        )
                            && (
                            this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                                && BlockUtils.canBreak(this.blockPos4)
                                || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos4))
                        )) {
                            event.renderer.box(this.blockPos4, (Color) this.sideColor.get(), (Color) this.lineColor.get(), (ShapeMode) this.shapeMode.get(), 0);
                        }
                    }
                }

                if (this.aorient.get() && this.playerpitch > 30 | this.playerpitch < -30
                    || this.mode.get() == Modes.Horizontal && !this.aorient.get()) {
                    if ((
                        this.listmode.get() == listModes.whitelist
                            && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos1).getBlock())
                            || this.listmode.get() == listModes.blacklist
                            && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos1).getBlock())
                    )
                        && (
                        this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                            && BlockUtils.canBreak(this.blockPos1)
                            || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos1))
                    )) {
                        event.renderer.box(this.blockPos1, (Color) this.sideColor.get(), (Color) this.lineColor.get(), (ShapeMode) this.shapeMode.get(), 0);
                    }

                    if ((
                        this.listmode.get() == listModes.whitelist
                            && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos2).getBlock())
                            || this.listmode.get() == listModes.blacklist
                            && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos2).getBlock())
                    )
                        && (
                        this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                            && BlockUtils.canBreak(this.blockPos2)
                            || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos2))
                    )) {
                        event.renderer.box(this.blockPos2, (Color) this.sideColor.get(), (Color) this.lineColor.get(), (ShapeMode) this.shapeMode.get(), 0);
                    }

                    if ((
                        this.listmode.get() == listModes.whitelist
                            && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos3).getBlock())
                            || this.listmode.get() == listModes.blacklist
                            && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos3).getBlock())
                    )
                        && (
                        this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                            && BlockUtils.canBreak(this.blockPos3)
                            || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos3))
                    )) {
                        event.renderer.box(this.blockPos3, (Color) this.sideColor.get(), (Color) this.lineColor.get(), (ShapeMode) this.shapeMode.get(), 0);
                    }

                    if ((
                        this.listmode.get() == listModes.whitelist
                            && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos4).getBlock())
                            || this.listmode.get() == listModes.blacklist
                            && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos4).getBlock())
                    )
                        && (
                        this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                            && BlockUtils.canBreak(this.blockPos4)
                            || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos4))
                    )) {
                        event.renderer.box(this.blockPos4, (Color) this.sideColor.get(), (Color) this.lineColor.get(), (ShapeMode) this.shapeMode.get(), 0);
                    }
                }
            }

            if (this.range.get() == 4) {
                if (this.aorient.get() && this.playerpitch <= 30 && this.playerpitch >= -30
                    || this.mode.get() == Modes.Vertical && !this.aorient.get()) {
                    if (this.playermovingdirection == Direction.NORTH || this.playermovingdirection == Direction.SOUTH) {
                        if ((
                            this.listmode.get() == listModes.whitelist
                                && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos1).getBlock())
                                || this.listmode.get() == listModes.blacklist
                                && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos1).getBlock())
                        )
                            && (
                            this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                                && BlockUtils.canBreak(this.blockPos1)
                                || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos1))
                        )) {
                            event.renderer.box(this.blockPos1, (Color) this.sideColor.get(), (Color) this.lineColor.get(), (ShapeMode) this.shapeMode.get(), 0);
                        }

                        if ((
                            this.listmode.get() == listModes.whitelist
                                && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos2).getBlock())
                                || this.listmode.get() == listModes.blacklist
                                && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos2).getBlock())
                        )
                            && (
                            this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                                && BlockUtils.canBreak(this.blockPos2)
                                || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos2))
                        )) {
                            event.renderer.box(this.blockPos2, (Color) this.sideColor.get(), (Color) this.lineColor.get(), (ShapeMode) this.shapeMode.get(), 0);
                        }

                        if ((
                            this.listmode.get() == listModes.whitelist
                                && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos9).getBlock())
                                || this.listmode.get() == listModes.blacklist
                                && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos9).getBlock())
                        )
                            && (
                            this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                                && BlockUtils.canBreak(this.blockPos9)
                                || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos9))
                        )) {
                            event.renderer.box(this.blockPos9, (Color) this.sideColor.get(), (Color) this.lineColor.get(), (ShapeMode) this.shapeMode.get(), 0);
                        }

                        if ((
                            this.listmode.get() == listModes.whitelist
                                && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos18).getBlock())
                                || this.listmode.get() == listModes.blacklist
                                && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos18).getBlock())
                        )
                            && (
                            this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                                && BlockUtils.canBreak(this.blockPos18)
                                || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos18))
                        )) {
                            event.renderer.box(this.blockPos18, (Color) this.sideColor.get(), (Color) this.lineColor.get(), (ShapeMode) this.shapeMode.get(), 0);
                        }

                        if ((
                            this.listmode.get() == listModes.whitelist
                                && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos10).getBlock())
                                || this.listmode.get() == listModes.blacklist
                                && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos10).getBlock())
                        )
                            && (
                            this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                                && BlockUtils.canBreak(this.blockPos10)
                                || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos10))
                        )) {
                            event.renderer.box(this.blockPos10, (Color) this.sideColor.get(), (Color) this.lineColor.get(), (ShapeMode) this.shapeMode.get(), 0);
                        }

                        if ((
                            this.listmode.get() == listModes.whitelist
                                && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos11).getBlock())
                                || this.listmode.get() == listModes.blacklist
                                && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos11).getBlock())
                        )
                            && (
                            this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                                && BlockUtils.canBreak(this.blockPos11)
                                || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos11))
                        )) {
                            event.renderer.box(this.blockPos11, (Color) this.sideColor.get(), (Color) this.lineColor.get(), (ShapeMode) this.shapeMode.get(), 0);
                        }

                        if ((
                            this.listmode.get() == listModes.whitelist
                                && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos19).getBlock())
                                || this.listmode.get() == listModes.blacklist
                                && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos19).getBlock())
                        )
                            && (
                            this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                                && BlockUtils.canBreak(this.blockPos19)
                                || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos19))
                        )) {
                            event.renderer.box(this.blockPos19, (Color) this.sideColor.get(), (Color) this.lineColor.get(), (ShapeMode) this.shapeMode.get(), 0);
                        }

                        if ((
                            this.listmode.get() == listModes.whitelist
                                && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos20).getBlock())
                                || this.listmode.get() == listModes.blacklist
                                && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos20).getBlock())
                        )
                            && (
                            this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                                && BlockUtils.canBreak(this.blockPos20)
                                || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos20))
                        )) {
                            event.renderer.box(this.blockPos20, (Color) this.sideColor.get(), (Color) this.lineColor.get(), (ShapeMode) this.shapeMode.get(), 0);
                        }
                    }

                    if (this.playermovingdirection == Direction.EAST || this.playermovingdirection == Direction.WEST) {
                        if ((
                            this.listmode.get() == listModes.whitelist
                                && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos9).getBlock())
                                || this.listmode.get() == listModes.blacklist
                                && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos9).getBlock())
                        )
                            && (
                            this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                                && BlockUtils.canBreak(this.blockPos9)
                                || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos9))
                        )) {
                            event.renderer.box(this.blockPos9, (Color) this.sideColor.get(), (Color) this.lineColor.get(), (ShapeMode) this.shapeMode.get(), 0);
                        }

                        if ((
                            this.listmode.get() == listModes.whitelist
                                && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos18).getBlock())
                                || this.listmode.get() == listModes.blacklist
                                && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos18).getBlock())
                        )
                            && (
                            this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                                && BlockUtils.canBreak(this.blockPos18)
                                || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos18))
                        )) {
                            event.renderer.box(this.blockPos18, (Color) this.sideColor.get(), (Color) this.lineColor.get(), (ShapeMode) this.shapeMode.get(), 0);
                        }

                        if ((
                            this.listmode.get() == listModes.whitelist
                                && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos3).getBlock())
                                || this.listmode.get() == listModes.blacklist
                                && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos3).getBlock())
                        )
                            && (
                            this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                                && BlockUtils.canBreak(this.blockPos3)
                                || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos3))
                        )) {
                            event.renderer.box(this.blockPos3, (Color) this.sideColor.get(), (Color) this.lineColor.get(), (ShapeMode) this.shapeMode.get(), 0);
                        }

                        if ((
                            this.listmode.get() == listModes.whitelist
                                && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos4).getBlock())
                                || this.listmode.get() == listModes.blacklist
                                && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos4).getBlock())
                        )
                            && (
                            this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                                && BlockUtils.canBreak(this.blockPos4)
                                || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos4))
                        )) {
                            event.renderer.box(this.blockPos4, (Color) this.sideColor.get(), (Color) this.lineColor.get(), (ShapeMode) this.shapeMode.get(), 0);
                        }

                        if ((
                            this.listmode.get() == listModes.whitelist
                                && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos12).getBlock())
                                || this.listmode.get() == listModes.blacklist
                                && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos12).getBlock())
                        )
                            && (
                            this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                                && BlockUtils.canBreak(this.blockPos12)
                                || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos12))
                        )) {
                            event.renderer.box(this.blockPos12, (Color) this.sideColor.get(), (Color) this.lineColor.get(), (ShapeMode) this.shapeMode.get(), 0);
                        }

                        if ((
                            this.listmode.get() == listModes.whitelist
                                && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos13).getBlock())
                                || this.listmode.get() == listModes.blacklist
                                && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos13).getBlock())
                        )
                            && (
                            this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                                && BlockUtils.canBreak(this.blockPos13)
                                || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos13))
                        )) {
                            event.renderer.box(this.blockPos13, (Color) this.sideColor.get(), (Color) this.lineColor.get(), (ShapeMode) this.shapeMode.get(), 0);
                        }

                        if ((
                            this.listmode.get() == listModes.whitelist
                                && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos21).getBlock())
                                || this.listmode.get() == listModes.blacklist
                                && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos21).getBlock())
                        )
                            && (
                            this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                                && BlockUtils.canBreak(this.blockPos21)
                                || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos21))
                        )) {
                            event.renderer.box(this.blockPos21, (Color) this.sideColor.get(), (Color) this.lineColor.get(), (ShapeMode) this.shapeMode.get(), 0);
                        }

                        if ((
                            this.listmode.get() == listModes.whitelist
                                && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos22).getBlock())
                                || this.listmode.get() == listModes.blacklist
                                && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos22).getBlock())
                        )
                            && (
                            this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                                && BlockUtils.canBreak(this.blockPos22)
                                || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos22))
                        )) {
                            event.renderer.box(this.blockPos22, (Color) this.sideColor.get(), (Color) this.lineColor.get(), (ShapeMode) this.shapeMode.get(), 0);
                        }
                    }
                }

                if (this.aorient.get() && this.playerpitch > 30 | this.playerpitch < -30
                    || this.mode.get() == Modes.Horizontal && !this.aorient.get()) {
                    if ((
                        this.listmode.get() == listModes.whitelist
                            && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos1).getBlock())
                            || this.listmode.get() == listModes.blacklist
                            && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos1).getBlock())
                    )
                        && (
                        this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                            && BlockUtils.canBreak(this.blockPos1)
                            || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos1))
                    )) {
                        event.renderer.box(this.blockPos1, (Color) this.sideColor.get(), (Color) this.lineColor.get(), (ShapeMode) this.shapeMode.get(), 0);
                    }

                    if ((
                        this.listmode.get() == listModes.whitelist
                            && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos2).getBlock())
                            || this.listmode.get() == listModes.blacklist
                            && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos2).getBlock())
                    )
                        && (
                        this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                            && BlockUtils.canBreak(this.blockPos2)
                            || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos2))
                    )) {
                        event.renderer.box(this.blockPos2, (Color) this.sideColor.get(), (Color) this.lineColor.get(), (ShapeMode) this.shapeMode.get(), 0);
                    }

                    if ((
                        this.listmode.get() == listModes.whitelist
                            && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos3).getBlock())
                            || this.listmode.get() == listModes.blacklist
                            && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos3).getBlock())
                    )
                        && (
                        this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                            && BlockUtils.canBreak(this.blockPos3)
                            || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos3))
                    )) {
                        event.renderer.box(this.blockPos3, (Color) this.sideColor.get(), (Color) this.lineColor.get(), (ShapeMode) this.shapeMode.get(), 0);
                    }

                    if ((
                        this.listmode.get() == listModes.whitelist
                            && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos4).getBlock())
                            || this.listmode.get() == listModes.blacklist
                            && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos4).getBlock())
                    )
                        && (
                        this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                            && BlockUtils.canBreak(this.blockPos4)
                            || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos4))
                    )) {
                        event.renderer.box(this.blockPos4, (Color) this.sideColor.get(), (Color) this.lineColor.get(), (ShapeMode) this.shapeMode.get(), 0);
                    }

                    if ((
                        this.listmode.get() == listModes.whitelist
                            && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos5).getBlock())
                            || this.listmode.get() == listModes.blacklist
                            && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos5).getBlock())
                    )
                        && (
                        this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                            && BlockUtils.canBreak(this.blockPos5)
                            || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos5))
                    )) {
                        event.renderer.box(this.blockPos5, (Color) this.sideColor.get(), (Color) this.lineColor.get(), (ShapeMode) this.shapeMode.get(), 0);
                    }

                    if ((
                        this.listmode.get() == listModes.whitelist
                            && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos6).getBlock())
                            || this.listmode.get() == listModes.blacklist
                            && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos6).getBlock())
                    )
                        && (
                        this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                            && BlockUtils.canBreak(this.blockPos6)
                            || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos6))
                    )) {
                        event.renderer.box(this.blockPos6, (Color) this.sideColor.get(), (Color) this.lineColor.get(), (ShapeMode) this.shapeMode.get(), 0);
                    }

                    if ((
                        this.listmode.get() == listModes.whitelist
                            && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos7).getBlock())
                            || this.listmode.get() == listModes.blacklist
                            && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos7).getBlock())
                    )
                        && (
                        this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                            && BlockUtils.canBreak(this.blockPos7)
                            || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos7))
                    )) {
                        event.renderer.box(this.blockPos7, (Color) this.sideColor.get(), (Color) this.lineColor.get(), (ShapeMode) this.shapeMode.get(), 0);
                    }

                    if ((
                        this.listmode.get() == listModes.whitelist
                            && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos8).getBlock())
                            || this.listmode.get() == listModes.blacklist
                            && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos8).getBlock())
                    )
                        && (
                        this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                            && BlockUtils.canBreak(this.blockPos8)
                            || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos8))
                    )) {
                        event.renderer.box(this.blockPos8, (Color) this.sideColor.get(), (Color) this.lineColor.get(), (ShapeMode) this.shapeMode.get(), 0);
                    }
                }
            }

            if (this.range.get() == 5) {
                if ((
                    this.listmode.get() == listModes.whitelist
                        && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos1).getBlock())
                        || this.listmode.get() == listModes.blacklist
                        && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos1).getBlock())
                )
                    && (
                    this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                        && BlockUtils.canBreak(this.blockPos1)
                        || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos1))
                )) {
                    event.renderer.box(this.blockPos1, (Color) this.sideColor.get(), (Color) this.lineColor.get(), (ShapeMode) this.shapeMode.get(), 0);
                }

                if ((
                    this.listmode.get() == listModes.whitelist
                        && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos2).getBlock())
                        || this.listmode.get() == listModes.blacklist
                        && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos2).getBlock())
                )
                    && (
                    this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                        && BlockUtils.canBreak(this.blockPos2)
                        || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos2))
                )) {
                    event.renderer.box(this.blockPos2, (Color) this.sideColor.get(), (Color) this.lineColor.get(), (ShapeMode) this.shapeMode.get(), 0);
                }

                if ((
                    this.listmode.get() == listModes.whitelist
                        && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos3).getBlock())
                        || this.listmode.get() == listModes.blacklist
                        && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos3).getBlock())
                )
                    && (
                    this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                        && BlockUtils.canBreak(this.blockPos3)
                        || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos3))
                )) {
                    event.renderer.box(this.blockPos3, (Color) this.sideColor.get(), (Color) this.lineColor.get(), (ShapeMode) this.shapeMode.get(), 0);
                }

                if ((
                    this.listmode.get() == listModes.whitelist
                        && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos4).getBlock())
                        || this.listmode.get() == listModes.blacklist
                        && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos4).getBlock())
                )
                    && (
                    this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                        && BlockUtils.canBreak(this.blockPos4)
                        || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos4))
                )) {
                    event.renderer.box(this.blockPos4, (Color) this.sideColor.get(), (Color) this.lineColor.get(), (ShapeMode) this.shapeMode.get(), 0);
                }

                if ((
                    this.listmode.get() == listModes.whitelist
                        && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos5).getBlock())
                        || this.listmode.get() == listModes.blacklist
                        && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos5).getBlock())
                )
                    && (
                    this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                        && BlockUtils.canBreak(this.blockPos5)
                        || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos5))
                )) {
                    event.renderer.box(this.blockPos5, (Color) this.sideColor.get(), (Color) this.lineColor.get(), (ShapeMode) this.shapeMode.get(), 0);
                }

                if ((
                    this.listmode.get() == listModes.whitelist
                        && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos6).getBlock())
                        || this.listmode.get() == listModes.blacklist
                        && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos6).getBlock())
                )
                    && (
                    this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                        && BlockUtils.canBreak(this.blockPos6)
                        || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos6))
                )) {
                    event.renderer.box(this.blockPos6, (Color) this.sideColor.get(), (Color) this.lineColor.get(), (ShapeMode) this.shapeMode.get(), 0);
                }

                if ((
                    this.listmode.get() == listModes.whitelist
                        && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos7).getBlock())
                        || this.listmode.get() == listModes.blacklist
                        && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos7).getBlock())
                )
                    && (
                    this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                        && BlockUtils.canBreak(this.blockPos7)
                        || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos7))
                )) {
                    event.renderer.box(this.blockPos7, (Color) this.sideColor.get(), (Color) this.lineColor.get(), (ShapeMode) this.shapeMode.get(), 0);
                }

                if ((
                    this.listmode.get() == listModes.whitelist
                        && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos8).getBlock())
                        || this.listmode.get() == listModes.blacklist
                        && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos8).getBlock())
                )
                    && (
                    this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                        && BlockUtils.canBreak(this.blockPos8)
                        || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos8))
                )) {
                    event.renderer.box(this.blockPos8, (Color) this.sideColor.get(), (Color) this.lineColor.get(), (ShapeMode) this.shapeMode.get(), 0);
                }

                if ((
                    this.listmode.get() == listModes.whitelist
                        && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos9).getBlock())
                        || this.listmode.get() == listModes.blacklist
                        && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos9).getBlock())
                )
                    && (
                    this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                        && BlockUtils.canBreak(this.blockPos9)
                        || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos9))
                )) {
                    event.renderer.box(this.blockPos9, (Color) this.sideColor.get(), (Color) this.lineColor.get(), (ShapeMode) this.shapeMode.get(), 0);
                }

                if ((
                    this.listmode.get() == listModes.whitelist
                        && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos18).getBlock())
                        || this.listmode.get() == listModes.blacklist
                        && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos18).getBlock())
                )
                    && (
                    this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                        && BlockUtils.canBreak(this.blockPos18)
                        || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos18))
                )) {
                    event.renderer.box(this.blockPos18, (Color) this.sideColor.get(), (Color) this.lineColor.get(), (ShapeMode) this.shapeMode.get(), 0);
                }
            }

            if (this.range.get() == 6) {
                if ((
                    this.listmode.get() == listModes.whitelist
                        && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos1).getBlock())
                        || this.listmode.get() == listModes.blacklist
                        && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos1).getBlock())
                )
                    && (
                    this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                        && BlockUtils.canBreak(this.blockPos1)
                        || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos1))
                )) {
                    event.renderer.box(this.blockPos1, (Color) this.sideColor.get(), (Color) this.lineColor.get(), (ShapeMode) this.shapeMode.get(), 0);
                }

                if ((
                    this.listmode.get() == listModes.whitelist
                        && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos2).getBlock())
                        || this.listmode.get() == listModes.blacklist
                        && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos2).getBlock())
                )
                    && (
                    this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                        && BlockUtils.canBreak(this.blockPos2)
                        || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos2))
                )) {
                    event.renderer.box(this.blockPos2, (Color) this.sideColor.get(), (Color) this.lineColor.get(), (ShapeMode) this.shapeMode.get(), 0);
                }

                if ((
                    this.listmode.get() == listModes.whitelist
                        && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos3).getBlock())
                        || this.listmode.get() == listModes.blacklist
                        && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos3).getBlock())
                )
                    && (
                    this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                        && BlockUtils.canBreak(this.blockPos3)
                        || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos3))
                )) {
                    event.renderer.box(this.blockPos3, (Color) this.sideColor.get(), (Color) this.lineColor.get(), (ShapeMode) this.shapeMode.get(), 0);
                }

                if ((
                    this.listmode.get() == listModes.whitelist
                        && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos4).getBlock())
                        || this.listmode.get() == listModes.blacklist
                        && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos4).getBlock())
                )
                    && (
                    this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                        && BlockUtils.canBreak(this.blockPos4)
                        || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos4))
                )) {
                    event.renderer.box(this.blockPos4, (Color) this.sideColor.get(), (Color) this.lineColor.get(), (ShapeMode) this.shapeMode.get(), 0);
                }

                if ((
                    this.listmode.get() == listModes.whitelist
                        && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos5).getBlock())
                        || this.listmode.get() == listModes.blacklist
                        && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos5).getBlock())
                )
                    && (
                    this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                        && BlockUtils.canBreak(this.blockPos5)
                        || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos5))
                )) {
                    event.renderer.box(this.blockPos5, (Color) this.sideColor.get(), (Color) this.lineColor.get(), (ShapeMode) this.shapeMode.get(), 0);
                }

                if ((
                    this.listmode.get() == listModes.whitelist
                        && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos6).getBlock())
                        || this.listmode.get() == listModes.blacklist
                        && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos6).getBlock())
                )
                    && (
                    this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                        && BlockUtils.canBreak(this.blockPos6)
                        || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos6))
                )) {
                    event.renderer.box(this.blockPos6, (Color) this.sideColor.get(), (Color) this.lineColor.get(), (ShapeMode) this.shapeMode.get(), 0);
                }

                if ((
                    this.listmode.get() == listModes.whitelist
                        && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos7).getBlock())
                        || this.listmode.get() == listModes.blacklist
                        && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos7).getBlock())
                )
                    && (
                    this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                        && BlockUtils.canBreak(this.blockPos7)
                        || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos7))
                )) {
                    event.renderer.box(this.blockPos7, (Color) this.sideColor.get(), (Color) this.lineColor.get(), (ShapeMode) this.shapeMode.get(), 0);
                }

                if ((
                    this.listmode.get() == listModes.whitelist
                        && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos8).getBlock())
                        || this.listmode.get() == listModes.blacklist
                        && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos8).getBlock())
                )
                    && (
                    this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                        && BlockUtils.canBreak(this.blockPos8)
                        || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos8))
                )) {
                    event.renderer.box(this.blockPos8, (Color) this.sideColor.get(), (Color) this.lineColor.get(), (ShapeMode) this.shapeMode.get(), 0);
                }

                if ((
                    this.listmode.get() == listModes.whitelist
                        && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos9).getBlock())
                        || this.listmode.get() == listModes.blacklist
                        && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos9).getBlock())
                )
                    && (
                    this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                        && BlockUtils.canBreak(this.blockPos9)
                        || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos9))
                )) {
                    event.renderer.box(this.blockPos9, (Color) this.sideColor.get(), (Color) this.lineColor.get(), (ShapeMode) this.shapeMode.get(), 0);
                }

                if ((
                    this.listmode.get() == listModes.whitelist
                        && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos10).getBlock())
                        || this.listmode.get() == listModes.blacklist
                        && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos10).getBlock())
                )
                    && (
                    this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                        && BlockUtils.canBreak(this.blockPos10)
                        || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos10))
                )) {
                    event.renderer.box(this.blockPos10, (Color) this.sideColor.get(), (Color) this.lineColor.get(), (ShapeMode) this.shapeMode.get(), 0);
                }

                if ((
                    this.listmode.get() == listModes.whitelist
                        && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos11).getBlock())
                        || this.listmode.get() == listModes.blacklist
                        && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos11).getBlock())
                )
                    && (
                    this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                        && BlockUtils.canBreak(this.blockPos11)
                        || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos11))
                )) {
                    event.renderer.box(this.blockPos11, (Color) this.sideColor.get(), (Color) this.lineColor.get(), (ShapeMode) this.shapeMode.get(), 0);
                }

                if ((
                    this.listmode.get() == listModes.whitelist
                        && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos12).getBlock())
                        || this.listmode.get() == listModes.blacklist
                        && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos12).getBlock())
                )
                    && (
                    this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                        && BlockUtils.canBreak(this.blockPos12)
                        || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos12))
                )) {
                    event.renderer.box(this.blockPos12, (Color) this.sideColor.get(), (Color) this.lineColor.get(), (ShapeMode) this.shapeMode.get(), 0);
                }

                if ((
                    this.listmode.get() == listModes.whitelist
                        && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos13).getBlock())
                        || this.listmode.get() == listModes.blacklist
                        && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos13).getBlock())
                )
                    && (
                    this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                        && BlockUtils.canBreak(this.blockPos13)
                        || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos13))
                )) {
                    event.renderer.box(this.blockPos13, (Color) this.sideColor.get(), (Color) this.lineColor.get(), (ShapeMode) this.shapeMode.get(), 0);
                }

                if ((
                    this.listmode.get() == listModes.whitelist
                        && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos18).getBlock())
                        || this.listmode.get() == listModes.blacklist
                        && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos18).getBlock())
                )
                    && (
                    this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                        && BlockUtils.canBreak(this.blockPos18)
                        || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos18))
                )) {
                    event.renderer.box(this.blockPos18, (Color) this.sideColor.get(), (Color) this.lineColor.get(), (ShapeMode) this.shapeMode.get(), 0);
                }

                if ((
                    this.listmode.get() == listModes.whitelist
                        && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos19).getBlock())
                        || this.listmode.get() == listModes.blacklist
                        && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos19).getBlock())
                )
                    && (
                    this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                        && BlockUtils.canBreak(this.blockPos19)
                        || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos19))
                )) {
                    event.renderer.box(this.blockPos19, (Color) this.sideColor.get(), (Color) this.lineColor.get(), (ShapeMode) this.shapeMode.get(), 0);
                }

                if ((
                    this.listmode.get() == listModes.whitelist
                        && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos20).getBlock())
                        || this.listmode.get() == listModes.blacklist
                        && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos20).getBlock())
                )
                    && (
                    this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                        && BlockUtils.canBreak(this.blockPos20)
                        || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos20))
                )) {
                    event.renderer.box(this.blockPos20, (Color) this.sideColor.get(), (Color) this.lineColor.get(), (ShapeMode) this.shapeMode.get(), 0);
                }

                if ((
                    this.listmode.get() == listModes.whitelist
                        && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos21).getBlock())
                        || this.listmode.get() == listModes.blacklist
                        && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos21).getBlock())
                )
                    && (
                    this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                        && BlockUtils.canBreak(this.blockPos21)
                        || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos21))
                )) {
                    event.renderer.box(this.blockPos21, (Color) this.sideColor.get(), (Color) this.lineColor.get(), (ShapeMode) this.shapeMode.get(), 0);
                }

                if ((
                    this.listmode.get() == listModes.whitelist
                        && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos22).getBlock())
                        || this.listmode.get() == listModes.blacklist
                        && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos22).getBlock())
                )
                    && (
                    this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                        && BlockUtils.canBreak(this.blockPos22)
                        || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos22))
                )) {
                    event.renderer.box(this.blockPos22, (Color) this.sideColor.get(), (Color) this.lineColor.get(), (ShapeMode) this.shapeMode.get(), 0);
                }
            }

            if (this.range.get() == 7) {
                if ((
                    this.listmode.get() == listModes.whitelist
                        && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos1).getBlock())
                        || this.listmode.get() == listModes.blacklist
                        && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos1).getBlock())
                )
                    && (
                    this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                        && BlockUtils.canBreak(this.blockPos1)
                        || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos1))
                )) {
                    event.renderer.box(this.blockPos1, (Color) this.sideColor.get(), (Color) this.lineColor.get(), (ShapeMode) this.shapeMode.get(), 0);
                }

                if ((
                    this.listmode.get() == listModes.whitelist
                        && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos2).getBlock())
                        || this.listmode.get() == listModes.blacklist
                        && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos2).getBlock())
                )
                    && (
                    this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                        && BlockUtils.canBreak(this.blockPos2)
                        || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos2))
                )) {
                    event.renderer.box(this.blockPos2, (Color) this.sideColor.get(), (Color) this.lineColor.get(), (ShapeMode) this.shapeMode.get(), 0);
                }

                if ((
                    this.listmode.get() == listModes.whitelist
                        && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos3).getBlock())
                        || this.listmode.get() == listModes.blacklist
                        && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos3).getBlock())
                )
                    && (
                    this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                        && BlockUtils.canBreak(this.blockPos3)
                        || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos3))
                )) {
                    event.renderer.box(this.blockPos3, (Color) this.sideColor.get(), (Color) this.lineColor.get(), (ShapeMode) this.shapeMode.get(), 0);
                }

                if ((
                    this.listmode.get() == listModes.whitelist
                        && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos4).getBlock())
                        || this.listmode.get() == listModes.blacklist
                        && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos4).getBlock())
                )
                    && (
                    this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                        && BlockUtils.canBreak(this.blockPos4)
                        || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos4))
                )) {
                    event.renderer.box(this.blockPos4, (Color) this.sideColor.get(), (Color) this.lineColor.get(), (ShapeMode) this.shapeMode.get(), 0);
                }

                if ((
                    this.listmode.get() == listModes.whitelist
                        && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos5).getBlock())
                        || this.listmode.get() == listModes.blacklist
                        && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos5).getBlock())
                )
                    && (
                    this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                        && BlockUtils.canBreak(this.blockPos5)
                        || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos5))
                )) {
                    event.renderer.box(this.blockPos5, (Color) this.sideColor.get(), (Color) this.lineColor.get(), (ShapeMode) this.shapeMode.get(), 0);
                }

                if ((
                    this.listmode.get() == listModes.whitelist
                        && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos6).getBlock())
                        || this.listmode.get() == listModes.blacklist
                        && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos6).getBlock())
                )
                    && (
                    this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                        && BlockUtils.canBreak(this.blockPos6)
                        || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos6))
                )) {
                    event.renderer.box(this.blockPos6, (Color) this.sideColor.get(), (Color) this.lineColor.get(), (ShapeMode) this.shapeMode.get(), 0);
                }

                if ((
                    this.listmode.get() == listModes.whitelist
                        && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos7).getBlock())
                        || this.listmode.get() == listModes.blacklist
                        && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos7).getBlock())
                )
                    && (
                    this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                        && BlockUtils.canBreak(this.blockPos7)
                        || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos7))
                )) {
                    event.renderer.box(this.blockPos7, (Color) this.sideColor.get(), (Color) this.lineColor.get(), (ShapeMode) this.shapeMode.get(), 0);
                }

                if ((
                    this.listmode.get() == listModes.whitelist
                        && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos8).getBlock())
                        || this.listmode.get() == listModes.blacklist
                        && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos8).getBlock())
                )
                    && (
                    this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                        && BlockUtils.canBreak(this.blockPos8)
                        || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos8))
                )) {
                    event.renderer.box(this.blockPos8, (Color) this.sideColor.get(), (Color) this.lineColor.get(), (ShapeMode) this.shapeMode.get(), 0);
                }

                if ((
                    this.listmode.get() == listModes.whitelist
                        && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos9).getBlock())
                        || this.listmode.get() == listModes.blacklist
                        && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos9).getBlock())
                )
                    && (
                    this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                        && BlockUtils.canBreak(this.blockPos9)
                        || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos9))
                )) {
                    event.renderer.box(this.blockPos9, (Color) this.sideColor.get(), (Color) this.lineColor.get(), (ShapeMode) this.shapeMode.get(), 0);
                }

                if ((
                    this.listmode.get() == listModes.whitelist
                        && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos10).getBlock())
                        || this.listmode.get() == listModes.blacklist
                        && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos10).getBlock())
                )
                    && (
                    this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                        && BlockUtils.canBreak(this.blockPos10)
                        || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos10))
                )) {
                    event.renderer.box(this.blockPos10, (Color) this.sideColor.get(), (Color) this.lineColor.get(), (ShapeMode) this.shapeMode.get(), 0);
                }

                if ((
                    this.listmode.get() == listModes.whitelist
                        && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos11).getBlock())
                        || this.listmode.get() == listModes.blacklist
                        && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos11).getBlock())
                )
                    && (
                    this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                        && BlockUtils.canBreak(this.blockPos11)
                        || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos11))
                )) {
                    event.renderer.box(this.blockPos11, (Color) this.sideColor.get(), (Color) this.lineColor.get(), (ShapeMode) this.shapeMode.get(), 0);
                }

                if ((
                    this.listmode.get() == listModes.whitelist
                        && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos12).getBlock())
                        || this.listmode.get() == listModes.blacklist
                        && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos12).getBlock())
                )
                    && (
                    this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                        && BlockUtils.canBreak(this.blockPos12)
                        || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos12))
                )) {
                    event.renderer.box(this.blockPos12, (Color) this.sideColor.get(), (Color) this.lineColor.get(), (ShapeMode) this.shapeMode.get(), 0);
                }

                if ((
                    this.listmode.get() == listModes.whitelist
                        && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos13).getBlock())
                        || this.listmode.get() == listModes.blacklist
                        && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos13).getBlock())
                )
                    && (
                    this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                        && BlockUtils.canBreak(this.blockPos13)
                        || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos13))
                )) {
                    event.renderer.box(this.blockPos13, (Color) this.sideColor.get(), (Color) this.lineColor.get(), (ShapeMode) this.shapeMode.get(), 0);
                }

                if ((
                    this.listmode.get() == listModes.whitelist
                        && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos14).getBlock())
                        || this.listmode.get() == listModes.blacklist
                        && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos14).getBlock())
                )
                    && (
                    this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                        && BlockUtils.canBreak(this.blockPos14)
                        || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos14))
                )) {
                    event.renderer.box(this.blockPos14, (Color) this.sideColor.get(), (Color) this.lineColor.get(), (ShapeMode) this.shapeMode.get(), 0);
                }

                if ((
                    this.listmode.get() == listModes.whitelist
                        && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos15).getBlock())
                        || this.listmode.get() == listModes.blacklist
                        && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos15).getBlock())
                )
                    && (
                    this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                        && BlockUtils.canBreak(this.blockPos15)
                        || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos15))
                )) {
                    event.renderer.box(this.blockPos15, (Color) this.sideColor.get(), (Color) this.lineColor.get(), (ShapeMode) this.shapeMode.get(), 0);
                }

                if ((
                    this.listmode.get() == listModes.whitelist
                        && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos16).getBlock())
                        || this.listmode.get() == listModes.blacklist
                        && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos16).getBlock())
                )
                    && (
                    this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                        && BlockUtils.canBreak(this.blockPos16)
                        || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos16))
                )) {
                    event.renderer.box(this.blockPos16, (Color) this.sideColor.get(), (Color) this.lineColor.get(), (ShapeMode) this.shapeMode.get(), 0);
                }

                if ((
                    this.listmode.get() == listModes.whitelist
                        && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos17).getBlock())
                        || this.listmode.get() == listModes.blacklist
                        && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos17).getBlock())
                )
                    && (
                    this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                        && BlockUtils.canBreak(this.blockPos17)
                        || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos17))
                )) {
                    event.renderer.box(this.blockPos17, (Color) this.sideColor.get(), (Color) this.lineColor.get(), (ShapeMode) this.shapeMode.get(), 0);
                }

                if ((
                    this.listmode.get() == listModes.whitelist
                        && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos18).getBlock())
                        || this.listmode.get() == listModes.blacklist
                        && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos18).getBlock())
                )
                    && (
                    this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                        && BlockUtils.canBreak(this.blockPos18)
                        || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos18))
                )) {
                    event.renderer.box(this.blockPos18, (Color) this.sideColor.get(), (Color) this.lineColor.get(), (ShapeMode) this.shapeMode.get(), 0);
                }

                if ((
                    this.listmode.get() == listModes.whitelist
                        && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos19).getBlock())
                        || this.listmode.get() == listModes.blacklist
                        && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos19).getBlock())
                )
                    && (
                    this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                        && BlockUtils.canBreak(this.blockPos19)
                        || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos19))
                )) {
                    event.renderer.box(this.blockPos19, (Color) this.sideColor.get(), (Color) this.lineColor.get(), (ShapeMode) this.shapeMode.get(), 0);
                }

                if ((
                    this.listmode.get() == listModes.whitelist
                        && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos20).getBlock())
                        || this.listmode.get() == listModes.blacklist
                        && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos20).getBlock())
                )
                    && (
                    this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                        && BlockUtils.canBreak(this.blockPos20)
                        || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos20))
                )) {
                    event.renderer.box(this.blockPos20, (Color) this.sideColor.get(), (Color) this.lineColor.get(), (ShapeMode) this.shapeMode.get(), 0);
                }

                if ((
                    this.listmode.get() == listModes.whitelist
                        && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos21).getBlock())
                        || this.listmode.get() == listModes.blacklist
                        && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos21).getBlock())
                )
                    && (
                    this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                        && BlockUtils.canBreak(this.blockPos21)
                        || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos21))
                )) {
                    event.renderer.box(this.blockPos21, (Color) this.sideColor.get(), (Color) this.lineColor.get(), (ShapeMode) this.shapeMode.get(), 0);
                }

                if ((
                    this.listmode.get() == listModes.whitelist
                        && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos22).getBlock())
                        || this.listmode.get() == listModes.blacklist
                        && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos22).getBlock())
                )
                    && (
                    this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                        && BlockUtils.canBreak(this.blockPos22)
                        || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos22))
                )) {
                    event.renderer.box(this.blockPos22, (Color) this.sideColor.get(), (Color) this.lineColor.get(), (ShapeMode) this.shapeMode.get(), 0);
                }

                if ((
                    this.listmode.get() == listModes.whitelist
                        && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos23).getBlock())
                        || this.listmode.get() == listModes.blacklist
                        && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos23).getBlock())
                )
                    && (
                    this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                        && BlockUtils.canBreak(this.blockPos23)
                        || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos23))
                )) {
                    event.renderer.box(this.blockPos23, (Color) this.sideColor.get(), (Color) this.lineColor.get(), (ShapeMode) this.shapeMode.get(), 0);
                }

                if ((
                    this.listmode.get() == listModes.whitelist
                        && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos24).getBlock())
                        || this.listmode.get() == listModes.blacklist
                        && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos24).getBlock())
                )
                    && (
                    this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                        && BlockUtils.canBreak(this.blockPos24)
                        || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos24))
                )) {
                    event.renderer.box(this.blockPos24, (Color) this.sideColor.get(), (Color) this.lineColor.get(), (ShapeMode) this.shapeMode.get(), 0);
                }

                if ((
                    this.listmode.get() == listModes.whitelist
                        && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos25).getBlock())
                        || this.listmode.get() == listModes.blacklist
                        && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos25).getBlock())
                )
                    && (
                    this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                        && BlockUtils.canBreak(this.blockPos25)
                        || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos25))
                )) {
                    event.renderer.box(this.blockPos25, (Color) this.sideColor.get(), (Color) this.lineColor.get(), (ShapeMode) this.shapeMode.get(), 0);
                }

                if ((
                    this.listmode.get() == listModes.whitelist
                        && (this.nonskippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos26).getBlock())
                        || this.listmode.get() == listModes.blacklist
                        && !(this.skippableBlox.get()).contains(this.mc.world.getBlockState(this.blockPos26).getBlock())
                )
                    && (
                    this.mc.player.getAbilities().creativeMode | !(this.mc.player.getMainHandStack().getItem() instanceof ToolItem)
                        && BlockUtils.canBreak(this.blockPos26)
                        || this.mc.player.getMainHandStack().isSuitableFor(this.mc.world.getBlockState(this.blockPos26))
                )) {
                    event.renderer.box(this.blockPos26, (Color) this.sideColor.get(), (Color) this.lineColor.get(), (ShapeMode) this.shapeMode.get(), 0);
                }
            }
        }
    }

    public static enum Modes {
        Horizontal,
        Vertical;
    }

    public static enum listModes {
        whitelist,
        blacklist;
    }
}
