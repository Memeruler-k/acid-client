package dev.abstr3act.addon.modules.Compassion;

import dev.abstr3act.addon.Compassion;
import dev.abstr3act.addon.events.EventAttackBlock;
import dev.abstr3act.addon.mixin.accessor.IInteractionManager;
import dev.abstr3act.addon.module.CompassionModule;
import dev.abstr3act.addon.utils.Render3DEngine;
import dev.abstr3act.addon.utils.math.InventoryUtility;
import dev.abstr3act.addon.utils.math.MathUtility;
import dev.abstr3act.addon.utils.math.PlayerUtility;
import dev.abstr3act.addon.utils.render.Render2DEngine;
import dev.abstr3act.addon.utils.timers.Timer;
import meteordevelopment.meteorclient.events.packets.PacketEvent.Send;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.events.world.TickEvent.Pre;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.EnumSetting.Builder;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.AirBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.EndCrystalItem;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket.Action;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public final class SpeedMine extends CompassionModule {
    public static BlockPos minePosition;
    public static float progress;
    public static float prevProgress;
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
    public final Setting<Mode> mode = this.sgGeneral
        .add(((Builder) ((Builder) ((Builder) new Builder().name("Mode")).description(".")).defaultValue(Mode.Packet)).build());
    public final Setting<StartMode> startMode = this.sgGeneral
        .add(
            ((Builder) ((Builder) ((Builder) ((Builder) new Builder().name("StartMode")).description(".")).defaultValue(StartMode.StartAbort))
                .visible(() -> this.mode.get() == Mode.Packet))
                .build()
        );
    public final Setting<SwitchMode> switchMode = this.sgGeneral
        .add(
            ((Builder) ((Builder) ((Builder) ((Builder) new Builder().name("SwitchMode")).description(".")).defaultValue(SwitchMode.Silent))
                .visible(() -> this.mode.get() != Mode.Damage))
                .build()
        );
    private final Setting<Integer> swapDelay = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("SwapDelay"))
                .description("."))
                .defaultValue(50))
                .sliderRange(0, 1000)
                .visible(() -> this.switchMode.get() == SwitchMode.Alternative && this.mode.get() != Mode.Damage))
                .build()
        );
    private final Setting<Double> factor = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("Factor"))
                .description("."))
                .defaultValue(1.0)
                .sliderRange(0.5, 2.0)
                .visible(() -> this.mode.get() != Mode.Damage))
                .build()
        );
    private final Setting<Double> rebreakfactor = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("RebreakFactor"))
                .description("."))
                .defaultValue(7.0)
                .sliderRange(0.5, 20.0)
                .visible(() -> this.mode.get() == Mode.GrimInstant))
                .build()
        );
    private final Setting<Double> speed = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("Speed"))
                .description("."))
                .defaultValue(0.5)
                .sliderRange(0.0, 1.0)
                .visible(() -> this.mode.get() == Mode.Damage))
                .build()
        );
    private final Setting<Double> range = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("Range"))
                .description("."))
                .defaultValue(4.2F)
                .sliderRange(3.0, 200.0)
                .visible(() -> this.mode.get() != Mode.Damage))
                .build()
        );
    private final Setting<Integer> breakAttempts = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("BreakAttempts"))
                .description("."))
                .defaultValue(10))
                .sliderRange(1, 50)
                .visible(() -> this.mode.get() == Mode.Packet))
                .build()
        );
    public final Setting<RenderMode> renderMode = this.sgGeneral
        .add(((Builder) ((Builder) ((Builder) new Builder().name("RenderMode")).description(".")).defaultValue(RenderMode.Shrink)).build());
    private final Setting<Boolean> rotate = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("Rotate"))
                .description("."))
                .defaultValue(false))
                .build()
        );
    private final Setting<Boolean> resetOnSwitch = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("ResetOnSwitch"))
                .description("."))
                .defaultValue(false))
                .build()
        );
    private final Setting<Boolean> stop = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("Stop"))
                .description("."))
                .defaultValue(true))
                .build()
        );
    private final Setting<Boolean> abort = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("Abort"))
                .description("."))
                .defaultValue(true))
                .build()
        );
    private final Setting<Boolean> start = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("Start"))
                .description("."))
                .defaultValue(true))
                .build()
        );
    private final Setting<Boolean> stop2 = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("Stop2"))
                .description("."))
                .defaultValue(true))
                .build()
        );
    private final Setting<Boolean> smooth = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("Smooth"))
                .description("."))
                .defaultValue(true))
                .build()
        );
    private final Setting<Boolean> notOnCrystal = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("NotOnCrystal"))
                .description("."))
                .defaultValue(true))
                .build()
        );
    private final Setting<ShapeMode> shapeMode = this.sgGeneral
        .add(((Builder) ((Builder) ((Builder) new Builder().name("shape-mode")).description("How the shapes are rendered.")).defaultValue(ShapeMode.Both)).build());
    private final Setting<SettingColor> startLineColor = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                .name("Start Line Color"))
                .description("."))
                .defaultValue(new Color(255, 255, 255, 255))
                .build()
        );
    private final Setting<SettingColor> endLineColor = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                .name("End Line Color"))
                .description("."))
                .defaultValue(new Color(255, 255, 255, 255))
                .build()
        );
    private final Setting<Integer> lineWidth = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("Line Width"))
                .description("."))
                .defaultValue(2))
                .min(1)
                .sliderRange(1, 10)
                .build()
        );
    private final Setting<SettingColor> startFillColor = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                .name("Start Fill Color"))
                .description("."))
                .defaultValue(new Color(255, 255, 255, 255))
                .build()
        );
    private final Setting<SettingColor> endFillColor = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                .name("End Line Color"))
                .description("."))
                .defaultValue(new Color(255, 255, 255, 255))
                .build()
        );
    private final Timer attackTimer = new Timer();
    public boolean worth = false;
    private Direction mineFacing;
    private int mineBreaks;

    public SpeedMine() {
        super(Compassion.COMPASSION, "SpeedMine", "SpeedMine.");
    }

    public static float[] calcAngle(Vec3d from, Vec3d to) {
        if (to == null) {
            return null;
        } else {
            double difX = to.x - from.x;
            double difY = (to.y - from.y) * -1.0;
            double difZ = to.z - from.z;
            double dist = MathHelper.sqrt((float) (difX * difX + difZ * difZ));
            return new float[]{
                (float) MathHelper.wrapDegrees(Math.toDegrees(Math.atan2(difZ, difX)) - 90.0), (float) MathHelper.wrapDegrees(Math.toDegrees(Math.atan2(difY, dist)))
            };
        }
    }

    public void onActivate() {
        this.reset();
    }

    public void onDeactivate() {
        this.reset();
    }

    @EventHandler
    public void onUpdate(Pre event) {
        if (this.mc.player != null && this.mc.world != null && this.mc.interactionManager != null && !this.mc.player.getAbilities().creativeMode) {
            if (!this.notOnCrystal.get() || !(this.mc.player.getMainHandStack().getItem() instanceof EndCrystalItem)) {
                if (this.mode.get() == Mode.Damage) {
                    if (((IInteractionManager) this.mc.interactionManager).getCurBlockDamageMP() < this.speed.get()) {
                        ((IInteractionManager) this.mc.interactionManager).setCurBlockDamageMP((this.speed.get()).floatValue());
                    }
                } else if (this.mode.get() == Mode.Packet) {
                    if (minePosition != null) {
                        if (this.mineBreaks >= this.breakAttempts.get()
                            || PlayerUtility.squaredDistanceFromEyes(minePosition.toCenterPos()) > Math.pow(this.range.get(), 2.0)) {
                            this.reset();
                            return;
                        }

                        if (progress == 0.0F && !this.mc.world.isAir(minePosition) && this.attackTimer.passedMs(800L)) {
                            this.mc.interactionManager.attackBlock(minePosition, this.mineFacing);
                            this.mc.player.swingHand(Hand.MAIN_HAND);
                            this.attackTimer.reset();
                        }
                    }

                    System.out.println(minePosition);
                    if (minePosition != null && !this.mc.world.isAir(minePosition)) {
                        int invPickSlot = this.getTool(minePosition);
                        int hotBarPickSlot = InventoryUtility.getPickAxeHotbar().slot();
                        int prevSlot = -1;
                        if (invPickSlot == -1 && this.switchMode.get() == SwitchMode.Alternative) {
                            return;
                        }

                        if (hotBarPickSlot == -1 && this.switchMode.get() != SwitchMode.Alternative) {
                            return;
                        }

                        System.out.println(progress);
                        if (progress >= 1.0F) {
                            if (this.switchMode.get() == SwitchMode.Alternative) {
                                this.mc
                                    .interactionManager
                                    .clickSlot(
                                        this.mc.player.currentScreenHandler.syncId,
                                        invPickSlot < 9 ? invPickSlot + 36 : invPickSlot,
                                        this.mc.player.getInventory().selectedSlot,
                                        SlotActionType.SWAP,
                                        this.mc.player
                                    );
                                this.closeScreen();
                            } else if (this.switchMode.get() == SwitchMode.Normal || this.switchMode.get() == SwitchMode.Silent) {
                                prevSlot = this.mc.player.getInventory().selectedSlot;
                                InvUtils.swap(InvUtils.findFastestTool(this.mc.world.getBlockState(minePosition)).slot(), false);
                            }

                            if (this.stop.get()) {
                                this.sendPacket(new PlayerActionC2SPacket(Action.STOP_DESTROY_BLOCK, minePosition, this.mineFacing));
                            }

                            if (this.abort.get()) {
                                this.sendPacket(new PlayerActionC2SPacket(Action.ABORT_DESTROY_BLOCK, minePosition, this.mineFacing));
                            }

                            if (this.start.get()) {
                                this.sendPacket(new PlayerActionC2SPacket(Action.START_DESTROY_BLOCK, minePosition, this.mineFacing));
                            }

                            if (this.stop2.get()) {
                                this.sendPacket(new PlayerActionC2SPacket(Action.STOP_DESTROY_BLOCK, minePosition, this.mineFacing));
                            }

                            if (this.switchMode.get() == SwitchMode.Alternative) {
                                if (this.swapDelay.get() != 0) {
                                    this.mc
                                        .interactionManager
                                        .clickSlot(
                                            this.mc.player.currentScreenHandler.syncId,
                                            invPickSlot < 9 ? invPickSlot + 36 : invPickSlot,
                                            this.mc.player.getInventory().selectedSlot,
                                            SlotActionType.SWAP,
                                            this.mc.player
                                        );
                                    this.closeScreen();
                                    this.swapDelay.get();
                                } else {
                                    this.mc
                                        .interactionManager
                                        .clickSlot(
                                            this.mc.player.currentScreenHandler.syncId,
                                            invPickSlot < 9 ? invPickSlot + 36 : invPickSlot,
                                            this.mc.player.getInventory().selectedSlot,
                                            SlotActionType.SWAP,
                                            this.mc.player
                                        );
                                    this.closeScreen();
                                }
                            } else if (this.switchMode.get() == SwitchMode.Silent) {
                                InventoryUtility.switchTo(prevSlot);
                            }

                            progress = 0.0F;
                            this.mineBreaks++;
                        }

                        prevProgress = progress;
                        progress = progress + this.getBlockStrength(this.mc.world.getBlockState(minePosition), minePosition);
                    } else {
                        progress = 0.0F;
                        prevProgress = 0.0F;
                    }

                    if (!((Mode) this.mode.get()).equals(Mode.Damage)
                        && this.rotate.get()
                        && progress > 0.95
                        && minePosition != null
                        && this.mc.player != null) {
                        float[] var5 = calcAngle(this.mc.player.getEyePos(), minePosition.toCenterPos());
                    }
                } else if (this.mode.get() == Mode.GrimInstant) {
                    if (minePosition != null && PlayerUtility.squaredDistanceFromEyes(minePosition.toCenterPos()) > Math.pow(this.range.get(), 2.0)) {
                        this.reset();
                        return;
                    }

                    if (minePosition != null) {
                        if (this.mc.world.isAir(minePosition)) {
                            return;
                        }

                        int invPickSlotx = this.getTool(minePosition);
                        int hotBarPickSlotx = InventoryUtility.getPickAxeHotbar().slot();
                        int prevSlotx = -1;
                        if (invPickSlotx == -1 && this.switchMode.get() == SwitchMode.Alternative) {
                            return;
                        }

                        if (hotBarPickSlotx == -1 && this.switchMode.get() != SwitchMode.Alternative) {
                            return;
                        }

                        if (progress >= 1.0F) {
                            if (this.switchMode.get() == SwitchMode.Alternative) {
                                this.mc
                                    .interactionManager
                                    .clickSlot(
                                        this.mc.player.currentScreenHandler.syncId,
                                        invPickSlotx < 9 ? invPickSlotx + 36 : invPickSlotx,
                                        this.mc.player.getInventory().selectedSlot,
                                        SlotActionType.SWAP,
                                        this.mc.player
                                    );
                                this.closeScreen();
                            } else if (this.switchMode.get() == SwitchMode.Normal || this.switchMode.get() == SwitchMode.Silent) {
                                prevSlotx = this.mc.player.getInventory().selectedSlot;
                                InventoryUtility.getPickAxeHotbar().switchTo();
                            }

                            this.sendPacket(new PlayerActionC2SPacket(Action.STOP_DESTROY_BLOCK, minePosition, this.mineFacing));
                            if (this.switchMode.get() == SwitchMode.Alternative) {
                                this.mc
                                    .interactionManager
                                    .clickSlot(
                                        this.mc.player.currentScreenHandler.syncId,
                                        invPickSlotx < 9 ? invPickSlotx + 36 : invPickSlotx,
                                        this.mc.player.getInventory().selectedSlot,
                                        SlotActionType.SWAP,
                                        this.mc.player
                                    );
                                this.closeScreen();
                            } else if (this.switchMode.get() == SwitchMode.Silent) {
                                InventoryUtility.switchTo(prevSlotx);
                            }

                            progress = 0.0F;
                            this.mineBreaks++;
                        }

                        prevProgress = progress;
                        progress = (float) (
                            progress
                                + this.getBlockStrength(this.mc.world.getBlockState(minePosition), minePosition)
                                * (this.mineBreaks >= 1 ? this.rebreakfactor.get() : 1.0)
                        );
                    } else {
                        progress = 0.0F;
                        prevProgress = 0.0F;
                    }
                }
            }
        }
    }

    @EventHandler
    public void onRender3D(Render3DEvent event) {
        this.worth = this.checkWorth();
        if (!this.notOnCrystal.get() || !(this.mc.player.getMainHandStack().getItem() instanceof EndCrystalItem)) {
            if (this.mode.get() != Mode.Damage && this.mc.world != null && minePosition != null && !this.mc.world.isAir(minePosition)) {
                switch ((RenderMode) this.renderMode.get()) {
                    case Block:
                        Box renderBox = new Box(minePosition);
                        this.render(event, minePosition, renderBox);
                        Render3DEngine.FILLED_QUEUE
                            .add(
                                new Render3DEngine.FillAction(
                                    renderBox,
                                    this.getColor(
                                        new java.awt.Color(((SettingColor) this.startFillColor.get()).getPacked(), true),
                                        new java.awt.Color(((SettingColor) this.endFillColor.get()).getPacked(), true),
                                        progress
                                    )
                                )
                            );
                        Render3DEngine.OUTLINE_QUEUE
                            .add(
                                new Render3DEngine.OutlineAction(
                                    renderBox,
                                    this.getColor(
                                        new java.awt.Color(((SettingColor) this.startLineColor.get()).getPacked(), true),
                                        new java.awt.Color(((SettingColor) this.startLineColor.get()).getPacked(), true),
                                        progress
                                    ),
                                    (this.lineWidth.get()).intValue()
                                )
                            );
                        break;
                    case Shrink: {
                        Box shrunkMineBox = new Box(
                            minePosition.getX(), minePosition.getY(), minePosition.getZ(), minePosition.getX(), minePosition.getY(), minePosition.getZ()
                        );
                        float noom = (float) MathUtility.clamp(
                            Render2DEngine.interpolate(prevProgress, progress, this.mc.getRenderTickCounter().getTickDelta(true)), 0.0, 1.0
                        );
                        this.render(event, minePosition, shrunkMineBox.shrink(noom, noom, noom).offset(0.5 + noom * 0.5, 0.5 + noom * 0.5, 0.5 + noom * 0.5));
                        Render3DEngine.FILLED_QUEUE
                            .add(
                                new Render3DEngine.FillAction(
                                    shrunkMineBox.shrink(noom, noom, noom).offset(0.5 + noom * 0.5, 0.5 + noom * 0.5, 0.5 + noom * 0.5),
                                    this.getColor(
                                        new java.awt.Color(((SettingColor) this.startFillColor.get()).getPacked(), true),
                                        new java.awt.Color(((SettingColor) this.endFillColor.get()).getPacked(), true),
                                        progress
                                    )
                                )
                            );
                        Render3DEngine.OUTLINE_QUEUE
                            .add(
                                new Render3DEngine.OutlineAction(
                                    shrunkMineBox.shrink(noom, noom, noom).offset(0.5 + noom * 0.5, 0.5 + noom * 0.5, 0.5 + noom * 0.5),
                                    this.getColor(
                                        new java.awt.Color(((SettingColor) this.startLineColor.get()).getPacked(), true),
                                        new java.awt.Color(((SettingColor) this.endLineColor.get()).getPacked(), true),
                                        progress
                                    ),
                                    (this.lineWidth.get()).intValue()
                                )
                            );
                        break;
                    }
                    case Grow: {
                        float noom = (float) MathUtility.clamp(
                            Render2DEngine.interpolate(prevProgress, progress, this.mc.getRenderTickCounter().getTickDelta(true)), 0.0, 1.0
                        );
                        Box shrunkMineBox = new Box(
                            minePosition.getX(), minePosition.getY(), minePosition.getZ(), minePosition.getX() + 1, minePosition.getY() + noom, minePosition.getZ() + 1
                        );
                        this.render(event, minePosition, shrunkMineBox);
                        Render3DEngine.FILLED_QUEUE
                            .add(
                                new Render3DEngine.FillAction(
                                    shrunkMineBox,
                                    this.getColor(
                                        new java.awt.Color(((SettingColor) this.startFillColor.get()).getPacked(), true),
                                        new java.awt.Color(((SettingColor) this.endFillColor.get()).getPacked(), true),
                                        progress
                                    )
                                )
                            );
                        Render3DEngine.OUTLINE_QUEUE
                            .add(
                                new Render3DEngine.OutlineAction(
                                    shrunkMineBox,
                                    this.getColor(
                                        new java.awt.Color(((SettingColor) this.startLineColor.get()).getPacked(), true),
                                        new java.awt.Color(((SettingColor) this.startLineColor.get()).getPacked(), true),
                                        progress
                                    ),
                                    (this.lineWidth.get()).intValue()
                                )
                            );
                    }
                }
            }
        }
    }

    private void render(Render3DEvent event, BlockPos bp, Box box) {
        event.renderer
            .box(
                bp.getX() + box.minX,
                bp.getY() + box.minY,
                bp.getZ() + box.minZ,
                bp.getX() + box.maxX,
                bp.getY() + box.maxY,
                bp.getZ() + box.maxZ,
                this.color,
                this.color,
                (ShapeMode) this.shapeMode.get(),
                0
            );
    }

    @EventHandler
    public void onAttackBlock(@NotNull EventAttackBlock event) {
        if (!this.notOnCrystal.get() || !(this.mc.player.getMainHandStack().getItem() instanceof EndCrystalItem)) {
            if (this.mc.player != null
                && this.canBreak(event.getBlockPos())
                && !this.mc.player.getAbilities().creativeMode
                && (this.mode.get() == Mode.Packet || this.mode.get() == Mode.GrimInstant)
                && !event.getBlockPos().equals(minePosition)) {
                this.addBlockToMine(event.getBlockPos(), event.getEnumFacing(), true);
            }
        }
    }

    @EventHandler(
        priority = -100
    )
    private void onSync(TickEvent event) {
        if (!this.notOnCrystal.get() || !(this.mc.player.getMainHandStack().getItem() instanceof EndCrystalItem)) {
            if (this.rotate.get() && progress > 0.95 && minePosition != null && this.mc.player != null) {
                float[] angle = calcAngle(this.mc.player.getEyePos(), minePosition.toCenterPos());
                this.mc.player.setYaw(angle[0]);
                this.mc.player.setPitch(angle[1]);
            }
        }
    }

    @EventHandler
    private void onPacketSend(Send e) {
        if (!this.notOnCrystal.get() || !(this.mc.player.getMainHandStack().getItem() instanceof EndCrystalItem)) {
            if (e.packet instanceof UpdateSelectedSlotC2SPacket
                && this.resetOnSwitch.get()
                && !((SwitchMode) this.switchMode.get()).equals(SwitchMode.Silent)
                && !((Mode) this.mode.get()).equals(Mode.GrimInstant)) {
                this.addBlockToMine(minePosition, this.mineFacing, true);
            }
        }
    }

    private void reset() {
        minePosition = null;
        this.mineFacing = null;
        progress = 0.0F;
        this.mineBreaks = 0;
        prevProgress = 0.0F;
    }

    private void closeScreen() {
        if (this.mc.player != null) {
            this.sendPacket(new CloseHandledScreenC2SPacket(this.mc.player.currentScreenHandler.syncId));
        }
    }

    private boolean checkWorth() {
        return this.checkWorth(7.5F, minePosition);
    }

    public boolean checkWorth(float damage, BlockPos pos) {
        return true;
    }

    private float getBlockStrength(@NotNull BlockState state, BlockPos position) {
        if (state == Blocks.AIR.getDefaultState()) {
            return 0.02F;
        } else {
            float hardness = state.getHardness(this.mc.world, position);
            return hardness < 0.0F ? 0.0F : this.getDigSpeed(state, position) / hardness / (this.canBreak(position) ? 30.0F : 100.0F);
        }
    }

    private float getDestroySpeed(BlockPos position, BlockState state) {
        float destroySpeed = 1.0F;
        int slot = this.getTool(position);
        if (this.mc.player == null) {
            return 0.0F;
        } else {
            if (slot != -1 && this.mc.player.getInventory().getStack(slot) != null && !this.mc.player.getInventory().getStack(slot).isEmpty()) {
                destroySpeed *= this.mc.player.getInventory().getStack(slot).getMiningSpeedMultiplier(state);
            }

            return destroySpeed;
        }
    }

    public float getDigSpeed(BlockState state, BlockPos position) {
        if (this.mc.player == null) {
            return 0.0F;
        } else {
            float digSpeed = this.getDestroySpeed(position, state);
            if (digSpeed > 1.0F) {
                int slot = this.getTool(position);
                if (slot != -1) {
                    ItemStack itemstack = this.mc.player.getInventory().getStack(slot);
                    int efficiencyModifier = EnchantmentHelper.getLevel(
                        (RegistryEntry) this.mc.world.getRegistryManager().get(Enchantments.PROTECTION.getRegistryRef()).getEntry(Enchantments.EFFICIENCY).get(), itemstack
                    );
                    if (efficiencyModifier > 0 && !itemstack.isEmpty()) {
                        digSpeed += (float) (StrictMath.pow(efficiencyModifier, 2.0) + 1.0);
                    }
                }
            }

            if (this.mc.player.hasStatusEffect(StatusEffects.HASTE)) {
                digSpeed *= 1.0F + (Objects.requireNonNull(this.mc.player.getStatusEffect(StatusEffects.HASTE)).getAmplifier() + 1) * 0.2F;
            }

            if (this.mc.player.hasStatusEffect(StatusEffects.MINING_FATIGUE)) {
                digSpeed *= (float) Math.pow(0.3F, Objects.requireNonNull(this.mc.player.getStatusEffect(StatusEffects.MINING_FATIGUE)).getAmplifier() + 1);
            }

            if (this.mc.player.isSubmergedInWater()) {
                digSpeed *= (float) this.mc.player.getAttributeInstance(EntityAttributes.PLAYER_SUBMERGED_MINING_SPEED).getValue();
            }

            if (!this.mc.player.isOnGround()) {
                digSpeed /= 5.0F;
            }

            return digSpeed < 0.0F ? 0.0F : (float) (digSpeed * this.factor.get());
        }
    }

    private int getTool(BlockPos pos) {
        int index = -1;
        float currentFastest = 1.0F;
        if (this.mc.world != null && this.mc.player != null && !(this.mc.world.getBlockState(pos).getBlock() instanceof AirBlock)) {
            for (int i = 9; i < 45; i++) {
                ItemStack stack = this.mc.player.getInventory().getStack(i >= 36 ? i - 36 : i);
                if (stack != ItemStack.EMPTY && stack.getMaxDamage() - stack.getDamage() > 10) {
                    float digSpeed = this.getDigSpeed(this.mc.world.getBlockState(pos), pos);
                    float destroySpeed = stack.getMiningSpeedMultiplier(this.mc.world.getBlockState(pos));
                    if (digSpeed + destroySpeed > currentFastest) {
                        currentFastest = digSpeed + destroySpeed;
                        index = i;
                    }
                }
            }

            return index >= 36 ? index - 36 : index;
        } else {
            return -1;
        }
    }

    private boolean canBreak(BlockPos pos) {
        if (this.mc.world == null) {
            return false;
        } else {
            BlockState blockState = this.mc.world.getBlockState(pos);
            Block block = blockState.getBlock();
            return block.getHardness() != -1.0F;
        }
    }

    @NotNull
    private java.awt.Color getColor(@NotNull java.awt.Color startColor, @NotNull java.awt.Color endColor, float progress) {
        if (!this.smooth.get()) {
            return progress >= 0.95 ? endColor : startColor;
        } else {
            int rDiff = endColor.getRed() - startColor.getRed();
            int gDiff = endColor.getGreen() - startColor.getGreen();
            int bDiff = endColor.getBlue() - startColor.getBlue();
            int aDiff = endColor.getAlpha() - startColor.getAlpha();
            return new java.awt.Color(
                this.fixColorValue(startColor.getRed() + (int) (rDiff * progress)),
                this.fixColorValue(startColor.getGreen() + (int) (gDiff * progress)),
                this.fixColorValue(startColor.getBlue() + (int) (bDiff * progress)),
                this.fixColorValue(startColor.getAlpha() + (int) (aDiff * progress))
            );
        }
    }

    private int fixColorValue(int colorVal) {
        return colorVal > 255 ? 255 : Math.max(colorVal, 0);
    }

    public void addBlockToMine(BlockPos pos, @Nullable Direction facing, boolean allowReMine) {
        if (allowReMine || minePosition == null && progress == 0.0F) {
            if (this.mc.player != null) {
                progress = 0.0F;
                this.mineBreaks = 0;
                minePosition = pos;
                this.mineFacing = facing == null ? this.mc.player.getHorizontalFacing() : facing;
                if (pos != null && this.mineFacing != null) {
                    this.sendPacket(new PlayerActionC2SPacket(Action.START_DESTROY_BLOCK, pos, this.mineFacing));
                    this.sendPacket(
                        new PlayerActionC2SPacket(
                            this.startMode.get() == StartMode.StartAbort ? Action.ABORT_DESTROY_BLOCK : Action.STOP_DESTROY_BLOCK, minePosition, this.mineFacing
                        )
                    );
                }
            }
        }
    }

    public static enum Mode {
        Packet,
        GrimInstant,
        Damage;
    }

    public static enum RenderMode {
        Block,
        Shrink,
        Grow;
    }

    public static enum StartMode {
        StartAbort,
        StartStop;
    }

    public static enum SwitchMode {
        Silent,
        Normal,
        Alternative;
    }
}
