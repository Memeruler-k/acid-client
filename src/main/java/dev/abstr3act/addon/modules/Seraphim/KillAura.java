package dev.abstr3act.addon.modules.Seraphim;

import dev.abstr3act.addon.Compassion;
import dev.abstr3act.addon.command.ForceTargetCommand;
import dev.abstr3act.addon.events.legacy.HandleInputEvent;
import dev.abstr3act.addon.events.legacy.WorldEvent;
import dev.abstr3act.addon.hud.notifications.NotificationsHudElement;
import dev.abstr3act.addon.module.SeraphimModule;
import dev.abstr3act.addon.modules.Seraphim.AutoBlock.BlinkUtil;
import dev.abstr3act.addon.notifications.Notification;
import dev.abstr3act.addon.notifications.NotificationsManager;
import dev.abstr3act.addon.utils.PlayerManager;
import dev.abstr3act.addon.utils.RotationUtil;
import dev.abstr3act.addon.utils.TargetUtil;
import dev.abstr3act.addon.utils.fragment.blinkUtils.PacketUtil;
import dev.abstr3act.addon.utils.pathfinder.MainPathFinder;
import meteordevelopment.meteorclient.events.packets.PacketEvent.Receive;
import meteordevelopment.meteorclient.settings.DoubleSetting.Builder;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.utils.entity.DamageUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Blocks;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket.PositionAndOnGround;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class KillAura extends SeraphimModule {
    public static Entity closestEntity = null;
    public static Entity lockedTarget = null;
    public static boolean wasTargeting = false;
    public static boolean isBlocking = false;
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
    public final Setting<AutoBlock> autoBlockMode = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) new meteordevelopment.meteorclient.settings.EnumSetting.Builder()
                .name("Auto Block"))
                .description("."))
                .defaultValue(AutoBlock.None))
                .build()
        );
    private final Setting<Double> targetRange = this.sgGeneral
        .add(((Builder) ((Builder) new Builder().name("TargetRange")).description("Range")).sliderRange(0.0, 8.0).defaultValue(4.0).build());
    private final Setting<Double> attackRange = this.sgGeneral
        .add(((Builder) ((Builder) new Builder().name("AttackRange")).description("Range")).sliderRange(0.0, 6.0).defaultValue(3.0).build());
    private final Setting<APSMode> apsMode = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) new meteordevelopment.meteorclient.settings.EnumSetting.Builder()
                .name("APS Mode"))
                .description("Range"))
                .defaultValue(APSMode.Randomized))
                .build()
        );
    private final Setting<Integer> maxCps = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("Max CPS"))
                .description("."))
                .sliderRange(1, 20)
                .defaultValue(12))
                .visible(() -> ((APSMode) this.apsMode.get()).equals(APSMode.Randomized)))
                .build()
        );
    private final Setting<Integer> minCps = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("Min CPS"))
                .description("."))
                .sliderRange(1, 20)
                .defaultValue(6))
                .visible(() -> ((APSMode) this.apsMode.get()).equals(APSMode.Randomized)))
                .build()
        );
    private final Setting<SwingOrder> swingOrderMode = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) new meteordevelopment.meteorclient.settings.EnumSetting.Builder()
                .name("Swing Order"))
                .description("."))
                .defaultValue(SwingOrder.New))
                .build()
        );
    private final Setting<Boolean> targetLock = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("Lock Target"))
                .description("."))
                .defaultValue(false))
                .build()
        );
    private final Setting<Boolean> onlyWeapon = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("OnlyWeapon"))
                .description("."))
                .defaultValue(false))
                .build()
        );
    private final Setting<Boolean> rotations = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("Rotations"))
                .description("."))
                .defaultValue(true))
                .build()
        );
    private final Setting<Double> rotationSpeed = this.sgGeneral
        .add(
            ((Builder) ((Builder) ((Builder) new Builder().name("RotationSpeed")).description("."))
                .sliderRange(1.0, 180.0)
                .defaultValue(40.0)
                .visible(this.rotations::get))
                .build()
        );
    private final Setting<Boolean> randomizedRotation = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("Randomized Rotation"))
                .description("."))
                .defaultValue(true))
                .visible(this.rotations::get))
                .build()
        );
    private final Setting<Double> randomTurnSpeed = this.sgGeneral
        .add(
            ((Builder) ((Builder) ((Builder) new Builder().name("Random Turn Value")).description("."))
                .sliderRange(1.0, 20.0)
                .defaultValue(15.0)
                .visible(() -> this.rotations.get() && this.randomizedRotation.get()))
                .build()
        );
    private final Setting<Boolean> onlyPlayers = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("OnlyPlayers"))
                .description("."))
                .defaultValue(true))
                .build()
        );
    private final Setting<Boolean> tpReach = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("ExtendReach"))
                .description("."))
                .defaultValue(true))
                .build()
        );
    private final Setting<Double> tpExtendedReach = this.sgGeneral
        .add(
            ((Builder) ((Builder) ((Builder) new Builder().name("TP Extended Reach")).description("."))
                .sliderRange(0.0, 100.0)
                .defaultValue(40.0)
                .visible(this.tpReach::get))
                .build()
        );
    private final Setting<Boolean> tpBack = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("TP Back"))
                .description("."))
                .defaultValue(true))
                .visible(this.tpReach::get))
                .build()
        );
    private final Setting<Boolean> deactivateOnWorld = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("AutoToggle"))
                .description("Deactivate on world change"))
                .defaultValue(false))
                .build()
        );
    private final Setting<Boolean> shieldBreaker = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("ShieldBreaker"))
                .description("."))
                .defaultValue(true))
                .build()
        );
    private final Setting<Boolean> instant = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("instant"))
                .description("."))
                .defaultValue(true))
                .visible(this.shieldBreaker::get))
                .build()
        );
    private final Setting<Boolean> smartCrit = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("SmartCrit"))
                .description("."))
                .defaultValue(true))
                .build()
        );
    private final Setting<Double> critDistance = this.sgGeneral
        .add(
            ((Builder) ((Builder) ((Builder) new Builder().name("CritFallDistance")).description("."))
                .defaultValue(1.14)
                .min(0.0)
                .sliderRange(0.0, 3.0)
                .visible(this.smartCrit::get))
                .build()
        );
    private final Setting<Boolean> onlySpace = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("OnlyCrit"))
                .description("."))
                .defaultValue(false))
                .visible(this.smartCrit::get))
                .build()
        );
    private final Setting<Boolean> autoJump = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("AutoJump"))
                .description("."))
                .defaultValue(false))
                .visible(this.smartCrit::get))
                .build()
        );
    public boolean animation = false;
    private long lastAttackTime = 0L;
    private int randomDelay = 0;
    private int abTick = 0;
    private boolean wasBlinking = false;
    private boolean wasPacketBlocking = false;
    private int unBlockTick = 0;

    public KillAura() {
        super(Compassion.SERAPHIM, "KillAuraV2", ".");
    }

    private boolean autoCrit() {
        boolean reasonForSkipCrit = !this.smartCrit.get()
            || this.mc.player.getAbilities().flying
            || this.mc.player.isFallFlying()
            || this.mc.player.hasStatusEffect(StatusEffects.BLINDNESS)
            || this.mc.player.isHoldingOntoLadder()
            || this.mc.world.getBlockState(BlockPos.ofFloored(this.mc.player.getPos())).getBlock() == Blocks.COBWEB;
        if (closestEntity instanceof LivingEntity
            && DamageUtils.getAttackDamage(this.mc.player, (LivingEntity) closestEntity) >= ((LivingEntity) closestEntity).getHealth()) {
            return true;
        } else if (this.mc.player.fallDistance > 1.0F && this.mc.player.fallDistance < this.critDistance.get()) {
            return false;
        } else if (!this.mc.options.jumpKey.isPressed() && !this.onlySpace.get() && !this.autoJump.get()) {
            return true;
        } else if (this.mc.player.isInLava()) {
            return true;
        } else {
            return reasonForSkipCrit ? true : !this.mc.player.isOnGround() && this.mc.player.fallDistance > 0.0F;
        }
    }

    private float getRealTargetReach() {
        float reach = (this.targetRange.get()).floatValue();
        if (this.mc.player.isCreative() && reach <= 4.5F) {
            reach++;
        }

        if (this.tpReach.get()) {
            reach = (float) (reach + this.tpExtendedReach.get());
        }

        return reach;
    }

    @EventHandler
    public void onWorldEvent(WorldEvent event) {
        if (this.deactivateOnWorld.get()) {
            this.toggle();
        }
    }

    private float getRealAttackReach() {
        float reach = (this.attackRange.get()).floatValue();
        if (this.mc.player.isCreative() && reach <= 4.5F) {
            reach++;
        }

        if (this.tpReach.get()) {
            reach = (float) (reach + this.tpExtendedReach.get());
        }

        return reach;
    }

    public String getInfoString() {
        return ((APSMode) this.apsMode.get()).equals(APSMode.Randomized)
            ? this.minCps.get() + "-" + this.maxCps.get()
            : ((APSMode) this.apsMode.get()).name();
    }

    public void onDeactivate() {
        if (this.mc.player != null) {
            this.resetPacketUnblocking();
        }

        this.wasPacketBlocking = false;
        wasTargeting = false;
        this.unBlockTick = 0;
        isBlocking = false;
        lockedTarget = null;
        this.animation = false;
        this.reset();
    }

    public void onActivate() {
    }

    public boolean isWeapon(Item item) {
        return item instanceof AxeItem || item instanceof SwordItem || item instanceof MaceItem || item instanceof TridentItem;
    }

    public boolean shouldAnimate() {
        if (!this.animation && this.mc.player != null && this.mc.player.getMainHandStack().getItem() instanceof SwordItem) {
            this.mc.gameRenderer.firstPersonRenderer.resetEquipProgress(Hand.MAIN_HAND);
        }

        return this.animation;
    }

    @EventHandler
    public void onHandleInput(HandleInputEvent event) {
        if (this.mc.player != null && this.mc.world != null) {
            if (!TargetUtil.noKillAura && !this.mc.player.isSpectator()) {
                List<Entity> worldEntities = new ArrayList<>();

                for (Entity entity : this.mc.world.getEntities()) {
                    if (entity instanceof LivingEntity
                        && !((LivingEntity) entity).isDead()
                        && !(entity instanceof ArmorStandEntity)
                        && !(entity instanceof ClientPlayerEntity)
                        && !TargetUtil.isBot((LivingEntity) entity)
                        && this.mc.player.distanceTo(entity) <= this.getRealTargetReach()
                        && (!this.onlyPlayers.get() || entity instanceof PlayerEntity)) {
                        if (entity instanceof PlayerEntity && !Friends.get().isFriend((PlayerEntity) entity)) {
                            worldEntities.add(entity);
                        } else if (!(entity instanceof PlayerEntity)) {
                            worldEntities.add(entity);
                        }
                    }
                }

                if (worldEntities.isEmpty()) {
                    if (((AutoBlock) this.autoBlockMode.get()).equals(AutoBlock.Vanilla)) {
                        this.resetPacketUnblocking();
                    }

                    this.resetVisualBlocking();
                    this.reset();
                } else {
                    List<Entity> sortedEntities = worldEntities.stream().sorted(Comparator.comparingDouble(entityx -> this.mc.player.distanceTo(entityx))).toList();
                    Entity entityx = (Entity) (ForceTargetCommand.target != null && this.mc.player.distanceTo(ForceTargetCommand.target) <= this.targetRange.get()
                        ? ForceTargetCommand.target
                        : (sortedEntities.isEmpty() ? null : sortedEntities.get(0)));
                    if (lockedTarget == null || !worldEntities.contains(lockedTarget)) {
                        lockedTarget = entityx;
                    }

                    closestEntity = lockedTarget != null && this.mc.player.distanceTo(lockedTarget) <= this.targetRange.get() ? lockedTarget : entityx;
                    if (this.isWeapon(this.mc.player.getMainHandStack().getItem()) || !this.onlyWeapon.get()) {
                        if (this.rotations.get()) {
                            RotationUtil.aimAtEntity(
                                closestEntity,
                                (this.rotationSpeed.get()).floatValue(),
                                this.randomizedRotation.get(),
                                (this.randomTurnSpeed.get()).floatValue(),
                                0.0F,
                                0.2F
                            );
                        }

                        this.animation = false;
                        if (this.mc.player.distanceTo(closestEntity) <= this.targetRange.get()
                            && ((AutoBlock) this.autoBlockMode.get()).equals(AutoBlock.Fake)) {
                            this.animation = true;
                        }

                        if (this.mc.player.distanceTo(closestEntity) > this.getRealAttackReach()) {
                            this.animation = false;
                            this.resetVisualBlocking();
                            switch ((AutoBlock) this.autoBlockMode.get()) {
                                case HypixelFull:
                                    if (this.wasBlinking) {
                                        BlinkUtil.sync(true, true);
                                        BlinkUtil.stopBlink();
                                        this.wasBlinking = false;
                                    }
                                case Fake:
                                case None:
                                default:
                                    break;
                                case Interact:
                                    this.mc.player.input.pressingRight = false;
                                case Vanilla:
                                    this.resetPacketUnblocking();
                            }
                        } else {
                            wasTargeting = true;
                            isBlocking = true;
                            this.unBlockTick = 0;
                            if (this.attackCooldownMath(closestEntity)) {

                                float yaw = RotationUtil.currentYaw != null ? RotationUtil.currentYaw.floatValue() : this.mc.player.getYaw();
                                float pitch = RotationUtil.currentPitch != null ? RotationUtil.currentPitch.floatValue() : this.mc.player.getPitch();
                                this.abTick++;
                                switch ((AutoBlock) this.autoBlockMode.get()) {
                                    case HypixelFull:
                                        if (this.mc.player.getMainHandStack().getItem() instanceof SwordItem && this.abTick >= 1) {
                                            BlinkUtil.doBlink();
                                            PacketUtil.sendPacket(new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, 0, this.mc.player.getYaw(), this.mc.player.getPitch()));
                                            this.wasBlinking = true;
                                        }
                                    case Fake:
                                    case None:
                                    default:
                                        break;
                                    case Interact:
                                        this.mc.player.input.pressingRight = true;
                                    case Vanilla:
                                        if (this.mc.player.getOffHandStack().getItem() instanceof ShieldItem) {
                                            PacketUtil.sendPacket(new PlayerInteractItemC2SPacket(Hand.OFF_HAND, 0, this.mc.player.getYaw(), this.mc.player.getPitch()));
                                        } else if (this.mc.player.getMainHandStack().getItem() instanceof SwordItem && !this.wasPacketBlocking) {
                                            PacketUtil.sendPacket(new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, 0, this.mc.player.getYaw(), this.mc.player.getPitch()));
                                        }
                                }

                                this.attackToEntity(closestEntity, this.tpReach.get() && this.mc.player.distanceTo(closestEntity) >= this.attackRange.get());
                                switch ((AutoBlock) this.autoBlockMode.get()) {
                                    case HypixelFull:
                                        if (this.mc.player.getMainHandStack().getItem() instanceof SwordItem && this.abTick >= 1) {
                                            BlinkUtil.sync(true, true);
                                            PacketUtil.sendPacket(new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, 0, this.mc.player.getYaw(), this.mc.player.getPitch()));
                                            BlinkUtil.stopBlink();
                                            this.abTick = 0;
                                        }
                                    case Fake:
                                    case None:
                                    default:
                                        break;
                                    case Interact:
                                        this.mc.player.input.pressingRight = false;
                                    case Vanilla:
                                        if (this.mc.player.getOffHandStack().getItem() instanceof ShieldItem) {
                                            PacketUtil.sendPacket(new PlayerInteractItemC2SPacket(Hand.OFF_HAND, 0, this.mc.player.getYaw(), this.mc.player.getPitch()));
                                        } else if (this.mc.player.getMainHandStack().getItem() instanceof SwordItem && this.wasPacketBlocking) {
                                            PacketUtil.sendPacket(new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, 0, this.mc.player.getYaw(), this.mc.player.getPitch()));
                                        }

                                        this.wasPacketBlocking = true;
                                }
                            }
                        }
                    }
                }
            } else {
                wasTargeting = false;
                this.unBlockTick = 0;
                isBlocking = false;
                this.reset();
            }
        }
    }

    private boolean attackCooldownMath(Entity target) {
        switch ((APSMode) this.apsMode.get()) {
            case Cooldown:
                return this.mc.player.getAttackCooldownProgress(0.0F) >= 1.0F;
            case Randomized:
                long currentTime = System.currentTimeMillis();
                if (this.lastAttackTime == 0L || currentTime - this.lastAttackTime >= this.randomDelay) {
                    this.lastAttackTime = currentTime;
                    this.randomDelay = (int) (
                        1000.0 / ((this.minCps.get()).intValue() + Math.random() * (this.maxCps.get() - this.minCps.get()))
                    );
                    return true;
                }
            default:
                return false;
            case HurtTime:
                return ((LivingEntity) target).hurtTime <= 5;
            case NoDelay:
                return true;
        }
    }

    private void resetPacketUnblocking() {
        if (this.wasPacketBlocking) {
            int oldSlot = this.mc.player.getInventory().selectedSlot;
            if (oldSlot == 0) {
                PacketUtil.sendPacket(new UpdateSelectedSlotC2SPacket(oldSlot + 1));
                PacketUtil.sendPacket(new UpdateSelectedSlotC2SPacket(oldSlot));
            } else {
                PacketUtil.sendPacket(new UpdateSelectedSlotC2SPacket(oldSlot - 1));
                PacketUtil.sendPacket(new UpdateSelectedSlotC2SPacket(oldSlot));
            }

            this.wasPacketBlocking = false;
        }
    }

    private void resetVisualBlocking() {
        if (wasTargeting) {
            this.unBlockTick++;
            switch (this.unBlockTick) {
                case 5:
                    if (this.mc.player != null && this.mc.player.getMainHandStack().getItem() instanceof SwordItem) {
                        this.mc.gameRenderer.firstPersonRenderer.resetEquipProgress(Hand.MAIN_HAND);
                    }

                    isBlocking = false;
                    break;
                case 6:
                    isBlocking = true;
            }

            if (this.unBlockTick >= 8) {
                isBlocking = false;
                this.unBlockTick = 0;
                wasTargeting = false;
            }
        }
    }

    @EventHandler
    public void onPacketEvent(Receive event) {
        if (event.packet instanceof EntityStatusS2CPacket status
            && closestEntity instanceof PlayerEntity
            && status.getStatus() == 30
            && status.getEntity(this.mc.world) != null
            && status.getEntity(this.mc.world) == closestEntity) {
            NotificationsManager.add(
                new Notification(
                    "KillAura", "Successfully destroyed " + closestEntity.getName().getString() + "'s shield", Color.WHITE, NotificationsHudElement.icon.ENABLE
                )
            );
        }
    }

    private void attackToEntity(Entity target, boolean tpMode) {
        ArrayList<Vec3d> paths = null;
        if (tpMode) {
            paths = MainPathFinder.computePath(this.mc.player.getPos(), closestEntity.getPos());
            if (paths == null || paths.isEmpty()) {
                return;
            }

            for (Vec3d path : paths) {
                PacketUtil.sendPacket(new PositionAndOnGround(path.x, path.y, path.z, true));
            }

            if (!this.tpBack.get()) {
                this.mc.player.setPosition(paths.getLast());
            }
        }

        if (this.shieldBreaker.get()) {
            PlayerManager.shieldBreaker(this.instant.get(), target);
        }

        if (this.autoCrit() || !this.smartCrit.get()) {
            switch ((SwingOrder) this.swingOrderMode.get()) {
                case Legacy:
                    this.mc.player.swingHand(Hand.MAIN_HAND);
                    this.mc.interactionManager.attackEntity(this.mc.player, target);
                    break;
                case New:
                    this.mc.interactionManager.attackEntity(this.mc.player, target);
                    this.mc.player.swingHand(Hand.MAIN_HAND);
                    break;
                case None:
                    this.mc.interactionManager.attackEntity(this.mc.player, target);
            }

            if (this.mc.player.getMainHandStack().hasEnchantments()) {
                this.mc.player.addEnchantedHitParticles(target);
            }

            if (this.mc.player.getVelocity().y < -0.1) {
                this.mc.player.addCritParticles(target);
            }

            if (tpMode && !paths.isEmpty() && this.tpBack.get()) {
                for (Vec3d path : paths.reversed()) {
                    PacketUtil.sendPacket(new PositionAndOnGround(path.x, path.y, path.z, true));
                }
            }
        }
    }

    private void reset() {
        if (closestEntity != null) {
            RotationUtil.reset();
        }

        if (((AutoBlock) this.autoBlockMode.get()).equals(AutoBlock.HypixelFull) && this.wasBlinking) {
            BlinkUtil.sync(true, true);
            BlinkUtil.stopBlink();
        }

        this.lastAttackTime = 0L;
        this.randomDelay = 0;
        this.abTick = 0;
        closestEntity = null;
        this.wasBlinking = false;
    }

    static enum APSMode {
        Cooldown,
        Randomized,
        HurtTime,
        NoDelay;
    }

    public static enum AutoBlock {
        Vanilla,
        HypixelFull,
        Fake,
        None,
        Interact;
    }

    static enum SwingOrder {
        Legacy,
        New,
        None;
    }
}
