package dev.abstr3act.addon.utils.fragment;

import meteordevelopment.meteorclient.MeteorClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

public class PredictUtil {
    public static boolean predicting = false;

    public static List<Vec3d> predict(int tick) {
        predicting = true;
        List<Vec3d> positions = new ArrayList<>();
        ClientPlayerEntity player = MeteorClient.mc.player;
        if (player == null) {
            predicting = false;
            return positions;
        } else {
            ClientWorld world = MeteorClient.mc.world;
            if (world == null) {
                predicting = false;
                return positions;
            } else {
                ClientPlayerEntity simulatedPlayer = new ClientPlayerEntity(
                    MeteorClient.mc, world, player.networkHandler, player.getStatHandler(), player.getRecipeBook(), true, true
                );
                simulatedPlayer.setPosition(player.getX(), player.getY(), player.getZ());
                simulatedPlayer.setYaw(player.getYaw());
                simulatedPlayer.setPitch(player.getPitch());
                simulatedPlayer.setOnGround(player.isOnGround());
                simulatedPlayer.setSprinting(player.isSprinting());
                simulatedPlayer.setSneaking(player.isSneaking());
                simulatedPlayer.setVelocity(player.getVelocity());
                simulatedPlayer.getAbilities().allowFlying = player.getAbilities().allowFlying;
                simulatedPlayer.getAbilities().flying = player.getAbilities().flying;
                simulatedPlayer.getAbilities().creativeMode = player.getAbilities().creativeMode;
                simulatedPlayer.noClip = player.noClip;

                for (int i = 0; i < tick; i++) {
                    simulatedPlayer.input.movementForward = player.input.movementForward;
                    simulatedPlayer.input.movementSideways = player.input.movementSideways;
                    simulatedPlayer.tickMovement();
                    positions.add(simulatedPlayer.getPos());
                }

                predicting = false;
                return positions;
            }
        }
    }
}
