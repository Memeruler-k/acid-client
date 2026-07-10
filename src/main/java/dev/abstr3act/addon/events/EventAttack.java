package dev.abstr3act.addon.events;

import meteordevelopment.meteorclient.events.Cancellable;
import net.minecraft.entity.Entity;

public class EventAttack extends Cancellable {
    boolean pre;
    private Entity entity;

    public EventAttack(Entity entity, boolean pre) {
        this.entity = entity;
        this.pre = pre;
    }

    public Entity getEntity() {
        return this.entity;
    }

    public boolean isPre() {
        return this.pre;
    }
}
