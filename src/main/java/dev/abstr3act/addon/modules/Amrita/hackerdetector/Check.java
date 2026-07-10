package dev.abstr3act.addon.modules.Amrita.hackerdetector;

import dev.abstr3act.addon.events.legacy.MotionEvent;
import dev.abstr3act.addon.utils.abnormally.AChatUtils;
import meteordevelopment.meteorclient.events.packets.PacketEvent.Receive;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public abstract class Check {
    public boolean activate;

    public abstract String getName();

    public abstract void onPacketReceive(Receive var1, PlayerEntity var2);

    public abstract void onUpdate(PlayerEntity var1);

    public void onMotion(MotionEvent event, double x, double y, double z) {
    }

    public void flag(PlayerEntity player, String verbose) {
        AChatUtils.sendMsgAmritaAC(Text.of(player.getName().getString() + " failed " + Formatting.GRAY + this.getName() + Formatting.WHITE + " (" + verbose + ")"));
    }
}
