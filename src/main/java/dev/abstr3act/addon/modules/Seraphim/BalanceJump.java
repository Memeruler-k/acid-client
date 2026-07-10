package dev.abstr3act.addon.modules.Seraphim;

import dev.abstr3act.addon.Compassion;
import dev.abstr3act.addon.module.SeraphimModule;
import meteordevelopment.meteorclient.events.entity.player.PlayerMoveEvent;
import meteordevelopment.meteorclient.events.world.TickEvent.Post;
import meteordevelopment.meteorclient.settings.DoubleSetting.Builder;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;

public class BalanceJump extends SeraphimModule {
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
    private final Setting<Double> range = this.sgGeneral.add(((Builder) ((Builder) new Builder().name("Velocity")).description("v")).defaultValue(5.0).build());
    private final Setting<Double> upVelocity = this.sgGeneral
        .add(((Builder) ((Builder) new Builder().name("Up Velocity")).description("uv")).defaultValue(5.0).build());
    private final Setting<Double> jumpFactor = this.sgGeneral
        .add(((Builder) ((Builder) new Builder().name("jumpFactor")).description("uv")).defaultValue(0.3).build());
    stage s = stage.JUMP_A;

    public BalanceJump() {
        super(Compassion.SERAPHIM, "BalanceJump", ".");
    }

    @EventHandler
    public void onTick(Post event) {
        if (this.mc.player != null) {
            if (this.mc.player.isOnGround()) {
                switch (this.s) {
                    case JUMP_A:
                        this.mc.player.setVelocity(0.0, this.jumpFactor.get(), 0.0);
                        this.s = stage.JUMP_B;
                        break;
                    case JUMP_B:
                        this.mc.player.jump();
                        this.s = stage.VELOCITY;
                        break;
                    case VELOCITY:
                        this.fly();
                        this.s = stage.JUMP_A;
                        this.toggle("Successfully released motion storage!");
                }
            }
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (this.s != stage.VELOCITY) {
            this.mc.player.setVelocity(0.0, this.mc.player.getVelocity().y, 0.0);
        }
    }

    private void fly() {
        PlayerEntity player = this.mc.player;
        float yaw = player.getYaw();
        float pitch = player.getPitch();
        double yawRadian = Math.toRadians(yaw);
        double pitchRadian = Math.toRadians(pitch);
        double x = -Math.sin(yawRadian) * Math.cos(pitchRadian);
        double y = -this.mc.player.getVelocity().y * this.upVelocity.get();
        double z = Math.cos(yawRadian) * Math.cos(pitchRadian);
        Vec3d direction = new Vec3d(x, y, z).normalize().multiply(this.range.get());
        player.setVelocity(direction);
        player.velocityDirty = true;
    }

    static enum stage {
        JUMP_A,
        JUMP_B,
        VELOCITY;
    }
}
