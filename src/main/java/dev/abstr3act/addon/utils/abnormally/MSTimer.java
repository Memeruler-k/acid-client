package dev.abstr3act.addon.utils.abnormally;

public class MSTimer {
    private long lastTime = System.currentTimeMillis();

    public final boolean hasPassTime(long time) {
        return this.getPassTime() >= time;
    }

    public final boolean hasPassTime(int time) {
        return this.getPassTime() >= time;
    }

    public final void reset() {
        this.lastTime = System.currentTimeMillis();
    }

    public final long getPassTime() {
        return System.currentTimeMillis() - this.lastTime;
    }
}
