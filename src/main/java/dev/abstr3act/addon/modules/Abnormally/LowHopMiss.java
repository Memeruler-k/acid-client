package dev.abstr3act.addon.modules.Abnormally;

import dev.abstr3act.addon.Compassion;
import dev.abstr3act.addon.module.AbnormallyModule;
import meteordevelopment.meteorclient.events.packets.PacketEvent.Send;
import meteordevelopment.meteorclient.events.world.TickEvent.Post;
import meteordevelopment.meteorclient.events.world.TickEvent.Pre;
import meteordevelopment.meteorclient.mixininterface.IPlayerInteractEntityC2SPacket;
import meteordevelopment.meteorclient.mixininterface.IPlayerMoveC2SPacket;
import meteordevelopment.meteorclient.settings.EnumSetting.Builder;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket.InteractType;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket.PositionAndOnGround;

public class LowHopMiss extends AbnormallyModule {
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
    private final Setting<Mode> mode = this.sgGeneral
        .add(
            ((Builder) ((Builder) ((Builder) new Builder().name("mode")).description("The mode on how LowHopMiss will function."))
                .defaultValue(Mode.Velocity))
                .build()
        );
    private final Setting<Double> tpDistance = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("Teleport Distance"))
                .description("Set the teleport distance"))
                .defaultValue(0.5)
                .sliderRange(0.01, 3.0)
                .visible(() -> this.mode.get() == Mode.Teleport))
                .build()
        );
    private final Setting<Double> velocity = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("Velocity Power"))
                .description("Set the velocity power"))
                .defaultValue(0.1)
                .sliderRange(0.01, 5.0)
                .visible(() -> this.mode.get() == Mode.Velocity))
                .build()
        );
    private final Setting<Boolean> onGround = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("OnGround"))
                .description("Set player on ground or not while process"))
                .defaultValue(false))
                .build()
        );
    private final Setting<tpMode> TPMode = this.sgGeneral
        .add(
            ((Builder) ((Builder) ((Builder) new Builder().name("TPMode")).description("The mode on how LowHopMiss will function."))
                .defaultValue(tpMode.Normal))
                .build()
        );
    public boolean hopped = false;
    private PlayerInteractEntityC2SPacket attackPacket;
    private HandSwingC2SPacket swingPacket;
    private boolean sendPackets;
    private int sendTimer;

    public LowHopMiss() {
        super(Compassion.ABNORMALLY, "LowHopMiss", "Increase the chance of totem missing");
    }

    public void onActivate() {
        this.attackPacket = null;
        this.swingPacket = null;
        this.sendPackets = false;
        this.sendTimer = 0;
    }

    @EventHandler
    private void onSendPacket(Send event) {
        if (event.packet instanceof IPlayerInteractEntityC2SPacket packet && packet.getType() == InteractType.ATTACK) {
            if (this.skip()) {
                return;
            }

            Entity entity = packet.getEntity();
            if (!(entity instanceof LivingEntity)) {
                return;
            }

            if (this.mode.get() == Mode.Velocity) {
                this.lowHop(this.onGround.get(), this.velocity.get());
            } else if (this.mode.get() == Mode.Teleport) {
            }
        }
    }

    @EventHandler
    private void onTick(Pre event) {
        if (this.sendPackets) {
            if (this.sendTimer <= 0) {
                this.sendPackets = false;
                if (this.attackPacket == null || this.swingPacket == null) {
                    return;
                }

                this.mc.getNetworkHandler().sendPacket(this.attackPacket);
                this.mc.getNetworkHandler().sendPacket(this.swingPacket);
                this.attackPacket = null;
                this.swingPacket = null;
            } else {
                this.sendTimer--;
            }
        }
    }

    @EventHandler
    private void onPost(Post event) {
        if (this.hopped) {
            switch ((tpMode) this.TPMode.get()) {
                case Silent:
                    this.sendPacket(this.tpDistance.get());
                    break;
                case Normal:
                    this.mc.player.setPos(this.mc.player.getX(), this.mc.player.getY() + this.tpDistance.get(), this.mc.player.getZ());
            }

            this.hopped = false;
        } else {
            switch ((tpMode) this.TPMode.get()) {
                case Silent:
                    this.sendPacket(-this.tpDistance.get());
                    break;
                case Normal:
                    this.mc.player.setPos(this.mc.player.getX(), this.mc.player.getY() - this.tpDistance.get(), this.mc.player.getZ());
            }

            this.hopped = true;
        }
    }

    private void sendPacket(double height, boolean onGround) {
        double x = this.mc.player.getX();
        double y = this.mc.player.getY();
        double z = this.mc.player.getZ();
        PlayerMoveC2SPacket packet = new PositionAndOnGround(x, y + height, z, onGround);
        ((IPlayerMoveC2SPacket) packet).setTag(1337);
        this.mc.player.networkHandler.sendPacket(packet);
    }

    private void sendPacket(double height) {
        double x = this.mc.player.getX();
        double y = this.mc.player.getY();
        double z = this.mc.player.getZ();
        PlayerMoveC2SPacket packet = new PositionAndOnGround(x, y + height, z, false);
        ((IPlayerMoveC2SPacket) packet).setTag(1337);
        this.mc.player.networkHandler.sendPacket(packet);
    }

    private boolean skip() {
        return !this.mc.player.isOnGround() || this.mc.player.isSubmergedInWater() || this.mc.player.isInLava() || this.mc.player.isClimbing();
    }

    public String getInfoString() {
        return this.mc.player == null ? "intermediary" : String.valueOf(Math.round(this.mc.player.getVelocity().getY()));
    }

    private void lowHop(boolean onGround, double power) {
        this.mc.player.addVelocity(0.0, power, 0.0);
        this.mc.player.fallDistance = 0.1F;
        this.mc.player.setOnGround(onGround);
    }

    public static enum Mode {
        Velocity,
        Teleport;
    }

    public static enum tpMode {
        Silent,
        Normal;
    }
}
