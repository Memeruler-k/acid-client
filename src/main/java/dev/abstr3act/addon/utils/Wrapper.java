package dev.abstr3act.addon.utils;

import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.meteorclient.utils.world.TickRate;
import net.minecraft.client.tutorial.TutorialStep;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;

public class Wrapper {
    public static void disableTutorial() {
        MeteorClient.mc.getTutorialManager().setStep(TutorialStep.NONE);
    }

    public static boolean isLagging() {
        return TickRate.INSTANCE.getTimeSinceLastTick() >= 0.8;
    }

    public static float getTotalHealth(PlayerEntity p) {
        return p.getHealth() + p.getAbsorptionAmount();
    }

    public static Item getItemFromSlot(Integer slot) {
        if (slot == -1) {
            return null;
        } else {
            return slot == 45 ? MeteorClient.mc.player.getOffHandStack().getItem() : MeteorClient.mc.player.getInventory().getStack(slot).getItem();
        }
    }

    public static void updateSlot(int newSlot) {
        MeteorClient.mc.player.getInventory().selectedSlot = newSlot;
    }

    public static void swingHand(boolean offhand) {
        if (offhand) {
            MeteorClient.mc.player.swingHand(Hand.OFF_HAND);
        } else {
            MeteorClient.mc.player.swingHand(Hand.MAIN_HAND);
        }
    }

    public static boolean isPlayerMoving(PlayerEntity p) {
        return p.forwardSpeed != 0.0F || p.sidewaysSpeed != 0.0F;
    }

    public static boolean isInHole(PlayerEntity p) {
        BlockPos pos = p.getBlockPos();
        return !MeteorClient.mc.world.getBlockState(pos.add(1, 0, 0)).isAir()
            && !MeteorClient.mc.world.getBlockState(pos.add(-1, 0, 0)).isAir()
            && !MeteorClient.mc.world.getBlockState(pos.add(0, 0, 1)).isAir()
            && !MeteorClient.mc.world.getBlockState(pos.add(0, 0, -1)).isAir()
            && !MeteorClient.mc.world.getBlockState(pos.add(0, -1, 0)).isAir();
    }

    public static int randomNum(int min, int max) {
        return min + (int) (Math.random() * (max - min + 1));
    }

    public static void messagePlayer(String playerName, String m) {
        assert MeteorClient.mc.player != null;

        ChatUtils.sendPlayerMsg("/msg " + playerName + " " + m);
    }
}
