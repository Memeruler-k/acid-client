package dev.abstr3act.addon.modules.Seraphim;

import dev.abstr3act.addon.Compassion;
import dev.abstr3act.addon.events.EventPlayerUpdate;
import dev.abstr3act.addon.module.CompassionModule;
import dev.abstr3act.addon.modules.Abnormally.CaptureESP;
import meteordevelopment.meteorclient.settings.DoubleSetting.Builder;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.utils.entity.EntityUtils;
import meteordevelopment.meteorclient.utils.entity.fakeplayer.FakePlayerEntity;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.AxeItem;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolItem;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.RaycastContext.FluidHandling;
import net.minecraft.world.RaycastContext.ShapeType;
import org.jetbrains.annotations.NotNull;

public final class AutoFire extends CompassionModule {
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
    private final SettingGroup sgAttack = this.settings.createGroup("Attack");
    private final Setting<Double> attackRange = this.sgGeneral
        .add(((Builder) ((Builder) new Builder().name("Range")).description(".")).defaultValue(30.0).min(0.0).sliderRange(0.0, 30.0).build());
    private final Setting<Boolean> ignoreWalls = this.sgAttack
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("IgnoreWalls"))
                .description("."))
                .defaultValue(false))
                .build()
        );
    private final Setting<Boolean> ignoreTeam = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("IgnoreTeam"))
                .description("Ignore teammates while aiming."))
                .defaultValue(true))
                .build()
        );

    public AutoFire() {
        super(Compassion.SERAPHIM, "AutoFire", ".");
    }

    @EventHandler
    public void onAttack(EventPlayerUpdate e) {
        Entity ent = this.getRtxTarget(
            this.mc.player.getYaw(), this.mc.player.getPitch(), (this.attackRange.get()).floatValue(), this.ignoreWalls.get()
        );
        if (!(ent instanceof FakePlayerEntity)) {
            if (ent instanceof PlayerEntity) {
                if (Friends.get().isFriend((PlayerEntity) ent)) {
                    return;
                }

                if (this.mc.player.getMainHandStack().getItem() instanceof ToolItem
                    && !(this.mc.player.getMainHandStack().getItem() instanceof AxeItem)
                    && !(this.mc.player.getMainHandStack().getItem() instanceof SwordItem)
                    && !this.mc.player.getMainHandStack().isEmpty()) {
                    CaptureESP.target = ent;
                    this.sendPacket(new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, 0, this.mc.player.getYaw(), this.mc.player.getPitch()));
                }
            }
        }
    }

    public boolean shouldAttack(Entity entity) {
        if (entity == null) {
            return false;
        } else {
            if (entity instanceof PlayerEntity) {
                GameMode gm = EntityUtils.getGameMode((PlayerEntity) entity);
                if (gm == null) {
                    return false;
                }

                if (Friends.get().isFriend((PlayerEntity) entity)) {
                    return false;
                }
            }

            return entity.getTeamColorValue() != this.mc.player.getTeamColorValue()
                || !this.ignoreTeam.get()
                || this.mc.player.getTeamColorValue() == 16777215;
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
