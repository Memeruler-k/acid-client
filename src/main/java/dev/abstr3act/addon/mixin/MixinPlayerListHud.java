package dev.abstr3act.addon.mixin;

import net.minecraft.client.gui.hud.PlayerListHud;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(
    value = {PlayerListHud.class},
    priority = 2000
)
public abstract class MixinPlayerListHud {
}
