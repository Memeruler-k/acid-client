package dev.abstr3act.addon.utils.seraphim;

import meteordevelopment.meteorclient.utils.entity.ProjectileEntitySimulator;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class ProjectileUtils {
    public static Result getFinalHitResult(ProjectileEntitySimulator simulator, int maxSteps) {
        Vec3d finalPosition = new Vec3d(simulator.pos.x, simulator.pos.y, simulator.pos.z);
        boolean hitEntity = false;
        LivingEntity hitTarget = null;

        for (int step = 0; step < maxSteps; step++) {
            HitResult result = simulator.tick();
            if (result != null) {
                if (result.getType() == Type.ENTITY && result instanceof EntityHitResult) {
                    hitEntity = true;
                    hitTarget = (LivingEntity) ((EntityHitResult) result).getEntity();
                }
                break;
            }

            finalPosition = new Vec3d(simulator.pos.x, simulator.pos.y, simulator.pos.z);
        }

        return new Result(finalPosition, hitEntity, hitTarget);
    }

    public static HitResult checkArrow(PlayerEntity player, Vec3d arrowPos, Vec3d arrowVelocity, World world, int maxSteps, double stepSize) {
        Vec3d currentPosition = arrowPos;
        Vec3d currentVelocity = arrowVelocity;

        for (int step = 0; step < maxSteps; step++) {
            Vec3d nextPosition = currentPosition.add(currentVelocity.multiply(stepSize));
            Box arrowBox = new Box(currentPosition, nextPosition).expand(0.25);

            for (LivingEntity entity : world.getEntitiesByClass(LivingEntity.class, arrowBox, entityx -> entityx != player)) {
                if (entity.getBoundingBox().intersects(arrowBox)) {
                    return new EntityHitResult(entity);
                }
            }

            currentPosition = nextPosition;
            currentVelocity = currentVelocity.add(0.0, -0.05, 0.0);
        }

        return null;
    }

    public static class Result {
        private final Vec3d position;
        private final boolean hitEntity;
        private final LivingEntity hitTarget;

        public Result(Vec3d position, boolean hitEntity, LivingEntity hitTarget) {
            this.position = position;
            this.hitEntity = hitEntity;
            this.hitTarget = hitTarget;
        }

        public Vec3d getPosition() {
            return this.position;
        }

        public boolean hasHitEntity() {
            return this.hitEntity;
        }

        public LivingEntity getHitTarget() {
            return this.hitTarget;
        }
    }
}
