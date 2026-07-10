package dev.abstr3act.addon.modules.Amrita.killaura.sb;

import meteordevelopment.meteorclient.MeteorClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.RaycastContext.FluidHandling;
import net.minecraft.world.RaycastContext.ShapeType;

import java.util.function.Predicate;

public class RaycastUtils {
    public static EntityHitResult raycastEntity(double range, float yaw, float pitch, double boxexpand) {
        Entity camera = MeteorClient.mc.getCameraEntity();
        Vec3d cameraVec = camera.getCameraPosVec(1.0F);
        float yawCos = MathHelper.cos(-yaw * (float) (Math.PI / 180.0) - (float) Math.PI);
        float yawSin = MathHelper.sin(-yaw * (float) (Math.PI / 180.0) - (float) Math.PI);
        float pitchCos = -MathHelper.cos(-pitch * (float) (Math.PI / 180.0));
        float pitchSin = MathHelper.sin(-pitch * (float) (Math.PI / 180.0));
        Vec3d rotation = new Vec3d(yawSin * pitchCos, pitchSin, yawCos * pitchCos);
        Vec3d vec3d3 = cameraVec.add(rotation.x * range, rotation.y * range, rotation.z * range);
        Box box = camera.getBoundingBox().stretch(rotation.multiply(range)).expand(boxexpand, boxexpand, boxexpand);
        return ProjectileUtil.raycast(camera, cameraVec, vec3d3, box, new Predicate<Entity>() {
            public boolean test(Entity entity) {
                return !entity.isSpectator() && entity.isCollidable();
            }
        }, 0.0);
    }

    public static Vec3d getRotationVector(float pitch, float yaw) {
        float f = pitch * (float) (Math.PI / 180.0);
        float g = -yaw * (float) (Math.PI / 180.0);
        float h = MathHelper.cos(g);
        float i = MathHelper.sin(g);
        float j = MathHelper.cos(f);
        float k = MathHelper.sin(f);
        return new Vec3d(i * j, -k, h * j);
    }

    public static HitResult raycast(Vec3d camera, Vec3d rotation, double maxDistance, float tickDelta, boolean includeFluids) {
        Vec3d vec3d3 = camera.add(rotation.x * maxDistance, rotation.y * maxDistance, rotation.z * maxDistance);
        return MeteorClient.mc
            .world
            .raycast(new RaycastContext(camera, vec3d3, ShapeType.OUTLINE, includeFluids ? FluidHandling.ANY : FluidHandling.NONE, MeteorClient.mc.player));
    }

    public static BlockHitResult bucketRaycast(Vec3d camera, float pitch, float yaw, FluidHandling fluidHandling) {
        float h = MathHelper.cos(-yaw * (float) (Math.PI / 180.0) - (float) Math.PI);
        float i = MathHelper.sin(-yaw * (float) (Math.PI / 180.0) - (float) Math.PI);
        float j = -MathHelper.cos(-pitch * (float) (Math.PI / 180.0));
        float k = MathHelper.sin(-pitch * (float) (Math.PI / 180.0));
        float l = i * j;
        float n = h * j;
        double d = 5.0;
        Vec3d vec3d2 = camera.add(l * 5.0, k * 5.0, n * 5.0);
        return MeteorClient.mc.world.raycast(new RaycastContext(camera, vec3d2, ShapeType.OUTLINE, fluidHandling, MeteorClient.mc.player));
    }
}
