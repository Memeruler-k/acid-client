package dev.abstr3act.addon.mixin;

import dev.abstr3act.addon.modules.Lacrymira.Media;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({SkinTextures.class})
public class MixinSkinTextures {
    @Unique
    private final Identifier skin = Identifier.of("acid", "/misc/skin.png");

    @Inject(
        method = {"texture"},
        at = {@At("HEAD")},
        cancellable = true
    )
    public void getSkinTextureHook(CallbackInfoReturnable<Identifier> cir) {
        if (((Media) Modules.get().get(Media.class)).isActive() && ((Media) Modules.get().get(Media.class)).skinProtect.get()) {
            cir.setReturnValue(this.skin);
        }
    }
}
