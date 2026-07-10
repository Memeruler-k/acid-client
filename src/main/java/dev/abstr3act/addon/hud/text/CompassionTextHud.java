package dev.abstr3act.addon.hud.text;

import dev.abstr3act.addon.Compassion;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;

public class CompassionTextHud {

    public static final HudElementInfo<NewTextHud> INFO = new HudElementInfo<>(
        Compassion.HUD_GROUP, "new-text", "Displays arbitrary text with Starscript.", CompassionTextHud::create
    );
    public static final HudElementInfo<NewTextHud>.Preset PING = addPreset("Ping", "Ping: #1{ping}");
    public static final HudElementInfo<NewTextHud>.Preset SPEED = addPreset("Speed", "Speed: #1{round(player.speed, 1)}", 0);
    public static final HudElementInfo<NewTextHud>.Preset GAME_MODE = addPreset("Game mode", "Game mode: #1{player.gamemode}", 0);
    public static final HudElementInfo<NewTextHud>.Preset DURABILITY = addPreset("Durability", "Durability: #1{player.hand_or_offhand.durability}");
    public static final HudElementInfo<NewTextHud>.Preset POSITION = addPreset(
        "Position", "Pos: #1{floor(camera.pos.x)}, {floor(camera.pos.y)}, {floor(camera.pos.z)}", 0
    );
    public static final HudElementInfo<NewTextHud>.Preset OPPOSITE_POSITION = addPreset(
        "Opposite Position",
        "{player.opposite_dimension != \"End\" ? player.opposite_dimension + \":\" : \"\"} #1{player.opposite_dimension != \"End\" ? \"\" + floor(camera.opposite_dim_pos.x) + \", \" + floor(camera.opposite_dim_pos.y) + \", \" + floor(camera.opposite_dim_pos.z) : \"\"}",
        0
    );
    public static final HudElementInfo<NewTextHud>.Preset LOOKING_AT = addPreset("Looking at", "Looking at: #1{crosshair_target.value}", 0);
    public static final HudElementInfo<NewTextHud>.Preset LOOKING_AT_WITH_POSITION = addPreset(
        "Looking at with position",
        "Looking at: #1{crosshair_target.value} {crosshair_target.type != \"miss\" ? \"(\" + \"\" + floor(crosshair_target.value.pos.x) + \", \" + floor(crosshair_target.value.pos.y) + \", \" + floor(crosshair_target.value.pos.z) + \")\" : \"\"}",
        0
    );
    public static final HudElementInfo<NewTextHud>.Preset BREAKING_PROGRESS = addPreset(
        "Breaking progress", "Breaking progress: #1{round(player.breaking_progress * 100)}%", 0
    );
    public static final HudElementInfo<NewTextHud>.Preset SERVER = addPreset("Server", "Server: #1{server}");
    public static final HudElementInfo<NewTextHud>.Preset BIOME = addPreset("Biome", "Biome: #1{player.biome}", 0);
    public static final HudElementInfo<NewTextHud>.Preset WORLD_TIME = addPreset("World time", "Time: #1{server.time}");
    public static final HudElementInfo<NewTextHud>.Preset REAL_TIME = addPreset("Real time", "Time: #1{time}");
    public static final HudElementInfo<NewTextHud>.Preset ROTATION = addPreset(
        "Rotation", "{camera.direction} #1({round(camera.yaw, 1)}, {round(camera.pitch, 1)})", 0
    );
    public static final HudElementInfo<NewTextHud>.Preset MODULE_ENABLED = addPreset(
        "Module enabled", "Kill Aura: {meteor.is_module_active(\"kill-aura\") ? #2 \"ON\" : #3 \"OFF\"}", 0
    );
    public static final HudElementInfo<NewTextHud>.Preset MODULE_ENABLED_WITH_INFO = addPreset(
        "Module enabled with info", "Kill Aura: {meteor.is_module_active(\"kill-aura\") ? #2 \"ON\" : #3 \"OFF\"} #1{meteor.get_module_info(\"kill-aura\")}", 0
    );
    public static final HudElementInfo<NewTextHud>.Preset WATERMARK = addPreset("Watermark", "{meteor.name} #1{meteor.version}");
    public static final HudElementInfo<NewTextHud>.Preset BARITONE = addPreset("Baritone", "Baritone: #1{baritone.process_name}");

    private static NewTextHud create() {
        return new NewTextHud(INFO);
    }

    public static final HudElementInfo<NewTextHud>.Preset FPS = addPreset("FPS", "FPS: #1{fps}", 0);

    private static HudElementInfo<NewTextHud>.Preset addPreset(String title, String text, int updateDelay) {
        return INFO.addPreset(title, textHud -> {
            if (text != null) {
                textHud.text.set(text);
            }

            if (updateDelay != -1) {
                textHud.updateDelay.set(updateDelay);
            }
        });
    }
    public static final HudElementInfo<NewTextHud>.Preset TPS = addPreset("TPS", "TPS: #1{round(server.tps, 1)}");

    private static HudElementInfo<NewTextHud>.Preset addPreset(String title, String text) {
        return addPreset(title, text, -1);
    }
}
