package dev.abstr3act.addon.modules.Compassion;

import dev.abstr3act.addon.Compassion;
import dev.abstr3act.addon.module.CompassionModule;
import meteordevelopment.meteorclient.settings.EnumSetting.Builder;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;

public class ClientSpoof extends CompassionModule {
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
    private final Setting<Mode> mode = this.sgGeneral
        .add(((Builder) ((Builder) ((Builder) new Builder().name("Client")).description(".")).defaultValue(Mode.Vanilla)).build());
    private final Setting<String> custom = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.StringSetting.Builder) ((meteordevelopment.meteorclient.settings.StringSetting.Builder) ((meteordevelopment.meteorclient.settings.StringSetting.Builder) ((meteordevelopment.meteorclient.settings.StringSetting.Builder) new meteordevelopment.meteorclient.settings.StringSetting.Builder()
                .name("Custom"))
                .description("."))
                .defaultValue("cheatbreaker"))
                .visible(() -> ((Mode) this.mode.get()).equals(Mode.Custom)))
                .build()
        );

    public ClientSpoof() {
        super(Compassion.COMPASSION, "ClientSpoof", ".");
    }

    public String getClientName() {
        switch ((Mode) this.mode.get()) {
            case Vanilla:
                return "vanilla";
            case Lunar1_20_4:
                return "lunarclient:1.20.4";
            case Lunar1_20_1:
                return "lunarclient:1.20.1";
            case Lunar1_21_1:
                return "lunarclient:1.21.1";
            case Custom:
                return (String) this.custom.get();
            default:
                return null;
        }
    }

    public static enum Mode {
        Vanilla,
        Lunar1_20_4,
        Lunar1_20_1,
        Lunar1_21_1,
        Custom,
        Null;
    }
}
