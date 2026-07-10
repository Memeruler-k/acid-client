package dev.abstr3act.addon.events;

public class EventSprint {
    private boolean sprintState;

    public EventSprint(boolean sprintState) {
        this.sprintState = sprintState;
    }

    public boolean getSprintState() {
        return this.sprintState;
    }

    public void setSprintState(boolean sprintState) {
        this.sprintState = sprintState;
    }
}
