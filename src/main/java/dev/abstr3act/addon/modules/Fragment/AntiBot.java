package dev.abstr3act.addon.modules.Fragment;

import com.mojang.authlib.GameProfile;
import dev.abstr3act.addon.Compassion;
import dev.abstr3act.addon.events.EventPlayerUpdate;
import dev.abstr3act.addon.events.legacy.WorldEvent;
import dev.abstr3act.addon.module.LacrymiraModule;
import dev.abstr3act.addon.utils.timers.TickTimer;
import meteordevelopment.meteorclient.events.packets.PacketEvent.Receive;
import meteordevelopment.meteorclient.events.world.TickEvent.Post;
import meteordevelopment.meteorclient.settings.EnumSetting.Builder;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket.Entry;
import net.minecraft.network.packet.s2c.play.PlayerRemoveS2CPacket;

import java.util.*;

public class AntiBot extends LacrymiraModule {
    private static final Set<UUID> suspectList = new HashSet<>();
    private static final Set<UUID> botList = new HashSet<>();
    public static AntiBot INSTANCE;
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
    private final Setting<Mode> mode = this.sgGeneral
        .add(((Builder) ((Builder) ((Builder) new Builder().name("Mode")).description(".")).defaultValue(Mode.Advanced)).build());
    private final TickTimer armorTimer = new TickTimer();
    private boolean armorChecker = false;

    public AntiBot() {
        super(Compassion.LACRYMIRA, "AntiBot", "Advanced AntiBot");
        INSTANCE = this;
    }

    public boolean inBotList(LivingEntity playerEntity) {
        return botList.contains(playerEntity.getUuid())
            || "advanced".equalsIgnoreCase(((Mode) this.mode.get()).name) && !this.mc.getNetworkHandler().getPlayerUuids().contains(playerEntity.getUuid());
    }

    public void onActivate() {
        botList.clear();
        suspectList.clear();
        this.armorTimer.reset();
        this.armorChecker = false;
    }

    public void onDeactivate() {
    }

    @EventHandler
    public void onWorld(WorldEvent event) {
        botList.clear();
        suspectList.clear();
        this.armorTimer.reset();
        this.armorChecker = false;
    }

    @EventHandler
    public void onUpdate(EventPlayerUpdate event) {
        if (this.mc.player != null) {
            if ("hypixel".equalsIgnoreCase(((Mode) this.mode.get()).name)) {
                List<UUID> listPlayerProfiles = new ArrayList<>();
                this.mc.getNetworkHandler().getPlayerList().forEach(i -> listPlayerProfiles.add(i.getProfile().getId()));
                this.mc.world.getPlayers().forEach(i -> {
                    UUID profile = i.getGameProfile().getId();
                    if (profile != null) {
                        if (!listPlayerProfiles.contains(profile) && !botList.contains(profile)) {
                            botList.add(profile);
                        } else if (listPlayerProfiles.contains(profile) && botList.contains(profile)) {
                            botList.remove(profile);
                        }
                    }
                });
            }
        }
    }

    @EventHandler
    public void onTick(Post event) {
        if (this.mc.player != null) {
            if ("shotbow".equalsIgnoreCase(((Mode) this.mode.get()).name)) {
                if (suspectList.isEmpty()) {
                    return;
                }

                for (PlayerEntity entity : this.mc.world.getPlayers()) {
                    if (suspectList.contains(entity.getUuid())) {
                        Iterable<ItemStack> armor = null;
                        if (!this.isFullyArmored(entity)) {
                            armor = entity.getArmorItems();
                            this.armorChecker = true;
                        }

                        if (this.armorChecker) {
                            this.armorTimer.update();
                            if (this.armorTimer.hasTimePassed(2)) {
                                if ((this.isFullyArmored(entity) || this.updatesArmor(entity, armor)) && entity.getGameProfile().getProperties().isEmpty()) {
                                    botList.add(entity.getUuid());
                                }

                                suspectList.remove(entity.getUuid());
                                this.armorTimer.reset();
                                this.armorChecker = false;
                            }
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onReceivedPacket(Receive event) {
        if (this.mc.player != null) {
            Object packet = event.packet;
            if ("shotbow".equalsIgnoreCase(((Mode) this.mode.get()).name)) {
                if (packet instanceof PlayerListS2CPacket) {
                    for (Entry entry : ((PlayerListS2CPacket) packet).getPlayerAdditionEntries()) {
                        GameProfile profile = entry.profile();
                        if (profile != null && entry.latency() >= 2 && profile.getProperties().size() <= 0 && !this.isGameProfileUnique(profile)) {
                            if (this.isDuplicated(profile)) {
                                botList.add(entry.profileId());
                            } else {
                                suspectList.add(entry.profileId());
                            }
                        }
                    }
                } else if (packet instanceof PlayerRemoveS2CPacket) {
                    for (UUID uuid : ((PlayerRemoveS2CPacket) packet).profileIds()) {
                        suspectList.remove(uuid);
                        botList.remove(uuid);
                    }
                }
            }
        }
    }

    private boolean isGameProfileUnique(GameProfile originalProfile) {
        return this.mc
            .getNetworkHandler()
            .getPlayerList()
            .stream()
            .filter(entry -> entry.getProfile().getName().equals(originalProfile.getName()) && entry.getProfile().getId().equals(originalProfile.getId()))
            .count()
            == 1L;
    }

    private boolean isDuplicated(GameProfile originalProfile) {
        return this.mc
            .getNetworkHandler()
            .getPlayerList()
            .stream()
            .filter(entry -> entry.getProfile().getName().equals(originalProfile.getName()) && !entry.getProfile().getId().equals(originalProfile.getId()))
            .count()
            == 1L;
    }

    private boolean isFullyArmored(PlayerEntity entity) {
        for (int i = 0; i <= 3; i++) {
            ItemStack stack = entity.getInventory().getArmorStack(i);
            if (!(stack.getItem() instanceof ArmorItem) || !stack.hasEnchantments()) {
                return false;
            }
        }

        return true;
    }

    private boolean updatesArmor(PlayerEntity entity, Iterable<ItemStack> prevArmor) {
        return !Objects.equals(prevArmor, entity.getArmorItems());
    }

    static enum Mode {
        Advanced("Advanced"),
        Hypixel("Hypixel"),
        Shotbow("Shotbow");

        private final String name;

        private Mode(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return this.name;
        }
    }
}
