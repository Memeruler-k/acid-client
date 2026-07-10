package dev.abstr3act.addon.mixin;

import dev.abstr3act.addon.modules.Seraphim.KillAura;
import dev.abstr3act.addon.modules.Seraphim.SwingAnimation;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ShieldItem;
import net.minecraft.item.SwordItem;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import net.minecraft.util.UseAction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({HeldItemRenderer.class})
public abstract class MixinHeldItemRenderer {
    @Inject(
        method = {"renderFirstPersonItem"},
        at = {@At("HEAD")},
        cancellable = true
    )
    private void hideShield(
        AbstractClientPlayerEntity player,
        float tickDelta,
        float pitch,
        Hand hand,
        float swingProgress,
        ItemStack item,
        float equipProgress,
        MatrixStack matrices,
        VertexConsumerProvider vertexConsumers,
        int light,
        CallbackInfo ci
    ) {
        boolean shouldHide = ((SwingAnimation) Modules.get().get(SwingAnimation.class)).isActive()
            || KillAura.wasTargeting && !((KillAura.AutoBlock) ((KillAura) Modules.get().get(KillAura.class)).autoBlockMode.get()).equals(KillAura.AutoBlock.None);
        if (shouldHide
            && hand == Hand.OFF_HAND
            && item.getItem() instanceof ShieldItem
            && !player.getStackInHand(Hand.MAIN_HAND).isEmpty()
            && player.getStackInHand(Hand.MAIN_HAND).getItem() instanceof SwordItem) {
            ci.cancel();
        }
    }

    @Redirect(
        method = {"renderFirstPersonItem"},
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/item/ItemStack;getUseAction()Lnet/minecraft/util/UseAction;",
            ordinal = 0
        )
    )
    private UseAction hookUseAction(ItemStack instance) {
        Item item = instance.getItem();
        return item instanceof SwordItem
            && KillAura.wasTargeting
            && !((KillAura.AutoBlock) ((KillAura) Modules.get().get(KillAura.class)).autoBlockMode.get()).equals(KillAura.AutoBlock.None)
            ? UseAction.BLOCK
            : instance.getUseAction();
    }

    @Redirect(
        method = {"renderFirstPersonItem"},
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/network/AbstractClientPlayerEntity;isUsingItem()Z",
            ordinal = 1
        )
    )
    private boolean hookIsUseItem(AbstractClientPlayerEntity instance) {
        Item item = instance.getMainHandStack().getItem();
        return item instanceof SwordItem
            && KillAura.wasTargeting
            && !((KillAura.AutoBlock) ((KillAura) Modules.get().get(KillAura.class)).autoBlockMode.get()).equals(KillAura.AutoBlock.None)
            ? true
            : instance.isUsingItem();
    }

    @Redirect(
        method = {"renderFirstPersonItem"},
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/network/AbstractClientPlayerEntity;getActiveHand()Lnet/minecraft/util/Hand;",
            ordinal = 1
        )
    )
    private Hand hookActiveHand(AbstractClientPlayerEntity instance) {
        Item item = instance.getMainHandStack().getItem();
        return item instanceof SwordItem
            && KillAura.wasTargeting
            && !((KillAura.AutoBlock) ((KillAura) Modules.get().get(KillAura.class)).autoBlockMode.get()).equals(KillAura.AutoBlock.None)
            ? Hand.MAIN_HAND
            : instance.getActiveHand();
    }

    @Redirect(
        method = {"renderFirstPersonItem"},
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/network/AbstractClientPlayerEntity;getItemUseTimeLeft()I",
            ordinal = 1
        )
    )
    private int hookItemUseItem(AbstractClientPlayerEntity instance) {
        Item item = instance.getMainHandStack().getItem();
        return item instanceof SwordItem
            && KillAura.wasTargeting
            && !((KillAura.AutoBlock) ((KillAura) Modules.get().get(KillAura.class)).autoBlockMode.get()).equals(KillAura.AutoBlock.None)
            ? 7200
            : instance.getItemUseTimeLeft();
    }

    @Inject(
        method = {"renderFirstPersonItem"},
        slice = {@Slice(
            from = @At(
                value = "INVOKE",
                target = "Lnet/minecraft/item/ItemStack;getUseAction()Lnet/minecraft/util/UseAction;"
            )
        )},
        at = {@At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/render/item/HeldItemRenderer;applyEquipOffset(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/util/Arm;F)V",
            ordinal = 2,
            shift = Shift.AFTER
        )}
    )
    private void transformLegacyBlockAnimations(
        AbstractClientPlayerEntity player,
        float tickDelta,
        float pitch,
        Hand hand,
        float swingProgress,
        ItemStack item,
        float equipProgress,
        MatrixStack matrices,
        VertexConsumerProvider vertexConsumers,
        int light,
        CallbackInfo ci
    ) {
        boolean shouldAnimate = ((SwingAnimation) Modules.get().get(SwingAnimation.class)).isActive()
            || KillAura.wasTargeting && !((KillAura.AutoBlock) ((KillAura) Modules.get().get(KillAura.class)).autoBlockMode.get()).equals(KillAura.AutoBlock.None);
        if (shouldAnimate && item.getItem() instanceof SwordItem) {
            Arm arm = hand == Hand.MAIN_HAND ? player.getMainArm() : player.getMainArm().getOpposite();
            switch ((SwingAnimation.AnimationMode) ((SwingAnimation) Modules.get().get(SwingAnimation.class)).animationMode.get()) {
                case PULL:
                    ((SwingAnimation) Modules.get().get(SwingAnimation.class)).pullDown(matrices, arm, equipProgress, swingProgress);
                    break;
                case NORMAL:
                    ((SwingAnimation) Modules.get().get(SwingAnimation.class)).transform(matrices, arm, equipProgress, swingProgress);
                    break;
                case CUSTOM:
                    ((SwingAnimation) Modules.get().get(SwingAnimation.class)).custom(matrices, arm, equipProgress, swingProgress);
            }
        }

        if (((SwingAnimation) Modules.get().get(SwingAnimation.class)).isActive() && item.getItem().getComponents().get(DataComponentTypes.FOOD) != null) {
            Arm arm = hand == Hand.MAIN_HAND ? player.getMainArm() : player.getMainArm().getOpposite();
            ((SwingAnimation) Modules.get().get(SwingAnimation.class)).pullDown(matrices, arm, equipProgress, swingProgress);
            System.out.println(swingProgress);
        }
    }
}
