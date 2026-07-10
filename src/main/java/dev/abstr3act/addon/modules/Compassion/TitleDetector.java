package dev.abstr3act.addon.modules.Compassion;

import dev.abstr3act.addon.Compassion;
import dev.abstr3act.addon.module.CompassionModule;
import dev.abstr3act.addon.notifications.Notification;
import dev.abstr3act.addon.notifications.NotificationsManager;
import meteordevelopment.meteorclient.events.packets.PacketEvent.Receive;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.StringSetting.Builder;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class TitleDetector extends CompassionModule {
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
    public final Setting<String> targetString = this.sgGeneral
        .add(((Builder) ((Builder) ((Builder) new Builder().name("TargetString")).description("Key of chat encryption")).defaultValue("")).build());
    private final Setting<Boolean> logout = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("Logout"))
                .description("."))
                .defaultValue(false))
                .build()
        );
    private final Setting<Boolean> sendMsg = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("Reply"))
                .description("."))
                .defaultValue(false))
                .build()
        );
    private final Setting<Boolean> send = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("Send Original"))
                .description("."))
                .defaultValue(false))
                .visible(this.sendMsg::get))
                .build()
        );
    public final Setting<String> msg = this.sgGeneral
        .add(
            ((Builder) ((Builder) ((Builder) ((Builder) new Builder().name("Message")).description(".")).defaultValue(""))
                .visible(() -> this.sendMsg.get() && !this.send.get()))
                .build()
        );
    private final Setting<Boolean> notification = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("Notification"))
                .description("."))
                .defaultValue(false))
                .build()
        );
    private final Setting<Boolean> cancel = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("Cancel"))
                .description("."))
                .defaultValue(false))
                .build()
        );

    public TitleDetector() {
        super(Compassion.COMPASSION, "TitleDetector", "idk");
    }

    @EventHandler
    private void onPacketSent(Receive event) {
        if (event.packet instanceof TitleS2CPacket) {
            if (this.sendMsg.get() && !this.send.get()) {
                ChatUtils.sendPlayerMsg((String) this.msg.get());
            }

            if (this.sendMsg.get() && this.send.get()) {
                ChatUtils.sendPlayerMsg(((TitleS2CPacket) event.packet).text().getString());
            }

            if (this.notification.get()) {
                NotificationsManager.add(new Notification("Title", ((TitleS2CPacket) event.packet).text().getString()));
            }

            if (this.logout.get()) {
                this.mc.player.networkHandler.getConnection().disconnect(Text.of(Formatting.RED + "Disconnect due to title packet received"));
            }

            if (this.cancel.get()) {
                event.cancel();
            }
        }
    }
}
