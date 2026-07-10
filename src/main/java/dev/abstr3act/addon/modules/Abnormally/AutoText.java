package dev.abstr3act.addon.modules.Abnormally;

import dev.abstr3act.addon.Compassion;
import dev.abstr3act.addon.module.AbnormallyModule;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.StringSetting.Builder;
import meteordevelopment.meteorclient.utils.player.ChatUtils;

public class AutoText extends AbnormallyModule {
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
    public final Setting<String> command = this.sgGeneral
        .add(((Builder) ((Builder) ((Builder) new Builder().name("Text")).description("Text you want to send")).defaultValue("msg")).build());

    public AutoText() {
        super(Compassion.ABNORMALLY, "AutoText", "Send text");
    }

    public void onActivate() {
        ChatUtils.sendPlayerMsg((String) this.command.get());
        this.toggle();
    }
}
