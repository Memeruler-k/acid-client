package dev.abstr3act.addon.mixin.accessor;

import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin({ClientPlayerEntity.class})
public interface IClientPlayerEntity {
    @Accessor("lastYaw")
    float getLastYaw();

    @Accessor("lastYaw")
    void setLastYaw(float var1);

    @Accessor("lastPitch")
    float getLastPitch();

    @Accessor("lastPitch")
    void setLastPitch(float var1);

    @Accessor("mountJumpStrength")
    void setMountJumpStrength(float var1);
}
