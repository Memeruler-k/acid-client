package dev.abstr3act.addon.modules.Selena;

import dev.abstr3act.addon.Compassion;
import dev.abstr3act.addon.module.SelenaModule;
import meteordevelopment.meteorclient.events.world.TickEvent.Pre;
import meteordevelopment.meteorclient.settings.IntSetting.Builder;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.MovementType;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class GrimTridentFly extends SelenaModule {
    private final SettingGroup settingsModule = this.settings.getDefaultGroup();
    private final Setting<Integer> tridentDelay = this.settingsModule
        .add(
            ((Builder) ((Builder) ((Builder) new Builder().name("Delay")).description("delay (in ticks) between trident uses"))
                .sliderRange(0, 20)
                .range(0, 20)
                .defaultValue(0))
                .build()
        );
    private int currentTick = 0;
    private boolean isActive = false;

    public GrimTridentFly() {
        super(Compassion.SELENA, "GrimTridentFly", "Fly with Trident Spamming Riptide III. (Active GrimDisabler) [Patched]");
    }

    public void onActivate() {
        this.isActive = true;
    }

    public void onDeactivate() {
        this.isActive = false;
    }

    @EventHandler
    private void onTick(Pre event) {
        if (this.currentTick >= this.tridentDelay.get() && this.isActive) {
            this.currentTick = 0;

            assert this.mc.player != null;

            float f = this.mc.player.getYaw();
            float g = this.mc.player.getPitch();
            float h = -MathHelper.sin(f * (float) (Math.PI / 180.0)) * MathHelper.cos(g * (float) (Math.PI / 180.0));
            float k = -MathHelper.sin(g * (float) (Math.PI / 180.0));
            float l = MathHelper.cos(f * (float) (Math.PI / 180.0)) * MathHelper.cos(g * (float) (Math.PI / 180.0));
            float m = MathHelper.sqrt(h * h + k * k + l * l);
            float n = 3.0F;
            h *= n / m;
            k *= n / m;
            l *= n / m;
            this.mc.player.addVelocity(h, k, l);
            if (this.mc.player.isOnGround()) {
                this.mc.player.move(MovementType.SELF, new Vec3d(0.0, 1.1999999F, 0.0));
            }
        } else {
            this.currentTick++;
        }
    }
}
