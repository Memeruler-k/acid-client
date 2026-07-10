package dev.abstr3act.addon.modules.Compassion;

import dev.abstr3act.addon.Compassion;
import dev.abstr3act.addon.events.EventAttack;
import dev.abstr3act.addon.module.AbnormallyModule;
import dev.abstr3act.addon.utils.render.CaptureMark;
import meteordevelopment.meteorclient.events.packets.PacketEvent.Send;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent.Pre;
import meteordevelopment.meteorclient.mixininterface.IPlayerInteractEntityC2SPacket;
import meteordevelopment.meteorclient.settings.DoubleSetting.Builder;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.utils.entity.EntityUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;

import java.lang.reflect.Method;

public class UwUESP extends AbnormallyModule {
    public static final Identifier nami = Identifier.of("acid", "public/nami.png");
    public static final Identifier maya = Identifier.of("acid", "public/maya.png");
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
    private final Setting<Double> x = this.sgGeneral
        .add(((Builder) ((Builder) new Builder().name("X")).description(".")).defaultValue(0.0).sliderRange(-100.0, 100.0).build());
    private final Setting<Double> y = this.sgGeneral
        .add(((Builder) ((Builder) new Builder().name("Y")).description(".")).defaultValue(0.0).sliderRange(-100.0, 100.0).build());
    private final Setting<Double> z = this.sgGeneral
        .add(((Builder) ((Builder) new Builder().name("Z")).description(".")).defaultValue(0.0).sliderRange(-100.0, 100.0).build());
    private final Setting<Double> scale1 = this.sgGeneral
        .add(((Builder) ((Builder) new Builder().name("Scale1")).description(".")).defaultValue(1.5).sliderRange(0.0, 100.0).build());
    private final Setting<Double> scale2 = this.sgGeneral
        .add(((Builder) ((Builder) new Builder().name("Scale2")).description(".")).defaultValue(1.0).sliderRange(0.0, 100.0).build());
    Entity target;

    public UwUESP() {
        super(Compassion.COMPASSION, "UwUESP", "Renders entities through walls.");
    }

    @EventHandler
    private void onRender3D(Render3DEvent event) {
        if (this.target != null && this.shouldSkipAttack(this.target)) {
            if (this.target instanceof LivingEntity && ((LivingEntity) this.target).getHealth() + ((LivingEntity) this.target).getAbsorptionAmount() > 10.0F) {
                CaptureMark.renderUwU(
                    this.target,
                    this.x.get(),
                    this.y.get(),
                    this.z.get(),
                    nami,
                    (this.scale1.get()).floatValue(),
                    (this.scale2.get()).floatValue()
                );
            } else {
                CaptureMark.renderUwU(
                    this.target,
                    this.x.get(),
                    this.y.get(),
                    this.z.get(),
                    maya,
                    (this.scale1.get()).floatValue(),
                    (this.scale2.get()).floatValue()
                );
            }
        }
    }

    @EventHandler
    private void onTickEvent(Pre event) {
    }

    @EventHandler
    private void onAttack(EventAttack event) {
        if (event.getEntity() instanceof LivingEntity) {
            this.target = event.getEntity();
        }
    }

    @EventHandler
    private void onSendPacket(Send event) {
        if (event.packet instanceof IPlayerInteractEntityC2SPacket packet) {
            try {
                Class<?> packetClass = packet.getClass();
                Method getTypeMethod = packetClass.getDeclaredMethod("getType");
                getTypeMethod.setAccessible(true);
                Enum<?> interactType = (Enum<?>) getTypeMethod.invoke(packet);
                if (interactType.name().equals("ATTACK") && packet.getEntity() instanceof LivingEntity) {
                    this.target = (LivingEntity) packet.getEntity();
                }
            } catch (Exception var6) {
                var6.printStackTrace();
            }
        }
    }

    public boolean shouldSkipAttack(Entity e) {
        return e.isAlive() ? true : !EntityUtils.isInRenderDistance(e);
    }
}
