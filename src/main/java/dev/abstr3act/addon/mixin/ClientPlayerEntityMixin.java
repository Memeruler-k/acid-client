package dev.abstr3act.addon.mixin;

import com.mojang.authlib.GameProfile;
import dev.abstr3act.addon.events.EventPostSync;
import dev.abstr3act.addon.events.PlayerUseMultiplierEvent;
import dev.abstr3act.addon.module.BaseModule;
import dev.abstr3act.addon.modules.Seraphim.NoDeathAnimation;
import dev.abstr3act.addon.utils.MovementUtil;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.input.Input;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(
    value = {ClientPlayerEntity.class},
    priority = 900
)
public abstract class ClientPlayerEntityMixin extends PlayerEntity {
    @Final
    @Shadow
    public ClientPlayNetworkHandler networkHandler;
    @Shadow
    public Input input;
    @Shadow
    @Final
    protected MinecraftClient client;
    @Unique
    boolean pre_sprint_state = false;
    @Unique
    private Runnable postAction;
    private boolean hideNextItemUse;

    public ClientPlayerEntityMixin(World world, BlockPos pos, float yaw, GameProfile gameProfile) {
        super(world, pos, yaw, gameProfile);
    }

    @Inject(
        method = {"sendMovementPackets"},
        at = {@At("RETURN")},
        cancellable = true
    )
    private void sendMovementPacketsPostHook(CallbackInfo info) {
        if (!BaseModule.fullNullCheck()) {
            MeteorClient.mc.player.lastSprinting = this.pre_sprint_state;
            EventPostSync event = new EventPostSync();
            MeteorClient.EVENT_BUS.post(event);
            if (this.postAction != null) {
                this.postAction.run();
                this.postAction = null;
            }

            if (event.isCancelled()) {
                info.cancel();
            }
        }
    }

    @Inject(
        method = {"tickMovement"},
        at = {@At("HEAD")},
        cancellable = true
    )
    private void updateEvent(CallbackInfo ci) {
        if (((ClientPlayerEntity) (Object) (this)) instanceof ClientPlayerEntity) {
            if (MeteorClient.mc.player != null) {
                if (MeteorClient.mc.player.isOnGround()) {
                    MovementUtil.fallTicks = 0;
                } else {
                    MovementUtil.fallTicks++;
                }
            }
        }
    }

    @Inject(
        method = {"tickMovement"},
        at = {@At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/network/ClientPlayerEntity;isUsingItem()Z",
            ordinal = 0
        )}
    )
    private void hookCustomMultiplier(CallbackInfo ci) {
        PlayerUseMultiplierEvent playerUseMultiplier = new PlayerUseMultiplierEvent(0.2F, 0.2F);
        MeteorClient.EVENT_BUS.post(playerUseMultiplier);
        if (playerUseMultiplier.getForward() != 0.2F || playerUseMultiplier.getSideways() != 0.2F) {
            Input input = this.input;
            input.movementForward /= 0.2F;
            input.movementSideways /= 0.2F;
            input.movementForward = input.movementForward * playerUseMultiplier.getForward();
            input.movementSideways = input.movementSideways * playerUseMultiplier.getSideways();
        }
    }

    @Inject(
        at = {@At("HEAD")},
        method = {"isUsingItem()Z"},
        cancellable = true
    )
    private void onIsUsingItem(CallbackInfoReturnable<Boolean> cir) {
        if (this.hideNextItemUse) {
            cir.setReturnValue(false);
            this.hideNextItemUse = false;
        }
    }

    @Inject(
        at = {@At(
            value = "FIELD",
            target = "Lnet/minecraft/client/network/ClientPlayerEntity;ticksToNextAutojump:I",
            opcode = 180,
            ordinal = 0
        )},
        method = {"tickMovement()V"}
    )
    private void afterIsUsingItem(CallbackInfo ci) {
        this.hideNextItemUse = false;
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
            if (this.deathTime == 22) {
                this.remove(RemovalReason.KILLED);
            }
        }
    }
}
