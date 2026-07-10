package dev.abstr3act.addon.modules.Seraphim;

import dev.abstr3act.addon.Compassion;
import dev.abstr3act.addon.module.SeraphimModule;
import meteordevelopment.meteorclient.events.packets.PacketEvent.Send;
import meteordevelopment.meteorclient.events.packets.PacketEvent.Sent;
import meteordevelopment.meteorclient.events.world.TickEvent.Post;
import meteordevelopment.meteorclient.mixininterface.IPlayerInteractEntityC2SPacket;
import meteordevelopment.meteorclient.settings.EnumSetting.Builder;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket.InteractType;

public class NewSprint extends SeraphimModule {
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
    public final Setting<Mode> mode = this.sgGeneral
        .add(((Builder) ((Builder) ((Builder) new Builder().name("speed-mode")).description("What mode of sprinting.")).defaultValue(Mode.Strict)).build());
    public final Setting<Boolean> jumpFix = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("jump-fix"))
                .description("Whether to correct jumping directions."))
                .defaultValue(true))
                .visible(() -> this.mode.get() == Mode.Rage))
                .build()
        );
    private final Setting<Boolean> keepSprint = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("keep-sprint"))
                .description("Whether to keep sprinting after attacking an entity."))
                .defaultValue(false))
                .build()
        );
    private final Setting<Boolean> unsprintOnHit = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("unsprint-on-hit"))
                .description("Whether to stop sprinting when attacking, to ensure you get crits and sweep attacks."))
                .defaultValue(false))
                .build()
        );
    private final Setting<Boolean> unsprintInWater = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("unsprint-in-water"))
                .description("Whether to stop sprinting when in water."))
                .defaultValue(true))
                .build()
        );

    public NewSprint() {
        super(Compassion.SERAPHIM, "NewSprint", "Automatically sprints.");
    }

    public void onDeactivate() {
        this.mc.player.setSprinting(false);
    }

    @EventHandler
    private void onTickMovement(Post event) {
        if (this.shouldSprint()) {
            this.mc.player.setSprinting(true);
        }
    }

    @EventHandler
    private void onPacketSend(Send event) {
        if (this.unsprintOnHit.get() && event.packet instanceof IPlayerInteractEntityC2SPacket packet && packet.getType() == InteractType.ATTACK) {
            this.mc
                .getNetworkHandler()
                .sendPacket(new ClientCommandC2SPacket(this.mc.player, ClientCommandC2SPacket.Mode.STOP_SPRINTING));
            this.mc.player.setSprinting(false);
        }
    }

    @EventHandler
    private void onPacketSent(Sent event) {
        if (this.unsprintOnHit.get() && this.keepSprint.get()) {
            if (event.packet instanceof IPlayerInteractEntityC2SPacket packet && packet.getType() == InteractType.ATTACK) {
                if (this.shouldSprint() && !this.mc.player.isSprinting()) {
                    this.mc
                        .getNetworkHandler()
                        .sendPacket(new ClientCommandC2SPacket(this.mc.player, ClientCommandC2SPacket.Mode.START_SPRINTING));
                    this.mc.player.setSprinting(true);
                }
            }
        }
    }

    public boolean shouldSprint() {
        if (!this.unsprintInWater.get() || !this.mc.player.isTouchingWater() && !this.mc.player.isSubmergedInWater()) {
            boolean strictSprint = this.mc.player.forwardSpeed > 1.0E-5F
                && (!this.mc.player.horizontalCollision || this.mc.player.collidedSoftly)
                && (!this.mc.player.isTouchingWater() || this.mc.player.isSubmergedInWater());
            return this.isActive() && (this.mode.get() == Mode.Rage || strictSprint);
        } else {
            return false;
        }
    }

    public boolean rageSprint() {
        return this.isActive() && this.mode.get() == Mode.Rage;
    }

    public boolean stopSprinting() {
        return !this.isActive() || !this.keepSprint.get();
    }

    public static enum Mode {
        Strict,
        Rage;
    }
}
