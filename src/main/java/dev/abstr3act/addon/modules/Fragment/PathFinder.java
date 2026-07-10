package dev.abstr3act.addon.modules.Fragment;

import dev.abstr3act.addon.Compassion;
import dev.abstr3act.addon.module.LacrymiraModule;
import meteordevelopment.meteorclient.settings.EnumSetting.Builder;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;

public class PathFinder extends LacrymiraModule {
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
    public final Setting<Mode> mode = this.sgGeneral
        .add(((Builder) ((Builder) ((Builder) new Builder().name("Mode")).description(".")).defaultValue(Mode.Linear)).build());
    public final Setting<Integer> linearSteps = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("Linear Steps"))
                .description("."))
                .sliderRange(1, 10)
                .visible(() -> ((Mode) this.mode.get()).equals(Mode.Linear)))
                .build()
        );

    public PathFinder() {
        super(Compassion.LACRYMIRA, "PathFinder", ".");
    }

    public String getInfoString() {
        return ((Mode) this.mode.get()).name;
    }

    public void onDeactivate() {
    }

    public void onActivate() {
    }

    public static enum Mode {
        Reconstruct("Reconstruct"),
        Linear("Linear");

        public final String name;

        private Mode(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return this.name;
        }
    }
}
