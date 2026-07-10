package dev.abstr3act.addon.utils.luna;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;

public class EntityMovementUtil {
    public static Vec3d getPrevPos(Entity entity, double prev) {
        return new Vec3d(
            entity.getX() + (entity.getX() - entity.prevX) * prev,
            entity.getY() + (entity.getY() - entity.prevY) * prev,
            entity.getZ() + (entity.getZ() - entity.prevZ) * prev
        );
    }
}
