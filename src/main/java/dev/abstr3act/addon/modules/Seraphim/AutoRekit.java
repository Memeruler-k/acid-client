package dev.abstr3act.addon.modules.Seraphim;

import dev.abstr3act.addon.Compassion;
import dev.abstr3act.addon.events.EventPlayerUpdate;
import dev.abstr3act.addon.hud.notifications.NotificationsHudElement;
import dev.abstr3act.addon.module.SeraphimModule;
import dev.abstr3act.addon.notifications.Notification;
import dev.abstr3act.addon.notifications.NotificationsManager;
import meteordevelopment.meteorclient.events.entity.player.AttackEntityEvent;
import meteordevelopment.meteorclient.events.game.ReceiveMessageEvent;
import meteordevelopment.meteorclient.settings.EnumSetting.Builder;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.player.PlayerEntity;

import java.awt.*;

public class AutoRekit extends SeraphimModule {
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
    private final Setting<Mode> mode = this.sgGeneral
        .add(((Builder) ((Builder) ((Builder) new Builder().name("Mode")).description(".")).defaultValue(Mode.TargetLock)).build());
    private final Setting<String> targetString = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.StringSetting.Builder) ((meteordevelopment.meteorclient.settings.StringSetting.Builder) ((meteordevelopment.meteorclient.settings.StringSetting.Builder) ((meteordevelopment.meteorclient.settings.StringSetting.Builder) new meteordevelopment.meteorclient.settings.StringSetting.Builder()
                .name("TargetString"))
                .description("."))
                .defaultValue("You are no longer in combat"))
                .visible(() -> ((Mode) this.mode.get()).equals(Mode.ChatMessage)))
                .build()
        );
    private final Setting<String> command = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.StringSetting.Builder) ((meteordevelopment.meteorclient.settings.StringSetting.Builder) ((meteordevelopment.meteorclient.settings.StringSetting.Builder) new meteordevelopment.meteorclient.settings.StringSetting.Builder()
                .name("Command"))
                .description("."))
                .defaultValue("/k1"))
                .build()
        );
    PlayerEntity target;

    public AutoRekit() {
        super(Compassion.SERAPHIM, "AutoRekit", ".");
    }

    @EventHandler
    public void onAttackEntity(AttackEntityEvent event) {
        if (this.mode.get() == Mode.TargetLock) {
            if (event.entity instanceof PlayerEntity) {
                this.target = (PlayerEntity) event.entity;
            }
        }
    }

    @EventHandler
    public void onReceiveMessage(ReceiveMessageEvent event) {
        if (this.mode.get() == Mode.ChatMessage) {
            if (event.getMessage().getString().contains((CharSequence) this.targetString.get())) {
                ChatUtils.sendPlayerMsg((String) this.command.get());
                NotificationsManager.add(new Notification("AutoRekit", "Successfully rekitted", Color.WHITE, NotificationsHudElement.icon.ENABLE));
            }
        }
    }

    @EventHandler
    public void onUpdate(EventPlayerUpdate event) {
        if (this.mode.get() == Mode.TargetLock) {
            if (this.target != null) {
                if (this.target.isDead() || this.target.getHealth() <= 0.0F || this.target.deathTime > 0) {
                    ChatUtils.sendPlayerMsg((String) this.command.get());
                    this.target = null;
                }
            }
        }
    }

    static enum Mode {
        TargetLock,
        ChatMessage;
    }
}
