package dev.abstr3act.addon.modules.Abnormally;

import dev.abstr3act.addon.Compassion;
import dev.abstr3act.addon.module.AbnormallyModule;
import dev.abstr3act.addon.modules.Fragment.AntiBot;
import dev.abstr3act.addon.setting.DoubleListSetting;
import dev.abstr3act.addon.utils.TargetUtil;
import dev.abstr3act.addon.utils.TargetUtils;
import dev.abstr3act.addon.utils.abnormally.BlockUtil;
import dev.abstr3act.addon.utils.abnormally.MSTimer;
import dev.abstr3act.addon.utils.abnormally.TPUtil;
import meteordevelopment.meteorclient.events.world.TickEvent.Pre;
import meteordevelopment.meteorclient.settings.EntityTypeListSetting.Builder;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.combat.Criticals;
import meteordevelopment.meteorclient.utils.entity.SortPriority;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class MaceAura extends AbnormallyModule {
    private SettingGroup sgGeneral = this.settings.getDefaultGroup();
    private final Setting<Set<EntityType<?>>> targetTypes = this.sgGeneral
        .add(
            ((Builder) ((Builder) new Builder().name("target-type")).description("Entities to attack."))
                .onlyAttackable()
                .defaultValue(new EntityType[]{EntityType.PLAYER})
                .build()
        );
    private final Setting<SortPriority> priority = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) new meteordevelopment.meteorclient.settings.EnumSetting.Builder()
                .name("target-priority"))
                .description("Entity sorting priority"))
                .defaultValue(SortPriority.LowestDistance))
                .build()
        );
    private final Setting<Double> range = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("Range"))
                .description("range."))
                .defaultValue(3.0)
                .sliderMax(1.0)
                .sliderMin(10.0)
                .build()
        );
    private final Setting<List<Double>> vClip = this.sgGeneral
        .add(
            ((DoubleListSetting.Builder) ((DoubleListSetting.Builder) new DoubleListSetting.Builder().name("v-clip")).description("Distance of clip."))
                .defaultValue(10.0)
                .range(0.0, 1000.0)
                .sliderRange(0.0, 1000.0)
                .decimalPlaces(2)
                .onSliderRelease()
                .build()
        );
    private final Setting<Double> moveDistance = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("move-distance"))
                .description("Max distance for a packet to move."))
                .defaultValue(20.0)
                .sliderMax(1.0)
                .sliderMin(128.0)
                .build()
        );
    private final Setting<Boolean> swingHand = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("swing-hand"))
                .description("Whether swing hand when attacking."))
                .defaultValue(false))
                .build()
        );
    private final Setting<Boolean> livingTicks = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("LivingTicks"))
                .description("."))
                .defaultValue(false))
                .build()
        );
    private final Setting<Integer> ticks = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("Ticks"))
                .description("."))
                .defaultValue(50))
                .sliderMax(2000)
                .sliderMin(1)
                .visible(() -> !this.livingTicks.get()))
                .build()
        );
    private List<Entity> targets = new ArrayList<>();
    private MSTimer delayTimer = new MSTimer();
    private Setting<Boolean> useMace = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("use-mace"))
                .description("Whether use mace to attack."))
                .defaultValue(false))
                .build()
        );
    private Setting<Boolean> ignoreFriend = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("IgnoreFriend"))
                .description("."))
                .defaultValue(false))
                .build()
        );
    private Setting<Boolean> useCooldown = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("use-cooldown"))
                .description("Whether use the item cooldown to determine when attack."))
                .defaultValue(true))
                .build()
        );
    private final Setting<Double> useCooldownBaseTime = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("use-cooldown-base-time"))
                .description("The base time for using cooldown."))
                .defaultValue(0.75)
                .sliderMax(1.0)
                .sliderMin(0.1)
                .visible(() -> this.useCooldown.get()))
                .build()
        );
    private final Setting<Integer> attackDelay = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("attack-delay"))
                .description("attack delay."))
                .defaultValue(50))
                .sliderMax(2000)
                .sliderMin(1)
                .visible(() -> !this.useCooldown.get()))
                .build()
        );

    public MaceAura() {
        super(Compassion.ABNORMALLY, "MaceAura", "Kill Aura.");
    }

    public void onActivate() {
        this.targets.clear();
    }

    public void onDeactivate() {
        this.targets.clear();
    }

    private boolean isReadyToAttack() {
        return this.useCooldown.get()
            ? this.mc.player.getAttackCooldownProgress(-1.0F) >= this.useCooldownBaseTime.get()
            : this.delayTimer.hasPassTime(this.attackDelay.get());
    }

    @EventHandler
    public void onTick(Pre event) {
        this.updateTarget();
        this.doAura();
    }

    private boolean getCriticals() {
        return ((Criticals) Modules.get().get(Criticals.class)).isActive();
    }

    private void updateTarget() {
        List<Entity> entities = new ArrayList<>();
        TargetUtils.getList(entities, this::_targetCheck, (SortPriority) this.priority.get(), 1);
        this.targets.clear();
        this.targets.addAll(entities);
    }

    private boolean _targetCheck(Entity t) {
        if (!((Set) this.targetTypes.get()).contains(t.getType())) {
            return false;
        } else if (!(t instanceof LivingEntity) || !((LivingEntity) t).isDead() && !(((LivingEntity) t).getHealth() <= 0.0F) && ((LivingEntity) t).deathTime <= 0) {
            if (t instanceof PlayerEntity p) {
                if (((PlayerEntity) t).isDead() || ((PlayerEntity) t).getHealth() <= 0.0F || ((PlayerEntity) t).deathTime > 0) {
                    return false;
                } else if (t.age < this.ticks.get() && this.livingTicks.get()) {
                    return false;
                } else if (p.isCreative()) {
                    return false;
                } else if (p == this.mc.player) {
                    return false;
                } else if (AntiBot.INSTANCE.inBotList(p)) {
                    return false;
                } else if (TargetUtil.isBot(p)) {
                    return false;
                } else {
                    return this.ignoreFriend.get() ? true : Friends.get().shouldAttack(p);
                }
            } else {
                return true;
            }
        } else {
            return false;
        }
    }

    private void doAura() {
        if (!this.targets.isEmpty()) {
            if (this.isReadyToAttack()) {
                if (!(this.mc.player.distanceTo(this.targets.getFirst()) > this.range.get())) {
                    for (double vClips : this.vClip.get()) {
                        this.extraPacket(this.targets, vClips);
                    }
                }
            }
        }
    }

    private void extraPacket(List<Entity> entityList, double vclip) {
        if (!this.useMace.get() && this.getCriticals()) {
            TPUtil.sendMovePacket(this.mc.player.getX(), this.mc.player.getY() + 0.001, this.mc.player.getZ(), true);
            TPUtil.sendMovePacket(this.mc.player.getX(), this.mc.player.getY(), this.mc.player.getZ(), false);
        }

        if (this.useMace.get() && InvUtils.findInHotbar(new Item[]{Items.MACE}).found()) {
            FindItemResult mace = InvUtils.findInHotbar(new Item[]{Items.MACE});
            if (!mace.found()) {
                return;
            }

            InvUtils.swap(mace.slot(), true);
            BlockPos vec3d = this.mc.player.getBlockPos();
            BlockPos vecUp = BlockUtil.findVclipHole(vec3d, vclip);
            Vec3d vec3d2 = this.mc.player.getPos();
            TPUtil.doTp(vec3d2, Vec3d.of(vecUp), this.moveDistance.get(), false, 8, null);
            TPUtil.sendMovePacket(this.mc.player.getX(), this.mc.player.getY(), this.mc.player.getZ(), false);
            int n = Math.min(entityList.size(), 1);

            for (int i = 0; i < n; i++) {
                for (Entity element : entityList) {
                    this.mc.player.networkHandler.sendPacket(PlayerInteractEntityC2SPacket.attack(element, false));
                }
            }

            TPUtil.sendMovePacket(this.mc.player.getX(), this.mc.player.getY() + 1.0E-4, this.mc.player.getZ(), false);
            int n2 = Math.min(1, this.targets.size());

            for (int i = 0; i < n2; i++) {
                this.attack(this.targets.get(i));
                if (this.swingHand.get()) {
                    this.mc.player.swingHand(Hand.MAIN_HAND);
                }
            }

            InvUtils.swapBack();
        }
    }

    private void attack(Entity entity) {
        this.mc.player.networkHandler.sendPacket(PlayerInteractEntityC2SPacket.attack(entity, false));
    }
}
