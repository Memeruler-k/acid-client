package dev.abstr3act.addon.modules.Abnormally;

import dev.abstr3act.addon.Compassion;
import dev.abstr3act.addon.module.AbnormallyModule;
import meteordevelopment.meteorclient.events.packets.PacketEvent.Receive;
import meteordevelopment.meteorclient.settings.BoolSetting.Builder;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ExplosionS2CPacket;
import net.minecraft.network.packet.s2c.play.ParticleS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;

public class AntiCrash extends AbnormallyModule {
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
    private final Setting<Boolean> log = this.sgGeneral
        .add(((Builder) ((Builder) ((Builder) new Builder().name("log")).description("Logs when crash packet detected.")).defaultValue(false)).build());

    public AntiCrash() {
        super(Compassion.ABNORMALLY, "AntiCrash", "Attempts to cancel packets that may crash the client.");
    }

    @EventHandler
    private void onPacketReceive(Receive event) {
        if (event.packet instanceof ExplosionS2CPacket packet) {
            if (packet.getX() > 3.0E7
                || packet.getY() > 3.0E7
                || packet.getZ() > 3.0E7
                || packet.getX() < -3.0E7
                || packet.getY() < -3.0E7
                || packet.getZ() < -3.0E7
                || packet.getRadius() > 1000.0F
                || packet.getAffectedBlocks().size() > 100000
                || packet.getPlayerVelocityX() > 3.0E7F
                || packet.getPlayerVelocityY() > 3.0E7F
                || packet.getPlayerVelocityZ() > 3.0E7F
                || packet.getPlayerVelocityX() < -3.0E7F
                || packet.getPlayerVelocityY() < -3.0E7F
                || packet.getPlayerVelocityZ() < -3.0E7F) {
                this.cancel(event);
            }
        } else if (event.packet instanceof ParticleS2CPacket packetx) {
            if (packetx.getCount() > 100000) {
                this.cancel(event);
            }
        } else if (event.packet instanceof PlayerPositionLookS2CPacket packetxx) {
            if (packetxx.getX() > 3.0E7
                || packetxx.getY() > 3.0E7
                || packetxx.getZ() > 3.0E7
                || packetxx.getX() < -3.0E7
                || packetxx.getY() < -3.0E7
                || packetxx.getZ() < -3.0E7) {
                this.cancel(event);
            }
        } else if (event.packet instanceof EntityVelocityUpdateS2CPacket packetxxx
            && (
            packetxxx.getVelocityX() > 3.0E7
                || packetxxx.getVelocityY() > 3.0E7
                || packetxxx.getVelocityZ() > 3.0E7
                || packetxxx.getVelocityX() < -3.0E7
                || packetxxx.getVelocityY() < -3.0E7
                || packetxxx.getVelocityZ() < -3.0E7
        )) {
            this.cancel(event);
        }
    }

    private void cancel(Receive event) {
        if (this.log.get()) {
            this.warning("Server attempts to crash you", new Object[0]);
        }

        event.cancel();
    }
}
