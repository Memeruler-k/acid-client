package dev.abstr3act.addon.modules.Abnormally;

import dev.abstr3act.addon.Compassion;
import dev.abstr3act.addon.module.AbnormallyModule;
import meteordevelopment.meteorclient.events.packets.PacketEvent.Receive;
import meteordevelopment.meteorclient.events.world.TickEvent.Post;
import meteordevelopment.meteorclient.events.world.TickEvent.Pre;
import meteordevelopment.meteorclient.settings.IntSetting.Builder;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;

public class SilentTotem extends AbnormallyModule {
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
    private final Setting<Integer> delay = this.sgGeneral
        .add(
            ((Builder) ((Builder) ((Builder) new Builder().name("delay")).description("The ticks between slot movements.")).defaultValue(0))
                .sliderMin(0)
                .sliderMax(10)
                .build()
        );
    private final Setting<Boolean> pauseOnUse = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("Pause On Use"))
                .description("Pause on use items"))
                .defaultValue(true))
                .build()
        );
    private final Setting<Boolean> popDupe = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("PopDupe"))
                .description("Dupe totems while popped to refill your inventory"))
                .defaultValue(true))
                .build()
        );
    private final Setting<Integer> pop = this.sgGeneral
        .add(
            ((Builder) ((Builder) ((Builder) ((Builder) new Builder().name("pop-count")).description("The ticks between slot movements.")).defaultValue(2))
                .min(1)
                .sliderMin(1)
                .sliderMax(10)
                .visible(this.popDupe::get))
                .build()
        );
    private final Setting<String> dupeCommand = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.StringSetting.Builder) ((meteordevelopment.meteorclient.settings.StringSetting.Builder) ((meteordevelopment.meteorclient.settings.StringSetting.Builder) ((meteordevelopment.meteorclient.settings.StringSetting.Builder) new meteordevelopment.meteorclient.settings.StringSetting.Builder()
                .name("DupeCommand"))
                .description("Command to dupe items"))
                .defaultValue("dupe"))
                .visible(this.popDupe::get))
                .build()
        );
    private int totems;
    private int ticks;
    private int popCount;
    private int slot = -1;

    public SilentTotem() {
        super(Compassion.ABNORMALLY, "SilentTotem", "Silent totems");
    }

    @EventHandler(
        priority = 200
    )
    private void onRender(Post event) {
        if (!this.mc.player.isUsingItem() || !this.pauseOnUse.get()) {
            FindItemResult result = InvUtils.find(new Item[]{Items.TOTEM_OF_UNDYING});
            this.totems = result.count();
            if (this.totems > 0) {
                if (this.ticks >= this.delay.get()) {
                    this.ticks = 0;
                    InvUtils.move().from(result.slot()).toHotbar(8);

                    for (int i = 8; i >= 0; i--) {
                        if (this.mc.player.getInventory().getStack(0 + i).isEmpty()) {
                            FindItemResult emptyHotbarSlotResult = InvUtils.find(itemStack -> itemStack.getItem() == Items.TOTEM_OF_UNDYING, 9, 35);
                            if (emptyHotbarSlotResult.count() > 0) {
                                InvUtils.move().from(emptyHotbarSlotResult.slot()).to(i);
                            }
                        }
                    }

                    if (this.mc.player.getInventory().getStack(8).getItem() == Items.TOTEM_OF_UNDYING) {
                        InvUtils.swap(8, false);
                    }
                } else {
                    this.ticks++;
                }
            }
        }
    }

    @EventHandler
    private void onReceivePacket(Receive event) {
        FindItemResult result = InvUtils.find(new Item[]{Items.TOTEM_OF_UNDYING});
        this.totems = result.count();
        if (event.packet instanceof EntityStatusS2CPacket p) {
            if (p.getStatus() == 35) {
                Entity entity = p.getEntity(this.mc.world);
                if (entity instanceof PlayerEntity) {
                    if (entity.equals(this.mc.player)) {
                        InvUtils.move().from(result.slot()).to(this.mc.player.getInventory().selectedSlot);
                        if (this.popDupe.get()) {
                            if (this.popCount > this.pop.get()) {
                                ChatUtils.sendPlayerMsg("/" + (String) this.dupeCommand.get());
                                this.popCount = 0;
                            } else {
                                this.popCount++;
                            }
                        }
                    }
                }
            }
        }
    }

    @EventHandler(
        priority = 200
    )
    private void onPreTick(Pre event) {
        if (!this.mc.player.isUsingItem() || !this.pauseOnUse.get()) {
            if (this.slot != -1) {
                InvUtils.swap(this.slot, false);
            }
        }
    }
}
