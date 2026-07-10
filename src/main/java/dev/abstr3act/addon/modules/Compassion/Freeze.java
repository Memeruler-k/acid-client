package dev.abstr3act.addon.modules.Compassion;

import dev.abstr3act.addon.Compassion;
import dev.abstr3act.addon.module.CompassionModule;
import meteordevelopment.meteorclient.events.entity.EntityRemovedEvent;
import meteordevelopment.meteorclient.events.entity.player.InteractBlockEvent;
import meteordevelopment.meteorclient.events.game.GameLeftEvent;
import meteordevelopment.meteorclient.events.packets.PacketEvent.Send;
import meteordevelopment.meteorclient.events.world.TickEvent.Pre;
import meteordevelopment.meteorclient.settings.BoolSetting.Builder;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket.LookAndOnGround;
import net.minecraft.util.math.Vec3d;

public class Freeze extends CompassionModule {
    private final SettingGroup FSettings = this.settings.getDefaultGroup();
    private final Setting<Boolean> FreezeLook = this.FSettings
        .add(((Builder) ((Builder) ((Builder) new Builder().name("Freeze look")).description("Freezes your pitch and yaw.")).defaultValue(false)).build());
    private final Setting<Boolean> Packet = this.FSettings
        .add(((Builder) ((Builder) ((Builder) new Builder().name("Packet mode")).description("Enable packet mode, better.")).defaultValue(true)).build());
    private final Setting<Boolean> FreezeLookSilent = this.FSettings
        .add(
            ((Builder) ((Builder) ((Builder) ((Builder) new Builder().name("Freeze look silent")).description("Freezes your pitch and yaw silent.")).defaultValue(true))
                .visible(() -> this.Packet.get() && this.FreezeLook.get()))
                .build()
        );
    private final Setting<Boolean> FreezeLookPlace = this.FSettings
        .add(
            ((Builder) ((Builder) ((Builder) ((Builder) new Builder().name("Freeze look place support")).description("Unfreez you yaw and pitch on place"))
                .defaultValue(false))
                .visible(this.FreezeLookSilent::get))
                .build()
        );
    private float yaw = 0.0F;
    private float pitch = 0.0F;
    private Vec3d position = Vec3d.ZERO;
    private boolean rotate = false;

    public Freeze() {
        super(Compassion.COMPASSION, "NoC03", "Freezes your position for server.");
    }

    public void onActivate() {
        if (this.mc.player != null) {
            this.yaw = this.mc.player.getYaw();
            this.pitch = this.mc.player.getPitch();
            this.position = this.mc.player.getPos();
        }
    }

    private void setFreezeLook(Send event, PlayerMoveC2SPacket playerMove) {
        if (playerMove.changesLook() && this.FreezeLook.get() && this.FreezeLookSilent.get() && !this.rotate) {
            event.setCancelled(true);
        } else if (this.mc.player != null && playerMove.changesLook() && this.FreezeLook.get() && !this.FreezeLookSilent.get()) {
            event.setCancelled(true);
            this.mc.player.setYaw(this.yaw);
            this.mc.player.setPitch(this.pitch);
        }

        if (this.mc.player != null && playerMove.changesPosition()) {
            this.mc.player.setVelocity(0.0, 0.0, 0.0);
            this.mc.player.setPos(this.position.x, this.position.y, this.position.z);
            event.setCancelled(true);
        }
    }

    @EventHandler
    private void InteractBlockEvent(InteractBlockEvent event) {
        if (this.mc.player != null && this.mc.getNetworkHandler() != null && this.FreezeLookPlace.get()) {
            LookAndOnGround r = new LookAndOnGround(this.mc.player.getYaw(), this.mc.player.getPitch(), this.mc.player.isOnGround());
            this.rotate = true;
            this.mc.getNetworkHandler().sendPacket(r);
            this.rotate = false;
        }
    }

    @EventHandler
    private void onMovePacket(Send event) {
        if (event.packet instanceof PlayerMoveC2SPacket playerMove && this.Packet.get()) {
            this.setFreezeLook(event, playerMove);
        }
    }

    @EventHandler
    private void connectToServerEvent(GameLeftEvent event) {
        this.toggle();
    }

    @EventHandler
    private void onTick(Pre event) {
        if (this.mc.player != null) {
            this.mc.player.setVelocity(0.0, 0.0, 0.0);
            this.mc.player.setPos(this.position.x, this.position.y, this.position.z);
        }
    }

    @EventHandler
    private void remove(EntityRemovedEvent event) {
        if (event.entity == this.mc.player && this.isActive()) {
            this.toggle();
        }
    }

    @EventHandler
    public void onGameJoin(GameLeftEvent event) {
        this.toggle();
    }

    public String getInfoString() {
        return this.Packet.get() ? "Packet" : "Static";
    }
}
