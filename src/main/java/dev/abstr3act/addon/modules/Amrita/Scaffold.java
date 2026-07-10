package dev.abstr3act.addon.modules.Amrita;

import dev.abstr3act.addon.Compassion;
import dev.abstr3act.addon.events.EventMove;
import dev.abstr3act.addon.events.EventPostSync;
import dev.abstr3act.addon.events.EventSync;
import dev.abstr3act.addon.module.AmritaModule;
import dev.abstr3act.addon.modules.Amrita.autoweb.InteractionUtility;
import dev.abstr3act.addon.utils.MovementUtility;
import dev.abstr3act.addon.utils.RotationUtil;
import dev.abstr3act.addon.utils.math.InventoryUtility;
import dev.abstr3act.addon.utils.math.inv.SearchInvResult;
import dev.abstr3act.addon.utils.timers.Timer;
import meteordevelopment.meteorclient.events.world.TickEvent.Post;
import meteordevelopment.meteorclient.settings.EnumSetting.Builder;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.item.BlockItem;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket.Full;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.awt.*;

public class Scaffold extends AmritaModule {
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
    public final Setting<Boolean> sprint = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("sprint"))
                .description("Sprint"))
                .defaultValue(true))
                .build()
        );
    private final SettingGroup sgRender = this.settings.createGroup("Render");
    private final Setting<Mode> mode = this.sgGeneral
        .add(((Builder) ((Builder) ((Builder) new Builder().name("mode")).description("The mode of operation.")).defaultValue(Mode.NCP)).build());
    private final Setting<InteractionUtility.PlaceMode> placeMode = this.sgGeneral
        .add(
            ((Builder) ((Builder) ((Builder) ((Builder) new Builder().name("place-mode")).description("The place mode."))
                .defaultValue(InteractionUtility.PlaceMode.Normal))
                .visible(() -> !((Mode) this.mode.get()).equals(Mode.Grim)))
                .build()
        );
    private final Setting<Boolean> tower = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("tower"))
                .description("Enable tower."))
                .defaultValue(true))
                .visible(() -> !((Mode) this.mode.get()).equals(Mode.Grim)))
                .build()
        );
    private final Setting<Boolean> safewalk = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("safe-walk"))
                .description("Enable safe walk."))
                .defaultValue(true))
                .visible(() -> !((Mode) this.mode.get()).equals(Mode.Grim)))
                .build()
        );
    private final Setting<Switch> autoSwitch = this.sgGeneral
        .add(((Builder) ((Builder) ((Builder) new Builder().name("switch")).description("Auto switch mode.")).defaultValue(Switch.Silent)).build());
    private final Setting<Boolean> rotate = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("rotate"))
                .description("Enable rotation."))
                .defaultValue(true))
                .build()
        );
    private final Setting<Double> rotationSpeed = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("RotationSpeed"))
                .description("."))
                .sliderRange(1.0, 180.0)
                .defaultValue(40.0)
                .visible(this.rotate::get))
                .build()
        );
    private final Setting<Boolean> randomizedRotation = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("Randomized Rotation"))
                .description("."))
                .defaultValue(true))
                .visible(this.rotate::get))
                .build()
        );
    private final Setting<Double> randomTurnSpeed = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("Random Turn Value"))
                .description("."))
                .sliderRange(1.0, 20.0)
                .defaultValue(15.0)
                .visible(() -> this.rotate.get() && this.randomizedRotation.get()))
                .build()
        );
    private final Setting<Boolean> lockY = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("lock-y"))
                .description("Lock Y position."))
                .defaultValue(false))
                .build()
        );
    private final Setting<Boolean> onlyNotHoldingSpace = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("only-not-holding-space"))
                .description("Only allow if not holding space."))
                .defaultValue(false))
                .visible(this.lockY::get))
                .build()
        );
    private final Setting<Boolean> autoJump = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("auto-jump"))
                .description("Enable auto jump."))
                .defaultValue(false))
                .build()
        );
    private final Setting<Boolean> allowShift = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("work-while-sneaking"))
                .description("Enable work while sneaking."))
                .defaultValue(false))
                .build()
        );
    private final Setting<Boolean> render = this.sgRender
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("render"))
                .description("Enable rendering."))
                .defaultValue(true))
                .build()
        );
    private final Setting<SettingColor> renderFillColor = this.sgRender
        .add(
            ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                .name("render-fill-color"))
                .description("The fill color for rendering."))
                .defaultValue(new SettingColor(new Color(255, 255, 255, 100)))
                .build()
        );
    private final Setting<SettingColor> renderLineColor = this.sgRender
        .add(
            ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                .name("render-line-color"))
                .description("The line color for rendering."))
                .defaultValue(new SettingColor(new Color(255, 255, 255, 255)))
                .build()
        );
    private final Setting<Integer> renderLineWidth = this.sgRender
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("render-line-width"))
                .description("The width of the render lines."))
                .defaultValue(2))
                .min(1)
                .max(5)
                .build()
        );
    private final Timer timer = new Timer();
    private InteractionUtility.BlockPosWithFacing currentblock;
    private int prevY;

    public Scaffold() {
        super(Compassion.AMRITA, "ScaffoldV3", ".");
    }

    public void onActivate() {
        this.prevY = -999;
    }

    @EventHandler
    public void onMove(EventMove event) {
        if (!fullNullCheck()) {
            if (!this.sprint.get()) {
                if (this.mc.player.isSprinting()) {
                    this.mc
                        .getNetworkHandler()
                        .sendPacket(new ClientCommandC2SPacket(this.mc.player, ClientCommandC2SPacket.Mode.STOP_SPRINTING));
                }

                this.mc.player.setSprinting(false);
                this.mc.options.sprintKey.setPressed(false);
            }

            if (this.safewalk.get() && !((Mode) this.mode.get()).equals(Mode.Grim)) {
                double x = event.getX();
                double y = event.getY();
                double z = event.getZ();
                if (this.mc.player.isOnGround() && !this.mc.player.noClip) {
                    double increment = 0.05;

                    while (x != 0.0 && this.isOffsetBBEmpty(x, 0.0)) {
                        if (x < increment && x >= -increment) {
                            x = 0.0;
                        } else if (x > 0.0) {
                            x -= increment;
                        } else {
                            x += increment;
                        }
                    }

                    while (z != 0.0 && this.isOffsetBBEmpty(0.0, z)) {
                        if (z < increment && z >= -increment) {
                            z = 0.0;
                        } else if (z > 0.0) {
                            z -= increment;
                        } else {
                            z += increment;
                        }
                    }

                    while (x != 0.0 && z != 0.0 && this.isOffsetBBEmpty(x, z)) {
                        if (x < increment && x >= -increment) {
                            x = 0.0;
                        } else if (x > 0.0) {
                            x -= increment;
                        } else {
                            x += increment;
                        }

                        if (z < increment && z >= -increment) {
                            z = 0.0;
                        } else if (z > 0.0) {
                            z -= increment;
                        } else {
                            z += increment;
                        }
                    }
                }

                event.setX(x);
                event.setY(y);
                event.setZ(z);
                event.cancel();
            }
        }
    }

    @EventHandler
    public void onTick(Post e) {
        if (((Mode) this.mode.get()).equals(Mode.Grim)) {
            this.preAction();
            this.postAction();
        }
    }

    public void onDeactivate() {
        RotationUtil.reset();
        if (((Mode) this.mode.get()).equals(Mode.Vulcan)) {
            this.mc
                .getNetworkHandler()
                .sendPacket(new ClientCommandC2SPacket(this.mc.player, ClientCommandC2SPacket.Mode.PRESS_SHIFT_KEY));
        }
    }

    @EventHandler
    public void onPre(EventSync e) {
        if (!((Mode) this.mode.get()).equals(Mode.Grim)) {
            this.preAction();
        }
    }

    public void preAction() {
        this.currentblock = null;
        if (!this.mc.player.isSneaking() || this.allowShift.get()) {
            if (this.prePlace(false) != -1) {
                if (this.mc.options.jumpKey.isPressed() && !MovementUtility.isMoving()) {
                    this.prevY = (int) Math.floor(this.mc.player.getY() - 1.0);
                }

                if (MovementUtility.isMoving() && this.autoJump.get()) {
                    if (this.mc.options.jumpKey.isPressed()) {
                        if (this.onlyNotHoldingSpace.get()) {
                            this.prevY = (int) Math.floor(this.mc.player.getY() - 1.0);
                        }
                    } else if (this.mc.player.isOnGround()) {
                        this.mc.player.jump();
                    }
                }

                BlockPos blockPos2 = this.lockY.get() && this.prevY != -999
                    ? BlockPos.ofFloored(this.mc.player.getX(), this.prevY, this.mc.player.getZ())
                    : new BlockPos((int) Math.floor(this.mc.player.getX()), (int) Math.floor(this.mc.player.getY() - 1.0), (int) Math.floor(this.mc.player.getZ()));
                if (this.mc.world.getBlockState(blockPos2).isReplaceable()) {
                    this.currentblock = this.checkNearBlocksExtended(blockPos2);
                    if (this.currentblock != null && this.rotate.get() && !((Mode) this.mode.get()).equals(Mode.Grim)) {
                        Vec3d hitVec = new Vec3d(
                            this.currentblock.position().getX() + 0.5, this.currentblock.position().getY() + 0.5, this.currentblock.position().getZ() + 0.5
                        )
                            .add(new Vec3d(this.currentblock.facing().getUnitVector()).multiply(0.5));
                        float[] rotations = InteractionUtility.calculateAngle(hitVec);
                        RotationUtil.setRotation(rotations[0], rotations[1], (this.rotationSpeed.get()).floatValue(), this.randomizedRotation.get());
                    }
                }
            }
        }
    }

    @EventHandler
    public void onPost(EventPostSync e) {
        if (!((Mode) this.mode.get()).equals(Mode.Grim)) {
            this.postAction();
        }
    }

    public void postAction() {
        float offset = ((Mode) this.mode.get()).equals(Mode.Grim) ? 0.3F : 0.2F;
        if (!this.mc
            .world
            .getBlockCollisions(this.mc.player, this.mc.player.getBoundingBox().expand(-offset, 0.0, -offset).offset(0.0, -0.5, 0.0))
            .iterator()
            .hasNext()) {
            if (this.currentblock != null) {
                int prevItem = this.prePlace(true);
                if (((Mode) this.mode.get()).equals(Mode.Vulcan)) {
                    this.mc
                        .getNetworkHandler()
                        .sendPacket(new ClientCommandC2SPacket(this.mc.player, ClientCommandC2SPacket.Mode.PRESS_SHIFT_KEY));
                }

                if (prevItem != -1) {
                    if (this.mc.player.input.jumping
                        && !MovementUtility.isMoving()
                        && this.tower.get()
                        && !((Mode) this.mode.get()).equals(Mode.Grim)) {
                        this.mc.player.setVelocity(0.0, 0.42, 0.0);
                        if (this.timer.passedMs(1500L)) {
                            this.mc.player.setVelocity(this.mc.player.getVelocity().x, -0.28, this.mc.player.getVelocity().z);
                            this.timer.reset();
                        }
                    } else {
                        this.timer.reset();
                    }

                    BlockHitResult bhr;
                    if (((Mode) this.mode.get()).equals(Mode.StrictNCP)) {
                        bhr = new BlockHitResult(
                            new Vec3d(this.currentblock.position().getX() + 0.5, this.currentblock.position().getY() + 0.5, this.currentblock.position().getZ() + 0.5)
                                .add(new Vec3d(this.currentblock.facing().getUnitVector()).multiply(0.5)),
                            this.currentblock.facing(),
                            this.currentblock.position(),
                            false
                        );
                    } else {
                        bhr = new BlockHitResult(
                            new Vec3d(
                                this.currentblock.position().getX() + Math.random(),
                                this.currentblock.position().getY() + 0.99F,
                                this.currentblock.position().getZ() + Math.random()
                            ),
                            this.currentblock.facing(),
                            this.currentblock.position(),
                            false
                        );
                    }

                    float[] rotations = InteractionUtility.calculateAngle(bhr.getPos());
                    boolean sneak = InteractionUtility.needSneak(this.mc.world.getBlockState(bhr.getBlockPos()).getBlock()) && !this.mc.player.isSneaking();
                    if (sneak) {
                        this.mc
                            .player
                            .networkHandler
                            .sendPacket(new ClientCommandC2SPacket(this.mc.player, ClientCommandC2SPacket.Mode.PRESS_SHIFT_KEY));
                    }

                    if (((Mode) this.mode.get()).equals(Mode.Grim)) {
                        this.sendPacket(
                            new Full(this.mc.player.getX(), this.mc.player.getY(), this.mc.player.getZ(), rotations[0], rotations[1], this.mc.player.isOnGround())
                        );
                    }

                    if (this.placeMode.get() == InteractionUtility.PlaceMode.Packet && !((Mode) this.mode.get()).equals(Mode.Grim)) {
                        boolean finalIsOffhand = prevItem == -2;
                        this.sendSequencedPacket(id -> new PlayerInteractBlockC2SPacket(finalIsOffhand ? Hand.OFF_HAND : Hand.MAIN_HAND, bhr, id));
                    } else {
                        this.mc.interactionManager.interactBlock(this.mc.player, prevItem == -2 ? Hand.OFF_HAND : Hand.MAIN_HAND, bhr);
                    }

                    this.mc.player.networkHandler.sendPacket(new HandSwingC2SPacket(prevItem == -2 ? Hand.OFF_HAND : Hand.MAIN_HAND));
                    this.prevY = this.currentblock.position().getY();
                    if (sneak) {
                        this.mc
                            .player
                            .networkHandler
                            .sendPacket(new ClientCommandC2SPacket(this.mc.player, ClientCommandC2SPacket.Mode.RELEASE_SHIFT_KEY));
                    }

                    if (((Mode) this.mode.get()).equals(Mode.Grim)) {
                        this.sendPacket(
                            new Full(
                                this.mc.player.getX(),
                                this.mc.player.getY(),
                                this.mc.player.getZ(),
                                this.mc.player.getYaw(),
                                this.mc.player.getPitch(),
                                this.mc.player.isOnGround()
                            )
                        );
                    }

                    this.postPlace(prevItem);
                }
            }
        }
    }

    private InteractionUtility.BlockPosWithFacing checkNearBlocksExtended(BlockPos blockPos) {
        InteractionUtility.BlockPosWithFacing ret = null;
        ret = InteractionUtility.checkNearBlocks(blockPos);
        if (ret != null) {
            return ret;
        } else {
            ret = InteractionUtility.checkNearBlocks(blockPos.add(-1, 0, 0));
            if (ret != null) {
                return ret;
            } else {
                ret = InteractionUtility.checkNearBlocks(blockPos.add(1, 0, 0));
                if (ret != null) {
                    return ret;
                } else {
                    ret = InteractionUtility.checkNearBlocks(blockPos.add(0, 0, 1));
                    if (ret != null) {
                        return ret;
                    } else {
                        ret = InteractionUtility.checkNearBlocks(blockPos.add(0, 0, -1));
                        if (ret != null) {
                            return ret;
                        } else {
                            ret = InteractionUtility.checkNearBlocks(blockPos.add(-2, 0, 0));
                            if (ret != null) {
                                return ret;
                            } else {
                                ret = InteractionUtility.checkNearBlocks(blockPos.add(2, 0, 0));
                                if (ret != null) {
                                    return ret;
                                } else {
                                    ret = InteractionUtility.checkNearBlocks(blockPos.add(0, 0, 2));
                                    if (ret != null) {
                                        return ret;
                                    } else {
                                        ret = InteractionUtility.checkNearBlocks(blockPos.add(0, 0, -2));
                                        if (ret != null) {
                                            return ret;
                                        } else {
                                            ret = InteractionUtility.checkNearBlocks(blockPos.add(0, -1, 0));
                                            if (ret != null) {
                                                return ret;
                                            } else {
                                                ret = InteractionUtility.checkNearBlocks(blockPos.add(1, -1, 0));
                                                if (ret != null) {
                                                    return ret;
                                                } else {
                                                    ret = InteractionUtility.checkNearBlocks(blockPos.add(-1, -1, 0));
                                                    if (ret != null) {
                                                        return ret;
                                                    } else {
                                                        ret = InteractionUtility.checkNearBlocks(blockPos.add(0, -1, 1));
                                                        return ret != null ? ret : InteractionUtility.checkNearBlocks(blockPos.add(0, -1, -1));
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private int prePlace(boolean swap) {
        if (this.mc.player != null && this.mc.world != null && this.mc.interactionManager != null) {
            if (this.mc.player.getOffHandStack().getItem() instanceof BlockItem bi && !bi.getBlock().getDefaultState().isReplaceable()) {
                return -2;
            } else if (this.mc.player.getMainHandStack().getItem() instanceof BlockItem bi && !bi.getBlock().getDefaultState().isReplaceable()) {
                return this.mc.player.getInventory().selectedSlot;
            } else {
                int prevSlot = this.mc.player.getInventory().selectedSlot;
                SearchInvResult hotbarResult = InventoryUtility.findInHotBar(
                    i -> i.getItem() instanceof BlockItem bi && !bi.getBlock().getDefaultState().isReplaceable()
                );
                SearchInvResult invResult = InventoryUtility.findInInventory(
                    i -> i.getItem() instanceof BlockItem bi && !bi.getBlock().getDefaultState().isReplaceable()
                );
                if (swap) {
                    switch ((Switch) this.autoSwitch.get()) {
                        case Normal:
                        case Silent:
                            hotbarResult.switchTo();
                            break;
                        case Inventory:
                            if (invResult.found()) {
                                prevSlot = invResult.slot();
                                this.mc
                                    .interactionManager
                                    .clickSlot(
                                        this.mc.player.currentScreenHandler.syncId, prevSlot, this.mc.player.getInventory().selectedSlot, SlotActionType.SWAP, this.mc.player
                                    );
                                this.sendPacket(new CloseHandledScreenC2SPacket(this.mc.player.currentScreenHandler.syncId));
                            }
                    }
                }

                return prevSlot;
            }
        } else {
            return -1;
        }
    }

    private void postPlace(int prevSlot) {
        if (prevSlot != -1 && prevSlot != -2) {
            switch ((Switch) this.autoSwitch.get()) {
                case Silent:
                    InventoryUtility.switchTo(prevSlot);
                    break;
                case Inventory:
                    this.mc
                        .interactionManager
                        .clickSlot(this.mc.player.currentScreenHandler.syncId, prevSlot, this.mc.player.getInventory().selectedSlot, SlotActionType.SWAP, this.mc.player);
                    this.sendPacket(new CloseHandledScreenC2SPacket(this.mc.player.currentScreenHandler.syncId));
            }
        }
    }

    private boolean isOffsetBBEmpty(double x, double z) {
        return !this.mc.world.getBlockCollisions(this.mc.player, this.mc.player.getBoundingBox().expand(-0.1, 0.0, -0.1).offset(x, -2.0, z)).iterator().hasNext();
    }

    private static enum Mode {
        NCP,
        StrictNCP,
        Grim,
        Vulcan;
    }

    private static enum Switch {
        Normal,
        Silent,
        Inventory,
        None;
    }
}
