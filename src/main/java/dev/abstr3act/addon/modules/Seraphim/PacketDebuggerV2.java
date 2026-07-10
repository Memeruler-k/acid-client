package dev.abstr3act.addon.modules.Seraphim;

import dev.abstr3act.addon.Compassion;
import dev.abstr3act.addon.module.AbnormallyModule;
import dev.abstr3act.addon.utils.abnormally.AChatUtils;
import dev.abstr3act.addon.utils.abnormally.ReflectionUtils;
import meteordevelopment.meteorclient.events.packets.PacketEvent.Receive;
import meteordevelopment.meteorclient.events.packets.PacketEvent.Send;
import meteordevelopment.meteorclient.settings.PacketListSetting.Builder;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.utils.network.PacketUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.packet.Packet;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class PacketDebuggerV2 extends AbnormallyModule {
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
    private final Setting<Set<Class<? extends Packet<?>>>> s2cPackets = this.sgGeneral
        .add(
            ((Builder) ((Builder) new Builder().name("S2C-packets")).description("Server-to-client packets to cancel."))
                .filter(aClass -> PacketUtils.getS2CPackets().contains(aClass))
                .build()
        );
    private final Setting<Set<Class<? extends Packet<?>>>> c2sPackets = this.sgGeneral
        .add(
            ((Builder) ((Builder) new Builder().name("C2S-packets")).description("Client-to-server packets to cancel."))
                .filter(aClass -> PacketUtils.getC2SPackets().contains(aClass))
                .build()
        );

    public PacketDebuggerV2() {
        super(Compassion.SERAPHIM, "PacketDebuggerV2", "Packet Debugger");
    }

    public static String extract(String str) {
        String[] parts = str.split("@", 2);
        if (parts.length < 2) {
            return "";
        } else {
            String numberPart = parts[1].split(" ")[0];
            return "@" + numberPart;
        }
    }

    public static boolean isInstance(Object obj, List<Class<?>> classes) {
        for (Class<?> clazz : classes) {
            if (clazz.isInstance(obj)) {
                return true;
            }
        }

        return false;
    }

    @EventHandler(
        priority = 201
    )
    private void onReceivePacket(Receive event) {
        if (((Set) this.s2cPackets.get()).contains(event.packet.getClass())) {
            MutableText msg = Text.empty();
            msg.append("§8[§7PacketDebugger§8]§7 ");
            msg.append("§cReceived§7 ");
            msg.append(ReflectionUtils.getSignature(event.packet).replace("s2c", "§es2c§7"));
            AChatUtils.sendMsgSeraphim(Text.of(this.process(msg.getString()).replace(extract(msg.getString()), "§d" + extract(msg.getString()) + "§7")));
        }
    }

    @EventHandler(
        priority = 201
    )
    private void onSendPacket(Send event) {
        if (((Set) this.c2sPackets.get()).contains(event.packet.getClass())) {
            MutableText msg = Text.empty();
            msg.append("§8[§7PacketDebugger§8]§7 ");
            msg.append("§aSend§7 ");
            msg.append(ReflectionUtils.getSignature(event.packet).replace("c2s", "§ec2s§7"));
            AChatUtils.sendMsgSeraphim(Text.of(this.process(msg.getString()).replace(extract(msg.getString()), "§d" + extract(msg.getString()) + "§7")));
        }
    }

    private String process(String string) {
        String input = string;
        Map<String, String> replacements = Map.of(
            "common",
            "§dcommon§7",
            "config",
            "§dconfig§7",
            "handshake",
            "§dhandshake§7",
            "login",
            "§dlogin§7",
            "play",
            "§dplay§7",
            "query",
            "§dquery§7",
            "custom",
            "§dcustom§7"
        );

        for (Entry<String, String> entry : replacements.entrySet()) {
            input = input.replaceAll(entry.getKey(), entry.getValue());
        }

        return input;
    }
}
