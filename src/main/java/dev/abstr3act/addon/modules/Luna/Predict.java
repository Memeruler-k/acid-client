package dev.abstr3act.addon.modules.Luna;

import dev.abstr3act.addon.Compassion;
import dev.abstr3act.addon.module.LunaModule;
import dev.abstr3act.addon.utils.PredictUtility;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent.Post;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.DoubleSetting.Builder;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

public class Predict extends LunaModule {
    static PlayerEntity target;
    public List<Vec3d> predictedPositions = new ArrayList<>();
    private SettingGroup sgGeneral = this.settings.getDefaultGroup();
    private final Setting<Double> Range = this.sgGeneral
        .add(
            ((Builder) ((Builder) new Builder().name("range")).description("The range of the orbit in blocks"))
                .defaultValue(20.0)
                .sliderMin(1.0)
                .sliderMax(100.0)
                .build()
        );
    private final Setting<Integer> predictTicks = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("predict-ticks"))
                .description("Value of predict ticks."))
                .defaultValue(5))
                .sliderMin(1)
                .min(1)
                .sliderMax(20)
                .build()
        );
    public final Setting<ShapeMode> shapeMode = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) new meteordevelopment.meteorclient.settings.EnumSetting.Builder()
                .name("shape-mode"))
                .description("How the shapes are rendered."))
                .defaultValue(ShapeMode.Both))
                .build()
        );
    private final Setting<SettingColor> sideColor = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                .name("side-color"))
                .description("Color of sides"))
                .defaultValue(new SettingColor(255, 255, 255))
                .build()
        );
    private final Setting<SettingColor> lineColor = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                .name("line-color"))
                .description("Color of lines"))
                .defaultValue(new SettingColor(255, 255, 255))
                .build()
        );

    public Predict() {
        super(Compassion.LUNA, "Predict", "Predict player Movement");
    }

    @EventHandler
    public void onUpdate(Post event) {
        this.predictedPositions.clear();

        for (PlayerEntity player : this.mc.world.getPlayers()) {
            target = player;
            if (!player.equals(this.mc.player) && this.mc.player.squaredDistanceTo(player) <= this.Range.get() * this.Range.get()) {
                PlayerEntity predictedPosition = PredictUtility.predictPlayer(player, this.predictTicks.get());
                if (predictedPosition != null) {
                    this.predictedPositions.add(predictedPosition.getPos());
                }
            }
        }
    }

    private void drawBoundingBox(Render3DEvent event, Vec3d predictedPos, Entity entity) {
        if (this.color != null) {
            this.lineColor.set((SettingColor) this.sideColor.get());
            this.sideColor.set((SettingColor) this.lineColor.get());
        }

        double x = MathHelper.lerp(event.tickDelta, predictedPos.x, predictedPos.x) - predictedPos.x;
        double y = MathHelper.lerp(event.tickDelta, predictedPos.y, predictedPos.y) - predictedPos.y;
        double z = MathHelper.lerp(event.tickDelta, predictedPos.z, predictedPos.z) - predictedPos.z;
        Box box = PredictUtility.predictBox(target, this.predictTicks.get());
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
    public void onRender3D(Render3DEvent event) {
        if (target != null) {
            if (this.predictedPositions != null && !this.predictedPositions.isEmpty()) {
                for (Vec3d predictedPos : this.predictedPositions) {
                    this.drawBoundingBox(event, predictedPos, target);
                }
            }
        }
    }
}
