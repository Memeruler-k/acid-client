package dev.abstr3act.addon.mixin.accessor;

import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin({PlayerMoveC2SPacket.class})
public interface PlayerMoveC2SPacketAccessor {
    @Mutable
    @Accessor("x")
    void setX(double var1);

    @Mutable
    @Accessor("y")
    void setY(double var1);

    @Mutable
    @Accessor("z")
    void setZ(double var1);

    @Mutable
    @Accessor("yaw")
    void setYaw(float var1);

    @Mutable
    @Accessor("pitch")
    void setPitch(float var1);

    @Mutable
    @Accessor("onGround")
    void setOnGround(boolean var1);
}
