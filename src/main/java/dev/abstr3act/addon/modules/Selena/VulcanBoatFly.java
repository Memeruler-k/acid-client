package dev.abstr3act.addon.modules.Selena;

import dev.abstr3act.addon.Compassion;
import dev.abstr3act.addon.module.SelenaModule;
import meteordevelopment.meteorclient.events.entity.BoatMoveEvent;
import meteordevelopment.meteorclient.events.world.TickEvent.Pre;
import meteordevelopment.meteorclient.mixininterface.IVec3d;
import meteordevelopment.meteorclient.settings.DoubleSetting.Builder;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.world.Timer;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;

public class VulcanBoatFly extends SelenaModule {
    private static final double FALL_SPEED = 1.01;
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
    public final Setting<Double> timer = this.sgGeneral
        .add(((Builder) ((Builder) new Builder().name("timer")).description("Timer override.")).defaultValue(5.0).range(1.0, 5.0).sliderRange(1.0, 5.0).build());
    private final Setting<Double> speed = this.sgGeneral
        .add(
            ((Builder) ((Builder) new Builder().name("speed")).description("Horizontal speed in blocks per second."))
                .defaultValue(20.0)
                .range(0.0, 79.0)
                .sliderMax(79.0)
                .build()
        );
    private final Setting<Double> upwardSpeed = this.sgGeneral
        .add(
            ((Builder) ((Builder) new Builder().name("upward-speed-multiplier")).description("Upward speed multiplier."))
                .defaultValue(48.0)
                .range(5.0, 48.0)
                .sliderMax(48.0)
                .build()
        );
    private final Setting<Double> downwardSpeed = this.sgGeneral
        .add(
            ((Builder) ((Builder) new Builder().name("downward-speed")).description("Downward speed in blocks per second."))
                .defaultValue(100.0)
                .range(0.0, 100.0)
                .sliderMax(100.0)
                .build()
        );
    private int verticalMoveCooldown = 0;

    public VulcanBoatFly() {
        super(Compassion.SELENA, "VulcanBoatFlight", "395 bps with max settings guaranteed without flags");
    }

    public void onDeactivate() {
        ((Timer) Modules.get().get(Timer.class)).setOverride(1.0);
    }

    private boolean moveHorizontally(double amount) {
        Box boundingBox = this.mc.player.getBoundingBox().union(this.mc.player.getVehicle().getBoundingBox());
        boundingBox = boundingBox.offset(0.0, amount, 0.0).union(boundingBox);
        if (this.mc.world.getBlockCollisions(null, boundingBox).iterator().hasNext()) {
            return false;
        } else {
            this.mc.player.getVehicle().setPosition(this.mc.player.getVehicle().getPos().add(0.0, amount, 0.0));
            return true;
        }
    }

    @EventHandler
    private void onTick(Pre event) {
        if (this.verticalMoveCooldown > 0) {
            this.verticalMoveCooldown--;
        }

        if (this.mc.world != null && this.mc.player.getVehicle() != null && this.mc.player.getVehicle().getControllingPassenger() == this.mc.player) {
            long t = this.mc.world.getTime();
            if (t % 10L == 2L) {
                this.moveHorizontally(0.505);
            }

            float multiplier = this.verticalMoveCooldown > 0 ? 11.0F : 1.0F;
            this.moveHorizontally(-0.0505 * multiplier);
        }
    }

    @EventHandler
    private void onBoatMove(BoatMoveEvent event) {
        if (event.boat.getControllingPassenger() == this.mc.player) {
            boolean useTimer = this.mc.player.input.getMovementInput().lengthSquared() != 0.0F
                && !this.mc.options.jumpKey.isPressed()
                && !this.mc.options.sprintKey.isPressed();
            ((Timer) Modules.get().get(Timer.class)).setOverride(useTimer ? this.timer.get() : 1.0);
            long t = this.mc.world.getTime();
            event.boat.setYaw(this.mc.player.getYaw());
            Vec3d vel = PlayerUtils.getHorizontalVelocity(this.speed.get() * 5.0);
            double velX = t % 5L == 0L ? vel.getX() : 0.0;
            double velY = 0.0;
            double velZ = t % 5L == 0L ? vel.getZ() : 0.0;
            if (this.mc.options.jumpKey.isPressed() && this.verticalMoveCooldown <= 0 && t % 5L != 0L) {
                velY += this.upwardSpeed.get() / 2.5;
                this.verticalMoveCooldown = 8;
            }

            if (this.mc.options.sprintKey.isPressed() && t % 5L != 0L) {
                velY -= this.downwardSpeed.get() / 20.0 * 1.2;
            }

            Vec3d boatPosAfter = event.boat.getPos().add(velX, velY, velZ);
            ChunkPos cp = new ChunkPos(new BlockPos((int) boatPosAfter.x, (int) boatPosAfter.y, (int) boatPosAfter.z));
            if (this.mc.world.getChunkManager().isChunkLoaded(cp.x, cp.z)) {
                ((IVec3d) event.boat.getVelocity()).set(velX, velY, velZ);
            }
        }
    }
}
