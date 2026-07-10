package dev.abstr3act.addon.modules.Seraphim;

import dev.abstr3act.addon.Compassion;
import dev.abstr3act.addon.module.SeraphimModule;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.settings.DoubleSetting.Builder;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;

public class MotionCamera extends SeraphimModule {
    public static MotionCamera INSTANCE;
    public final SettingGroup sgGeneral = this.settings.getDefaultGroup();
    public final Setting<Double> interpolation = this.sgGeneral
        .add(((Builder) ((Builder) new Builder().name("Motion Interpolation")).description(".")).defaultValue(0.15).sliderRange(0.05, 0.5).build());
    public double prevRenderX = 0.0;
    public double prevRenderY = 0.0;
    public double prevRenderZ = 0.0;

    public MotionCamera() {
        super(Compassion.SERAPHIM, "MotionCamera", ".");
        INSTANCE = this;
    }

    @EventHandler
    public void onUpdate(Render3DEvent event) {
        float tickDelta = this.mc.getRenderTickCounter().getTickDelta(true);
        Entity focusedEntity = this.mc.getCameraEntity();
        if (focusedEntity != null) {
            double d0 = MathHelper.lerp(tickDelta, focusedEntity.prevX, focusedEntity.getX());
            double d1 = MathHelper.lerp(tickDelta, focusedEntity.prevY, focusedEntity.getY()) + focusedEntity.getStandingEyeHeight();
            double d2 = MathHelper.lerp(tickDelta, focusedEntity.prevZ, focusedEntity.getZ());
            this.prevRenderX = this.prevRenderX + (d0 - this.prevRenderX) * this.interpolation.get();
            this.prevRenderY = this.prevRenderY + (d1 - this.prevRenderY) * this.interpolation.get();
            this.prevRenderZ = this.prevRenderZ + (d2 - this.prevRenderZ) * this.interpolation.get();
        }
    }
}
