package dev.abstr3act.addon.modules.Amrita.killaura;

import meteordevelopment.meteorclient.events.packets.PacketEvent.Send;
import meteordevelopment.meteorclient.events.world.TickEvent.Post;
import meteordevelopment.meteorclient.events.world.TickEvent.Pre;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.client.MinecraftClient;

public class KillAuraPlusMode {
    protected final MinecraftClient mc;
    protected final KillAuraPlus settings = (KillAuraPlus) Modules.get().get(KillAuraPlus.class);
    private final KillAuraPlusModes type;

    public KillAuraPlusMode(KillAuraPlusModes type) {
        this.mc = MinecraftClient.getInstance();
        this.type = type;
    }

    public void onActivate() {
    }

    public void onDeactivate() {
    }

    public void onTickPre(Pre event) {
    }

    public void onTickPost(Post event) {
    }

    public void onSendPacket(Send event) {
    }

    public String getInfoString() {
        return "";
    }
}
