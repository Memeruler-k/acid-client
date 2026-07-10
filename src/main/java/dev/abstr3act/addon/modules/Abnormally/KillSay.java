package dev.abstr3act.addon.modules.Abnormally;

import dev.abstr3act.addon.Compassion;
import dev.abstr3act.addon.module.AbnormallyModule;
import dev.abstr3act.addon.utils.luna.ChineseUtils;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import meteordevelopment.meteorclient.events.game.GameJoinedEvent;
import meteordevelopment.meteorclient.events.packets.PacketEvent.Receive;
import meteordevelopment.meteorclient.events.world.TickEvent.Post;
import meteordevelopment.meteorclient.settings.BoolSetting.Builder;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.minecraft.util.math.Vec3d;

import java.util.*;

public class KillSay extends AbnormallyModule {
    private final SettingGroup sgTotemPops = this.settings.createGroup("Totem Pops");
    private final Setting<Boolean> totemPops = this.sgTotemPops
        .add(((Builder) ((Builder) ((Builder) new Builder().name("totem-pops")).description("Notifies you when a player pops a totem.")).defaultValue(true)).build());
    private final Setting<Boolean> totemsIgnoreOwn = this.sgTotemPops
        .add(((Builder) ((Builder) ((Builder) new Builder().name("ignore-own")).description("Ignores your own totem pops.")).defaultValue(true)).build());
    private final Object2IntMap<UUID> totemPopMap = new Object2IntOpenHashMap();
    private final Object2IntMap<UUID> deadMap = new Object2IntOpenHashMap();
    private final Object2IntMap<UUID> chatIdMap = new Object2IntOpenHashMap();
    private final Map<Integer, Vec3d> pearlStartPosMap = new HashMap<>();
    private final Random random = new Random();
    private int deadAmount = 0;
    private Set<UUID> deadPlayers = new HashSet<>();

    public KillSay() {
        super(Compassion.ABNORMALLY, "kill-say", "Southside kill say");
    }

    public void onActivate() {
        this.totemPopMap.clear();
        this.deadMap.clear();
        this.chatIdMap.clear();
        this.pearlStartPosMap.clear();
        this.deadAmount = 0;
    }

    public void onDeactivate() {
        this.deadAmount = 0;
    }

    @EventHandler
    private void onGameJoin(GameJoinedEvent event) {
        this.totemPopMap.clear();
        this.deadMap.clear();
        this.chatIdMap.clear();
        this.pearlStartPosMap.clear();
        this.deadAmount = 0;
    }

    private String getKillMessage(int killAmount) {
        return switch (killAmount) {
            case 1 -> ChineseUtils.convert(killAmount) + "破，卧龙出山，你已被abnormally客户端击毙";
            case 2 -> ChineseUtils.convert(killAmount) + "连，一战成名，你已被abnormally客户端击毙";
            case 3 -> ChineseUtils.convert(killAmount) + "联，举世皆惊，你已被abnormally客户端击毙";
            case 4 -> ChineseUtils.convert(killAmount) + "连，天下无敌，你已被abnormally客户端击毙";
            default -> ChineseUtils.convert(killAmount) + "连，诛天灭地，你已被abnormally客户端击毙";
        };
    }

    @EventHandler
    private void onReceivePacket(Receive event) {
        if (this.totemPops.get()) {
            if (event.packet instanceof EntityStatusS2CPacket p) {
                if (p.getStatus() == 35) {
                    Entity entity = p.getEntity(this.mc.world);
                    if (entity instanceof PlayerEntity) {
                        if ((!entity.equals(this.mc.player) || !this.totemsIgnoreOwn.get())
                            && !Friends.get().isFriend((PlayerEntity) entity)
                            && Friends.get().isFriend((PlayerEntity) entity)) {
                            synchronized (this.totemPopMap) {
                                int pops = this.totemPopMap.getOrDefault(entity.getUuid(), 0);
                                this.totemPopMap.put(entity.getUuid(), ++pops);
                            }
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    private void onTick(Post event) {
        if (this.deadAmount > 10) {
            this.deadAmount = 0;
        }

        synchronized (this.totemPopMap) {
            for (PlayerEntity player : this.mc.world.getPlayers()) {
                if ((player.isDead() || player.getHealth() <= 0.0F) && !this.deadPlayers.contains(player.getUuid())) {
                    if (this.totemPopMap.containsKey(player.getUuid())) {
                        this.deadAmount++;
                        String msg = this.getKillMessage(this.deadAmount);
                        this.mc.player.networkHandler.sendChatMessage(msg);
                    } else {
                        this.deadAmount++;
                        String msg = this.getKillMessage(this.deadAmount);
                        this.mc.player.networkHandler.sendChatMessage(msg);
                    }

                    this.deadPlayers.add(player.getUuid());
                    this.chatIdMap.removeInt(player.getUuid());
                }

                if (!player.isDead() && player.getHealth() > 0.0F && this.deadPlayers.contains(player.getUuid())) {
                    this.deadPlayers.remove(player.getUuid());
                }
            }
        }
    }
}
