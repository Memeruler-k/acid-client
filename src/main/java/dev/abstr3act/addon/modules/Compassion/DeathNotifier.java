package dev.abstr3act.addon.modules.Compassion;

import dev.abstr3act.addon.Compassion;
import dev.abstr3act.addon.hud.notifications.NotificationsHudElement;
import dev.abstr3act.addon.module.AbnormallyModule;
import dev.abstr3act.addon.notifications.Notification;
import dev.abstr3act.addon.notifications.NotificationsManager;
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

import java.awt.*;
import java.util.*;

public class DeathNotifier extends AbnormallyModule {
    private final SettingGroup sgTotemPops = this.settings.createGroup("Totem Pops");
    private final Setting<Boolean> totemPops = this.sgTotemPops
        .add(((Builder) ((Builder) ((Builder) new Builder().name("totem-pops")).description("Notifies you when a player pops a totem.")).defaultValue(true)).build());
    private final Setting<Boolean> totemsIgnoreOwn = this.sgTotemPops
        .add(((Builder) ((Builder) ((Builder) new Builder().name("ignore-own")).description("Ignores your own totem pops.")).defaultValue(false)).build());
    private final Setting<Boolean> totemsIgnoreFriends = this.sgTotemPops
        .add(((Builder) ((Builder) ((Builder) new Builder().name("ignore-friends")).description("Ignores friends totem pops.")).defaultValue(false)).build());
    private final Setting<Boolean> totemsIgnoreOthers = this.sgTotemPops
        .add(((Builder) ((Builder) ((Builder) new Builder().name("ignore-others")).description("Ignores other players totem pops.")).defaultValue(false)).build());
    private final Object2IntMap<UUID> totemPopMap = new Object2IntOpenHashMap();
    private final Object2IntMap<UUID> deadMap = new Object2IntOpenHashMap();
    private final Object2IntMap<UUID> chatIdMap = new Object2IntOpenHashMap();
    private final Map<Integer, Vec3d> pearlStartPosMap = new HashMap<>();
    private Set<UUID> deadPlayers = new HashSet<>();

    public DeathNotifier() {
        super(Compassion.COMPASSION, "DeathNotifier", "LOL");
    }

    public void onActivate() {
        this.totemPopMap.clear();
        this.deadMap.clear();
        this.chatIdMap.clear();
        this.pearlStartPosMap.clear();
    }

    public void onDeactivate() {
    }

    @EventHandler
    private void onGameJoin(GameJoinedEvent event) {
        this.totemPopMap.clear();
        this.deadMap.clear();
        this.chatIdMap.clear();
        this.pearlStartPosMap.clear();
    }

    @EventHandler
    private void onReceivePacket(Receive event) {
        if (this.totemPops.get()) {
            if (event.packet instanceof EntityStatusS2CPacket p) {
                if (p.getStatus() == 35) {
                    Entity entity = p.getEntity(this.mc.world);
                    if (entity instanceof PlayerEntity) {
                        if ((!entity.equals(this.mc.player) || !this.totemsIgnoreOwn.get())
                            && (!Friends.get().isFriend((PlayerEntity) entity) || !this.totemsIgnoreOthers.get())
                            && (Friends.get().isFriend((PlayerEntity) entity) || !this.totemsIgnoreFriends.get())) {
                            synchronized (this.totemPopMap) {
                                int pops = this.totemPopMap.getOrDefault(entity.getUuid(), 0);
                                if (!entity.equals(this.mc.player)) {
                                    this.totemPopMap.put(entity.getUuid(), ++pops);
                                    NotificationsManager.add(
                                        new Notification("Totem Pops", entity.getName().getString() + " popped " + pops + " " + (pops == 1 ? "totem" : "totems"), Color.WHITE)
                                    );
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    private void onTick(Post event) {
        synchronized (this.totemPopMap) {
            for (PlayerEntity player : this.mc.world.getPlayers()) {
                if (!player.equals(this.mc.player)) {
                    if ((player.isDead() || player.getHealth() <= 0.0F) && !this.deadPlayers.contains(player.getUuid())) {
                        if (this.totemPopMap.containsKey(player.getUuid())) {
                            if (Friends.get().isFriend(player)) {
                                NotificationsManager.add(
                                    new Notification("Friend Death", player.getName().getString() + " has been neutralized", Color.WHITE, NotificationsHudElement.icon.WARNING)
                                );
                            }

                            NotificationsManager.add(
                                new Notification("Target Death", player.getName().getString() + " has been neutralized", Color.WHITE, NotificationsHudElement.icon.ENABLE)
                            );
                        } else {
                            if (Friends.get().isFriend(player)) {
                                NotificationsManager.add(
                                    new Notification("Friend Death", player.getName().getString() + " has been neutralized", Color.WHITE, NotificationsHudElement.icon.WARNING)
                                );
                            }

                            NotificationsManager.add(
                                new Notification("Target Death", player.getName().getString() + " has been neutralized", Color.WHITE, NotificationsHudElement.icon.ENABLE)
                            );
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
}
