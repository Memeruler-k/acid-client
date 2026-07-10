package dev.abstr3act.addon.modules.Abnormally;

import dev.abstr3act.addon.Compassion;
import dev.abstr3act.addon.module.AbnormallyModule;
import dev.abstr3act.addon.utils.TargetUtils;
import dev.abstr3act.addon.utils.abnormally.AChatUtils;
import dev.abstr3act.addon.utils.abnormally.MSTimer;
import dev.abstr3act.addon.utils.abnormally.TPUtil;
import dev.abstr3act.addon.utils.luna.EntityMovementUtil;
import meteordevelopment.meteorclient.events.packets.PacketEvent.Receive;
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
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;

import java.util.Set;

public class TPAura extends AbnormallyModule {
    public SettingGroup sgGeneral = this.settings.getDefaultGroup();
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
    private final Setting<Double> vClip = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("v-clip"))
                .description("Distance of clip."))
                .defaultValue(20.0)
                .sliderMax(1.0)
                .sliderMin(128.0)
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
    private final Setting<Double> range = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("range"))
                .description("Max distance of a target."))
                .defaultValue(20.0)
                .sliderMax(1.0)
                .sliderMin(128.0)
                .build()
        );
    private final Setting<Double> prev = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("prev"))
                .description("Prev"))
                .defaultValue(0.0)
                .sliderMax(0.1)
                .sliderMin(5.0)
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
    private final Setting<Integer> attackTimes = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("attack-times"))
                .description("attack times."))
                .defaultValue(12))
                .sliderMax(200)
                .sliderMin(1)
                .build()
        );
    private Entity target = null;
    private MSTimer attackTimer = new MSTimer();
    private Setting<Boolean> useMace = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("use-mace"))
                .description("Whether use mace to attack."))
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

    public TPAura() {
        super(Compassion.ABNORMALLY, "TPAura", "Kill Aura.");
    }

    public void onActivate() {
        this.target = null;
    }

    public void onDeactivate() {
        this.target = null;
    }

    private boolean isReadyToAttack() {
        return this.useCooldown.get()
            ? this.mc.player.getAttackCooldownProgress(-1.0F) >= this.useCooldownBaseTime.get()
            : this.attackTimer.hasPassTime(this.attackDelay.get());
    }

    @EventHandler
    public void onTick(Pre event) {
        this.updateTarget();
        this.doAura();
    }

    private final boolean getCriticals() {
        return ((Criticals) Modules.get().get(Criticals.class)).isActive();
    }

    private void updateTarget() {
        Entity entity = TargetUtils.get(this::_targetCheck, (SortPriority) this.priority.get());
        if (entity != null) {
            this.target = entity;
        }
    }

    private boolean _targetCheck(Entity t) {
        if (!((Set) this.targetTypes.get()).contains(t.getType())) {
            return false;
        } else if (!(t instanceof LivingEntity) || !((LivingEntity) t).isDead() && !(((LivingEntity) t).getHealth() <= 0.0F) && ((LivingEntity) t).deathTime <= 0) {
            if (t instanceof PlayerEntity p) {
                if (((PlayerEntity) t).isDead() || ((PlayerEntity) t).getHealth() <= 0.0F || ((PlayerEntity) t).deathTime > 0) {
                    return false;
                } else if (p.isCreative()) {
                    return false;
                } else {
                    return p == this.mc.player ? false : Friends.get().shouldAttack(p);
                }
            } else {
                return true;
            }
        } else {
            return false;
        }
    }

    @EventHandler
    private void onReceivePacket(Receive event) {
        if (event.packet instanceof EntityStatusS2CPacket p) {
            if (p.getStatus() == 35) {
                Entity entity = p.getEntity(this.mc.world);
                if (entity instanceof PlayerEntity) {
                    if (!entity.equals(this.mc.player) && !Friends.get().isFriend((PlayerEntity) entity) && Friends.get().isFriend((PlayerEntity) entity)) {
                        if (entity == this.target) {
                            Vec3d pos = this.mc.player.getPos();
                            TPUtil.doTp(pos.x, pos.y, pos.z, pos.x, pos.y + this.vClip.get(), pos.z, this.moveDistance.get(), false);

                            for (int i = 0; i < this.attackTimes.get(); i++) {
                                if (this.useMace.get()) {
                                    InvUtils.swap(InvUtils.find(new Item[]{Items.MACE}).slot(), true);
                                }

                                this.mc.interactionManager.attackEntity(this.mc.player, entity);
                                if (this.useMace.get()) {
                                    InvUtils.swapBack();
                                }
                            }

                            TPUtil.doTp(pos.x, pos.y, pos.z, pos.x, pos.y, pos.z, this.moveDistance.get(), false);
                        }
                    }
                }
            }
        }
    }

    private void doAura() {
        if (this.isReadyToAttack() && this.target != null) {
            if (!(this.mc.player.distanceTo(this.target) > this.range.get())) {
                Vec3d targetVec = EntityMovementUtil.getPrevPos(this.target, this.prev.get());
                this.attackTimer.reset();
                Vec3d playerPos = new Vec3d(this.mc.player.prevX, this.mc.player.prevY, this.mc.player.prevZ);
                Vec3d vec3d = TPUtil.findVClipVecToMove(playerPos, targetVec, 1.8, false);
                Vec3d miss = TPUtil.findVClipVecToMove(playerPos, targetVec.add(0.0, -1.0, 0.0), 1.8, false);
                if (vec3d != null) {
                    Vec3d vec3d2 = TPUtil.findVClipVecToMove(targetVec, playerPos, 1.8, false);
                    if (vec3d2 != null) {
                        int a = (int) Math.ceil(playerPos.distanceTo(vec3d) / this.moveDistance.get());
                        int b = (int) Math.ceil(playerPos.distanceTo(vec3d2) / this.moveDistance.get());
                        int c = (int) Math.ceil(targetVec.distanceTo(targetVec) / this.moveDistance.get());
                        int maxPacket = Math.max(a, Math.max(b, c));
                        if (maxPacket > 20) {
                            AChatUtils.sendMsg(Text.of("TP packet limit exceeded"));
                        } else {
                            for (int i = 1; i < maxPacket; i++) {
                                TPUtil.sendMovePacket(playerPos.x, playerPos.y, playerPos.z, false);
                            }

                            TPUtil.sendMovePacket(vec3d.x, vec3d.y, vec3d.z, false);
                            TPUtil.sendMovePacket(targetVec.x, targetVec.y, targetVec.z, false);
                            if (this.useMace.get()) {
                                FindItemResult mace = InvUtils.find(new Item[]{Items.MACE});
                                int maceCount = mace.count();
                                if (maceCount > 0) {
                                    int n2 = this.attackTimes.get();

                                    for (int i = 1; i <= n2; i++) {
                                        this.mc.player.networkHandler.sendPacket(PlayerInteractEntityC2SPacket.attack(this.target, false));
                                    }

                                    if (this.swingHand.get()) {
                                        this.mc.player.swingHand(Hand.MAIN_HAND);
                                    }

                                    TPUtil.sendMovePacket(vec3d2.x, vec3d2.y, vec3d2.z, false);
                                    TPUtil.sendMovePacket(playerPos.x, playerPos.y, playerPos.z, false);
                                }
                            } else {
                                int n2 = this.attackTimes.get();

                                for (int i = 1; i <= n2; i++) {
                                    this.mc.player.networkHandler.sendPacket(PlayerInteractEntityC2SPacket.attack(this.target, false));
                                }

                                if (this.swingHand.get()) {
                                    this.mc.player.swingHand(Hand.MAIN_HAND);
                                }

                                TPUtil.sendMovePacket(vec3d2.x, vec3d2.y, vec3d2.z, false);
                                TPUtil.sendMovePacket(playerPos.x, playerPos.y, playerPos.z, false);
                            }
                        }
                    }
                }
            }
        }
    }
}
