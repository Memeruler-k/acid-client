package dev.abstr3act.addon.modules.Abnormally;

import dev.abstr3act.addon.Compassion;
import dev.abstr3act.addon.module.AbnormallyModule;
import dev.abstr3act.addon.notifications.Notification;
import dev.abstr3act.addon.notifications.NotificationsManager;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import meteordevelopment.meteorclient.events.game.GameJoinedEvent;
import meteordevelopment.meteorclient.events.packets.PacketEvent.Receive;
import meteordevelopment.meteorclient.events.world.TickEvent.Post;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.StringListSetting.Builder;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.minecraft.util.math.Vec3d;

import java.awt.*;
import java.util.*;
import java.util.List;

public class AutoEZ extends AbnormallyModule {
    private final SettingGroup sgTotemPops = this.settings.createGroup("Totem Pops");
    private final Setting<List<String>> killSay = this.sgTotemPops
        .add(
            ((Builder) ((Builder) ((Builder) new Builder().name("KillSay")).description("%s: PlayerName, %n: TotemPops"))
                .defaultValue(
                    List.of(
                        "%s 在pop %n 个图腾后被CompassionClient变成了卡带",
                        "人生自古谁无死？不幸的，%s 在pop %n个图腾后已无法与您继续互动，让我们一起缅怀他",
                        "%s♡ 就算你pop了 %n 个图腾我也会一直视奸你的♡",
                        "%s was missed after %n pops, thanks to Compassion Client",
                        "%s 在pop %n 个图腾后被害死了",
                        "EZZZZZ! %s was killed after %n pops"
                    )
                ))
                .build()
        );
    private final Setting<List<String>> dead = this.sgTotemPops
        .add(
            ((Builder) ((Builder) ((Builder) new Builder().name("KillSay")).description("%s: PlayerName, %n: TotemPops"))
                .defaultValue(
                    List.of(
                        "%s 被CompassionClient变成了卡带",
                        "人生自古谁无死？不幸的，%s已被Compassion击毙，无法与您继续互动，让我们一起缅怀他",
                        "%s♡ 我会一直视奸你的♡",
                        "%s died, thanks to Compassion Client",
                        "%s 被害死了",
                        "EZZZZZ! %s was killed by compassion client"
                    )
                ))
                .build()
        );
    private final Setting<Boolean> totemPops = this.sgTotemPops
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("totem-pops"))
                .description("Notifies you when a player pops a totem."))
                .defaultValue(true))
                .build()
        );
    private final Setting<Boolean> totemsIgnoreOwn = this.sgTotemPops
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("ignore-own"))
                .description("Ignores your own totem pops."))
                .defaultValue(false))
                .build()
        );
    private final Setting<Boolean> totemsIgnoreFriends = this.sgTotemPops
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("ignore-friends"))
                .description("Ignores friends totem pops."))
                .defaultValue(false))
                .build()
        );
    private final Setting<Boolean> totemsIgnoreOthers = this.sgTotemPops
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("ignore-others"))
                .description("Ignores other players totem pops."))
                .defaultValue(false))
                .build()
        );
    private final Object2IntMap<UUID> totemPopMap = new Object2IntOpenHashMap();
    private final Object2IntMap<UUID> deadMap = new Object2IntOpenHashMap();
    private final Object2IntMap<UUID> chatIdMap = new Object2IntOpenHashMap();
    private final Map<Integer, Vec3d> pearlStartPosMap = new HashMap<>();
    private Set<UUID> deadPlayers = new HashSet<>();

    public AutoEZ() {
        super(Compassion.ABNORMALLY, "AutoEZ", "LOL");
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

    @EventHandler
    private void onTick(Post event) {
        synchronized (this.totemPopMap) {
            for (PlayerEntity player : this.mc.world.getPlayers()) {
                if (!player.equals(this.mc.player)) {
                    if ((player.isDead() || player.getHealth() <= 0.0F) && !this.deadPlayers.contains(player.getUuid())) {
                        String coord = player.getBlockPos().getX() + " " + player.getBlockPos().getY() + " " + player.getBlockPos().getZ();
                        if (this.totemPopMap.containsKey(player.getUuid())) {
                            int pops = this.totemPopMap.removeInt(player.getUuid());
                            int index = new Random().nextInt((this.killSay.get()).size());
                            String msg = (String) (this.killSay.get()).get(index);
                            String finalMSG = msg.replace("%s", player.getName().getString()).replace("%c", coord).replace("%n", String.valueOf(pops));
                            NotificationsManager.add(new Notification("Player Death", player.getName().getString() + " has been neutralized", Color.WHITE));
                            this.mc.player.networkHandler.sendChatMessage(finalMSG);
                        } else {
                            int index = new Random().nextInt((this.dead.get()).size());
                            String msg = (String) (this.dead.get()).get(index);
                            String finalMSG = msg.replace("%s", player.getName().getString()).replace("%c", coord);
                            NotificationsManager.add(new Notification("Player Death", player.getName().getString() + " has been neutralized", Color.WHITE));
                            this.mc.player.networkHandler.sendChatMessage(finalMSG);
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
