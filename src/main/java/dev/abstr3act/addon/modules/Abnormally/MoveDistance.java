package dev.abstr3act.addon.modules.Abnormally;

import dev.abstr3act.addon.Compassion;
import dev.abstr3act.addon.module.AbnormallyModule;
import dev.abstr3act.addon.utils.abnormally.AChatUtils;
import meteordevelopment.meteorclient.events.packets.PacketEvent.Receive;
import meteordevelopment.meteorclient.events.world.TickEvent.Pre;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket.PositionAndOnGround;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;

public class MoveDistance extends AbnormallyModule {
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
    private final SettingGroup sgRender = this.settings.createGroup("Render");
    int timer;
    private boolean flagged;

    public MoveDistance() {
        super(Compassion.ABNORMALLY, "MoveDistance", "Calculate move distance of current server");
    }

    @EventHandler
    public void onSync(Pre event) {
        if (this.flagged) {
            AChatUtils.sendMsg("result: " + this.timer);
            this.timer = 0;
            this.toggle();
        }

        if (this.timer <= 1000) {
            this.mc
                .player
                .networkHandler
                .sendPacket(new PositionAndOnGround(this.mc.player.getX(), this.mc.player.getY() + this.timer, this.mc.player.getZ(), false));
            this.mc.player.networkHandler.sendPacket(new PositionAndOnGround(this.mc.player.getX(), this.mc.player.getY(), this.mc.player.getZ(), false));
            this.timer++;
        }
    }

    public void onActivate() {
        this.flagged = false;
        this.timer = 0;
    }

    @EventHandler
    private void onReceivePacket(Receive event) {
        if (event.packet instanceof PlayerPositionLookS2CPacket) {
            this.flagged = true;
        }
    }
}
