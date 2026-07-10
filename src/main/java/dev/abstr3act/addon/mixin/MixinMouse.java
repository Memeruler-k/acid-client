package dev.abstr3act.addon.mixin;

import dev.abstr3act.addon.events.madcat.MouseUpdateEvent;
import meteordevelopment.meteorclient.MeteorClient;
import net.minecraft.client.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({Mouse.class})
public class MixinMouse {
    @Inject(
        method = {"updateMouse"},
        at = {@At("RETURN")}
    )
    private void updateHook(CallbackInfo ci) {
        MeteorClient.EVENT_BUS.post(new MouseUpdateEvent());
    }
}
