package dev.abstr3act.addon.modules.Abnormally;

import dev.abstr3act.addon.Compassion;
import dev.abstr3act.addon.module.AbnormallyModule;
import dev.abstr3act.addon.utils.TargetUtils;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent.Pre;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.IntSetting.Builder;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.entity.SortPriority;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.player.Rotations;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Blocks;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.Objects;

public class MinecartAura extends AbnormallyModule {
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
    private final SettingGroup sgExplode = this.settings.createGroup("Explode");
    private final SettingGroup sgPause = this.settings.createGroup("Pause");
    private final SettingGroup sgRender = this.settings.createGroup("Render");
    private final Setting<Integer> range = this.sgGeneral
        .add(
            ((Builder) ((Builder) ((Builder) new Builder().name("target-range")).description("Range to find target.")).defaultValue(12))
                .range(1, 12)
                .sliderMax(12)
                .build()
        );
    private final Setting<Boolean> rotate = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("rotate"))
                .defaultValue(true))
                .build()
        );
    private final Setting<Boolean> oldPlacements = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("1.12-placement"))
                .defaultValue(true))
                .build()
        );
    private final Setting<Integer> railDelay = this.sgGeneral
        .add(
            ((Builder) ((Builder) ((Builder) new Builder().name("rail-delay")).description("Delay to place rail")).defaultValue(15)).range(1, 35).sliderMax(35).build()
        );
    private final Setting<Integer> tntDelay = this.sgGeneral
        .add(((Builder) ((Builder) ((Builder) new Builder().name("tnt-delay")).description("Delay to place tnt")).defaultValue(15)).range(1, 35).sliderMax(35).build());
    private final Setting<explode> explodeMethod = this.sgExplode
        .add(
            ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) new meteordevelopment.meteorclient.settings.EnumSetting.Builder()
                .name("explode-method"))
                .defaultValue(explode.BowFlame))
                .build()
        );
    private final Setting<Integer> pullDelay = this.sgExplode
        .add(
            ((Builder) ((Builder) ((Builder) new Builder().name("pull-delay")).defaultValue(20))
                .range(5, 20)
                .sliderMax(20)
                .visible(() -> this.explodeMethod.get() == explode.BowFlame))
                .build()
        );
    private final Setting<Integer> explodeDelay = this.sgExplode
        .add(
            ((Builder) ((Builder) ((Builder) new Builder().name("explode-delay")).description("Delay to place tnt")).defaultValue(15))
                .range(1, 35)
                .sliderMax(35)
                .build()
        );
    private final Setting<Boolean> instant = this.sgPause
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("instant"))
                .description(""))
                .defaultValue(false))
                .build()
        );
    private final Setting<Integer> cartCount = this.sgExplode
        .add(((Builder) ((Builder) new Builder().name("cart-count")).defaultValue(1)).range(1, 100).sliderMax(100).build());
    private final Setting<Boolean> pauseMine = this.sgPause
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("pause-on-mine"))
                .description(""))
                .defaultValue(true))
                .build()
        );
    private final Setting<Boolean> pauseEat = this.sgPause
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("pause-on-eat"))
                .description(""))
                .defaultValue(true))
                .build()
        );
    private final Setting<Boolean> pauseDrink = this.sgPause
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("pause-on-drink"))
                .description(""))
                .defaultValue(true))
                .build()
        );
    private final Setting<Boolean> web = this.sgRender
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("web"))
                .defaultValue(true))
                .build()
        );
    private final Setting<Integer> webDelay = this.sgExplode
        .add(
            ((Builder) ((Builder) ((Builder) ((Builder) new Builder().name("web-delay")).description("Delay to place web")).defaultValue(15))
                .range(1, 35)
                .sliderMax(35)
                .visible(this.web::get))
                .build()
        );
    private final Setting<Boolean> swing = this.sgRender
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("swing"))
                .defaultValue(true))
                .build()
        );
    private final Setting<ShapeMode> shapeMode = this.sgRender
        .add(
            ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) new meteordevelopment.meteorclient.settings.EnumSetting.Builder()
                .name("shape-mode"))
                .description("How the shapes are rendered."))
                .defaultValue(ShapeMode.Both))
                .build()
        );
    private final Setting<SettingColor> sideColor = this.sgRender
        .add(
            ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                .name("side-color"))
                .description("The side color of the rendering."))
                .defaultValue(new SettingColor(225, 0, 0, 75))
                .build()
        );
    private final Setting<SettingColor> lineColor = this.sgRender
        .add(
            ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                .name("line-color"))
                .description("The line color of the rendering."))
                .defaultValue(new SettingColor(225, 0, 0, 255))
                .build()
        );
    public PlayerEntity target;
    int railTick;
    int tntTick;
    int explodeTicks;
    int webTick;
    BlockPos targetPos;
    float yaw;
    float pitch;
    int t = 0;
    private Stage stage;

    public MinecartAura() {
        super(Compassion.ABNORMALLY, "MinecartAura", "Automatically places and explodes minecarts");
    }

    public void onActivate() {
        this.target = null;
        this.railTick = 0;
        this.tntTick = 0;
        this.webTick = 0;
        this.explodeTicks = 0;
        this.stage = Stage.Preparing;
        super.onActivate();
    }

    public void onDeactivate() {
        if (((explode) this.explodeMethod.get()).equals(explode.BowFlame)) {
            this.mc.options.useKey.setPressed(false);
        }

        Rotations.rotate(this.yaw, this.pitch, 50);
        super.onDeactivate();
    }

    public void rotateToEntityType(EntityType en) {
        double closest = Double.MAX_VALUE;
        Vec3d pos = this.mc.player.getPos();
        Vec3d nearest = null;

        for (Entity entity : this.mc.world.getEntities()) {
            if (entity.getType() == en) {
                double distance = entity.getPos().squaredDistanceTo(pos);
                if (distance < closest) {
                    closest = distance;
                    nearest = entity.getPos();
                }
            }
        }

        if (nearest != null) {
            double yaw = Math.atan2(nearest.getZ() - pos.getZ(), nearest.getX() - pos.getX()) * (180.0 / Math.PI) - 90.0;
            double pitch = Math.atan2(nearest.getY() - pos.getY(), Math.sqrt(Math.pow(nearest.getX() - pos.getX(), 2.0) + Math.pow(nearest.getZ() - pos.getZ(), 2.0)))
                * (180.0 / Math.PI);
            Rotations.rotate(yaw, pitch, 100);
        }
    }

    @EventHandler
    public void onTick(Pre event) {
        this.target = TargetUtils.getPlayerTarget((this.range.get()).intValue(), SortPriority.LowestDistance);
        if (!TargetUtils.isBadTarget(this.target, (this.range.get()).intValue())) {
            if (!PlayerUtils.shouldPause(this.pauseMine.get(), this.pauseEat.get(), this.pauseDrink.get())) {
                switch (this.stage) {
                    case Preparing:
                        if (this.findPlacePos(this.target.getBlockPos()) != null) {
                            this.yaw = this.mc.player.getYaw();
                            this.pitch = this.mc.player.getPitch();
                            this.targetPos = this.target.getBlockPos();
                            if (this.mc.world.getBlockState(this.targetPos.down()).getBlock() == Blocks.AIR && this.oldPlacements.get()) {
                                FindItemResult block = InvUtils.findInHotbar(itemStack -> itemStack.getItem() instanceof BlockItem);
                                BlockUtils.place(this.targetPos.down(1), block, this.rotate.get(), 50, this.swing.get(), true);
                            }

                            this.stage = Stage.Rail;
                        }
                        break;
                    case Rail:
                        this.railTick++;
                        if (this.railTick >= this.railDelay.get()) {
                            if (this.mc.world.getBlockState(this.targetPos).getBlock() == Blocks.RAIL) {
                                this.stage = Stage.Tnt;
                            }

                            FindItemResult rail = InvUtils.findInHotbar(new Item[]{Items.RAIL});
                            if (BlockUtils.place(this.targetPos, rail, this.rotate.get(), 50, this.swing.get(), true)) {
                                this.yaw = this.mc.player.getYaw();
                                this.pitch = this.mc.player.getPitch();
                                this.stage = Stage.Tnt;
                            }
                        }
                        break;
                    case Tnt:
                        this.tntTick++;
                        if (this.tntTick >= this.tntDelay.get()) {
                            FindItemResult tnt = InvUtils.findInHotbar(new Item[]{Items.TNT_MINECART});
                            if (!tnt.isHotbar() && tnt.count() != 0) {
                                InvUtils.move().from(tnt.slot()).toHotbar(this.mc.player.getInventory().selectedSlot);
                            }

                            InvUtils.swap(tnt.slot(), true);

                            for (int i = 0; i < this.cartCount.get(); i++) {
                                Objects.requireNonNull(this.mc.interactionManager)
                                    .interactBlock(
                                        this.mc.player,
                                        Hand.MAIN_HAND,
                                        new BlockHitResult(
                                            new Vec3d(this.targetPos.getX() + 0.5, this.targetPos.getY() + 0.5, this.target.getZ() + 0.5), Direction.UP, this.targetPos, true
                                        )
                                    );
                            }

                            InvUtils.swapBack();
                            if (this.web.get()) {
                                this.stage = Stage.Web;
                            } else {
                                this.stage = Stage.Ignite;
                            }
                        }
                        break;
                    case Ignite:
                        switch ((explode) this.explodeMethod.get()) {
                            case BowFlame:
                                this.explodeTicks++;
                                if (this.explodeTicks >= this.explodeDelay.get()) {
                                    int delay = this.instant.get() ? 5 : this.pullDelay.get();
                                    FindItemResult bow = InvUtils.findInHotbar(new Item[]{Items.BOW});
                                    if (bow.isHotbar()) {
                                        InvUtils.swap(bow.slot(), true);

                                        assert this.mc.player != null;

                                        ItemStack mainHandStack = this.mc.player.getMainHandStack();
                                        if (mainHandStack.getItem() == Items.BOW && Utils.hasEnchantments(mainHandStack, new RegistryKey[]{Enchantments.FLAME})) {
                                            this.mc.options.useKey.setPressed(true);
                                            if (this.mc.player.getItemUseTime() > delay) {
                                                assert this.mc.interactionManager != null;

                                                this.rotateToEntityType(EntityType.TNT_MINECART);
                                                this.mc.interactionManager.stopUsingItem(this.mc.player);
                                                this.mc.options.useKey.setPressed(false);
                                                InvUtils.swapBack();
                                                this.stage = Stage.Rail;
                                                return;
                                            }
                                        }

                                        return;
                                    }
                                }

                                return;
                            case FlintSteel:
                                this.explodeTicks++;
                                if (this.explodeTicks >= this.explodeDelay.get()) {
                                    FindItemResult flint = InvUtils.findInHotbar(new Item[]{Items.FLINT_AND_STEEL, Items.FIRE_CHARGE});
                                    if (flint.isHotbar()) {
                                        InvUtils.swap(flint.slot(), true);
                                        this.mc
                                            .interactionManager
                                            .interactBlock(
                                                this.mc.player,
                                                Hand.MAIN_HAND,
                                                new BlockHitResult(
                                                    new Vec3d(this.targetPos.getX() + 0.5, this.targetPos.getY() + 0.5, this.target.getZ() + 0.5), Direction.UP, this.targetPos, true
                                                )
                                            );
                                        InvUtils.swapBack();
                                        this.stage = Stage.Rail;
                                        return;
                                    }
                                }

                                return;
                            case RedstoneBlock:
                                this.explodeTicks++;
                                if (this.explodeTicks >= this.explodeDelay.get()) {
                                    FindItemResult r = InvUtils.findInHotbar(new Item[]{Items.REDSTONE_BLOCK});
                                    if (r.isHotbar()) {
                                        InvUtils.swap(r.slot(), true);
                                        BlockUtils.place(this.findPlacePos(this.targetPos), r, this.rotate.get(), 50, this.swing.get(), true);
                                        InvUtils.swapBack();
                                        this.stage = Stage.Rail;
                                        return;
                                    }
                                }

                                return;
                            case Torch:
                                this.explodeTicks++;
                                if (this.explodeTicks >= this.explodeDelay.get()) {
                                    FindItemResult t = InvUtils.findInHotbar(new Item[]{Items.REDSTONE_TORCH});
                                    if (t.isHotbar()) {
                                        InvUtils.swap(t.slot(), true);
                                        BlockUtils.place(this.findPlacePos(this.targetPos), t, this.rotate.get(), 50, this.swing.get(), true);
                                        InvUtils.swapBack();
                                        this.stage = Stage.Rail;
                                        return;
                                    }
                                }

                                return;
                            default:
                                return;
                        }
                    case Replace:
                        this.tntTick++;
                        if (this.tntTick >= this.tntDelay.get()) {
                            FindItemResult tntx = InvUtils.findInHotbar(new Item[]{Items.TNT_MINECART});
                            if (!tntx.isHotbar() && tntx.count() != 0) {
                                InvUtils.move().from(tntx.slot()).toHotbar(this.mc.player.getInventory().selectedSlot);
                            }

                            InvUtils.swap(tntx.slot(), true);

                            for (int i = 0; i < 5; i++) {
                                Objects.requireNonNull(this.mc.interactionManager)
                                    .interactBlock(
                                        this.mc.player,
                                        Hand.MAIN_HAND,
                                        new BlockHitResult(
                                            new Vec3d(this.targetPos.getX() + 0.5, this.targetPos.getY() + 0.5, this.target.getZ() + 0.5), Direction.UP, this.targetPos, true
                                        )
                                    );
                            }

                            InvUtils.swapBack();
                            this.stage = Stage.Ignite;
                        }
                        break;
                    case Web:
                        if (this.web.get()) {
                            this.webTick++;
                            if (this.webTick >= this.webDelay.get()) {
                                FindItemResult web = InvUtils.findInHotbar(new Item[]{Items.COBWEB});
                                if (!web.isHotbar() && web.count() != 0) {
                                    InvUtils.move().from(web.slot()).toHotbar(this.mc.player.getInventory().selectedSlot);
                                }

                                if (BlockUtils.place(this.targetPos.up(1), web, this.rotate.get(), 50, this.swing.get(), true)) {
                                    this.yaw = this.mc.player.getYaw();
                                    this.pitch = this.mc.player.getPitch();
                                    this.stage = Stage.Ignite;
                                }
                            }
                        }
                }
            }
        }
    }

    private BlockPos findPlacePos(BlockPos targetPos) {
        BlockPos var2 = new BlockPos(targetPos.down(1));
        if (BlockUtils.canPlace(var2.add(0, 1, 1))) {
            return var2.add(0, 1, 1);
        } else if (BlockUtils.canPlace(var2.add(1, 1, 0))) {
            return var2.add(1, 1, 0);
        } else if (BlockUtils.canPlace(var2.add(1, 1, -1))) {
            return var2.add(1, 1, -1);
        } else {
            return BlockUtils.canPlace(var2.add(-1, 1, 0)) ? var2.add(-1, 1, 0) : null;
        }
    }

    @EventHandler
    private void onRender(Render3DEvent event) {
        if (this.target != null || this.targetPos != null) {
            event.renderer.box(this.targetPos, (Color) this.sideColor.get(), (Color) this.lineColor.get(), (ShapeMode) this.shapeMode.get(), 0);
        }
    }

    private static enum Stage {
        Preparing,
        Rail,
        Tnt,
        Ignite,
        Replace,
        Web;
    }

    public static enum explode {
        BowFlame,
        FlintSteel,
        RedstoneBlock,
        Torch;
    }
}
