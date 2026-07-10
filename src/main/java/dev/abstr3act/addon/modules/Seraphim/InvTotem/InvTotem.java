package dev.abstr3act.addon.modules.Seraphim.InvTotem;

import dev.abstr3act.addon.Compassion;
import dev.abstr3act.addon.module.SeraphimModule;
import dev.abstr3act.addon.utils.abnormally.AChatUtils;
import meteordevelopment.meteorclient.events.packets.ContainerSlotUpdateEvent;
import meteordevelopment.meteorclient.events.packets.PacketEvent.Receive;
import meteordevelopment.meteorclient.events.render.Render2DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent.Pre;
import meteordevelopment.meteorclient.settings.EnumSetting.Builder;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.utils.entity.DamageUtils;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.RespawnAnchorBlock;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.*;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket.Action;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class InvTotem extends SeraphimModule {
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
    private final SettingGroup sgCheck = this.settings.createGroup("Checks");
    private final Setting<Mode> mode = this.sgGeneral
        .add(
            ((Builder) ((Builder) ((Builder) new Builder().name("mode")).description("Determines when to hold a totem, strict will always hold."))
                .defaultValue(Mode.Matrix))
                .build()
        );
    private final Setting<Integer> delay = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("delay"))
                .description("The ticks between slot movements."))
                .defaultValue(0))
                .min(0)
                .build()
        );
    private final Setting<Integer> delay1 = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("DoubleHandDelay"))
                .description("The ticks between slot movements."))
                .defaultValue(50))
                .min(1)
                .build()
        );
    private final Setting<Integer> delay2 = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("RefillTotemDelay"))
                .description("The ticks between slot movements."))
                .defaultValue(50))
                .min(1)
                .build()
        );
    private final Setting<Integer> delay3 = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("OffhandDelay"))
                .description("The ticks between slot movements."))
                .defaultValue(50))
                .min(1)
                .build()
        );
    private final Setting<Boolean> totemGuardStrict = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("TotemGuardBypass"))
                .description("."))
                .defaultValue(false))
                .build()
        );
    private final Setting<Boolean> openInv = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("OpenInventory"))
                .description("."))
                .defaultValue(false))
                .build()
        );
    private final Setting<Boolean> closeInv = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("CloseInventory"))
                .description("."))
                .defaultValue(false))
                .build()
        );
    private final Setting<Integer> delay4 = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("CloseDelay"))
                .description("."))
                .defaultValue(50))
                .min(1)
                .visible(this.closeInv::get))
                .build()
        );
    private final Setting<Double> randomFactor = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("randomFactor"))
                .description("."))
                .defaultValue(10.0)
                .min(0.0)
                .build()
        );
    private final Setting<Double> damageFactor = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("damageFactor"))
                .description("."))
                .defaultValue(1.0)
                .min(0.001)
                .build()
        );
    private final Setting<Double> YFactor = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("YFactor"))
                .description("."))
                .defaultValue(1.0)
                .min(0.001)
                .build()
        );
    private final Setting<Boolean> ncpStrict = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("ncpStrict"))
                .description("."))
                .defaultValue(false))
                .build()
        );
    private final Setting<Boolean> stopMotion = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("StopMotion"))
                .description("."))
                .defaultValue(false))
                .build()
        );
    private final Setting<Boolean> resetAttackCooldown = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("resetAttackCooldown"))
                .description("."))
                .defaultValue(false))
                .build()
        );
    private final Setting<Integer> re_pickSlot = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("Refill Slot"))
                .description("."))
                .defaultValue(2))
                .range(1, 9)
                .min(1)
                .max(9)
                .sliderRange(1, 9)
                .build()
        );
    private final Setting<Boolean> checkPlayers = this.sgCheck
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("checkPlayer"))
                .description("."))
                .defaultValue(true))
                .build()
        );
    private final Setting<Boolean> checkCrystals = this.sgCheck
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("checkCrystal"))
                .description("."))
                .defaultValue(true))
                .build()
        );
    private final Setting<Boolean> checkAnchors = this.sgCheck
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("checkAnchor"))
                .description("."))
                .defaultValue(true))
                .build()
        );
    public boolean locked;
    public boolean shouldDoubleHand = false;
    public Block targetBlock;
    public BlockPos targetBlockPos;
    List<Boolean> defaultGrid = new ArrayList<>(List.of(false));
    private int i1;
    private int i2;
    private int i3;
    private int totems;
    private int ticks;
    private List<Integer> totemList;

    public InvTotem() {
        super(Compassion.SERAPHIM, "InvTotem", "Automatically equips a totem in your offhand.");
    }

    public static long getRandom() {
        Random random = new Random();
        return random.nextInt(10);
    }

    public int findNearestCurrentItem() {
        int i = this.mc.player.getInventory().selectedSlot;
        if (i == 8) {
            return 7;
        } else {
            return i == 0 ? 1 : i - 1;
        }
    }

    public void swapTo(int slot) {
        if (slot != -1) {
            if (this.mc.currentScreen instanceof GenericContainerScreen) {
                return;
            }

            if (this.stopMotion.get()) {
                this.mc.player.setVelocity(0.0, this.mc.player.getVelocity().getY(), 0.0);
            }

            int nearestSlot = this.findNearestCurrentItem();
            int prevCurrentItem = this.mc.player.getInventory().selectedSlot;
            if (slot >= 9) {
                switch ((Mode) this.mode.get()) {
                    case Default:
                        if (this.ncpStrict.get()) {
                            this.sendPacket(new ClientCommandC2SPacket(this.mc.player, ClientCommandC2SPacket.Mode.STOP_SPRINTING));
                        }

                        this.clickSlot(slot);
                        this.clickSlot(45);
                        this.clickSlot(slot);
                        this.sendPacket(new CloseHandledScreenC2SPacket(this.mc.player.currentScreenHandler.syncId));
                        break;
                    case Alternative:
                        if (this.ncpStrict.get()) {
                            this.sendPacket(new ClientCommandC2SPacket(this.mc.player, ClientCommandC2SPacket.Mode.STOP_SPRINTING));
                        }

                        this.clickSlot(slot, nearestSlot, SlotActionType.SWAP);
                        this.clickSlot(45, nearestSlot, SlotActionType.SWAP);
                        this.clickSlot(slot, nearestSlot, SlotActionType.SWAP);
                        this.sendPacket(new CloseHandledScreenC2SPacket(this.mc.player.currentScreenHandler.syncId));
                        break;
                    case Matrix:
                        if (this.ncpStrict.get()) {
                            this.sendPacket(new ClientCommandC2SPacket(this.mc.player, ClientCommandC2SPacket.Mode.STOP_SPRINTING));
                        }

                        this.mc.interactionManager.clickSlot(this.mc.player.currentScreenHandler.syncId, slot, nearestSlot, SlotActionType.SWAP, this.mc.player);
                        this.sendPacket(new UpdateSelectedSlotC2SPacket(nearestSlot));
                        this.mc.player.getInventory().selectedSlot = nearestSlot;
                        ItemStack itemstack = this.mc.player.getOffHandStack();
                        this.mc.player.setStackInHand(Hand.OFF_HAND, this.mc.player.getMainHandStack());
                        this.mc.player.setStackInHand(Hand.MAIN_HAND, itemstack);
                        this.sendPacket(new PlayerActionC2SPacket(Action.SWAP_ITEM_WITH_OFFHAND, BlockPos.ORIGIN, Direction.DOWN));
                        this.sendPacket(new UpdateSelectedSlotC2SPacket(prevCurrentItem));
                        this.mc.player.getInventory().selectedSlot = prevCurrentItem;
                        this.mc.interactionManager.clickSlot(this.mc.player.currentScreenHandler.syncId, slot, nearestSlot, SlotActionType.SWAP, this.mc.player);
                        this.sendPacket(new CloseHandledScreenC2SPacket(this.mc.player.currentScreenHandler.syncId));
                        if (this.resetAttackCooldown.get()) {
                            this.mc.player.resetLastAttackedTicks();
                        }
                        break;
                    case MatrixPick:
                        this.sendPacket(new PickFromInventoryC2SPacket(slot));
                        this.sendPacket(new PlayerActionC2SPacket(Action.SWAP_ITEM_WITH_OFFHAND, BlockPos.ORIGIN, Direction.DOWN));
                        break;
                    case NewVersion:
                        this.mc.interactionManager.clickSlot(this.mc.player.currentScreenHandler.syncId, slot, 40, SlotActionType.SWAP, this.mc.player);
                        this.sendPacket(new CloseHandledScreenC2SPacket(this.mc.player.currentScreenHandler.syncId));
                }
            } else {
                this.sendPacket(new UpdateSelectedSlotC2SPacket(slot));
                this.mc.player.getInventory().selectedSlot = slot;
                this.sendPacket(new PlayerActionC2SPacket(Action.SWAP_ITEM_WITH_OFFHAND, BlockPos.ORIGIN, Direction.DOWN));
                this.sendPacket(new UpdateSelectedSlotC2SPacket(prevCurrentItem));
                this.mc.player.getInventory().selectedSlot = prevCurrentItem;
                if (this.resetAttackCooldown.get()) {
                    this.mc.player.resetLastAttackedTicks();
                }
            }
        }
    }

    public void closeScreen() {
        this.mc.setScreen(null);
    }

    @EventHandler(
        priority = 100
    )
    private void onTickEvent(Pre event) {
        this.totemList = this.getTotemList();
    }

    public List<Integer> getTotemList() {
        List<Integer> totemSlots = new ArrayList<>();
        PlayerInventory inventory = this.mc.player.getInventory();

        for (int i = 9; i < inventory.size(); i++) {
            ItemStack stack = inventory.getStack(i);
            if (stack.getItem() == Items.TOTEM_OF_UNDYING) {
                totemSlots.add(i);
            }
        }

        return totemSlots;
    }

    public int[] getVar(List<Integer> list) {
        Random random = new Random();
        int index1 = random.nextInt(list.size());
        if (list.size() < 2) {
            return new int[]{index1, -1};
        } else {
            int index2;
            do {
                index2 = random.nextInt(list.size());
            } while (index1 == index2);

            return new int[]{list.get(index1), list.get(index2)};
        }
    }

    public void displayGrid() {
        AChatUtils.sendMsgSeraphim(Text.of("[S] ■■■■■■■■■"));

        for (int row = 0; row < this.mc.player.getInventory().size() / 9; row++) {
            StringBuilder builder = new StringBuilder();

            for (int col = 0; col < 9; col++) {
                int index = row * 9 + col;
                builder.append(this.mc.player.getInventory().getStack(index).getItem().equals(Items.TOTEM_OF_UNDYING) ? "§a■§f" : "§c■§f");
            }

            AChatUtils.sendMsgSeraphim(Text.of("[" + (row == 0 ? "H" : row) + "] " + builder));
        }

        AChatUtils.sendMsgSeraphim(Text.of("[E] ■■■■■■■■■"));
    }

    @EventHandler(
        priority = 201
    )
    private void onTick(Render2DEvent event) {
        if (!this.mc.player.isBlocking() && !this.mc.player.getMainHandStack().getItem().equals(Items.SHIELD)) {
            if (this.closeInv.get()
                && this.mc.player.getInventory().getStack(this.re_pickSlot.get() - 1).getItem().equals(Items.TOTEM_OF_UNDYING)
                && this.mc.player.getOffHandStack().getItem().equals(Items.TOTEM_OF_UNDYING)) {
                if (this.mc.currentScreen instanceof InventoryScreen) {
                    this.mc.player.closeHandledScreen();
                }
            } else {
                if (this.shouldOpenInv() && this.mc.currentScreen == null && this.openInv.get()) {
                    this.mc.setScreen(new InventoryScreen(this.mc.player));
                }

                if (this.totemList != null && !this.totemList.isEmpty()) {
                    int a1 = Utils.getVar(this.totemList)[0];
                    int a2 = Utils.getVar(this.totemList)[1];
                    this.shouldDoubleHand = this.checkPlayer(6.0) && this.mc.player.hurtTime > 0
                        || this.shouldDoubleHand()
                        || this.checkGlobal()
                        || this.checkPlayer(6.0) && !this.mc.player.isBlocking() && !this.mc.player.getMainHandStack().getItem().equals(Items.SHIELD);
                    if (this.shouldDoubleHand && this.i1 <= 0) {
                        InvUtils.swap(InvUtils.find(new Item[]{Items.TOTEM_OF_UNDYING}).slot(), false);
                        this.i1 = this.totemGuardStrict.get()
                            ? (int) ((this.delay1.get()).intValue() + getRandom() * this.randomFactor.get())
                            : this.delay1.get();
                    }

                    if (this.i1 > 0) {
                        this.i1--;
                    }

                    if (this.i2 > 0) {
                        this.i2--;
                    }

                    if (this.i3 > 0) {
                        this.i3--;
                    }

                    if (this.mc.currentScreen instanceof InventoryScreen) {
                        if (this.totemList.isEmpty()) {
                            this.locked = false;
                        } else if (this.ticks >= this.delay.get()) {
                            if (this.defaultGrid == null || this.defaultGrid.size() < this.mc.player.getInventory().size()) {
                                for (int i = 9; i < this.mc.player.getInventory().size(); i++) {
                                    this.defaultGrid.add(false);
                                }
                            }

                            if (this.mc.player.getOffHandStack().getItem() != Items.TOTEM_OF_UNDYING) {
                                if (a1 == -1) {
                                    return;
                                }

                                if (this.i2 <= 0) {
                                    this.swapTo(a1);
                                    this.i2 = this.totemGuardStrict.get()
                                        ? (int) ((this.delay2.get()).intValue() + getRandom() * this.randomFactor.get())
                                        : this.delay2.get();
                                }
                            }

                            if (InvUtils.find(new Item[]{Items.TOTEM_OF_UNDYING}).found()
                                && !this.mc.player.getInventory().getStack(this.re_pickSlot.get() - 1).getItem().equals(Items.TOTEM_OF_UNDYING)) {
                                if (a2 == -1) {
                                    return;
                                }

                                if (this.i3 <= 0) {
                                    InvUtils.move().from(a2).to(this.re_pickSlot.get() - 1);
                                    this.i3 = this.totemGuardStrict.get()
                                        ? (int) ((this.delay3.get()).intValue() + getRandom() * this.randomFactor.get())
                                        : this.delay3.get();
                                }
                            }

                            this.ticks = 0;
                            return;
                        }

                        this.ticks++;
                    }
                }
            }
        }
    }

    public boolean shouldOpenInv() {
        return !this.mc.player.getOffHandStack().getItem().equals(Items.TOTEM_OF_UNDYING)
            ? true
            : !this.mc.player.getMainHandStack().getItem().equals(Items.TOTEM_OF_UNDYING) && this.mc.player.hurtTime > 0 && this.checkRange(this.mc.player, 10.0);
    }

    public boolean shouldDoubleHand() {
        if (this.mc.player == null) {
            return false;
        } else if (this.mc.player.getMainHandStack().getItem() != Items.OBSIDIAN
            && !this.mc.player.getMainHandStack().getItem().equals(Items.GLOWSTONE)
            && (!this.mc.player.getMainHandStack().getItem().equals(Items.RESPAWN_ANCHOR) || this.mc.player.hurtTime > 0)) {
            return this.checkRange(this.mc.player, 10.0) ? true : this.checkRange(this.mc.player, 10.0) && this.mc.player.hurtTime > 0;
        } else {
            return false;
        }
    }

    public boolean checkRange(PlayerEntity player, double radius) {
        if (!this.checkCrystals.get()) {
            return false;
        } else {
            World world = player.getWorld();
            Box detectionBox = new Box(
                player.getX() - radius, player.getY() - radius, player.getZ() - radius, player.getX() + radius, player.getY() + radius, player.getZ() + radius
            );

            for (Entity entity : world.getOtherEntities(player, detectionBox)) {
                if (entity instanceof EndCrystalEntity
                    && DamageUtils.crystalDamage(this.mc.player, entity.getPos())
                    >= (this.mc.player.getHealth() + this.mc.player.getAbsorptionAmount()) * this.damageFactor.get()) {
                    return true;
                }
            }

            return false;
        }
    }

    public boolean checkPlayer(double range) {
        if (!this.checkPlayers.get()) {
            return false;
        } else if (this.mc.player != null && this.mc.world != null) {
            Vec3d playerPos = this.mc.player.getPos();

            for (Entity entity : this.mc.world.getEntities()) {
                if (entity instanceof PlayerEntity && entity != this.mc.player) {
                    Vec3d entityPos = entity.getPos();
                    if (playerPos.distanceTo(entityPos) <= range
                        && DamageUtils.getAttackDamage((LivingEntity) entity, this.mc.player)
                        >= (this.mc.player.getHealth() + this.mc.player.getAbsorptionAmount()) * this.damageFactor.get()) {
                        return true;
                    }
                }
            }

            return false;
        } else {
            return false;
        }
    }

    public boolean checkY(double range) {
        if (this.mc.player != null && this.mc.world != null) {
            Vec3d playerPos = this.mc.player.getPos();

            for (Entity entity : this.mc.world.getEntities()) {
                if (entity instanceof PlayerEntity && entity != this.mc.player) {
                    Vec3d entityPos = entity.getPos();
                    if (playerPos.distanceTo(entityPos) <= range && this.mc.player.getY() - entityPos.y > 0.0) {
                        return true;
                    }
                }
            }

            return false;
        } else {
            return false;
        }
    }

    @EventHandler
    public void onPlayerClickSlot(ContainerSlotUpdateEvent event) {
    }

    public boolean checkGlobal() {
        if (this.mc.player.getMainHandStack().getItem().equals(Items.TOTEM_OF_UNDYING) && this.checkAnchor() && this.mc.player.hurtTime > 0) {
            return true;
        } else {
            return this.mc.player.getOffHandStack().getItem().equals(Items.TOTEM_OF_UNDYING) && this.checkAnchor() && this.mc.player.hurtTime > 0
                ? true
                : this.checkAnchor();
        }
    }

    public boolean checkAnchor() {
        if (!this.checkAnchors.get()) {
            return false;
        } else {
            World world = this.mc.player.getWorld();
            BlockPos playerPos = this.mc.player.getBlockPos();

            for (int x = -6; x <= 6; x++) {
                for (int y = -6; y <= 6; y++) {
                    for (int z = -6; z <= 6; z++) {
                        BlockPos currentPos = playerPos.add(x, y, z);
                        Block block = world.getBlockState(currentPos).getBlock();
                        if (block == Blocks.RESPAWN_ANCHOR) {
                            BlockState blockState = world.getBlockState(currentPos);
                            if (blockState.getBlock() instanceof RespawnAnchorBlock
                                && blockState.get(Properties.CHARGES) > 0
                                && DamageUtils.anchorDamage(this.mc.player, currentPos.toCenterPos())
                                >= (this.mc.player.getHealth() + this.mc.player.getAbsorptionAmount()) * this.damageFactor.get()) {
                                return true;
                            }
                        }
                    }
                }
            }

            return false;
        }
    }

    @EventHandler(
        priority = 100
    )
    private void onReceivePacket(Receive event) {
        if (event.packet instanceof EntityStatusS2CPacket p) {
            if (p.getStatus() == 35) {
                Entity entity = p.getEntity(this.mc.world);
                if (entity != null && entity.equals(this.mc.player)) {
                    if (this.mc.player.getMainHandStack().getItem() != Items.OBSIDIAN
                        && !this.mc.player.getMainHandStack().getItem().equals(Items.GLOWSTONE)
                        && !this.mc.player.getMainHandStack().getItem().equals(Items.RESPAWN_ANCHOR)) {
                        InvUtils.swap(InvUtils.find(new Item[]{Items.TOTEM_OF_UNDYING}).slot(), false);
                        this.ticks = 0;
                    }
                }
            }
        }
    }

    public int getSlot(PlayerEntity player) {
        for (int i = 9; i < this.mc.player.getInventory().size(); i++) {
            ItemStack stack = player.getInventory().getStack(i);
            if (stack.getItem().equals(Items.TOTEM_OF_UNDYING)) {
                return i;
            }
        }

        return -1;
    }

    public void onActivate() {
        this.i1 = 0;
        this.i2 = 0;
        this.i3 = 0;
    }

    public String getInfoString() {
        return String.valueOf(this.totems);
    }

    public static enum Mode {
        Default,
        Alternative,
        Matrix,
        MatrixPick,
        NewVersion;
    }
}
