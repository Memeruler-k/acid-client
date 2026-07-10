package dev.abstr3act.addon.modules.Lacrymira;

import dev.abstr3act.addon.Compassion;
import dev.abstr3act.addon.module.LacrymiraModule;
import meteordevelopment.meteorclient.settings.EnumSetting.Builder;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;

public class HUDSetting extends LacrymiraModule {
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
    public final Setting<bg> background = this.sgGeneral
        .add(((Builder) ((Builder) ((Builder) new Builder().name("Background")).description("bg")).defaultValue(bg.h)).build());
    public final Setting<Double> globalFactor = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("GlobalFactor"))
                .description("bg"))
                .range(0.1, 10.0)
                .defaultValue(1.0)
                .build()
        );

    public HUDSetting() {
        super(Compassion.CLIENT, "GUISetting", "Setting");
    }

    public static enum bg {
        a("bg.png", "Insight (Cut)", 1640, 984, 1.4F),
        b("bg_2.png", "Insight", 1920, 1080, 1.2F),
        c("bg_3.png", "Lagrange", 2777, 1659, 1.0F),
        d("bg_4.png", "Yuuki", 4041, 2217, 0.7F),
        e("bg_5.png", "GenShin", 3840, 2040, 0.7F),
        f("bg_6.png", "Arknights", 4089, 2300, 0.6F),
        g("bg_7.png", "GameOverGirl", 4631, 2417, 0.6F),
        h("106061930_p0.png", "Tairitsu & Hikari", 2000, 1405, 1.2F),
        i("117577427_p0.png", "AI Chan", 1466, 2096, 1.5F),
        j("124746220_p0.png", "Compassion", 1558, 2200, 1.45F),
        k("125607830_p0.png", "Kou (Winter)", 3116, 4440, 0.8F),
        l("bg_8.png", "ELBE", 4000, 2250, 0.7F),
        m("acid_1.png", "Acid_1", 2894, 5044, 0.67F),
        n("acid_2.png", "Acid_2", 2894, 5044, 0.67F),
        o("acid_3.png", "Acid_3", 1539, 2048, 1.27F);

        final float targetWidth = 1920.0F;
        private final String icons;
        private final String name;
        private final float scaledHeight;
        private final float scaleFactor;
        private final int width;
        private final int height;

        private bg(String icons, String name, int width, int height, float scaleFactor) {
            this.icons = icons;
            this.name = name;
            this.height = height;
            this.width = width;
            this.scaleFactor = scaleFactor;
            this.scaledHeight = height * (1920.0F / height);
        }

        @Override
        public String toString() {
            return this.name;
        }

        public String getName() {
            return this.icons;
        }

        public int getWidth() {
            return this.width;
        }

        public int getHeight() {
            return this.height;
        }

        public int getScaleFactor() {
            return (int) (1920.0F / this.width);
        }

        public int getScaledHeight() {
            return (int) this.scaledHeight;
        }

        public float getScale() {
            return this.scaleFactor;
        }

        public int getScaledWidth() {
            return 1920;
        }
    }
}
