package dev.abstr3act.addon.utils.seraphim;

import com.mojang.authlib.GameProfile;
import meteordevelopment.meteorclient.MeteorClient;
import net.minecraft.block.BlockState;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.network.packet.Packet;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Predicate;

public final class PlayerUtils {
    public static boolean invalid() {
        return MeteorClient.mc == null
            || MeteorClient.mc.player == null
            || MeteorClient.mc.world == null
            || MeteorClient.mc.interactionManager == null
            || MeteorClient.mc.options == null;
    }

    public static boolean valid() {
        return !invalid();
    }

    public static boolean playerValid(PlayerEntity player) {
        if (!invalid() && player != null) {
            ClientPlayerEntity p = MeteorClient.mc.player;
            GameProfile profile = player.getGameProfile();
            PlayerListEntry entry = p.networkHandler.getPlayerListEntry(profile.getId());
            return entry != null;
        } else {
            return false;
        }
    }

    public static ClientPlayerEntity player() {
        return MeteorClient.mc.player;
    }

    public static ClientPlayerInteractionManager getInteractions() {
        return MeteorClient.mc.interactionManager;
    }

    public static World getWorld() {
        return player().getWorld();
    }

    public static Vec3d getPos() {
        return player().getPos();
    }

    public static Vec3d getEyes() {
        return player().getEyePos();
    }

    public static ClientPlayerInteractionManager getInteractionManager() {
        return MeteorClient.mc.interactionManager;
    }

    public static void sendPacket(Packet<?> packet) {
        if (!invalid()) {
            player().networkHandler.sendPacket(packet);
        }
    }

    public static long getPing() {
        if (invalid()) {
            return -1L;
        } else {
            GameProfile p = player().getGameProfile();
            PlayerListEntry entry = player().networkHandler.getPlayerListEntry(p.getId());
            return entry == null ? -1L : entry.getLatency();
        }
    }

    public static int getFps() {
        return MeteorClient.mc.getCurrentFps();
    }

    public static boolean isMoving() {
        if (invalid()) {
            return false;
        } else {
            ClientPlayerEntity p = player();
            return p.sidewaysSpeed != 0.0F || p.forwardSpeed != 0.0F;
        }
    }

    public static boolean isColliding() {
        if (invalid()) {
            return false;
        } else {
            ClientPlayerEntity p = player();
            return p.horizontalCollision || p.verticalCollision;
        }
    }

    public static boolean isCollidingHorizontally() {
        if (invalid()) {
            return false;
        } else {
            ClientPlayerEntity p = player();
            return p.horizontalCollision;
        }
    }

    public static boolean isCollidingVertically() {
        if (invalid()) {
            return false;
        } else {
            ClientPlayerEntity p = player();
            return p.verticalCollision;
        }
    }

    public static int getChestPlate() {
        int slot = MeteorClient.mc.player.getInventory().selectedSlot;

        for (int i = 0; i < 9; i++) {
            ItemStack stack = MeteorClient.mc.player.getInventory().getStack(i);
            if (nameContains("_chestplate", stack)) {
                slot = i;
            }
        }

        return slot;
    }

    public static void boxIterator(World world, Box box, BiConsumer<BlockPos, BlockState> function) {
        for (double x = box.minX; x <= box.maxX; x++) {
            for (double y = box.minY; y <= box.maxY; y++) {
                for (double z = box.minZ; z <= box.maxZ; z++) {
                    BlockPos pos = new BlockPos((int) Math.floor(x), (int) Math.floor(y), (int) Math.floor(z));
                    BlockState state = world.getBlockState(pos);
                    if (state != null && !state.isAir()) {
                        function.accept(pos, state);
                    }
                }
            }
        }
    }

    public static Entity getNearestEntity(World world, Entity exclude, Vec3d at, double range, Predicate<Entity> filter) {
        List<Entity> candidates = world.getOtherEntities(exclude, Box.from(at).expand(range), filter)
            .stream()
            .sorted(Comparator.comparing(entity -> entity.getPos().distanceTo(at)))
            .toList();
        return candidates.isEmpty() ? null : candidates.get(0);
    }

    public static boolean hasEffects() {
        return valid() && !player().getStatusEffects().isEmpty();
    }

    public static Entity getNearestEntity(double range, Predicate<Entity> filter) {
        return invalid() ? null : getNearestEntity(getWorld(), player(), player().getPos(), range, filter);
    }

    public static PlayerEntity getNearestPlayer(double range, Predicate<Entity> filter) {
        return invalid()
            ? null
            : (PlayerEntity) getNearestEntity(getWorld(), player(), player().getPos(), range, entity -> entity instanceof PlayerEntity && filter.test(entity));
    }

