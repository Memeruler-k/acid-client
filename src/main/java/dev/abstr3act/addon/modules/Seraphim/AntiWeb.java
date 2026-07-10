package dev.abstr3act.addon.modules.Seraphim;

import dev.abstr3act.addon.Compassion;
import dev.abstr3act.addon.events.EventCollision;
import dev.abstr3act.addon.events.EventPlayerUpdate;
import dev.abstr3act.addon.module.SeraphimModule;
import dev.abstr3act.addon.utils.MovementUtility;
import meteordevelopment.meteorclient.settings.EnumSetting.Builder;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Blocks;
import net.minecraft.block.CobwebBlock;

public class AntiWeb extends SeraphimModule {
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
    public final Setting<Mode> mode = this.sgGeneral
        .add(((Builder) ((Builder) ((Builder) new Builder().name("Mode")).description(".")).defaultValue(Mode.Solid)).build());
    public final Setting<Boolean> grim = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("Grim"))
                .description("."))
                .defaultValue(false))
                .visible(() -> ((Mode) this.mode.get()).equals(Mode.Solid)))
                .build()
        );
    public final Setting<Double> speed = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("Speed"))
                .description("."))
                .defaultValue(0.3F)
                .range(0.0, 10.0)
                .visible(() -> ((Mode) this.mode.get()).equals(Mode.Fly)))
                .build()
        );

    public AntiWeb() {
        super(Compassion.SERAPHIM, "AntiWeb", ".");
    }

    @EventHandler
    public void onPlayerUpdate(EventPlayerUpdate e) {
        if (Compassion.playerManager.isInWeb() && this.mode.get() == Mode.Fly) {
            double[] dir = MovementUtility.forward(this.speed.get());
            this.mc.player.setVelocity(dir[0], 0.0, dir[1]);
            if (this.mc.options.jumpKey.isPressed()) {
                this.mc.player.setVelocity(this.mc.player.getVelocity().add(0.0, this.speed.get(), 0.0));
            }

            if (this.mc.options.sneakKey.isPressed()) {
                this.mc.player.setVelocity(this.mc.player.getVelocity().add(0.0, -this.speed.get(), 0.0));
            }
        }
    }

    @EventHandler
    public void onCollide(EventCollision e) {
        if (e.getState().getBlock() instanceof CobwebBlock && this.mode.get() == Mode.Solid) {
            e.setState(Blocks.DIRT.getDefaultState());
        }
    }

    public static enum Mode {
        Solid,
        Ignore,
        Fly;
    }
}
