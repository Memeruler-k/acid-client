package dev.abstr3act.addon.hud;

import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.utils.render.color.Color;

public class Username extends HudElement {
    public Username() {
        super(INFO);
    }    public static final HudElementInfo<Username> INFO = new HudElementInfo(
        dev.abstr3act.addon.Compassion.HUD_GROUP, "Username", "Shows your username, good for cracked servers", Username::new
    );

    public void render(HudRenderer renderer) {
        String username = MeteorClient.mc.getSession().getUsername();
        this.setSize(renderer.textWidth("Welcome User: " + username + "!", true), renderer.textHeight(true));
        renderer.text("Welcome User: " + username + "!", this.x, this.y, Color.WHITE, true);
    }


}
