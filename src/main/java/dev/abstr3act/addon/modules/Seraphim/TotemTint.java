package dev.abstr3act.addon.modules.Seraphim;

import dev.abstr3act.addon.Compassion;
import dev.abstr3act.addon.module.SeraphimModule;
import dev.abstr3act.addon.utils.math.MathUtility;
import dev.abstr3act.addon.utils.render.Render2DEngine;
import meteordevelopment.meteorclient.events.render.Render2DEvent;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.item.Items;

import java.awt.*;

public class TotemTint extends SeraphimModule {
    public TotemTint() {
        super(Compassion.SERAPHIM, "TotemTint", ".");
    }

    @EventHandler
    public void onRender2D(Render2DEvent event) {
        if (!this.mc.player.getOffHandStack().getItem().equals(Items.TOTEM_OF_UNDYING)) {
            float factor = 1.0F - MathUtility.clamp(this.mc.player.getHealth(), 0.0F, 12.0F) / 12.0F;
            Color red = new Color(16711680, true);
            if (factor < 1.0F) {
                Render2DEngine.draw2DGradientRect(
                    event.drawContext.getMatrices(),
                    0.0F,
                    0.0F,
                    this.mc.getWindow().getScaledWidth() * 2,
                    this.mc.getWindow().getScaledHeight() * 2,
                    Render2DEngine.injectAlpha(red, (int) (factor * 170.0F)),
                    red,
                    Render2DEngine.injectAlpha(red, (int) (factor * 170.0F)),
                    red
                );
            }
        }
    }
}
