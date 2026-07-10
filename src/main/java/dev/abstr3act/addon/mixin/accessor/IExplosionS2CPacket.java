package dev.abstr3act.addon.mixin.accessor;

import net.minecraft.network.packet.s2c.play.ExplosionS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin({ExplosionS2CPacket.class})
public interface IExplosionS2CPacket {
    @Mutable
    @Accessor("playerVelocityX")
    void setVelocityX(float var1);

    @Mutable
    @Accessor("playerVelocityY")
    void setVelocityY(float var1);

    @Mutable
    @Accessor("playerVelocityZ")
    void setVelocityZ(float var1);

    @Accessor("playerVelocityX")
    float getX();

    @Accessor("playerVelocityY")
    float getY();

    @Accessor("playerVelocityZ")
    float getZ();
}
