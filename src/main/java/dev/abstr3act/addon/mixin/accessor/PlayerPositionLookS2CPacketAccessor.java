package dev.abstr3act.addon.mixin.accessor;

import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.network.packet.s2c.play.PositionFlag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Set;

@Mixin({PlayerPositionLookS2CPacket.class})
public interface PlayerPositionLookS2CPacketAccessor {
    @Mutable
    @Accessor("yaw")
    void setYaw(float var1);

    @Mutable
    @Accessor("pitch")
    void setPitch(float var1);

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
    @Accessor("flags")
    void setFlags(Set<PositionFlag> var1);

    @Mutable
    @Accessor("teleportId")
    void setTeleportId(int var1);
}
