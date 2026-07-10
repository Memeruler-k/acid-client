package dev.abstr3act.addon.utils.luna;

import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.world.TickEvent.Post;
import meteordevelopment.orbit.EventHandler;

public class CountdownUtils {
    public static int INSTANCE = 0;
    public static boolean isEnd = false;
    public static boolean isRunning = false;
    int i = 0;
    private Durations durationsInstance;

    public boolean isStopped() {
        return isEnd;
    }

    public boolean isRunning() {
        return isRunning;
    }

    public void startTimer(int duration) {
        this.durationsInstance = new Durations(duration);
        isEnd = false;
        INSTANCE = duration;
        isRunning = true;
        MeteorClient.EVENT_BUS.subscribe(this);
    }

    public void runTask(int duration) {
        this.durationsInstance = new Durations(duration);
        isEnd = false;
        INSTANCE = duration;
        isRunning = true;
        MeteorClient.EVENT_BUS.subscribe(this);
    }

    public void stopTimer() {
        this.durationsInstance = new Durations(0);
        isRunning = false;
        INSTANCE = 0;
        isEnd = true;
        MeteorClient.EVENT_BUS.unsubscribe(this);
    }

    public int getTimer() {
        if (this.durationsInstance != null) {
            return this.durationsInstance.getDuration();
        } else {
            System.out.println("No timer set.");
            return 0;
        }
    }

    @EventHandler
    public void onTickEvent(Post event) {
        if (this.i < this.durationsInstance.getDuration()) {
            this.i++;
        } else {
            this.i = 0;
            this.stopTimer();
        }
    }

    static class Durations {
        private final int duration;

        public Durations(int duration) {
            this.duration = duration;
        }

        public int getDuration() {
            return this.duration;
        }
    }
}
