package dev.abstr3act.addon.events;

public class Event {
    private final Stage stage;
    private boolean cancel = false;

    public Event(Stage stage) {
        this.stage = stage;
    }

    public void cancel() {
        this.setCancelled(true);
    }

    public boolean isCancel() {
        return this.cancel;
    }

    public boolean isCancelled() {
        return this.cancel;
    }

    public void setCancelled(boolean cancel) {
        this.cancel = cancel;
    }

    public Stage getStage() {
        return this.stage;
    }

    public boolean isPost() {
        return this.stage == Stage.Post;
    }

    public boolean isPre() {
        return this.stage == Stage.Pre;
    }

    public static enum Stage {
        Pre,
        Post;
    }
}
