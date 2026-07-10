package dev.abstr3act.addon.mixin;

import dev.abstr3act.addon.events.AddPotionEvent;
import dev.abstr3act.addon.events.Event;
import dev.abstr3act.addon.events.madcat.SprintEvent;
import dev.abstr3act.addon.modules.Seraphim.NoDeathAnimation;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Attackable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({LivingEntity.class})
public abstract class LivingEntityMixin extends Entity implements Attackable {
    @Shadow
    public int deathTime;

    public LivingEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Inject(
        method = {"updatePostDeath"},
        at = {@At("HEAD")},
        cancellable = true
    )
    public void onDeathTicks(CallbackInfo ci) {
        if (((NoDeathAnimation) Modules.get().get(NoDeathAnimation.class)).isActive()) {
            ci.cancel();
            this.deathTime++;
            if (this.deathTime >= 22 && !this.getWorld().isClient() && !this.isRemoved()) {
                this.getWorld().sendEntityStatus(this, (byte) 60);
                this.remove(RemovalReason.KILLED);
            }
        }
    }

    @Inject(
        method = {"addStatusEffect"},
        at = {@At("HEAD")}
    )
    private void onAddPotionEffect(StatusEffectInstance effect, CallbackInfoReturnable<Boolean> cir) {
        AddPotionEvent event = new AddPotionEvent(effect);
        MeteorClient.EVENT_BUS.post(event);
    }

    @Inject(
        method = {"setSprinting"},
        at = {@At("HEAD")},
        cancellable = true
    )
    public void setSprintingHook(boolean sprinting, CallbackInfo ci) {
        if (((LivingEntity) (Object) this) == MinecraftClient.getInstance().player) {
            SprintEvent event = new SprintEvent(Event.Stage.Pre);
            MeteorClient.EVENT_BUS.post(event);
            if (event.isCancelled()) {
                ci.cancel();
                sprinting = event.isSprint();
                super.setSprinting(sprinting);
            }
        }
    }
}
