package dev.abstr3act.addon.modules.Amrita;

import dev.abstr3act.addon.Compassion;
import dev.abstr3act.addon.module.CompassionModule;
import meteordevelopment.meteorclient.events.game.ReceiveMessageEvent;
import meteordevelopment.meteorclient.settings.BoolSetting.Builder;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.orbit.EventHandler;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public final class DupeResolver extends CompassionModule {
    private static final File FILE = new File("D:/dupe_log.txt");
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
    private final Setting<Boolean> client = this.sgGeneral
        .add(((Builder) ((Builder) ((Builder) new Builder().name("Client")).description(".")).defaultValue(true)).build());
    private final Setting<Integer> delay = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("Delay"))
                .description("."))
                .defaultValue(1))
                .min(0)
                .max(2000)
                .build()
        );
    int i;
    int j;

    public DupeResolver() {
        super(Compassion.AMRITA, "DupeResolver", ".");
    }

    @EventHandler
    public void onReceive(ReceiveMessageEvent event) {
        if (event.getMessage().getString().toLowerCase().contains("won")) {
            this.writeToFile("1");
        }

        if (event.getMessage().getString().toLowerCase().contains("lost")) {
            this.writeToFile("intermediary");
        }
    }

    private void writeToFile(String value) {
        try (FileWriter writer = new FileWriter(FILE, true)) {
            writer.write(value + "\n");
        } catch (IOException var7) {
            var7.printStackTrace();
        }
    }

    public void onDeactivate() {
    }
}
