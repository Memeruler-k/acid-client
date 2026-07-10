package dev.abstr3act.addon.modules.Seraphim;

import com.mojang.authlib.GameProfile;
import dev.abstr3act.addon.Compassion;
import dev.abstr3act.addon.hud.notifications.NotificationsHudElement;
import dev.abstr3act.addon.module.AbnormallyModule;
import dev.abstr3act.addon.notifications.NotificationsManager;
import dev.abstr3act.addon.utils.abnormally.AChatUtils;
import meteordevelopment.meteorclient.events.game.GameJoinedEvent;
import meteordevelopment.meteorclient.events.packets.PacketEvent.Receive;
import meteordevelopment.meteorclient.events.world.TickEvent.Pre;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.StringListSetting.Builder;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket.Entry;
import net.minecraft.network.packet.s2c.play.PlayerRemoveS2CPacket;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;

import java.awt.*;
import java.util.*;
import java.util.List;

public class StaffAlert extends AbnormallyModule {
    private final Map<UUID, String> uuidNameCache = new HashMap<>();
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
    private final Setting<List<String>> staffList = this.sgGeneral
        .add(((Builder) ((Builder) ((Builder) new Builder().name("StaffList")).description(".")).defaultValue(List.of("oLwon"))).build());
    private final Setting<Mode> mode = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) new meteordevelopment.meteorclient.settings.EnumSetting.Builder()
                .name("Mode"))
                .description("."))
                .defaultValue(Mode.Logout))
                .build()
        );
    private final Setting<String> command = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.StringSetting.Builder) ((meteordevelopment.meteorclient.settings.StringSetting.Builder) ((meteordevelopment.meteorclient.settings.StringSetting.Builder) ((meteordevelopment.meteorclient.settings.StringSetting.Builder) new meteordevelopment.meteorclient.settings.StringSetting.Builder()
                .name("Command"))
                .description("."))
                .defaultValue("/hub"))
                .visible(() -> ((Mode) this.mode.get()).equals(Mode.RunCommand)))
                .build()
        );
    private final Setting<Notification> notification = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) new meteordevelopment.meteorclient.settings.EnumSetting.Builder()
                .name("Notification"))
                .description("."))
                .defaultValue(Notification.Notification))
                .build()
        );
    private final Setting<Boolean> alwaysLog = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("AlwaysLogout"))
                .description("."))
                .defaultValue(false))
                .build()
        );
    private final Setting<Boolean> checkWorld = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("CheckWorld"))
                .description("."))
                .defaultValue(false))
                .build()
        );
    private final Setting<Boolean> playSound = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("PlaySound"))
                .description("."))
                .defaultValue(false))
                .build()
        );
    private final Setting<List<SoundEvent>> sound_join = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.SoundEventListSetting.Builder) ((meteordevelopment.meteorclient.settings.SoundEventListSetting.Builder) ((meteordevelopment.meteorclient.settings.SoundEventListSetting.Builder) new meteordevelopment.meteorclient.settings.SoundEventListSetting.Builder()
                .name("JoinSound"))
                .description("."))
                .visible(this.playSound::get))
                .build()
        );
    private final Setting<List<SoundEvent>> sound_exit = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.SoundEventListSetting.Builder) ((meteordevelopment.meteorclient.settings.SoundEventListSetting.Builder) ((meteordevelopment.meteorclient.settings.SoundEventListSetting.Builder) new meteordevelopment.meteorclient.settings.SoundEventListSetting.Builder()
                .name("ExitSound"))
                .description("."))
                .visible(this.playSound::get))
                .build()
        );
    private String joinMessageFormat = "Staff detected : %s ";
    private String leaveMessageFormat = "Staff left : %s ";

    public StaffAlert() {
        super(Compassion.COMPASSION, "StaffAlert", "Anti staff XD");
    }

    private static List<String> extractPlayerNames(Collection<PlayerListEntry> playerEntries) {
        List<String> playerNames = new ArrayList<>();

        for (PlayerListEntry entry : playerEntries) {
            playerNames.add(entry.getProfile().getName());
        }

        return playerNames;
    }

    public static Object getPatch(Collection<?> list1, List<?> list2) {
        Set<?> set = new HashSet(list2);

        for (Object item : list1) {
            if (set.contains(item)) {
                return item;
            }
        }

        return null;
    }

    public static boolean patch(Collection<?> list1, List<?> list2) {
        for (Object item : list1) {
            if (list2.contains(item)) {
                return true;
            }
        }

        return false;
    }

    @EventHandler
    private void onGameJoin(GameJoinedEvent event) {
        Collection<PlayerListEntry> list = this.mc.getNetworkHandler().getPlayerList();
        if (this.mc.player != null) {
            List<String> playerNames = extractPlayerNames(list);
            if (this.alwaysLog.get() && this.contains(playerNames, (List<String>) this.staffList.get())) {
                this.logout(Text.of("§7[§9Acid§fClient§7] §7检测到死妈管理员/客服 " + this.patchs(playerNames, (List<String>) this.staffList.get())));
            }

            if (patch(list, (List<?>) this.staffList.get())) {
                this.logout(Text.of("§7[§9Acid§fClient§7] §7检测到死妈管理员/客服 " + getPatch(list, (List<?>) this.staffList.get())));
                if (this.playSound.get()) {
                    for (SoundEvent sound : this.sound_join.get()) {
                        this.mc.player.playSound(sound);
                    }
                }

                switch ((Notification) this.notification.get()) {
                    case Chat:
                        AChatUtils.sendMsgSeraphim(Text.of("Staff detected: " + getPatch(list, (List<?>) this.staffList.get())));
                        break;
                    case Notification:
                        NotificationsManager.add(
                            new dev.abstr3act.addon.notifications.Notification(
                                "StaffAlert", "Staff detected: " + getPatch(list, (List<?>) this.staffList.get()), Color.WHITE, NotificationsHudElement.icon.WARNING
                            )
                        );
                }
            }
        }
    }

    private boolean contains(List<String> listA, List<String> listB) {
        boolean contains = false;

        for (String itemA : listA) {
            for (String itemB : listB) {
                if (itemA.contains(itemB)) {
                    contains = true;
                    break;
                }
            }

            if (contains) {
                return contains;
            }
        }

        return contains;
    }

    private String patchs(List<String> listA, List<String> listB) {
        for (String itemA : listA) {
            for (String itemB : listB) {
                if (itemA.contains(itemB)) {
                    return itemB;
                }
            }
        }

        return null;
    }

    @EventHandler
    private void onTick(Pre event) {
        Collection<PlayerListEntry> list = this.mc.getNetworkHandler().getPlayerList();
        if (this.mc.player != null) {
            if (this.checkWorld.get()) {
                for (PlayerEntity entity : this.mc.world.getPlayers()) {
                    for (String keyword : this.staffList.get()) {
                        if (entity.getName().getString().contains(keyword)) {
                            this.logout(Text.of("§7[§9Acid§fClient§7] §7检测到死妈管理员/客服 " + entity.getName().getString()));
                            break;
                        }
                    }
                }
            }

            if (this.alwaysLog.get()) {
                List<String> playerNames = extractPlayerNames(list);
                if (this.contains(playerNames, (List<String>) this.staffList.get())) {
                    this.logout(Text.of("§7[§9Acid§fClient§7] §7检测到死妈管理员/客服 " + this.patchs(playerNames, (List<String>) this.staffList.get())));
                }
            }
        }
    }

    private void logout(Text disconnect) {
        switch ((Mode) this.mode.get()) {
            case RunCommand:
                ChatUtils.sendPlayerMsg((String) this.command.get());
                break;
            case Logout:
                this.mc.player.networkHandler.getConnection().disconnect(Text.of(disconnect));
            case None:
        }
    }

    @EventHandler
    private void onPacketEvent(Receive event) {
        if (event.packet instanceof PlayerListS2CPacket listPacket) {
            for (Entry entry : listPacket.getPlayerAdditionEntries()) {
                GameProfile profile = entry.profile();
                if (profile != null && (this.staffList.get()).contains(profile.getName()) && profile.getName() != null && profile.getName().length() > 2) {
                    this.uuidNameCache.put(profile.getId(), profile.getName());
                    String message = String.format(this.joinMessageFormat, profile.getName());
                    this.logout(Text.of("§7[§9Acid§fClient§7] §7检测到死妈管理员/客服 " + profile.getName()));
                    switch ((Notification) this.notification.get()) {
                        case Chat:
                            AChatUtils.sendMsgSeraphim(Text.of(message));
                            break;
                        case Notification:
                            NotificationsManager.add(
                                new dev.abstr3act.addon.notifications.Notification("StaffAlert", message, Color.WHITE, NotificationsHudElement.icon.WARNING)
                            );
                    }

                    if (this.playSound.get()) {
                        for (SoundEvent sound : this.sound_join.get()) {
                            this.mc.player.playSound(sound);
                        }
                    }
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
                if (entryx != null
                    && (this.staffList.get()).contains(entryx.getProfile().getName())
                    && entryx.getProfile().getName() != null
                    && entryx.getProfile().getName().length() > 2) {
                    String message = String.format(this.leaveMessageFormat, this.uuidNameCache.get(entryx.getProfile().getId()));
                    switch ((Notification) this.notification.get()) {
                        case Chat:
                            AChatUtils.sendMsgSeraphim(Text.of(message));
                            break;
                        case Notification:
                            NotificationsManager.add(
                                new dev.abstr3act.addon.notifications.Notification("StaffAlert", message, Color.WHITE, NotificationsHudElement.icon.INFO)
                            );
                    }

                    if (this.playSound.get()) {
                        for (SoundEvent sound : this.sound_exit.get()) {
                            this.mc.player.playSound(sound);
                        }
                    }

                    this.uuidNameCache.remove(entryx.getProfile().getId());
                }
            }
        }
    }

    static enum Mode {
        RunCommand,
        Logout,
        None;
    }

    static enum Notification {
        Chat,
        Notification;
    }
}
