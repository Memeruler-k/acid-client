package dev.abstr3act.addon.modules.Seraphim;

import dev.abstr3act.addon.Compassion;
import dev.abstr3act.addon.module.SeraphimModule;
import meteordevelopment.meteorclient.events.world.TickEvent.Pre;
import meteordevelopment.meteorclient.settings.EnumSetting.Builder;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.util.Hand;

public class AutoSoup extends SeraphimModule {
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
    private final Setting<Mode> mode = this.sgGeneral
        .add(((Builder) ((Builder) ((Builder) new Builder().name("Mode")).description(".")).defaultValue(Mode.Auto)).build());
    private final Setting<Integer> health = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("Health"))
                .description("."))
                .defaultValue(7))
                .min(1)
                .visible(() -> ((Mode) this.mode.get()).equals(Mode.Auto)))
                .sliderRange(1, 36)
                .build()
        );

    public AutoSoup() {
        super(Compassion.SERAPHIM, "AutoSoup", ".");
    }

    @EventHandler
    public void onTick(Pre event) {
        switch ((Mode) this.mode.get()) {
            case Legit:
                if (this.mc.options.useKey.isPressed()) {
                    FindItemResult regen = InvUtils.find(new Item[]{Items.MUSHROOM_STEW});
                    if (regen.found()) {
                        InvUtils.swap(regen.slot(), true);
                    }
                }
                break;
            case Auto:
                if (this.mc.player.getHealth() + this.mc.player.getAbsorptionAmount() <= (this.health.get()).intValue()) {
                    FindItemResult regen = InvUtils.find(new Item[]{Items.MUSHROOM_STEW});
                    if (regen.found()) {
                        InvUtils.swap(regen.slot(), true);
                        this.sendSequencedPacket(id -> new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, id, this.mc.player.getYaw(), this.mc.player.getPitch()));
                        InvUtils.swapBack();
                    }
                }
        }
    }

    static enum Mode {
        Legit,
        Auto;
    }
}
