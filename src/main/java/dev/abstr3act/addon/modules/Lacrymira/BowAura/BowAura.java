package dev.abstr3act.addon.modules.Lacrymira.BowAura;

import dev.abstr3act.addon.Compassion;
import dev.abstr3act.addon.events.EventPlayerUpdate;
import dev.abstr3act.addon.events.EventSync;
import dev.abstr3act.addon.mixin.accessor.IEntity;
import dev.abstr3act.addon.module.LacrymiraModule;
import dev.abstr3act.addon.utils.PredictUtility;
import dev.abstr3act.addon.utils.TargetUtils;
import dev.abstr3act.addon.utils.abnormally.AChatUtils;
import dev.abstr3act.addon.utils.abnormally.TPUtil;
import dev.abstr3act.addon.utils.compassion.impl.BlockUtil;
import dev.abstr3act.addon.utils.compassion.impl.PlayerUtil;
import dev.abstr3act.addon.utils.math.MathUtility;
import dev.abstr3act.addon.utils.timers.Timer;
import meteordevelopment.meteorclient.events.packets.PacketEvent.Send;
import meteordevelopment.meteorclient.settings.EnumSetting.Builder;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.utils.entity.SortPriority;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BowItem;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket.Mode;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket.Action;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket.LookAndOnGround;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public final class BowAura extends LacrymiraModule {
    public static Timer delayTimer = new Timer();
    private static BowAura instance;
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
    private final SettingGroup sgTp = this.settings.createGroup("TpMode");
    private final SettingGroup sgSmart = this.settings.createGroup("Smart");
    private final SettingGroup sgSelection = this.settings.createGroup("Selection");
    private final Setting<SortPriority> targetLogic = this.sgGeneral
        .add(
            ((Builder) ((Builder) ((Builder) new Builder().name("TargetLogic")).description("The logic for selecting targets."))
                .defaultValue(SortPriority.LowestDistance))
                .build()
        );
    private final Setting<Double> targetRange = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("TargetRange"))
                .description("The range to target entities."))
                .defaultValue(100.0)
                .min(0.1F)
                .max(250.0)
                .build()
        );
    private final Setting<Double> searchStep = this.sgTp
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("SearchStep"))
                .description("Step size for searching."))
                .defaultValue(1.8F)
                .min(0.1F)
                .max(10.0)
                .build()
        );
    private final Setting<Integer> maxPacket = this.sgTp
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("MaxPacket"))
                .description("Maximum number of packets."))
                .defaultValue(20))
                .min(5)
                .max(100)
                .build()
        );
    private final Setting<Integer> predictSelf = this.sgTp
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("PredictSelf"))
                .description("Self-prediction value."))
                .defaultValue(0))
                .min(0)
                .max(5)
                .build()
        );
    private final Setting<Integer> predictTarget = this.sgTp
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("PredictTarget"))
                .description("Target prediction value."))
                .defaultValue(4))
                .min(0)
                .max(5)
                .build()
        );
    private final Setting<Boolean> autoShot = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("AutoShot"))
                .description("Automatically shoot."))
                .defaultValue(true))
                .build()
        );
    private final Setting<Integer> ticks = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("Delay"))
                .description("Delay in ticks."))
                .defaultValue(20))
                .min(1)
                .max(20)
                .visible(this.autoShot::get))
                .build()
        );
    private final Setting<Boolean> faster = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("Faster"))
                .description("Enable faster mode."))
                .defaultValue(true))
                .build()
        );
    private final Setting<Double> fasterMoveDistance = this.sgTp
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("FasterMoveDistance"))
                .description("Distance for faster movement."))
                .defaultValue(13.0)
                .min(1.0)
                .max(200.0)
                .visible(this.faster::get))
                .build()
        );
    private final Setting<Boolean> onlyCanFastMove = this.sgSmart
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("OnlyCanFasterMove"))
                .description("Only allow faster movement."))
                .defaultValue(true))
                .visible(this.faster::get))
                .build()
        );
    private final Setting<Boolean> onlyTarget = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("OnlyTarget"))
                .description("Only target specific entities."))
                .defaultValue(true))
                .visible(this.faster::get))
                .build()
        );
    private final Setting<Double> powerDistance = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("PowerDistance"))
                .description("Distance for power."))
                .defaultValue(50.0)
                .min(1.0)
                .max(200.0)
                .visible(this.faster::get))
                .build()
        );
    private final Setting<Double> moveDistance = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("MoveDistance"))
                .description("Movement distance."))
                .defaultValue(13.0)
                .min(1.0)
                .max(200.0)
                .build()
        );
    private final Setting<Double> delay = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("Delay"))
                .description("Delay in seconds."))
                .defaultValue(0.0)
                .min(0.0)
                .max(10.0)
                .build()
        );
    private final Setting<Boolean> onlyCanMove = this.sgSmart
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("OnlyCanMove"))
                .description("Only allow movement."))
                .defaultValue(false))
                .build()
        );
    private final Setting<Double> calcFrom = this.sgSmart
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("CalcFrom"))
                .description("Calculation starting range."))
                .defaultValue(2.0)
                .min(0.0)
                .max(5.0)
                .build()
        );
    private final Setting<Double> precise = this.sgSmart
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("Precise"))
                .description("Precision value."))
                .defaultValue(1.0)
                .min(0.5)
                .max(1.5)
                .build()
        );
    private final Setting<Integer> calcDelay = this.sgSmart
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("CalcDelay"))
                .description("Delay for calculations."))
                .defaultValue(200))
                .min(0)
                .max(1000)
                .build()
        );
    private final Setting<Boolean> rotate = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("Rotate"))
                .description("Enable rotation."))
                .defaultValue(true))
                .build()
        );
    private final Setting<Boolean> smart = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("Smart"))
                .description("Smart settings."))
                .defaultValue(true))
                .build()
        );
    private final Setting<Boolean> light = this.sgSmart
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("Light"))
                .description("Enable light."))
                .defaultValue(true))
                .build()
        );
    private final Setting<Boolean> thread = this.sgSmart
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("Thread"))
                .description("Enable threading."))
                .defaultValue(true))
                .build()
        );
    private final Setting<Double> calcRange = this.sgSmart
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("CalcRange"))
                .description("Calculation range."))
                .defaultValue(3.0)
                .min(0.0)
                .max(5.0)
                .build()
        );
    private final Setting<Double> addY = this.sgTp
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("AddY"))
                .description("Offset for the Y-axis."))
                .defaultValue(1.0)
                .min(1.0)
                .max(5.0)
                .build()
        );
    private final Setting<Boolean> bow = this.sgSelection
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("Bows"))
                .description("Include bows."))
                .defaultValue(true))
                .build()
        );
    private final Setting<Boolean> pearls = this.sgSelection
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("EPearls"))
                .description("Include ender pearls."))
                .defaultValue(true))
                .build()
        );
    private final Setting<Boolean> xp = this.sgSelection
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("XP"))
                .description("Include experience."))
                .defaultValue(true))
                .build()
        );
    private final Setting<Boolean> eggs = this.sgSelection
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("Eggs"))
                .description("Include eggs."))
                .defaultValue(true))
                .build()
        );
    private final Setting<Boolean> potions = this.sgSelection
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("SplashPotions"))
                .description("Include splash potions."))
                .defaultValue(true))
                .build()
        );
    private final Setting<Boolean> snowballs = this.sgSelection
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("Snowballs"))
                .description("Include snowballs."))
                .defaultValue(true))
                .build()
        );
    private final Timer calcTimer = new Timer();
    private PlayerEntity target;
    private Vec3d tpPos;

    public BowAura() {
        super(Compassion.LACRYMIRA, "BowAura", ".");
        instance = this;
    }

    public static BowAura getInstance() {
        return instance;
    }

    public void onDeactivate() {
        this.target = null;
        this.tpPos = null;
    }

    public void onActivate() {
        this.target = null;
        this.tpPos = null;
    }

    @EventHandler
    public void onThread(EventSync event) {
        if (!fullNullCheck()) {
            if (this.thread.get()) {
                this.doCalc();
            }
        }
    }

    @EventHandler
    public void onSync(EventSync event) {
        if (this.mc.player.getActiveItem().getItem() instanceof BowItem
            && this.autoShot.get()
            && this.mc.player.getItemUseTime() >= this.ticks.get()) {
            this.sendPacket(new PlayerActionC2SPacket(Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, this.mc.player.getHorizontalFacing()));
            this.sendSequencedPacket(
                id -> new PlayerInteractItemC2SPacket(
                    this.mc.player.getOffHandStack().getItem() == Items.BOW ? Hand.OFF_HAND : Hand.MAIN_HAND, id, this.mc.player.getYaw(), this.mc.player.getPitch()
                )
            );
            this.mc.player.stopUsingItem();
        }
    }

    @EventHandler
    public void onUpdate(EventPlayerUpdate event) {
        if (!fullNullCheck()) {
            this.target = TargetUtils.getPlayerTarget(this.targetRange.get(), (SortPriority) this.targetLogic.get());
            if (!this.thread.get()) {
                this.doCalc();
            }
        }
    }

    public void doCalc() {
        if (this.target != null) {
            Vec3d targetVec = PredictUtility.predictPosition(this.target, this.predictTarget.get());
            if (this.smart.get()) {
                if (!BlockUtil.checkCanMove(
                    targetVec.add(0.0, this.addY.get(), 0.0),
                    targetVec.add(0.0, this.addY.get(), 0.0),
                    dev.abstr3act.addon.utils.abnormally.BlockUtil.getBufferMap(),
                    PlayerUtil.PlayerState.Normal
                )
                    || !(targetVec.distanceTo(this.target.getPos()) <= Math.pow(this.calcFrom.get(), 2.0))
                    || this.onlyCanMove.get()
                    && !dev.abstr3act.addon.utils.abnormally.BlockUtil.checkCanMove(
                    this.mc.player.getPos(),
                    targetVec.add(0.0, this.addY.get(), 0.0),
                    dev.abstr3act.addon.utils.abnormally.BlockUtil.getBufferMap(),
                    PlayerUtil.PlayerState.Normal
                )) {
                    if (this.calcTimer.passed((long) (this.calcDelay.get()).intValue())
                        && targetVec.distanceTo(this.target.getPos()) <= Math.pow(this.calcFrom.get(), 2.0)) {
                        this.tpPos = null;

                        for (Vec3d vec : Utils.getVecSphere((this.calcRange.get()).floatValue(), targetVec, this.precise.get())) {
                            Vec3d aimVec = Utils.getVisiblePoint(
                                vec.add(0.0, this.mc.player.getEyeHeight(this.mc.player.getPose()), 0.0),
                                ((IEntity) this.mc.player)
                                    .getDimensions()
                                    .getBoxAt(
                                        this.smart.get()
                                            ? (targetVec.distanceTo(this.target.getPos()) <= Math.pow(this.calcFrom.get(), 2.0) ? targetVec : this.target.getPos())
                                            : targetVec
                                    )
                            );
                            if (BlockUtil.checkCanMove(vec, vec, dev.abstr3act.addon.utils.abnormally.BlockUtil.getBufferMap(), PlayerUtil.PlayerState.Normal)
                                && Utils.canSee(
                                vec.add(0.0, this.mc.player.getEyeHeight(this.mc.player.getPose()), 0.0), ((IEntity) this.mc.player).getDimensions().getBoxAt(targetVec)
                            )
                                && !vec.equals(targetVec)
                                && (
                                !this.faster.get()
                                    || !this.onlyCanFastMove.get()
                                    || !this.getFarVec3d(vec.add(0.0, this.mc.player.getEyeHeight(this.mc.player.getPose()), 0.0), aimVec, -this.powerDistance.get())
                                    .equals(vec.add(0.0, this.mc.player.getEyeHeight(this.mc.player.getPose()), 0.0))
                            )
                                && (
                                !this.onlyCanMove.get()
                                    || BlockUtil.checkCanMove(
                                    this.mc.player.getPos(), vec, dev.abstr3act.addon.utils.abnormally.BlockUtil.getBufferMap(), PlayerUtil.PlayerState.Normal
                                )
                            )) {
                                this.tpPos = vec;
                                if (this.light.get()) {
                                    break;
                                }
                            }
                        }

                        this.calcTimer.reset();
                    } else {
                        this.tpPos = targetVec;
                    }
                } else {
                    this.tpPos = targetVec.add(0.0, this.addY.get(), 0.0);
                }
            } else {
                this.tpPos = targetVec.add(0.0, this.addY.get(), 0.0);
            }
        }
    }

    @EventHandler(
        priority = 201
    )
    private void onPacketSend(Send event) {
        if (!fullNullCheck() && delayTimer.passedMs((long) (this.delay.get() * 1000.0))) {
            if (event.packet instanceof PlayerActionC2SPacket
                && ((PlayerActionC2SPacket) event.packet).getAction() == Action.RELEASE_USE_ITEM
                && this.mc.player.getActiveItem().getItem() == Items.BOW
                && this.bow.get()
                || event.packet instanceof PlayerInteractItemC2SPacket
                && ((PlayerInteractItemC2SPacket) event.packet).getHand() == Hand.MAIN_HAND
                && (
                this.mc.player.getMainHandStack().getItem() == Items.ENDER_PEARL && this.pearls.get()
                    || this.mc.player.getMainHandStack().getItem() == Items.EXPERIENCE_BOTTLE && this.xp.get()
                    || this.mc.player.getMainHandStack().getItem() == Items.EGG && this.eggs.get()
                    || this.mc.player.getMainHandStack().getItem() == Items.SPLASH_POTION && this.potions.get()
                    || this.mc.player.getMainHandStack().getItem() == Items.SNOWBALL && this.snowballs.get()
            )) {
                if (this.target == null) {
                    if (this.faster.get() && !this.onlyTarget.get()) {
                        Vec3d farVec = this.getFarVec3d(
                            this.mc.player.getEyePos(), this.mc.getCameraEntity().raycast(1.0, 0.0F, false).getPos(), -this.powerDistance.get()
                        );
                        this.doSpoofs(this.mc.player.getPos(), farVec);
                    }
                } else if (this.tpPos != null) {
                    this.doTpAura();
                }

                delayTimer.reset();
            }
        }
    }

    public void doSpoofs(Vec3d vec3d, Vec3d targetPos) {
        this.sendPacket(new ClientCommandC2SPacket(this.mc.player, Mode.START_SPRINTING));
        TPUtil.doTp(vec3d, targetPos, this.fasterMoveDistance.get(), false);
        this.sendPacket(new ClientCommandC2SPacket(this.mc.player, Mode.STOP_SPRINTING));
        TPUtil.sendMovePacket(vec3d, false);
    }

    private void doTpAura() {
        Vec3d targetVec = PredictUtility.predictPosition(this.target, this.predictTarget.get());
        Vec3d playerPos = PredictUtility.predictPosition(this.mc.player, this.predictSelf.get());
        Vec3d vec3d = TPUtil.findVClipVecToMove(playerPos, targetVec, this.searchStep.get(), false);
        if (vec3d != null && this.tpPos != null) {
            int maxPacket = this.calculateMaxPacket(playerPos, vec3d, this.tpPos, (this.moveDistance.get()).floatValue());
            if (maxPacket > this.maxPacket.get()) {
                AChatUtils.sendMsgLacrymira(Text.of("TP packet limit exceeded"));
                return;
            }

            for (int i = 1; i < maxPacket; i++) {
                TPUtil.sendMovePacket(playerPos.x, playerPos.y, playerPos.z, false);
            }

            Vec3d aimVec = Utils.getVisiblePoint(
                this.tpPos.add(0.0, this.mc.player.getEyeHeight(this.mc.player.getPose()), 0.0),
                ((IEntity) this.mc.player)
                    .getDimensions()
                    .getBoxAt(
                        this.smart.get()
                            ? (targetVec.distanceTo(this.target.getPos()) <= Math.pow(this.calcFrom.get(), 2.0) ? targetVec : this.target.getPos())
                            : targetVec
                    )
            );
            TPUtil.sendMovePacket(vec3d, false);
            float[] angle = Utils.calculateAngle(this.tpPos.add(0.0, this.mc.player.getEyeHeight(this.mc.player.getPose()), 0.0), aimVec);
            Vec3d farVec = this.getFarVec3d(
                this.tpPos.add(0.0, this.mc.player.getEyeHeight(this.mc.player.getPose()), 0.0), aimVec, -this.powerDistance.get()
            );
            if (this.rotate.get()) {
                this.sendPacket(new LookAndOnGround(angle[0], angle[1], false));
            }

            if (this.faster.get()) {
                this.doSpoofs(this.tpPos, farVec);
            }
        }
    }

    private float calculateArc(Vec3d player, Vec3d target, double duration) {
        double yArc = target.getY() - player.getY();
        double dX = target.getX() - this.mc.player.getX();
        double dZ = target.getZ() - this.mc.player.getZ();
        double dirRoot = Math.sqrt(dX * dX + dZ * dZ);
        return this.calculateArc(duration, dirRoot, yArc);
    }

    private float calculateArc(double d, double dr, double y) {
        y = 2.0 * y * (d * d);
        y = 0.05F * (0.05F * (dr * dr) + y);
        y = Math.sqrt(d * d * d * d - y);
        d = d * d - y;
        y = Math.atan2(d * d + y, 0.05F * dr);
        d = Math.atan2(d, 0.05F * dr);
        return (float) Math.min(y, d);
    }

    private int calculateMaxPacket(Vec3d playerPos, Vec3d vec3d, Vec3d vec3d2, float moveDistance) {
        int a = (int) Math.ceil(playerPos.distanceTo(vec3d) / moveDistance);
        int b = (int) Math.ceil(playerPos.distanceTo(vec3d2) / moveDistance);
        return Math.max(a, b);
    }

    private int calculateMaxPacket(Vec3d playerPos, Vec3d vec3d2, float moveDistance) {
        return (int) Math.ceil(playerPos.distanceTo(vec3d2) / moveDistance);
    }

    public Vec3d getFarVec3d(Vec3d fromVec, Vec3d toVec, double distance) {
        double dis = distance;
        if (distance > 0.0) {
            while (dis > 0.0) {
                Vec3d stepPos = MathUtility.getStraightVec(fromVec, toVec, dis).add(0.0, -1.62, 0.0);
                if (stepPos.y > -64.0
                    && BlockUtil.checkNoPosCollie(stepPos, dev.abstr3act.addon.utils.abnormally.BlockUtil.getBufferMap())
                    && BlockUtil.checkCanMove(fromVec, stepPos, dev.abstr3act.addon.utils.abnormally.BlockUtil.getBufferMap(), PlayerUtil.PlayerState.Normal)
                    && BlockUtil.checkCanMove(stepPos, fromVec, dev.abstr3act.addon.utils.abnormally.BlockUtil.getBufferMap(), PlayerUtil.PlayerState.Normal)) {
                    return stepPos;
                }

                dis--;
            }
        } else {
            while (dis <= 0.0) {
                Vec3d stepPos = MathUtility.getStraightVec(fromVec, toVec, dis).add(0.0, -1.62, 0.0);
                if (stepPos.y > -64.0
                    && BlockUtil.checkNoPosCollie(stepPos, dev.abstr3act.addon.utils.abnormally.BlockUtil.getBufferMap())
                    && BlockUtil.checkCanMove(fromVec, stepPos, dev.abstr3act.addon.utils.abnormally.BlockUtil.getBufferMap(), PlayerUtil.PlayerState.Normal)
                    && BlockUtil.checkCanMove(stepPos, fromVec, dev.abstr3act.addon.utils.abnormally.BlockUtil.getBufferMap(), PlayerUtil.PlayerState.Normal)) {
                    return stepPos;
                }

                dis++;
            }
        }

        return fromVec;
    }
}
