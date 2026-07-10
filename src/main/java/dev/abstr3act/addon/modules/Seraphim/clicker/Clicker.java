package dev.abstr3act.addon.modules.Seraphim.clicker;

import dev.abstr3act.addon.Compassion;
import dev.abstr3act.addon.utils.math.MathUtility;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.settings.IntSetting.Builder;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.misc.CPSUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.hit.BlockHitResult;
import org.lwjgl.glfw.GLFW;

public class Clicker extends Module {
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
    private final Setting<Integer> delay = this.sgGeneral
        .add(((Builder) ((Builder) ((Builder) new Builder().name("Delay")).description(".")).defaultValue(600)).min(0).sliderRange(0, 10000).build());
    private final Setting<Boolean> randomize = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("Randomize"))
                .description("."))
                .defaultValue(true))
                .build()
        );
    private final Setting<Integer> randomizeValue = this.sgGeneral
        .add(((Builder) ((Builder) ((Builder) new Builder().name("Value")).description(".")).defaultValue(600)).min(0).sliderRange(0, 10000).build());
    private final Setting<Mode> mode = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) new meteordevelopment.meteorclient.settings.EnumSetting.Builder()
                .name("Mode"))
                .description("."))
                .defaultValue(Mode.Left))
                .build()
        );
    private final Setting<Boolean> legit = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("Legit"))
                .description("."))
                .defaultValue(true))
                .build()
        );
    private final Setting<Boolean> onlyPressed = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("OnlyPressed"))
                .description("."))
                .defaultValue(true))
                .build()
        );
    private final Setting<Boolean> notOnBlocks = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("NotOnBlocks"))
                .description("."))
                .defaultValue(true))
                .build()
        );
    private final Timer timer = new Timer();

    public Clicker() {
        super(Compassion.SERAPHIM, "Clicker", ".");
    }

    @EventHandler
    public void onUpdate(Render3DEvent event) {
        if (!(this.mc.crosshairTarget instanceof BlockHitResult) || !this.notOnBlocks.get() || !((Mode) this.mode.get()).equals(Mode.Left)) {
            long window = MinecraftClient.getInstance().getWindow().getHandle();
            if (!((Mode) this.mode.get()).equals(Mode.Left) || GLFW.glfwGetMouseButton(window, 0) == 1 || !this.onlyPressed.get()) {
                if (!((Mode) this.mode.get()).equals(Mode.Right) || this.mc.options.useKey.isPressed() || !this.onlyPressed.get()) {
                    if (this.timer
                        .every(
                            (long) (
                                (this.delay.get()).intValue()
                                    + (this.randomize.get() ? MathUtility.random(0.0F, (float) (this.randomizeValue.get()).intValue()) : 0.0F)
                            )
                        )) {
                        if (this.mode.get() == Mode.Left) {
                            Utils.leftClick();
                        } else {
                            Utils.rightClick();
                        }
                    }
                }
            }
        }
    }

    public String getInfoString() {
        return String.valueOf(CPSUtils.getCpsAverage());
    }

    private static enum Mode {
        Right,
        Left;
    }
}
