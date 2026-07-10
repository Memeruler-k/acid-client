package dev.abstr3act.addon.modules.Seraphim;

import dev.abstr3act.addon.Compassion;
import dev.abstr3act.addon.module.SeraphimModule;
import dev.abstr3act.addon.utils.abnormally.AChatUtils;
import meteordevelopment.meteorclient.events.game.GameJoinedEvent;
import meteordevelopment.meteorclient.events.packets.PacketEvent.Receive;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.StringSetting.Builder;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.minecraft.text.Text;

public class TotemDebugger extends SeraphimModule {
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
    private final Setting<String> targetName = this.sgGeneral
        .add(((Builder) ((Builder) ((Builder) new Builder().name("targetName")).description(".")).defaultValue("")).build());
    long lastMillisecond = 0L;
    int count = 0;

    public TotemDebugger() {
        super(Compassion.SERAPHIM, "TotemDebugger", ".");
    }

    @EventHandler
    private void onGameJoin(GameJoinedEvent event) {
        this.count = 0;
        this.lastMillisecond = 0L;
    }

    @EventHandler(
        priority = 100
    )
    private void onReceivePacket(Receive event) {
        if (event.packet instanceof EntityStatusS2CPacket p) {
            if (p.getStatus() == 35) {
                Entity entity = p.getEntity(this.mc.world);
                if (entity != null && entity.getName().getString().contains((CharSequence) this.targetName.get())) {
                    this.count++;
                    AChatUtils.sendMsgSeraphim(
                        Text.of(entity.getName().getString() + " popped " + this.count + " totem at " + (System.currentTimeMillis() - this.lastMillisecond) + "ms")
                    );
                    System.out
                        .println(entity.getName().getString() + " popped " + this.count + " totem at " + (System.currentTimeMillis() - this.lastMillisecond) + "ms");
                    this.lastMillisecond = System.currentTimeMillis();
                }
            }
        }
    }
}
