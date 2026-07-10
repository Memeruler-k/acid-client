package dev.abstr3act.addon.modules.Compassion;

import dev.abstr3act.addon.Compassion;
import dev.abstr3act.addon.hud.notifications.NotificationsHudElement;
import dev.abstr3act.addon.module.CompassionModule;
import dev.abstr3act.addon.notifications.Notification;
import dev.abstr3act.addon.notifications.NotificationsManager;
import dev.abstr3act.addon.setting.DoubleListSetting;
import dev.abstr3act.addon.utils.InventoryUtility;
import dev.abstr3act.addon.utils.PredictUtility;
import dev.abstr3act.addon.utils.TargetUtils;
import dev.abstr3act.addon.utils.abnormally.BlockUtil;
import dev.abstr3act.addon.utils.abnormally.TPUtil;
import meteordevelopment.meteorclient.events.packets.PacketEvent.Receive;
import meteordevelopment.meteorclient.events.render.Render2DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent.Post;
import meteordevelopment.meteorclient.settings.EntityTypeListSetting.Builder;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.friends.Friends;
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
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class LacrymiraAura extends CompassionModule {
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
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
    private final Setting<Integer> maxTarget = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("max-target"))
                .description("Maximum number of targets."))
                .defaultValue(10))
                .sliderMax(25)
                .sliderMin(1)
                .build()
        );
    private final Setting<Double> range = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("range"))
                .description("Max distance of a target."))
                .defaultValue(50.0)
                .sliderMax(1.0)
                .sliderMin(200.0)
                .build()
        );
    private final Setting<Double> delay = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("delay"))
                .description("Max distance for a packet to move."))
                .defaultValue(1.0)
                .sliderMax(10.0)
                .sliderMin(1.0)
                .decimalPlaces(0)
                .build()
        );
    private final Setting<SwitchMode> switchMode = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) new meteordevelopment.meteorclient.settings.EnumSetting.Builder()
                .name("SwitchMode"))
                .description("."))
                .defaultValue(SwitchMode.Silent))
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
    private final Setting<Boolean> breach = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("breach"))
                .description("breach."))
                .defaultValue(true))
                .build()
        );
    private final Setting<Double> ignoreCount = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("ignoreCount"))
                .description("ignoreCount"))
                .defaultValue(0.0)
                .sliderMax(4.0)
                .sliderMin(0.0)
                .sliderRange(0.0, 4.0)
                .decimalPlaces(0)
                .visible(this.breach::get))
                .build()
        );
    private final Setting<List<Double>> breachVclip = this.sgGeneral
        .add(
            ((DoubleListSetting.Builder) ((DoubleListSetting.Builder) ((DoubleListSetting.Builder) ((DoubleListSetting.Builder) new DoubleListSetting.Builder()
                .name("breach-v-clip"))
                .description("Distance of clip."))
                .defaultValue(List.of(20.0, 60.0, 120.0)))
                .sliderRange(0.0, 200.0)
                .decimalPlaces(2)
                .onSliderRelease()
                .visible(this.breach::get))
                .build()
        );
    private final Setting<Double> breachMoveDistance = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("breach-move-distance"))
                .description("Max distance for a packet to move."))
                .defaultValue(128.0)
                .sliderMax(128.0)
                .sliderMin(1.0)
                .visible(this.breach::get))
                .build()
        );
    private final Setting<List<Double>> missVclip = this.sgGeneral
        .add(
            ((DoubleListSetting.Builder) ((DoubleListSetting.Builder) ((DoubleListSetting.Builder) new DoubleListSetting.Builder().name("miss-v-clip"))
                .description("Distance of clip."))
                .defaultValue(List.of(10.0, 15.0, 20.0, 25.0, 30.0)))
                .sliderRange(0.0, 200.0)
                .decimalPlaces(2)
                .onSliderRelease()
                .build()
        );
    private final Setting<Double> missMoveDistance = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("miss-move-distance"))
                .description("Max distance for a packet to move."))
                .defaultValue(10.0)
                .sliderMax(128.0)
                .sliderMin(1.0)
                .build()
        );
    private List<Entity> targets = new ArrayList<>();
    private int timer = 0;

    public LacrymiraAura() {
        super(Compassion.COMPASSION, "LacrymiraAura", "skidder?");
    }

    private boolean isPlayerWithArmor(Entity target) {
        return target instanceof PlayerEntity p && InventoryUtility.getRemainingArmorCount(p) > (this.ignoreCount.get()).intValue();
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

    private void swap(int slot, SwitchMode switchMode) {
        switch (switchMode) {
            case Normal:
                InvUtils.swap(slot, false);
                break;
            case Silent:
                InvUtils.swap(slot, true);
        }
    }

    private void swapBack(int slot, SwitchMode switchMode) {
        switch (switchMode) {
            case Silent:
                InvUtils.swapBack();
                break;
            case Inventory:
                InvUtils.move().from(this.mc.player.getInventory().selectedSlot).to(slot);
        }
    }

    @EventHandler
    public void onRender(Render2DEvent event) {
        if (this.timer <= 0) {
            List<Entity> entities = new ArrayList<>();
            TargetUtils.getList(entities, this::_targetCheck, (SortPriority) this.priority.get(), this.maxTarget.get());
            this.targets.clear();
            this.targets.addAll(entities);
            if (this.targets.isEmpty()) {
                return;
            }

            for (Entity entity : this.targets) {
                if (entity.getPos().squaredDistanceTo(this.mc.player.getPos()) >= this.range.get() * this.range.get()) {
                    return;
                }
            }

            FindItemResult mace = InvUtils.find(new Item[]{Items.MACE});
            if (!mace.found()) {
                return;
            }

            Entity target = this.targets.removeFirst();
            Vec3d playerVec = new Vec3d(this.mc.player.getX(), this.mc.player.getY(), this.mc.player.getZ());
            if (this.isPlayerWithArmor(target) && this.breach.get()) {
                double[] attackHeights = (this.breachVclip.get())
                    .stream()
                    .mapToDouble(Double::doubleValue)
                    .mapToObj(d -> (float) d)
                    .mapToDouble(f -> f.floatValue())
                    .toArray();

                for (double attackHeight : attackHeights) {
                    Vec3d targetVec = new Vec3d(target.getX(), target.getY() + attackHeight, target.getZ());
                    TPUtil.doTp(playerVec, targetVec, this.breachMoveDistance.get(), false);
                    TPUtil.doTp(targetVec, new Vec3d(target.getX(), target.getY(), target.getZ()), this.breachMoveDistance.get(), false);
                    if (InvUtils.find(new Item[]{Items.MACE}).found()) {
                        if (((SwitchMode) this.switchMode.get()).equals(SwitchMode.Inventory)) {
                            this.mc
                                .interactionManager
                                .clickSlot(
                                    this.mc.player.currentScreenHandler.syncId, mace.slot(), this.mc.player.getInventory().selectedSlot, SlotActionType.SWAP, this.mc.player
                                );
                            this.sendPacket(new CloseHandledScreenC2SPacket(this.mc.player.currentScreenHandler.syncId));
                        } else {
                            this.swap(mace.slot(), (SwitchMode) this.switchMode.get());
                        }
                    }

                    this.attack(target);
                    if (((SwitchMode) this.switchMode.get()).equals(SwitchMode.Inventory)) {
                        this.mc
                            .interactionManager
                            .clickSlot(
                                this.mc.player.currentScreenHandler.syncId, mace.slot(), this.mc.player.getInventory().selectedSlot, SlotActionType.SWAP, this.mc.player
                            );
                        this.sendPacket(new CloseHandledScreenC2SPacket(this.mc.player.currentScreenHandler.syncId));
                    } else {
                        this.swapBack(mace.slot(), (SwitchMode) this.switchMode.get());
                    }

                    TPUtil.doTp(new Vec3d(target.getX(), target.getY(), target.getZ()), playerVec, this.breachMoveDistance.get(), false);
                }
            }

            this.timer = (this.delay.get()).intValue();
        } else {
            this.timer--;
        }
    }

    @EventHandler
    public void onTickEvent(Post event) {
        List<Entity> entities = new ArrayList<>();
        TargetUtils.getList(entities, this::_targetCheck, (SortPriority) this.priority.get(), this.maxTarget.get());
        this.targets.clear();
        this.targets.addAll(entities);
        if (!this.targets.isEmpty()) {
            for (Entity entity : this.targets) {
                if (entity.getPos().squaredDistanceTo(this.mc.player.getPos()) >= this.range.get() * this.range.get()) {
                    return;
                }
            }

            FindItemResult mace = InvUtils.find(new Item[]{Items.MACE});
            if (mace.found()) {
                Entity target = this.targets.removeFirst();
                if (!this.isPlayerWithArmor(target)) {
                    double[] attackHeights = (this.missVclip.get())
                        .stream()
                        .mapToDouble(Double::doubleValue)
                        .mapToObj(d -> (float) d)
                        .mapToDouble(f -> f.floatValue())
                        .toArray();
                    this.doTpAura(target, attackHeights, this.missMoveDistance.get(), 0, this.swingHand.get());
                }
            }
        }
    }

    public void doTpAura(Entity target, double[] h, double moveDistance, int predictTicks, boolean swingHand) {
        Vec3d targetVec = new Vec3d(
            PredictUtility.predictPosition(target, predictTicks).x,
            PredictUtility.predictPosition(target, predictTicks).y,
            PredictUtility.predictPosition(target, predictTicks).z
        );
        Vec3d playerPos = new Vec3d(this.mc.player.getX(), this.mc.player.getY(), this.mc.player.getZ());
        Vec3d vec3d2 = TPUtil.findVClipVecToMove(targetVec, playerPos, 1.8, false);
        if (vec3d2 != null) {
            int b = (int) Math.ceil(playerPos.distanceTo(vec3d2) / moveDistance);
            int c = (int) Math.ceil(targetVec.distanceTo(targetVec) / moveDistance);
            int maxPacket = Math.max(b, c);
            if (maxPacket > 20) {
                this.mc.player.sendMessage(Text.of("TP packet limit exceeded"));
            } else {
                for (int i = 1; i < maxPacket; i++) {
                    TPUtil.sendMovePacket(playerPos.x, playerPos.y, playerPos.z, false);
                }

                for (double attackVclip : h) {
                    Vec3d newMiss = Vec3d.ofBottomCenter(BlockUtil.findVclipHole(this.mc.player.getBlockPos(), attackVclip));
                    FindItemResult mace = InvUtils.find(new Item[]{Items.MACE});
                    if (InvUtils.find(new Item[]{Items.MACE}).found()) {
                        if (((SwitchMode) this.switchMode.get()).equals(SwitchMode.Inventory)) {
                            this.mc
                                .interactionManager
                                .clickSlot(
                                    this.mc.player.currentScreenHandler.syncId, mace.slot(), this.mc.player.getInventory().selectedSlot, SlotActionType.SWAP, this.mc.player
                                );
                            this.sendPacket(new CloseHandledScreenC2SPacket(this.mc.player.currentScreenHandler.syncId));
                        } else {
                            this.swap(mace.slot(), (SwitchMode) this.switchMode.get());
                        }
                    }

                    TPUtil.doTp(playerPos, newMiss, moveDistance, false);
                    TPUtil.sendMovePacket(vec3d2.x, vec3d2.y, vec3d2.z, false);
                    if (swingHand) {
                        this.mc.player.swingHand(Hand.MAIN_HAND);
                    }

                    this.mc.interactionManager.attackEntity(this.mc.player, target);
                    if (((SwitchMode) this.switchMode.get()).equals(SwitchMode.Inventory)) {
                        this.mc
                            .interactionManager
                            .clickSlot(
                                this.mc.player.currentScreenHandler.syncId, mace.slot(), this.mc.player.getInventory().selectedSlot, SlotActionType.SWAP, this.mc.player
                            );
                        this.sendPacket(new CloseHandledScreenC2SPacket(this.mc.player.currentScreenHandler.syncId));
                    } else {
                        this.swapBack(mace.slot(), (SwitchMode) this.switchMode.get());
                    }
                }

                TPUtil.sendMovePacket(playerPos.x, playerPos.y, playerPos.z, false);
            }
        }
    }

    private void attack(Entity entity) {
        this.mc.player.networkHandler.sendPacket(PlayerInteractEntityC2SPacket.attack(entity, false));
        if (this.swingHand.get()) {
            this.mc.player.swingHand(Hand.MAIN_HAND, false);
        }
    }

    @EventHandler
    public void onPacketEvent(Receive event) {
        if (event.packet instanceof EntityStatusS2CPacket status && ((EntityStatusS2CPacket) event.packet).getEntity(this.mc.world) instanceof PlayerEntity) {
            Entity entity = ((EntityStatusS2CPacket) event.packet).getEntity(this.mc.world);
            if (entity == null) {
                return;
            }

            if (status.getStatus() == 50 && status.getEntity(this.mc.world) != null) {
                NotificationsManager.add(
                    new Notification(
                        "LacrymiraAura", "Successfully destroyed " + entity.getName().getString() + "'s armor", Color.WHITE, NotificationsHudElement.icon.ENABLE
                    )
                );
            }
        }
    }

    public String getInfoString() {
        return this.targets.isEmpty() ? "None" : this.targets.getFirst().getName().getString();
    }

    static enum SwitchMode {
        Normal,
        None,
        Silent,
        Inventory;
    }
}
