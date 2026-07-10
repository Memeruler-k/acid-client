package dev.abstr3act.addon.utils.timers;

public final class TickTimer {
    public int tick = 0;

    public void update() {
        this.tick++;
    }

    public void reset() {
        this.tick = 0;
    }

    public boolean hasTimePassed(int ticks) {
        return this.tick >= ticks;
    }
}
