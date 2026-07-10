package dev.abstr3act.addon.modules.Compassion;

import dev.abstr3act.addon.Compassion;
import dev.abstr3act.addon.module.AbnormallyModule;
import meteordevelopment.meteorclient.events.game.GameJoinedEvent;
import meteordevelopment.meteorclient.events.game.GameLeftEvent;
import meteordevelopment.meteorclient.events.world.TickEvent.Post;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.StringSetting.Builder;
import meteordevelopment.meteorclient.utils.misc.Keybind;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.item.Items;

public class AutoLag extends AbnormallyModule {
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
    public final Setting<String> command = this.sgGeneral
        .add(((Builder) ((Builder) ((Builder) new Builder().name("Text")).description("Text you want to send")).defaultValue("/k1")).build());
    public final Setting<Boolean> onlyAnti = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("OnlyAntiLag"))
                .description("."))
                .defaultValue(false))
                .build()
        );
    public final Setting<Boolean> onlyOnPressed = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("OnlyOnPress"))
                .description("."))
                .defaultValue(false))
                .build()
        );
    private final Setting<Keybind> bind = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.KeybindSetting.Builder) ((meteordevelopment.meteorclient.settings.KeybindSetting.Builder) ((meteordevelopment.meteorclient.settings.KeybindSetting.Builder) new meteordevelopment.meteorclient.settings.KeybindSetting.Builder()
                .name("binds"))
                .description("binds"))
                .defaultValue(Keybind.none()))
                .build()
        );
    int t = 0;

    public AutoLag() {
        super(Compassion.COMPASSION, "AutoLag", "");
    }

    public void onActivate() {
        ChatUtils.sendPlayerMsg((String) this.command.get());
    }

    public boolean isMoving() {
        return this.mc.player != null
            && this.mc.world != null
            && this.mc.player.input != null
            && (this.mc.player.input.movementForward != 0.0 || this.mc.player.input.movementSideways != 0.0);
    }

    @EventHandler
    public void onTickEvent(Post event) {
        if (((Keybind) this.bind.get()).isPressed() || !this.onlyOnPressed.get()) {
            if (!this.isMoving()) {
                if (!this.onlyAnti.get()) {
                    if (this.t < 25) {
                        this.t++;
                    } else {
                        ChatUtils.sendPlayerMsg((String) this.command.get());

                        for (int i = 0; i < this.mc.player.getInventory().size(); i++) {
                            if (!this.mc.player.getInventory().getStack(i).getItem().getDefaultStack().equals(Items.AIR.getDefaultStack())) {
                                InvUtils.drop().slot(i);
                            }
                        }

                        if (this.mc.player.getOffHandStack().getItem().getDefaultStack() != Items.AIR.getDefaultStack()) {
                            InvUtils.drop().slotOffhand();
                        }

                        this.t = 0;
                    }
                }
            }
        }
    }

    @EventHandler
    public void onGameJoin(GameJoinedEvent event) {
        this.toggle();
    }

    @EventHandler
    public void onGameLeft(GameLeftEvent event) {
        this.toggle();
    }
}
