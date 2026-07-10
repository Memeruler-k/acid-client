package dev.abstr3act.addon.mixin;

import dev.abstr3act.addon.modules.Lacrymira.Media;
import dev.abstr3act.addon.modules.Seraphim.DesignAnt;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.text.TextVisitFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin({TextVisitFactory.class})
public abstract class TextVisitFactoryMixin {
    @ModifyArg(
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/text/TextVisitFactory;visitFormatted(Ljava/lang/String;ILnet/minecraft/text/Style;Lnet/minecraft/text/Style;Lnet/minecraft/text/CharacterVisitor;)Z",
            ordinal = 0
        ),
        method = {"visitFormatted(Ljava/lang/String;ILnet/minecraft/text/Style;Lnet/minecraft/text/CharacterVisitor;)Z"},
        index = 0
    )
    private static String adjust(String text) {
        return Modules.get() != null && ((DesignAnt) Modules.get().get(DesignAnt.class)).isActive()
            ? ((DesignAnt) Modules.get().get(DesignAnt.class)).replaceName(text)
            : text;
    }

    @ModifyArg(
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/text/TextVisitFactory;visitFormatted(Ljava/lang/String;ILnet/minecraft/text/Style;Lnet/minecraft/text/Style;Lnet/minecraft/text/CharacterVisitor;)Z",
            ordinal = 0
        ),
        method = {"visitFormatted(Ljava/lang/String;ILnet/minecraft/text/Style;Lnet/minecraft/text/CharacterVisitor;)Z"},
        index = 0
    )
    private static String adjust2(String text) {
        return Modules.get() != null && ((Media) Modules.get().get(Media.class)).isActive() ? ((Media) Modules.get().get(Media.class)).replaceName(text) : text;
    }

    @ModifyArg(
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/text/TextVisitFactory;visitFormatted(Ljava/lang/String;ILnet/minecraft/text/Style;Lnet/minecraft/text/Style;Lnet/minecraft/text/CharacterVisitor;)Z",
            ordinal = 0
        ),
        method = {"visitFormatted(Ljava/lang/String;ILnet/minecraft/text/Style;Lnet/minecraft/text/CharacterVisitor;)Z"},
        index = 0
    )
    private static String adjust3(String text) {
        return Modules.get() != null
            && ((Media) Modules.get().get(Media.class)).isActive()
            && ((Media) Modules.get().get(Media.class)).hideIP.get()
            && ((Media) Modules.get().get(Media.class)).getServerIP() != null
            ? ((Media) Modules.get().get(Media.class)).replaceIP(((Media) Modules.get().get(Media.class)).getServerIP())
            : text;
    }
}
