package dev.abstr3act.addon.modules.Abnormally;

import dev.abstr3act.addon.Compassion;
import dev.abstr3act.addon.module.AbnormallyModule;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent.Post;
import meteordevelopment.meteorclient.events.world.TickEvent.Pre;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.IntSetting.Builder;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.utils.misc.Pool;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.player.Rotations;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.meteorclient.utils.world.BlockIterator;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShapeContext;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.Mutable;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class AutoWither extends AbnormallyModule {
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
    private final SettingGroup sgRender = this.settings.createGroup("Render");
    private final Setting<Integer> horizontalRadius = this.sgGeneral
        .add(
            ((Builder) ((Builder) ((Builder) new Builder().name("horizontal-radius")).description("Horizontal radius for placement")).defaultValue(4))
                .min(0)
                .sliderMax(6)
                .build()
        );
    private final Setting<Integer> verticalRadius = this.sgGeneral
        .add(
            ((Builder) ((Builder) ((Builder) new Builder().name("vertical-radius")).description("Vertical radius for placement")).defaultValue(3))
                .min(0)
                .sliderMax(6)
                .build()
        );
    private final Setting<Priority> priority = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) new meteordevelopment.meteorclient.settings.EnumSetting.Builder()
                .name("priority"))
                .description("Priority"))
                .defaultValue(Priority.Random))
                .build()
        );
    private final Setting<Integer> witherDelay = this.sgGeneral
        .add(
            ((Builder) ((Builder) ((Builder) new Builder().name("wither-delay")).description("Delay in ticks between wither placements")).defaultValue(1))
                .min(1)
                .sliderMax(10)
                .build()
        );
    private final Setting<Integer> blockDelay = this.sgGeneral
        .add(
            ((Builder) ((Builder) ((Builder) new Builder().name("block-delay")).description("Delay in ticks between block placements")).defaultValue(1))
                .min(0)
                .sliderMax(10)
                .build()
        );
    private final Setting<Boolean> rotate = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("rotate"))
                .description("Whether or not to rotate while building"))
                .defaultValue(true))
                .build()
        );
    private final Setting<Boolean> turnOff = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("turn-off"))
                .description("Turns off automatically after building a single wither."))
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
                .description("The side color of the target block rendering."))
                .defaultValue(new SettingColor(197, 137, 232, 10))
                .build()
        );
    private final Setting<SettingColor> lineColor = this.sgRender
        .add(
            ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                .name("line-color"))
                .description("The line color of the target block rendering."))
                .defaultValue(new SettingColor(197, 137, 232))
                .build()
        );
    private final Pool<Wither> witherPool = new Pool(Wither::new);
    private final ArrayList<Wither> withers = new ArrayList<>();
    private Wither wither;
    private int witherTicksWaited;
    private int blockTicksWaited;

    public AutoWither() {
        super(Compassion.ABNORMALLY, "AutoWither", "Automatically builds withers.");
    }

    public void onDeactivate() {
        this.wither = null;
    }

    @EventHandler
    private void onTick(Pre event) {
        if (this.wither == null) {
            if (this.witherTicksWaited < this.witherDelay.get() - 1) {
                return;
            }

            for (Wither wither : this.withers) {
                this.witherPool.free(wither);
            }

            this.withers.clear();
            BlockIterator.register(this.horizontalRadius.get(), this.verticalRadius.get(), (blockPos, blockState) -> {
                Direction dir = Direction.fromRotation(Rotations.getYaw(blockPos)).getOpposite();
                if (this.isValidSpawn(blockPos, dir)) {
                    this.withers.add(((Wither) this.witherPool.get()).set(blockPos, dir));
                }
            });
        }
    }

    @EventHandler
    private void onPostTick(Post event) {
        if (this.wither == null) {
            if (this.witherTicksWaited < this.witherDelay.get() - 1) {
                this.witherTicksWaited++;
                return;
            }

            if (this.withers.isEmpty()) {
                return;
            }

            switch ((Priority) this.priority.get()) {
                case Closest:
                    this.withers.sort(Comparator.comparingDouble(w -> PlayerUtils.distanceTo(w.foot)));
                case Furthest:
                    this.withers.sort((w1, w2) -> {
                        int sort = Double.compare(PlayerUtils.distanceTo(w1.foot), PlayerUtils.distanceTo(w2.foot));
                        if (sort == 0) {
                            return 0;
                        } else {
                            return sort > 0 ? -1 : 1;
                        }
                    });
                case Random:
                    Collections.shuffle(this.withers);
                default:
                    this.wither = this.withers.get(0);
            }
        }

        FindItemResult findSoulSand = InvUtils.findInHotbar(new Item[]{Items.SOUL_SAND});
        if (!findSoulSand.found()) {
            findSoulSand = InvUtils.findInHotbar(new Item[]{Items.SOUL_SOIL});
        }

        FindItemResult findWitherSkull = InvUtils.findInHotbar(new Item[]{Items.WITHER_SKELETON_SKULL});
        if (findSoulSand.found() && findWitherSkull.found()) {
            if (this.blockDelay.get() == 0) {
                BlockUtils.place(this.wither.foot, findSoulSand, this.rotate.get(), -50);
                BlockUtils.place(this.wither.foot.up(), findSoulSand, this.rotate.get(), -50);
                BlockUtils.place(this.wither.foot.up().offset(this.wither.axis, -1), findSoulSand, this.rotate.get(), -50);
                BlockUtils.place(this.wither.foot.up().offset(this.wither.axis, 1), findSoulSand, this.rotate.get(), -50);
                BlockUtils.place(this.wither.foot.up().up(), findWitherSkull, this.rotate.get(), -50);
                BlockUtils.place(this.wither.foot.up().up().offset(this.wither.axis, -1), findWitherSkull, this.rotate.get(), -50);
                BlockUtils.place(this.wither.foot.up().up().offset(this.wither.axis, 1), findWitherSkull, this.rotate.get(), -50);
                if (this.turnOff.get()) {
                    this.wither = null;
                    this.toggle();
                }
            } else {
                if (this.blockTicksWaited < this.blockDelay.get() - 1) {
                    this.blockTicksWaited++;
                    return;
                }

                switch (this.wither.stage) {
                    case 0:
                        if (BlockUtils.place(this.wither.foot, findSoulSand, this.rotate.get(), -50)) {
                            this.wither.stage++;
                        }
                        break;
                    case 1:
                        if (BlockUtils.place(this.wither.foot.up(), findSoulSand, this.rotate.get(), -50)) {
                            this.wither.stage++;
                        }
                        break;
                    case 2:
                        if (BlockUtils.place(this.wither.foot.up().offset(this.wither.axis, -1), findSoulSand, this.rotate.get(), -50)) {
                            this.wither.stage++;
                        }
                        break;
                    case 3:
                        if (BlockUtils.place(this.wither.foot.up().offset(this.wither.axis, 1), findSoulSand, this.rotate.get(), -50)) {
                            this.wither.stage++;
                        }
                        break;
                    case 4:
                        if (BlockUtils.place(this.wither.foot.up().up(), findWitherSkull, this.rotate.get(), -50)) {
                            this.wither.stage++;
                        }
                        break;
                    case 5:
                        if (BlockUtils.place(this.wither.foot.up().up().offset(this.wither.axis, -1), findWitherSkull, this.rotate.get(), -50)) {
                            this.wither.stage++;
                        }
                        break;
                    case 6:
                        if (BlockUtils.place(this.wither.foot.up().up().offset(this.wither.axis, 1), findWitherSkull, this.rotate.get(), -50)) {
                            this.wither.stage++;
                        }
                        break;
                    case 7:
                        if (this.turnOff.get()) {
                            this.wither = null;
                            this.toggle();
                        }
                }
            }

            this.witherTicksWaited = 0;
        } else {
            this.error("Not enough resources in hotbar", new Object[0]);
            this.toggle();
        }
    }

    @EventHandler
    private void onRender(Render3DEvent event) {
        if (this.wither != null) {
            event.renderer.box(this.wither.foot, (Color) this.sideColor.get(), (Color) this.lineColor.get(), (ShapeMode) this.shapeMode.get(), 0);
            event.renderer.box(this.wither.foot.up(), (Color) this.sideColor.get(), (Color) this.lineColor.get(), (ShapeMode) this.shapeMode.get(), 0);
            event.renderer
                .box(this.wither.foot.up().offset(this.wither.axis, -1), (Color) this.sideColor.get(), (Color) this.lineColor.get(), (ShapeMode) this.shapeMode.get(), 0);
            event.renderer
                .box(this.wither.foot.up().offset(this.wither.axis, 1), (Color) this.sideColor.get(), (Color) this.lineColor.get(), (ShapeMode) this.shapeMode.get(), 0);
            BlockPos midHead = this.wither.foot.up().up();
            BlockPos leftHead = this.wither.foot.up().up().offset(this.wither.axis, -1);
            BlockPos rightHead = this.wither.foot.up().up().offset(this.wither.axis, 1);
            event.renderer
                .box(
                    midHead.getX() + 0.2,
                    midHead.getX(),
                    midHead.getX() + 0.2,
                    midHead.getX() + 0.8,
                    midHead.getX() + 0.7,
                    midHead.getX() + 0.8,
                    (Color) this.sideColor.get(),
                    (Color) this.lineColor.get(),
                    (ShapeMode) this.shapeMode.get(),
                    0
                );
            event.renderer
                .box(
                    leftHead.getX() + 0.2,
                    leftHead.getX(),
                    leftHead.getX() + 0.2,
                    leftHead.getX() + 0.8,
                    leftHead.getX() + 0.7,
                    leftHead.getX() + 0.8,
                    (Color) this.sideColor.get(),
                    (Color) this.lineColor.get(),
                    (ShapeMode) this.shapeMode.get(),
                    0
                );
            event.renderer
                .box(
                    rightHead.getX() + 0.2,
                    rightHead.getX(),
                    rightHead.getX() + 0.2,
                    rightHead.getX() + 0.8,
                    rightHead.getX() + 0.7,
                    rightHead.getX() + 0.8,
                    (Color) this.sideColor.get(),
                    (Color) this.lineColor.get(),
                    (ShapeMode) this.shapeMode.get(),
                    0
                );
        }
    }

    private boolean isValidSpawn(BlockPos blockPos, Direction direction) {
        if (blockPos.getY() > 252) {
            return false;
        } else {
            int widthX = 0;
            int widthZ = 0;
            if (direction == Direction.EAST || direction == Direction.WEST) {
                widthZ = 1;
            }

            if (direction == Direction.NORTH || direction == Direction.SOUTH) {
                widthX = 1;
            }

            Mutable bp = new Mutable();

            for (int x = blockPos.getX() - widthX; x <= blockPos.getX() + widthX; x++) {
                for (int z = blockPos.getZ() - widthZ; z <= blockPos.getZ(); z++) {
                    for (int y = blockPos.getY(); y <= blockPos.getY() + 2; y++) {
                        bp.set(x, y, z);
                        if (!this.mc.world.getBlockState(bp).isReplaceable()) {
                            return false;
                        }

                        if (!this.mc.world.canPlace(Blocks.STONE.getDefaultState(), bp, ShapeContext.absent())) {
                            return false;
                        }
                    }
                }
            }

            return true;
        }
    }

    public static enum Priority {
        Closest,
        Furthest,
        Random;
    }

    private static class Wither {
        public int stage;
        public Mutable foot = new Mutable();
        public Direction facing;
        public Axis axis;

        public Wither set(BlockPos pos, Direction dir) {
            this.stage = 0;
            this.foot.set(pos);
            this.facing = dir;
            this.axis = dir.getAxis();
            return this;
        }
    }
}
