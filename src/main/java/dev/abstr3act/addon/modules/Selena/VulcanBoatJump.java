package dev.abstr3act.addon.modules.Selena;

import dev.abstr3act.addon.Compassion;
import dev.abstr3act.addon.module.SelenaModule;
import meteordevelopment.meteorclient.events.entity.BoatMoveEvent;
import meteordevelopment.meteorclient.events.world.TickEvent.Post;
import meteordevelopment.meteorclient.events.world.TickEvent.Pre;
import meteordevelopment.meteorclient.mixininterface.IVec3d;
import meteordevelopment.meteorclient.settings.DoubleSetting.Builder;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.world.Timer;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;

public class VulcanBoatJump extends SelenaModule {
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
    private final Setting<Double> range = this.sgGeneral.add(((Builder) ((Builder) new Builder().name("Velocity")).description("v")).defaultValue(5.0).build());
    private final Setting<Double> upVeloity = this.sgGeneral
        .add(((Builder) ((Builder) new Builder().name("Up Velocity")).description("uv")).defaultValue(5.0).build());
    boolean start = true;
    private int verticalMoveCooldown = 0;

    public VulcanBoatJump() {
        super(Compassion.SELENA, "VulcanBoatJump", "Teleports you to flyyyyyyyyy!");
    }

    public void onDeactivate() {
        this.start = true;
        ((Timer) Modules.get().get(Timer.class)).setOverride(1.0);
    }

    public void onActivate() {
        this.start = true;
    }

    @EventHandler
    private void onTick(Pre event) {
        if (this.mc.player.isRiding() && this.isBoat(this.mc.player)) {
            this.start = false;
        }

        if (!this.mc.player.isRiding() && !this.start) {
            this.fly(this.range.get());
            this.toggle();
        }
    }

    private void fly(double speed) {
        PlayerEntity player = this.mc.player;
        float yaw = player.getYaw();
        double radian = Math.toRadians(yaw);
        Vec3d direction = new Vec3d(-Math.sin(radian), this.upVeloity.get(), Math.cos(radian)).normalize().multiply(this.range.get());
        player.setVelocity(direction);
        player.velocityDirty = true;
    }

    public boolean isBoat(PlayerEntity player) {
        return player.getVehicle() instanceof BoatEntity;
    }

    @EventHandler
    private void onTick(Post event) {
        if (this.verticalMoveCooldown > 0) {
            this.verticalMoveCooldown--;
        }

        if (this.mc.world == null || this.mc.player.getVehicle() == null || this.mc.player.getVehicle().getControllingPassenger() != this.mc.player) {
            ;
        }
    }

    @EventHandler
    private void onBoatMove(BoatMoveEvent event) {
        if (event.boat.getControllingPassenger() == this.mc.player) {
            boolean useTimer = this.mc.player.input.getMovementInput().lengthSquared() != 0.0F
                && !this.mc.options.jumpKey.isPressed()
                && !this.mc.options.sprintKey.isPressed();
            ((Timer) Modules.get().get(Timer.class)).setOverride(useTimer ? 0.5 : 1.0);
            long t = this.mc.world.getTime();
            event.boat.setYaw(this.mc.player.getYaw());
            double velY = 0.1;
            Vec3d boatPosAfter = event.boat.getPos().add(0.0, velY, 0.0);
            ChunkPos cp = new ChunkPos(new BlockPos((int) boatPosAfter.x, (int) boatPosAfter.y, (int) boatPosAfter.z));
            if (this.mc.world.getChunkManager().isChunkLoaded(cp.x, cp.z)) {
                ((IVec3d) event.boat.getVelocity()).set(0.0, velY, 0.0);
            }
        }
    }

    public void dismountFromBoat(PlayerEntity player, BoatEntity boat) {
        Vec3d boatPosition = boat.getPos();
        BlockPos targetPosition = new BlockPos((int) boatPosition.x, (int) boatPosition.y, (int) boatPosition.z);
        player.stopRiding();
        player.requestTeleport(targetPosition.getX(), targetPosition.getY(), targetPosition.getZ());
    }
}
