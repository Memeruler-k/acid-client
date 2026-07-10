package dev.abstr3act.addon.modules.Amrita.crystalac;

import dev.abstr3act.addon.Compassion;
import dev.abstr3act.addon.events.EventPlayerUpdate;
import dev.abstr3act.addon.module.AmritaModule;
import dev.abstr3act.addon.modules.Amrita.crystalac.impl.*;
import dev.abstr3act.addon.modules.Fragment.AntiBot;
import meteordevelopment.meteorclient.events.packets.PacketEvent.Receive;
import meteordevelopment.meteorclient.settings.BoolSetting.Builder;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.c2s.play.TeleportConfirmC2SPacket;
import net.minecraft.network.packet.s2c.play.EntityS2CPacket;

import java.util.*;

public class CrystalAC extends AmritaModule {
    private final SettingGroup sgChecks = this.settings.createGroup("Checks");
    public final Setting<Boolean> dig = this.sgChecks
        .add(((Builder) ((Builder) ((Builder) new Builder().name("Digger")).description("")).defaultValue(false)).build());
    public final Setting<Boolean> elytra = this.sgChecks
        .add(((Builder) ((Builder) ((Builder) new Builder().name("elytra")).description("")).defaultValue(false)).build());
    public final Setting<Boolean> mace = this.sgChecks
        .add(((Builder) ((Builder) ((Builder) new Builder().name("mace")).description("")).defaultValue(false)).build());
    public final Setting<Boolean> shield = this.sgChecks
        .add(((Builder) ((Builder) ((Builder) new Builder().name("shield")).description("")).defaultValue(false)).build());
    public final Setting<Boolean> turtleMaster = this.sgChecks
        .add(((Builder) ((Builder) ((Builder) new Builder().name("turtleMaster")).description("")).defaultValue(false)).build());
    public final Setting<Boolean> selfCheck = this.sgChecks
        .add(((Builder) ((Builder) ((Builder) new Builder().name("Self")).description("")).defaultValue(false)).build());
    public final Setting<Boolean> shout = this.sgChecks
        .add(((Builder) ((Builder) ((Builder) new Builder().name("Shout")).description("")).defaultValue(false)).build());
    private final Set<PlayerEntity> hackers = new HashSet<>();
    private final ArrayList<Check> checks = new ArrayList<>();
    private final Map<String, Setting<Boolean>> options = new HashMap<>();

    public CrystalAC() {
        super(Compassion.AMRITA, "CrystalAC", "?");
        this.addChecks(new DiggerCheck(), new ShieldCheck(), new TurtleMasterCheck(), new ElytraCheck(), new MaceCheck());
        this.options.put("Digger".toLowerCase(), this.dig);
        this.options.put("Elytra".toLowerCase(), this.elytra);
        this.options.put("Mace".toLowerCase(), this.mace);
        this.options.put("Shield".toLowerCase(), this.shield);
        this.options.put("TurtleMaster".toLowerCase(), this.turtleMaster);
    }

    public boolean isEnabled(String name) {
        Setting<Boolean> setting = this.options.get(name.toLowerCase());
        return setting != null && setting.get();
    }

    @EventHandler
    public void onUpdate(EventPlayerUpdate event) {
        for (PlayerEntity player : this.mc.world.getPlayers()) {
            for (Check check : this.checks) {
                if ((this.selfCheck.get() || player != this.mc.player)
                    && !player.isDead()
                    && !Friends.get().isFriend(player)
                    && (!((AntiBot) Modules.get().get(AntiBot.class)).isActive() || !((AntiBot) Modules.get().get(AntiBot.class)).inBotList(player))
                    && this.isEnabled(check.getName())) {
                    check.onUpdate(player);
                }
            }
        }
    }

    @EventHandler
    public final void onPacketReceive(Receive event) {
        if (event.packet instanceof EntityS2CPacket || event.packet instanceof TeleportConfirmC2SPacket) {
            for (PlayerEntity player : this.mc.world.getPlayers()) {
                for (Check check : this.checks) {
                    if ((this.selfCheck.get() || player != this.mc.player)
                        && !player.isDead()
                        && !Friends.get().isFriend(player)
                        && (!((AntiBot) Modules.get().get(AntiBot.class)).isActive() || !((AntiBot) Modules.get().get(AntiBot.class)).inBotList(player))
                        && this.isEnabled(check.getName())) {
                        check.onPacketReceive(event, player);
                    }
                }
            }
        }
    }

    public void onDeactivate() {
        this.hackers.clear();
    }

    public void addChecks(Check... checks) {
        this.checks.addAll(Arrays.asList(checks));
    }

    public void mark(PlayerEntity ent) {
        this.hackers.add(ent);
    }

    public boolean isHacker(PlayerEntity ent) {
        for (PlayerEntity hacker : this.hackers) {
            if (ent.getName().equals(hacker.getName())) {
                return true;
            }
        }

        return false;
    }
}
