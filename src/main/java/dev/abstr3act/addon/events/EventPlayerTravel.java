package dev.abstr3act.addon.events;

import meteordevelopment.meteorclient.events.Cancellable;
import net.minecraft.util.math.Vec3d;

public class EventPlayerTravel extends Cancellable {
    private Vec3d mVec;
    private boolean pre;

    public EventPlayerTravel(Vec3d mVec, boolean pre) {
        this.mVec = mVec;
        this.pre = pre;
    }

    public Vec3d getVec() {
        return this.mVec;
    }

    public boolean isPre() {
        return this.pre;
    }
}
