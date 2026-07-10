package dev.abstr3act.addon.events.madcat;

import dev.abstr3act.addon.events.Event;

public class SprintEvent extends Event {
    private boolean sprint = false;

    public SprintEvent(Stage stage) {
        super(stage);
    }

    public boolean isSprint() {
        return this.sprint;
    }

    public void setSprint(boolean sprint) {
        this.sprint = sprint;
    }
}
