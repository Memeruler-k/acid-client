package dev.abstr3act.addon.utils;

import dev.abstr3act.addon.mixin.accessor.IExplosion;
import meteordevelopment.meteorclient.MeteorClient;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.DamageUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.util.math.*;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.Difficulty;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.RaycastContext.FluidHandling;
import net.minecraft.world.RaycastContext.ShapeType;
import net.minecraft.world.explosion.Explosion;
import net.minecraft.world.explosion.Explosion.DestructionType;

import java.util.Objects;

public final class ExplosionUtility {
    public static boolean terrainIgnore = false;
    public static Explosion explosion;

    public static float getAutoCrystalDamage(Vec3d crystalPos, PlayerEntity target, int predictTicks, boolean optimized) {
        return predictTicks == 0
            ? getExplosionDamage(crystalPos, target, optimized)
            : getExplosionDamageWPredict(crystalPos, target, PredictUtility.predictBox(target, predictTicks), optimized);
    }

    public static float getExplosionDamage(Vec3d explosionPos, PlayerEntity target, boolean optimized) {
        if (MeteorClient.mc.world.getDifficulty() != Difficulty.PEACEFUL && target != null) {
            if (explosion == null) {
                explosion = new Explosion(MeteorClient.mc.world, MeteorClient.mc.player, 1.0, 33.0, 7.0, 6.0F, false, DestructionType.DESTROY);
            }

            ((IExplosion) explosion).setX(explosionPos.x);
            ((IExplosion) explosion).setY(explosionPos.y);
            ((IExplosion) explosion).setZ(explosionPos.z);
            if (((IExplosion) explosion).getWorld() != MeteorClient.mc.world) {
                ((IExplosion) explosion).setWorld(MeteorClient.mc.world);
            }

            if (!new Box(
                MathHelper.floor(explosionPos.x - 11.0),
                MathHelper.floor(explosionPos.y - 11.0),
                MathHelper.floor(explosionPos.z - 11.0),
                MathHelper.floor(explosionPos.x + 13.0),
                MathHelper.floor(explosionPos.y + 13.0),
                MathHelper.floor(explosionPos.z + 13.0)
            )
                .intersects(target.getBoundingBox())) {
                return 0.0F;
            } else {
                if (!target.isImmuneToExplosion(explosion) && !target.isInvulnerable()) {
                    double distExposure = (float) target.squaredDistanceTo(explosionPos) / 144.0;
                    if (distExposure <= 1.0) {
                        double exposure = getExposure(explosionPos, target.getBoundingBox(), optimized);
                        terrainIgnore = false;
                        double finalExposure = (1.0 - distExposure) * exposure;
                        float toDamage = (float) Math.floor((finalExposure * finalExposure + finalExposure) / 2.0 * 7.0 * 12.0 + 1.0);
                        if (MeteorClient.mc.world.getDifficulty() == Difficulty.EASY) {
                            toDamage = Math.min(toDamage / 2.0F + 1.0F, toDamage);
                        } else if (MeteorClient.mc.world.getDifficulty() == Difficulty.HARD) {
                            toDamage = toDamage * 3.0F / 2.0F;
                        }

                        float var12 = DamageUtil.getDamageLeft(
                            target,
                            toDamage,
                            ((IExplosion) explosion).getDamageSource(),
                            target.getArmor(),
                            (float) target.getAttributeInstance(EntityAttributes.GENERIC_ARMOR_TOUGHNESS).getValue()
                        );
                        if (target.hasStatusEffect(StatusEffects.RESISTANCE)) {
                            int resistance = 25 - (target.getStatusEffect(StatusEffects.RESISTANCE).getAmplifier() + 1) * 5;
                            float resistance_1 = var12 * resistance;
                            var12 = Math.max(resistance_1 / 25.0F, 0.0F);
                        }

                        return var12;
                    }
                }

                return 0.0F;
            }
        } else {
            return 0.0F;
        }
    }

    public static float getExplosionDamageWPredict(Vec3d explosionPos, PlayerEntity target, Box predict, boolean optimized) {
        if (MeteorClient.mc.world.getDifficulty() == Difficulty.PEACEFUL) {
            return 0.0F;
        } else if (target != null && predict != null) {
            if (explosion == null) {
                explosion = new Explosion(MeteorClient.mc.world, MeteorClient.mc.player, 1.0, 33.0, 7.0, 6.0F, false, DestructionType.DESTROY);
            }

            ((IExplosion) explosion).setX(explosionPos.x);
            ((IExplosion) explosion).setY(explosionPos.y);
            ((IExplosion) explosion).setZ(explosionPos.z);
            if (((IExplosion) explosion).getWorld() != MeteorClient.mc.world) {
                ((IExplosion) explosion).setWorld(MeteorClient.mc.world);
            }

            if (!new Box(
                MathHelper.floor(explosionPos.x - 11.0),
                MathHelper.floor(explosionPos.y - 11.0),
                MathHelper.floor(explosionPos.z - 11.0),
                MathHelper.floor(explosionPos.x + 13.0),
                MathHelper.floor(explosionPos.y + 13.0),
                MathHelper.floor(explosionPos.z + 13.0)
            )
                .intersects(predict)) {
                return 0.0F;
            } else {
                if (!target.isImmuneToExplosion(explosion) && !target.isInvulnerable()) {
                    double distExposure = predict.getCenter().add(0.0, -0.9, 0.0).squaredDistanceTo(explosionPos) / 144.0;
                    if (distExposure <= 1.0) {
                        double exposure = getExposure(explosionPos, predict, optimized);
                        terrainIgnore = false;
                        double finalExposure = (1.0 - distExposure) * exposure;
                        float toDamage = (float) Math.floor((finalExposure * finalExposure + finalExposure) / 2.0 * 7.0 * 12.0 + 1.0);
                        if (MeteorClient.mc.world.getDifficulty() == Difficulty.EASY) {
                            toDamage = Math.min(toDamage / 2.0F + 1.0F, toDamage);
                        } else if (MeteorClient.mc.world.getDifficulty() == Difficulty.HARD) {
                            toDamage = toDamage * 3.0F / 2.0F;
                        }

                        float var13 = DamageUtil.getDamageLeft(
                            target,
                            toDamage,
                            ((IExplosion) explosion).getDamageSource(),
                            target.getArmor(),
                            (float) target.getAttributeInstance(EntityAttributes.GENERIC_ARMOR_TOUGHNESS).getValue()
                        );
                        if (target.hasStatusEffect(StatusEffects.RESISTANCE)) {
                            int resistance = 25 - (Objects.requireNonNull(target.getStatusEffect(StatusEffects.RESISTANCE)).getAmplifier() + 1) * 5;
                            float resistance_1 = var13 * resistance;
                            var13 = Math.max(resistance_1 / 25.0F, 0.0F);
                        }

                        return var13;
                    }
                }

                return 0.0F;
            }
        } else {
            return 0.0F;
        }
    }

