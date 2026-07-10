package dev.abstr3act.addon.events;

import net.minecraft.entity.effect.StatusEffectInstance;

public class AddPotionEvent {
    private final StatusEffectInstance potionEffect;

    public AddPotionEvent(StatusEffectInstance potionEffect) {
        this.potionEffect = potionEffect;
    }

    public StatusEffectInstance getPotionEffect() {
        return this.potionEffect;
    }

    public int getDuration() {
        return this.potionEffect.getDuration();
    }
}
