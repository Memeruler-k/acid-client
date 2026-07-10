package dev.abstr3act.addon.mixin;

import com.mojang.authlib.GameProfile;
import dev.abstr3act.addon.events.*;
import dev.abstr3act.addon.module.BaseModule;
import dev.abstr3act.addon.modules.Seraphim.NoSlowV2;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.input.Input;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.util.ClientPlayerTickable;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.MovementType;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(
    value = {ClientPlayerEntity.class},
    priority = 800
)
public abstract class MixinClientPlayerEntity extends AbstractClientPlayerEntity {
    @Shadow
    public double lastX;
    @Shadow
    public double lastBaseY;
    @Shadow
    public double lastZ;
    @Shadow
    public float lastYaw;
    @Shadow
    public float lastPitch;
    @Shadow
    public boolean lastOnGround;
    @Final
    @Shadow
    public ClientPlayNetworkHandler networkHandler;
    @Shadow
    public int ticksSinceLastPositionPacketSent;
    @Shadow
    public Input input;
    @Shadow
    @Final
    protected MinecraftClient client;
    @Unique
    boolean pre_sprint_state = false;
    private boolean hideNextItemUse;
    @Unique
    private int fakeHurtTime = 0;
    @Shadow
    private boolean autoJumpEnabled;
    @Shadow
    @Final
    private List<ClientPlayerTickable> tickables;
    @Unique
    private boolean updateLock = false;
    @Unique
    private Runnable postAction;

    public MixinClientPlayerEntity(ClientWorld world, GameProfile profile) {
        super(world, profile);
    }

    @Shadow
    public abstract float getPitch(float var1);

    @Shadow
    protected abstract void sendSprintingPacket();

    @Shadow
    protected abstract boolean isCamera();

    @Inject(
        method = {"tick"},
        at = {@At("HEAD")}
    )
    public void tickHook(CallbackInfo info) {
        if (!BaseModule.fullNullCheck()) {
            MeteorClient.EVENT_BUS.post(new EventPlayerUpdate());
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

    @Redirect(
        method = {"tickMovement"},
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/network/ClientPlayerEntity;isUsingItem()Z"
        ),
        require = 0
    )
    private boolean tickMovementHook(ClientPlayerEntity player) {
        return ((NoSlowV2) Modules.get().get(NoSlowV2.class)).isActive() && ((NoSlowV2) Modules.get().get(NoSlowV2.class)).canNoSlow() ? false : player.isUsingItem();
    }

    @Inject(
        method = {"shouldSlowDown"},
        at = {@At("HEAD")},
        cancellable = true
    )
    public void shouldSlowDownHook(CallbackInfoReturnable<Boolean> cir) {
        if (((NoSlowV2) Modules.get().get(NoSlowV2.class)).isActive()) {
            if (this.isCrawling()) {
                if (((NoSlowV2) Modules.get().get(NoSlowV2.class)).crawl.get()) {
                    cir.setReturnValue(false);
                }
            } else if (((NoSlowV2) Modules.get().get(NoSlowV2.class)).sneak.get()) {
                cir.setReturnValue(false);
            }
        }
    }

    @Inject(
        method = {"move"},
        at = {@At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/network/AbstractClientPlayerEntity;move(Lnet/minecraft/entity/MovementType;Lnet/minecraft/util/math/Vec3d;)V"
        )},
        cancellable = true
    )
    public void onMoveHook(MovementType movementType, Vec3d movement, CallbackInfo ci) {
        if (MeteorClient.mc.player != null) {
            EventPlayerMove event = new EventPlayerMove(movement.x, movement.y, movement.z);
            MeteorClient.EVENT_BUS.post(event);
            if (event.isCancelled()) {
                super.move(movementType, new Vec3d(event.getX(), event.getY(), event.getZ()));
                ci.cancel();
            }
        }
    }

    @Inject(
        method = {"sendMovementPackets"},
        at = {@At("HEAD")},
        cancellable = true
    )
    private void sendMovementPacketsHook(CallbackInfo info) {
        if (!BaseModule.fullNullCheck()) {
            EventSync event = new EventSync(this.getYaw(), this.getPitch());
            MeteorClient.EVENT_BUS.post(event);
            this.postAction = event.getPostAction();
            EventSprint e = new EventSprint(this.isSprinting());
            MeteorClient.EVENT_BUS.post(e);
            MeteorClient.EVENT_BUS.post(new EventAfterRotate());
        }
    }
}
