package dev.abstr3act.addon.modules.Seraphim;

import dev.abstr3act.addon.Compassion;
import dev.abstr3act.addon.module.SeraphimModule;
import meteordevelopment.meteorclient.events.game.ReceiveMessageEvent;
import meteordevelopment.meteorclient.events.packets.PacketEvent.Receive;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.StringSetting.Builder;
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

public class OPPopLag extends SeraphimModule {
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
    public final Setting<String> message = this.sgGeneral
        .add(
            ((Builder) ((Builder) ((Builder) new Builder().name("Command")).description("Command for send private message"))
                .defaultValue(
                    "&k操你妈操你妈操你妈操你妈操你妈操你妈操你妈操你妈操你妈操你妈操你妈操你妈操你妈操你妈操你妈操你妈操你妈操你妈操你妈操你妈操你妈操你妈操你妈操你妈操你妈操你妈操你妈操你妈操你妈操你妈操你妈操你妈操你妈操你妈操你妈操你妈操你妈操你妈操你妈操你妈操你妈操你妈操你妈操你妈操你妈操你妈操你妈操你妈操你妈操你妈操你妈操你妈操你妈操你妈操你妈操你妈操你妈操你妈操你妈操你妈操你妈操你妈操你妈"
                ))
                .build()
        );
    public final Setting<String> replaceString = this.sgGeneral
        .add(((Builder) ((Builder) ((Builder) new Builder().name("ReplaceString")).description("Command for send private message")).defaultValue("操你妈")).build());
    public final Setting<SettingColor> feedbackColor = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                .name("feedback-color"))
                .description("Color of the feedback text."))
                .defaultValue(new SettingColor(176, 224, 230))
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

    public OPPopLag() {
        super(Compassion.SERAPHIM, "OPPopLag", "They text lag me! ");
    }

    @EventHandler
    public void onReceiveMessage(ReceiveMessageEvent event) {
        StringBuilder builder = new StringBuilder();
        event.getMessage().asOrderedText().accept((i, style, codePoint) -> {
            builder.append(new String(Character.toChars(codePoint)));
            return true;
        });
        StringBuilder wrap = new StringBuilder();
        if (event.getMessage().getString().contains((CharSequence) this.replaceString.get())) {
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
        if (event.packet instanceof EntityStatusS2CPacket p) {
            if (p.getStatus() == 35) {
                Entity entity = p.getEntity(this.mc.world);
                if (entity instanceof PlayerEntity) {
                    if (!entity.equals(this.mc.player)
                        && (!Friends.get().isFriend((PlayerEntity) entity) || !this.totemsIgnoreOthers.get())
                        && (Friends.get().isFriend((PlayerEntity) entity) || !this.totemsIgnoreFriends.get())) {
                        this.mc.player.networkHandler.sendChatMessage((String) this.message.get());
                    }
                }
            }
        }
    }
}
