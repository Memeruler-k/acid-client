package dev.abstr3act.addon.modules.Lacrymira;

import dev.abstr3act.addon.Compassion;
import dev.abstr3act.addon.module.LacrymiraModule;
import dev.abstr3act.addon.utils.TargetUtils;
import dev.abstr3act.addon.utils.abnormally.AChatUtils;
import dev.abstr3act.addon.utils.abnormally.MSTimer;
import dev.abstr3act.addon.utils.abnormally.TPUtil;
import dev.abstr3act.addon.utils.compassion.impl.BlockUtil;
import dev.abstr3act.addon.utils.compassion.impl.PlayerUtil;
import dev.abstr3act.addon.utils.luna.EntityMovementUtil;
import dev.abstr3act.addon.utils.math.MathUtility;
import meteordevelopment.meteorclient.events.packets.PacketEvent.Send;
import meteordevelopment.meteorclient.events.world.TickEvent.Pre;
import meteordevelopment.meteorclient.settings.EntityTypeListSetting.Builder;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.utils.entity.SortPriority;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.network.PendingUpdateManager;
import net.minecraft.client.network.SequencedPacketCreator;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket.Mode;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket.Action;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;

import java.util.Set;

public class TPBowGod extends LacrymiraModule {
    private final MSTimer attackTimer = new MSTimer();
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
    private final Setting<Double> moveDistance = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("move-distance"))
                .description("Max distance for a packet to move."))
                .defaultValue(20.0)
                .sliderMax(128.0)
                .sliderMin(1.0)
                .build()
        );
    private final Setting<Double> spoofMoveDistance = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("spoof-move-distance"))
                .description("Max distance for a packet to move."))
                .defaultValue(50.0)
                .sliderMax(128.0)
                .sliderMin(1.0)
                .build()
        );
    private final Setting<Double> powerDistance = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("power-distance"))
                .description("Max distance for a packet to move."))
                .defaultValue(50.0)
                .sliderMax(128.0)
                .sliderMin(1.0)
                .build()
        );
    private final Setting<Double> range = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("range"))
                .description("Max distance of a target."))
                .defaultValue(200.0)
                .sliderMax(128.0)
                .sliderMin(1.0)
                .build()
        );
    private final Setting<Double> y_prev = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("y prev"))
                .description("Y prev"))
                .defaultValue(2.0)
                .sliderMax(128.0)
                .sliderMin(1.0)
                .build()
        );
    private final Setting<Double> prev = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("prev"))
                .description("Prev"))
                .defaultValue(0.0)
                .sliderMax(5.0)
                .sliderMin(0.1)
                .build()
        );
    private Entity target = null;

    public TPBowGod() {
        super(Compassion.LACRYMIRA, "TPBowGod", "Use motion to speedup your arrow to destroy your enemy in a super large range");
    }

    public void onActivate() {
        this.target = null;
    }

    public void onDeactivate() {
        this.target = null;
    }

    @EventHandler
    public void onTick(Pre event) {
        this.updateTarget();
    }

    @EventHandler
    private void onPacketSend(Send event) {
        if (this.mc.player != null && this.mc.world != null && this.target != null) {
            if (event.packet instanceof PlayerActionC2SPacket
                && ((PlayerActionC2SPacket) event.packet).getAction() == Action.RELEASE_USE_ITEM
                && this.mc.player.getActiveItem().getItem() == Items.BOW) {
                this.doAura();
            }
        }
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
        } else if (t instanceof PlayerEntity p) {
            if (p.isCreative()) {
                return false;
            } else {
                return p == this.mc.player ? false : Friends.get().shouldAttack(p);
            }
        } else {
            return true;
        }
    }

    @Override
    protected void sendSequencedPacket(SequencedPacketCreator packetCreator) {
        if (this.mc.getNetworkHandler() != null && this.mc.world != null) {
            PendingUpdateManager pendingUpdateManager = this.mc.world.getPendingUpdateManager().incrementSequence();

            try {
                int i = pendingUpdateManager.getSequence();
                this.mc.getNetworkHandler().sendPacket(packetCreator.predict(i));
            } catch (Throwable var6) {
                if (pendingUpdateManager != null) {
                    try {
                        pendingUpdateManager.close();
                    } catch (Throwable var5) {
                        var6.addSuppressed(var5);
                    }
                }

                throw var6;
            }

            if (pendingUpdateManager != null) {
                pendingUpdateManager.close();
            }
        }
    }

    private void doAura() {
        if (this.target != null) {
            assert this.mc.player != null;

            if (!(this.mc.player.distanceTo(this.target) > this.range.get())) {
                Vec3d targetVec = EntityMovementUtil.getPrevPos(this.target, this.prev.get());
                this.attackTimer.reset();
                Vec3d playerPos = new Vec3d(this.mc.player.prevX, this.mc.player.prevY, this.mc.player.prevZ);
                Vec3d vec3d = TPUtil.findVClipVecToMove(playerPos, targetVec, 1.8, false);
                if (vec3d != null) {
                    Vec3d vec3d2 = TPUtil.findVClipVecToMove(targetVec, playerPos, 1.8, false);
                    if (vec3d2 != null) {
                        int a = (int) Math.ceil(playerPos.distanceTo(vec3d) / this.moveDistance.get());
                        int b = (int) Math.ceil(playerPos.distanceTo(vec3d2) / this.moveDistance.get());
                        int c = (int) Math.ceil(targetVec.distanceTo(targetVec) / this.moveDistance.get());
                        int maxPacket = Math.max(a, Math.max(b, c));
                        if (maxPacket > 20) {
                            AChatUtils.sendMsg(Text.of("Tp packet limit exceeded"));
                        } else {
                            for (int i = 1; i < maxPacket; i++) {
                                TPUtil.sendMovePacket(playerPos.x, playerPos.y, playerPos.z, false);
                            }

                            TPUtil.sendMovePacket(vec3d.x, vec3d.y + this.y_prev.get(), vec3d.z, false);
                            TPUtil.sendMovePacket(targetVec.x, targetVec.y + this.y_prev.get(), targetVec.z, false);
                            Vec3d farVec = this.getFarVec3d(
                                new Vec3d(targetVec.x, targetVec.y + this.y_prev.get(), targetVec.z), targetVec, -this.powerDistance.get()
                            );
                            this.doSpoofs(new Vec3d(targetVec.x, targetVec.y + this.y_prev.get(), targetVec.z), farVec);
                        }
                    }
                }
            }
        }
    }

    public void doSpoofs(Vec3d vec3d, Vec3d targetPos) {
        this.sendPacket(new ClientCommandC2SPacket(this.mc.player, Mode.START_SPRINTING));
        TPUtil.doTp(vec3d, targetPos, this.spoofMoveDistance.get(), false);
        this.sendPacket(new ClientCommandC2SPacket(this.mc.player, Mode.STOP_SPRINTING));
        TPUtil.sendMovePacket(vec3d, false);
    }

    private Vec3d getFarVec3d(Vec3d fromVec, Vec3d toVec, double distance) {
        double dis = distance;
        if (distance > 0.0) {
            while (dis > 0.0) {
                Vec3d stepPos = MathUtility.getStraightVec(fromVec, toVec, dis).add(0.0, -1.62, 0.0);
                if (stepPos.y > -64.0
                    && BlockUtil.checkNoPosCollie(stepPos, dev.abstr3act.addon.utils.abnormally.BlockUtil.getBufferMap())
                    && dev.abstr3act.addon.utils.abnormally.BlockUtil.checkCanMove(
                    fromVec, stepPos, dev.abstr3act.addon.utils.abnormally.BlockUtil.getBufferMap(), PlayerUtil.PlayerState.Normal
                )
                    && dev.abstr3act.addon.utils.abnormally.BlockUtil.checkCanMove(
                    stepPos, fromVec, dev.abstr3act.addon.utils.abnormally.BlockUtil.getBufferMap(), PlayerUtil.PlayerState.Normal
                )) {
                    return stepPos;
                }

                dis--;
            }
        } else {
            while (dis <= 0.0) {
                Vec3d stepPos = MathUtility.getStraightVec(fromVec, toVec, dis).add(0.0, -1.62, 0.0);
                if (stepPos.y > -64.0
                    && BlockUtil.checkNoPosCollie(stepPos, dev.abstr3act.addon.utils.abnormally.BlockUtil.getBufferMap())
                    && dev.abstr3act.addon.utils.abnormally.BlockUtil.checkCanMove(
                    fromVec, stepPos, dev.abstr3act.addon.utils.abnormally.BlockUtil.getBufferMap(), PlayerUtil.PlayerState.Normal
                )
                    && dev.abstr3act.addon.utils.abnormally.BlockUtil.checkCanMove(
                    stepPos, fromVec, dev.abstr3act.addon.utils.abnormally.BlockUtil.getBufferMap(), PlayerUtil.PlayerState.Normal
                )) {
                    return stepPos;
                }

                dis++;
            }
        }

        return fromVec;
    }
}
