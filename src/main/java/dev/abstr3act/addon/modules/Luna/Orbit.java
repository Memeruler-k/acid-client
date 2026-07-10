package dev.abstr3act.addon.modules.Luna;

import dev.abstr3act.addon.Compassion;
import dev.abstr3act.addon.module.LunaModule;
import meteordevelopment.meteorclient.events.game.GameJoinedEvent;
import meteordevelopment.meteorclient.events.game.GameLeftEvent;
import meteordevelopment.meteorclient.events.packets.PacketEvent.Receive;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent.Pre;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.DoubleSetting.Builder;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.Random;

public class Orbit extends LunaModule {
    Random r = new Random();
    private SettingGroup sgGeneral = this.settings.getDefaultGroup();
    private Vec3d centerVec3d;
    private double orbitAngle;
    private boolean isPopped = false;
    private Setting<Double> orbitRange = this.sgGeneral
        .add(
            ((Builder) ((Builder) new Builder().name("orbit-range")).description("The range of the orbit in blocks"))
                .defaultValue(50.0)
                .sliderMin(1.0)
                .sliderMax(128.0)
                .build()
        );
    private Setting<Boolean> random = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("random-speed"))
                .description("Randomize speed"))
                .defaultValue(false))
                .build()
        );
    private Setting<Double> orbitSpeed = this.sgGeneral
        .add(
            ((Builder) ((Builder) new Builder().name("orbit-speed")).description("The speed of the orbit in blocks per second"))
                .defaultValue(1.0)
                .sliderMin(0.1)
                .sliderMax(50.0)
                .build()
        );
    private Setting<Boolean> onlyPopped = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("only-popped"))
                .description("Only circle strafe while popped totems"))
                .defaultValue(false))
                .build()
        );
    private Setting<Boolean> showCenter = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("show-center"))
                .description("Show the center of the orbit"))
                .defaultValue(false))
                .build()
        );
    public final Setting<ShapeMode> shapeMode = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) new meteordevelopment.meteorclient.settings.EnumSetting.Builder()
                .name("shape-mode"))
                .description("How the shapes are rendered."))
                .defaultValue(ShapeMode.Both))
                .visible(this.showCenter::get))
                .build()
        );
    private final Setting<SettingColor> sideColor = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                .name("side-color"))
                .description("Color of sides"))
                .defaultValue(new SettingColor(255, 255, 255))
                .visible(this.showCenter::get))
                .build()
        );
    private final Setting<SettingColor> lineColor = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                .name("line-color"))
                .description("Color of lines"))
                .defaultValue(new SettingColor(255, 255, 255))
                .visible(this.showCenter::get))
                .build()
        );

    public Orbit() {
        super(Compassion.LUNA, "Orbit", "Orbiting around the player position");
    }

    @EventHandler
    public void onGameJoined(GameJoinedEvent event) {
        this.orbitAngle = 0.0;
        this.isPopped = false;
        this.toggle();
    }

    @EventHandler
    private void onReceivePacket(Receive event) {
        if (this.onlyPopped.get()) {
            if (event.packet instanceof EntityStatusS2CPacket p) {
                if (p.getStatus() == 35) {
                    Entity entity = p.getEntity(this.mc.world);
                    if (entity instanceof PlayerEntity) {
                        if (entity.equals(this.mc.player)) {
                            this.isPopped = true;
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onGameLeft(GameLeftEvent event) {
        this.orbitAngle = 0.0;
        this.isPopped = false;
        this.toggle();
    }

    public void onActivate() {
        this.isPopped = false;
        this.centerVec3d = this.mc.player.getPos();
        this.orbitAngle = 0.0;
    }

    public void onDeactivate() {
        this.isPopped = false;
        this.centerVec3d = null;
        this.orbitAngle = 0.0;
    }

    @EventHandler
    private void onRender3D(Render3DEvent event) {
        if (this.showCenter.get()) {
            this.drawBoundingBox(event, this.mc.player);
        }
    }

    private void drawBoundingBox(Render3DEvent event, Entity entity) {
        double x = MathHelper.lerp(event.tickDelta, entity.lastRenderX, this.centerVec3d.x) - this.centerVec3d.x;
        double y = MathHelper.lerp(event.tickDelta, entity.lastRenderY, this.centerVec3d.y) - this.centerVec3d.y;
        double z = MathHelper.lerp(event.tickDelta, entity.lastRenderZ, this.centerVec3d.z) - this.centerVec3d.z;
        Box box = entity.getBoundingBox();
        event.renderer
            .box(
                x + box.minX,
                y + box.minY,
                z + box.minZ,
                x + box.maxX,
                y + box.maxY,
                z + box.maxZ,
                (Color) this.sideColor.get(),
                (Color) this.lineColor.get(),
                (ShapeMode) this.shapeMode.get(),
                0
            );
    }

    @EventHandler
    public void onTick(Pre event) {
        if (!this.onlyPopped.get()) {
            if (!this.random.get()) {
                this.orbitAngle = this.orbitAngle + this.orbitSpeed.get();
            } else {
                this.orbitAngle = this.orbitAngle + this.r.nextDouble(1.0, 50.0);
            }

            this.mc
                .player
                .setPos(
                    this.centerVec3d.x + this.orbitRange.get() * Math.cos(this.orbitAngle),
                    this.centerVec3d.y,
                    this.centerVec3d.z + this.orbitRange.get() * Math.sin(this.orbitAngle)
                );
        } else if (this.isPopped) {
            if (!this.random.get()) {
                this.orbitAngle = this.orbitAngle + this.orbitSpeed.get();
            } else {
                this.orbitAngle = this.orbitAngle + this.r.nextDouble(1.0, 50.0);
            }

            this.mc
                .player
                .setPos(
                    this.centerVec3d.x + this.orbitRange.get() * Math.cos(this.orbitAngle),
                    this.centerVec3d.y,
                    this.centerVec3d.z + this.orbitRange.get() * Math.sin(this.orbitAngle)
                );
        }
    }
}
