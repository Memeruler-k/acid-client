package dev.abstr3act.addon.modules.Compassion;

import dev.abstr3act.addon.Compassion;
import dev.abstr3act.addon.events.EventPlayerUpdate;
import dev.abstr3act.addon.hud.notifications.NotificationsHudElement;
import dev.abstr3act.addon.module.CompassionModule;
import dev.abstr3act.addon.notifications.Notification;
import dev.abstr3act.addon.notifications.NotificationsManager;
import dev.abstr3act.addon.utils.PlayerManager;
import dev.abstr3act.addon.utils.math.MathUtility;
import dev.abstr3act.addon.utils.timers.Timer;
import meteordevelopment.meteorclient.events.packets.PacketEvent.Receive;
import meteordevelopment.meteorclient.events.world.TickEvent.Post;
import meteordevelopment.meteorclient.settings.BoolSetting.Builder;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.entity.fakeplayer.FakePlayerEntity;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.*;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.RaycastContext.FluidHandling;
import net.minecraft.world.RaycastContext.ShapeType;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.Random;

public final class TriggerBot extends CompassionModule {
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
    private final SettingGroup sgAttack = this.settings.createGroup("Attack");
    private final Setting<Boolean> randomRange = this.sgGeneral
        .add(((Builder) ((Builder) ((Builder) new Builder().name("RandomRange")).description(".")).defaultValue(true)).build());
    private final Setting<Double> attackRange2 = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("MinRange"))
                .description("."))
                .defaultValue(3.0)
                .min(0.0)
                .sliderRange(0.0, 7.0)
                .visible(this.randomRange::get))
                .build()
        );
    private final Setting<Boolean> swingFix = this.sgGeneral
        .add(((Builder) ((Builder) ((Builder) new Builder().name("SwingFix")).description(".")).defaultValue(true)).build());
    private final Setting<Double> targetRange = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("TargetRange"))
                .description("."))
                .defaultValue(7.0)
                .min(0.0)
                .sliderRange(0.0, 20.0)
                .build()
        );
    private final Setting<Double> attackRange = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("MaxRange"))
                .description("."))
                .defaultValue(3.0)
                .min(0.0)
                .sliderRange(0.0, 7.0)
                .build()
        );
    private final Setting<Integer> random1 = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("MinDelay"))
                .description("."))
                .defaultValue(10))
                .min(0)
                .sliderRange(0, 100)
                .build()
        );
    private final Setting<Integer> random2 = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("MaxDelay"))
                .description("."))
                .defaultValue(10))
                .min(0)
                .sliderRange(0, 100)
                .build()
        );
    private final Setting<Boolean> breakShield = this.sgGeneral
        .add(((Builder) ((Builder) ((Builder) new Builder().name("BreakShield")).description(".")).defaultValue(true)).build());
    private final Setting<Boolean> silentInstant = this.sgGeneral
        .add(
            ((Builder) ((Builder) ((Builder) ((Builder) new Builder().name("silentInstant")).description(".")).defaultValue(true)).visible(this.breakShield::get))
                .build()
        );
    private final Setting<Double> swapBackDelay = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("SwapBackDelay"))
                .description("."))
                .defaultValue(3.0)
                .min(0.0)
                .sliderRange(0.0, 7.0)
                .visible(this.breakShield::get))
                .build()
        );
    private final Setting<Boolean> onlyWeapon = this.sgGeneral
        .add(((Builder) ((Builder) ((Builder) new Builder().name("OnlyWeapon")).description(".")).defaultValue(false)).build());
    private final Setting<Boolean> smartCrit = this.sgAttack
        .add(((Builder) ((Builder) ((Builder) new Builder().name("Smart")).description(".")).defaultValue(true)).build());
    private final Setting<Double> critDistance = this.sgAttack
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("CritFallDistance"))
                .description("."))
                .defaultValue(1.14)
                .min(0.0)
                .sliderRange(0.0, 3.0)
                .visible(this.smartCrit::get))
                .build()
        );
    private final Setting<Boolean> onlySpace = this.sgAttack
        .add(((Builder) ((Builder) ((Builder) ((Builder) new Builder().name("OnlyCrit")).description(".")).defaultValue(false)).visible(this.smartCrit::get)).build());
    private final Setting<Boolean> autoJump = this.sgAttack
        .add(((Builder) ((Builder) ((Builder) ((Builder) new Builder().name("AutoJump")).description(".")).defaultValue(false)).visible(this.smartCrit::get)).build());
    private final Setting<Boolean> ignoreWalls = this.sgAttack
        .add(((Builder) ((Builder) ((Builder) new Builder().name("IgnoreWalls")).description(".")).defaultValue(false)).build());
    private final Setting<Boolean> pauseEating = this.sgAttack
        .add(((Builder) ((Builder) ((Builder) new Builder().name("PauseWhileEating")).description(".")).defaultValue(false)).build());
    private final Setting<Boolean> failSwing = this.sgAttack
        .add(((Builder) ((Builder) ((Builder) new Builder().name("FailSwing")).description(".")).defaultValue(false)).build());
    private final Setting<Integer> failChanceMax = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("Max FailChance 1"))
                .description("."))
                .defaultValue(10))
                .min(0)
                .range(0, 100)
                .sliderRange(0, 100)
                .visible(this.failSwing::get))
                .build()
        );
    private final Setting<Integer> failChanceMin = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("Min FailChance"))
                .description("."))
                .defaultValue(30))
                .min(0)
                .range(0, 100)
                .sliderRange(0, 100)
                .visible(this.failSwing::get))
                .build()
        );
    private final Setting<Integer> failDelayMax = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("FailDelay Max"))
                .description("."))
                .defaultValue(10))
                .min(0)
                .sliderRange(0, 1000)
                .visible(this.failSwing::get))
                .build()
        );
    private final Setting<Integer> failDelayMin = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("FailDelay Min"))
                .description("."))
                .defaultValue(30))
                .min(0)
                .sliderRange(0, 1000)
                .visible(this.failSwing::get))
                .build()
        );
    private final Setting<FailMode> failMode = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) new meteordevelopment.meteorclient.settings.EnumSetting.Builder()
                .name("Fail Mode"))
                .description(""))
                .defaultValue(FailMode.Client))
                .build()
        );
    int swapDelay = 0;
    boolean swapped = false;
    String var = String.valueOf(0.0);
    Timer failTimer = new Timer();
    Timer hitTimer = new Timer();
    Entity target;
    private int delay;

    public TriggerBot() {
        super(Compassion.COMPASSION, "TriggerBot", ".");
    }

    @EventHandler
    public void onAttack(EventPlayerUpdate e) {
        if (!this.mc.player.isUsingItem() || !this.pauseEating.get()) {
            if (!this.mc.options.jumpKey.isPressed() && this.mc.player.isOnGround() && this.autoJump.get()) {
                this.mc.player.jump();
            }

            if (this.delay > 0) {
                this.delay--;
            } else if (this.autoCrit()) {
                float range = MathUtility.random((this.attackRange.get()).floatValue(), (this.attackRange2.get()).floatValue());
                Entity ent = this.randomRange.get()
                    ? this.getRtxTarget(this.mc.player.getYaw(), this.mc.player.getPitch(), range, this.ignoreWalls.get())
                    : this.getRtxTarget(
                    this.mc.player.getYaw(), this.mc.player.getPitch(), (this.attackRange.get()).floatValue(), this.ignoreWalls.get()
                );
                if (ent instanceof LivingEntity) {
                    if (!(ent instanceof FakePlayerEntity)) {
                        if (this.shouldAttack(ent)) {
                            this.var = String.valueOf(this.mc.player.distanceTo(ent));
                            if (ent instanceof PlayerEntity) {
                                if (!this.isWeapon(this.mc.player.getMainHandStack().getItem()) && this.onlyWeapon.get()) {
                                    return;
                                }

                                if (((PlayerEntity) ent).isBlocking() && this.breakShield.get() && !this.silentInstant.get()) {
                                    InvUtils.swap(this.getAxeItem(), true);
                                    NotificationsManager.add(new Notification("TriggerBot", "Try to break " + ent.getName().getString() + "'s shield"));
                                    this.swapDelay = 0;
                                    this.swapped = true;
                                }
                            }

                            if (this.silentInstant.get() && PlayerManager.shieldBreaker(false, ent)) {
                                return;
                            }

                            if (this.swingFix.get()) {
                                Utils.leftClick();
                            } else if (this.shouldFail() && this.failSwing.get()) {
                                wait(() -> {
                                    switch ((FailMode) this.failMode.get()) {
                                        case Client:
                                            this.mc.player.swingHand(Hand.MAIN_HAND, false);
                                            break;
                                        case Packet:
                                            this.sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
                                            break;
                                        case Legit:
                                            Utils.leftClick();
                                    }
                                }, MathUtility.randomLong((this.failDelayMin.get()).intValue(), (this.failDelayMax.get()).intValue()));
                            } else {
                                this.mc.interactionManager.attackEntity(this.mc.player, ent);
                                this.mc.player.swingHand(Hand.MAIN_HAND);
                            }

                            this.delay = this.random1.get() >= this.random2.get()
                                ? this.random1.get()
                                : new Random().nextInt(this.random1.get(), this.random2.get());
                        }
                    }
                }
            }
        }
    }

    private boolean shouldFail() {
        int failChance = (int) MathUtility.random((float) (this.failChanceMin.get()).intValue(), (float) (this.failChanceMax.get()).intValue());
        return new Random().nextInt(100) < failChance;
    }

    public boolean shouldAttack(Entity entity) {
        boolean blocking = false;
        if (entity == null) {
            return false;
        } else if (entity instanceof FakePlayerEntity) {
            return false;
        } else if (!(entity instanceof LivingEntity)
            || !((LivingEntity) entity).isDead() && !(((LivingEntity) entity).getHealth() <= 0.0F) && entity.isLiving() && ((LivingEntity) entity).deathTime <= 0) {
            if (entity instanceof PlayerEntity) {
                if (((PlayerEntity) entity).isBlocking() && this.breakShield.get()) {
                    blocking = true;
                }

                if (Friends.get().isFriend((PlayerEntity) entity)) {
                    return false;
                }
            }

            return blocking ? false : this.isWeapon(this.mc.player.getMainHandStack().getItem()) || !this.onlyWeapon.get();
        } else {
            return false;
        }
    }

    public boolean isWeapon(Item item) {
        return item instanceof AxeItem || item instanceof SwordItem || item instanceof MaceItem;
    }

    @EventHandler
    public void onTickEvent(Post event) {
        if (this.swapped) {
            if (this.swapDelay < this.swapBackDelay.get()) {
                this.swapDelay++;
            } else {
                InvUtils.swapBack();
                this.swapped = false;
                this.swapDelay = 0;
            }
        }
    }

    @EventHandler
    public void onPacketEvent(Receive event) {
        Entity ent = this.getRtxTarget(
            this.mc.player.getYaw(), this.mc.player.getPitch(), (this.attackRange.get()).floatValue(), this.ignoreWalls.get()
        );
        if (event.packet instanceof EntityStatusS2CPacket status
            && ent instanceof PlayerEntity
            && status.getStatus() == 30
            && status.getEntity(this.mc.world) != null
            && status.getEntity(this.mc.world) == ent) {
            NotificationsManager.add(
                new Notification("TriggerBot", "Successfully destroyed " + ent.getName().getString() + "'s shield", Color.WHITE, NotificationsHudElement.icon.ENABLE)
            );
        }
    }

    public Entity getRtxTarget(float yaw, float pitch, float distance, boolean ignoreWalls) {
        Entity targetedEntity = null;
        HitResult result = ignoreWalls ? null : this.rayTrace(distance, yaw, pitch);
        Vec3d vec3d = this.mc.player.getPos().add(0.0, this.mc.player.getEyeHeight(this.mc.player.getPose()), 0.0);
        double distancePow2 = Math.pow(distance, 2.0);
        if (result != null) {
            distancePow2 = result.getPos().squaredDistanceTo(vec3d);
        }

        Vec3d vec3d2 = this.getRotationVector(pitch, yaw);
        Vec3d vec3d3 = vec3d.add(vec3d2.x * distance, vec3d2.y * distance, vec3d2.z * distance);
        Box box = this.mc.player.getBoundingBox().stretch(vec3d2.multiply(distance)).expand(1.0, 1.0, 1.0);
        EntityHitResult entityHitResult = ProjectileUtil.raycast(
            this.mc.player, vec3d, vec3d3, box, entity -> !entity.isSpectator() && entity.canHit(), distancePow2
        );
        if (entityHitResult != null) {
            Entity entity2 = entityHitResult.getEntity();
            Vec3d vec3d4 = entityHitResult.getPos();
            double g = vec3d.squaredDistanceTo(vec3d4);
            if ((g < distancePow2 || result == null) && entity2 instanceof Entity) {
                return entity2;
            }
        }

        return targetedEntity;
    }

    private int getAxeItem() {
        int slotA = this.mc.player.getInventory().selectedSlot;

        for (int i = 0; i < 9; i++) {
            ItemStack stack = this.mc.player.getInventory().getStack(i);
            if (stack.getItem() instanceof AxeItem && stack.getMaxDamage() - stack.getDamage() > 10) {
                slotA = i;
            }
        }

        return slotA;
    }

    public HitResult rayTrace(double dst, float yaw, float pitch) {
        Vec3d vec3d = this.mc.player.getCameraPosVec(this.mc.getRenderTickCounter().getTickDelta(true));
        Vec3d vec3d2 = this.getRotationVector(pitch, yaw);
        Vec3d vec3d3 = vec3d.add(vec3d2.x * dst, vec3d2.y * dst, vec3d2.z * dst);
        return this.mc.world.raycast(new RaycastContext(vec3d, vec3d3, ShapeType.OUTLINE, FluidHandling.NONE, this.mc.player));
    }

    @NotNull
    public Vec3d getRotationVector(float yaw, float pitch) {
        return new Vec3d(
            MathHelper.sin(-pitch * (float) (Math.PI / 180.0)) * MathHelper.cos(yaw * (float) (Math.PI / 180.0)),
            -MathHelper.sin(yaw * (float) (Math.PI / 180.0)),
            MathHelper.cos(-pitch * (float) (Math.PI / 180.0)) * MathHelper.cos(yaw * (float) (Math.PI / 180.0))
        );
    }

    private boolean autoCrit() {
        boolean reasonForSkipCrit = !this.smartCrit.get()
            || this.mc.player.getAbilities().flying
            || this.mc.player.isFallFlying()
            || this.mc.player.hasStatusEffect(StatusEffects.BLINDNESS)
            || this.mc.player.isHoldingOntoLadder()
            || this.mc.world.getBlockState(BlockPos.ofFloored(this.mc.player.getPos())).getBlock() == Blocks.COBWEB;
        if (this.mc.player.fallDistance > 1.0F && this.mc.player.fallDistance < this.critDistance.get()) {
            return false;
        } else if (!this.mc.options.jumpKey.isPressed() && !this.onlySpace.get() && !this.autoJump.get()) {
            return true;
        } else if (this.mc.player.isInLava()) {
            return true;
        } else {
            return reasonForSkipCrit ? true : !this.mc.player.isOnGround() && this.mc.player.fallDistance > 0.0F;
        }
    }

    public String getInfoString() {
        return this.smartCrit.get() ? "Smart" : "Static";
    }

    static enum FailMode {
        Client,
        Packet,
        Legit;
    }
}