    private static float getExposureGhost(Vec3d source, Entity entity, BlockPos pos) {
        Box box = entity.getBoundingBox();
        double d = 1.0 / ((box.maxX - box.minX) * 2.0 + 1.0);
        double e = 1.0 / ((box.maxY - box.minY) * 2.0 + 1.0);
        double f = 1.0 / ((box.maxZ - box.minZ) * 2.0 + 1.0);
        double g = (1.0 - Math.floor(1.0 / d) * d) / 2.0;
        double h = (1.0 - Math.floor(1.0 / f) * f) / 2.0;
        if (!(d < 0.0) && !(e < 0.0) && !(f < 0.0)) {
            int i = 0;
            int j = 0;

            for (double k = 0.0; k <= 1.0; k += d) {
                for (double l = 0.0; l <= 1.0; l += e) {
                    for (double m = 0.0; m <= 1.0; m += f) {
                        double n = MathHelper.lerp(k, box.minX, box.maxX);
                        double o = MathHelper.lerp(l, box.minY, box.maxY);
                        double p = MathHelper.lerp(m, box.minZ, box.maxZ);
                        Vec3d vec3d = new Vec3d(n + g, o, p + h);
                        if (raycastGhost(new RaycastContext(vec3d, source, ShapeType.COLLIDER, FluidHandling.NONE, entity), pos).getType() == Type.MISS) {
                            i++;
                        }

                        j++;
                    }
                }
            }

            return (float) i / j;
        } else {
            return 0.0F;
        }
    }

    public static float getExposure(Vec3d source, Box box, boolean optimized) {
        if (!optimized) {
            return getExposure(source, box);
        } else {
            int miss = 0;
            int hit = 0;

            for (int k = 0; k <= 1; k++) {
                for (int l = 0; l <= 1; l++) {
                    for (int m = 0; m <= 1; m++) {
                        double n = MathHelper.lerp(k, box.minX, box.maxX);
                        double o = MathHelper.lerp(l, box.minY, box.maxY);
                        double p = MathHelper.lerp(m, box.minZ, box.maxZ);
                        new Vec3d(n, o, p);
                        hit++;
                    }
                }
            }

            return (float) miss / hit;
        }
    }

    public static float getExposure(Vec3d source, Box box) {
        double d = 0.4545454446934474;
        double e = 0.21739130885479366;
        double f = 0.4545454446934474;
        int i = 0;
        int j = 0;

        for (double k = 0.0; k <= 1.0; k += d) {
            for (double l = 0.0; l <= 1.0; l += e) {
                for (double m = 0.0; m <= 1.0; m += f) {
                    double n = MathHelper.lerp(k, box.minX, box.maxX);
                    double o = MathHelper.lerp(l, box.minY, box.maxY);
                    double p = MathHelper.lerp(m, box.minZ, box.maxZ);
                    new Vec3d(n + 0.045454555306552624, o, p + 0.045454555306552624);
                    j++;
                }
            }
        }

        return (float) i / j;
    }

    private static BlockHitResult raycastGhost(RaycastContext context, BlockPos bPos) {
        return (BlockHitResult) BlockView.raycast(context.getStart(), context.getEnd(), context, (innerContext, pos) -> {
            Vec3d vec3d = innerContext.getStart();
            Vec3d vec3d2 = innerContext.getEnd();
            BlockState blockState;
            if (!pos.equals(bPos)) {
                blockState = MeteorClient.mc.world.getBlockState(bPos);
            } else {
                blockState = Blocks.OBSIDIAN.getDefaultState();
            }

            VoxelShape voxelShape = innerContext.getBlockShape(blockState, MeteorClient.mc.world, pos);
            BlockHitResult blockHitResult = MeteorClient.mc.world.raycastBlock(vec3d, vec3d2, pos, voxelShape, blockState);
            BlockHitResult blockHitResult2 = VoxelShapes.empty().raycast(vec3d, vec3d2, pos);
            double d = blockHitResult == null ? Double.MAX_VALUE : innerContext.getStart().squaredDistanceTo(blockHitResult.getPos());
            double e = blockHitResult2 == null ? Double.MAX_VALUE : innerContext.getStart().squaredDistanceTo(blockHitResult2.getPos());
            return d <= e ? blockHitResult : blockHitResult2;
        }, innerContext -> {
            Vec3d vec3d = innerContext.getStart().subtract(innerContext.getEnd());
            return BlockHitResult.createMissed(innerContext.getEnd(), Direction.getFacing(vec3d.x, vec3d.y, vec3d.z), BlockPos.ofFloored(innerContext.getEnd()));
        });
    }
}
