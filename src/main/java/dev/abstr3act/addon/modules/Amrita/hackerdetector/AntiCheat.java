package dev.abstr3act.addon.modules.Amrita.hackerdetector;

import dev.abstr3act.addon.Compassion;
import dev.abstr3act.addon.events.EventPlayerUpdate;
import dev.abstr3act.addon.module.AmritaModule;
import dev.abstr3act.addon.modules.Amrita.hackerdetector.impl.*;
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

public class AntiCheat extends AmritaModule {
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
    private final SettingGroup sgChecks = this.settings.createGroup("Checks");
    public final Setting<Boolean> angle = this.sgChecks
        .add(((Builder) ((Builder) ((Builder) new Builder().name("Angle")).description("")).defaultValue(false)).build());
    public final Setting<Boolean> ab = this.sgChecks
        .add(((Builder) ((Builder) ((Builder) new Builder().name("AutoBlock")).description("")).defaultValue(false)).build());
    public final Setting<Boolean> lgScaffold = this.sgChecks
        .add(((Builder) ((Builder) ((Builder) new Builder().name("LegitScaffold")).description("")).defaultValue(false)).build());
    public final Setting<Boolean> invalidMotion = this.sgChecks
        .add(((Builder) ((Builder) ((Builder) new Builder().name("InvalidMotion")).description("")).defaultValue(false)).build());
    public final Setting<Boolean> noFall = this.sgChecks
        .add(((Builder) ((Builder) ((Builder) new Builder().name("NoFall")).description("")).defaultValue(false)).build());
    public final Setting<Boolean> noSlow = this.sgChecks
        .add(((Builder) ((Builder) ((Builder) new Builder().name("NoSlow")).description("")).defaultValue(false)).build());
    public final Setting<Boolean> scaffold = this.sgChecks
        .add(((Builder) ((Builder) ((Builder) new Builder().name("Scaffold")).description("")).defaultValue(false)).build());
    public final Setting<Boolean> velocity = this.sgChecks
        .add(((Builder) ((Builder) ((Builder) new Builder().name("Velocity")).description("")).defaultValue(false)).build());
    public final Setting<Boolean> omniSprint = this.sgChecks
        .add(((Builder) ((Builder) ((Builder) new Builder().name("OmniSprint")).description("")).defaultValue(false)).build());
    public final Setting<Boolean> selfCheck = this.sgChecks
        .add(((Builder) ((Builder) ((Builder) new Builder().name("Self")).description("")).defaultValue(false)).build());
    private final Set<PlayerEntity> hackers = new HashSet<>();
    private final ArrayList<Check> checks = new ArrayList<>();
    private final Map<String, Setting<Boolean>> options = new HashMap<>();

    public AntiCheat() {
        super(Compassion.AMRITA, "AntiCheat", "?");
        this.addChecks(
            new AngleCheck(),
            new AutoBlockCheck(),
            new LegitScaffoldCheck(),
            new MotionCheck(),
            new NoFallCheck(),
            new NoSlowCheck(),
            new ScaffoldCheck(),
            new VelocityCheck(),
            new OmniSprintCheck()
        );
        this.options.put("Angle".toLowerCase(), this.angle);
        this.options.put("AutoBlock".toLowerCase(), this.ab);
        this.options.put("LegitScaffold".toLowerCase(), this.lgScaffold);
        this.options.put("InvalidMotion".toLowerCase(), this.invalidMotion);
        this.options.put("NoFall".toLowerCase(), this.noFall);
        this.options.put("NoSlow".toLowerCase(), this.noSlow);
        this.options.put("Scaffold".toLowerCase(), this.scaffold);
        this.options.put("Velocity".toLowerCase(), this.velocity);
        this.options.put("OmniSprint".toLowerCase(), this.omniSprint);
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
