package dev.abstr3act.addon.modules.Amrita;

import dev.abstr3act.addon.Compassion;
import dev.abstr3act.addon.events.legacy.ModifyPacketEvent;
import dev.abstr3act.addon.events.legacy.WorldEvent;
import dev.abstr3act.addon.module.AmritaModule;
import dev.abstr3act.addon.utils.abnormally.AChatUtils;
import meteordevelopment.meteorclient.settings.EnumSetting.Builder;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.packet.BrandCustomPayload;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.common.CustomPayloadC2SPacket;
import net.minecraft.text.Text;

public class Protocol extends AmritaModule {
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
    private final Setting<ProtocolMode> mode = this.sgGeneral
        .add(((Builder) ((Builder) ((Builder) new Builder().name("Protocol")).description(".")).defaultValue(ProtocolMode.Vanilla)).build());
    private boolean spoofed = false;

    public Protocol() {
        super(Compassion.AMRITA, "Protocol", "");
    }

    public String getInfoString() {
        return ((ProtocolMode) this.mode.get()).name();
    }

    public void onDeactivate() {
        this.spoofed = false;
    }

    @EventHandler
    public void onWorld(WorldEvent event) {
        if (this.spoofed) {
            AChatUtils.sendMsgAmrita(Text.of("Spoofed client brand to \"vanilla\""));
            this.spoofed = false;
        }
    }

    @EventHandler
    public void onModifyPacket(ModifyPacketEvent event) {
        Packet<?> packet = event.packet;
        if (((ProtocolMode) this.mode.get()).equals(ProtocolMode.Vanilla)
            && packet instanceof CustomPayloadC2SPacket
            && ((CustomPayloadC2SPacket) packet).payload() instanceof BrandCustomPayload) {
            event.packet = new CustomPayloadC2SPacket(new BrandCustomPayload("vanilla"));
            this.spoofed = true;
        }
    }

    static enum ProtocolMode {
        Vanilla,
        Heypixel,
        Bedrock;
    }
}
