package dev.abstr3act.addon.events.legacy;

import meteordevelopment.meteorclient.events.Cancellable;
import net.minecraft.entity.Entity;

public class AttackEvent extends Cancellable {
    public final Entity entity;

    public AttackEvent(Entity entity) {
        this.entity = entity;
    }
}
