package dev.abstr3act.addon.mixin;

import dev.abstr3act.addon.Compassion;
import dev.abstr3act.addon.events.EventKeyboardInput;
import meteordevelopment.meteorclient.MeteorClient;
import net.minecraft.client.input.KeyboardInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({KeyboardInput.class})
public class MixinKeyboardInput {
    @Inject(
        method = {"tick"},
        at = {@At(
            value = "FIELD",
            target = "Lnet/minecraft/client/input/KeyboardInput;sneaking:Z",
            shift = Shift.BEFORE
        )},
        cancellable = true
    )
    private void onSneak(boolean slowDown, float slowDownFactor, CallbackInfo ci) {
        if (!Compassion.fullNullCheck()) {
            EventKeyboardInput event = new EventKeyboardInput();
            MeteorClient.EVENT_BUS.post(event);
            if (event.isCancelled()) {
                ci.cancel();
            }
        }
    }
}
