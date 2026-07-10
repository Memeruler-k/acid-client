package dev.abstr3act.addon.hud;

import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.systems.hud.Hud;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import net.minecraft.client.gui.DrawContext;

public abstract class NewHudElement extends HudElement {
    public static NewHudElement INSTANCE;
    private final Hud hud = Hud.get();

    public NewHudElement(HudElementInfo<?> info) {
        super(info);
        INSTANCE = this;
    }

    public int getNewX() {
        int scale = switch (MeteorClient.mc.options.getGuiScale().getValue()) {
            case 1 -> 1;
            case 2 -> 2;
            default -> 3;
        };
        return this.x / scale;
    }

    public int getNewY() {
        int scale = switch (MeteorClient.mc.options.getGuiScale().getValue()) {
            case 1 -> 1;
            case 2 -> 2;
            default -> 3;
        };
        return this.y / scale;
    }

    public void onRender2D(DrawContext context) {
    }
}
