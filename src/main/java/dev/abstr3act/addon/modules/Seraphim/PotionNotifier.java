package dev.abstr3act.addon.modules.Seraphim;

import dev.abstr3act.addon.Compassion;
import dev.abstr3act.addon.hud.notifications.NotificationsHudElement;
import dev.abstr3act.addon.module.BaseModule;
import dev.abstr3act.addon.notifications.Notification;
import dev.abstr3act.addon.notifications.NotificationsManager;
import meteordevelopment.meteorclient.events.entity.EntityAddedEvent;
import meteordevelopment.meteorclient.events.game.GameJoinedEvent;
import meteordevelopment.meteorclient.events.world.TickEvent.Post;
import meteordevelopment.meteorclient.settings.BoolSetting.Builder;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.thrown.PotionEntity;

import java.awt.*;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class PotionNotifier extends Module {
    private static final Set<UUID> trackedPlayers = new HashSet<>();
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
    private final Setting<Boolean> ignoreSelf = this.sgGeneral
        .add(((Builder) ((Builder) ((Builder) new Builder().name("IgnoreSelf")).description(".")).defaultValue(true)).build());

    public PotionNotifier() {
        super(Compassion.SERAPHIM, "PotionNotifier", ".");
    }

    public void onActivate() {
    }

    @EventHandler
    private void onEntityAdded(EntityAddedEvent event) {
        if (event.entity instanceof PotionEntity entity && entity.getOwner() != null) {
            NotificationsManager.add(
                new Notification(
                    "PotionsDetector",
                    entity.getOwner().getName().getString() + " throws a potion at " + entity.getBlockX() + " " + entity.getBlockY() + " " + entity.getBlockZ() + "!",
                    Color.WHITE,
                    NotificationsHudElement.icon.WARNING
                )
            );
        }
    }

    @EventHandler
    private void onGameJoin(GameJoinedEvent event) {
        trackedPlayers.clear();
    }

    @EventHandler
    private void onTickEvent(Post event) {
        if (!BaseModule.fullNullCheck()) {
            for (PlayerEntity player : this.mc.world.getPlayers()) {
                if (player != this.mc.player || !this.ignoreSelf.get()) {
                    if (player.hasStatusEffect(StatusEffects.RESISTANCE) && player.hasStatusEffect(StatusEffects.SLOWNESS) && !trackedPlayers.contains(player.getUuid())) {
                        NotificationsManager.add(
                            new Notification(
                                "PotionsDetector", player.getName().getString() + " has Turtle Master effect!", Color.WHITE, NotificationsHudElement.icon.WARNING
                            )
                        );
                        trackedPlayers.add(player.getUuid());
                    } else if (!player.hasStatusEffect(StatusEffects.RESISTANCE) && !player.hasStatusEffect(StatusEffects.SLOWNESS)) {
                        trackedPlayers.remove(player.getUuid());
                    }
                }
            }
        }
    }
}
