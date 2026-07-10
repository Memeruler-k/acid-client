package dev.abstr3act.addon.modules.Seraphim;

import dev.abstr3act.addon.Compassion;
import dev.abstr3act.addon.module.SeraphimModule;
import dev.abstr3act.addon.notifications.Notification;
import dev.abstr3act.addon.notifications.NotificationsManager;
import dev.abstr3act.addon.utils.TargetUtils;
import meteordevelopment.meteorclient.events.world.TickEvent.Pre;
import meteordevelopment.meteorclient.settings.IntSetting.Builder;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.utils.entity.SortPriority;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.RaycastContext.FluidHandling;
import net.minecraft.world.RaycastContext.ShapeType;
import org.jetbrains.annotations.NotNull;

public class AutoSkill extends SeraphimModule {
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
    private final Setting<Integer> delay = this.sgGeneral
        .add(((Builder) ((Builder) ((Builder) new Builder().name("Delay")).description(".")).defaultValue(15)).sliderRange(1, 200).build());
    private final Setting<SortPriority> priority = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) new meteordevelopment.meteorclient.settings.EnumSetting.Builder()
                .name("target-priority"))
                .description("Entity sorting priority"))
                .defaultValue(SortPriority.LowestDistance))
                .build()
        );
    private final Setting<Boolean> onlyOnSight = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("OnlyOnSight"))
                .description("."))
                .defaultValue(true))
                .build()
        );
    private final Setting<Double> range = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("Range"))
                .description("."))
                .defaultValue(6.0)
                .sliderRange(1.0, 10.0)
                .build()
        );
    private final Setting<String> weaponName = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.StringSetting.Builder) ((meteordevelopment.meteorclient.settings.StringSetting.Builder) ((meteordevelopment.meteorclient.settings.StringSetting.Builder) new meteordevelopment.meteorclient.settings.StringSetting.Builder()
                .name("ItemName"))
                .description("Item name"))
                .defaultValue(""))
                .build()
        );
    private final Setting<Boolean> ignoreWalls = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("IgnoreWalls"))
                .description("."))
                .defaultValue(false))
                .build()
        );
    private int ticks = 0;
    private Entity target = null;

    public AutoSkill() {
        super(Compassion.SERAPHIM, "AutoSkill", ".");
    }

    public static int patchItem(PlayerEntity player, String itemName, boolean exactMatch) {
        PlayerInventory inventory = player.getInventory();

        for (int i = 0; i < 9; i++) {
            ItemStack stack = (ItemStack) inventory.main.get(i);
            if (!stack.isEmpty()) {
                String currentItemName = stack.getName().getString();
                if (exactMatch && currentItemName.equals(itemName) || !exactMatch && currentItemName.contains(itemName)) {
                    return i;
                }
            }
        }

        return -1;
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
            if ((g < distancePow2 || result == null) && entity2 instanceof LivingEntity) {
                return entity2;
            }
        }

        return targetedEntity;
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

    @EventHandler(
        priority = 200
    )
    private void onTick(Pre event) {
        if (!fullNullCheck()) {
            this.updateTarget();
            if (this.ticks < this.delay.get() * 20) {
                this.ticks++;
            } else {
                if (this.target == null) {
                    return;
                }

                assert this.mc.player != null;

                if (this.mc.player.distanceTo(this.target) > this.range.get()) {
                    return;
                }

                this.ticks = 0;
                NotificationsManager.add(new Notification("AutoSkill", "Successfully released skill"));
                InvUtils.swap(patchItem(this.mc.player, (String) this.weaponName.get(), false), true);
                this.sendSequencedPacket(id -> new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, id, this.mc.player.getYaw(), this.mc.player.getPitch()));
            }
        }
    }

    private void updateTarget() {
        Entity entity;
        if (this.onlyOnSight.get()) {
            entity = this.getRtxTarget(this.mc.player.getYaw(), this.mc.player.getPitch(), (this.range.get()).floatValue(), this.ignoreWalls.get());
        } else {
            entity = TargetUtils.get(this::_targetCheck, (SortPriority) this.priority.get());
        }

        if (entity != null) {
            this.target = entity;
        }
    }

    private boolean _targetCheck(Entity t) {
        if (t instanceof PlayerEntity p) {
            if (p.isCreative()) {
                return false;
            } else {
                return p == this.mc.player ? false : Friends.get().shouldAttack(p);
            }
        } else {
            return true;
        }
    }

    public String getInfoString() {
        return this.ticks == 0 ? "Standby" : String.valueOf(this.delay.get() - this.ticks / 20);
    }
}
