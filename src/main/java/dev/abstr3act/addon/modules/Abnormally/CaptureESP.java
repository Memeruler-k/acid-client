package dev.abstr3act.addon.modules.Abnormally;

import dev.abstr3act.addon.Compassion;
import dev.abstr3act.addon.events.EventAttack;
import dev.abstr3act.addon.module.AbnormallyModule;
import dev.abstr3act.addon.utils.render.CaptureMark;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent.Pre;
import meteordevelopment.meteorclient.settings.EnumSetting.Builder;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.utils.entity.EntityUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;

public class CaptureESP extends AbnormallyModule {
    public static Entity target;
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
    private final Setting<CaptureMark.Captures> mode = this.sgGeneral
        .add(((Builder) ((Builder) ((Builder) new Builder().name("Mode")).description(".")).defaultValue(CaptureMark.Captures.CAPTURE)).build());

    public CaptureESP() {
        super(Compassion.ABNORMALLY, "Capture", "Renders entities through walls.");
    }

    @EventHandler
    private void onRender3D(Render3DEvent event) {
        if (target != null && this.shouldSkipAttack(target)) {
            CaptureMark.render(target, (CaptureMark.Captures) this.mode.get());
        }
    }

    @EventHandler
    private void onTickEvent(Pre event) {
        CaptureMark.tick();
    }

    @EventHandler
    private void onAttack(EventAttack event) {
        if (event.getEntity() instanceof LivingEntity) {
            target = event.getEntity();
        }
    }

    public boolean shouldSkipAttack(Entity e) {
        return e.isAlive() ? true : !EntityUtils.isInRenderDistance(e);
    }
}
