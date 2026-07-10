package dev.abstr3act.addon.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.abstr3act.addon.modules.Seraphim.KillAura;
import dev.abstr3act.addon.modules.Seraphim.SwingAnimation;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({Item.class})
public class MixinItem {
    @Inject(
        method = {"use"},
        at = {@At("HEAD")},
        cancellable = true
    )
    private void hookSwordUse(World world, PlayerEntity user, Hand hand, CallbackInfoReturnable<TypedActionResult<ItemStack>> cir) {
        if (MeteorClient.mc.player.getMainHandStack().getItem() instanceof SwordItem
            && ((SwingAnimation) Modules.get().get(SwingAnimation.class)).isActive()
            && hand == Hand.MAIN_HAND) {
            ItemStack itemStack = user.getStackInHand(hand);
            user.setCurrentHand(hand);
            cir.setReturnValue(TypedActionResult.consume(itemStack));
        }

        if (MeteorClient.mc.player.getMainHandStack().getItem().getComponents().get(DataComponentTypes.FOOD) != null
            && ((SwingAnimation) Modules.get().get(SwingAnimation.class)).isActive()
            && hand == Hand.MAIN_HAND) {
            ItemStack itemStack = user.getStackInHand(hand);
            user.setCurrentHand(hand);
            cir.setReturnValue(TypedActionResult.consume(itemStack));
        }
    }

    @ModifyReturnValue(
        method = {"getUseAction"},
        at = {@At("RETURN")}
    )
    private UseAction hookSwordUseAction(UseAction original) {
        if (KillAura.wasTargeting && !((KillAura.AutoBlock) ((KillAura) Modules.get().get(KillAura.class)).autoBlockMode.get()).equals(KillAura.AutoBlock.None)) {
            return UseAction.BLOCK;
        } else if (MeteorClient.mc.player.getMainHandStack().getItem() instanceof SwordItem && ((SwingAnimation) Modules.get().get(SwingAnimation.class)).isActive()
        ) {
            return UseAction.BLOCK;
        } else {
            return MeteorClient.mc.player.getMainHandStack().getItem().getComponents().get(DataComponentTypes.FOOD) != null
                && ((SwingAnimation) Modules.get().get(SwingAnimation.class)).isActive()
                ? UseAction.EAT
                : original;
        }
    }

    @ModifyReturnValue(
        method = {"getMaxUseTime"},
        at = {@At("RETURN")}
    )
    private int hookMaxUseTime(int original) {
        return MeteorClient.mc.player.getMainHandStack().getItem() instanceof SwordItem && ((SwingAnimation) Modules.get().get(SwingAnimation.class)).isActive()
            ? 72000
            : original;
    }
}
