package dev.abstr3act.addon.notifications.events;

import dev.abstr3act.addon.notifications.NotificationEvent;
import meteordevelopment.meteorclient.systems.modules.Module;

public class ModuleToggledNotificationEvent extends NotificationEvent {
    private final Module toggledModule;
    private final boolean newState;

    public ModuleToggledNotificationEvent(Module toggledModule) {
        this.toggledModule = toggledModule;
        this.newState = toggledModule.isActive();
    }

    public Module getToggledModule() {
        return this.toggledModule;
    }

    public boolean getNewState() {
        return this.newState;
    }
}
