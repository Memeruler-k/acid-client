package dev.abstr3act.addon.modules.Abnormally;

import dev.abstr3act.addon.Compassion;
import dev.abstr3act.addon.module.AbnormallyModule;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.EnumSetting.Builder;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.utils.entity.EntityUtils;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.render.WireframeEntityRenderer;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;

public class CrystalChams extends AbnormallyModule {
    private final SettingGroup sgRender = this.settings.createGroup("Render");
    public final Setting<ShapeMode> shapeMode = this.sgRender
        .add(((Builder) ((Builder) ((Builder) new Builder().name("Shape Mode")).description("How the shapes are rendered.")).defaultValue(ShapeMode.Both)).build());
    public final Setting<Double> fillOpacity = this.sgRender
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("Fill Opacity"))
                .description("The opacity of the shape fill."))
                .visible(() -> this.shapeMode.get() != ShapeMode.Lines))
                .defaultValue(0.3)
                .range(0.0, 1.0)
                .sliderMax(1.0)
                .build()
        );
    private final SettingGroup sgColor = this.settings.createGroup("Color");
    public final Setting<Boolean> getDistance = this.sgColor
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("Distance Colors"))
                .description("Changes the color of tracers depending on distance."))
                .defaultValue(false))
                .build()
        );
    private final Setting<SettingColor> color = this.sgColor
        .add(
            ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                .name("Color"))
                .description("The color."))
                .defaultValue(new SettingColor(175, 175, 175, 255))
                .visible(() -> !this.getDistance.get()))
                .build()
        );
    private final Setting<Double> renderDistance = this.sgRender
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("Render Distance"))
                .description("."))
                .defaultValue(12.0)
                .min(0.0)
                .sliderMax(256.0)
                .build()
        );
    private final Setting<Double> scale = this.sgRender
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("Scale"))
                .description("The size."))
                .defaultValue(1.0)
                .min(0.0)
                .sliderMax(10.0)
                .build()
        );
    private final Setting<Double> fadeDistance = this.sgRender
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("Fade Distance"))
                .description("The distance from an entity where the color begins to fade."))
                .defaultValue(3.0)
                .min(0.0)
                .sliderMax(12.0)
                .build()
        );
    private final Color lineColor = new Color();
    private final Color sideColor = new Color();
    private final Color baseColor = new Color();
    private int count;

    public CrystalChams() {
        super(Compassion.ABNORMALLY, "CrystalChams", "Renders end crystal.");
    }

    @EventHandler
    private void onRender3D(Render3DEvent event) {
        this.count = 0;

        for (Entity entity : this.mc.world.getEntities()) {
            if (!this.shouldSkip(entity)) {
                Color color = this.getColor(entity);
                if (color != null) {
                    this.lineColor.set(color);
                    this.sideColor.set(color).a((int) (this.sideColor.a * this.fillOpacity.get()));
                }

                WireframeEntityRenderer.render(event, entity, this.scale.get(), this.sideColor, this.lineColor, (ShapeMode) this.shapeMode.get());
                this.count++;
            }
        }
    }

    public boolean shouldSkip(Entity entity) {
        if (!entity.getType().equals(EntityType.END_CRYSTAL)) {
            return true;
        } else if (entity == this.mc.cameraEntity && this.mc.options.getPerspective().isFirstPerson()) {
            return true;
        } else {
            return PlayerUtils.distanceTo(entity) > this.renderDistance.get() ? true : !EntityUtils.isInRenderDistance(entity);
        }
    }

    public Color getColor(Entity entity) {
        if (!entity.getType().equals(EntityType.END_CRYSTAL)) {
            return null;
        } else {
            double alpha = this.getFadeAlpha(entity);
            if (alpha == 0.0) {
                return null;
            } else {
                Color color = this.getEntityTypeColor(entity);
                return this.baseColor.set(color.r, color.g, color.b, (int) (color.a * alpha));
            }
        }
    }

    private double getFadeAlpha(Entity entity) {
        double dist = PlayerUtils.squaredDistanceToCamera(
            entity.getX() + entity.getWidth() / 2.0F, entity.getY() + entity.getEyeHeight(entity.getPose()), entity.getZ() + entity.getWidth() / 2.0F
        );
        double fadeDist = Math.pow(this.fadeDistance.get(), 2.0);
        double alpha = 1.0;
        if (dist <= fadeDist * fadeDist) {
            alpha = (float) (Math.sqrt(dist) / fadeDist);
        }

        if (alpha <= 0.075) {
            alpha = 0.0;
        }

        return alpha;
    }

    public Color getEntityTypeColor(Entity entity) {
        return this.getDistance.get() ? EntityUtils.getColorFromDistance(entity) : (Color) this.color.get();
    }

    public String getInfoString() {
        return Integer.toString(this.count);
    }
}
