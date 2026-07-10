package dev.abstr3act.addon.modules.Lacrymira;

import dev.abstr3act.addon.Compassion;
import dev.abstr3act.addon.module.AbnormallyModule;
import dev.abstr3act.addon.utils.TargetUtils;
import meteordevelopment.meteorclient.events.world.TickEvent.Pre;
import meteordevelopment.meteorclient.settings.DoubleSetting.Builder;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.utils.entity.SortPriority;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.GameMode;

import java.util.ArrayList;
import java.util.List;

public class CommandAura extends AbnormallyModule {
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
    public final Setting<String> command = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.StringSetting.Builder) ((meteordevelopment.meteorclient.settings.StringSetting.Builder) ((meteordevelopment.meteorclient.settings.StringSetting.Builder) new meteordevelopment.meteorclient.settings.StringSetting.Builder()
                .name("Command"))
                .description("Command for send private message"))
                .defaultValue("/kill %s"))
                .build()
        );
    private final SettingGroup sgTargeting = this.settings.createGroup("Targeting");
    private final SettingGroup sgDelay = this.settings.createGroup("Delay");
    private final Setting<Double> range = this.sgTargeting
        .add(
            ((Builder) ((Builder) new Builder().name("range")).description("The maximum range the entity can be to attack it."))
                .defaultValue(6.0)
                .min(0.0)
                .sliderMax(20.0)
                .build()
        );
    private final Setting<SortPriority> priority = this.sgTargeting
        .add(
            ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) new meteordevelopment.meteorclient.settings.EnumSetting.Builder()
                .name("priority"))
                .description("How to filter targets within range."))
                .defaultValue(SortPriority.ClosestAngle))
                .build()
        );
    private final Setting<Integer> delay = this.sgDelay
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("delay"))
                .description("How fast you send message."))
                .defaultValue(0))
                .min(0)
                .sliderMax(60)
                .build()
        );
    private final List<Entity> targets = new ArrayList<>();
    private int hitDelayTimer;

    public CommandAura() {
        super(Compassion.LACRYMIRA, "CommandAura", "CommandAura");
    }

    public void onDeactivate() {
        this.hitDelayTimer = 0;
        this.targets.clear();
    }

    @EventHandler
    private void onTick(Pre event) {
        if (this.mc.player.isAlive() && PlayerUtils.getGameMode() != GameMode.SPECTATOR) {
            TargetUtils.getList(this.targets, this::entityCheck, (SortPriority) this.priority.get(), 1);
            if (this.delayCheck()) {
                this.targets.forEach(this::lag);
            }
        }
    }

    private boolean delayCheck() {
        if (this.hitDelayTimer > 0) {
            this.hitDelayTimer--;
            return false;
        } else {
            this.hitDelayTimer = this.delay.get();
            return true;
        }
    }

    private boolean entityCheck(Entity entity) {
        if (!(entity instanceof PlayerEntity player)) {
            return false;
        } else if (!entity.equals(this.mc.player) && !entity.equals(this.mc.cameraEntity)) {
            if ((!(entity instanceof PlayerEntity) || !((PlayerEntity) entity).isDead()) && entity.isAlive()) {
                if (!PlayerUtils.isWithin(entity, this.range.get())) {
                    return false;
                } else {
                    return player.isCreative() ? false : Friends.get().shouldAttack(player);
                }
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    private void lag(Entity target) {
        ChatUtils.sendPlayerMsg(((String) this.command.get()).replace("%s", target.getName().getString()));
    }

    public Entity getTarget() {
        return !this.targets.isEmpty() ? this.targets.get(0) : null;
    }
}
