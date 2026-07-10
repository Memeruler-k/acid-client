package dev.abstr3act.addon.mixin;

import dev.abstr3act.addon.modules.Compassion.AutoLag;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({ItemEntity.class})
public abstract class ItemEntityMixin extends Entity {
    public ItemEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Inject(
        method = {"tick"},
        at = {@At("HEAD")},
        cancellable = true
    )
    private void cancelTick(CallbackInfo ci) {
        if (((AutoLag) Modules.get().get(AutoLag.class)).isActive()) {
            ci.cancel();
        }
    }
}
