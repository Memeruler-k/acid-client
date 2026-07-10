package dev.abstr3act.addon.modules.Amrita.scaffold3;

import dev.abstr3act.addon.Compassion;
import dev.abstr3act.addon.events.EventUpdate;
import dev.abstr3act.addon.module.AmritaModule;
import dev.abstr3act.addon.setting.FloatSetting;
import dev.abstr3act.addon.utils.MovementUtil;
import dev.abstr3act.addon.utils.RotationUtil;
import dev.abstr3act.addon.utils.TargetUtil;
import dev.abstr3act.addon.utils.TimerUtil;
import meteordevelopment.meteorclient.events.world.TickEvent.Post;
import meteordevelopment.meteorclient.settings.BoolSetting.Builder;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.*;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.PlayerHeadItem;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.Mutable;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Scaffold extends AmritaModule {
    private final SettingGroup sgGeneral = this.settings.createGroup("General");
    public final Setting<Boolean> tower = this.sgGeneral
        .add(((Builder) ((Builder) ((Builder) new Builder().name("tower")).description("Enable tower.")).defaultValue(true)).build());
    public final Setting<Boolean> towerCenter = this.sgGeneral
        .add(
            ((Builder) ((Builder) ((Builder) ((Builder) new Builder().name("tower-center")).description("Center when tower is active.")).defaultValue(false))
                .visible(this.tower::get))
                .build()
        );
    public final Setting<TowerWhen> towerWhen = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) new meteordevelopment.meteorclient.settings.EnumSetting.Builder()
                .name("tower-when"))
                .description("When to tower."))
                .defaultValue(TowerWhen.Always))
                .visible(this.tower::get))
                .build()
        );
    public final Setting<TowerMode> towerMode = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) new meteordevelopment.meteorclient.settings.EnumSetting.Builder()
                .name("tower-mode"))
                .description("Tower mode."))
                .defaultValue(TowerMode.Vanilla))
                .visible(this.tower::get))
                .build()
        );
    public final Setting<Float> towerSpeed = this.sgGeneral
        .add(
            ((FloatSetting.Builder) ((FloatSetting.Builder) ((FloatSetting.Builder) new FloatSetting.Builder().name("tower-speed")).description("Speed of tower."))
                .defaultValue(1.0F)
                .min(0.1F)
                .max(1.0F)
                .visible(() -> this.tower.get() && ((TowerMode) this.towerMode.get()).equals(TowerMode.Vanilla)))
                .build()
        );
    public final Setting<RotationMode> rotationMode = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) new meteordevelopment.meteorclient.settings.EnumSetting.Builder()
                .name("rotation-mode"))
                .description("Rotation mode."))
                .defaultValue(RotationMode.Math))
                .build()
        );
    public final Setting<Boolean> hypixelMode = this.sgGeneral
        .add(
            ((Builder) ((Builder) ((Builder) ((Builder) new Builder().name("hypixel-mode")).description("Enable Hypixel mode.")).defaultValue(false))
                .visible(() -> ((RotationMode) this.rotationMode.get()).equals(RotationMode.Math)))
                .build()
        );
    public final Setting<Double> rotationSpeed = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("rotation-speed"))
                .description("Speed of rotation."))
                .defaultValue(40.0)
                .min(0.0)
                .max(180.0)
                .visible(
                    () -> ((RotationMode) this.rotationMode.get()).equals(RotationMode.Math)
                        || ((RotationMode) this.rotationMode.get()).equals(RotationMode.Simple)
                        || ((RotationMode) this.rotationMode.get()).equals(RotationMode.Hypixel)
                ))
                .build()
        );
    public final Setting<Boolean> randomizedRotation = this.sgGeneral
        .add(
            ((Builder) ((Builder) ((Builder) ((Builder) new Builder().name("randomized-rotation")).description("Enable randomized rotation.")).defaultValue(true))
                .visible(() -> ((RotationMode) this.rotationMode.get()).equals(RotationMode.Math)))
                .build()
        );
    public final Setting<Double> randomTurnSpeed = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("random-turn-speed"))
                .description("Speed of random turn."))
                .defaultValue(15.0)
                .min(0.0)
                .max(20.0)
                .visible(() -> ((RotationMode) this.rotationMode.get()).equals(RotationMode.Math) && this.randomizedRotation.get()))
                .build()
        );
    public final Setting<Integer> searchRange = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("search-range"))
                .description("Range for searching."))
                .defaultValue(2))
                .min(0)
                .max(5)
                .build()
        );
    public final Setting<Boolean> customTimer = this.sgGeneral
        .add(((Builder) ((Builder) ((Builder) new Builder().name("custom-timer")).description("Enable custom timer.")).defaultValue(false)).build());
    public final Setting<Double> towerTimerSpeed = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("tower-timer-speed"))
                .description("Speed of tower timer."))
                .defaultValue(1.5)
                .min(0.1F)
                .max(10.0)
                .visible(() -> this.customTimer.get() && this.tower.get()))
                .build()
        );
    public final Setting<Double> normalTimerSpeed = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("normal-timer-speed"))
                .description("Speed of normal timer."))
                .defaultValue(1.5)
                .min(0.1F)
                .max(10.0)
                .visible(this.customTimer::get))
                .build()
        );
    public final Setting<Boolean> keepY = this.sgGeneral
        .add(((Builder) ((Builder) ((Builder) new Builder().name("keep-y")).description("Keep Y position.")).defaultValue(true)).build());
    public final Setting<Boolean> keepYOnlySpeed = this.sgGeneral
        .add(
            ((Builder) ((Builder) ((Builder) ((Builder) new Builder().name("keep-y-only-speed")).description("Only keep Y when speed is active.")).defaultValue(true))
                .visible(this.keepY::get))
                .build()
        );
    public final Setting<Boolean> intelligentPicker = this.sgGeneral
        .add(((Builder) ((Builder) ((Builder) new Builder().name("intelligent-picker")).description("Enable intelligent picker.")).defaultValue(true)).build());
    public final Setting<Boolean> noSprint = this.sgGeneral
        .add(((Builder) ((Builder) ((Builder) new Builder().name("no-sprint")).description("Disable sprinting.")).defaultValue(false)).build());
    public final Setting<Boolean> jump = this.sgGeneral
        .add(((Builder) ((Builder) ((Builder) new Builder().name("Jump")).description("Move downward.")).defaultValue(true)).build());
    public final Setting<Boolean> down = this.sgGeneral
        .add(((Builder) ((Builder) ((Builder) new Builder().name("down")).description("Move downward.")).defaultValue(true)).build());
    public int hotbarStackSize = 0;
    public boolean jumped = false;
    public boolean wasTowering = false;
    public boolean startedScaffold = false;
    public Integer lastSlot = null;
    public int spoofTick = 0;
    private boolean firstJumped = false;
    private boolean once = false;
    private boolean hypBasePlaced = false;
    private int hypTowerTicks = 0;
    private double jumpGround = 0.0;
    private boolean checkGround = false;
    private Integer launchY = null;
    private boolean hypStartIsAllowed = false;

    public Scaffold() {
        super(Compassion.AMRITA, "SiMaScaffold", "傻逼");
    }

    public void onDeactivate() {
        if (this.mc.player != null && this.mc.world != null) {
            if (this.wasTowering) {
                switch ((TowerMode) this.towerMode.get()) {
                    case Vanilla:
                    case NCP:
                        if (this.hasAroundBlock()) {
                            MovementUtil.setVelocityY(-0.4);
                        }
                    case Matrix:
                    case Vulcan:
                    default:
                        break;
                    case Hypixel:
                        this.launchY = this.mc.player.getBlockPos().getY() - 1;
                }
            }

            if (this.once) {
                RotationUtil.reset();
                TimerUtil.reset();
                TargetUtil.noKillAura = false;
                if (this.lastSlot != null) {
                    this.mc.player.getInventory().selectedSlot = this.lastSlot;
                    this.spoofTick = 160;
                }
            }
        }

        this.firstJumped = false;
        this.jumped = false;
        this.once = false;
        this.hypStartIsAllowed = false;
        this.hypBasePlaced = false;
        this.hypTowerTicks = 0;
        this.jumpGround = 0.0;
        this.wasTowering = false;
        this.checkGround = false;
        this.launchY = null;
        this.startedScaffold = false;
    }

    public void onActivate() {
        if (this.mc.player != null && this.mc.world != null) {
            this.launchY = this.mc.player.getBlockPos().getY();
            this.lastSlot = this.mc.player.getInventory().selectedSlot;
        }
    }

    @EventHandler
    public void onTick(Post event) {
        if (this.mc.player != null && this.mc.world != null) {
            if (this.mc.player.isOnGround() && this.jump.get()) {
                this.mc.player.jump();
            }

            if (this.down.get()) {
                this.mc.options.sneakKey.setPressed(false);
            }

            if (!this.jumped
                && this.mc.player.isOnGround()
                && (
                ((RotationMode) this.rotationMode.get()).equals(RotationMode.Math) && this.hypixelMode.get()
                    || ((RotationMode) this.rotationMode.get()).equals(RotationMode.Hypixel)
            )) {
                this.mc.options.jumpKey.setPressed(false);
            }

            if (this.wasTowering) {
                this.mc.options.jumpKey.setPressed(false);
            }

            if (!this.wasTowering
                && InputUtil.isKeyPressed(this.mc.getWindow().getHandle(), InputUtil.fromTranslationKey(this.mc.options.jumpKey.getBoundKeyTranslationKey()).getCode())
            ) {
                this.mc.options.jumpKey.setPressed(false);
            }
        }
    }

    private void countBlockItems(PlayerInventory inventory) {
        this.hotbarStackSize = 0;

        for (int i = 0; i <= 8; i++) {
            ItemStack stack = inventory.getStack(i);
            if (!stack.isEmpty() && stack.getItem() instanceof BlockItem) {
                Block block = ((BlockItem) stack.getItem()).getBlock();
                if (!block.getDefaultState().isAir()
                    && !(block instanceof ChestBlock)
                    && !(block instanceof CobwebBlock)
                    && !(block instanceof CakeBlock)
                    && !(block instanceof CandleCakeBlock)
                    && !(block instanceof BrewingStandBlock)
                    && !(block instanceof EnderChestBlock)
                    && !(block instanceof ShulkerBoxBlock)
                    && !(block instanceof FurnaceBlock)
                    && !(block instanceof CraftingTableBlock)
                    && !(block instanceof CrafterBlock)
                    && !(block instanceof SmokerBlock)
                    && !(block instanceof BlastFurnaceBlock)
                    && !(block instanceof CartographyTableBlock)
                    && !(block instanceof AnvilBlock)
                    && !(block instanceof BellBlock)
                    && !(block instanceof BeaconBlock)
                    && !(block instanceof DragonEggBlock)
                    && !(block instanceof LeverBlock)
                    && !(block instanceof EnchantingTableBlock)
                    && !(block instanceof ButtonBlock)
                    && !(block instanceof GrindstoneBlock)
                    && !(block instanceof LoomBlock)
                    && !(block instanceof NoteBlock)
                    && !(block instanceof FenceGateBlock)
                    && !(block instanceof DoorBlock)
                    && !(block instanceof TrapdoorBlock)
                    && !(block instanceof StonecutterBlock)
                    && !(block instanceof SignBlock)
                    && !(block instanceof WallSignBlock)
                    && !(block instanceof HangingSignBlock)
                    && !(block instanceof WallHangingSignBlock)
                    && !(block instanceof RepeaterBlock)
                    && !(block instanceof ComparatorBlock)
                    && !(block instanceof DispenserBlock)
                    && !(block instanceof JigsawBlock)
                    && !(block instanceof CommandBlock)
                    && !(block instanceof StructureBlock)
                    && !(block instanceof HopperBlock)
                    && !(block instanceof BedBlock)
                    && !(block instanceof FenceBlock)
                    && !(block instanceof SlabBlock)
                    && !(block instanceof PressurePlateBlock)
                    && !(block instanceof WallBlock)
                    && !(block instanceof StairsBlock)
                    && !(block instanceof LadderBlock)
                    && !(block instanceof ChainBlock)
                    && !(block instanceof CarpetBlock)
                    && !(block instanceof BarrelBlock)
                    && !(block instanceof RailBlock)
                    && !(block instanceof PoweredRailBlock)
                    && !(block instanceof DetectorRailBlock)
                    && !(block.asItem() instanceof PlayerHeadItem)
                    && !(block instanceof MushroomPlantBlock)
                    && block.asItem() != Items.TORCH
                    && block.asItem() != Items.REDSTONE
                    && block.asItem() != Items.REDSTONE_TORCH
                    && block.asItem() != Items.STRING) {
                    this.hotbarStackSize = this.hotbarStackSize + stack.getCount();
                }
            }
        }
    }

    public String getInfoString() {
        return ((TowerMode) this.towerMode.get()).name();
    }

    @EventHandler
    public void onUpdate(EventUpdate event) {
        if (this.mc.player != null && this.mc.world != null) {
            this.countBlockItems(this.mc.player.getInventory());
            if (this.hotbarStackSize <= 0) {
                if (this.wasTowering) {
                    switch ((TowerMode) this.towerMode.get()) {
                        case Vanilla:
                        case NCP:
                            if (this.hasAroundBlock()) {
                                MovementUtil.setVelocityY(-0.4);
                            }
                        case Matrix:
                        case Vulcan:
                        default:
                            break;
                        case Hypixel:
                            this.launchY = this.mc.player.getBlockPos().getY() - 1;
                    }
                }

                this.firstJumped = false;
                this.jumped = false;
                this.hypBasePlaced = false;
                this.hypTowerTicks = 0;
                this.jumpGround = 0.0;
                this.wasTowering = false;
                this.checkGround = false;
                if (this.once) {
                    RotationUtil.reset();
                    TimerUtil.reset();
                    TargetUtil.noKillAura = false;
                    if (this.lastSlot != null) {
                        this.mc.player.getInventory().selectedSlot = this.lastSlot;
                        this.spoofTick = 160;
                    }
                }

                this.once = false;
            } else {
                this.once = true;
                if (this.noSprint.get()) {
                    this.mc.player.setSprinting(false);
                }

                if (MovementUtil.isDiagonal(40.0F)
                    && (
                    ((RotationMode) this.rotationMode.get()).equals(RotationMode.Hypixel)
                        || ((RotationMode) this.rotationMode.get()).equals(RotationMode.Math) && this.hypixelMode.get()
                )) {
                    MovementUtil.strafe(0.03F);
                }

                if (!this.jumped
                    && (
                    ((RotationMode) this.rotationMode.get()).equals(RotationMode.Math) && this.hypixelMode.get()
                        || ((RotationMode) this.rotationMode.get()).equals(RotationMode.Hypixel)
                )) {
                    if (!this.mc.player.isOnGround() && MovementUtil.fallTicks >= 7 && this.firstJumped) {
                        this.launchY = this.mc.player.getBlockPos().getY();
                        this.jumped = true;
                    }

                    if (!this.firstJumped && this.mc.player.isOnGround()) {
                        this.mc.player.jump();
                        this.firstJumped = true;
                    }
                }

                boolean hypixelPlaced = false;
                if (this.mc.player.isOnGround()) {
                    this.hypTowerTicks = 0;
                } else {
                    this.hypTowerTicks++;
                }

                if (this.customTimer.get()) {
                    TimerUtil.timerSpeed = this.wasTowering ? (this.towerTimerSpeed.get()).floatValue() : (this.normalTimerSpeed.get()).floatValue();
                }

                if (this.tower.get()
                    && (
                    !((TowerMode) this.towerMode.get()).equals(TowerMode.Hypixel)
                        || !this.mc.player.hasStatusEffect(StatusEffects.JUMP_BOOST)
                        || this.mc.player.hasStatusEffect(StatusEffects.SPEED)
                )
                    && (
                    ((TowerWhen) this.towerWhen.get()).equals(TowerWhen.Always)
                        || ((TowerWhen) this.towerWhen.get()).equals(TowerWhen.Standing)
                )
                    && !MovementUtil.isMoving()
                    || ((TowerWhen) this.towerWhen.get()).equals(TowerWhen.Moving) && MovementUtil.isMoving()) {
                    if (InputUtil.isKeyPressed(
                        this.mc.getWindow().getHandle(), InputUtil.fromTranslationKey(this.mc.options.jumpKey.getBoundKeyTranslationKey()).getCode()
                    )) {
                        if (!((TowerMode) this.towerMode.get()).equals(TowerMode.Hypixel) || this.mc.player.isOnGround()) {
                            this.wasTowering = true;
                        }

                        if (this.hasAroundBlock() && this.wasTowering) {
                            if (((TowerWhen) this.towerWhen.get()).equals(TowerWhen.Always) && !MovementUtil.isMoving()
                                || ((TowerWhen) this.towerWhen.get()).equals(TowerWhen.Standing)) {
                                MovementUtil.stopMoving();
                            }

                            if (this.towerCenter.get() && !MovementUtil.isMoving()) {
                                this.mc.player.setPosition(this.mc.player.getBlockPos().getX() + 0.5, this.mc.player.getY(), this.mc.player.getBlockPos().getZ() + 0.5);
                            }

                            switch ((TowerMode) this.towerMode.get()) {
                                case Vanilla:
                                    if (MovementUtil.isMoving() && ((Float) this.towerSpeed.get()).floatValue() >= 0.6) {
                                        MovementUtil.setVelocityY(0.5);
                                    } else {
                                        MovementUtil.setVelocityY(((Float) this.towerSpeed.get()).doubleValue());
                                    }
                                    break;
                                case NCP:
                                    if (this.mc.player.isOnGround()) {
                                        this.jumpGround = this.mc.player.getY();
                                        MovementUtil.setVelocityY(0.42);
                                    }

                                    if (this.mc.player.getY() > this.jumpGround + 0.79F) {
                                        this.mc.player.setPosition(this.mc.player.getX(), (int) this.mc.player.getY(), this.mc.player.getZ());
                                        MovementUtil.setVelocityY(0.42);
                                        this.jumpGround = this.mc.player.getY();
                                    }
                                case Matrix:
                                case Vulcan:
                                default:
                                    break;
                                case Hypixel:
                                    if (MovementUtil.isMoving()) {
                                        if (this.mc.player.isOnGround()) {
                                            this.checkGround = true;
                                        }

                                        if (this.checkGround) {
                                            if (MovementUtil.fallTicks >= 18) {
                                                this.wasTowering = false;
                                                hypixelPlaced = true;
                                            } else {
                                                switch (MovementUtil.fallTicks % 3) {
                                                    case 0:
                                                        if (this.mc.player.hasStatusEffect(StatusEffects.SPEED)) {
                                                            MovementUtil.strafe(
                                                                0.22F
                                                                    + (this.mc.player.getStatusEffect(StatusEffects.SPEED).getAmplifier() + 1)
                                                                    * (this.mc.player.getStatusEffect(StatusEffects.SPEED).getAmplifier() == 0 ? 0.036F : 0.042F)
                                                            );
                                                        } else {
                                                            MovementUtil.strafe(0.22F);
                                                        }

                                                        MovementUtil.setVelocityY(0.42);
                                                        break;
                                                    case 1:
                                                        MovementUtil.setVelocityY(0.33);
                                                        break;
                                                    case 2:
                                                        MovementUtil.setVelocityY(this.mc.player.getBlockPos().getY() + 1.0 - this.mc.player.getY());
                                                }
                                            }
                                        }
                                    }
                            }
                        } else if (this.wasTowering) {
                            switch ((TowerMode) this.towerMode.get()) {
                                case Vanilla:
                                case NCP:
                                    if (this.hasAroundBlock()) {
                                        MovementUtil.setVelocityY(-0.4);
                                    }
                                case Matrix:
                                case Vulcan:
                                default:
                                    break;
                                case Hypixel:
                                    this.launchY = this.mc.player.getBlockPos().getY() - 1;
                            }
                        }
                    } else if (this.wasTowering) {
                        switch ((TowerMode) this.towerMode.get()) {
                            case Vanilla:
                            case NCP:
                                if (this.hasAroundBlock()) {
                                    MovementUtil.setVelocityY(-0.4);
                                }
                            case Matrix:
                            case Vulcan:
                            default:
                                break;
                            case Hypixel:
                                this.launchY = this.mc.player.getBlockPos().getY() - 1;
                        }

                        this.hypBasePlaced = false;
                        this.hypTowerTicks = 0;
                        this.jumpGround = 0.0;
                        this.wasTowering = false;
                    }
                } else if (this.wasTowering) {
                    switch ((TowerMode) this.towerMode.get()) {
                        case Vanilla:
                        case NCP:
                            if (this.hasAroundBlock()) {
                                MovementUtil.setVelocityY(-0.4);
                            }
                        case Matrix:
                        case Vulcan:
                        default:
                            break;
                        case Hypixel:
                            this.launchY = this.mc.player.getBlockPos().getY() - 1;
                    }

                    this.hypBasePlaced = false;
                    this.hypTowerTicks = 0;
                    this.jumpGround = 0.0;
                    this.wasTowering = false;
                }

                if (this.wasTowering) {
                    this.startedScaffold = true;
                }

                if (this.keepY.get()) {
                    if (this.keepYOnlySpeed.get()
                        && InputUtil.isKeyPressed(
                        this.mc.getWindow().getHandle(), InputUtil.fromTranslationKey(this.mc.options.jumpKey.getBoundKeyTranslationKey()).getCode()
                    )
                        && (this.wasTowering || MovementUtil.fallTicks >= 4)) {
                        this.launchY = this.mc.player.getBlockPos().getY();
                    }
                } else if (this.wasTowering || MovementUtil.fallTicks >= 4) {
                    this.launchY = this.mc.player.getBlockPos().getY();
                }

                if (this.mc.player.squaredDistanceTo(new Vec3d(this.mc.player.getX(), this.launchY.intValue(), this.mc.player.getZ())) > 15.0) {
                    this.launchY = this.mc.player.getBlockPos().getY();
                }

                if (this.down.get()
                    && InputUtil.isKeyPressed(
                    this.mc.getWindow().getHandle(), InputUtil.fromTranslationKey(this.mc.options.sneakKey.getBoundKeyTranslationKey()).getCode()
                )
                    && !this.wasTowering) {
                    this.launchY = this.mc.player.getBlockPos().getY() - 1;
                }

                Mutable targetBlock = new Mutable(this.mc.player.getBlockPos().getX(), this.launchY.intValue() - 0.75, this.mc.player.getBlockPos().getZ());
                if (((RotationMode) this.rotationMode.get()).equals(RotationMode.Simple)
                    || ((RotationMode) this.rotationMode.get()).equals(RotationMode.Hypixel)
                    || !((RotationMode) this.rotationMode.get()).equals(RotationMode.None) && !this.startedScaffold) {
                    float direction = MovementUtil.getPlayerDirection() - 180.0F;
                    float yaw = (direction % 360.0F + 360.0F) % 360.0F;
                    boolean isNorth = yaw < 80.0F || yaw > 280.0F;
                    boolean isSouth = yaw > 100.0F && yaw < 260.0F;
                    boolean isEast = yaw > 10.0F && yaw < 170.0F;
                    boolean isWest = yaw > 190.0F && yaw < 350.0F;
                    float groundYaw;
                    if (((RotationMode) this.rotationMode.get()).equals(RotationMode.Hypixel)
                        || ((RotationMode) this.rotationMode.get()).equals(RotationMode.Math) && this.hypixelMode.get()) {
                        if (!this.mc.world.getBlockState(this.mc.player.getBlockPos().down(1)).isAir()) {
                            groundYaw = direction + 70.0F;
                        } else if (!MovementUtil.isDiagonal(6.0F)) {
                            groundYaw = (direction + 60.0F) % 360.0F;
                        } else if (isNorth && isWest) {
                            groundYaw = 45.0F;
                        } else if (isNorth && isEast) {
                            groundYaw = 135.0F;
                        } else if (isSouth && isEast) {
                            groundYaw = 225.0F;
                        } else if (isSouth && isWest) {
                            groundYaw = 315.0F;
                        } else {
                            groundYaw = (direction + 60.0F) % 360.0F;
                        }
                    } else {
                        groundYaw = direction;
                    }

                    float offGroundYaw;
                    if (((RotationMode) this.rotationMode.get()).equals(RotationMode.Math)
                        || ((RotationMode) this.rotationMode.get()).equals(RotationMode.Hypixel) && this.hypixelMode.get()) {
                        if (!this.mc.world.getBlockState(this.mc.player.getBlockPos().down(1)).isAir()) {
                            offGroundYaw = direction + 70.0F;
                        } else if (!MovementUtil.isDiagonal(6.0F)) {
                            offGroundYaw = (direction + 60.0F) % 360.0F;
                        } else if (isNorth && isWest) {
                            offGroundYaw = 45.0F;
                        } else if (isNorth && isEast) {
                            offGroundYaw = 135.0F;
                        } else if (isSouth && isEast) {
                            offGroundYaw = 225.0F;
                        } else if (isSouth && isWest) {
                            offGroundYaw = 315.0F;
                        } else {
                            offGroundYaw = (direction + 60.0F) % 360.0F;
                        }
                    } else {
                        offGroundYaw = direction;
                    }

                    float groundPitch = !((RotationMode) this.rotationMode.get()).equals(RotationMode.Hypixel)
                        && (!((RotationMode) this.rotationMode.get()).equals(RotationMode.Math) || !this.hypixelMode.get())
                        ? 70.0F
                        : 78.0F;
                    float offGroundPitch = !((RotationMode) this.rotationMode.get()).equals(RotationMode.Hypixel)
                        && (!((RotationMode) this.rotationMode.get()).equals(RotationMode.Math) || !this.hypixelMode.get())
                        ? 80.0F
                        : 88.0F;
                    if (this.mc.player.isOnGround()) {
                        RotationUtil.setRotation(groundYaw, groundPitch, (this.rotationSpeed.get()).floatValue());
                    } else {
                        RotationUtil.setRotation(offGroundYaw, offGroundPitch, (this.rotationSpeed.get()).floatValue());
                    }
                }

                if (this.mc.world.getBlockState(targetBlock).isAir()) {
                    this.startedScaffold = true;
                    if (!hypixelPlaced) {
                        this.placeBlock(targetBlock);
                        this.hypBasePlaced = true;
                    }
                }
            }
        }
    }

    private void placeBlock(BlockPos pos) {
        if (this.mc.world.getBlockState(pos).isAir()) {
            Integer slot = this.findBlockSlot(this.mc.player.getInventory());
            if (slot != null) {
                if (this.mc.player.getInventory().selectedSlot != slot) {
                    this.mc.player.getInventory().selectedSlot = slot;
                }

                if (!this.startedScaffold) {
                    Vec3d hitPos = Vec3d.ofCenter(pos);
                    Direction side = this.getPlaceSide(pos);
                    if (side != null) {
                        BlockPos neighbour;
                        if (side == Direction.UP) {
                            neighbour = pos;
                        } else {
                            neighbour = pos.offset(side);
                            hitPos = hitPos.add(side.getOffsetX() * 0.5, side.getOffsetY() * 0.5, side.getOffsetZ() * 0.5);
                        }

                        BlockHitResult bhr = new BlockHitResult(hitPos, side.getOpposite(), neighbour, false);
                        this.mc
                            .execute(
                                () -> {
                                    try {
                                        if (this.mc.player.getInventory().selectedSlot != slot) {
                                            this.mc.player.getInventory().selectedSlot = slot;
                                        }

                                        if (((RotationMode) this.rotationMode.get()).equals(RotationMode.Math)) {
                                            RotationUtil.scaffoldRotation(
                                                this.hypixelMode.get(),
                                                Vec3d.ofCenter(neighbour),
                                                (this.rotationSpeed.get()).floatValue(),
                                                this.randomizedRotation.get(),
                                                (this.randomTurnSpeed.get()).floatValue(),
                                                50L,
                                                100L
                                            );
                                        }

                                        if (this.mc.interactionManager.interactBlock(this.mc.player, Hand.MAIN_HAND, bhr).isAccepted()) {
                                            this.mc.player.swingHand(Hand.MAIN_HAND);
                                            this.mc.gameRenderer.firstPersonRenderer.resetEquipProgress(Hand.MAIN_HAND);
                                        }
                                    } catch (Exception var5x) {
                                        var5x.printStackTrace();
                                    }
                                }
                            );
                    }
                } else {
                    ArrayList<BlockPos> upperRange = new ArrayList<>(
                        List.of(
                            new BlockPos(0, 1, 0),
                            new BlockPos(1, 1, 0),
                            new BlockPos(0, 1, 1),
                            new BlockPos(-1, 1, 0),
                            new BlockPos(0, 1, -1),
                            new BlockPos(1, 1, -1),
                            new BlockPos(-1, 1, 1),
                            new BlockPos(1, 1, 1),
                            new BlockPos(-1, 1, -1)
                        )
                    );
                    upperRange.sort(
                        Comparator.comparingDouble(
                            a -> this.mc.player.squaredDistanceTo(this.mc.player.getX() + a.getX(), this.mc.player.getY() + a.getY(), this.mc.player.getZ() + a.getZ())
                        )
                    );
                    ArrayList<BlockPos> extendedRange = new ArrayList<>();
                    int range = this.down.get()
                        && InputUtil.isKeyPressed(
                        this.mc.getWindow().getHandle(), InputUtil.fromTranslationKey(this.mc.options.sneakKey.getBoundKeyTranslationKey()).getCode()
                    )
                        ? 1
                        : this.searchRange.get();

                    for (int y : List.of(0, -1)) {
                        for (int x = -range; x <= range; x++) {
                            for (int z = -range; z <= range; z++) {
                                extendedRange.add(new BlockPos(x, y, z));
                            }
                        }
                    }

                    extendedRange.sort(
                        Comparator.comparingDouble(
                            a -> this.mc.player.squaredDistanceTo(this.mc.player.getX() + a.getX(), this.mc.player.getY() + a.getY(), this.mc.player.getZ() + a.getZ())
                        )
                    );

                    for (BlockPos offset : extendedRange) {
                        BlockPos neighbourPos = pos.add(offset);
                        if (!this.mc.world.getBlockState(neighbourPos).isAir()) {
                            Vec3d hitPos = Vec3d.ofCenter(neighbourPos);
                            Direction oppositeDirection = Direction.getFacing(offset.getX(), offset.getY(), offset.getZ()).getOpposite();
                            BlockHitResult bhr = new BlockHitResult(hitPos, oppositeDirection, neighbourPos, false);
                            this.mc
                                .execute(
                                    () -> {
                                        try {
                                            if (this.mc.player.getInventory().selectedSlot != slot) {
                                                this.mc.player.getInventory().selectedSlot = slot;
                                            }

                                            if (((RotationMode) this.rotationMode.get()).equals(RotationMode.Math)) {
                                                RotationUtil.scaffoldRotation(
                                                    this.hypixelMode.get(),
                                                    Vec3d.ofCenter(neighbourPos),
                                                    (this.rotationSpeed.get()).floatValue(),
                                                    this.randomizedRotation.get(),
                                                    (this.randomTurnSpeed.get()).floatValue(),
                                                    50L,
                                                    10L
                                                );
                                            }

                                            if (this.mc.interactionManager.interactBlock(this.mc.player, Hand.MAIN_HAND, bhr).isAccepted()) {
                                                this.mc.player.swingHand(Hand.MAIN_HAND);
                                                this.mc.gameRenderer.firstPersonRenderer.resetEquipProgress(Hand.MAIN_HAND);
                                            }
                                        } catch (Exception var5x) {
                                            var5x.printStackTrace();
                                        }
                                    }
                                );
                            return;
                        }
                    }

                    if (this.down.get()
                        && InputUtil.isKeyPressed(
                        this.mc.getWindow().getHandle(), InputUtil.fromTranslationKey(this.mc.options.sneakKey.getBoundKeyTranslationKey()).getCode()
                    )) {
                        for (BlockPos offsetx : upperRange) {
                            BlockPos neighbourPos = pos.add(offsetx);
                            if (!this.mc.world.getBlockState(neighbourPos).isAir()) {
                                Vec3d hitPos = Vec3d.ofCenter(neighbourPos);
                                Direction oppositeDirection = Direction.getFacing(offsetx.getX(), offsetx.getY(), offsetx.getZ()).getOpposite();
                                BlockHitResult bhr = new BlockHitResult(hitPos, oppositeDirection, neighbourPos, false);
                                this.mc
                                    .execute(
                                        () -> {
                                            try {
                                                if (this.mc.player.getInventory().selectedSlot != slot) {
                                                    this.mc.player.getInventory().selectedSlot = slot;
                                                }

                                                if (((RotationMode) this.rotationMode.get()).equals(RotationMode.Math)) {
                                                    RotationUtil.scaffoldRotation(
                                                        this.hypixelMode.get(),
                                                        Vec3d.ofCenter(neighbourPos),
                                                        (this.rotationSpeed.get()).floatValue(),
                                                        this.randomizedRotation.get(),
                                                        (this.randomTurnSpeed.get()).floatValue(),
                                                        50L,
                                                        100L
                                                    );
                                                }

                                                if (this.mc.interactionManager.interactBlock(this.mc.player, Hand.MAIN_HAND, bhr).isAccepted()) {
                                                    this.mc.player.swingHand(Hand.MAIN_HAND);
                                                    this.mc.gameRenderer.firstPersonRenderer.resetEquipProgress(Hand.MAIN_HAND);
                                                }
                                            } catch (Exception var5x) {
                                                var5x.printStackTrace();
                                            }
                                        }
                                    );
                                return;
                            }
                        }
                    }

                    Vec3d hitPos = Vec3d.ofCenter(pos);
                    Direction side = this.getPlaceSide(pos);
                    if (side != null) {
                        BlockPos neighbour;
                        if (side == Direction.UP) {
                            neighbour = pos;
                        } else {
                            neighbour = pos.offset(side);
                            hitPos = hitPos.add(side.getOffsetX() * 0.5, side.getOffsetY() * 0.5, side.getOffsetZ() * 0.5);
                        }

                        Vec3d playerPos = this.mc.player.getPos();
                        if (playerPos.x % 1.0 == 0.5) {
                            hitPos = hitPos.add(playerPos.x > pos.getX() ? 0.5 : -0.5, 0.0, 0.0);
                        }

                        if (playerPos.z % 1.0 == 0.5) {
                            hitPos = hitPos.add(0.0, 0.0, playerPos.z > pos.getZ() ? 0.5 : -0.5);
                        }

                        BlockHitResult bhr = new BlockHitResult(hitPos, side.getOpposite(), neighbour, false);
                        this.mc
                            .execute(
                                () -> {
                                    try {
                                        if (this.mc.player.getInventory().selectedSlot != slot) {
                                            this.mc.player.getInventory().selectedSlot = slot;
                                        }

                                        if (((RotationMode) this.rotationMode.get()).equals(RotationMode.Math)) {
                                            RotationUtil.scaffoldRotation(
                                                this.hypixelMode.get(),
                                                Vec3d.ofCenter(neighbour),
                                                (this.rotationSpeed.get()).floatValue(),
                                                this.randomizedRotation.get(),
                                                (this.randomTurnSpeed.get()).floatValue(),
                                                50L,
                                                100L
                                            );
                                        }

                                        if (this.mc.interactionManager.interactBlock(this.mc.player, Hand.MAIN_HAND, bhr).isAccepted()) {
                                            this.mc.player.swingHand(Hand.MAIN_HAND);
                                            this.mc.gameRenderer.firstPersonRenderer.resetEquipProgress(Hand.MAIN_HAND);
                                        }
                                    } catch (Exception var5x) {
                                        var5x.printStackTrace();
                                    }
                                }
                            );
                    }
                }
            }
        }
    }

    private Integer findBlockSlot(PlayerInventory inv) {
        Integer maxStackSlot = null;
        int maxStackSize = 0;

        for (int i = 0; i < inv.main.size(); i++) {
            if (i >= 0 && i <= 8) {
                ItemStack stack = inv.getStack(i);
                if (!stack.isEmpty() && stack.getItem() instanceof BlockItem) {
                    Block block = ((BlockItem) stack.getItem()).getBlock();
                    if (!block.getDefaultState().isAir()
                        && !(block instanceof ChestBlock)
                        && !(block instanceof CobwebBlock)
                        && !(block instanceof CakeBlock)
                        && !(block instanceof CandleCakeBlock)
                        && !(block instanceof BrewingStandBlock)
                        && !(block instanceof EnderChestBlock)
                        && !(block instanceof ShulkerBoxBlock)
                        && !(block instanceof FurnaceBlock)
                        && !(block instanceof CraftingTableBlock)
                        && !(block instanceof CrafterBlock)
                        && !(block instanceof SmokerBlock)
                        && !(block instanceof BlastFurnaceBlock)
                        && !(block instanceof CartographyTableBlock)
                        && !(block instanceof AnvilBlock)
                        && !(block instanceof BellBlock)
                        && !(block instanceof BeaconBlock)
                        && !(block instanceof DragonEggBlock)
                        && !(block instanceof LeverBlock)
                        && !(block instanceof EnchantingTableBlock)
                        && !(block instanceof ButtonBlock)
                        && !(block instanceof GrindstoneBlock)
                        && !(block instanceof LoomBlock)
                        && !(block instanceof NoteBlock)
                        && !(block instanceof FenceGateBlock)
                        && !(block instanceof DoorBlock)
                        && !(block instanceof TrapdoorBlock)
                        && !(block instanceof StonecutterBlock)
                        && !(block instanceof SignBlock)
                        && !(block instanceof WallSignBlock)
                        && !(block instanceof HangingSignBlock)
                        && !(block instanceof WallHangingSignBlock)
                        && !(block instanceof RepeaterBlock)
                        && !(block instanceof ComparatorBlock)
                        && !(block instanceof DispenserBlock)
                        && !(block instanceof JigsawBlock)
                        && !(block instanceof CommandBlock)
                        && !(block instanceof StructureBlock)
                        && !(block instanceof HopperBlock)
                        && !(block instanceof BedBlock)
                        && !(block instanceof FenceBlock)
                        && !(block instanceof SlabBlock)
                        && !(block instanceof PressurePlateBlock)
                        && !(block instanceof WallBlock)
                        && !(block instanceof StairsBlock)
                        && !(block instanceof LadderBlock)
                        && !(block instanceof ChainBlock)
                        && !(block instanceof CarpetBlock)
                        && !(block instanceof BarrelBlock)
                        && !(block instanceof RailBlock)
                        && !(block instanceof PoweredRailBlock)
                        && !(block instanceof DetectorRailBlock)
                        && !(block.asItem() instanceof PlayerHeadItem)
                        && !(block instanceof MushroomPlantBlock)
                        && block.asItem() != Items.TORCH
                        && block.asItem() != Items.REDSTONE
                        && block.asItem() != Items.REDSTONE_TORCH
                        && block.asItem() != Items.STRING) {
                        if (!this.intelligentPicker.get()) {
                            return i;
                        }

                        if (stack.getCount() > maxStackSize) {
                            maxStackSize = stack.getCount();
                            maxStackSlot = i;
                        }
                    }
                }
            }
        }

        return maxStackSlot;
    }

    private Direction getPlaceSide(BlockPos blockPos) {
        Vec3d lookVec = blockPos.toCenterPos().subtract(this.mc.player.getEyePos());
        double bestRelevancy = -Double.MAX_VALUE;
        Direction bestSide = null;

        for (Direction side : Direction.values()) {
            BlockPos neighbor = blockPos.offset(side);
            BlockState state = this.mc.world.getBlockState(neighbor);
            if (!state.isAir() && state.getFluidState().isEmpty()) {
                double relevancy = side.getAxis().choose(lookVec.x, lookVec.y, lookVec.z) * side.getDirection().offset();
                if (relevancy > bestRelevancy) {
                    bestRelevancy = relevancy;
                    bestSide = side;
                }
            }
        }

        return bestSide;
    }

    private boolean hasAroundBlock() {
        return this.mc.player != null && this.mc.world != null
            ? !this.mc
            .world
            .getBlockState(new BlockPos(this.mc.player.getBlockPos().getX(), this.mc.player.getBlockPos().getY(), this.mc.player.getBlockPos().getZ()))
            .isAir()
            || !this.mc
            .world
            .getBlockState(new BlockPos(this.mc.player.getBlockPos().getX() + 1, this.mc.player.getBlockPos().getY(), this.mc.player.getBlockPos().getZ()))
            .isAir()
            || !this.mc
            .world
            .getBlockState(new BlockPos(this.mc.player.getBlockPos().getX() - 1, this.mc.player.getBlockPos().getY(), this.mc.player.getBlockPos().getZ()))
            .isAir()
            || !this.mc
            .world
            .getBlockState(new BlockPos(this.mc.player.getBlockPos().getX(), this.mc.player.getBlockPos().getY(), this.mc.player.getBlockPos().getZ() + 1))
            .isAir()
            || !this.mc
            .world
            .getBlockState(new BlockPos(this.mc.player.getBlockPos().getX(), this.mc.player.getBlockPos().getY(), this.mc.player.getBlockPos().getZ() - 1))
            .isAir()
            || !this.mc
            .world
            .getBlockState(new BlockPos(this.mc.player.getBlockPos().getX() + 1, this.mc.player.getBlockPos().getY(), this.mc.player.getBlockPos().getZ() + 1))
            .isAir()
            || !this.mc
            .world
            .getBlockState(new BlockPos(this.mc.player.getBlockPos().getX() + 1, this.mc.player.getBlockPos().getY(), this.mc.player.getBlockPos().getZ() - 1))
            .isAir()
            || !this.mc
            .world
            .getBlockState(new BlockPos(this.mc.player.getBlockPos().getX() - 1, this.mc.player.getBlockPos().getY(), this.mc.player.getBlockPos().getZ() + 1))
            .isAir()
            || !this.mc
            .world
            .getBlockState(new BlockPos(this.mc.player.getBlockPos().getX() - 1, this.mc.player.getBlockPos().getY(), this.mc.player.getBlockPos().getZ() - 1))
            .isAir()
            || !this.mc
            .world
            .getBlockState(new BlockPos(this.mc.player.getBlockPos().getX(), this.mc.player.getBlockPos().getY() - 1, this.mc.player.getBlockPos().getZ()))
            .isAir()
            || !this.mc
            .world
            .getBlockState(new BlockPos(this.mc.player.getBlockPos().getX() + 1, this.mc.player.getBlockPos().getY() - 1, this.mc.player.getBlockPos().getZ()))
            .isAir()
            || !this.mc
            .world
            .getBlockState(new BlockPos(this.mc.player.getBlockPos().getX() - 1, this.mc.player.getBlockPos().getY() - 1, this.mc.player.getBlockPos().getZ()))
            .isAir()
            || !this.mc
            .world
            .getBlockState(new BlockPos(this.mc.player.getBlockPos().getX(), this.mc.player.getBlockPos().getY() - 1, this.mc.player.getBlockPos().getZ() + 1))
            .isAir()
            || !this.mc
            .world
            .getBlockState(new BlockPos(this.mc.player.getBlockPos().getX(), this.mc.player.getBlockPos().getY() - 1, this.mc.player.getBlockPos().getZ() - 1))
            .isAir()
            || !this.mc
            .world
            .getBlockState(
                new BlockPos(this.mc.player.getBlockPos().getX() + 1, this.mc.player.getBlockPos().getY() - 1, this.mc.player.getBlockPos().getZ() + 1)
            )
            .isAir()
            || !this.mc
            .world
            .getBlockState(
                new BlockPos(this.mc.player.getBlockPos().getX() + 1, this.mc.player.getBlockPos().getY() - 1, this.mc.player.getBlockPos().getZ() - 1)
            )
            .isAir()
            || !this.mc
            .world
            .getBlockState(
                new BlockPos(this.mc.player.getBlockPos().getX() - 1, this.mc.player.getBlockPos().getY() - 1, this.mc.player.getBlockPos().getZ() + 1)
            )
            .isAir()
            || !this.mc
            .world
            .getBlockState(
                new BlockPos(this.mc.player.getBlockPos().getX() - 1, this.mc.player.getBlockPos().getY() - 1, this.mc.player.getBlockPos().getZ() - 1)
            )
            .isAir()
            || !this.mc
            .world
            .getBlockState(new BlockPos(this.mc.player.getBlockPos().getX(), this.mc.player.getBlockPos().getY() - 2, this.mc.player.getBlockPos().getZ()))
            .isAir()
            || !this.mc
            .world
            .getBlockState(new BlockPos(this.mc.player.getBlockPos().getX() + 1, this.mc.player.getBlockPos().getY() - 2, this.mc.player.getBlockPos().getZ()))
            .isAir()
            || !this.mc
            .world
            .getBlockState(new BlockPos(this.mc.player.getBlockPos().getX() - 1, this.mc.player.getBlockPos().getY() - 2, this.mc.player.getBlockPos().getZ()))
            .isAir()
            || !this.mc
            .world
            .getBlockState(new BlockPos(this.mc.player.getBlockPos().getX(), this.mc.player.getBlockPos().getY() - 2, this.mc.player.getBlockPos().getZ() + 1))
            .isAir()
            || !this.mc
            .world
            .getBlockState(new BlockPos(this.mc.player.getBlockPos().getX(), this.mc.player.getBlockPos().getY() - 2, this.mc.player.getBlockPos().getZ() - 1))
            .isAir()
            || !this.mc
            .world
            .getBlockState(
                new BlockPos(this.mc.player.getBlockPos().getX() + 1, this.mc.player.getBlockPos().getY() - 2, this.mc.player.getBlockPos().getZ() + 1)
            )
            .isAir()
            || !this.mc
            .world
            .getBlockState(
                new BlockPos(this.mc.player.getBlockPos().getX() + 1, this.mc.player.getBlockPos().getY() - 2, this.mc.player.getBlockPos().getZ() - 1)
            )
            .isAir()
            || !this.mc
            .world
            .getBlockState(
                new BlockPos(this.mc.player.getBlockPos().getX() - 1, this.mc.player.getBlockPos().getY() - 2, this.mc.player.getBlockPos().getZ() + 1)
            )
            .isAir()
            || !this.mc
            .world
            .getBlockState(
                new BlockPos(this.mc.player.getBlockPos().getX() - 1, this.mc.player.getBlockPos().getY() - 2, this.mc.player.getBlockPos().getZ() - 1)
            )
            .isAir()
            : false;
    }

    public static enum RotationMode {
        Math,
        Simple,
        Hypixel,
        None;
    }

    public static enum TowerMode {
        Vanilla,
        NCP,
        Matrix,
        Vulcan,
        Hypixel;
    }

    public static enum TowerWhen {
        Always,
        Moving,
        Standing;
    }
}
