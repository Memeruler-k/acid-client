package dev.abstr3act.addon.modules.Abnormally;

import dev.abstr3act.addon.Compassion;
import dev.abstr3act.addon.module.AbnormallyModule;
import dev.abstr3act.addon.utils.abnormally.AChatUtils;
import dev.abstr3act.addon.utils.xor.XORUtils;
import meteordevelopment.meteorclient.events.game.ReceiveMessageEvent;
import meteordevelopment.meteorclient.events.game.SendMessageEvent;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.StringSetting.Builder;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.HoverEvent.Action;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class ChatEncryption extends AbnormallyModule {
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
    public final Setting<String> prefix = this.sgGeneral
        .add(((Builder) ((Builder) ((Builder) new Builder().name("Prefix")).description("Prefix of chat encryption")).defaultValue("msg")).build());
    public final Setting<String> aeskey = this.sgGeneral
        .add(((Builder) ((Builder) ((Builder) new Builder().name("AES-Key")).description("Key of chat encryption")).defaultValue("50FA30D3092CD133")).build());
    public final Setting<SettingColor> feedbackColor = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                .name("feedback-color"))
                .description("Color of the feedback text."))
                .defaultValue(new SettingColor(176, 224, 230))
                .build()
        );

    public ChatEncryption() {
        super(Compassion.ABNORMALLY, "ChatEncryption", "IDK");
    }

    @EventHandler
    public void onMessageSend(SendMessageEvent event) {
        if (((String) this.prefix.get()).length() > 1) {
            AChatUtils.sendMsg("Prefix length cannot be larger than 1! ");
            this.toggle();
            event.cancel();
        }

        if (event.message.startsWith((String) this.prefix.get())) {
            event.message = (String) this.prefix.get() + Base64.getEncoder().encodeToString(XORUtils.encoding(event.message, (String) this.aeskey.get()));
        }
    }

    @EventHandler
    public void onReceiveMessage(ReceiveMessageEvent event) {
        String message = event.getMessage().getString();
        String[] parts = message.split("> ", 2);
        if (parts.length >= 2) {
            String playerName = parts[0].substring(parts[0].indexOf(60) + 1);
            String messageContent = parts[1];
            StringBuilder builder = new StringBuilder();
            event.getMessage().asOrderedText().accept((i, style, codePoint) -> {
                builder.append(new String(Character.toChars(codePoint)));
                return true;
            });
            StringBuilder wrap = new StringBuilder();
            if (messageContent.startsWith((String) this.prefix.get())) {
                try {
                    String r = XORUtils.removeNonAscii(XORUtils.removePrefix(messageContent));
                    String msg = new String(XORUtils.decoding(Base64.getDecoder().decode(r.getBytes()), (String) this.aeskey.get()), StandardCharsets.UTF_8);
                    event.setMessage(
                        Text.literal(" <" + playerName + "> ")
                            .append(msg)
                            .append(
                                Text.literal(" [Encrypted] ")
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
                } catch (Exception var10) {
                    event.setMessage(
                        Text.literal(" <" + playerName + "> ")
                            .append("Failed to decode message.")
                            .append(
                                Text.literal(" [Encrypted] ")
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
                }
            }
        }
    }
}
