package dev.abstr3act.addon.hud;

import dev.abstr3act.addon.font.FontRenderers;
import dev.abstr3act.addon.interfaces.SidebarEntry;
import dev.abstr3act.addon.utils.render.Render2DEngine;
import meteordevelopment.meteorclient.settings.IntSetting.Builder;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.scoreboard.*;
import net.minecraft.scoreboard.number.NumberFormat;
import net.minecraft.scoreboard.number.StyledNumberFormat;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EventTimer extends HudElement {
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
    private final Setting<Integer> width = this.sgGeneral
        .add(((Builder) ((Builder) new Builder().name("Width")).defaultValue(50)).sliderRange(-1000, 1000).build());    public static final HudElementInfo<EventTimer> INFO = new HudElementInfo(
        dev.abstr3act.addon.Compassion.HUD_GROUP, "EventTimer", "It's a Cat girl what do you want", EventTimer::new
    );
    private final Setting<Integer> height = this.sgGeneral
        .add(((Builder) ((Builder) new Builder().name("Height")).defaultValue(50)).sliderRange(-1000, 1000).build());
    private final Setting<String> pattern = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.StringSetting.Builder) ((meteordevelopment.meteorclient.settings.StringSetting.Builder) new meteordevelopment.meteorclient.settings.StringSetting.Builder()
                .name("Pattern"))
                .defaultValue("(.+?): \\((\\d{2}:\\d{2}:\\d{2})\\)"))
                .build()
        );
    public EventTimer() {
        super(INFO);
    }

    public void render(HudRenderer renderer) {
        renderer.post(
            () -> {
                renderer.drawContext.getMatrices().push();
                int y = this.getY();
                int height = 18;

                for (String[] ignored : this.getTimers()) {
                    height += 32;
                }

                Render2DEngine.drawRect(renderer.drawContext.getMatrices(), 0.0F, 0.0F, 0.0F, 0.0F, Color.cyan);
                Render2DEngine.drawRoundedBlur(
                    renderer.drawContext.getMatrices(), this.getX(), this.getY(), (this.width.get()).intValue(), height, 0.0F, Color.BLACK
                );
                FontRenderers.monsterrat_16.drawStringFix(renderer.drawContext.getMatrices(), "EventTimer", this.getX(), y, Color.WHITE);
                int var6 = y + 18;

                for (String[] strings : this.getTimers()) {
                    FontRenderers.monsterrat_16
                        .drawStringFix(
                            renderer.drawContext.getMatrices(), strings[0], this.getX(), var6, strings[0].toLowerCase().contains("start") ? Color.GREEN : Color.RED
                        );
                    y = var6 + 16;
                    FontRenderers.monsterrat_16.drawStringFix(renderer.drawContext.getMatrices(), strings[1], this.getX(), y, Color.WHITE);
                    var6 = y + 16;
                }

                Render2DEngine.drawRect(renderer.drawContext.getMatrices(), 0.0F, 0.0F, 0.0F, 0.0F, Color.cyan);
                this.setSize((this.width.get()).intValue(), height);
                renderer.drawContext.getMatrices().pop();
            }
        );
    }

    private List<String[]> getTimers() {
        List<String[]> timerList = new ArrayList<>();

        try {
            Pattern TIME_ENTRY_PATTERN = Pattern.compile((String) this.pattern.get());
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.world != null && client.player != null) {
                Scoreboard scoreboard = client.world.getScoreboard();
                ScoreboardObjective objective = scoreboard.getObjectiveForSlot(ScoreboardDisplaySlot.SIDEBAR);
                if (objective == null) {
                    return Collections.singletonList(new String[]{"null", "00:00:00"});
                } else {
                    Collection<ScoreboardEntry> entries = scoreboard.getScoreboardEntries(objective);
                    NumberFormat numberFormat = objective.getNumberFormatOr(StyledNumberFormat.RED);
                    Comparator<ScoreboardEntry> comparator = Comparator.comparing(ScoreboardEntry::value)
                        .reversed()
                        .thenComparing(ScoreboardEntry::owner, String.CASE_INSENSITIVE_ORDER);
                    SidebarEntry[] sidebarEntries = entries.stream().filter(score -> !score.hidden()).sorted(comparator).map(scoreboardEntry -> {
                        Team team = scoreboard.getScoreHolderTeam(scoreboardEntry.owner());
                        Text text = scoreboardEntry.name();
                        MutableText text2 = Team.decorateName(team, text);
                        MutableText text3 = scoreboardEntry.formatted(numberFormat);
                        return new SidebarEntry(text2, text3, 0);
                    }).toArray(SidebarEntry[]::new);

                    for (SidebarEntry sidebarEntry : sidebarEntries) {
                        String entry = sidebarEntry.name().getString();
                        Matcher matcher = TIME_ENTRY_PATTERN.matcher(entry);
                        if (matcher.find()) {
                            String title = matcher.group(1) != null ? matcher.group(1) : "null";
                            String time = matcher.group(2) != null ? matcher.group(2) : "00:00:00";
                            timerList.add(new String[]{title, time});
                        }
                    }

                    return timerList.isEmpty() ? Collections.singletonList(new String[]{"null", "00:00:00"}) : timerList;
                }
            } else {
                return Collections.singletonList(new String[]{"null", "00:00:00"});
            }
        } catch (Exception var18) {
            return Collections.singletonList(new String[]{"null", "00:00:00"});
        }
    }


}
