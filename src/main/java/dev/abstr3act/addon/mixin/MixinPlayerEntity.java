package dev.abstr3act.addon.mixin;

import dev.abstr3act.addon.events.*;
import dev.abstr3act.addon.modules.Compassion.AutoSprint;
import dev.abstr3act.addon.modules.Lacrymira.Media;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(
    value = {PlayerEntity.class},
    priority = 800
)
public class MixinPlayerEntity {
    @Inject(
        method = {"travel"},
        at = {@At("HEAD")},
        cancellable = true
    )
    private void onTravelhookPre(Vec3d movementInput, CallbackInfo ci) {
        if (MeteorClient.mc.player != null) {
            EventPlayerTravel event = new EventPlayerTravel(movementInput, true);
            MeteorClient.EVENT_BUS.post(event);
            if (event.isCancelled()) {
                MeteorClient.mc.player.move(MovementType.SELF, MeteorClient.mc.player.getVelocity());
                ci.cancel();
            }
        }
    }

    @Inject(
        method = {"tickMovement"},
        at = {@At("HEAD")},
        cancellable = true
    )
    private void updateEvent(CallbackInfo ci) {
        if (((PlayerEntity) (Object) (this)) instanceof ClientPlayerEntity) {
            EventUpdate event = new EventUpdate();
            MeteorClient.EVENT_BUS.post(event);
            if (event.isCancelled()) {
                ci.cancel();
            }
        }
    }

    @Inject(
        method = {"getOffGroundSpeed"},
        at = {@At("RETURN")},
        cancellable = true
    )
    private void onGetOffGroundSpeed(CallbackInfoReturnable<Float> cir) {
        cir.setReturnValue(((EventOffGroundSpeed) MeteorClient.EVENT_BUS.post(EventOffGroundSpeed.get(cir.getReturnValueF()))).speed);
    }

    @Inject(
        method = {"getDisplayName"},
        at = {@At("HEAD")},
        cancellable = true
    )
    public void getDisplayNameHook(CallbackInfoReturnable<Text> cir) {
        if (((Media) Modules.get().get(Media.class)).isActive() && ((Media) Modules.get().get(Media.class)).nameProtect.get()) {
            cir.setReturnValue(Text.of((String) ((Media) Modules.get().get(Media.class)).protectedString.get()));
        }
    }

    @Inject(
        method = {"eatFood"},
        at = {@At("RETURN")}
    )
    public void eatFoodHook(World world, ItemStack stack, FoodComponent foodComponent, CallbackInfoReturnable<ItemStack> cir) {
        MeteorClient.EVENT_BUS.post(new EventEatFood((ItemStack) cir.getReturnValue()));
    }

    @Inject(
        method = {"attack"},
        at = {@At(
            value = "INVOKE",
            target = "Lnet/minecraft/entity/player/PlayerEntity;setSprinting(Z)V",
            shift = Shift.AFTER
        )}
    )
    public void attackAHook(CallbackInfo callbackInfo) {
        if (((AutoSprint) Modules.get().get(AutoSprint.class)).isActive() && ((AutoSprint) Modules.get().get(AutoSprint.class)).sprint.get()) {
            float multiplier = (float) (0.6F + 0.4F * ((AutoSprint) Modules.get().get(AutoSprint.class)).motion.get());
            MeteorClient.mc
                .player
                .setVelocity(
                    MeteorClient.mc.player.getVelocity().x / 0.6 * multiplier,
                    MeteorClient.mc.player.getVelocity().y,
                    MeteorClient.mc.player.getVelocity().z / 0.6 * multiplier
                );
            MeteorClient.mc.player.setSprinting(true);
        }
    }

    @Inject(
        method = {"attack"},
        at = {@At("HEAD")},
        cancellable = true
    )
    private void attackAHook2(Entity target, CallbackInfo ci) {
        EventAttack event = new EventAttack(target, false);
        MeteorClient.EVENT_BUS.post(event);
        if (event.isCancelled()) {
            ci.cancel();
        }
    }

    @Inject(
        method = {"travel"},
        at = {@At("RETURN")},
        cancellable = true
    )
    private void onTravelhookPost(Vec3d movementInput, CallbackInfo ci) {
        if (MeteorClient.mc.player != null) {
            EventPlayerTravel event = new EventPlayerTravel(movementInput, false);
            MeteorClient.EVENT_BUS.post(event);
            if (event.isCancelled()) {
                MeteorClient.mc.player.move(MovementType.SELF, MeteorClient.mc.player.getVelocity());
                ci.cancel();
            }
        }
    }

    @Inject(
        method = {"jump"},
        at = {@At("HEAD")}
    )
    private void onJumpPre(CallbackInfo ci) {
        MeteorClient.EVENT_BUS.post(new EventPlayerJump(true));
    }

    @Inject(
        method = {"jump"},
        at = {@At("RETURN")}
    )
    private void onJumpPost(CallbackInfo ci) {
        MeteorClient.EVENT_BUS.post(new EventPlayerJump(false));
    }
}
