package dev.abstr3act.addon.modules.Abnormally;

import dev.abstr3act.addon.Compassion;
import dev.abstr3act.addon.module.AbnormallyModule;
import dev.abstr3act.addon.utils.abnormally.AChatUtils;
import meteordevelopment.meteorclient.events.game.ReceiveMessageEvent;
import meteordevelopment.meteorclient.events.packets.PacketEvent.Receive;
import meteordevelopment.meteorclient.settings.IntSetting.Builder;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.HoverEvent.Action;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;

import java.util.Random;

public class PopLag extends AbnormallyModule {
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
    public final Setting<String> command = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.StringSetting.Builder) ((meteordevelopment.meteorclient.settings.StringSetting.Builder) ((meteordevelopment.meteorclient.settings.StringSetting.Builder) new meteordevelopment.meteorclient.settings.StringSetting.Builder()
                .name("Command"))
                .description("Command for send private message"))
                .defaultValue("msg"))
                .build()
        );
    public final Setting<Boolean> Str1 = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("Latin Extended-A & IPA Extensions"))
                .description("Range 0x00CA to 0x02AF"))
                .defaultValue(false))
                .build()
        );
    public final Setting<Boolean> Str2 = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("Greek & Coptic"))
                .description("Range 0x0370 to 0x03FF"))
                .defaultValue(false))
                .build()
        );
    public final Setting<Boolean> Str3 = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("Cyrillic"))
                .description("Range 0x0400 to 0x04FF"))
                .defaultValue(true))
                .build()
        );
    public final Setting<Boolean> Str4 = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("CJK Unified Ideographs"))
                .description("0x4E00 to 0x9FFF"))
                .defaultValue(true))
                .build()
        );
    public final Setting<Boolean> Str5 = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("Hangul Syllables"))
                .description("Range 0xAC00 to 0xD7AF"))
                .defaultValue(true))
                .build()
        );
    public final Setting<Boolean> Str6 = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("Alphabetic Presentation Forms"))
                .description("Range 0xFB00 to 0xFB4F"))
                .defaultValue(false))
                .build()
        );
    public final Setting<Boolean> Str7 = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("Halfwidth & Fullwidth Forms"))
                .description("Range 0xFF00 to 0xFF73"))
                .defaultValue(false))
                .build()
        );
    public final Setting<SettingColor> feedbackColor = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                .name("feedback-color"))
                .description("Color of the feedback text."))
                .defaultValue(new SettingColor(176, 224, 230))
                .build()
        );
    private final Setting<Integer> messageLength = this.sgGeneral
        .add(
            ((Builder) ((Builder) ((Builder) new Builder().name("message-length")).description("The length of the message.")).defaultValue(200))
                .min(1)
                .sliderMin(1)
                .sliderMax(1000)
                .build()
        );
    private final Setting<Integer> amount = this.sgGeneral
        .add(
            ((Builder) ((Builder) ((Builder) new Builder().name("message-amount")).description("The amount of the message.")).defaultValue(3))
                .min(1)
                .sliderMin(1)
                .sliderMax(10)
                .build()
        );
    private final Setting<Boolean> totemsIgnoreFriends = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("ignore-friends"))
                .description("Ignores friends totem pops."))
                .defaultValue(false))
                .build()
        );
    private final Setting<Boolean> totemsIgnoreOthers = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("ignore-others"))
                .description("Ignores other players totem pops."))
                .defaultValue(false))
                .build()
        );
    String msg = "none";

    public PopLag() {
        super(Compassion.ABNORMALLY, "PopLag", "They text lag me! ");
    }

    public static String generateRandomString(int length) {
        Random random = new Random();
        StringBuilder sb = new StringBuilder(length);
        int[][] validRanges = new int[][]{{1024, 1279}, {19968, 40959}, {44032, 55215}};

        for (int i = 0; i < length; i++) {
            int[] range = validRanges[random.nextInt(validRanges.length)];
            int randomCodePoint = random.nextInt(range[1] - range[0]) + range[0];
            sb.append((char) randomCodePoint);
        }

        return sb.toString();
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

    @EventHandler
    public void onReceiveMessage(ReceiveMessageEvent event) {
        StringBuilder builder = new StringBuilder();
        event.getMessage().asOrderedText().accept((i, style, codePoint) -> {
            builder.append(new String(Character.toChars(codePoint)));
            return true;
        });
        StringBuilder wrap = new StringBuilder();
        if (event.getMessage().contains(Text.of(this.msg))) {
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

    @EventHandler
    private void onReceivePacket(Receive event) {
        boolean[] list = new boolean[]{
            this.Str1.get(),
            this.Str2.get(),
            this.Str3.get(),
            this.Str4.get(),
            this.Str5.get(),
            this.Str6.get(),
            this.Str7.get()
        };
        if (event.packet instanceof EntityStatusS2CPacket p) {
            if (p.getStatus() == 35) {
                Entity entity = p.getEntity(this.mc.world);
                if (entity instanceof PlayerEntity) {
                    if (!entity.equals(this.mc.player)
                        && (!Friends.get().isFriend((PlayerEntity) entity) || !this.totemsIgnoreOthers.get())
                        && (Friends.get().isFriend((PlayerEntity) entity) || !this.totemsIgnoreFriends.get())) {
                        for (int i = 0; i < this.amount.get(); i++) {
                            this.msg = generateRandomString(this.messageLength.get(), list);
                            this.mc.player.networkHandler.sendChatCommand((String) this.command.get() + " " + entity.getName().getString() + " " + this.msg);
                        }
                    }
                }
            }
        }
    }
}