    public static boolean runOnNearestBlock(double range, BiPredicate<BlockPos, BlockState> filter, BiConsumer<BlockPos, BlockState> function) {
        if (invalid()) {
            return false;
        } else {
            AtomicReference<Double> nearestDist = new AtomicReference<>(64.0);
            AtomicReference<BlockPos> nearestPos = new AtomicReference<>();
            AtomicReference<BlockState> nearestState = new AtomicReference<>();
            Box box = player().getBoundingBox().expand(range);
            Vec3d player = player().getPos();
            World world = getWorld();
            boxIterator(world, box, (pos, state) -> {
                if (filter.test(pos, state) && pos.isWithinDistance(player, nearestDist.get())) {
                    nearestDist.set(Math.sqrt(pos.getSquaredDistance(player)));
                    nearestPos.set(pos);
                    nearestState.set(state);
                }
            });
            if (nearestState.get() != null && nearestPos.get() != null) {
                function.accept(nearestPos.get(), nearestState.get());
                return true;
            } else {
                return false;
            }
        }
    }

    public static boolean canBreak() {
        if (invalid()) {
            return false;
        } else {
            ClientPlayerEntity p = player();
            StatusEffectInstance s = p.getStatusEffect(StatusEffects.STRENGTH);
            StatusEffectInstance w = p.getStatusEffect(StatusEffects.WEAKNESS);
            if (s == null && w == null) {
                return true;
            } else if (w == null) {
                return true;
            } else {
                return s != null && s.getAmplifier() > w.getAmplifier() ? true : isHoldingTool();
            }
        }
    }

    public static boolean canBreakCrystal() {
        if (invalid()) {
            return false;
        } else {
            ClientPlayerEntity p = player();
            StatusEffectInstance s = p.getStatusEffect(StatusEffects.STRENGTH);
            StatusEffectInstance w = p.getStatusEffect(StatusEffects.WEAKNESS);
            if (s == null && w == null) {
                return true;
            } else {
                return w == null ? true : s != null && s.getAmplifier() > w.getAmplifier();
            }
        }
    }

    public static boolean isValidWeaknessItem(ItemStack itemStack) {
        if (itemStack.getItem() instanceof ToolItem && !(itemStack.getItem() instanceof HoeItem)) {
            ToolMaterial material = ((ToolItem) itemStack.getItem()).getMaterial();
            return material == ToolMaterials.DIAMOND || material == ToolMaterials.NETHERITE;
        } else {
            return false;
        }
    }

    public static boolean isHoldingTool() {
        return nameContains("_sword")
            || nameContains("_pickaxe")
            || nameContains("_axe")
            || nameContains("_hoe")
            || nameContains("_shovel")
            || nameContains("trident");
    }

    public static boolean nameContains(String contains) {
        return nameContains(contains, Hand.MAIN_HAND);
    }

    public static boolean nameContains(String contains, Hand hand) {
        if (invalid()) {
            return false;
        } else {
            ItemStack item = player().getStackInHand(hand);
            return item != null && item.getTranslationKey().toLowerCase().contains(contains.toLowerCase());
        }
    }

    public static boolean nameContains(String contains, int slot) {
        if (invalid()) {
            return false;
        } else {
            ItemStack item = player().getInventory().getStack(slot);
            return item != null && item.getTranslationKey().toLowerCase().contains(contains.toLowerCase());
        }
    }

    public static boolean nameContains(String contains, ItemStack itemStack) {
        return invalid() ? false : itemStack != null && itemStack.getTranslationKey().toLowerCase().contains(contains.toLowerCase());
    }

    public static boolean runOnNearestBlock(double range, Predicate<BlockState> filter, BiConsumer<BlockPos, BlockState> function) {
        return runOnNearestBlock(range, (pos, state) -> filter.test(state), function);
    }

    public static boolean runOnNearestEntity(double range, Predicate<Entity> filter, Consumer<Entity> function) {
        if (invalid()) {
            return false;
        } else {
            Entity ent = getNearestEntity(range, filter);
            if (ent != null) {
                function.accept(ent);
                return true;
            } else {
                return false;
            }
        }
    }

    public static BlockPos getNearestBlock(double range, Predicate<BlockState> filter) {
        if (invalid()) {
            return null;
        } else {
            AtomicReference<Double> nearestDist = new AtomicReference<>(range);
            AtomicReference<BlockPos> nearestPos = new AtomicReference<>();
            Box box = player().getBoundingBox().expand(range);
            Vec3d playerPos = player().getPos();
            World world = getWorld();
            boxIterator(world, box, (pos, state) -> {
                if (filter.test(state) && pos.isWithinDistance(playerPos, nearestDist.get())) {
                    double distance = Math.sqrt(pos.getSquaredDistance(playerPos));
                    if (distance < nearestDist.get()) {
                        nearestDist.set(distance);
                        nearestPos.set(pos);
                    }
                }
            });
            return nearestPos.get();
        }
    }
}
