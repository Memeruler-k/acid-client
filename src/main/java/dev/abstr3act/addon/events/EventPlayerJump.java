package dev.abstr3act.addon.events;

public class EventPlayerJump {
    private boolean pre;

    public EventPlayerJump(boolean pre) {
        this.pre = pre;
    }

    public boolean isPre() {
        return this.pre;
    }
}
