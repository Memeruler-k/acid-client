package dev.abstr3act.addon.utils.compassion.impl;

import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.util.math.Box;

import java.util.ArrayList;

public final class EntityUtil {
    public static final EntityUtil INSTANCE = new EntityUtil();
    private static final ArrayList<Entity> worldEntitiesList = new ArrayList<>();

    private EntityUtil() {
    }

    public static ArrayList<Entity> getWorldEntitiesList() {
        synchronized (worldEntitiesList) {
            return new ArrayList<>(worldEntitiesList);
        }
    }

    public static boolean checkAnyEntitiesCollie$default(EntityUtil entityUtil, Box box, boolean ignoreCrystal, ArrayList<Entity> list, int n, Object object) {
        if ((n & 4) != 0) {
            list = getWorldEntitiesList();
        }

        return entityUtil.checkAnyEntitiesCollie(box, ignoreCrystal, list);
    }

    public static boolean checkAnyLivingEntityCollie$default(EntityUtil entityUtil, Box box, ArrayList<Entity> list, int n, Object object) {
        if ((n & 2) != 0) {
            list = getWorldEntitiesList();
        }

        return entityUtil.checkAnyLivingEntityCollie(box, list);
    }

    public boolean checkAnyEntitiesCollie(Box box, boolean ignoreCrystal, ArrayList<Entity> list) {
        if (box != null && list != null) {
            for (Entity entity : list) {
                if ((!(entity instanceof EndCrystalEntity) || !ignoreCrystal || !box.getCenter().equals(((EndCrystalEntity) entity).getBoundingBox().getCenter()))
                    && entity.getBoundingBox().intersects(box)) {
                    return true;
                }
            }

            return false;
        } else {
            throw new IllegalArgumentException("box or list cannot be null");
        }
    }

    public boolean checkAnyLivingEntityCollie(Box box, ArrayList<Entity> list) {
        if (box != null && list != null) {
            for (Entity entity : list) {
                if (entity.intersectionChecked && entity.getBoundingBox().intersects(box)) {
                    return true;
                }
            }

            return false;
        } else {
            throw new IllegalArgumentException("box or list cannot be null");
        }
    }

    public boolean checkAnyLivingEntityCollideInEntities(Box box, ArrayList<Entity> list) {
        if (box != null && list != null) {
            for (Entity entity : list) {
                if (entity.intersectionChecked && entity.getBoundingBox().intersects(box)) {
                    return true;
                }
            }

            return false;
        } else {
            throw new IllegalArgumentException("box or list cannot be null");
        }
    }
}
