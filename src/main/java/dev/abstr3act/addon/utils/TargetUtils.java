package dev.abstr3act.addon.utils;

import dev.abstr3act.addon.command.ForceTargetCommand;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.entity.EntityUtils;
import meteordevelopment.meteorclient.utils.entity.SortPriority;
import meteordevelopment.meteorclient.utils.entity.fakeplayer.FakePlayerEntity;
import meteordevelopment.meteorclient.utils.entity.fakeplayer.FakePlayerManager;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.GameMode;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class TargetUtils {
    public static final List<Entity> ENTITIES = new ArrayList<>();

    private TargetUtils() {
    }

    @Nullable
    public static Entity get(Predicate<Entity> isGood, SortPriority sortPriority) {
        ENTITIES.clear();
        getList(ENTITIES, isGood, sortPriority, 1);
        if (ForceTargetCommand.target != null) {
            return ForceTargetCommand.target;
        } else {
            return !ENTITIES.isEmpty() ? ENTITIES.getFirst() : null;
        }
    }

    public static void getList(List<Entity> targetList, Predicate<Entity> isGood, SortPriority sortPriority, int maxCount) {
        targetList.clear();

        for (Entity entity : MeteorClient.mc.world.getEntities()) {
            if (entity != null && isGood.test(entity)) {
                targetList.add(entity);
            }
        }

        FakePlayerManager.forEach(fp -> {
            if (fp != null && isGood.test(fp)) {
                targetList.add(fp);
            }
        });
        targetList.sort(sortPriority);
        if (ForceTargetCommand.target != null) {
            targetList.add(0, ForceTargetCommand.target);
        }

        for (int i = targetList.size() - 1; i >= maxCount; i--) {
            targetList.remove(i);
        }
    }

    @Nullable
    public static PlayerEntity getPlayerTarget(double range, SortPriority priority) {
        return !Utils.canUpdate()
            ? null
            : (PlayerEntity) get(
            entity -> {
                if (entity instanceof PlayerEntity && entity != MeteorClient.mc.player) {
                    if (((PlayerEntity) entity).isDead() || ((PlayerEntity) entity).getHealth() <= 0.0F) {
                        return false;
                    } else if (!PlayerUtils.isWithin(entity, range)) {
                        return false;
                    } else if (ForceTargetCommand.target != null && entity != ForceTargetCommand.target) {
                        return false;
                    } else {
                        return !Friends.get().shouldAttack((PlayerEntity) entity)
                            ? false
                            : EntityUtils.getGameMode((PlayerEntity) entity) == GameMode.SURVIVAL || entity instanceof FakePlayerEntity;
                    }
                } else {
                    return false;
                }
            },
            priority
        );
    }

    public static boolean isBadTarget(PlayerEntity target, double range) {
        return target == null ? true : !PlayerUtils.isWithin(target, range) || !target.isAlive() || target.isDead() || target.getHealth() <= 0.0F;
    }
}
