package dev.abstr3act.addon.modules.Seraphim.fly;

import meteordevelopment.meteorclient.events.entity.DamageEvent;
import meteordevelopment.meteorclient.events.entity.player.CanWalkOnFluidEvent;
import meteordevelopment.meteorclient.events.entity.player.PlayerMoveEvent;
import meteordevelopment.meteorclient.events.entity.player.SendMovementPacketsEvent.Pre;
import meteordevelopment.meteorclient.events.packets.PacketEvent.Receive;
import meteordevelopment.meteorclient.events.packets.PacketEvent.Send;
import meteordevelopment.meteorclient.events.packets.PacketEvent.Sent;
import meteordevelopment.meteorclient.events.world.CollisionShapeEvent;
import meteordevelopment.meteorclient.events.world.TickEvent.Post;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.client.MinecraftClient;

public class FlyMode {
    protected final MinecraftClient mc;
    protected final SeraphimFly settings = (SeraphimFly) Modules.get().get(SeraphimFly.class);
    private final FlyModes type;

    public FlyMode(FlyModes type) {
        this.mc = MinecraftClient.getInstance();
        this.type = type;
    }

    public void onSendPacket(Send event) {
    }

    public void onSentPacket(Sent event) {
    }

    public void onRecivePacket(Receive event) {
    }

    public void onPlayerMoveEvent(PlayerMoveEvent event) {
    }

    public void onPlayerMoveSendPre(Pre event) {
    }

    public void onCanWalkOnFluid(CanWalkOnFluidEvent event) {
    }

    public void onCollisionShape(CollisionShapeEvent event) {
    }

    public void onDamage(DamageEvent event) {
    }

    public void onTickEventPre(meteordevelopment.meteorclient.events.world.TickEvent.Pre event) {
    }

    public void onTickEventPost(Post event) {
    }

    public void onActivate() {
    }

    public void onDeactivate() {
    }
}
