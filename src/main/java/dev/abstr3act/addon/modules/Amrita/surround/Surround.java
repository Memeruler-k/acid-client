package dev.abstr3act.addon.modules.Amrita.surround;

import dev.abstr3act.addon.Compassion;
import dev.abstr3act.addon.events.EventUpdate;
import dev.abstr3act.addon.module.AmritaModule;
import dev.abstr3act.addon.modules.Amrita.autoweb.InteractionUtility;
import dev.abstr3act.addon.modules.Seraphim.clicker.Timer;
import dev.abstr3act.addon.utils.math.InventoryUtility;
import dev.abstr3act.addon.utils.math.PlayerUtility;
import dev.abstr3act.addon.utils.math.inv.SearchInvResult;
import meteordevelopment.meteorclient.events.packets.PacketEvent.Receive;
import meteordevelopment.meteorclient.events.world.TickEvent.Pre;
import meteordevelopment.meteorclient.settings.DoubleSetting.Builder;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity.RemovalReason;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket.PositionAndOnGround;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class Surround extends AmritaModule {
    public final Timer inactivityTimer = new Timer();
    public final Timer pauseTimer = new Timer();
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
    private final SettingGroup sgCrystalBreaker = this.settings.createGroup("Crystal Breaker");
    private final SettingGroup sgBlocks = this.settings.createGroup("Blocks");
    private final SettingGroup sgPause = this.settings.createGroup("Pause");
    private final SettingGroup sgRender = this.settings.createGroup("Render");
    private final SettingGroup sgAutoDisable = this.settings.createGroup("Auto Disable");
    private final Setting<Double> range = this.sgGeneral
        .add(((Builder) ((Builder) new Builder().name("Range")).description("Block placement range.")).defaultValue(5.0).min(0.0).max(7.0).build());
    private final Setting<InteractionUtility.Interact> interact = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) new meteordevelopment.meteorclient.settings.EnumSetting.Builder()
                .name("Interact"))
                .description("Interaction type."))
                .defaultValue(InteractionUtility.Interact.Strict))
                .build()
        );
    private final Setting<InteractMode> placeMode = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) new meteordevelopment.meteorclient.settings.EnumSetting.Builder()
                .name("Place Mode"))
                .description("Mode for placing blocks."))
                .defaultValue(InteractMode.Normal))
                .build()
        );
    private final Setting<InteractionUtility.Rotate> rotate = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) new meteordevelopment.meteorclient.settings.EnumSetting.Builder()
                .name("Rotate"))
                .description("Rotation mode for placement."))
                .defaultValue(InteractionUtility.Rotate.None))
                .build()
        );
    private final Setting<Boolean> swing = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("Swing"))
                .description("Swing hand animation."))
                .defaultValue(false))
                .build()
        );
    private final Setting<Boolean> crystalBreaker = this.sgCrystalBreaker
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("Crystal Breaker"))
                .description("Enable crystal breaking."))
                .defaultValue(false))
                .build()
        );
    private final Setting<Integer> crystalAge = this.sgCrystalBreaker
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("Crystal Age"))
                .description("Minimum crystal age before breaking."))
                .defaultValue(0))
                .min(0)
                .max(20)
                .build()
        );
    private final Setting<Integer> breakDelay = this.sgCrystalBreaker
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("Break Delay"))
                .description("Delay before breaking crystals."))
                .defaultValue(100))
                .min(1)
                .max(1000)
                .build()
        );
    private final Setting<Boolean> remove = this.sgCrystalBreaker
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("Remove"))
                .description("Remove crystals after breaking."))
                .defaultValue(false))
                .build()
        );
    private final Setting<InteractMode> breakCrystalMode = this.sgCrystalBreaker
        .add(
            ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) new meteordevelopment.meteorclient.settings.EnumSetting.Builder()
                .name("Break Mode"))
                .description("Mode for breaking crystals."))
                .defaultValue(InteractMode.Normal))
                .build()
        );
    private final Setting<Boolean> antiWeakness = this.sgCrystalBreaker
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("Anti Weakness"))
                .description("Bypass weakness effect."))
                .defaultValue(false))
                .build()
        );
    private final Setting<Boolean> obsidian = this.sgBlocks
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("Obsidian"))
                .description("Enable obsidian placement."))
                .defaultValue(true))
                .build()
        );
    private final Setting<Boolean> anchor = this.sgBlocks
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("Anchor"))
                .description("Enable respawn anchor placement."))
                .defaultValue(false))
                .build()
        );
    private final Setting<Boolean> enderChest = this.sgBlocks
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("EnderChest"))
                .description("Enable ender chest placement."))
                .defaultValue(true))
                .build()
        );
    private final Setting<Boolean> netherite = this.sgBlocks
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("Netherite"))
                .description("Enable netherite block placement."))
                .defaultValue(false))
                .build()
        );
    private final Setting<Boolean> cryingObsidian = this.sgBlocks
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("Crying Obsidian"))
                .description("Enable crying obsidian placement."))
                .defaultValue(true))
                .build()
        );
    private final Setting<Boolean> dirt = this.sgBlocks
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("Dirt"))
                .description("Enable dirt placement."))
                .defaultValue(false))
                .build()
        );
    private final Setting<Boolean> oakPlanks = this.sgBlocks
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("Oak Planks"))
                .description("Enable oak planks placement."))
                .defaultValue(false))
                .build()
        );
    private final Setting<Boolean> eatPause = this.sgPause
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("On Eat"))
                .description("Pause when eating."))
                .defaultValue(false))
                .build()
        );
    private final Setting<Boolean> breakPause = this.sgPause
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("On Break"))
                .description("Pause when breaking blocks."))
                .defaultValue(false))
                .build()
        );
    private final Setting<Boolean> render = this.sgRender
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("Render"))
                .description("Enable block rendering."))
                .defaultValue(true))
                .build()
        );
    private final Setting<SettingColor> renderFillColor = this.sgRender
        .add(
            ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                .name("Fill Color"))
                .description("Color of the block rendering fill."))
                .defaultValue(new SettingColor(255, 0, 0, 75))
                .build()
        );
    private final Setting<SettingColor> renderLineColor = this.sgRender
        .add(
            ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                .name("Line Color"))
                .description("Color of the block rendering outline."))
                .defaultValue(new SettingColor(255, 0, 0, 255))
                .build()
        );
    private final Setting<Integer> renderLineWidth = this.sgRender
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("Line Width"))
                .description("Thickness of block rendering lines."))
                .defaultValue(2))
                .min(1)
                .max(5)
                .build()
        );
    private final Setting<Integer> blocksPerTick = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("Blocks/Place"))
                .description("How many blocks to place per tick."))
                .defaultValue(8))
                .min(1)
                .max(12)
                .build()
        );
    private final Setting<Integer> placeDelay = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("Delay/Place"))
                .description("Delay between placing blocks."))
                .defaultValue(3))
                .min(0)
                .max(10)
                .build()
        );
    private final Setting<CenterMode> center = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) new meteordevelopment.meteorclient.settings.EnumSetting.Builder()
                .name("Center"))
                .description("Centering mode."))
                .defaultValue(CenterMode.Disabled))
                .build()
        );
    private final Setting<Boolean> onYChange = this.sgAutoDisable
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("On Y Change"))
                .description("Disable module when Y level changes."))
                .defaultValue(true))
                .build()
        );
    private final Setting<OnTpAction> onTp = this.sgAutoDisable
        .add(
            ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) new meteordevelopment.meteorclient.settings.EnumSetting.Builder()
                .name("On Tp"))
                .description("Action when teleporting."))
                .defaultValue(OnTpAction.None))
                .build()
        );
    private final Setting<Boolean> onDeath = this.sgAutoDisable
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("On Death"))
                .description("Disable module on death."))
                .defaultValue(false))
                .build()
        );
    private final Setting<Boolean> quad = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("QuadPlace"))
                .description("Disable module on death."))
                .defaultValue(false))
                .build()
        );
    private final Timer attackTimer = new Timer();
    private int delay;
    private double prevY;

    public Surround() {
        super(Compassion.AMRITA, "SurroundV2", ".");
    }

    public void onActivate() {
        if (this.mc.player != null) {
            this.delay = 0;
            this.prevY = this.mc.player.getY();
            if (this.center.get() == CenterMode.Teleport) {
                this.mc.player.updatePosition(MathHelper.floor(this.mc.player.getX()) + 0.5, this.mc.player.getY(), MathHelper.floor(this.mc.player.getZ()) + 0.5);
                this.sendPacket(new PositionAndOnGround(this.mc.player.getX(), this.mc.player.getY(), this.mc.player.getZ(), this.mc.player.isOnGround()));
            }
        }
    }

    private boolean shouldPause() {
        return this.eatPause.get() && PlayerUtility.isEating()
            || this.breakPause.get() && PlayerUtility.isMining()
            || !this.pauseTimer.passedMs(350L);
    }

    private boolean placeBlock(BlockPos pos) {
        return this.placeBlock(pos, this.crystalBreaker.get(), (InteractMode) this.placeMode.get(), (InteractionUtility.Rotate) this.rotate.get());
    }

    private boolean placeBlock(BlockPos pos, boolean ignoreEntities) {
        return this.placeBlock(pos, ignoreEntities, (InteractMode) this.placeMode.get(), (InteractionUtility.Rotate) this.rotate.get());
    }

    private boolean placeBlock(BlockPos pos, InteractionUtility.Rotate rotate) {
        return this.placeBlock(pos, this.crystalBreaker.get(), (InteractMode) this.placeMode.get(), rotate);
    }

    private boolean placeBlock(BlockPos pos, InteractMode mode) {
        return this.placeBlock(pos, this.crystalBreaker.get(), mode, (InteractionUtility.Rotate) this.rotate.get());
    }

    private boolean placeBlock(BlockPos pos, boolean ignoreEntities, InteractMode mode, InteractionUtility.Rotate rotate) {
        if (this.shouldPause()) {
            return false;
        } else {
            boolean validInteraction = false;
            SearchInvResult result = this.getBlockResult();
            if (!result.found()) {
                return false;
            } else {
                if (this.crystalBreaker.get() && this.mc.world != null && this.attackTimer.passedMs((this.breakDelay.get()).intValue())) {
                    this.mc.world.getNonSpectatingEntities(EndCrystalEntity.class, new Box(pos)).stream().findFirst().ifPresent(this::breakCrystal);
                }

                if (mode == InteractMode.Packet) {
                    validInteraction = InteractionUtility.placeBlock(
                        pos, rotate, (InteractionUtility.Interact) this.interact.get(), InteractionUtility.PlaceMode.Packet, result.slot(), true, ignoreEntities
                    );
                }

                if (mode == InteractMode.Normal) {
                    validInteraction = InteractionUtility.placeBlock(
                        pos, rotate, (InteractionUtility.Interact) this.interact.get(), InteractionUtility.PlaceMode.Normal, result.slot(), true, ignoreEntities
                    );
                }

                if (validInteraction && this.mc.player != null && this.swing.get()) {
                    this.mc.player.swingHand(Hand.MAIN_HAND);
                }

                return validInteraction;
            }
        }
    }

    public String getInfoString() {
        return ((InteractMode) this.placeMode.get()).name();
    }

    private void breakCrystal(EndCrystalEntity entity) {
        if (this.mc.player != null
            && this.mc.world != null
            && this.mc.interactionManager != null
            && !this.shouldPause()
            && this.attackTimer.passedMs((this.breakDelay.get()).intValue())
            && !(this.mc.player.squaredDistanceTo(entity) > Math.pow(this.range.get(), 2.0))
            && entity.age >= this.crystalAge.get()) {
            int preSlot = this.mc.player.getInventory().selectedSlot;
            if (this.antiWeakness.get() && this.mc.player.hasStatusEffect(StatusEffects.WEAKNESS)) {
                SearchInvResult result = InventoryUtility.getAntiWeaknessItem();
                if (!result.found()) {
                    return;
                }

                result.switchTo();
            }

            if (this.breakCrystalMode.get() == InteractMode.Packet) {
                this.sendPacket(PlayerInteractEntityC2SPacket.attack(entity, this.mc.player.isSneaking()));
            }

            if (this.breakCrystalMode.get() == InteractMode.Normal) {
                this.mc.interactionManager.attackEntity(this.mc.player, entity);
            }

            this.sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
            this.attackTimer.reset();
            if (this.remove.get()) {
                entity.kill();
                entity.setRemoved(RemovalReason.KILLED);
                entity.onRemoved();
            }

            if (this.antiWeakness.get() && this.mc.player.hasStatusEffect(StatusEffects.WEAKNESS)) {
                InventoryUtility.switchTo(preSlot);
            }
        }
    }

    private boolean canPlaceBlock(BlockPos pos, boolean ignoreEntities) {
        return InteractionUtility.canPlaceBlock(pos, (InteractionUtility.Interact) this.interact.get(), ignoreEntities);
    }

    private SearchInvResult getBlockResult() {
        List<Block> canUseBlocks = new ArrayList<>();
        if (this.mc.player == null) {
            return SearchInvResult.notFound();
        } else {
            if (this.obsidian.get()) {
                canUseBlocks.add(Blocks.OBSIDIAN);
            }

            if (this.enderChest.get()) {
                canUseBlocks.add(Blocks.ENDER_CHEST);
            }

            if (this.cryingObsidian.get()) {
                canUseBlocks.add(Blocks.CRYING_OBSIDIAN);
            }

            if (this.netherite.get()) {
                canUseBlocks.add(Blocks.NETHERITE_BLOCK);
            }

            if (this.anchor.get()) {
                canUseBlocks.add(Blocks.RESPAWN_ANCHOR);
            }

            if (this.dirt.get()) {
                canUseBlocks.addAll(List.of(Blocks.DIRT, Blocks.GRASS_BLOCK, Blocks.PODZOL));
            }

            if (this.oakPlanks.get()) {
                canUseBlocks.addAll(List.of(Blocks.OAK_PLANKS, Blocks.BIRCH_PLANKS, Blocks.DARK_OAK_PLANKS));
            }

            ItemStack mainHandStack = this.mc.player.getMainHandStack();
            if (mainHandStack != ItemStack.EMPTY && mainHandStack.getItem() instanceof BlockItem) {
                Block blockFromMainHandItem = ((BlockItem) mainHandStack.getItem()).getBlock();
                if (canUseBlocks.contains(blockFromMainHandItem)) {
                    return new SearchInvResult(this.mc.player.getInventory().selectedSlot, true, mainHandStack);
                }
            }

            return InventoryUtility.findBlockInHotBar(canUseBlocks);
        }
    }

    public void pause() {
        this.pauseTimer.reset();
    }

    @EventHandler
    public void onUpdate(EventUpdate event) {
        if ((this.mc.player.isDead() || !this.mc.player.isAlive() || this.mc.player.getHealth() + this.mc.player.getAbsorptionAmount() <= 0.0F)
            && this.onDeath.get()) {
            this.toggle();
        }
    }

    @EventHandler(
        priority = 200
    )
    private void onTick(Pre event) {
        if (this.prevY != this.mc.player.getY() && this.onYChange.get()) {
            this.toggle();
        } else {
            this.prevY = this.mc.player.getY();
            Vec3d centerVec = new Vec3d(MathHelper.floor(this.mc.player.getX()) + 0.5, this.mc.player.getY(), MathHelper.floor(this.mc.player.getZ()) + 0.5);
            Box centerBox = new Box(
                centerVec.getX() - 0.2, centerVec.getY() - 0.1, centerVec.getZ() - 0.2, centerVec.getX() + 0.2, centerVec.getY() + 0.1, centerVec.getZ() + 0.2
            );
            if (this.center.get() == CenterMode.Motion && !centerBox.contains(this.mc.player.getPos())) {
                this.mc
                    .player
                    .move(MovementType.SELF, new Vec3d((centerVec.getX() - this.mc.player.getX()) / 2.0, 0.0, (centerVec.getZ() - this.mc.player.getZ()) / 2.0));
            } else {
                List<BlockPos> blocks = this.getBlocks();
                if (!blocks.isEmpty()) {
                    if (this.delay > 0) {
                        this.delay--;
                    } else {
                        if (!this.getBlockResult().found()) {
                            this.toggle();
                        }

                        int placed = 0;
                        if (this.delay <= 0) {
                            while (placed < this.blocksPerTick.get()) {
                                if (!this.getBlockResult().found()) {
                                    this.toggle();
                                }

                                BlockPos targetBlock = this.getSequentialPos();
                                if (targetBlock == null || !this.placeBlock(targetBlock, true)) {
                                    break;
                                }

                                placed++;
                                this.delay = this.placeDelay.get();
                                this.inactivityTimer.reset();
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
    private void onPacketReceive(@NotNull Receive event) {
        if (!this.getBlockResult().found()) {
            this.toggle();
        }

        if (event.packet instanceof EntitySpawnS2CPacket spawn && spawn.getEntityType() == EntityType.END_CRYSTAL) {
            EndCrystalEntity cr = new EndCrystalEntity(this.mc.world, spawn.getX(), spawn.getY(), spawn.getZ());
            cr.setId(spawn.getEntityId());
            if (this.crystalBreaker.get() && cr.squaredDistanceTo(this.mc.player) <= this.getPow2Value(this.remove.get())) {
                this.handlePacket();
            }
        }

        if (event.packet instanceof BlockUpdateS2CPacket pac
            && this.mc.player.squaredDistanceTo(pac.getPos().toCenterPos()) < Math.pow(this.range.get(), 2.0)
            && pac.getState().isReplaceable()) {
            this.handlePacket();
        }

        if (event.packet instanceof PlayerPositionLookS2CPacket && this.onTp.get() == OnTpAction.Disable) {
            this.toggle();
        }
    }

    private void handlePacket() {
        BlockPos bp = this.getSequentialPos();
        if (bp != null && this.placeBlock(bp, InteractMode.Packet)) {
            this.inactivityTimer.reset();
        }
    }

    @Nullable
    private BlockPos getSequentialPos() {
        for (BlockPos bp : this.getBlocks()) {
            if (!new Box(bp).intersects(this.mc.player.getBoundingBox())
                && InteractionUtility.canPlaceBlock(bp, (InteractionUtility.Interact) this.interact.get(), true)
                && this.mc.world.getBlockState(bp).isReplaceable()) {
                return bp;
            }
        }

        return null;
    }

    @NotNull
    private List<BlockPos> getBlocks() {
        BlockPos playerPos = this.getPlayerPos();
        List<BlockPos> offsets = new ArrayList<>();
        if (this.center.get() == CenterMode.Disabled && this.mc.player != null) {
            double decimalX = Math.abs(this.mc.player.getX()) - Math.floor(Math.abs(this.mc.player.getX()));
            double decimalZ = Math.abs(this.mc.player.getZ()) - Math.floor(Math.abs(this.mc.player.getZ()));
            int lengthXPos = HoleUtility.calcLength(decimalX, false);
            int lengthXNeg = HoleUtility.calcLength(decimalX, true);
            int lengthZPos = HoleUtility.calcLength(decimalZ, false);
            int lengthZNeg = HoleUtility.calcLength(decimalZ, true);
            ArrayList<BlockPos> tempOffsets = new ArrayList<>();
            offsets.addAll(this.getOverlapPos());

            for (int x = 1; x < lengthXPos + 1; x++) {
                tempOffsets.add(HoleUtility.addToPlayer(playerPos, x, 0.0, 1 + lengthZPos));
                tempOffsets.add(HoleUtility.addToPlayer(playerPos, x, 0.0, -(1 + lengthZNeg)));
            }

            for (int var19 = 0; var19 <= lengthXNeg; var19++) {
                tempOffsets.add(HoleUtility.addToPlayer(playerPos, -var19, 0.0, 1 + lengthZPos));
                tempOffsets.add(HoleUtility.addToPlayer(playerPos, -var19, 0.0, -(1 + lengthZNeg)));
            }

            for (int z = 1; z < lengthZPos + 1; z++) {
                tempOffsets.add(HoleUtility.addToPlayer(playerPos, 1 + lengthXPos, 0.0, z));
                tempOffsets.add(HoleUtility.addToPlayer(playerPos, -(1 + lengthXNeg), 0.0, z));
            }

            for (int var17 = 0; var17 <= lengthZNeg; var17++) {
                tempOffsets.add(HoleUtility.addToPlayer(playerPos, 1 + lengthXPos, 0.0, -var17));
                tempOffsets.add(HoleUtility.addToPlayer(playerPos, -(1 + lengthXNeg), 0.0, -var17));
            }

            for (BlockPos pos : tempOffsets) {
                if (this.getDown(pos)) {
                    offsets.add(pos.add(0, -1, 0));
                }

                offsets.add(pos);
            }
        } else {
            offsets.add(playerPos.add(0, -1, 0));

            for (Vec3i surround : this.quad.get() ? HoleUtility.SQUAD_PATTERN : HoleUtility.VECTOR_PATTERN) {
                if (this.getDown(playerPos.add(surround))) {
                    offsets.add(playerPos.add(surround.getX(), -1, surround.getZ()));
                }

                offsets.add(playerPos.add(surround));
            }
        }

        return offsets;
    }

    private boolean getDown(BlockPos pos) {
        for (Direction dir : Direction.values()) {
            if (!this.mc.world.getBlockState(pos.add(dir.getVector())).isReplaceable()) {
                return false;
            }
        }

        return this.mc.world.getBlockState(pos).isReplaceable() && this.interact.get() != InteractionUtility.Interact.AirPlace;
    }

    @NotNull
    private List<BlockPos> getOverlapPos() {
        List<BlockPos> positions = new ArrayList<>();
        if (this.mc.player != null) {
            double decimalX = this.mc.player.getX() - Math.floor(this.mc.player.getX());
            double decimalZ = this.mc.player.getZ() - Math.floor(this.mc.player.getZ());
            int offX = HoleUtility.calcOffset(decimalX);
            int offZ = HoleUtility.calcOffset(decimalZ);
            positions.add(this.getPlayerPos());

            for (int x = 0; x <= Math.abs(offX); x++) {
                for (int z = 0; z <= Math.abs(offZ); z++) {
                    int properX = x * offX;
                    int properZ = z * offZ;
                    positions.add(Objects.requireNonNull(this.getPlayerPos()).add(properX, -1, properZ));
                }
            }
        }

        return positions;
    }

    @NotNull
    private BlockPos getPlayerPos() {
        return BlockPos.ofFloored(
            this.mc.player.getX(),
            this.mc.player.getY() - Math.floor(this.mc.player.getY()) > 0.8 ? Math.floor(this.mc.player.getY()) + 1.0 : Math.floor(this.mc.player.getY()),
            this.mc.player.getZ()
        );
    }

    private static enum CenterMode {
        Teleport,
        Motion,
        Disabled;
    }

    private static enum InteractMode {
        Packet,
        Normal;
    }

    private static enum OnTpAction {
        Disable,
        Stay,
        None;
    }
}
