package dev.abstr3act.addon.mixin;

import dev.abstr3act.addon.utils.notifications.DrawUtils;
import meteordevelopment.meteorclient.renderer.Renderer2D;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({Renderer2D.class})
public class Renderer2DMixin {
    @Inject(
        method = {"init"},
        at = {@At("HEAD")},
        remap = false
    )
    private static void doInject(CallbackInfo ci) {
        DrawUtils.init();
    }
}
