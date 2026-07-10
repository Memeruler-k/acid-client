package dev.abstr3act.addon.mixin;

import dev.abstr3act.addon.events.EventFixVelocity;
import dev.abstr3act.addon.mixin.accessor.IEntity;
import dev.abstr3act.addon.module.BaseModule;
import dev.abstr3act.addon.utils.RotationUtil;
import meteordevelopment.meteorclient.MeteorClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({Entity.class})
public abstract class MixinEntity implements IEntity {
    @Shadow
    private Box boundingBox;

    @Unique
    private static Vec3d movementInputToVelocityC(Vec3d movementInput, float speed, float yaw) {
        double d = movementInput.lengthSquared();
        if (d < 1.0E-7) {
            return Vec3d.ZERO;
        } else {
            Vec3d vec3d = (d > 1.0 ? movementInput.normalize() : movementInput).multiply(speed);
            float f = MathHelper.sin(yaw * (float) (Math.PI / 180.0));
            float g = MathHelper.cos(yaw * (float) (Math.PI / 180.0));
            return new Vec3d(vec3d.x * g - vec3d.z * f, vec3d.y, vec3d.z * g + vec3d.x * f);
        }
    }

    @Shadow
    protected abstract BlockPos getVelocityAffectingPos();

    @Inject(
        method = {"updateVelocity"},
        at = {@At("HEAD")},
        cancellable = true
    )
    public void updateVelocityHook(float speed, Vec3d movementInput, CallbackInfo ci) {
        if (!BaseModule.fullNullCheck()) {
            if (((Entity) (Object) this) == MeteorClient.mc.player) {
                ci.cancel();
                EventFixVelocity event = new EventFixVelocity(
                    movementInput, speed, MeteorClient.mc.player.getYaw(), movementInputToVelocityC(movementInput, speed, MeteorClient.mc.player.getYaw())
                );
                MeteorClient.EVENT_BUS.post(event);
                MeteorClient.mc.player.setVelocity(MeteorClient.mc.player.getVelocity().add(event.getVelocity()));
            }
        }
    }

    @Inject(
        method = {"changeLookDirection"},
        at = {@At("HEAD")}
    )
    public void baseRotationHook(double xDelta, double yDelta, CallbackInfo ci) {
        if (((Entity) (Object) this) instanceof ClientPlayerEntity) {
            double pitchDelta = yDelta * 0.15;
            double yawDelta = xDelta * 0.15;
            RotationUtil.basePitch = MathHelper.clamp(RotationUtil.basePitch + (float) pitchDelta, -90.0F, 90.0F);
            RotationUtil.baseYaw += (float) yawDelta;
        }
    }
}
