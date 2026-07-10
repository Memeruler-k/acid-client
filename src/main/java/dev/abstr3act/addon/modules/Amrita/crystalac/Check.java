package dev.abstr3act.addon.modules.Amrita.crystalac;

import dev.abstr3act.addon.events.legacy.MotionEvent;
import dev.abstr3act.addon.utils.abnormally.AChatUtils;
import meteordevelopment.meteorclient.events.packets.PacketEvent.Receive;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
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
        if (((CrystalAC) Modules.get().get(CrystalAC.class)).shout.get()) {
            ChatUtils.sendPlayerMsg("[CrystalAC] " + player.getName().getString() + " failed " + this.getName() + " (" + verbose + ")");
        } else {
            AChatUtils.sendMsgAmritaAC(
                Text.of(
                    Formatting.AQUA
                        + player.getName().getString()
                        + Formatting.GRAY
                        + " failed "
                        + Formatting.AQUA
                        + this.getName()
                        + Formatting.WHITE
                        + " ("
                        + verbose
                        + ")"
                )
            );
        }
    }
}
