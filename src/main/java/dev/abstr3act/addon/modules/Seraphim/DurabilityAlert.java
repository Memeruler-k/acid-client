package dev.abstr3act.addon.modules.Seraphim;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.abstr3act.addon.Compassion;
import dev.abstr3act.addon.font.FontRenderers;
import dev.abstr3act.addon.module.SeraphimModule;
import dev.abstr3act.addon.utils.timers.Timer;
import meteordevelopment.meteorclient.events.render.Render2DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent.Pre;
import meteordevelopment.meteorclient.settings.BoolSetting.Builder;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

import java.awt.*;

public class DurabilityAlert extends SeraphimModule {
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
    private final Setting<Boolean> friends = this.sgGeneral
        .add(((Builder) ((Builder) ((Builder) new Builder().name("percent")).description(".")).defaultValue(true)).build());
    private final Setting<Integer> percent = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("percent"))
                .description("."))
                .defaultValue(20))
                .min(1)
                .sliderRange(1, 100)
                .build()
        );
    private final Setting<Integer> offsetX = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("offsetX"))
                .description("."))
                .defaultValue(0))
                .sliderRange(-1000, 1000)
                .build()
        );
    private final Setting<Integer> offsetY = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("offsetY"))
                .description("."))
                .defaultValue(0))
                .sliderRange(-1000, 1000)
                .build()
        );
    private final Identifier ICON = Identifier.of("acid", "icons/impl/shield.png");
    private final Timer timer = new Timer();
    int i = 0;
    int j = 0;
    private boolean need_alert = false;
    private int level;

    public DurabilityAlert() {
        super(Compassion.SERAPHIM, "DurabilityAlert", "sb.");
    }

    public static int getDurability(ItemStack stack) {
        return (int) ((stack.getMaxDamage() - stack.getDamage()) / Math.max(0.1, stack.getMaxDamage()) * 100.0);
    }

    public void onActivate() {
        this.level = this.mc.player.getBlockPos().getY();
    }

    @EventHandler
    public void onUpdate(Pre event) {
        if (this.friends.get()) {
            for (PlayerEntity player : this.mc.world.getPlayers()) {
                if (Friends.get().isFriend(player) && player != this.mc.player) {
                    for (ItemStack stack : player.getInventory().armor) {
                        if (!stack.isEmpty() && stack.getItem() instanceof ArmorItem && getDurability(stack) < this.percent.get() && this.timer.passedMs(30000L)) {
                            this.mc.player.networkHandler.sendChatCommand("msg " + player.getName().getString() + "Low armor durability detected, fix ur armor rn");
                            this.timer.reset();
                        }
                    }
                }
            }
        }

        boolean flag = false;

        for (ItemStack stackx : this.mc.player.getInventory().armor) {
            if (!stackx.isEmpty() && stackx.getItem() instanceof ArmorItem && getDurability(stackx) < this.percent.get()) {
                this.need_alert = true;
                flag = true;
            }
        }

        if (!flag && this.need_alert) {
            this.need_alert = false;
        }
    }

    @EventHandler
    public void onRender2D(Render2DEvent context) {
        if (this.need_alert) {
            FontRenderers.exo_regular_32
                .drawCenteredString(
                    context.drawContext.getMatrices(),
                    "Low armor durability",
                    this.mc.getWindow().getScaledWidth() / 2.0F + (this.offsetX.get()).intValue(),
                    this.mc.getWindow().getScaledHeight() / 3.0F + (this.offsetY.get()).intValue(),
                    new Color(16728128).getRGB()
                );
            Color c1 = new Color(16733268);
            RenderSystem.setShaderColor(c1.getRed() / 255.0F, c1.getGreen() / 255.0F, c1.getBlue() / 255.0F, 1.0F);
            context.drawContext
                .drawTexture(
                    this.ICON,
                    (int) (this.mc.getWindow().getScaledWidth() / 2.0F - 40.0F) + this.offsetX.get(),
                    (int) (this.mc.getWindow().getScaledHeight() / 3.0F - 120.0F) + this.offsetY.get(),
                    80,
                    80,
                    0.0F,
                    0.0F,
                    80,
                    80,
                    80,
                    80
                );
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        }
    }
}
