package dev.abstr3act.addon.modules.Seraphim;

import dev.abstr3act.addon.Compassion;
import dev.abstr3act.addon.events.EventInteractBlock;
import dev.abstr3act.addon.module.SeraphimModule;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.util.math.BlockPos;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class NoBadInteracts extends SeraphimModule {
    private final Map<UUID, InteractionData> playerInteractions = new HashMap<>();

    public NoBadInteracts() {
        super(Compassion.SERAPHIM, "NoBadInteract", "Prevent GrimAC MultiInteract Checks");
    }

    @EventHandler
    public void onInteractBlock(EventInteractBlock event) {
        UUID playerId = this.mc.player.getUuid();
        BlockPos blockPos = event.getHitResult().getBlockPos();
        long currentTime = this.mc.world.getTime();
        InteractionData lastInteraction = this.playerInteractions.get(playerId);
        if (lastInteraction != null && lastInteraction.blockPos.equals(blockPos) && currentTime - lastInteraction.time <= 1L) {
            event.cancel();
        }

        this.playerInteractions.put(playerId, new InteractionData(blockPos, currentTime));
    }

    private record InteractionData(BlockPos blockPos, long time) {
    }
}
