package dev.abstr3act.addon.hud;

import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.packets.PacketEvent.Receive;
import meteordevelopment.meteorclient.events.world.TickEvent.Post;
import meteordevelopment.meteorclient.renderer.GL;
import meteordevelopment.meteorclient.renderer.Renderer2D;
import meteordevelopment.meteorclient.settings.DoubleSetting.Builder;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.minecraft.util.Identifier;

public class PopNotifier extends HudElement {
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
    private final Setting<Double> girlScale = this.sgGeneral
        .add(
            ((Builder) ((Builder) new Builder().name("Scale")).description("Modify the size of the Catgirl."))
                .defaultValue(1.0)
                .min(0.0)
                .sliderRange(0.0, 10.0)
                .build()
        );
    private final Setting<image> pop = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) new meteordevelopment.meteorclient.settings.EnumSetting.Builder()
                .name("Self Pop Image"))
                .description("The module of bot"))
                .defaultValue(image.maya))
                .build()
        );
    private final Setting<image> other_pop = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) new meteordevelopment.meteorclient.settings.EnumSetting.Builder()
                .name("Other pops Image"))
                .description("The module of bot"))
                .defaultValue(image.luna))
                .build()
        );
    private final Setting<Boolean> isBlink = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("Blink"))
                .description("The module of bot"))
                .defaultValue(false))
                .build()
        );
    private final Setting<image> blinkImage = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) new meteordevelopment.meteorclient.settings.EnumSetting.Builder()
                .name("Blink Image"))
                .description("The module of bot"))
                .defaultValue(image.luna))
                .visible(this.isBlink::get))
                .build()
        );
    private final Setting<Double> blinkDelay = this.sgGeneral
        .add(
            ((Builder) ((Builder) ((Builder) new Builder().name("Blink Delay")).description("Blink delay"))
                .defaultValue(0.5)
                .min(0.01)
                .sliderRange(0.0, 10.0)
                .visible(this.isBlink::get))
                .build()
        );
    private final Setting<Double> blinkWaitDelay = this.sgGeneral
        .add(
            ((Builder) ((Builder) ((Builder) new Builder().name("Blink Wait Delay")).description("Blink Wait delay"))
                .defaultValue(0.5)
                .min(0.01)
                .sliderRange(0.0, 10.0)
                .visible(this.isBlink::get))
                .build()
        );
    private final Setting<image> normal = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) new meteordevelopment.meteorclient.settings.EnumSetting.Builder()
                .name("Normal Image"))
                .description("The module of bot"))
                .defaultValue(image.ayu))
                .build()
        );
    private final Setting<Double> delay = this.sgGeneral
        .add(((Builder) ((Builder) new Builder().name("Display Delay")).description("Display delay")).defaultValue(3.0).min(0.0).sliderRange(0.0, 10.0).build());
    private final Setting<SideMode> side = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) new meteordevelopment.meteorclient.settings.EnumSetting.Builder()
                .name("Kill Message Mode"))
                .description("What kind of messages to send."))
                .defaultValue(SideMode.Right))
                .build()
        );
    public boolean isPoppedOther = false;
    int i = 0;
    int t = 0;    public static final HudElementInfo<PopNotifier> INFO = new HudElementInfo(
        dev.abstr3act.addon.Compassion.HUD_GROUP, "popNotifier", "Display images while pop totems", PopNotifier::new
    );
    boolean blinking = false;
    private int timer;
    private Identifier img = Identifier.of("acid", "public/ayu.png");
    private boolean popped = false;
    public PopNotifier() {
        super(INFO);
        MeteorClient.EVENT_BUS.subscribe(this);
    }

    @EventHandler
    public void render(HudRenderer renderer) {
        if (this.popped && !this.blinking) {
            this.img = Identifier.of("acid", "public/" + this.pop.get());
        }

        if (this.isPoppedOther && !this.blinking) {
            this.img = Identifier.of("acid", "public/" + this.other_pop.get());
        }

        this.setSize(347.0 * this.girlScale.get(), 347.0 * this.girlScale.get());
        MatrixStack matrixStack = new MatrixStack();
        GL.bindTexture(this.img);
        Renderer2D.TEXTURE.begin();
        Renderer2D.TEXTURE
            .texQuad(
                this.x + (this.side.get() == SideMode.Left ? this.girlScale.get() * 347.0 : 0.0),
                this.y,
                this.girlScale.get() * (this.side.get() == SideMode.Left ? this.girlScale.get() * -347.0 : 347.0),
                this.girlScale.get() * 347.0,
                new Color(255, 255, 255, 255)
            );
        Renderer2D.TEXTURE.render(matrixStack);
    }

    @EventHandler
    public void onTick(Post event) {
        if (!this.popped && !this.isPoppedOther && this.isBlink.get()) {
            this.i++;
        }

        if (this.i > this.blinkDelay.get() * 20.0 && !this.popped && !this.isPoppedOther) {
            this.img = Identifier.of("acid", "public/" + this.blinkImage.get());
            this.blinking = true;
            this.i = 0;
        }

        if (!this.popped && !this.isPoppedOther && this.isBlink.get() && this.blinking) {
            this.t++;
        }

        if (this.t > this.blinkWaitDelay.get() * 20.0 && !this.popped && !this.isPoppedOther) {
            this.img = Identifier.of("acid", "public/" + this.normal.get());
            this.blinking = false;
            this.t = 0;
        }

        if (this.popped) {
            this.img = Identifier.of("acid", "public/" + this.pop.get());
            if (this.timer >= this.delay.get() * 20.0) {
                this.img = Identifier.of("acid", "public/" + this.normal.get());
                this.popped = false;
                this.timer = 0;
            } else {
                this.timer++;
            }
        }

        if (this.isPoppedOther) {
            this.img = Identifier.of("acid", "public/" + this.other_pop.get());
            if (this.timer >= this.delay.get() * 20.0) {
                this.img = Identifier.of("acid", "public/" + this.normal.get());
                this.isPoppedOther = false;
                this.timer = 0;
            } else {
                this.timer++;
            }
        }
    }

    @EventHandler
    private void onReceivePacket(Receive event) {
        if (event.packet instanceof EntityStatusS2CPacket p) {
            if (p.getStatus() == 35) {
                Entity entity = p.getEntity(MeteorClient.mc.world);
                if (entity instanceof PlayerEntity) {
                    if (!entity.equals(MeteorClient.mc.player)) {
                        this.isPoppedOther = true;
                    }

                    this.popped = true;
                }
            }
        }
    }

    public static enum SideMode {
        Right,
        Left;
    }

    public static enum image {
        aichan("aichan.png"),
        ayu("ayu.png"),
        eto("eto.png"),
        hikari("hikari.png"),
        hikari2("hikari2.png"),
        ilith("ilith.png"),
        insight("insight.png"),
        kanae("kanae.png"),
        kou("kou.png"),
        lagrange("lagrange.png"),
        lethe("lethe.png"),
        luna("luna.png"),
        maya("maya.png"),
        nami("nami.png"),
        saya("saya.png"),
        shirabe("shirabe.png"),
        shirahime("shirahime.png"),
        tairitsu("tairitsu.png"),
        tairitsu2("tairitsu2.png"),
        tairitsu3("tairitsu3.png"),
        vita("vita.png"),
        ant1("a.png"),
        ant2("b.png"),
        ant3("c.png"),
        ant4("d.png"),
        ant5("e.png");

        private final String image;

        private image(String image) {
            this.image = image;
        }

        @Override
        public String toString() {
            return this.image;
        }
    }


}
