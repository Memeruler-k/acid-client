package dev.abstr3act.addon.modules.Abnormally;

import dev.abstr3act.addon.Compassion;
import dev.abstr3act.addon.module.AbnormallyModule;
import dev.abstr3act.addon.utils.abnormally.AChatUtils;
import meteordevelopment.meteorclient.events.game.ReceiveMessageEvent;
import meteordevelopment.meteorclient.events.world.TickEvent.Post;
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
import java.util.Random;

public class TextLag extends AbnormallyModule {
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
    public final Setting<String> command = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.StringSetting.Builder) ((meteordevelopment.meteorclient.settings.StringSetting.Builder) ((meteordevelopment.meteorclient.settings.StringSetting.Builder) new meteordevelopment.meteorclient.settings.StringSetting.Builder()
                .name("Command"))
                .description("Command for send private message"))
                .defaultValue("msg"))
                .build()
        );
    public final Setting<Integer> delay = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("delay"))
                .description("The delay between specified messages in ticks."))
                .defaultValue(20))
                .min(0)
                .sliderMax(200)
                .build()
        );
    public final Setting<Integer> messageLength = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("message-length"))
                .description("The length of the message."))
                .defaultValue(200))
                .min(1)
                .sliderMin(1)
                .sliderMax(1000)
                .build()
        );
    public final Setting<Boolean> Str1 = this.sgGeneral
        .add(
            ((Builder) ((Builder) ((Builder) new Builder().name("Latin Extended-A & IPA Extensions")).description("Range 0x00CA to 0x02AF")).defaultValue(false))
                .build()
        );
    public final Setting<Boolean> Str2 = this.sgGeneral
        .add(((Builder) ((Builder) ((Builder) new Builder().name("Greek & Coptic")).description("Range 0x0370 to 0x03FF")).defaultValue(false)).build());
    public final Setting<Boolean> Str3 = this.sgGeneral
        .add(((Builder) ((Builder) ((Builder) new Builder().name("Cyrillic")).description("Range 0x0400 to 0x04FF")).defaultValue(true)).build());
    public final Setting<Boolean> Str4 = this.sgGeneral
        .add(((Builder) ((Builder) ((Builder) new Builder().name("CJK Unified Ideographs")).description("0x4E00 to 0x9FFF")).defaultValue(true)).build());
    public final Setting<Boolean> Str5 = this.sgGeneral
        .add(((Builder) ((Builder) ((Builder) new Builder().name("Hangul Syllables")).description("Range 0xAC00 to 0xD7AF")).defaultValue(true)).build());
    public final Setting<Boolean> Str6 = this.sgGeneral
        .add(((Builder) ((Builder) ((Builder) new Builder().name("Alphabetic Presentation Forms")).description("Range 0xFB00 to 0xFB4F")).defaultValue(false)).build());
    public final Setting<Boolean> Str7 = this.sgGeneral
        .add(((Builder) ((Builder) ((Builder) new Builder().name("Halfwidth & Fullwidth Forms")).description("Range 0xFF00 to 0xFF73")).defaultValue(false)).build());
    public final Setting<SettingColor> feedbackColor = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                .name("feedback-color"))
                .description("Color of the feedback text."))
                .defaultValue(new SettingColor(176, 224, 230))
                .build()
        );
    private final Setting<Boolean> multi = this.sgGeneral
        .add(((Builder) ((Builder) ((Builder) new Builder().name("Multi mode")).description("UwU")).defaultValue(false)).build());
    public final Setting<List<String>> whitelist = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.StringListSetting.Builder) ((meteordevelopment.meteorclient.settings.StringListSetting.Builder) ((meteordevelopment.meteorclient.settings.StringListSetting.Builder) ((meteordevelopment.meteorclient.settings.StringListSetting.Builder) new meteordevelopment.meteorclient.settings.StringListSetting.Builder()
                .name("Target"))
                .description("Target"))
                .defaultValue(List.of("NoStrict")))
                .visible(this.multi::get))
                .build()
        );
    public final Setting<String> target = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.StringSetting.Builder) ((meteordevelopment.meteorclient.settings.StringSetting.Builder) ((meteordevelopment.meteorclient.settings.StringSetting.Builder) ((meteordevelopment.meteorclient.settings.StringSetting.Builder) new meteordevelopment.meteorclient.settings.StringSetting.Builder()
                .name("Target"))
                .description("Target :P"))
                .defaultValue("NoStrict"))
                .visible(() -> !this.multi.get()))
                .build()
        );
    private final Setting<Boolean> protectSelf = this.sgGeneral
        .add(
            ((Builder) ((Builder) ((Builder) new Builder().name("Self Protect")).description("Prevent you from fuck up by the random string")).defaultValue(false))
                .build()
        );
    int i = 0;
    String msg;
    private int timer;

    public TextLag() {
        super(Compassion.ABNORMALLY, "TextLag", "Use random string to fuck your enemy up");
    }

    public static String generateRandomString(int length, boolean[] rangeEnabled) {
        Random random = new Random();
        StringBuilder sb = new StringBuilder(length);
        int[][] validRanges = new int[][]{{202, 687}, {880, 1023}, {1024, 1279}, {19968, 40959}, {44032, 55215}, {64256, 64335}, {65280, 65395}};
        boolean atLeastOneEnabled = false;

        for (boolean enabled : rangeEnabled) {
            if (enabled) {
                atLeastOneEnabled = true;
                break;
            }
        }

        if (!atLeastOneEnabled) {
            rangeEnabled[0] = true;
            TextLag textLag = new TextLag();
            textLag.Str1.set(true);
            AChatUtils.sendMsg(
                "h-hewwo, senpai! i-i'm sowwy, but no wanges wewe enabwed, so i had to automaticawwy enabwe the fiwst wange! pwease fowgive me, i hope it's okay with you x3."
            );
        }

        for (int i = 0; i < length; i++) {
            int randomCodePoint = 0;
            boolean validCharFound = false;

            while (!validCharFound) {
                int rangeIndex;
                do {
                    rangeIndex = random.nextInt(validRanges.length);
                } while (!rangeEnabled[rangeIndex]);

                int[] range = validRanges[rangeIndex];
                randomCodePoint = random.nextInt(range[1] - range[0] + 1) + range[0];
                if (Character.isValidCodePoint(randomCodePoint)) {
                    validCharFound = true;
                }
            }

            sb.append((char) randomCodePoint);
        }

        return sb.toString();
    }

    public void onActivate() {
        this.timer = this.delay.get();
    }

    @EventHandler
    private void onTick(Post event) {
        boolean[] list = new boolean[]{
            this.Str1.get(),
            this.Str2.get(),
            this.Str3.get(),
            this.Str4.get(),
            this.Str5.get(),
            this.Str6.get(),
            this.Str7.get()
        };
        if ((this.whitelist.get()).isEmpty() && this.multi.get()) {
            AChatUtils.sendMsg("oh noes! no targets in da list! x3");
            this.toggle();
        }

        if (this.multi.get()) {
            if (this.timer <= 0) {
                if (this.i <= (this.whitelist.get()).size()) {
                    this.i = 0;
                }

                this.msg = generateRandomString(this.messageLength.get(), list);
                this.mc.player.networkHandler.sendChatCommand((String) this.command.get() + " " + (String) (this.whitelist.get()).get(this.i) + " " + this.msg);
                this.i++;
                this.timer = this.delay.get();
            } else {
                this.timer--;
            }
        } else if (this.timer <= 0) {
            this.msg = generateRandomString(this.messageLength.get(), list);
            this.mc.player.networkHandler.sendChatCommand((String) this.command.get() + " " + (String) this.target.get() + " " + this.msg);
            this.timer = this.delay.get();
        } else {
            this.timer--;
        }
    }

    @EventHandler
    public void onReceiveMessage(ReceiveMessageEvent event) {
        StringBuilder builder = new StringBuilder();
        event.getMessage().asOrderedText().accept((i, style, codePoint) -> {
            builder.append(new String(Character.toChars(codePoint)));
            return true;
        });
        StringBuilder wrap = new StringBuilder();
        if (event.getMessage().contains(Text.of(this.msg)) || event.getMessage().contains(Text.of((String) this.target.get()))) {
            event.setMessage(
                Text.literal("[Lag Message]")
                    .setStyle(
                        Style.EMPTY
                            .withColor(TextColor.fromRgb(((SettingColor) this.feedbackColor.get()).getPacked()))
                            .withHoverEvent(
                                new HoverEvent(
                                    Action.SHOW_TEXT,
                                    Text.literal(wrap.toString()).setStyle(Style.EMPTY.withColor(TextColor.fromRgb(((SettingColor) this.feedbackColor.get()).getPacked())))
                                )
                            )
                    )
            );
        }
    }
}
