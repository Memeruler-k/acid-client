package dev.abstr3act.addon.modules.Seraphim;

import dev.abstr3act.addon.Compassion;
import dev.abstr3act.addon.events.EntityVelocityUpdateEvent;
import dev.abstr3act.addon.events.EventPlayerMove;
import dev.abstr3act.addon.events.EventUpdate;
import dev.abstr3act.addon.events.legacy.WorldEvent;
import dev.abstr3act.addon.mixin.accessor.IEntityVelocityUpdateS2CPacket;
import dev.abstr3act.addon.mixin.accessor.IExplosionS2CPacket;
import dev.abstr3act.addon.module.BaseModule;
import dev.abstr3act.addon.module.SeraphimModule;
import dev.abstr3act.addon.utils.abnormally.AChatUtils;
import dev.abstr3act.addon.utils.compassion.BlockPosX;
import dev.abstr3act.addon.utils.seraphim.movement.MovementUtils;
import dev.abstr3act.addon.utils.timers.Timer;
import meteordevelopment.meteorclient.events.packets.PacketEvent.Receive;
import meteordevelopment.meteorclient.events.packets.PacketEvent.Send;
import meteordevelopment.meteorclient.settings.EnumSetting.Builder;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket.Action;
import net.minecraft.network.packet.c2s.play.TeleportConfirmC2SPacket;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ExplosionS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class VelocityV2 extends SeraphimModule {
    public static int fallTicks = 0;
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
    public final Setting<Mode> mode = this.sgGeneral
        .add(((Builder) ((Builder) ((Builder) new Builder().name("Mode")).description(".")).defaultValue(Mode.Normal)).build());
    private final Setting<Double> strength = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("Strength"))
                .description(""))
                .defaultValue(1.0)
                .sliderRange(0.01F, 10.0)
                .visible(() -> ((Mode) this.mode.get()).equals(Mode.Strafe)))
                .build()
        );
    private final Setting<Integer> delay = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("Delay"))
                .description(""))
                .defaultValue(2))
                .sliderRange(0, 20)
                .visible(() -> ((Mode) this.mode.get()).equals(Mode.Strafe)))
                .build()
        );
    private final Setting<Boolean> untilGround = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("untilGround"))
                .description("."))
                .defaultValue(true))
                .visible(() -> ((Mode) this.mode.get()).equals(Mode.Strafe)))
                .build()
        );
    private final Setting<Boolean> cancelTeleport = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("CancelTeleport"))
                .description("."))
                .defaultValue(true))
                .visible(() -> ((Mode) this.mode.get()).equals(Mode.Vulcan)))
                .build()
        );
    private final Setting<Boolean> cancelEntityStatus = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("CancelEntityStatus"))
                .description("."))
                .defaultValue(true))
                .visible(() -> ((Mode) this.mode.get()).equals(Mode.Vulcan)))
                .build()
        );
    private final Setting<Double> horizontal = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("Horizontal"))
                .description("."))
                .defaultValue(0.0)
                .sliderRange(0.0, 100.0)
                .range(0.0, 100.0)
                .build()
        );
    private final Setting<Double> vertical = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("Vertical"))
                .description("."))
                .defaultValue(0.0)
                .sliderRange(0.0, 100.0)
                .range(0.0, 100.0)
                .build()
        );
    private final Setting<Boolean> pauseInLiquid = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("PauseInLiquid"))
                .description("."))
                .defaultValue(false))
                .build()
        );
    private final Setting<Boolean> flagInWall = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("flagInWall"))
                .description("."))
                .defaultValue(false))
                .build()
        );
    private final Setting<Boolean> fishBob = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("fishBob"))
                .description("."))
                .defaultValue(true))
                .build()
        );
    private final Setting<Boolean> noExplosions = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("noExplosions"))
                .description("."))
                .defaultValue(true))
                .build()
        );
    private final Timer lagBackTimer = new Timer();
    int tick = 0;
    boolean skip = false;
    private boolean flag;
    private boolean applyStrafe = false;

    public VelocityV2() {
        super(Compassion.SERAPHIM, "VelocityV2", ".");
    }

    @EventHandler
    public void onVelocity(EntityVelocityUpdateEvent event) {
        if (!BaseModule.fullNullCheck()) {
            if (!((Mode) this.mode.get()).equals(Mode.Hypixel)) {
                if (!this.mc.player.isTouchingWater() && !this.mc.player.isSubmergedInWater() && !this.mc.player.isInLava() || !this.pauseInLiquid.get()) {
                    if (((Mode) this.mode.get()).equals(Mode.Vulcan) && !this.skip) {
                        event.cancel();
                    } else {
                        if (((Mode) this.mode.get()).equals(Mode.Grim) || ((Mode) this.mode.get()).equals(Mode.Wall)) {
                            if (!this.lagBackTimer.passed(100L)) {
                                return;
                            }

                            boolean insideBlock = this.isInsideBlock();
                            if (((Mode) this.mode.get()).equals(Mode.Wall) && !insideBlock) {
                                return;
                            }

                            event.cancel();
                            this.flag = true;
                        }
                    }
                }
            }
        }
    }

    public double getSqrtSpeed(Vec3d vec3d) {
        return Math.sqrt(vec3d.x * vec3d.x + vec3d.z * vec3d.z);
    }

    @EventHandler
    private void onPacket(Receive receive) {
        if (((Mode) this.mode.get()).equals(Mode.Strafe)) {
            Object packet = receive.packet;
            if (packet instanceof EntityVelocityUpdateS2CPacket && ((EntityVelocityUpdateS2CPacket) packet).getEntityId() == this.mc.player.getId()
                || packet instanceof ExplosionS2CPacket) {
                AChatUtils.sendMsgAmrita(Text.of("Strafe"));
                MovementUtils.strafe(MovementUtils.getSpeed() * this.strength.get());
                if (this.untilGround.get()) {
                    this.applyStrafe = true;
                }
            }
        }
    }

    @EventHandler
    private void onWorld(WorldEvent event) {
        this.skip = true;
    }

    @EventHandler
    private void onPacket2(Send event) {
        if (((Mode) this.mode.get()).equals(Mode.Vulcan)) {
            if (this.skip) {
                this.skip = false;
            } else {
                if (event.packet instanceof TeleportConfirmC2SPacket && this.cancelTeleport.get()) {
                    event.cancel();
                }
            }
        }
    }

    @EventHandler
    private void onPacket2(Receive receive) {
        if (((Mode) this.mode.get()).equals(Mode.Vulcan)) {
            if (this.skip) {
                this.skip = false;
            } else {
                Object packet = receive.packet;
                if (packet instanceof EntityVelocityUpdateS2CPacket && ((EntityVelocityUpdateS2CPacket) packet).getEntityId() == this.mc.player.getId()
                    || packet instanceof ExplosionS2CPacket) {
                    receive.cancel();
                }

                if (packet instanceof EntityStatusS2CPacket && this.cancelEntityStatus.get()) {
                    receive.cancel();
                }
            }
        }
    }

    public Vec3d strafe(double x, double y, double z, float yaw, double speed, double strength) {
        double prevX = x * (1.0 - strength);
        double prevZ = z * (1.0 - strength);
        double useSpeed = speed * strength;
        double angle = Math.toRadians(yaw);
        x = -Math.sin(angle) * useSpeed + prevX;
        z = Math.cos(angle) * useSpeed + prevZ;
        return new Vec3d(x, y, z);
    }

    @EventHandler
    public void handle(EventPlayerMove event) {
        if (((Mode) this.mode.get()).equals(Mode.Strafe)) {
            if (this.mc.player.isOnGround()) {
                this.applyStrafe = false;
            } else if (this.applyStrafe) {
                MovementUtils.strafe(MovementUtils.getSpeed() * this.strength.get());
            }
        }
    }

    @EventHandler
    public void onReceivePacket(Receive event) {
        if (!fullNullCheck()) {
            if (!((Mode) this.mode.get()).equals(Mode.Hypixel)) {
                if (!((Mode) this.mode.get()).equals(Mode.Strafe)) {
                    if (!this.mc.player.isTouchingWater() && !this.mc.player.isSubmergedInWater() && !this.mc.player.isInLava() || !this.pauseInLiquid.get()) {
                        if (this.fishBob.get()
                            && event.packet instanceof EntityStatusS2CPacket packet
                            && packet.getStatus() == 31
                            && packet.getEntity(this.mc.world) instanceof FishingBobberEntity fishHook
                            && fishHook.getHookedEntity() == this.mc.player) {
                            event.setCancelled(true);
                        }

                        if (!((Mode) this.mode.get()).equals(Mode.Grim)
                            && (!((Mode) this.mode.get()).equals(Mode.Wall) || this.mode.get() == Mode.Hypixel)) {
                            float h = (this.horizontal.get()).floatValue() / 100.0F;
                            float v = (this.vertical.get()).floatValue() / 100.0F;
                            if (event.packet instanceof ExplosionS2CPacket) {
                                IExplosionS2CPacket packet = (IExplosionS2CPacket) event.packet;
                                packet.setVelocityX(packet.getX() * h);
                                packet.setVelocityY(packet.getY() * v);
                                packet.setVelocityZ(packet.getZ() * h);
                                if (this.noExplosions.get()) {
                                    event.cancel();
                                }

                                return;
                            }

                            if (event.packet instanceof EntityVelocityUpdateS2CPacket packet && packet.getEntityId() == this.mc.player.getId()) {
                                if (this.horizontal.get() == 0.0 && this.vertical.get() == 0.0) {
                                    event.cancel();
                                } else {
                                    ((IEntityVelocityUpdateS2CPacket) packet).setX((int) (packet.getVelocityX() * h));
                                    ((IEntityVelocityUpdateS2CPacket) packet).setY((int) (packet.getVelocityY() * v));
                                    ((IEntityVelocityUpdateS2CPacket) packet).setZ((int) (packet.getVelocityZ() * h));
                                }
                            }
                        } else {
                            if (!this.lagBackTimer.passed(100L)) {
                                return;
                            }

                            boolean insideBlock = this.isInsideBlock();
                            if (((Mode) this.mode.get()).equals(Mode.Wall) && !insideBlock) {
                                return;
                            }

                            if (event.packet instanceof ExplosionS2CPacket explosion) {
                                ((IExplosionS2CPacket) explosion).setVelocityX(0.0F);
                                ((IExplosionS2CPacket) explosion).setVelocityY(0.0F);
                                ((IExplosionS2CPacket) explosion).setVelocityZ(0.0F);
                                this.flag = true;
                            }
                        }
                    }
                }
            }
        }
    }

    public boolean isInsideBlock() {
        return this.getBlock(this.getPlayerPos(true)) == Blocks.ENDER_CHEST ? true : this.mc.world.canCollide(this.mc.player, this.mc.player.getBoundingBox());
    }

    public Block getBlock(BlockPos pos) {
        return this.mc.world.getBlockState(pos).getBlock();
    }

    public BlockPos getPlayerPos(boolean fix) {
        return new BlockPosX(this.mc.player.getPos(), fix);
    }

    @EventHandler
    private void onPacketEvent(Receive event) {
        if (this.mc.player != null) {
            if (!((Mode) this.mode.get()).equals(Mode.Strafe)) {
                if (event.packet instanceof PlayerPositionLookS2CPacket) {
                    this.lagBackTimer.reset();
                }

                if (((Mode) this.mode.get()).equals(Mode.Normal)
                    && event.packet instanceof EntityVelocityUpdateS2CPacket
                    && ((EntityVelocityUpdateS2CPacket) event.packet).getEntityId() == this.mc.player.getId()) {
                    event.cancel();
                }

                if (((Mode) this.mode.get()).equals(Mode.Hypixel)
                    && event.packet instanceof EntityVelocityUpdateS2CPacket
                    && ((EntityVelocityUpdateS2CPacket) event.packet).getEntityId() == this.mc.player.getId()) {
                    event.cancel();
                    if (fallTicks >= 6 && this.mc.player.getVelocity().y >= 0.0 || this.mc.player.isOnGround()) {
                        this.setVelocityY(((EntityVelocityUpdateS2CPacket) event.packet).getVelocityY() / 8000.0);
                    }
                }
            }
        }
    }

    public void setVelocityY(Double y) {
        if (this.mc.player != null) {
            this.mc.player.setVelocity(this.mc.player.getVelocity().x, y, this.mc.player.getVelocity().z);
        }
    }

    public String getInfoString() {
        return ((Mode) this.mode.get()).name();
    }

    @EventHandler
    public void onUpdate(EventUpdate event) {
        if (!((Mode) this.mode.get()).equals(Mode.Hypixel)) {
            if (!((Mode) this.mode.get()).equals(Mode.Strafe)) {
                if (this.mc.player == null
                    || !this.mc.player.isTouchingWater() && !this.mc.player.isSubmergedInWater() && !this.mc.player.isInLava()
                    || !this.pauseInLiquid.get()) {
                    if (this.flag) {
                        if (this.lagBackTimer.passed(100L) && (this.flagInWall.get() || !this.isInsideBlock())) {
                            Compassion.ROTATION.snapBack();
                            this.mc
                                .getNetworkHandler()
                                .sendPacket(
                                    new PlayerActionC2SPacket(
                                        Action.STOP_DESTROY_BLOCK, this.mc.player.isCrawling() ? this.mc.player.getBlockPos() : this.mc.player.getBlockPos().up(), Direction.DOWN
                                    )
                                );
                        }

                        this.flag = false;
                    }
                }
            }
        }
    }

    public static enum Mode {
        Normal,
        Legit,
        Matrix,
        Strafe,
        Intave,
        Hypixel,
        Grim,
        Wall,
        Vulcan;
    }
}
