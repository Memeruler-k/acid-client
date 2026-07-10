package dev.abstr3act.addon.modules.Abnormally;

import dev.abstr3act.addon.Compassion;
import dev.abstr3act.addon.module.AbnormallyModule;
import dev.abstr3act.addon.utils.abnormally.AChatUtils;
import meteordevelopment.meteorclient.events.packets.PacketEvent.Receive;
import meteordevelopment.meteorclient.settings.BoolSetting.Builder;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;

public class AntiMiss extends AbnormallyModule {
    private final SettingGroup sgNormal = this.settings.createGroup("Totem Pops");
    private final Setting<Boolean> popDupe = this.sgNormal
        .add(
            ((Builder) ((Builder) ((Builder) new Builder().name("PopDupe")).description("Dupe totems while popped to refill your inventory")).defaultValue(true))
                .build()
        );
    private final Setting<String> dupeCommand = this.sgNormal
        .add(
            ((meteordevelopment.meteorclient.settings.StringSetting.Builder) ((meteordevelopment.meteorclient.settings.StringSetting.Builder) ((meteordevelopment.meteorclient.settings.StringSetting.Builder) ((meteordevelopment.meteorclient.settings.StringSetting.Builder) new meteordevelopment.meteorclient.settings.StringSetting.Builder()
                .name("DupeCommand"))
                .description("Command to dupe items"))
                .defaultValue("dupe"))
                .visible(this.popDupe::get))
                .build()
        );
    private final Setting<Integer> pop = this.sgNormal
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("PopCount"))
                .description("Pop count of AutoDupe"))
                .min(1)
                .defaultValue(2))
                .visible(this.popDupe::get))
                .sliderRange(1, 10)
                .build()
        );
    private final ItemStack[] items = new ItemStack[10];
    int mainHandSlot;
    int offHandSlot;
    int popCount;
    private int totems;
    private int ticks;
    private int slot = -1;

    public AntiMiss() {
        super(Compassion.ABNORMALLY, "AntiMiss", "Prevent you from miss totem (Mace, Crystal, Anchor)");
    }

    @EventHandler(
        priority = 200
    )
    private void onReceivePacket(Receive event) {
        if (event.packet instanceof EntityStatusS2CPacket p) {
            if (p.getStatus() == 35) {
                Entity entity = p.getEntity(this.mc.world);
                if (entity instanceof PlayerEntity) {
                    if (entity.equals(this.mc.player)) {
                        InvUtils.swap(this.getTotemSlot(), false);
                        AChatUtils.sendDebugMsg(String.valueOf(this.getTotemSlot()));
                        this.mainHandSlot = this.findItem(Items.TOTEM_OF_UNDYING.getDefaultStack(), -1);
                        AChatUtils.sendDebugMsg(String.valueOf(this.mainHandSlot));
                        this.addSlots(this.mc.player.getInventory().selectedSlot, this.mainHandSlot);
                        AChatUtils.sendDebugMsg(this.mainHandSlot + " -> " + this.mc.player.getInventory().selectedSlot);
                        this.offHandSlot = this.findItem(Items.TOTEM_OF_UNDYING.getDefaultStack(), this.mainHandSlot);
                        InvUtils.move().from(this.offHandSlot).toOffhand();
                        AChatUtils.sendDebugMsg(this.offHandSlot + " -> 45");
                        FindItemResult result = InvUtils.find(new Item[]{Items.TOTEM_OF_UNDYING});
                        this.totems = result.count();
                        this.slot = this.mc.player.getInventory().selectedSlot;
                        InvUtils.move().from(result.slot()).toHotbar(8);

                        for (int i = 8; i >= 0; i--) {
                            result = InvUtils.find(itemStack -> itemStack.getItem() == Items.TOTEM_OF_UNDYING, 9, 35);
                            if (this.mc.player.getInventory().getStack(0 + i).isEmpty()) {
                                InvUtils.move().from(result.slot()).to(i);
                            }
                        }

                        InvUtils.swap(8, false);
                        this.popCount++;
                        if (this.popCount >= this.pop.get()) {
                            if (this.popDupe.get()) {
                                ChatUtils.sendPlayerMsg("/" + (String) this.dupeCommand.get());
                            }

                            this.popCount = 0;
                        }
                    }
                }
            }
        }
    }

    private void addSlots(int to, int from) {
        InvUtils.move().from(from).to(to);
    }

    private int findItem(ItemStack itemStack, int excludedSlot) {
        int slot = -1;
        int count = 0;

        for (int i = this.mc.player.getInventory().size() - 2; i >= 9; i--) {
            ItemStack stack = this.mc.player.getInventory().getStack(i);
            if (i != excludedSlot && stack.getItem() == itemStack.getItem() && ItemStack.areItemsAndComponentsEqual(itemStack, stack) && stack.getCount() > count) {
                slot = i;
                count = stack.getCount();
            }
        }

        return slot;
    }

    public int getTotemSlot() {
        int slot = this.mc.player.getInventory().selectedSlot;

        for (int i = 0; i < 9; i++) {
            ItemStack stack = this.mc.player.getInventory().getStack(i);
            if (stack.getItem() == Items.TOTEM_OF_UNDYING) {
                slot = i;
            }
        }

        return slot;
    }
}
