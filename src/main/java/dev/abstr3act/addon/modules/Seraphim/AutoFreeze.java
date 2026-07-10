package dev.abstr3act.addon.modules.Seraphim;

import dev.abstr3act.addon.Compassion;
import dev.abstr3act.addon.module.SeraphimModule;
import dev.abstr3act.addon.notifications.Notification;
import dev.abstr3act.addon.notifications.NotificationsManager;
import meteordevelopment.meteorclient.events.entity.player.AttackEntityEvent;
import meteordevelopment.meteorclient.events.game.GameJoinedEvent;
import meteordevelopment.meteorclient.events.world.TickEvent.Post;
import meteordevelopment.meteorclient.settings.IntSetting.Builder;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;

public class AutoFreeze extends SeraphimModule {
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
    public final Setting<Integer> cooldown = this.sgGeneral
        .add(((Builder) ((Builder) ((Builder) new Builder().name("cooldown")).description(".")).defaultValue(3)).sliderMax(10000).min(0).build());
    public final Setting<Integer> swapBackDelay = this.sgGeneral
        .add(((Builder) ((Builder) ((Builder) new Builder().name("swapBackDelay")).description(".")).defaultValue(3)).sliderMax(10).min(0).build());
    int t = 0;
    int o = 0;

    public AutoFreeze() {
        super(Compassion.SERAPHIM, "AutoFreeze", "Freezes target in SMP");
    }

    @EventHandler
    public void onAttackEntity(AttackEntityEvent event) {
        if (event.entity instanceof PlayerEntity && this.t >= this.cooldown.get()) {
            InvUtils.swap(InvUtils.find(new Item[]{Items.ICE}).slot(), true);
            NotificationsManager.add(new Notification("AutoFreeze", "Successfully froze player: " + event.entity.getName().getString()));
            this.t = 0;
            this.o = this.swapBackDelay.get();
        }
    }

    @EventHandler
    public void onJoin(GameJoinedEvent event) {
        this.t = this.cooldown.get();
    }

    @EventHandler
    public void onTickEvent(Post event) {
        if (this.t < this.cooldown.get()) {
            this.t++;
        }

        if (this.o > 0) {
            this.o--;
        } else {
            InvUtils.swapBack();
        }
    }

    public String getInfoString() {
        return "[" + this.t + " | " + this.o + "]";
    }
}
