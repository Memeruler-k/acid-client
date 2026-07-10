package dev.abstr3act.addon.utils.luna;

import com.mojang.authlib.GameProfile;
import meteordevelopment.meteorclient.MeteorClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.UUID;

public class PredictUtils {
    public static PlayerEntity predictPlayer(PlayerEntity entity, int ticks) {
        Vec3d posVec = predictPosition(entity, ticks);
        return posVec == null ? null : equipAndReturn(entity, posVec);
    }

    public static Vec3d predictPosition(PlayerEntity entity, int ticks) {
        if (entity == null) {
            return null;
        } else {
            Vec3d posVec = new Vec3d(entity.getX(), entity.getY(), entity.getZ());
            double motionX = entity.getX() - entity.prevX;
            double motionY = entity.getY() - entity.prevY;
            double motionZ = entity.getZ() - entity.prevZ;
            if (entity == MeteorClient.mc.player) {
                motionY = 0.0;
            }

            for (int i = 0; i < ticks; i++) {
                if (!MeteorClient.mc.world.isAir(BlockPos.ofFloored(posVec.add(0.0, motionY, 0.0)))) {
                    motionY = 0.0;
                }

                if (!MeteorClient.mc.world.isAir(BlockPos.ofFloored(posVec.add(motionX, 0.0, 0.0)))
                    || !MeteorClient.mc.world.isAir(BlockPos.ofFloored(posVec.add(motionX, 1.0, 0.0)))) {
                    motionX = 0.0;
                }

                if (!MeteorClient.mc.world.isAir(BlockPos.ofFloored(posVec.add(0.0, 0.0, motionZ)))
                    || !MeteorClient.mc.world.isAir(BlockPos.ofFloored(posVec.add(0.0, 1.0, motionZ)))) {
                    motionZ = 0.0;
                }

                posVec = posVec.add(motionX, motionY, motionZ);
            }

            return posVec;
        }
    }

    public static Box predictBox(PlayerEntity entity, int ticks) {
        Vec3d posVec = predictPosition(entity, ticks);
        return posVec == null ? null : createBox(posVec, entity);
    }

    public static Box createBox(Vec3d vec, Entity entity) {
        return entity.getBoundingBox().offset(entity.getPos().relativize(vec));
    }

    public static PlayerEntity equipAndReturn(PlayerEntity original, Vec3d posVec) {
        PlayerEntity copyEntity = new PlayerEntity(
            MeteorClient.mc.world,
            original.getBlockPos(),
            original.getYaw(),
            new GameProfile(UUID.fromString("66123666-1234-5432-6666-667563866600"), "PredictEntity339")
        ) {
            public boolean isSpectator() {
                return false;
            }

            public boolean isCreative() {
                return false;
            }
        };
        copyEntity.setPosition(posVec);
        copyEntity.setHealth(original.getHealth());
        copyEntity.prevX = original.prevX;
        copyEntity.prevZ = original.prevZ;
        copyEntity.prevY = original.prevY;
        copyEntity.getInventory().clone(original.getInventory());

        for (StatusEffectInstance se : original.getStatusEffects()) {
            copyEntity.addStatusEffect(se);
        }

        return copyEntity;
    }
}
