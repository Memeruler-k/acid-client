package dev.abstr3act.addon.modules.Seraphim;

import dev.abstr3act.addon.Compassion;
import dev.abstr3act.addon.events.EventOffGroundSpeed;
import dev.abstr3act.addon.module.SeraphimModule;
import meteordevelopment.meteorclient.events.world.TickEvent.Post;
import meteordevelopment.meteorclient.settings.DoubleSetting.Builder;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public class Glide extends SeraphimModule {
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
    public final Setting<Double> fallSpeed = this.sgGeneral
        .add(((Builder) ((Builder) new Builder().name("fall-speed")).description("Fall speed.")).defaultValue(0.125).min(0.005).sliderRange(0.005, 0.25).build());
    public final Setting<Double> moveSpeed = this.sgGeneral
        .add(
            ((Builder) ((Builder) new Builder().name("move-speed")).description("Horizontal movement factor."))
                .defaultValue(1.2)
                .min(1.0)
                .sliderRange(1.0, 5.0)
                .build()
        );
    public final Setting<Double> minHeight = this.sgGeneral
        .add(
            ((Builder) ((Builder) new Builder().name("min-height")).description("Won't glide when you are too close to the ground."))
                .defaultValue(0.0)
                .min(0.0)
                .sliderRange(0.0, 2.0)
                .build()
        );

    public Glide() {
        super(Compassion.SERAPHIM, "Glide", ".");
    }

    @EventHandler
    private void onTick(Post event) {
        ClientPlayerEntity player = this.mc.player;
        Vec3d v = player.getVelocity();
        if (!player.isOnGround() && !player.isTouchingWater() && !player.isInLava() && !player.isClimbing() && !(v.y >= 0.0)) {
            if (this.minHeight.get() > 0.0) {
                Box box = player.getBoundingBox();
                box = box.union(box.offset(0.0, -this.minHeight.get(), 0.0));
                if (!this.mc.world.isSpaceEmpty(box)) {
                    return;
                }
            }

            player.setVelocity(v.x, Math.max(v.y, -this.fallSpeed.get()), v.z);
        }
    }

    @EventHandler
    private void onOffGroundSpeed(EventOffGroundSpeed event) {
        event.speed = event.speed * (this.moveSpeed.get()).floatValue();
    }
}
