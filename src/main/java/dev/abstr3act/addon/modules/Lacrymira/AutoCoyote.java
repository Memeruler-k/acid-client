package dev.abstr3act.addon.modules.Lacrymira;

import dev.abstr3act.addon.Compassion;
import dev.abstr3act.addon.module.LacrymiraModule;
import meteordevelopment.meteorclient.events.entity.player.*;
import meteordevelopment.meteorclient.events.game.GameJoinedEvent;
import meteordevelopment.meteorclient.events.game.GameLeftEvent;
import meteordevelopment.meteorclient.events.world.TickEvent.Post;
import meteordevelopment.meteorclient.events.world.TickEvent.Pre;
import meteordevelopment.meteorclient.settings.BoolSetting.Builder;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.orbit.EventHandler;

public class AutoCoyote extends LacrymiraModule {
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
    private final SettingGroup sgTriggers = this.settings.createGroup("Triggers");
    private final Setting<Boolean> gameJoin = this.sgTriggers
        .add(((Builder) ((Builder) ((Builder) new Builder().name("GameJoinedEvent")).description(".")).defaultValue(false)).build());
    private final Setting<Boolean> gameLeft = this.sgTriggers
        .add(((Builder) ((Builder) ((Builder) new Builder().name("GameLeftEvent")).description(".")).defaultValue(false)).build());
    private final Setting<Boolean> tickPre = this.sgTriggers
        .add(((Builder) ((Builder) ((Builder) new Builder().name("PreTickEvent")).description(".")).defaultValue(false)).build());
    private final Setting<Boolean> tickPost = this.sgTriggers
        .add(((Builder) ((Builder) ((Builder) new Builder().name("PostTickEvent")).description(".")).defaultValue(false)).build());
    private final Setting<Boolean> move = this.sgTriggers
        .add(((Builder) ((Builder) ((Builder) new Builder().name("MoveEvent")).description(".")).defaultValue(false)).build());
    private final Setting<Boolean> attack = this.sgTriggers
        .add(((Builder) ((Builder) ((Builder) new Builder().name("AttackEvent")).description(".")).defaultValue(false)).build());
    private final Setting<Boolean> interactEntity = this.sgTriggers
        .add(((Builder) ((Builder) ((Builder) new Builder().name("InteractEntityEvent")).description(".")).defaultValue(false)).build());
    private final Setting<Boolean> interactBlock = this.sgTriggers
        .add(((Builder) ((Builder) ((Builder) new Builder().name("InteractBlockEvent")).description(".")).defaultValue(false)).build());
    private final Setting<Boolean> interactItem = this.sgTriggers
        .add(((Builder) ((Builder) ((Builder) new Builder().name("InteractItemEvent")).description(".")).defaultValue(false)).build());

    public AutoCoyote() {
        super(Compassion.CLIENT, "AutoCoyote", "idk");
    }

    @EventHandler
    private void onGameJoinEvent(GameJoinedEvent event) {
        if (this.gameJoin.get()) {
            this.trigger(200.0F);
        }
    }

    @EventHandler
    private void onGameLeftEvent(GameLeftEvent event) {
        if (this.gameLeft.get()) {
            this.trigger(200.0F);
        }
    }

    @EventHandler
    private void onTickPre(Pre event) {
        if (this.tickPre.get()) {
            this.trigger(200.0F);
        }
    }

    @EventHandler
    private void onTickPost(Post event) {
        if (this.tickPost.get()) {
            this.trigger(200.0F);
        }
    }

    @EventHandler
    private void onMove(PlayerMoveEvent event) {
        if (this.move.get()) {
            this.trigger(200.0F);
        }
    }

    @EventHandler
    private void onAttack(AttackEntityEvent event) {
        if (this.attack.get()) {
            this.trigger(200.0F);
        }
    }

    @EventHandler
    private void onInteractEntityEvent(InteractEntityEvent event) {
        if (this.interactEntity.get()) {
            this.trigger(200.0F);
        }
    }

    @EventHandler
    private void onInteractBlockEvent(InteractBlockEvent event) {
        if (this.interactBlock.get()) {
            this.trigger(200.0F);
        }
    }

    @EventHandler
    private void onInteractItemEvent(InteractItemEvent event) {
        if (this.interactItem.get()) {
            this.trigger(200.0F);
        }
    }

    private void trigger(float value) {
    }
}
