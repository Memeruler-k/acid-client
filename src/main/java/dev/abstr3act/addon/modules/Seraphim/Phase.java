package dev.abstr3act.addon.modules.Seraphim;

import dev.abstr3act.addon.Compassion;
import dev.abstr3act.addon.events.EventBreakBlock;
import dev.abstr3act.addon.events.EventCollision;
import dev.abstr3act.addon.events.EventPostSync;
import dev.abstr3act.addon.events.EventSync;
import dev.abstr3act.addon.module.SeraphimModule;
import dev.abstr3act.addon.utils.MovementUtility;
import dev.abstr3act.addon.utils.math.InventoryUtility;
import meteordevelopment.meteorclient.settings.EnumSetting.Builder;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Blocks;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket.Action;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket.LookAndOnGround;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;

public class Phase extends SeraphimModule {
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
    private final Setting<Mode> mode = this.sgGeneral.add(((Builder) ((Builder) new Builder().name("mode")).defaultValue(Mode.Vanilla)).build());
    private final Setting<Boolean> silent = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("silent"))
                .defaultValue(false))
                .visible(() -> this.mode.get() == Mode.Sunrise))
                .build()
        );
    private final Setting<Boolean> waitBreak = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("wait-break"))
                .defaultValue(true))
                .visible(() -> this.mode.get() == Mode.Sunrise))
                .build()
        );
    private final Setting<Integer> afterBreak = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("break-timeout"))
                .defaultValue(4))
                .min(1)
                .max(20)
                .visible(() -> this.mode.get() == Mode.Sunrise && this.waitBreak.get()))
                .build()
        );
    private final Setting<Boolean> onlyOnGround = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("only-on-ground"))
                .defaultValue(false))
                .visible(() -> this.mode.get() == Mode.Pearl))
                .build()
        );
    private final Setting<Boolean> autoDisable = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("auto-disable"))
                .defaultValue(false))
                .visible(() -> this.mode.get() == Mode.Pearl))
                .build()
        );
    private final Setting<Integer> afterPearl = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("pearl-timeout"))
                .defaultValue(0))
                .min(0)
                .max(60)
                .visible(() -> this.mode.get() == Mode.Pearl))
                .build()
        );
    private final Setting<Double> pitch = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("pitch"))
                .defaultValue(80.0)
                .min(0.0)
                .max(90.0)
                .visible(() -> this.mode.get() == Mode.Pearl))
                .build()
        );
    private final Setting<Boolean> strict = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("strict"))
                .defaultValue(false))
                .visible(() -> this.mode.get() == Mode.ForceMine))
                .build()
        );
    public int clipTimer;
    public int afterPearlTime;

    public Phase() {
        super(Compassion.SERAPHIM, "Phase", "");
    }

    @EventHandler
    public void onCollide(EventCollision e) {
        if (!fullNullCheck()) {
            BlockPos playerPos = BlockPos.ofFloored(this.mc.player.getPos());
            if ((
                !((Mode) this.mode.get()).equals(Mode.Pearl) && !((Mode) this.mode.get()).equals(Mode.ForceMine) && this.canNoClip()
                    || this.afterPearlTime > 0
            )
                && (!e.getPos().equals(playerPos.down()) || this.mc.options.sneakKey.isPressed())) {
                e.setState(Blocks.AIR.getDefaultState());
            }

            if (((Mode) this.mode.get()).equals(Mode.ForceMine)) {
                float xDelta = Math.abs(playerPos.getX() - e.getPos().getX());
                float zDelta = Math.abs(playerPos.getZ() - e.getPos().getZ());
                if (xDelta != 0.0F && zDelta != 0.0F && this.strict.get()) {
                    return;
                }

                if (!e.getPos().equals(playerPos.down()) || this.mc.options.sneakKey.isPressed()) {
                    e.setState(Blocks.AIR.getDefaultState());
                }
            }
        }
    }

    public void onActivate() {
        this.afterPearlTime = 0;
        this.clipTimer = 0;
    }

    @EventHandler
    public void onSync(EventSync e) {
        if (!fullNullCheck()) {
            if (this.clipTimer > 0) {
                this.clipTimer--;
            }

            if (this.afterPearlTime > 0) {
                this.afterPearlTime--;
            }

            if (this.mode.get() == Mode.Sunrise
                && (this.mc.player.horizontalCollision || this.playerInsideBlock())
                && !this.mc.player.isSubmergedInWater()
                && !this.mc.player.isInLava()
                && this.clipTimer <= 0) {
                double[] dir = MovementUtility.forward(0.5);
                BlockPos blockToBreak = null;
                if (this.mc.options.jumpKey.isPressed()) {
                    blockToBreak = BlockPos.ofFloored(this.mc.player.getX() + dir[0], this.mc.player.getY() + 2.0, this.mc.player.getZ() + dir[1]);
                } else if (this.mc.options.sneakKey.isPressed()) {
                    blockToBreak = BlockPos.ofFloored(this.mc.player.getX() + dir[0], this.mc.player.getY() - 1.0, this.mc.player.getZ() + dir[1]);
                } else if (MovementUtility.isMoving()) {
                    blockToBreak = BlockPos.ofFloored(this.mc.player.getX() + dir[0], this.mc.player.getY(), this.mc.player.getZ() + dir[1]);
                }

                if (blockToBreak == null) {
                    return;
                }

                int prevItem = this.mc.player.getInventory().selectedSlot;
                this.mc.interactionManager.updateBlockBreakingProgress(blockToBreak, this.mc.player.getHorizontalFacing());
                this.mc.player.swingHand(Hand.MAIN_HAND);
                if (this.silent.get()) {
                    InventoryUtility.switchTo(prevItem);
                }
            }

            if (this.mode.get() == Mode.ForceMine
                && (this.mc.player.horizontalCollision || this.playerInsideBlock())
                && !this.mc.player.isSubmergedInWater()
                && !this.mc.player.isInLava()) {
                for (int x = -2; x < 2; x++) {
                    for (int y = -1; y < 3; y++) {
                        for (int z = -2; z < 2; z++) {
                            if ((x != 0 || y != 0 || z != 0) && (x != 0 || y != 1 || z != 0) || this.mc.options.sneakKey.isPressed()) {
                                BlockPos bp = BlockPos.ofFloored(this.mc.player.getPos()).add(x, y, z);
                                if (this.mc.player.getBoundingBox().intersects(new Box(bp)) && !this.mc.world.isAir(bp)) {
                                    this.sendPacket(new PlayerActionC2SPacket(Action.STOP_DESTROY_BLOCK, bp, Direction.UP));
                                }
                            }
                        }
                    }
                }
            }

            if (this.mode.get() == Mode.Pearl
                && (this.mc.player.isOnGround() || !this.onlyOnGround.get())
                && this.mc.player.horizontalCollision
                && !this.playerInsideBlock()
                && this.clipTimer <= 0
                && this.mc.player.age > 60) {
                double[] dirx = MovementUtility.forward(0.5);
                BlockPos block = BlockPos.ofFloored(this.mc.player.getX() + dirx[0], this.mc.player.getY(), this.mc.player.getZ() + dirx[1]);
                if (this.mc.options.sneakKey.isPressed()) {
                    return;
                }

                float[] angle = PlayerUtils.calculateAngle(block.toCenterPos());
                int epSlot = this.findEPSlot();
                if (epSlot != -1) {
                    this.sendPacket(new LookAndOnGround(angle[0], (this.pitch.get()).floatValue(), this.mc.player.isOnGround()));
                }
            }
        }
    }

    @EventHandler
    public void onPostSync(EventPostSync e) {
        if (this.mode.get() == Mode.Pearl
            && (this.mc.player.isOnGround() || !this.onlyOnGround.get())
            && this.mc.player.horizontalCollision
            && !this.playerInsideBlock()
            && this.clipTimer <= 0
            && this.mc.player.age > 60) {
            if (this.mc.options.sneakKey.isPressed()) {
                return;
            }

            int epSlot = this.findEPSlot();
            int prevItem = this.mc.player.getInventory().selectedSlot;
            if (epSlot != -1) {
                InventoryUtility.switchTo(epSlot);
                this.sendSequencedPacket(id -> new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, id, this.mc.player.getYaw(), this.mc.player.getPitch()));
                this.sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
                InventoryUtility.switchTo(prevItem);
                if (this.autoDisable.get()) {
                    this.toggle();
                }
            }

            this.clipTimer = 20;
            this.afterPearlTime = this.afterPearl.get();
        }
    }

    private int findEPSlot() {
        int epSlot = -1;
        if (this.mc.player.getMainHandStack().getItem() == Items.ENDER_PEARL) {
            epSlot = this.mc.player.getInventory().selectedSlot;
        }

        if (epSlot == -1) {
            for (int l = 0; l < 9; l++) {
                if (this.mc.player.getInventory().getStack(l).getItem() == Items.ENDER_PEARL) {
                    epSlot = l;
                    break;
                }
            }
        }

        return epSlot;
    }

    public boolean canNoClip() {
        if (((Mode) this.mode.get()).equals(Mode.Vanilla)) {
            return true;
        } else {
            return !this.waitBreak.get() ? true : this.clipTimer != 0;
        }
    }

    public boolean playerInsideBlock() {
        return !this.mc.world.isAir(BlockPos.ofFloored(this.mc.player.getPos()));
    }

    @EventHandler
    public void onBreakBlock(EventBreakBlock e) {
        this.clipTimer = this.afterBreak.get();
    }

    private static enum Mode {
        Vanilla,
        Pearl,
        Sunrise,
        ForceMine;
    }
}
