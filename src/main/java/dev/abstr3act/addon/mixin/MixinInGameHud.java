package dev.abstr3act.addon.mixin;

import dev.abstr3act.addon.font.FontRenderers;
import dev.abstr3act.addon.hud.HealthBar;
import dev.abstr3act.addon.hud.HotBar;
import dev.abstr3act.addon.hud.NewHudElement;
import dev.abstr3act.addon.hud.ScoreboardHud;
import dev.abstr3act.addon.interfaces.SidebarEntry;
import dev.abstr3act.addon.utils.render.Render2DEngine;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.gui.tabs.builtin.HudTab.HudScreen;
import meteordevelopment.meteorclient.systems.hud.Hud;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.systems.hud.screens.HudEditorScreen;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardEntry;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.Team;
import net.minecraft.scoreboard.number.NumberFormat;
import net.minecraft.scoreboard.number.StyledNumberFormat;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.*;
import java.util.Collection;
import java.util.Comparator;

@Mixin({InGameHud.class})
public abstract class MixinInGameHud {
    @Unique
    float max_length;

    @Inject(
        at = {@At("HEAD")},
        method = {"render"}
    )
    public void renderHook(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        if (MeteorClient.mc.world != null && MeteorClient.mc.player != null && NewHudElement.INSTANCE != null) {
            if (Hud.get().active || MeteorClient.mc.currentScreen instanceof HudEditorScreen || MeteorClient.mc.currentScreen instanceof HudScreen) {
                NewHudElement.INSTANCE.onRender2D(context);
            }
        }
    }

    @Inject(
        at = {@At("HEAD")},
        method = {"renderHotbar"},
        cancellable = true
    )
    public void renderHotbarCustom(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        if (HotBar.INSTANCE != null && HotBar.INSTANCE.isActive() && HotBar.INSTANCE.cancel.get()) {
            ci.cancel();
        }
    }

    @Inject(
        method = {"renderExperienceBar"},
        at = {@At("HEAD")},
        cancellable = true
    )
    public void renderXpBarCustom(DrawContext context, int x, CallbackInfo ci) {
        if (HealthBar.INSTANCE != null && HealthBar.INSTANCE.isActive() && HealthBar.INSTANCE.cancel.get()) {
            ci.cancel();
        }
    }

    @Inject(
        method = {"renderHeldItemTooltip"},
        at = {@At("HEAD")},
        cancellable = true
    )
    private void onRenderHeldItemTooltip(DrawContext context, CallbackInfo ci) {
        if (HotBar.INSTANCE != null && HotBar.INSTANCE.isActive() && HotBar.INSTANCE.cancel.get()) {
            ci.cancel();
        }
    }

    @Inject(
        method = {"renderExperienceLevel"},
        at = {@At("HEAD")},
        cancellable = true
    )
    private void onRenderXpLevel(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        if (HealthBar.INSTANCE != null && HealthBar.INSTANCE.isActive() && HealthBar.INSTANCE.cancel.get()) {
            ci.cancel();
        }
    }

    @Inject(
        method = {"renderStatusBars"},
        at = {@At("HEAD")},
        cancellable = true
    )
    private void hookRenderStatusBars(CallbackInfo ci) {
        if (HealthBar.INSTANCE != null) {
            if (HealthBar.INSTANCE.isActive() && HealthBar.INSTANCE.cancel.get()) {
                ci.cancel();
            }
        }
    }

