package dev.abstr3act.addon.hud;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.abstr3act.addon.events.EventEatFood;
import dev.abstr3act.addon.utils.Render2DUtils;
import dev.abstr3act.addon.utils.abnormally.AChatUtils;
import dev.abstr3act.addon.utils.render.MathUtility;
import dev.abstr3act.addon.utils.render.Render2DEngine;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.world.TickEvent.Post;
import meteordevelopment.meteorclient.renderer.text.TextRenderer;
import meteordevelopment.meteorclient.settings.DoubleSetting.Builder;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.math.RotationAxis;

import java.awt.*;

public class GappleHUD extends HudElement {
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
    private final Setting<Double> scale = this.sgGeneral
        .add(((Builder) ((Builder) new Builder().name("scale")).description("The scale.")).defaultValue(2.0).min(1.0).sliderRange(1.0, 5.0).build());
    private final Setting<Double> scaleFactor = this.sgGeneral
        .add(((Builder) ((Builder) new Builder().name("ScaleFactor")).description("The scale.")).defaultValue(2.0).min(1.0).sliderRange(1.0, 5.0).build());
    private final Setting<Double> t_offset_x = this.sgGeneral
        .add(((Builder) ((Builder) new Builder().name("Text offset X")).description("The scale.")).defaultValue(0.0).sliderRange(-100.0, 100.0).build());
    private final Setting<Double> t_offset_y = this.sgGeneral
        .add(((Builder) ((Builder) new Builder().name("Text offset Y")).description("The scale.")).defaultValue(0.0).sliderRange(-100.0, 100.0).build());
    private final Setting<Boolean> crapple = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("crapple"))
                .description("."))
                .defaultValue(false))
                .build()
        );
    private float angle;    public static final HudElementInfo<GappleHUD> INFO = new HudElementInfo(dev.abstr3act.addon.Compassion.HUD_GROUP, "GappleHUD", ".", GappleHUD::new);
    private float prevAngle;
    public GappleHUD() {
        super(INFO);
        MeteorClient.EVENT_BUS.subscribe(this);
    }

    @EventHandler
    public void onTick(Post event) {
        this.prevAngle = this.angle;
        if (this.angle > 0.0F) {
            this.angle--;
        }
    }

    public void render(HudRenderer renderer) {
        this.setSize(100.0, 100.0);
        Item targetItem = this.crapple.get() ? Items.GOLDEN_APPLE : Items.ENCHANTED_GOLDEN_APPLE;
        if (this.getItemCount(targetItem) != 0) {
            float xPos = this.getX();
            float yPos = this.getY();
            float factor = this.angle > 0.0F ? this.angle / 15.0F : 0.0F;
            float factor2 = 1.0F - MeteorClient.mc.player.getItemUseTime() / 40.0F;
            if (MeteorClient.mc.player.getActiveItem().getItem() != targetItem) {
                factor2 = 1.0F;
            }

            factor2 = MathUtility.clamp(factor2, 0.01F, 1.0F);
            DrawContext context = renderer.drawContext;
            context.getMatrices().push();
            context.getMatrices().translate(xPos, yPos, 0.0F);
            context.getMatrices().scale((this.scaleFactor.get()).floatValue(), (this.scaleFactor.get()).floatValue(), 1.0F);
            context.getMatrices().translate(-xPos, -yPos, 0.0F);
            context.getMatrices().translate(xPos, yPos, 0.0F);
            context.getMatrices()
                .multiply(
                    RotationAxis.NEGATIVE_Z
                        .rotation(
                            (float) Math.toRadians(-Render2DEngine.interpolateFloat(this.prevAngle, this.angle, MeteorClient.mc.getRenderTickCounter().getTickDelta(true)))
                        )
                );
            context.getMatrices().translate(-xPos, -yPos, 0.0F);
            RenderSystem.setShaderColor(0.3F, 0.3F, 0.3F, 1.0F);
            context.getMatrices().translate(xPos + 20.0F, yPos - 9.0F, 0.0F);
            context.drawItem(targetItem.getDefaultStack(), 0, 0);
            context.getMatrices().translate(-(xPos + 20.0F), -(yPos - 9.0F), 0.0F);
            RenderSystem.setShaderColor(1.0F, 1.0F - factor, 1.0F - factor, 1.0F);
            context.getMatrices().translate(xPos + 28.0F, yPos - 1.0F, 0.0F);
            context.getMatrices().scale(factor2, factor2, 1.0F);
            context.drawItem(targetItem.getDefaultStack(), -8, -8);
            context.getMatrices().scale(factor2 != 0.0F ? 1.0F / factor2 : 1.0F, factor2 != 0.0F ? 1.0F / factor2 : 1.0F, 1.0F);
            context.getMatrices().translate(-(xPos + 28.0F), -(yPos - 1.0F), 0.0F);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            if (factor > 0.0F) {
                Render2DEngine.drawBlurredShadow(
                    context.getMatrices(),
                    xPos + 22.0F,
                    yPos - 6.0F,
                    11.0F,
                    11.0F,
                    8,
                    Render2DUtils.injectAlpha(new Color(255, 120, 140), (int) (255.0F * factor)).toSetting()
                );
            }

            TextRenderer.get().begin(this.scale.get());
            TextRenderer.get()
                .render(
                    this.getItemCount(targetItem) + "",
                    xPos + this.t_offset_x.get(),
                    yPos + this.t_offset_y.get(),
                    meteordevelopment.meteorclient.utils.render.color.Color.WHITE,
                    true
                );
            TextRenderer.get().end();
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            context.getMatrices().pop();
        }
    }

    @EventHandler
    public void onEatFood(EventEatFood e) {
        if (e.getFood().getItem() == Items.GOLDEN_APPLE || e.getFood().getItem() == Items.ENCHANTED_GOLDEN_APPLE) {
            this.angle = 15.0F;
        }

        AChatUtils.sendMsgAmrita(Text.of("Regenerated."));
    }

    public int getItemCount(Item item) {
        if (MeteorClient.mc.player == null) {
            return 0;
        } else {
            int n = 0;
            int n2 = 44;

            for (int i = 0; i <= n2; i++) {
                ItemStack itemStack = MeteorClient.mc.player.getInventory().getStack(i);
                if (itemStack.getItem() == item) {
                    n += itemStack.getCount();
                }
            }

            return n;
        }
    }


}
