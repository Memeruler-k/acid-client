package dev.abstr3act.addon.modules.Compassion;

import dev.abstr3act.addon.Compassion;
import dev.abstr3act.addon.module.AbnormallyModule;
import meteordevelopment.meteorclient.events.game.SendMessageEvent;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.StringListSetting.Builder;
import meteordevelopment.orbit.EventHandler;

import java.util.List;

public class RandomSuffix extends AbnormallyModule {
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
    private final Setting<List<String>> v = this.sgGeneral
        .add(
            ((Builder) ((Builder) new Builder().name("Value")).description("."))
                .defaultValue(
                    new String[]{
                        " | ✡ Compassion",
                        " | ♡ Testify[N]",
                        " | ◁| Abnormally |▷",
                        " | TestifyX",
                        "ℜ\ud835\udd22\ud835\udd1f\ud835\udd26\ud835\udd2f\ud835\udd31\ud835\udd25",
                        "\ud835\udd10\ud835\udd22\ud835\udd29\ud835\udd2c\ud835\udd2b\ud835\udd05\ud835\udd22\ud835\udd31\ud835\udd1e",
                        "\ud835\udde0\ud835\uddf6\ud835\uddfc"
                    }
                )
                .build()
        );

    public RandomSuffix() {
        super(Compassion.COMPASSION, "RandomSuffix", "Prevent your message to block by server");
    }

    public void onActivate() {
    }

    public void onDeactivate() {
    }

    @EventHandler
    public void onMessageSend(SendMessageEvent event) {
        String value = event.message;
        int i = (int) (Math.random() * (this.v.get()).size());
        event.message = value + (String) (this.v.get()).get(i);
    }
}
