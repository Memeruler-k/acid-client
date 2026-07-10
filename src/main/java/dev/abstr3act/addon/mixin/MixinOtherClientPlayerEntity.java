package dev.abstr3act.addon.mixin;

import com.mojang.authlib.GameProfile;
import dev.abstr3act.addon.interfaces.IOtherClientPlayerEntity;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin({OtherClientPlayerEntity.class})
public class MixinOtherClientPlayerEntity extends AbstractClientPlayerEntity implements IOtherClientPlayerEntity {
    @Unique
    private double backUpX;
    @Unique
    private double backUpY;
    @Unique
    private double backUpZ;

    public MixinOtherClientPlayerEntity(ClientWorld world, GameProfile profile) {
        super(world, profile);
    }

    @Override
    public void releaseResolver() {
        if (this.backUpY != -999.0) {
            this.setPosition(this.backUpX, this.backUpY, this.backUpZ);
            this.backUpY = -999.0;
        }
    }
}
