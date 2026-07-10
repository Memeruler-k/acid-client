package dev.abstr3act.addon.modules.Compassion;

import dev.abstr3act.addon.Compassion;
import dev.abstr3act.addon.module.CompassionModule;
import meteordevelopment.meteorclient.events.world.TickEvent.Post;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.StringSetting.Builder;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.item.Item;
import net.minecraft.item.Items;

public class AutoSell extends CompassionModule {
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
    private final Setting<String> command = this.sgGeneral
        .add(((Builder) ((Builder) ((Builder) new Builder().name("SellCommand")).description(".")).defaultValue("/sell bamboo")).build());
    private final Setting<Item> items = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.ItemSetting.Builder) ((meteordevelopment.meteorclient.settings.ItemSetting.Builder) ((meteordevelopment.meteorclient.settings.ItemSetting.Builder) new meteordevelopment.meteorclient.settings.ItemSetting.Builder()
                .name("TargetItem"))
                .description("."))
                .defaultValue(Items.BAMBOO))
                .build()
        );
    private final Setting<Integer> count = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("MinimumCount"))
                .description("."))
                .min(1)
                .sliderRange(1, 64)
                .defaultValue(1))
                .build()
        );
    private final Setting<Double> delay = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("Delay (S)"))
                .description("."))
                .min(0.0)
                .sliderRange(1.0, 100.0)
                .defaultValue(0.5)
                .build()
        );
    int i = 0;

    public AutoSell() {
        super(Compassion.COMPASSION, "AutoSell", "Automatically sell specific items");
    }

    @EventHandler
    private void onTick(Post event) {
        FindItemResult item = InvUtils.find(new Item[]{(Item) this.items.get()});
        if (this.i >= this.delay.get() * 20.0) {
            if (item.found() && item.count() >= this.count.get()) {
                ChatUtils.sendPlayerMsg((String) this.command.get());
            }

            this.i = 0;
        } else {
            this.i++;
        }
    }
}
