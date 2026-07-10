package dev.abstr3act.addon.modules.Compassion;

import dev.abstr3act.addon.Compassion;
import dev.abstr3act.addon.events.EventUpdate;
import dev.abstr3act.addon.module.CompassionModule;
import dev.abstr3act.addon.utils.abnormally.AChatUtils;
import dev.abstr3act.addon.utils.math.MathUtility;
import meteordevelopment.meteorclient.events.packets.PacketEvent.Receive;
import meteordevelopment.meteorclient.events.packets.PacketEvent.Send;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.packet.c2s.common.ResourcePackStatusC2SPacket;
import net.minecraft.network.packet.c2s.common.ResourcePackStatusC2SPacket.Status;
import net.minecraft.text.Text;

public class AntiResourcePack extends CompassionModule {
    private boolean confirm;
    private boolean accepted;
    private int delay;

    public AntiResourcePack() {
        super(Compassion.COMPASSION, "AntiResourcePack", "idk");
    }

    @EventHandler
    private void onPacketSent(Receive event) {
        if (event.packet instanceof ResourcePackStatusC2SPacket packet) {
            Status status = packet.status();
            this.confirm = true;
            this.accepted = false;
            this.delay = 0;
            AChatUtils.sendMsgCompassion(Text.of("成功拦截了一个 " + status.name() + " 的死妈材质包"));
            event.cancel();
        }
    }

    @EventHandler
    private void onUpdate(EventUpdate event) {
        if (this.confirm) {
            this.delay++;
            if (this.delay > MathUtility.random(15.0F, 30.0F) && !this.accepted) {
                this.sendPacket(new ResourcePackStatusC2SPacket(this.mc.player.getUuid(), Status.ACCEPTED));
                this.accepted = true;
            }

            if (this.delay > MathUtility.random(40.0F, 60.0F) && this.accepted) {
                this.sendPacket(new ResourcePackStatusC2SPacket(this.mc.player.getUuid(), Status.SUCCESSFULLY_LOADED));
                this.confirm = false;
            }
        }
    }

    @EventHandler
    private void onPacketSent(Send event) {
        if (event.packet instanceof ResourcePackStatusC2SPacket) {
            event.cancel();
        }
    }
}
