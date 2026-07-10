package dev.abstr3act.addon.modules.Amrita.invtotem;

import dev.abstr3act.addon.Compassion;
import dev.abstr3act.addon.module.AmritaModule;
import dev.abstr3act.addon.modules.Seraphim.clicker.Timer;
import kotlin.Pair;
import meteordevelopment.meteorclient.events.packets.PacketEvent.Receive;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
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
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class InventoryTotem extends AmritaModule {
    private static final Random RANDOM = new Random();
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
    private final SettingGroup sgCheck = this.settings.createGroup("Checks");
    private final Setting<Mode> mode = this.sgGeneral
        .add(
            ((Builder) ((Builder) ((Builder) new Builder().name("mode")).description("Determines when to hold a totem, strict will always hold."))
                .defaultValue(Mode.Matrix))
                .build()
        );
    private final Setting<Integer> doubleHandDelay = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("DoubleHandDelay"))
                .description("The ticks between slot movements."))
                .defaultValue(0))
                .min(0)
                .sliderRange(0, 1000)
                .build()
        );
    private final Setting<Integer> refillDelay = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("RefillDelay"))
                .description("The ticks between slot movements."))
                .defaultValue(0))
                .min(0)
                .sliderRange(0, 1000)
                .build()
        );
    private final Setting<Integer> offhandDelay = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("OffhandDelay"))
                .description("The ticks between slot movements."))
                .defaultValue(0))
                .min(0)
                .sliderRange(0, 1000)
                .build()
        );
    private final Setting<Boolean> randomDelay = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("RandomDelay"))
                .description("."))
                .defaultValue(true))
                .build()
        );
    private final Setting<Integer> maxDelay = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("MaxDelay"))
                .description("The ticks between slot movements."))
                .defaultValue(1))
                .min(1)
                .sliderRange(1, 10000)
                .visible(this.randomDelay::get))
                .build()
        );
    private final Setting<Integer> minDelay = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("MinDelay"))
                .description("The ticks between slot movements."))
                .defaultValue(1))
                .min(1)
                .sliderRange(1, 10000)
                .visible(this.randomDelay::get))
                .build()
        );
    private final Setting<Integer> refillSlot = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("RefillSlot"))
                .description("."))
                .defaultValue(2))
                .range(1, 9)
                .min(1)
                .max(9)
                .sliderRange(1, 9)
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
    private final Setting<Boolean> checkPlayers = this.sgCheck
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("checkPlayer"))
                .description("."))
                .defaultValue(true))
                .build()
        );
    private final Setting<Integer> playerRange = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("PlayerRange"))
                .description("."))
                .defaultValue(6))
                .min(0)
                .sliderRange(1, 20)
                .visible(this.checkPlayers::get))
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
    private final Setting<Integer> crystalRange = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("CrystalRange"))
                .description("."))
                .defaultValue(10))
                .min(0)
                .sliderRange(1, 20)
                .visible(this.checkCrystals::get))
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
    private final Setting<Integer> anchorRange = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("AnchorRange"))
                .description("."))
                .defaultValue(10))
                .min(0)
                .sliderRange(1, 20)
                .visible(this.checkAnchors::get))
                .build()
        );
    private final Setting<Boolean> checkHurtTime = this.sgCheck
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("CheckHurtTime"))
                .description("."))
                .defaultValue(true))
                .build()
        );
    private final Setting<Boolean> checkTeleport = this.sgCheck
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("CheckPearl"))
                .description("."))
                .defaultValue(true))
                .build()
        );
    private final Setting<Boolean> checkPops = this.sgCheck
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("CheckPops"))
                .description("."))
                .defaultValue(true))
                .build()
        );
    private final Setting<Boolean> checkFallDamage = this.sgCheck
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("CheckFallDamage"))
                .description("."))
                .defaultValue(true))
                .build()
        );
    private final Setting<Boolean> ignoreShield = this.sgCheck
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("IgnoreShield"))
                .description("."))
                .defaultValue(true))
                .build()
        );
    Timer doubleHandTimer = new Timer();
    Timer offHandTimer = new Timer();
    Timer refillTimer = new Timer();
    Random offhandRandom = new Random(System.currentTimeMillis());
    Random doubleHandRandom = new Random(System.nanoTime());
    Random refillRandom = new Random();

    public InventoryTotem() {
        super(Compassion.AMRITA, "InventoryTotem", "Automatically equips a totem in your offhand.");
    }

    public static Pair<Integer, Integer> getTotemSlots(PlayerEntity player) {
        List<Integer> totemSlots = new ArrayList<>();

        for (int i = 9; i < 36; i++) {
            if (player.getInventory().getStack(i).getItem() == Items.TOTEM_OF_UNDYING) {
                totemSlots.add(i);
            }
        }

        if (totemSlots.isEmpty()) {
            return new Pair(-1, -1);
        } else {
            int firstSlot = totemSlots.get(RANDOM.nextInt(totemSlots.size()));
            List<Integer> nearbySlots = new ArrayList<>();
            List<Integer> fartherSlots = new ArrayList<>();
            int x1 = firstSlot % 9;
            int y1 = firstSlot / 9;

            for (int slot : totemSlots) {
                if (slot != firstSlot) {
                    int x2 = slot % 9;
                    int y2 = slot / 9;
                    int dx = Math.abs(x2 - x1);
                    int dy = Math.abs(y2 - y1);
                    int dist = dx + dy;
                    if (dist != 1 && (dx != 1 || dy != 1)) {
                        if (dist == 2) {
                            fartherSlots.add(slot);
                        }
                    } else {
                        nearbySlots.add(slot);
                    }
                }
            }

            if (!nearbySlots.isEmpty() && (fartherSlots.isEmpty() || RANDOM.nextDouble() < 0.75)) {
                return new Pair(firstSlot, nearbySlots.get(RANDOM.nextInt(nearbySlots.size())));
            } else {
                return !fartherSlots.isEmpty() ? new Pair(firstSlot, fartherSlots.get(RANDOM.nextInt(fartherSlots.size()))) : new Pair(firstSlot, -1);
            }
        }
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
                        break;
                    case QuickSwap:
                        this.mc.interactionManager.clickSlot(this.mc.player.currentScreenHandler.syncId, slot, 40, SlotActionType.QUICK_MOVE, this.mc.player);
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

    public Integer getTotem() {
        PlayerInventory inventory = this.mc.player.getInventory();

        for (int i = 0; i < 9; i++) {
            ItemStack stack = inventory.getStack(i);
            if (stack.getItem() == Items.TOTEM_OF_UNDYING) {
                return i;
            }
        }

        return -1;
    }

    @EventHandler(
        priority = 201
    )
    private void onUpdate(Render3DEvent event) {
        if (this.mc.player != null) {
            if (this.shouldDoubleHand()
                && this.doubleHandTimer
                .passedMs(
                    this.doubleHandDelay.get()
                        + (this.randomDelay.get() ? this.minDelay.get() + this.doubleHandRandom.nextInt(this.maxDelay.get()) : 0)
                )
                && !this.mc.player.getInventory().getMainHandStack().getItem().equals(Items.TOTEM_OF_UNDYING)) {
                int hotBar = this.getTotem();
                if (hotBar != -1) {
                    InvUtils.swap(this.getTotem(), false);
                }

                this.doubleHandTimer.reset();
            }

            Pair<Integer, Integer> totemSlots = getTotemSlots(this.mc.player);
            if (this.mc.currentScreen instanceof InventoryScreen) {
                if (this.offHandTimer
                    .passedMs(
                        this.offhandDelay.get()
                            + (this.randomDelay.get() ? this.minDelay.get() + this.offhandRandom.nextInt(this.maxDelay.get()) : 0)
                    )
                    && !this.mc.player.getOffHandStack().getItem().equals(Items.TOTEM_OF_UNDYING)) {
                    int firstSlot = totemSlots.getFirst();
                    if (firstSlot != -1) {
                        this.swapTo(firstSlot);
                        this.offHandTimer.reset();
                    }
                }

                if (this.refillTimer
                    .passedMs(
                        this.refillDelay.get()
                            + (this.randomDelay.get() ? this.minDelay.get() + this.refillRandom.nextInt(this.maxDelay.get()) : 0)
                    )
                    && !this.mc.player.getInventory().getStack(this.refillSlot.get()).getItem().equals(Items.TOTEM_OF_UNDYING)) {
                    int secondSlot = totemSlots.getSecond();
                    if (secondSlot != -1 && !this.mc.player.getInventory().getStack(this.refillSlot.get() - 1).getItem().equals(Items.TOTEM_OF_UNDYING)) {
                        InvUtils.move().from(secondSlot).to(this.refillSlot.get() - 1);
                        this.refillTimer.reset();
                    }
                }
            }
        }
    }

    public boolean shouldDoubleHand() {
        if (this.ignoreShield.get() && this.mc.player.isBlocking() && this.mc.player.getMainHandStack().getItem().equals(Items.SHIELD)) {
            return false;
        } else if (this.checkCrystal(this.mc.player, (this.crystalRange.get()).intValue()) && this.checkCrystals.get()) {
            return true;
        } else if (this.checkAnchor(this.anchorRange.get()) && this.checkAnchors.get()) {
            return true;
        } else if (this.checkPlayer((this.playerRange.get()).intValue()) && this.checkPlayers.get()) {
            return true;
        } else {
            return this.checkHurtTime.get() && this.mc.player.hurtTime > 0 && this.mc.player.getVelocity().y > 0.0
                ? true
                : this.checkFallDamage.get() && DamageUtils.fallDamage(this.mc.player) >= this.mc.player.getHealth() + this.mc.player.getAbsorptionAmount();
        }
    }

    public boolean checkCrystal(PlayerEntity player, double radius) {
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

    public boolean checkPlayer(double range) {
        if (this.mc.player != null && this.mc.world != null) {
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

    public boolean checkAnchor(int range) {
        World world = this.mc.player.getWorld();
        BlockPos playerPos = this.mc.player.getBlockPos();

        for (int x = -range; x <= range; x++) {
            for (int y = -range; y <= range; y++) {
                for (int z = -range; z <= range; z++) {
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

    @EventHandler(
        priority = 201
    )
    private void onReceivePacket(Receive event) {
        if (event.packet instanceof EntityStatusS2CPacket p) {
            if (p.getStatus() == 35) {
                Entity entity = p.getEntity(this.mc.world);
                if (entity != null && entity.equals(this.mc.player)) {
                    if (this.checkPops.get()) {
                        InvUtils.swap(InvUtils.findInHotbar(new Item[]{Items.TOTEM_OF_UNDYING}).slot(), false);
                    }
                }
            }
        }
    }

    @EventHandler(
        priority = 201
    )
    private void onTeleport(Receive event) {
        if (event.packet instanceof TeleportConfirmC2SPacket) {
            if (this.checkTeleport.get()) {
                InvUtils.swap(InvUtils.findInHotbar(new Item[]{Items.TOTEM_OF_UNDYING}).slot(), false);
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
    }

    public String getInfoString() {
        return String.valueOf(InvUtils.find(new Item[]{Items.TOTEM_OF_UNDYING}).count());
    }

    public static enum Mode {
        Default,
        Alternative,
        Matrix,
        MatrixPick,
        NewVersion,
        QuickSwap;
    }
}
