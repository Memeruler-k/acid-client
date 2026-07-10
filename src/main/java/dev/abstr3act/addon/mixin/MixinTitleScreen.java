package dev.abstr3act.addon.mixin;

import dev.abstr3act.addon.gui.MainMenuScreen;
import meteordevelopment.meteorclient.MeteorClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({TitleScreen.class})
public class MixinTitleScreen extends Screen {
    protected MixinTitleScreen(Text title) {
        super(title);
    }

    @Inject(
        method = {"init"},
        at = {@At("RETURN")}
    )
    public void postInitHook(CallbackInfo ci) {
        if (!MainMenuScreen.getInstance().confirm) {
            MeteorClient.mc.setScreen(MainMenuScreen.getInstance());
        }
    }
}
