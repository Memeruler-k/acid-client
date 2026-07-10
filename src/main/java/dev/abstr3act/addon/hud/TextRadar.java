package dev.abstr3act.addon.hud;

import dev.abstr3act.addon.font.FontRenderers;
import dev.abstr3act.addon.utils.render.Render2DEngine;
import dev.abstr3act.addon.utils.render.shaders.AnimationUtility;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import net.minecraft.entity.player.PlayerEntity;

import java.awt.*;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class TextRadar extends HudElement {
    float hAnimation;    public static final HudElementInfo<TextRadar> INFO = new HudElementInfo(dev.abstr3act.addon.Compassion.HUD_GROUP, "TextRadar", "123", TextRadar::new);
    float vAnimation;
    public TextRadar() {
        super(INFO);
        MeteorClient.EVENT_BUS.subscribe(this);
    }

    public static List<PlayerEntity> getPlayers() {
        return MeteorClient.mc.world != null && MeteorClient.mc.player != null
            ? MeteorClient.mc
            .world
            .getPlayers()
            .stream()
            .sorted(Comparator.comparingDouble(p -> MeteorClient.mc.player.squaredDistanceTo(p)))
            .collect(Collectors.toList())
            : List.of();
    }

    public void render(HudRenderer renderer) {
        renderer.post(
            () -> {
                int y = this.getY();
                int x = this.getX();
                int height = 0;
                float maxWidth = 0.0F;
                renderer.drawContext.getMatrices().push();

                for (PlayerEntity entity : getPlayers()) {
                    String playerName = entity.getName().getString();
                    String distance = String.format("%.1f", MeteorClient.mc.player.distanceTo(entity));
                    String health = String.format("%.1f", entity.getHealth() + entity.getAbsorptionAmount());
                    float width = 0.0F;
                    width += FontRenderers.monsterrat_16.getStringWidth(playerName + " ");
                    width += FontRenderers.monsterrat_16.getStringWidth(distance + " ");
                    float var21 = width + FontRenderers.monsterrat_16.getStringWidth(health);
                    if (var21 > maxWidth) {
                        maxWidth = var21;
                    }

                    height += 16;
                }

                height += 16;
                this.hAnimation = AnimationUtility.ease(this.hAnimation, maxWidth, 3.0F);
                this.vAnimation = AnimationUtility.ease(this.vAnimation, height, 3.0F);
                Render2DEngine.drawRoundedBlur(renderer.drawContext.getMatrices(), x, y, this.hAnimation, this.vAnimation, 0.0F, Color.BLACK);
                FontRenderers.monsterrat_16.drawStringFix(renderer.drawContext.getMatrices(), "Players", x, y, Color.WHITE);
                int var12 = y + 16;

                for (PlayerEntity entity : getPlayers()) {
                    String playerName = entity.getName().getString();
                    String distance = String.format("%.1f", MeteorClient.mc.player.distanceTo(entity));
                    String health = String.format("%.1f", entity.getHealth() + entity.getAbsorptionAmount());
                    FontRenderers.monsterrat_16
                        .drawStringFix(
                            renderer.drawContext.getMatrices(),
                            playerName,
                            x,
                            var12,
                            new Color(PlayerUtils.getPlayerColor(entity, meteordevelopment.meteorclient.utils.render.color.Color.WHITE).getPacked())
                        );
                    float width = FontRenderers.monsterrat_16.getStringWidth(playerName + " ");
                    FontRenderers.monsterrat_16
                        .drawStringFix(
                            renderer.drawContext.getMatrices(),
                            distance,
                            x + width,
                            var12,
                            new Color(PlayerUtils.getPlayerColor(entity, meteordevelopment.meteorclient.utils.render.color.Color.WHITE).getPacked())
                        );
                    width += FontRenderers.monsterrat_16.getStringWidth(distance + " ");
                    FontRenderers.monsterrat_16
                        .drawStringFix(
                            renderer.drawContext.getMatrices(),
                            health,
                            x + width,
                            var12,
                            new Color(PlayerUtils.getPlayerColor(entity, meteordevelopment.meteorclient.utils.render.color.Color.WHITE).getPacked())
                        );
                    var12 += 16;
                }

                this.setSize(100.0, var12 - this.getY());
                if (this.isInEditor()) {
                    this.setSize(100.0, 100.0);
                }

                renderer.drawContext.getMatrices().pop();
            }
        );
    }


}
