package dev.abstr3act.addon.mixin;

import dev.abstr3act.addon.events.EventAttackBlock;
import dev.abstr3act.addon.events.EventBreakBlock;
import dev.abstr3act.addon.events.EventClickSlot;
import dev.abstr3act.addon.events.EventInteractBlock;
import dev.abstr3act.addon.events.legacy.AttackEvent;
import dev.abstr3act.addon.module.BaseModule;
import meteordevelopment.meteorclient.MeteorClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({ClientPlayerInteractionManager.class})
public class MixinClientPlayerInteractionManager {
    @Inject(
        method = {"interactBlock"},
        at = {@At("HEAD")},
        cancellable = true
    )
    public void interactBlock(ClientPlayerEntity player, Hand hand, BlockHitResult hitResult, CallbackInfoReturnable cl) {
        EventInteractBlock eventInteractBlock = new EventInteractBlock(player, hand, hitResult);
        MeteorClient.EVENT_BUS.post(eventInteractBlock);
        if (eventInteractBlock.isCancelled()) {
            cl.setReturnValue(ActionResult.FAIL);
            cl.cancel();
        }
    }

    @Inject(
        method = {"clickSlot"},
        at = {@At("HEAD")},
        cancellable = true
    )
    public void clickSlotHook(int syncId, int slotId, int button, SlotActionType actionType, PlayerEntity player, CallbackInfo ci) {
        if (!BaseModule.fullNullCheck()) {
            EventClickSlot event = new EventClickSlot(actionType, slotId, button, syncId);
            MeteorClient.EVENT_BUS.post(event);
            if (event.isCancelled()) {
                ci.cancel();
            }
        }
    }

    @Inject(
        method = {"breakBlock"},
        at = {@At("HEAD")},
        cancellable = true
    )
    public void breakBlockHook(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (!BaseModule.fullNullCheck()) {
            EventBreakBlock event = new EventBreakBlock(pos);
            MeteorClient.EVENT_BUS.post(event);
            if (event.isCancelled()) {
                cir.setReturnValue(false);
            }
        }
    }

    @Inject(
        method = {"attackBlock"},
        at = {@At("HEAD")},
        cancellable = true
    )
    private void attackBlockHook(BlockPos pos, Direction direction, CallbackInfoReturnable<Boolean> cir) {
        if (!BaseModule.fullNullCheck()) {
            EventAttackBlock event = new EventAttackBlock(pos, direction);
            MeteorClient.EVENT_BUS.post(event);
            if (event.isCancelled()) {
                cir.setReturnValue(false);
            }
        }
    }

    @Inject(
        method = {"attackEntity"},
        at = {@At("HEAD")},
        cancellable = true
    )
    public void attackEntity(PlayerEntity player, Entity entity, CallbackInfo ci) {
        AttackEvent event = new AttackEvent(entity);
        MeteorClient.EVENT_BUS.post(event);
        if (event.isCancelled()) {
            ci.cancel();
        }
    }
}
