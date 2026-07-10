package dev.abstr3act.addon.modules.Amrita.nofall;

import meteordevelopment.meteorclient.events.packets.PacketEvent.Receive;
import meteordevelopment.meteorclient.events.packets.PacketEvent.Send;
import meteordevelopment.meteorclient.events.packets.PacketEvent.Sent;
import meteordevelopment.meteorclient.events.world.TickEvent.Post;
import meteordevelopment.meteorclient.events.world.TickEvent.Pre;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.client.MinecraftClient;

public class NoFallMode {
    protected final MinecraftClient mc;
    protected final NoFallPlus settings = (NoFallPlus) Modules.get().get(NoFallPlus.class);
    private final NoFallModes type;

    public NoFallMode(NoFallModes type) {
        this.mc = MinecraftClient.getInstance();
        this.type = type;
    }

    public void onSendPacket(Send event) {
    }

    public void onSentPacket(Sent event) {
    }

    public void onReceivePacket(Receive event) {
    }

    public void onTickEventPre(Pre event) {
    }

    public void onTickEventPost(Post event) {
    }

    public void onActivate() {
    }

    public void onDeactivate() {
    }
}
