package dev.abstr3act.addon.hud;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.abstr3act.addon.hud.storage.TextureStorage;
import dev.abstr3act.addon.utils.render.Render2DEngine;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.renderer.text.TextRenderer;
import meteordevelopment.meteorclient.settings.DoubleSetting.Builder;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.misc.NameProtect;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.util.math.MatrixStack;
import org.lwjgl.opengl.GL13;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class ClientBoard extends HudElement {
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
    public final Setting<String> prefix = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.StringSetting.Builder) ((meteordevelopment.meteorclient.settings.StringSetting.Builder) ((meteordevelopment.meteorclient.settings.StringSetting.Builder) new meteordevelopment.meteorclient.settings.StringSetting.Builder()
                .name("Prefix"))
                .description("."))
                .defaultValue("C"))
                .build()
        );
    public final Setting<String> str = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.StringSetting.Builder) ((meteordevelopment.meteorclient.settings.StringSetting.Builder) ((meteordevelopment.meteorclient.settings.StringSetting.Builder) new meteordevelopment.meteorclient.settings.StringSetting.Builder()
                .name("String"))
                .description("."))
                .defaultValue("ompassion "))
                .build()
        );
    public final Setting<String> sux = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.StringSetting.Builder) ((meteordevelopment.meteorclient.settings.StringSetting.Builder) ((meteordevelopment.meteorclient.settings.StringSetting.Builder) new meteordevelopment.meteorclient.settings.StringSetting.Builder()
                .name("Suffix"))
                .description("."))
                .defaultValue("V7 "))
                .build()
        );
    private final Setting<Double> scale = this.sgGeneral
        .add(((Builder) ((Builder) new Builder().name("scale")).description("The scale.")).defaultValue(2.0).min(1.0).sliderRange(1.0, 5.0).build());
    private final Setting<Double> Tscale = this.sgGeneral
        .add(((Builder) ((Builder) new Builder().name("Tscale")).description("The scale.")).defaultValue(2.0).min(1.0).sliderRange(1.0, 5.0).build());
    private final Setting<Double> rainbowSpeed = this.sgGeneral
        .add(
            ((Builder) ((Builder) new Builder().name("rainbow-speed")).description("Rainbow speed of rainbow color mode."))
                .defaultValue(0.05)
                .sliderMin(0.01)
                .sliderMax(0.2)
                .decimalPlaces(4)
                .build()
        );
    private final Setting<Double> space = this.sgGeneral
        .add(((Builder) ((Builder) new Builder().name("space")).description("")).defaultValue(0.0).sliderMin(-1000.0).sliderMax(1000.0).build());
    private final Setting<Double> spaceX = this.sgGeneral
        .add(((Builder) ((Builder) new Builder().name("space-x")).description("")).defaultValue(0.0).sliderMin(-1000.0).sliderMax(1000.0).build());
    private final Setting<Double> sizeX = this.sgGeneral
        .add(((Builder) ((Builder) new Builder().name("sizeX")).description("")).defaultValue(0.0).sliderMin(-1000.0).sliderMax(1000.0).build());
    private final Setting<Double> sizeY = this.sgGeneral
        .add(((Builder) ((Builder) new Builder().name("sizeY")).description("")).defaultValue(0.0).sliderMin(-1000.0).sliderMax(1000.0).build());
    private final Setting<Double> TsizeX = this.sgGeneral
        .add(((Builder) ((Builder) new Builder().name("TsizeX")).description("")).defaultValue(0.0).sliderMin(-1000.0).sliderMax(1000.0).build());
    private final Setting<Double> TsizeY = this.sgGeneral
        .add(((Builder) ((Builder) new Builder().name("TsizeY")).description("")).defaultValue(0.0).sliderMin(-1000.0).sliderMax(1000.0).build());
    private final Setting<Double> tiY = this.sgGeneral
        .add(((Builder) ((Builder) new Builder().name("TiY")).description("")).defaultValue(0.0).sliderMin(-1000.0).sliderMax(1000.0).build());
    private final Setting<Double> iX = this.sgGeneral
        .add(((Builder) ((Builder) new Builder().name("iX")).description("")).defaultValue(0.0).sliderMin(-1000.0).sliderMax(1000.0).build());
    private final Setting<Double> iY = this.sgGeneral
        .add(((Builder) ((Builder) new Builder().name("iY")).description("")).defaultValue(0.0).sliderMin(-1000.0).sliderMax(1000.0).build());
    private final Setting<Double> tX = this.sgGeneral
        .add(((Builder) ((Builder) new Builder().name("tX")).description("")).defaultValue(0.0).sliderMin(-1000.0).sliderMax(1000.0).build());
    private final Setting<Double> tY = this.sgGeneral
        .add(((Builder) ((Builder) new Builder().name("tY")).description("")).defaultValue(0.0).sliderMin(-1000.0).sliderMax(1000.0).build());
    private final Setting<Double> iconScale = this.sgGeneral
        .add(((Builder) ((Builder) new Builder().name("iS")).description("")).defaultValue(1.0).sliderMin(0.0).sliderMax(2.0).build());
    private final Setting<SettingColor> bg1 = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                .name("bg-color-1"))
                .description("Color for flat color mode."))
                .defaultValue(new SettingColor(225, 25, 25))
                .build()
        );
    private final Setting<SettingColor> bg2 = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                .name("bg-color-2"))
                .description("Color for flat color mode."))
                .defaultValue(new SettingColor(225, 25, 25))
                .build()
        );
    private final Setting<SettingColor> dp1 = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                .name("double-color-1"))
                .description("Color for flat color mode."))
                .defaultValue(new SettingColor(225, 25, 25))
                .build()
        );
    private final Setting<SettingColor> dp2 = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                .name("double-color-2"))
                .description("Color for flat color mode."))
                .defaultValue(new SettingColor(225, 25, 25))
                .build()
        );
    private final Color rainbow = new Color(255, 255, 255);    public static final HudElementInfo<ClientBoard> INFO = new HudElementInfo(
        dev.abstr3act.addon.Compassion.HUD_GROUP, "ClientBoard", "ClientBoard", ClientBoard::new
    );
    String greeting = "";
    String time = "";
    private double rainbowHue2;
    private double rainbowHue1;
    public ClientBoard() {
        super(INFO);
    }

    public static String getGreeting(String name) {
        LocalTime now = LocalTime.now();
        int hour = now.getHour();
        if (hour >= 5 && hour < 12) {
            return String.format("Good morning %s", name) + "~";
        } else {
            return hour >= 12 && hour < 18 ? String.format("Good afternoon %s", name) + "~" : String.format("Good night %s", name) + "~";
        }
    }

    public static String getCurrentServerIp() {
        MinecraftClient client = MinecraftClient.getInstance();
        ServerInfo serverInfo = client.getCurrentServerEntry();
        return serverInfo != null ? serverInfo.address : "Local";
    }

    public static int getPing() {
        if (MeteorClient.mc.getNetworkHandler() != null && MeteorClient.mc.player != null) {
            PlayerListEntry playerListEntry = MeteorClient.mc.getNetworkHandler().getPlayerListEntry(MeteorClient.mc.player.getUuid());
            return playerListEntry == null ? 0 : playerListEntry.getLatency();
        } else {
            return 0;
        }
    }

    public void tick(HudRenderer renderer) {
        if (MeteorClient.mc.player != null) {
            this.greeting = getGreeting(
                ((NameProtect) Modules.get().get(NameProtect.class)).isActive()
                    ? ((NameProtect) Modules.get().get(NameProtect.class)).getName(MeteorClient.mc.player.getName().getString())
                    : MeteorClient.mc.player.getName().getString()
            );
            LocalTime time = LocalTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
            this.time = time.format(formatter);
        }
    }

    public void drawIcon(MatrixStack stack, int x, int y) {
        int y1 = (int) (y + this.tiY.get());
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
        GL13.glEnable(32925);
        RenderSystem.setShaderTexture(0, TextureStorage.time);
        Render2DEngine.renderTextureX(
            stack, x + this.iX.get(), y1, 16.0 * this.iconScale.get(), 16.0 * this.iconScale.get(), 0.0F, 0.0F, 256.0, 256.0, 256.0, 256.0
        );
        y1 = (int) (y1 + this.iY.get());
        RenderSystem.setShaderTexture(0, TextureStorage.fps);
        Render2DEngine.renderTextureX(
            stack, x + this.iX.get(), y1, 16.0 * this.iconScale.get(), 16.0 * this.iconScale.get(), 0.0F, 0.0F, 256.0, 256.0, 256.0, 256.0
        );
        y1 = (int) (y1 + this.iY.get());
        RenderSystem.setShaderTexture(0, TextureStorage.serverInfo);
        Render2DEngine.renderTextureX(
            stack, x + this.iX.get(), y1, 16.0 * this.iconScale.get(), 16.0 * this.iconScale.get(), 0.0F, 0.0F, 256.0, 256.0, 256.0, 256.0
        );
        y1 = (int) (y1 + this.iY.get());
        RenderSystem.setShaderTexture(0, TextureStorage.ping);
        Render2DEngine.renderTextureX(
            stack, x + this.iX.get(), y1, 16.0 * this.iconScale.get(), 16.0 * this.iconScale.get(), 0.0F, 0.0F, 256.0, 256.0, 256.0, 256.0
        );
        GL13.glDisable(32925);
        RenderSystem.disableBlend();
        RenderSystem.disableDepthTest();
    }

    public void render(HudRenderer renderer) {
        String prefix = (String) this.prefix.get();
        String middle = (String) this.str.get();
        String suffix = (String) this.sux.get();
        String total = prefix + middle + suffix;
        Render2DEngine.drawRoundedBlur(
            renderer.drawContext.getMatrices(),
            this.x,
            this.y,
            (this.sizeX.get()).floatValue(),
            (this.sizeY.get()).floatValue(),
            50.0F,
            new java.awt.Color(((SettingColor) this.bg1.get()).getPacked())
        );
        Render2DEngine.drawRoundedBlur(
            renderer.drawContext.getMatrices(),
            this.x,
            this.y,
            (this.TsizeX.get()).floatValue(),
            (this.TsizeY.get()).floatValue(),
            50.0F,
            new java.awt.Color(((SettingColor) this.bg2.get()).getPacked())
        );
        this.rainbowHue1 = this.rainbowHue1 + this.rainbowSpeed.get() * renderer.delta;
        if (this.rainbowHue1 > 1.0) {
            this.rainbowHue1--;
        } else if (this.rainbowHue1 < 0.0) {
            this.rainbowHue1++;
        }

        double progress = Math.sin((this.rainbowHue1 + 1.0) * Math.PI * 2.0);
        progress = (progress + 1.0) / 2.0;
        Color redColor = (Color) this.dp1.get();
        Color whiteColor = (Color) this.dp2.get();
        int red = (int) (redColor.r * (1.0 - progress) + whiteColor.r * progress);
        int green = (int) (redColor.g * (1.0 - progress) + whiteColor.g * progress);
        int blue = (int) (redColor.b * (1.0 - progress) + whiteColor.b * progress);
        this.rainbow.set(red, green, blue, 255);
        int x = this.getX();
        int y = this.getY();
        this.drawIcon(renderer.drawContext.getMatrices(), x, y);
        renderer.post(
            () -> {
                LocalDateTime now = LocalDateTime.now().withNano(0);
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                String formattedTime = now.format(formatter);
                int x1 = this.getX();
                int y1 = this.getY();
                TextRenderer.get().begin(this.Tscale.get(), false, true);
                TextRenderer.get().render(prefix, x1 + this.spaceX.get() + this.tX.get(), y1 + this.tY.get(), this.rainbow, true);
                TextRenderer.get()
                    .render(
                        middle,
                        x1 + this.spaceX.get() + TextRenderer.get().getWidth(prefix) + this.tX.get(),
                        y1 + this.tY.get(),
                        new Color(255, 255, 255),
                        true
                    );
                TextRenderer.get()
                    .render(
                        suffix,
                        x1 + this.spaceX.get() + TextRenderer.get().getWidth(prefix + middle) + this.tX.get(),
                        y1 + this.tY.get(),
                        Color.WHITE,
                        true
                    );
                TextRenderer.get().end(null);
                TextRenderer.get().begin(this.scale.get(), false, true);
                y1 = (int) (y1 + this.space.get());
                TextRenderer.get().render(formattedTime, x1 + this.spaceX.get(), y1, new Color(255, 255, 255), true);
                y1 = (int) (y1 + this.space.get());
                TextRenderer.get().render(MeteorClient.mc.getCurrentFps() + " FPS", x1 + this.spaceX.get(), y1, new Color(255, 255, 255), true);
                y1 = (int) (y1 + this.space.get());
                TextRenderer.get().render(getCurrentServerIp(), x1 + this.spaceX.get(), y1, new Color(255, 255, 255), true);
                y1 = (int) (y1 + this.space.get());
                TextRenderer.get().render("Ping: " + getPing() + "ms", x1 + this.spaceX.get(), y1, new Color(255, 255, 255), true);
                this.setSize(TextRenderer.get().getWidth(total), 10.0 * this.scale.get());
                TextRenderer.get().end(null);
            }
        );
    }


}
