package dev.abstr3act.addon.modules.Lacrymira;

import dev.abstr3act.addon.Compassion;
import dev.abstr3act.addon.module.LacrymiraModule;
import dev.abstr3act.addon.notifications.Notification;
import dev.abstr3act.addon.notifications.NotificationsManager;
import dev.abstr3act.addon.utils.abnormally.TPUtil;
import meteordevelopment.meteorclient.events.world.TickEvent.Post;
import meteordevelopment.meteorclient.settings.DoubleSetting.Builder;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.utils.entity.fakeplayer.FakePlayerEntity;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.*;
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

public final class TPAttack extends LacrymiraModule {
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
    private final Setting<Double> attackRange = this.sgGeneral
        .add(((Builder) ((Builder) new Builder().name("Range")).description(".")).defaultValue(100.0).min(0.0).sliderRange(0.0, 257.0).build());
    private final Setting<Integer> attackDelay = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("Delay"))
                .description("."))
                .defaultValue(10))
                .min(0)
                .sliderRange(0, 1000)
                .build()
        );
    private final Setting<Boolean> breakShield = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("BreakShield"))
                .description("."))
                .defaultValue(true))
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
    private final Setting<Double> moveDistance = this.sgGeneral
        .add(
            ((Builder) ((Builder) new Builder().name("move-distance")).description("Max distance for a packet to move."))
                .defaultValue(20.0)
                .sliderMax(1.0)
                .sliderMin(128.0)
                .build()
        );
    int swapDelay = 0;
    boolean swapped = false;
    private int delay;

    public TPAttack() {
        super(Compassion.LACRYMIRA, "TPAttack", "Allow you to fuck your target in a super large lange");
    }

    @EventHandler
    public void onAttack(Post e) {
        if (this.mc.options.attackKey.isPressed()) {
            Entity ent = this.getRtxTarget(this.mc.player.getYaw(), this.mc.player.getPitch(), (this.attackRange.get()).floatValue(), true);
            if (!(ent instanceof FakePlayerEntity)) {
                if (!(ent instanceof PlayerEntity) || !Friends.get().isFriend((PlayerEntity) ent)) {
                    if (this.shouldAttack(ent)) {
                        if (ent instanceof PlayerEntity) {
                            if (!this.isWeapon(this.mc.player.getMainHandStack().getItem()) && this.onlyWeapon.get()) {
                                return;
                            }

                            if (((PlayerEntity) ent).isBlocking() && this.breakShield.get()) {
                                InvUtils.swap(this.getAxeItem(), false);
                                NotificationsManager.add(new Notification("TPAttack", "Try to break " + ent.getName().getString() + "'s shield"));
                                this.swapDelay = 0;
                                this.swapped = true;
                            }
                        }

                        TPUtil.doTp(this.mc.player.getPos(), ent.getPos(), this.moveDistance.get(), false);
                        this.mc.interactionManager.attackEntity(this.mc.player, ent);
                        this.mc.player.swingHand(Hand.MAIN_HAND);
                        TPUtil.doTp(ent.getPos(), this.mc.player.getPos(), this.moveDistance.get(), false);
                        this.mc.options.attackKey.setPressed(false);
                    }
                }
            }
        }
    }

    public boolean shouldAttack(Entity entity) {
        boolean blocking = false;
        if (entity == null) {
            return false;
        } else if (entity instanceof FakePlayerEntity) {
            return false;
        } else {
            if (entity instanceof PlayerEntity) {
                if (((PlayerEntity) entity).isBlocking() && this.breakShield.get()) {
                    return true;
                }

                if (Friends.get().isFriend((PlayerEntity) entity)) {
                    return false;
                }
            }

            return this.isWeapon(this.mc.player.getMainHandStack().getItem()) || !this.onlyWeapon.get();
        }
    }

    public boolean isWeapon(Item item) {
        return item instanceof AxeItem || item instanceof SwordItem || item instanceof MaceItem;
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
}
