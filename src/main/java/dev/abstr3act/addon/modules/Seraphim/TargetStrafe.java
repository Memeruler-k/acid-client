package dev.abstr3act.addon.modules.Seraphim;

import dev.abstr3act.addon.Compassion;
import dev.abstr3act.addon.events.EventPlayerMove;
import dev.abstr3act.addon.events.EventPlayerUpdate;
import dev.abstr3act.addon.events.EventSprint;
import dev.abstr3act.addon.events.EventSync;
import dev.abstr3act.addon.mixin.accessor.ISPacketEntityVelocity;
import dev.abstr3act.addon.module.SeraphimModule;
import dev.abstr3act.addon.utils.Render3DEngine;
import dev.abstr3act.addon.utils.math.InventoryUtility;
import dev.abstr3act.addon.utils.seraphim.PlayerUtils;
import meteordevelopment.meteorclient.events.packets.PacketEvent.Receive;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.settings.BoolSetting.Builder;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket.Mode;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.Mutable;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction.Axis;

public class TargetStrafe extends SeraphimModule {
    public static double oldSpeed;
    public static double contextFriction;
    public static double fovval;
    public static boolean needSwap;
    public static boolean needSprintState;
    public static boolean skip;
    public static boolean switchDir;
    public static boolean disabled;
    public static int noSlowTicks;
    public static int jumpTicks;
    public static int waterTicks;
    static long disableTime;
    private static TargetStrafe instance;
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
    public final Setting<Boolean> jump = this.sgGeneral.add(((Builder) ((Builder) new Builder().name("jump")).defaultValue(true)).build());
    public final Setting<Boolean> lowHop = this.sgGeneral
        .add(((Builder) ((Builder) ((Builder) new Builder().name("LowHop")).defaultValue(false)).visible(this.jump::get)).build());
    public final Setting<Double> motion = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("Motion"))
                .defaultValue(0.1)
                .min(0.2)
                .max(7.0)
                .visible(this.lowHop::get))
                .build()
        );
    public final Setting<Double> getDistance = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder().name("getDistance"))
                .defaultValue(1.3)
                .min(0.2)
                .max(7.0)
                .build()
        );
    public final Setting<Integer> onGroundTicks = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("On Ground Ticks"))
                .description(""))
                .min(0)
                .sliderRange(0, 500)
                .defaultValue(2))
                .build()
        );
    public final Setting<Integer> points = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("points"))
                .description("Ignores yourself drawing the shader."))
                .defaultValue(30))
                .sliderRange(1, 100)
                .build()
        );
    public final Setting<Integer> colorOffset = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("colorOffset"))
                .description("Ignores yourself drawing the shader."))
                .defaultValue(30))
                .sliderRange(0, 100)
                .build()
        );
    public final Setting<SettingColor> color = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                .name("color"))
                .description("Ignores yourself drawing the shader."))
                .defaultValue(new Color(255, 255, 255, 255))
                .build()
        );
    private final Setting<Boost> boost = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) new meteordevelopment.meteorclient.settings.EnumSetting.Builder()
                .name("boost"))
                .defaultValue(Boost.None))
                .build()
        );
    public final Setting<Double> setSpeed = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("speed"))
                .defaultValue(1.3)
                .min(0.0)
                .max(2.0)
                .visible(() -> this.boost.get() == Boost.Elytra))
                .build()
        );
    private final Setting<Double> velReduction = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("reduction"))
                .defaultValue(6.0)
                .min(0.1)
                .max(10.0)
                .visible(() -> this.boost.get() == Boost.Damage))
                .build()
        );
    private final Setting<Double> maxVelocitySpeed = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("max-velocity"))
                .defaultValue(0.8)
                .min(0.1)
                .max(2.0)
                .visible(() -> this.boost.get() == Boost.Damage))
                .build()
        );
    private final Setting<Double> speedFactor = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder().name("speedFactor"))
                .defaultValue(0.0)
                .min(0.0)
                .max(5.0)
                .build()
        );
    private final Setting<Double> onGroundSpeedFactor = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("OnGroundSpeedFactor"))
                .defaultValue(0.0)
                .min(0.0)
                .max(5.0)
                .build()
        );
    private final Setting<Double> onGroundFrictionFactor = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("OnGroundFrictionFactor"))
                .defaultValue(0.0)
                .min(0.0)
                .max(5.0)
                .build()
        );
    Entity target;
    int ticks;

    public TargetStrafe() {
        super(Compassion.SERAPHIM, "TargetStrafe", "");
        instance = this;
    }

    public void onActivate() {
        oldSpeed = 0.0;
        fovval = this.mc.options.getFovEffectScale().getValue();
        this.mc.options.getFovEffectScale().setValue(0.0);
        skip = true;
    }

    public boolean canStrafe() {
        if (this.mc.player.isSneaking()) {
            return false;
        } else if (this.mc.player.isInLava()) {
            return false;
        } else {
            return !this.mc.player.isSubmergedInWater() && waterTicks <= 0 ? !this.mc.player.getAbilities().flying : false;
        }
    }

    public boolean needToSwitch(double x, double z) {
        if (!this.mc.player.horizontalCollision && (!this.mc.options.leftKey.isPressed() && !this.mc.options.rightKey.isPressed() || jumpTicks > 0)) {
            for (int i = (int) (this.mc.player.getY() + 4.0); i >= 0; i--) {
                BlockPos playerPos = new BlockPos((int) Math.floor(x), (int) Math.floor(i), (int) Math.floor(z));
                if (this.mc.world.getBlockState(playerPos).getBlock().equals(Blocks.LAVA) || this.mc.world.getBlockState(playerPos).getBlock().equals(Blocks.FIRE)) {
                    return true;
                }

                if (!this.mc.world.isAir(playerPos)) {
                    return false;
                }
            }

            return false;
        } else {
            jumpTicks = 10;
            return true;
        }
    }

    @EventHandler
    public void onRender3DEvent(Render3DEvent event) {
        if (KillAura.closestEntity != null) {
            Render3DEngine.drawCircle3D(
                event.matrices,
                KillAura.closestEntity,
                (this.getDistance.get()).floatValue(),
                ((SettingColor) this.color.get()).getPacked(),
                this.points.get(),
                false,
                this.colorOffset.get()
            );
        }
    }

    public void onDeactivate() {
        this.mc.options.getFovEffectScale().setValue(fovval);
    }

    public double calculateSpeed(EventPlayerMove move) {
        jumpTicks--;
        float speedAttributes = this.getAIMoveSpeed();
        float frictionFactor = this.mc
            .world
            .getBlockState(new Mutable().set(this.mc.player.getX(), this.getBoundingBox().getMin(Axis.Y) - move.getY(), this.mc.player.getZ()))
            .getBlock()
            .getSlipperiness()
            * 0.91F;
        float n6 = this.mc.player.hasStatusEffect(StatusEffects.JUMP_BOOST) && this.mc.player.isUsingItem()
            ? 0.88F
            : (float) (oldSpeed > 0.32 && this.mc.player.isUsingItem() ? 0.88 : 0.91F);
        if (this.mc.player.isOnGround()) {
            n6 = frictionFactor * (this.onGroundFrictionFactor.get()).floatValue();
        }

        float n7 = (float) (0.1631F / Math.pow(n6, 3.0));
        float n8;
        if (this.mc.player.isOnGround()) {
            n8 = speedAttributes * n7 * (this.onGroundSpeedFactor.get()).floatValue();
            if (move.y > 0.0) {
                n8 += this.boost.get() == Boost.Elytra && InventoryUtility.getElytra() != -1 && disabled ? 0.65F : 0.2F;
            }

            disabled = false;
        } else {
            n8 = 0.0255F + (this.speedFactor.get()).floatValue();
        }

        boolean noslow = false;
        double max2 = oldSpeed + n8;
        double max = 0.0;
        if (this.mc.player.isUsingItem() && move.getY() <= 0.0) {
            double n10 = oldSpeed + n8 * 0.25;
            double motionY2 = move.getY();
            if (motionY2 != 0.0 && Math.abs(motionY2) < 0.08) {
                n10 += 0.055;
            }

            if (max2 > (max = Math.max(0.043, n10))) {
                noslow = true;
                noSlowTicks++;
            } else {
                noSlowTicks = Math.max(noSlowTicks - 1, 0);
            }
        } else {
            noSlowTicks = 0;
        }

        if (noSlowTicks > 3) {
            max2 = max - 0.019;
        } else {
            max2 = Math.max(noslow ? 0.0 : 0.25, max2) - (this.mc.player.age % 2 == 0 ? 0.001 : 0.002);
        }

        contextFriction = n6;
        if (!this.mc.player.isOnGround()) {
            needSprintState = !this.mc.player.lastSprinting;
            needSwap = true;
        } else {
            needSprintState = false;
        }

        return max2;
    }

    public Box getBoundingBox() {
        return new Box(
            this.mc.player.getX() - 0.1,
            this.mc.player.getY(),
            this.mc.player.getZ() - 0.1,
            this.mc.player.getX() + 0.1,
            this.mc.player.getY() + 1.0,
            this.mc.player.getZ() + 0.1
        );
    }

    public float getAIMoveSpeed() {
        boolean prevSprinting = this.mc.player.isSprinting();
        this.mc.player.setSprinting(false);
        float speed = this.mc.player.getMovementSpeed() * 1.3F;
        this.mc.player.setSprinting(prevSprinting);
        return speed;
    }

    public void disabler(int elytra) {
        if (elytra != -1) {
            if (System.currentTimeMillis() - disableTime > 190L) {
                if (elytra != -2) {
                    this.mc.interactionManager.clickSlot(0, elytra, 1, SlotActionType.PICKUP, this.mc.player);
                    this.mc.interactionManager.clickSlot(0, 6, 1, SlotActionType.PICKUP, this.mc.player);
                }

                this.mc.player.networkHandler.sendPacket(new ClientCommandC2SPacket(this.mc.player, Mode.START_FALL_FLYING));
                this.mc.player.networkHandler.sendPacket(new ClientCommandC2SPacket(this.mc.player, Mode.START_FALL_FLYING));
                if (elytra != -2) {
                    this.mc.interactionManager.clickSlot(0, 6, 1, SlotActionType.PICKUP, this.mc.player);
                    this.mc.interactionManager.clickSlot(0, elytra, 1, SlotActionType.PICKUP, this.mc.player);
                }

                disableTime = System.currentTimeMillis();
            }

            disabled = true;
        }
    }

    private double wrapDS(double x, double z) {
        double diffX = x - this.mc.player.getX();
        double diffZ = z - this.mc.player.getZ();
        return Math.toDegrees(Math.atan2(diffZ, diffX)) - 90.0;
    }

    @EventHandler
    public void onMove(EventPlayerMove event) {
        int elytraSlot = InventoryUtility.getElytra();
        if (this.boost.get() == Boost.Elytra
            && elytraSlot != -1
            && PlayerUtils.isMoving()
            && !this.mc.player.isOnGround()
            && this.mc.world.getBlockCollisions(this.mc.player, this.mc.player.getBoundingBox().offset(0.0, event.getY(), 0.0)).iterator().hasNext()
            && disabled) {
            oldSpeed = this.setSpeed.get();
        }

        if (this.canStrafe()) {
            if (KillAura.closestEntity != null && ((KillAura) Modules.get().get(KillAura.class)).isActive()) {
                double speed = this.calculateSpeed(event);
                Entity entity = KillAura.closestEntity;
                double wrap = Math.atan2(this.mc.player.getZ() - entity.getZ(), this.mc.player.getX() - entity.getX());
                double var12 = wrap
                    + (switchDir ? speed / Math.sqrt(this.mc.player.squaredDistanceTo(entity)) : -(speed / Math.sqrt(this.mc.player.squaredDistanceTo(entity))));
                double x = entity.getX() + this.getDistance.get() * Math.cos(var12);
                double z = entity.getZ() + this.getDistance.get() * Math.sin(var12);
                if (this.needToSwitch(x, z)) {
                    switchDir = !switchDir;
                    wrap = var12
                        + 2.0 * (switchDir ? speed / Math.sqrt(this.mc.player.squaredDistanceTo(entity)) : -(speed / Math.sqrt(this.mc.player.squaredDistanceTo(entity))));
                    x = entity.getX() + this.getDistance.get() * Math.cos(wrap);
                    z = entity.getZ() + this.getDistance.get() * Math.sin(wrap);
                }

                event.setX(speed * -Math.sin(Math.toRadians(this.wrapDS(x, z))));
                event.setZ(speed * Math.cos(Math.toRadians(this.wrapDS(x, z))));
                event.cancel();
            }
        } else {
            oldSpeed = 0.0;
        }
    }

    @EventHandler
    public void updateValues(EventSync e) {
        oldSpeed = Math.hypot(this.mc.player.getX() - this.mc.player.prevX, this.mc.player.getZ() - this.mc.player.prevZ) * contextFriction;
        if (this.mc.player.isOnGround() && this.jump.get() && KillAura.closestEntity != null && this.ticks <= 0) {
            if (this.lowHop.get()) {
                this.mc.player.setVelocity(this.mc.player.getVelocity().x, this.motion.get(), this.mc.player.getVelocity().z);
            } else {
                this.mc.player.jump();
            }

            this.ticks = this.onGroundTicks.get();
        }

        if (this.ticks > 0) {
            this.ticks--;
        }

        if (this.mc.player.isSubmergedInWater()) {
            waterTicks = 10;
        } else {
            waterTicks--;
        }
    }

    @EventHandler
    public void onUpdate(EventPlayerUpdate event) {
        if (this.boost.get() == Boost.Elytra
            && InventoryUtility.getElytra() != -1
            && !this.mc.player.isOnGround()
            && this.mc.player.fallDistance > 0.0F
            && !disabled) {
            this.disabler(InventoryUtility.getElytra());
        }
    }

    @EventHandler
    public void onPacketReceive(Receive e) {
        if (e.packet instanceof PlayerPositionLookS2CPacket) {
            oldSpeed = 0.0;
        }

        EntityVelocityUpdateS2CPacket velocity;
        if (e.packet instanceof EntityVelocityUpdateS2CPacket
            && (velocity = (EntityVelocityUpdateS2CPacket) e.packet).getEntityId() == this.mc.player.getId()
            && this.boost.get() == Boost.Damage) {
            if (this.mc.player.isOnGround()) {
                return;
            }

            int vX = (int) velocity.getVelocityX();
            int vZ = (int) velocity.getVelocityZ();
            if (vX < 0) {
                vX *= -1;
            }

            if (vZ < 0) {
                vZ *= -1;
            }

            oldSpeed = (vX + vZ) / (this.velReduction.get() * 1000.0);
            oldSpeed = Math.min(oldSpeed, this.maxVelocitySpeed.get());
            ((ISPacketEntityVelocity) velocity).setMotionX(0);
            ((ISPacketEntityVelocity) velocity).setMotionY(0);
            ((ISPacketEntityVelocity) velocity).setMotionZ(0);
        }
    }

    @EventHandler
    public void actionEvent(EventSprint eventAction) {
        if (needSwap) {
            eventAction.setSprintState(!this.mc.player.lastSprinting);
            needSwap = false;
        }
    }

    private static enum Boost {
        None,
        Elytra,
        Damage;
    }
}
