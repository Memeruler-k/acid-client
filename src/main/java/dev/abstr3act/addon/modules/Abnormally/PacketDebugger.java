package dev.abstr3act.addon.modules.Abnormally;

import dev.abstr3act.addon.Compassion;
import dev.abstr3act.addon.module.AbnormallyModule;
import dev.abstr3act.addon.utils.abnormally.AChatUtils;
import dev.abstr3act.addon.utils.abnormally.ReflectionUtils;
import meteordevelopment.meteorclient.events.packets.PacketEvent.Receive;
import meteordevelopment.meteorclient.events.packets.PacketEvent.Send;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

public class PacketDebugger extends AbnormallyModule {
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
    private final SettingGroup sgRender = this.settings.createGroup("Render");

    public PacketDebugger() {
        super(Compassion.ABNORMALLY, "PacketDebugger", "Packet Debugger");
    }

    @EventHandler
    public void onSendPacket(Send event) {
        MutableText msg = Text.empty();
        msg.append("[PacketDebugger] ");
        msg.append("Sent ");
        msg.append(ReflectionUtils.getSignature(event.packet));
        AChatUtils.sendMsg(msg);
    }

    @EventHandler
    public void onReceivePacket(Receive event) {
        MutableText msg = Text.empty();
        msg.append("[PacketDebugger] ");
        msg.append("Received ");
        msg.append(ReflectionUtils.getSignature(event.packet));
        AChatUtils.sendMsg(msg);
    }
}
