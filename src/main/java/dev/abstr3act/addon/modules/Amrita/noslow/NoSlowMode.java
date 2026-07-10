package dev.abstr3act.addon.modules.Amrita.noslow;

import dev.abstr3act.addon.events.PlayerUseMultiplierEvent;
import meteordevelopment.meteorclient.events.world.TickEvent.Pre;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.client.MinecraftClient;

public class NoSlowMode {
    protected final MinecraftClient mc;
    protected final NoSlowPlus settings = (NoSlowPlus) Modules.get().get(NoSlowPlus.class);
    private final NoSlowModes type;

    public NoSlowMode(NoSlowModes type) {
        this.mc = MinecraftClient.getInstance();
        this.type = type;
    }

    public void onUse(PlayerUseMultiplierEvent event) {
    }

    public void onTickEventPre(Pre event) {
    }

    public void onActivate() {
    }
}