    @Inject(
        method = {"renderScoreboardSidebar(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/scoreboard/ScoreboardObjective;)V"},
        at = {@At("HEAD")},
        cancellable = true
    )
    private void renderScoreboardSidebarHook(DrawContext context, ScoreboardObjective objective, CallbackInfo ci) {
        for (HudElement element : Hud.get()) {
            if (element instanceof ScoreboardHud scoreboardHud) {
                ci.cancel();
                Scoreboard scoreboard = objective.getScoreboard();
                NumberFormat numberFormat = objective.getNumberFormatOr(StyledNumberFormat.RED);
                Comparator<ScoreboardEntry> comparatorString = Comparator.comparing(entry -> entry.name().getLiteralString(), String.CASE_INSENSITIVE_ORDER);
                Comparator<ScoreboardEntry> comparator = Comparator.comparing(ScoreboardEntry::value)
                    .reversed()
                    .thenComparing(ScoreboardEntry::owner, String.CASE_INSENSITIVE_ORDER);
                Collection<ScoreboardEntry> entries = scoreboard.getScoreboardEntries(objective);
                SidebarEntry[] sidebarEntries = entries.stream().filter(score -> !score.hidden()).sorted(comparator).map(scoreboardEntry -> {
                    Team team = scoreboard.getScoreHolderTeam(scoreboardEntry.owner());
                    Text textx = scoreboardEntry.name();
                    MutableText text2 = Team.decorateName(team, textx);
                    MutableText text3 = scoreboardEntry.formatted(numberFormat);
                    int width = this.getWidth(text3);
                    return new SidebarEntry(text2, text3, width);
                }).toArray(SidebarEntry[]::new);
                Text text = objective.getDisplayName();
                int j = this.getWidth(text);
                int joinerWidth = this.getWidth(Text.of(": "));

                for (SidebarEntry sidebarEntry : sidebarEntries) {
                    j = Math.max(j, this.getWidth(sidebarEntry.name()) + (sidebarEntry.scoreWidth() > 0 ? joinerWidth + sidebarEntry.scoreWidth() : 0));
                }

                int finalJ = j;
                context.draw(
                    () -> {
                        int length = sidebarEntries.length;
                        int m = scoreboardHud.getNewY() + length * this.getHeight() + this.getHeight() + 3;
                        int o = scoreboardHud.getNewX() + 3;
                        int splitLine = m - length * this.getHeight();
                        int totalHeight = this.getHeight() + 1 + (m - (splitLine - 1));
                        int p = o + finalJ + 2;
                        Render2DEngine.drawRoundedBlur2(
                            context.getMatrices(), o - 2, splitLine - this.getHeight() - 1, p - (o - 2), this.getHeight(), 2.0F, new Color(-872415232, true)
                        );
                        Render2DEngine.drawRoundedBlur2(
                            context.getMatrices(), o - 2, splitLine - 1, p - (o - 2), m - (splitLine - 1) + 1, 2.0F, new Color(-872415232, true)
                        );
                        scoreboardHud.setSize(p - o + 2, totalHeight + 2);
                        int titleColor = 16777215;
                        int textColor = 16777215;
                        FontRenderers.monsterrat_8.drawString(context.getMatrices(), text, scoreboardHud.offsetX.get() + o, splitLine - this.getHeight() + 2);

                        for (int t = 0; t < length; t++) {
                            SidebarEntry sidebarEntryx = sidebarEntries[t];
                            int u = m - (length - t) * this.getHeight();
                            if (sidebarEntryx.name().getStyle().getColor() != null) {
                                sidebarEntryx.name().getStyle().getColor().getRgb();
                            }

                            if (sidebarEntryx.score().getStyle().getColor() != null) {
                                sidebarEntryx.score().getStyle().getColor().getRgb();
                            }

                            FontRenderers.monsterrat_8.drawString(context.getMatrices(), sidebarEntryx.name(), o, u);
                            FontRenderers.monsterrat_8.drawString(context.getMatrices(), sidebarEntryx.score(), p - sidebarEntryx.scoreWidth(), u);
                        }
                    }
                );
            }
        }
    }

    @Shadow
    public abstract TextRenderer getTextRenderer();

    @Unique
    private int getWidth(Text text) {
        return this.getTextRenderer().getWidth(text);
    }

    @Unique
    private int getHeight() {
        return (int) FontRenderers.monsterrat_8.getFontHeight("idk");
    }

    @Unique
    private void drawText(DrawContext context, Text text, int x, int y, int color) {
        context.drawText(this.getTextRenderer(), text, x, y, color, false);
    }
}
