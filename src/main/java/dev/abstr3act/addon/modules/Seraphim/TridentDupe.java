package dev.abstr3act.addon.modules.Seraphim;

import dev.abstr3act.addon.Compassion;
import dev.abstr3act.addon.module.SeraphimModule;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import meteordevelopment.meteorclient.events.game.GameLeftEvent;
import meteordevelopment.meteorclient.events.game.OpenScreenEvent;
import meteordevelopment.meteorclient.events.packets.PacketEvent.Send;
import meteordevelopment.meteorclient.events.world.TickEvent.Pre;
import meteordevelopment.meteorclient.settings.DoubleSetting.Builder;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.screen.DisconnectedScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.*;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket.Action;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.*;

public class TridentDupe extends SeraphimModule {
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
    private final Setting<Double> delay = this.sgGeneral
        .add(
            ((Builder) ((Builder) new Builder().name("dupe-delay")).description("Raise this if it isn't working. This is how fast you'll dupe. 5 is good for most."))
                .defaultValue(5.0)
                .build()
        );
    private final Setting<Boolean> dropTridents = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("dropTridents"))
                .description("Drops tridents in your last hotbar slot."))
                .defaultValue(true))
                .build()
        );
    private final Setting<Boolean> durabilityManagement = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("durabilityManagement"))
                .description("(More AFKable) Attempts to dupe the highest durability trident in your hotbar."))
                .defaultValue(true))
                .build()
        );
    private final Queue<Packet<?>> delayedPackets = new LinkedList<>();
    private final List<Pair<Long, Runnable>> scheduledTasks = new ArrayList<>();
    private final List<Pair<Long, Runnable>> scheduledTasks2 = new ArrayList<>();
    private boolean cancel = true;

    public TridentDupe() {
        super(Compassion.SERAPHIM, "TridentDupe", "Dupes tridents in first hotbar slot. / / Killet / / Laztec / / Ionar");
    }

    @EventHandler(
        priority = 201
    )
    private void onSendPacket(Send event) {
        if (!(event.packet instanceof PlayerMoveC2SPacket) && !(event.packet instanceof CloseHandledScreenC2SPacket)) {
            if (event.packet instanceof ClickSlotC2SPacket || event.packet instanceof PlayerActionC2SPacket) {
                if (this.cancel) {
                    MutableText packetStr = Text.literal(event.packet.toString()).formatted(Formatting.WHITE);
                    event.cancel();
                }
            }
        }
    }

    public void onActivate() {
        if (this.mc.player != null) {
            for (int i = 0; i < 9; i++) {
                if (this.mc.player.getInventory().getStack(i).getItem() == Items.TRIDENT) {
                    Integer modifiedStacks = this.mc.player.getInventory().getStack(i).getDamage();
                }
            }

            new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, 10, this.mc.player.getYaw(), this.mc.player.getPitch());
            Int2ObjectMap<ItemStack> modifiedStacks = new Int2ObjectOpenHashMap();
            modifiedStacks.put(3, this.mc.player.getInventory().getStack(this.mc.player.getInventory().selectedSlot));
            modifiedStacks.put(36, this.mc.player.getInventory().getStack(this.mc.player.getInventory().selectedSlot));
            new ClickSlotC2SPacket(0, 15, 0, 0, SlotActionType.SWAP, new ItemStack(Items.AIR), modifiedStacks);
            this.scheduledTasks.clear();
            this.dupe();
        }
    }

    private void dupe() {
        int delayInt = (this.delay.get()).intValue() * 100;
        System.out.println(delayInt);
        int lowestHotbarSlot = 0;
        int lowestHotbarDamage = 1000;

        for (int i = 0; i < 9; i++) {
            if (this.mc.player.getInventory().getStack(i).getItem() == Items.TRIDENT) {
                Integer currentHotbarDamage = this.mc.player.getInventory().getStack(i).getDamage();
                if (lowestHotbarDamage > currentHotbarDamage) {
                    lowestHotbarSlot = i;
                    lowestHotbarDamage = currentHotbarDamage;
                }
            }
        }

        this.mc.interactionManager.interactItem(this.mc.player, Hand.MAIN_HAND);
        this.cancel = true;
        int finalLowestHotbarSlot = lowestHotbarSlot;
        this.scheduleTask(() -> {
            this.cancel = false;
            if (this.durabilityManagement.get() && finalLowestHotbarSlot != 0) {
                this.mc.interactionManager.clickSlot(this.mc.player.currentScreenHandler.syncId, 44, 0, SlotActionType.SWAP, this.mc.player);
                if (this.dropTridents.get()) {
                    this.mc.interactionManager.clickSlot(this.mc.player.currentScreenHandler.syncId, 44, 0, SlotActionType.THROW, this.mc.player);
                }

                this.mc.interactionManager.clickSlot(this.mc.player.currentScreenHandler.syncId, 36 + finalLowestHotbarSlot, 0, SlotActionType.SWAP, this.mc.player);
            }

            this.mc.interactionManager.clickSlot(this.mc.player.currentScreenHandler.syncId, 3, 0, SlotActionType.SWAP, this.mc.player);
            PlayerActionC2SPacket packet2 = new PlayerActionC2SPacket(Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, Direction.DOWN, 0);
            this.mc.getNetworkHandler().sendPacket(packet2);
            if (this.dropTridents.get()) {
                this.mc.interactionManager.clickSlot(this.mc.player.currentScreenHandler.syncId, 44, 0, SlotActionType.THROW, this.mc.player);
            }

            this.cancel = true;
            this.scheduleTask2(this::dupe, delayInt);
        }, delayInt);
    }

    public void scheduleTask(Runnable task, long delayMillis) {
        long executeTime = System.currentTimeMillis() + delayMillis;
        this.scheduledTasks.add(new Pair(executeTime, task));
    }

    public void scheduleTask2(Runnable task, long delayMillis) {
        long executeTime = System.currentTimeMillis() + delayMillis;
        this.scheduledTasks2.add(new Pair(executeTime, task));
    }

    @EventHandler
    private void onTick(Pre event) {
        long currentTime = System.currentTimeMillis();
        Iterator<Pair<Long, Runnable>> iterator = this.scheduledTasks.iterator();

        while (iterator.hasNext()) {
            Pair<Long, Runnable> entry = iterator.next();
            if ((Long) entry.getLeft() <= currentTime) {
                ((Runnable) entry.getRight()).run();
                iterator.remove();
            }
        }

        iterator = this.scheduledTasks2.iterator();

        while (iterator.hasNext()) {
            Pair<Long, Runnable> entry = iterator.next();
            if ((Long) entry.getLeft() <= currentTime) {
                ((Runnable) entry.getRight()).run();
                iterator.remove();
            }
        }
    }

    @EventHandler
    private void onGameLeft(GameLeftEvent event) {
        this.toggle();
    }

    @EventHandler
    private void onScreenOpen(OpenScreenEvent event) {
        if (event.screen instanceof DisconnectedScreen) {
            this.toggle();
        }
    }
}
