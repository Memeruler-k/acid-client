package dev.abstr3act.addon.modules.Compassion;

import com.mojang.authlib.GameProfile;
import dev.abstr3act.addon.Compassion;
import dev.abstr3act.addon.module.AbnormallyModule;
import dev.abstr3act.addon.utils.abnormally.AChatUtils;
import meteordevelopment.meteorclient.events.packets.PacketEvent.Receive;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket.Entry;
import net.minecraft.network.packet.s2c.play.PlayerRemoveS2CPacket;
import net.minecraft.text.Text;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerNotifier extends AbnormallyModule {
    private final Map<UUID, String> uuidNameCache = new HashMap<>();
    private String joinMessageFormat = "Player %s has joined the game.";
    private String leaveMessageFormat = "Player %s has left the game.";

    public PlayerNotifier() {
        super(Compassion.COMPASSION, "PlayerNotifier", "Skid from CCBlueX");
    }

    @EventHandler
    private void onPacketEvent(Receive event) {
        if (event.packet instanceof PlayerListS2CPacket listPacket) {
            for (Entry entry : listPacket.getPlayerAdditionEntries()) {
                GameProfile profile = entry.profile();
                if (profile != null && profile.getName() != null && profile.getName().length() > 2) {
                    this.uuidNameCache.put(profile.getId(), profile.getName());
                    String message = String.format(this.joinMessageFormat, profile.getName());
                    AChatUtils.sendMsgCompassion(Text.of(message));
                }
            }
        } else if (event.packet instanceof PlayerRemoveS2CPacket removePacket) {
            for (UUID uuid : removePacket.profileIds()) {
                PlayerListEntry entryx = this.mc
                    .player
                    .networkHandler
                    .getPlayerList()
                    .stream()
                    .filter(e -> e.getProfile().getId().equals(uuid))
                    .findFirst()
                    .orElse(null);
                if (entryx != null && entryx.getProfile().getName() != null && entryx.getProfile().getName().length() > 2) {
                    String message = String.format(this.leaveMessageFormat, this.uuidNameCache.get(entryx.getProfile().getId()));
                    AChatUtils.sendMsgCompassion(Text.of(message));
                    this.uuidNameCache.remove(entryx.getProfile().getId());
                }
            }
        }
    }
}
