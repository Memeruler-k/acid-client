package dev.abstr3act.addon.modules.Seraphim;

import dev.abstr3act.addon.Compassion;
import dev.abstr3act.addon.module.SeraphimModule;
import dev.abstr3act.addon.utils.timers.Timer;
import meteordevelopment.meteorclient.events.world.TickEvent.Post;
import meteordevelopment.meteorclient.mixininterface.IMinecraftClient;
import meteordevelopment.meteorclient.settings.IntSetting.Builder;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.util.Hand;

public final class AutoRegen extends SeraphimModule {
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
    public final Setting<Integer> Delay = this.sgGeneral
        .add(((Builder) ((Builder) ((Builder) new Builder().name("UseDelay")).description(".")).defaultValue(0)).sliderMax(2000).min(0).build());
    private final Setting<Double> health = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("health"))
                .description("."))
                .defaultValue(15.0)
                .sliderRange(1.0, 36.0)
                .build()
        );
    private final Setting<Boolean> absorption = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("Absorption"))
                .description("."))
                .defaultValue(false))
                .build()
        );
    private final Setting<Boolean> packet = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("PacketMode"))
                .description("."))
                .defaultValue(false))
                .build()
        );
    private final Timer useDelay = new Timer();
    private boolean isActive;

    public AutoRegen() {
        super(Compassion.SERAPHIM, "AutoRegen", "Regen your health while low health");
    }

    @EventHandler
    public void onUpdate(Post e) {
        if (!fullNullCheck()) {
            if (this.GapInOffHand()) {
                if (this.mc.player.getHealth() + (this.absorption.get() ? this.mc.player.getAbsorptionAmount() : 0.0F) <= this.health.get()
                    && this.useDelay.passedMs((long) (this.Delay.get()).intValue())) {
                    if (this.packet.get()) {
                        this.sendPacket(new PlayerInteractItemC2SPacket(Hand.OFF_HAND, 0, this.mc.player.getYaw(), this.mc.player.getPitch()));
                        return;
                    }

                    this.isActive = true;
                    if (this.mc.currentScreen != null && !this.mc.player.isUsingItem()) {
                        ((IMinecraftClient) this.mc).meteor_client$rightClick();
                    } else {
                        this.mc.options.useKey.setPressed(true);
                    }
                } else if (this.isActive) {
                    this.isActive = false;
                    this.mc.options.useKey.setPressed(false);
                }
            } else if (this.isActive) {
                this.isActive = false;
                this.mc.options.useKey.setPressed(false);
            }
        }
    }

    private boolean GapInOffHand() {
        return !this.mc.player.getOffHandStack().isEmpty()
            && (this.mc.player.getOffHandStack().getItem() == Items.GOLDEN_APPLE || this.mc.player.getOffHandStack().getItem() == Items.ENCHANTED_GOLDEN_APPLE);
    }
}
