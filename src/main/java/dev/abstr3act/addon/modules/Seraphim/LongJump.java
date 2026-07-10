package dev.abstr3act.addon.modules.Seraphim;

import dev.abstr3act.addon.Compassion;
import dev.abstr3act.addon.module.SeraphimModule;
import dev.abstr3act.addon.utils.MovementUtil;
import meteordevelopment.meteorclient.events.entity.player.PlayerMoveEvent;
import meteordevelopment.meteorclient.events.packets.PacketEvent.Receive;
import meteordevelopment.meteorclient.events.world.TickEvent.Pre;
import meteordevelopment.meteorclient.mixininterface.IVec3d;
import meteordevelopment.meteorclient.settings.EnumSetting.Builder;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.world.Timer;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Blocks;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.network.packet.s2c.play.ExplosionS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.screen.ShulkerBoxScreenHandler;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;

public class LongJump extends SeraphimModule {
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
    public final Setting<JumpMode> jumpMode = this.sgGeneral
        .add(((Builder) ((Builder) ((Builder) new Builder().name("mode")).description("The method of jumping.")).defaultValue(JumpMode.Vanilla)).build());
    private final Setting<Double> boostFactor = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("BoostFactor"))
                .description("The amount by which to boost the jump."))
                .visible(
                    () -> this.jumpMode.get() == JumpMode.TakaDamage
                        || this.jumpMode.get() == JumpMode.TakaSlime
                        || this.jumpMode.get() == JumpMode.TakaBoat
                        || this.jumpMode.get() == JumpMode.VulcanWall
                        || this.jumpMode.get() == JumpMode.MatrixWater
                        || this.jumpMode.get() == JumpMode.Explosion
                ))
                .defaultValue(1.261)
                .min(0.0)
                .sliderMax(5.0)
                .build()
        );
    private final Setting<Double> boostFactor_Y = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("BoostFactor Y"))
                .description("The amount by which to boost the jump."))
                .visible(
                    () -> this.jumpMode.get() == JumpMode.TakaDamage
                        || this.jumpMode.get() == JumpMode.TakaSlime
                        || this.jumpMode.get() == JumpMode.TakaBoat
                        || this.jumpMode.get() == JumpMode.VulcanWall
                        || this.jumpMode.get() == JumpMode.MatrixWater
                        || this.jumpMode.get() == JumpMode.GrimContainer
                        || this.jumpMode.get() == JumpMode.Explosion
                ))
                .defaultValue(1.261)
                .min(0.0)
                .sliderMax(5.0)
                .build()
        );
    private final Setting<Double> vanillaBoostFactor = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("vanilla-boost-factor"))
                .description("The amount by which to boost the jump."))
                .visible(() -> this.jumpMode.get() == JumpMode.Vanilla))
                .defaultValue(1.261)
                .min(0.0)
                .sliderMax(5.0)
                .build()
        );
    private final Setting<Double> burstInitialSpeed = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("burst-initial-speed"))
                .description("The initial speed of the runup."))
                .visible(() -> this.jumpMode.get() == JumpMode.Burst))
                .defaultValue(6.0)
                .min(0.0)
                .sliderMax(20.0)
                .build()
        );
    private final Setting<Double> burstBoostFactor = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("burst-boost-factor"))
                .description("The amount by which to boost the jump."))
                .visible(() -> this.jumpMode.get() == JumpMode.Burst))
                .defaultValue(2.149)
                .min(0.0)
                .sliderMax(20.0)
                .build()
        );
    private final Setting<Boolean> onlyOnGround = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("only-on-ground"))
                .description("Only performs the jump if you are on the ground."))
                .visible(() -> this.jumpMode.get() == JumpMode.Burst))
                .defaultValue(true))
                .build()
        );
    private final Setting<Boolean> onJump = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("on-jump"))
                .description("Whether the player needs to jump first or not."))
                .visible(() -> this.jumpMode.get() == JumpMode.Burst))
                .defaultValue(false))
                .build()
        );
    private final Setting<Double> glideMultiplier = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("glide-multiplier"))
                .description("The amount by to multiply the glide velocity."))
                .visible(() -> this.jumpMode.get() == JumpMode.Glide))
                .defaultValue(1.0)
                .min(0.0)
                .sliderMax(5.0)
                .build()
        );
    private final Setting<Boolean> autoDisable = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("auto-disable"))
                .description("Automatically disabled the module after jumping."))
                .visible(() -> this.jumpMode.get() != JumpMode.Vanilla))
                .defaultValue(true))
                .build()
        );
    private final Setting<Boolean> autoWindCharges = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("AutoWindCharges"))
                .description("Whether the player needs to jump first or not."))
                .visible(() -> this.jumpMode.get() == JumpMode.Explosion))
                .defaultValue(false))
                .build()
        );
    public final Setting<Double> timer = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("timer"))
                .description("Timer override."))
                .defaultValue(1.0)
                .min(0.01)
                .sliderMin(0.01)
                .build()
        );
    private final Setting<Boolean> disableOnRubberband = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("disable-on-rubberband"))
                .description("Disables the module when you get lagged back."))
                .defaultValue(true))
                .build()
        );
    boolean start = true;
    private int stage;
    private double moveSpeed;
    private boolean jumping = false;
    private int airTicks;
    private int groundTicks;
    private boolean jumped = false;

    public LongJump() {
        super(Compassion.SERAPHIM, "NewLongJump", "Allows you to jump further than normal.");
    }

    public void onActivate() {
        this.stage = 0;
        this.jumping = false;
        this.airTicks = 0;
        this.groundTicks = -5;
    }

    public void onDeactivate() {
        this.start = true;
        ((Timer) Modules.get().get(Timer.class)).setOverride(1.0);
    }

    @EventHandler
    private void onPacketReceive(Receive event) {
        if (event.packet instanceof PlayerPositionLookS2CPacket && this.disableOnRubberband.get()) {
            this.info("Rubberband detected! Disabling...", new Object[0]);
            this.toggle();
        }
    }

    @EventHandler
    private void onPlayerMove(PlayerMoveEvent event) {
        if (this.timer.get() != 1.0) {
            ((Timer) Modules.get().get(Timer.class)).setOverride(PlayerUtils.isMoving() ? this.timer.get() : 1.0);
        }

        switch ((JumpMode) this.jumpMode.get()) {
            case Vanilla:
                if (PlayerUtils.isMoving() && this.mc.options.jumpKey.isPressed()) {
                    double dir = this.getDir();
                    double xDir = Math.cos(Math.toRadians(dir + 90.0));
                    double zDir = Math.sin(Math.toRadians(dir + 90.0));
                    if (!this.mc.world.isSpaceEmpty(this.mc.player.getBoundingBox().offset(0.0, this.mc.player.getVelocity().y, 0.0)) || this.mc.player.verticalCollision
                    ) {
                        ((IVec3d) event.movement).setXZ(xDir * 0.29F, zDir * 0.29F);
                    }

                    if (event.movement.getY() == 0.33319999363422365) {
                        ((IVec3d) event.movement).setXZ(xDir * this.vanillaBoostFactor.get(), zDir * this.vanillaBoostFactor.get());
                    }
                }
                break;
            case Burst:
                if (this.stage != 0 && !this.mc.player.isOnGround() && this.autoDisable.get()) {
                    this.jumping = true;
                }

                if (this.jumping && this.mc.player.getY() - (int) this.mc.player.getY() < 0.01) {
                    this.jumping = false;
                    this.toggle();
                    this.info("Disabling after jump.", new Object[0]);
                }

                if (this.onlyOnGround.get() && !this.mc.player.isOnGround() && this.stage == 0) {
                    return;
                }

                double xDist = this.mc.player.getX() - this.mc.player.prevX;
                double zDist = this.mc.player.getZ() - this.mc.player.prevZ;
                double lastDist = Math.sqrt(xDist * xDist + zDist * zDist);
                if (PlayerUtils.isMoving()
                    && (!this.onJump.get() || this.mc.options.jumpKey.isPressed())
                    && !this.mc.player.isInLava()
                    && !this.mc.player.isTouchingWater()) {
                    if (this.stage == 0) {
                        this.moveSpeed = this.getMoveSpeed() * this.burstInitialSpeed.get();
                    } else if (this.stage == 1) {
                        ((IVec3d) event.movement).setY(0.42);
                        this.moveSpeed = this.moveSpeed * this.burstBoostFactor.get();
                    } else if (this.stage == 2) {
                        double difference = lastDist - this.getMoveSpeed();
                        this.moveSpeed = lastDist - difference;
                    } else {
                        this.moveSpeed = lastDist - lastDist / 159.0;
                    }

                    this.setMoveSpeed(event, this.moveSpeed = Math.max(this.getMoveSpeed(), this.moveSpeed));
                    if (!this.mc.player.verticalCollision
                        && !this.mc.world.isSpaceEmpty(this.mc.player.getBoundingBox().offset(0.0, this.mc.player.getVelocity().y, 0.0))
                        && !this.mc.world.isSpaceEmpty(this.mc.player.getBoundingBox().offset(0.0, -0.4, 0.0))) {
                        ((IVec3d) event.movement).setY(-0.001);
                    }

                    this.stage++;
                }
        }
    }

    public boolean isBoat(PlayerEntity player) {
        return player.getVehicle() instanceof BoatEntity;
    }

    private void jump() {
        PlayerEntity player = this.mc.player;
        float yaw = player.getYaw();
        double radian = Math.toRadians(yaw);
        Vec3d direction = new Vec3d(-Math.sin(radian), this.boostFactor_Y.get(), Math.cos(radian)).normalize().multiply(this.boostFactor.get());
        player.setVelocity(direction);
        player.velocityDirty = true;
    }

    @EventHandler
    private void onTick(Pre event) {
        if (Utils.canUpdate() && ((JumpMode) this.jumpMode.get()).equals(JumpMode.TakaBoat)) {
            if (this.mc.player.isRiding() && this.isBoat(this.mc.player)) {
                this.start = false;
            }

            if (!this.mc.player.isRiding() && !this.start) {
                this.jump();
                this.toggle();
            }
        }

        if (Utils.canUpdate() && ((JumpMode) this.jumpMode.get()).equals(JumpMode.Explosion) && this.autoWindCharges.get()) {
            InvUtils.swap(InvUtils.find(new Item[]{Items.WIND_CHARGE}).slot(), true);
            this.sendPacket(new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, 0, this.mc.player.getYaw(), 90.0F));
            InvUtils.swapBack();
        }

        if (Utils.canUpdate() && this.mc.player.hurtTime > 0 && ((JumpMode) this.jumpMode.get()).equals(JumpMode.TakaDamage)) {
            this.jump();
            this.toggle();
        }

        if (Utils.canUpdate()
            && this.mc.world.getBlockState(this.mc.player.getBlockPos().down()).getBlock().equals(Blocks.SLIME_BLOCK)
            && ((JumpMode) this.jumpMode.get()).equals(JumpMode.TakaSlime)) {
            this.jump();
            this.toggle();
        }

        if (Utils.canUpdate() && this.jumpMode.get() == JumpMode.MatrixWater) {
            if (!this.mc.player.horizontalCollision) {
                return;
            }

            if (!this.mc.player.isTouchingWater()) {
                return;
            }

            Vec3d velocity = this.mc.player.getVelocity();
            if (velocity.y >= 0.2) {
                return;
            }

            this.mc.player.setVelocity(velocity.x, this.boostFactor.get(), velocity.z);
        }

        if (Utils.canUpdate() && this.jumpMode.get() == JumpMode.VulcanWall) {
            if (!this.mc.player.horizontalCollision) {
                return;
            }

            Vec3d velocity = this.mc.player.getVelocity();
            if (velocity.y >= 0.2) {
                return;
            }

            this.mc.player.setVelocity(velocity.x, this.boostFactor.get(), velocity.z);
        }

        if (Utils.canUpdate() && this.jumpMode.get() == JumpMode.Glide) {
            if (!PlayerUtils.isMoving()) {
                return;
            }

            float yaw = this.mc.player.getYaw() + 90.0F;
            double forward = this.mc.player.forwardSpeed != 0.0F ? (this.mc.player.forwardSpeed > 0.0F ? 1 : -1) : 0;
            float[] motion = new float[]{
                0.4206065F,
                0.4179245F,
                0.41525924F,
                0.41261F,
                0.409978F,
                0.407361F,
                0.404761F,
                0.402178F,
                0.399611F,
                0.39706F,
                0.394525F,
                0.392F,
                0.3894F,
                0.38644F,
                0.383655F,
                0.381105F,
                0.37867F,
                0.37625F,
                0.37384F,
                0.37145F,
                0.369F,
                0.3666F,
                0.3642F,
                0.3618F,
                0.35945F,
                0.357F,
                0.354F,
                0.351F,
                0.348F,
                0.345F,
                0.342F,
                0.339F,
                0.336F,
                0.333F,
                0.33F,
                0.327F,
                0.324F,
                0.321F,
                0.318F,
                0.315F,
                0.312F,
                0.309F,
                0.307F,
                0.305F,
                0.303F,
                0.3F,
                0.297F,
                0.295F,
                0.293F,
                0.291F,
                0.289F,
                0.287F,
                0.285F,
                0.283F,
                0.281F,
                0.279F,
                0.277F,
                0.275F,
                0.273F,
                0.271F,
                0.269F,
                0.267F,
                0.265F,
                0.263F,
                0.261F,
                0.259F,
                0.257F,
                0.255F,
                0.253F,
                0.251F,
                0.249F,
                0.247F,
                0.245F,
                0.243F,
                0.241F,
                0.239F,
                0.237F
            };
            float[] glide = new float[]{0.3425F, 0.5445F, 0.65425F, 0.685F, 0.675F, 0.2F, 0.895F, 0.719F, 0.76F};
            double cos = Math.cos(Math.toRadians(yaw));
            double sin = Math.sin(Math.toRadians(yaw));
            if (!this.mc.player.verticalCollision && !this.mc.player.isOnGround()) {
                this.jumped = true;
                this.airTicks++;
                this.groundTicks = -5;
                double velocityY = this.mc.player.getVelocity().y;
                if (this.airTicks - 6 >= 0 && this.airTicks - 6 < glide.length) {
                    this.updateY(velocityY * glide[this.airTicks - 6] * this.glideMultiplier.get());
                }

                if (velocityY < -0.2 && velocityY > -0.24) {
                    this.updateY(velocityY * 0.7 * this.glideMultiplier.get());
                } else if (velocityY < -0.25 && velocityY > -0.32) {
                    this.updateY(velocityY * 0.8 * this.glideMultiplier.get());
                } else if (velocityY < -0.35 && velocityY > -0.8) {
                    this.updateY(velocityY * 0.98 * this.glideMultiplier.get());
                }

                if (this.airTicks - 1 >= 0 && this.airTicks - 1 < motion.length) {
                    this.mc
                        .player
                        .setVelocity(
                            forward * motion[this.airTicks - 1] * 3.0 * cos * this.glideMultiplier.get(),
                            this.mc.player.getVelocity().y,
                            forward * motion[this.airTicks - 1] * 3.0 * sin * this.glideMultiplier.get()
                        );
                } else {
                    this.mc.player.setVelocity(0.0, this.mc.player.getVelocity().y, 0.0);
                }
            } else {
                if (this.autoDisable.get() && this.jumped) {
                    this.jumped = false;
                    this.toggle();
                    this.info("Disabling after jump.", new Object[0]);
                }

                this.airTicks = 0;
                this.groundTicks++;
                if (this.groundTicks <= 2) {
                    this.mc
                        .player
                        .setVelocity(
                            forward * 0.01F * cos * this.glideMultiplier.get(),
                            this.mc.player.getVelocity().y,
                            forward * 0.01F * sin * this.glideMultiplier.get()
                        );
                } else {
                    this.mc
                        .player
                        .setVelocity(forward * 0.3F * cos * this.glideMultiplier.get(), 0.424F, forward * 0.3F * sin * this.glideMultiplier.get());
                }
            }
        }
    }

    @EventHandler
    private void onPacketEvent(Receive event) {
        if (((JumpMode) this.jumpMode.get()).equals(JumpMode.Explosion)
            && event.packet instanceof ExplosionS2CPacket packet
            && this.mc.player.squaredDistanceTo(packet.getX(), packet.getY(), packet.getZ()) <= 10.0) {
            this.updateY(this.boostFactor_Y.get());
            MovementUtil.strafe((this.boostFactor.get()).floatValue());
            this.toggle();
        }

        if (this.jumpMode.get() == JumpMode.GrimContainer) {
            if (this.mc.player.currentScreenHandler instanceof ShulkerBoxScreenHandler) {
                this.updateY(this.boostFactor_Y.get());
                this.toggle();
            }
        }
    }

    private void updateY(double amount) {
        this.mc.player.setVelocity(this.mc.player.getVelocity().x, amount, this.mc.player.getVelocity().z);
    }

    private double getDir() {
        double dir = 0.0;
        if (Utils.canUpdate()) {
            dir = this.mc.player.getYaw() + (this.mc.player.forwardSpeed < 0.0F ? 180 : 0);
            if (this.mc.player.sidewaysSpeed > 0.0F) {
                dir += -90.0F * (this.mc.player.forwardSpeed < 0.0F ? -0.5F : (this.mc.player.forwardSpeed > 0.0F ? 0.5F : 1.0F));
            } else if (this.mc.player.sidewaysSpeed < 0.0F) {
                dir += 90.0F * (this.mc.player.forwardSpeed < 0.0F ? -0.5F : (this.mc.player.forwardSpeed > 0.0F ? 0.5F : 1.0F));
            }
        }

        return dir;
    }

    private double getMoveSpeed() {
        double base = 0.2873;
        if (this.mc.player.hasStatusEffect(StatusEffects.SPEED)) {
            base *= 1.0 + 0.2 * (this.mc.player.getStatusEffect(StatusEffects.SPEED).getAmplifier() + 1);
        }

        return base;
    }

    private void setMoveSpeed(PlayerMoveEvent event, double speed) {
        double forward = this.mc.player.forwardSpeed;
        double strafe = this.mc.player.sidewaysSpeed;
        float yaw = this.mc.player.getYaw();
        if (!PlayerUtils.isMoving()) {
            ((IVec3d) event.movement).setXZ(0.0, 0.0);
        } else {
            if (forward != 0.0) {
                if (strafe > 0.0) {
                    yaw += forward > 0.0 ? -45 : 45;
                } else if (strafe < 0.0) {
                    yaw += forward > 0.0 ? 45 : -45;
                }
            }

            strafe = 0.0;
            if (forward > 0.0) {
                forward = 1.0;
            } else if (forward < 0.0) {
                forward = -1.0;
            }
        }

        double cos = Math.cos(Math.toRadians(yaw + 90.0F));
        double sin = Math.sin(Math.toRadians(yaw + 90.0F));
        ((IVec3d) event.movement).setXZ(forward * speed * cos + strafe * speed * sin, forward * speed * sin + strafe * speed * cos);
    }

    public String getInfoString() {
        return ((JumpMode) this.jumpMode.get()).name();
    }

    public static enum JumpMode {
        Vanilla,
        Burst,
        Glide,
        TakaDamage,
        TakaSlime,
        TakaBoat,
        MatrixWater,
        VulcanWall,
        GrimContainer,
        Explosion;
    }
}
