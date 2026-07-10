package dev.abstr3act.addon.modules.Amrita.spider;

import meteordevelopment.meteorclient.events.packets.PacketEvent.Send;
import meteordevelopment.meteorclient.events.packets.PacketEvent.Sent;
import meteordevelopment.meteorclient.events.world.TickEvent.Post;
import meteordevelopment.meteorclient.events.world.TickEvent.Pre;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.client.MinecraftClient;

public class SpiderMode {
    protected final MinecraftClient mc;
    protected final SpiderPlus settings = (SpiderPlus) Modules.get().get(SpiderPlus.class);
    private final SpiderModes type;

    public SpiderMode(SpiderModes type) {
        this.mc = MinecraftClient.getInstance();
        this.type = type;
    }

    public void onSendPacket(Send event) {
    }

    public void onSentPacket(Sent event) {
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
