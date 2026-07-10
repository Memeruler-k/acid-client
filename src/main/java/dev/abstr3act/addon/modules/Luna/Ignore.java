package dev.abstr3act.addon.modules.Luna;

import dev.abstr3act.addon.Compassion;
import dev.abstr3act.addon.module.AbnormallyModule;
import dev.abstr3act.addon.utils.luna.StringUtils;
import meteordevelopment.meteorclient.events.game.GameJoinedEvent;
import meteordevelopment.meteorclient.events.game.GameLeftEvent;
import meteordevelopment.meteorclient.events.game.ReceiveMessageEvent;
import meteordevelopment.meteorclient.settings.BoolSetting.Builder;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.HoverEvent.Action;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;

import java.util.List;

public class Ignore extends AbnormallyModule {
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
    private final Setting<Boolean> sendFeedback = this.sgGeneral
        .add(
            ((Builder) ((Builder) ((Builder) new Builder().name("send-feedback")).description("Send a feedback while the target is barking")).defaultValue(false))
                .build()
        );
    public final Setting<SettingColor> feedbackColor = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                .name("feedback-color"))
                .description("Color of the feedback text."))
                .defaultValue(new SettingColor(176, 224, 230))
                .visible(this.sendFeedback::get))
                .build()
        );
    private final Setting<String> barkMsg = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.StringSetting.Builder) ((meteordevelopment.meteorclient.settings.StringSetting.Builder) ((meteordevelopment.meteorclient.settings.StringSetting.Builder) ((meteordevelopment.meteorclient.settings.StringSetting.Builder) new meteordevelopment.meteorclient.settings.StringSetting.Builder()
                .name("Bark-feedback"))
                .description("Feedback message"))
                .defaultValue("Barking"))
                .visible(this.sendFeedback::get))
                .build()
        );
    private final Setting<Boolean> sarcasm = this.sgGeneral
        .add(((Builder) ((Builder) ((Builder) new Builder().name("sarcasm")).description("Sarcasm dogs")).defaultValue(false)).build());
    private final Setting<Integer> messageLimit = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("maximum-message-limit"))
                .description("Sarcasm message limit"))
                .defaultValue(20))
                .visible(this.sarcasm::get))
                .build()
        );
    private final Setting<String> sarcasmMsg = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.StringSetting.Builder) ((meteordevelopment.meteorclient.settings.StringSetting.Builder) ((meteordevelopment.meteorclient.settings.StringSetting.Builder) ((meteordevelopment.meteorclient.settings.StringSetting.Builder) new meteordevelopment.meteorclient.settings.StringSetting.Builder()
                .name("sarcasm-message"))
                .description("Sarcasm message (%p: Player Name, %n: Message Number)"))
                .defaultValue("我发现死全家%p狗叫了%n条信息"))
                .visible(this.sarcasm::get))
                .build()
        );
    private final Setting<List<String>> players = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.StringListSetting.Builder) ((meteordevelopment.meteorclient.settings.StringListSetting.Builder) ((meteordevelopment.meteorclient.settings.StringListSetting.Builder) new meteordevelopment.meteorclient.settings.StringListSetting.Builder()
                .name("players"))
                .description("Players to use for ignore."))
                .defaultValue(List.of("NoStrict")))
                .build()
        );
    long msg;

    public Ignore() {
        super(Compassion.LUNA, "Ignore", "Prevent dogs bark to you");
    }

    @EventHandler
    public void onGameLeft(GameLeftEvent event) {
        this.msg = 0L;
    }

    @EventHandler
    public void onGameJoin(GameJoinedEvent event) {
        this.msg = 0L;
    }

    public void onActivate() {
        this.msg = 0L;
    }

    public void onDeactivate() {
        this.msg = 0L;
    }

    @EventHandler
    public void onReceiveMessage(ReceiveMessageEvent event) {
        StringBuilder builder = new StringBuilder();
        event.getMessage().asOrderedText().accept((i, style, codePoint) -> {
            builder.append(new String(Character.toChars(codePoint)));
            return true;
        });
        if (StringUtils.getParts(event.getMessage()).length >= 2) {
            StringBuilder wrap = new StringBuilder();
            if (StringUtils.hasMatchingSubstring(event.getMessage().getString(), (List<String>) this.players.get())) {
                this.msg++;
                if (this.msg >= (this.messageLimit.get()).intValue() && this.sarcasm.get()) {
                    String sarcasms = ((String) this.sarcasmMsg.get())
                        .replace("%p", StringUtils.getPlayerName(event.getMessage()))
                        .replace("%n", String.valueOf(this.msg));
                    this.mc.getNetworkHandler().sendChatMessage(sarcasms);
                    this.msg = 0L;
                }

                if (this.sendFeedback.get()) {
                    event.setMessage(
                        Text.literal(" <" + StringUtils.getPlayerName(event.getMessage()) + "> ")
                            .append(
                                Text.literal((String) this.barkMsg.get())
                                    .setStyle(
                                        Style.EMPTY
                                            .withColor(TextColor.fromRgb(((SettingColor) this.feedbackColor.get()).getPacked()))
                                            .withHoverEvent(
                                                new HoverEvent(
                                                    Action.SHOW_TEXT,
                                                    Text.literal(wrap.toString())
                                                        .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(((SettingColor) this.feedbackColor.get()).getPacked())))
                                                )
                                            )
                                    )
                            )
                    );
                } else {
                    event.cancel();
                }
            }
        }
    }
}
