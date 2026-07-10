package dev.abstr3act.addon.modules.Abnormally;

import dev.abstr3act.addon.Compassion;
import dev.abstr3act.addon.module.AbnormallyModule;
import dev.abstr3act.addon.modules.Compassion.MaceKill;
import meteordevelopment.meteorclient.events.world.TickEvent.Pre;
import meteordevelopment.meteorclient.settings.KeybindSetting.Builder;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.misc.Keybind;
import meteordevelopment.orbit.EventHandler;

import java.util.List;

public class ExtraBind extends AbnormallyModule {
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
    public final Setting<List<Module>> acModules = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.ModuleListSetting.Builder) ((meteordevelopment.meteorclient.settings.ModuleListSetting.Builder) new meteordevelopment.meteorclient.settings.ModuleListSetting.Builder()
                .name("Modules"))
                .description("modules"))
                .defaultValue(new Class[]{MaceKill.class})
                .build()
        );
    private final Setting<Keybind> binds1 = this.sgGeneral
        .add(((Builder) ((Builder) ((Builder) new Builder().name("binds")).description("binds")).defaultValue(Keybind.none())).build());
    private final Setting<Keybind> binds2 = this.sgGeneral
        .add(((Builder) ((Builder) ((Builder) new Builder().name("binds")).description("binds")).defaultValue(Keybind.none())).build());
    private final Setting<Keybind> binds3 = this.sgGeneral
        .add(((Builder) ((Builder) ((Builder) new Builder().name("binds")).description("binds")).defaultValue(Keybind.none())).build());
    private final Setting<Keybind> binds4 = this.sgGeneral
        .add(((Builder) ((Builder) ((Builder) new Builder().name("binds")).description("binds")).defaultValue(Keybind.none())).build());

    public ExtraBind() {
        super(Compassion.ABNORMALLY, "ExtraBinds", "MultiBinds");
    }

    public void onActivate() {
    }

    public void onDeactivate() {
    }

    private boolean shouleEnable() {
        return ((Keybind) this.binds1.get()).isPressed()
            || ((Keybind) this.binds2.get()).isPressed()
            || ((Keybind) this.binds3.get()).isPressed()
            || ((Keybind) this.binds4.get()).isPressed();
    }

    @EventHandler
    public void onTickEvent(Pre event) {
        if (this.shouleEnable()) {
            for (Module module : this.acModules.get()) {
                module.toggle();
            }
        }
    }
}
