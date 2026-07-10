package dev.abstr3act.addon.events;

import meteordevelopment.meteorclient.events.Cancellable;
import net.minecraft.screen.slot.SlotActionType;

public class EventClickSlot extends Cancellable {
    private final SlotActionType slotActionType;
    private final int slot;
    private final int button;
    private final int id;

    public EventClickSlot(SlotActionType slotActionType, int slot, int button, int id) {
        this.slot = slot;
        this.button = button;
        this.id = id;
        this.slotActionType = slotActionType;
    }

    public SlotActionType getSlotActionType() {
        return this.slotActionType;
    }

    public int getSlot() {
        return this.slot;
    }

    public int getButton() {
        return this.button;
    }

    public int getId() {
        return this.id;
    }
}
