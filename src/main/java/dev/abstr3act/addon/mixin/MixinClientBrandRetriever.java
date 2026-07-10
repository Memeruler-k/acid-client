package dev.abstr3act.addon.mixin;

import dev.abstr3act.addon.modules.Compassion.ClientSpoof;
import net.minecraft.client.ClientBrandRetriever;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({ClientBrandRetriever.class})
public class MixinClientBrandRetriever {
    @Inject(
        method = {"getClientModName"},
        at = {@At("HEAD")},
        cancellable = true,
        remap = false
    )
    private static void getClientModNameHook(CallbackInfoReturnable<String> cir) {
        ClientSpoof clientSpoof = new ClientSpoof();
        if (clientSpoof.isActive()) {
            cir.setReturnValue(clientSpoof.getClientName());
        }
    }
}
