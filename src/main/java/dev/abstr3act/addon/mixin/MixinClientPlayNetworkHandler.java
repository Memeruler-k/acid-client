package dev.abstr3act.addon.mixin;

import dev.abstr3act.addon.Compassion;
import dev.abstr3act.addon.events.EntityVelocityUpdateEvent;
import dev.abstr3act.addon.modules.Compassion.MoveFix;
import meteordevelopment.meteorclient.MeteorClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientCommonNetworkHandler;
import net.minecraft.client.network.ClientConnectionState;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.NetworkThreadUtils;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket.Full;
import net.minecraft.network.packet.c2s.play.TeleportConfirmC2SPacket;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.network.packet.s2c.play.PositionFlag;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({ClientPlayNetworkHandler.class})
public abstract class MixinClientPlayNetworkHandler extends ClientCommonNetworkHandler {
    @Shadow
    private ClientWorld world;

    protected MixinClientPlayNetworkHandler(MinecraftClient client, ClientConnection connection, ClientConnectionState connectionState) {
        super(client, connection, connectionState);
    }

    @Inject(
        method = {"onEntityVelocityUpdate"},
        at = {@At("HEAD")},
        cancellable = true
    )
    public void test(EntityVelocityUpdateS2CPacket packet, CallbackInfo ci) {
        NetworkThreadUtils.forceMainThread(packet, (ClientPlayNetworkHandler) (Object) this, this.client);
        Entity entity = this.world.getEntityById(packet.getEntityId());
        if (entity != null && entity == MinecraftClient.getInstance().player) {
            EntityVelocityUpdateEvent event = new EntityVelocityUpdateEvent();
            MeteorClient.EVENT_BUS.post(event);
            if (event.isCancelled()) {
                entity.setVelocityClient(packet.getVelocityX() / 8000.0, packet.getVelocityY() / 8000.0, packet.getVelocityZ() / 8000.0);
                ci.cancel();
            }
        }
    }

    @Inject(
        method = {"onPlayerPositionLook"},
        at = {@At("HEAD")},
        cancellable = true
    )
    public void onPlayerPositionLook(PlayerPositionLookS2CPacket packet, CallbackInfo ci) {
        if (MoveFix.INSTANCE.isActive()) {
            ci.cancel();
            NetworkThreadUtils.forceMainThread(packet, (ClientPlayNetworkHandler) (Object) this, this.client);
            PlayerEntity playerEntity = this.client.player;
            Vec3d vec3d = playerEntity.getVelocity();
            boolean bl = packet.getFlags().contains(PositionFlag.X);
            boolean bl2 = packet.getFlags().contains(PositionFlag.Y);
            boolean bl3 = packet.getFlags().contains(PositionFlag.Z);
            double d;
            double e;
            if (bl) {
                d = vec3d.getX();
                e = playerEntity.getX() + packet.getX();
                playerEntity.lastRenderX = playerEntity.lastRenderX + packet.getX();
                playerEntity.prevX = playerEntity.prevX + packet.getX();
            } else {
                d = 0.0;
                e = packet.getX();
                playerEntity.lastRenderX = e;
                playerEntity.prevX = e;
            }

            double f;
            double g;
            if (bl2) {
                f = vec3d.getY();
                g = playerEntity.getY() + packet.getY();
                playerEntity.lastRenderY = playerEntity.lastRenderY + packet.getY();
                playerEntity.prevY = playerEntity.prevY + packet.getY();
            } else {
                f = 0.0;
                g = packet.getY();
                playerEntity.lastRenderY = g;
                playerEntity.prevY = g;
            }

            double h;
            double i;
            if (bl3) {
                h = vec3d.getZ();
                i = playerEntity.getZ() + packet.getZ();
                playerEntity.lastRenderZ = playerEntity.lastRenderZ + packet.getZ();
                playerEntity.prevZ = playerEntity.prevZ + packet.getZ();
            } else {
                h = 0.0;
                i = packet.getZ();
                playerEntity.lastRenderZ = i;
                playerEntity.prevZ = i;
            }

            playerEntity.setPosition(e, g, i);
            playerEntity.setVelocity(d, f, h);
            if (MoveFix.INSTANCE.rotate.get()) {
                float j = packet.getYaw();
                float k = packet.getPitch();
                if (packet.getFlags().contains(PositionFlag.X_ROT)) {
                    playerEntity.setPitch(playerEntity.getPitch() + k);
                    playerEntity.prevPitch += k;
                } else {
                    playerEntity.setPitch(k);
                    playerEntity.prevPitch = k;
                }

                if (packet.getFlags().contains(PositionFlag.Y_ROT)) {
                    playerEntity.setYaw(playerEntity.getYaw() + j);
                    playerEntity.prevYaw += j;
                } else {
                    playerEntity.setYaw(j);
                    playerEntity.prevYaw = j;
                }

                this.connection.send(new TeleportConfirmC2SPacket(packet.getTeleportId()));
                this.connection.send(new Full(playerEntity.getX(), playerEntity.getY(), playerEntity.getZ(), playerEntity.getYaw(), playerEntity.getPitch(), false));
            } else if (MoveFix.INSTANCE.applyYaw.get()) {
                float jx = packet.getYaw();
                float kx = packet.getPitch();
                if (packet.getFlags().contains(PositionFlag.X_ROT)) {
                    kx += Compassion.ROTATION.lastYaw;
                }

                if (packet.getFlags().contains(PositionFlag.Y_ROT)) {
                    jx += Compassion.ROTATION.lastPitch;
                }

                this.connection.send(new TeleportConfirmC2SPacket(packet.getTeleportId()));
                this.connection.send(new Full(playerEntity.getX(), playerEntity.getY(), playerEntity.getZ(), jx, kx, false));
            } else {
                this.connection.send(new TeleportConfirmC2SPacket(packet.getTeleportId()));
                this.connection
                    .send(
                        new Full(playerEntity.getX(), playerEntity.getY(), playerEntity.getZ(), Compassion.ROTATION.rotationYaw, Compassion.ROTATION.rotationPitch, false)
                    );
            }
        }
    }
}
