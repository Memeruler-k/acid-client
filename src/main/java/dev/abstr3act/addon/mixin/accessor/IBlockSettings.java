package dev.abstr3act.addon.mixin.accessor;

import net.minecraft.block.AbstractBlock.Settings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin({Settings.class})
public interface IBlockSettings {
    @Accessor("replaceable")
    boolean replaceable();
}
