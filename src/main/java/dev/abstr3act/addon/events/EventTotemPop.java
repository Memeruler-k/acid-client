package dev.abstr3act.addon.events;

import net.minecraft.entity.player.PlayerEntity;

public class EventTotemPop extends Event {
    private final PlayerEntity entity;
    private int pops;

    public EventTotemPop(PlayerEntity entity, int pops) {
        super(Stage.Post);
        this.entity = entity;
        this.pops = pops;
    }

    public PlayerEntity getEntity() {
        return this.entity;
    }

    public int getPops() {
        return this.pops;
    }
}
