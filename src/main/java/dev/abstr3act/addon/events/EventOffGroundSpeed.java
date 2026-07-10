package dev.abstr3act.addon.events;

public class EventOffGroundSpeed {
    public static final EventOffGroundSpeed INSTANCE = new EventOffGroundSpeed();
    public float speed;

    public static EventOffGroundSpeed get(float speed) {
        INSTANCE.speed = speed;
        return INSTANCE;
    }
}
