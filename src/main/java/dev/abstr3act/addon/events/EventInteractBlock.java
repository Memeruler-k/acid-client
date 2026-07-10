package dev.abstr3act.addon.events;

import meteordevelopment.meteorclient.events.Cancellable;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;

public class EventInteractBlock extends Cancellable {
    private ClientPlayerEntity player;
    private Hand hand;
    private BlockHitResult hitResult;

    public EventInteractBlock(ClientPlayerEntity player, Hand hand, BlockHitResult hitResult) {
        this.player = player;
        this.hand = hand;
        this.hitResult = hitResult;
    }

    public ClientPlayerEntity getPlayer() {
        return this.player;
    }

    public Hand getHand() {
        return this.hand;
    }

    public BlockHitResult getHitResult() {
        return this.hitResult;
    }
}
