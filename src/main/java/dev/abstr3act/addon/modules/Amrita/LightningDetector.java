package dev.abstr3act.addon.modules.Amrita;

import dev.abstr3act.addon.Compassion;
import dev.abstr3act.addon.events.legacy.WorldEvent;
import dev.abstr3act.addon.module.AmritaModule;
import dev.abstr3act.addon.utils.abnormally.AChatUtils;
import meteordevelopment.meteorclient.events.packets.PacketEvent.Receive;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.ClickEvent.Action;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

public class LightningDetector extends AmritaModule {
    private boolean spoofed = false;

    public LightningDetector() {
        super(Compassion.AMRITA, "LightningDetector", "");
    }

    public static float squaredDistanceToWithoutY(Entity entity, double x, double z) {
        double d = entity.getX() - x;
        double f = entity.getZ() - z;
        return MathHelper.sqrt((float) (d * d + f * f));
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

    public void onReceivedPacket(Receive event) {
        if (this.mc.player != null && this.mc.world != null) {
            if (event.packet instanceof EntitySpawnS2CPacket entityPacket && entityPacket.getEntityType() == EntityType.LIGHTNING_BOLT) {
                int x = (int) entityPacket.getX();
                int y = (int) entityPacket.getY();
                int z = (int) entityPacket.getZ();
                float distance = squaredDistanceToWithoutY(this.mc.player, entityPacket.getX(), entityPacket.getZ());
                MutableText tpMessage = Text.literal(
                        "§7[§eM§7] (§eClick to Teleport§7) §eDetected lightning at " + x + " " + y + " " + z + " (" + (int) distance + " blocks away)"
                    )
                    .styled(
                        style -> style.withClickEvent(new ClickEvent(Action.SUGGEST_COMMAND, "/tp " + x + " " + y + " " + z))
                            .withHoverEvent(
                                new HoverEvent(
                                    HoverEvent.Action.SHOW_TEXT, Text.literal("§lClick to easily prepare for teleport to " + x + ", " + y + ", " + z)
                                )
                            )
                    );
                AChatUtils.sendMsgAmrita(tpMessage);
            }
        }
    }
}
