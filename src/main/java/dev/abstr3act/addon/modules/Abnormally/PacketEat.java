package dev.abstr3act.addon.modules.Abnormally;

import dev.abstr3act.addon.Compassion;
import dev.abstr3act.addon.module.AbnormallyModule;
import meteordevelopment.meteorclient.events.packets.PacketEvent.Send;
import meteordevelopment.meteorclient.events.world.TickEvent.Post;
import meteordevelopment.meteorclient.settings.BoolSetting.Builder;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket.Action;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.util.Hand;

public class PacketEat extends AbnormallyModule {
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
    private final Setting<Boolean> deSync = this.sgGeneral
        .add(((Builder) ((Builder) ((Builder) new Builder().name("Desync")).description(".")).defaultValue(true)).build());

    public PacketEat() {
        super(Compassion.ABNORMALLY, "PacketEat", "packet eat");
    }

    public void onActivate() {
    }

    public void onDeactivate() {
    }

    @EventHandler
    public void onUpdate(Post event) {
        if (this.deSync.get() && this.mc.player.isUsingItem() && this.mc.player.getActiveItem().getEatSound() != null) {
            this.sendSequencedPacket(id -> new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, id, this.mc.player.getYaw(), this.mc.player.getPitch()));
        }
    }

    @EventHandler
    public void onPacket(Send event) {
        if (event.packet instanceof PlayerActionC2SPacket packet
            && packet.getAction() == Action.RELEASE_USE_ITEM
            && this.mc.player.getActiveItem().getEatSound() != null) {
            event.cancel();
        }
    }
}
