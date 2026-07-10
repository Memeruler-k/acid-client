package dev.abstr3act.addon.modules.Compassion;

import dev.abstr3act.addon.Compassion;
import dev.abstr3act.addon.utils.abnormally.TPUtil;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent.Post;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.DoubleSetting.Builder;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.render.BreakIndicators;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.misc.Pool;
import meteordevelopment.meteorclient.utils.player.Rotations;
import meteordevelopment.meteorclient.utils.render.RenderUtils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket.Action;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;

import java.util.ArrayList;
import java.util.List;

public class TpMine extends Module {
    public final List<MyBlock> blocks = new ArrayList<>();
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
    private final SettingGroup sgRender = this.settings.createGroup("Render");
    private final Setting<Double> maxDistance = this.sgGeneral
        .add(((Builder) ((Builder) new Builder().name("max-distance")).description("The maximum distance you can teleport.")).defaultValue(20.0).min(0.0).build());
    private final Setting<Boolean> isRenderBlock = this.sgRender
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("render"))
                .description("Renders the target block."))
                .defaultValue(true))
                .build()
        );
    private final Setting<SettingColor> targetColor = this.sgRender
        .add(
            ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                .name("render-color"))
                .description("Set target block render color."))
                .defaultValue(new SettingColor(0, 255, 150, 255))
                .visible(this.isRenderBlock::get))
                .build()
        );
    private final Setting<Integer> delay = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("delay"))
                .description("Delay between mining blocks in ticks."))
                .defaultValue(1))
                .min(0)
                .build()
        );
    private final Setting<Boolean> rotate = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("rotate"))
                .description("Sends rotation packets to the server when mining."))
                .defaultValue(true))
                .build()
        );
    private final Setting<Boolean> autoSwitch = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("auto-switch"))
                .description("Automatically switches to the best tool when the block is ready to be mined instantly."))
                .defaultValue(false))
                .build()
        );
    private final Setting<Boolean> notOnUse = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("not-on-use"))
                .description("Won't auto switch if you're using an item."))
                .defaultValue(true))
                .visible(this.autoSwitch::get))
                .build()
        );
    private final Setting<Boolean> render = this.sgRender
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("render"))
                .description("Whether or not to render the block being mined."))
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
    private final Setting<SettingColor> readySideColor = this.sgRender
        .add(
            ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                .name("ready-side-color"))
                .description("The color of the sides of the blocks that can be broken."))
                .defaultValue(new SettingColor(0, 204, 0, 10))
                .build()
        );
    private final Setting<SettingColor> readyLineColor = this.sgRender
        .add(
            ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                .name("ready-line-color"))
                .description("The color of the lines of the blocks that can be broken."))
                .defaultValue(new SettingColor(0, 204, 0, 255))
                .build()
        );
    private final Setting<SettingColor> sideColor = this.sgRender
        .add(
            ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                .name("side-color"))
                .description("The color of the sides of the blocks being rendered."))
                .defaultValue(new SettingColor(204, 0, 0, 10))
                .build()
        );
    private final Setting<SettingColor> lineColor = this.sgRender
        .add(
            ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                .name("line-color"))
                .description("The color of the lines of the blocks being rendered."))
                .defaultValue(new SettingColor(204, 0, 0, 255))
                .build()
        );
    private final Pool<MyBlock> blockPool = new Pool(() -> new MyBlock());
    private final Setting<Double> moveDistance = this.sgGeneral
        .add(
            ((Builder) ((Builder) new Builder().name("move-distance")).description("Max distance for a packet to move."))
                .defaultValue(20.0)
                .sliderMax(1.0)
                .sliderMin(128.0)
                .build()
        );
    private boolean swapped;
    private boolean shouldUpdateSlot;
    private HitResult hitResult;
    private BlockPos pos;

    public TpMine() {
        super(Compassion.COMPASSION, "TPMine", "Teleports you to the block silently and breaks it.");
    }

    @EventHandler
    private void onTick(Post event) {
        this.blocks.removeIf(MyBlock::shouldRemove);
        this.hitResult = this.mc.player.raycast(this.maxDistance.get(), 0.05F, false);
        this.pos = ((BlockHitResult) this.hitResult).getBlockPos();
        if (this.mc.options.attackKey.isPressed() && this.hitResult.getType() == Type.BLOCK) {
            this.blocks.add(((MyBlock) this.blockPool.get()).set((BlockHitResult) this.hitResult));
            this.blocks.getFirst().sendMinePackets();
        }

        if (!this.blocks.isEmpty()) {
            Direction side = ((BlockHitResult) this.hitResult).getSide();
            BlockState state = this.mc.world.getBlockState(this.pos);
            VoxelShape shape = state.getCollisionShape(this.mc.world, this.pos);
            if (shape.isEmpty()) {
                shape = state.getOutlineShape(this.mc.world, this.pos);
            }

            double height = shape.isEmpty() ? 1.0 : shape.getMax(Axis.Y);
            double tx = this.pos.getX() + 0.5 + side.getOffsetX();
            double ty = this.pos.getY() + height;
            double tz = this.pos.getZ() + 0.5 + side.getOffsetZ();
            TPUtil.doTp(this.mc.player.getPos(), new Vec3d(tx, ty, tz), this.moveDistance.get(), false);
            this.blocks.getFirst().sendMinePackets();
        }
    }

    @EventHandler
    private void onRender(Render3DEvent event) {
        if (this.isRenderBlock.get()) {
            if (this.pos != null && this.hitResult.getType() == Type.BLOCK) {
                RenderUtils.renderTickingBlock(this.pos, (Color) this.targetColor.get(), (Color) this.targetColor.get(), ShapeMode.Lines, 0, 0, false, false);
                if (this.render.get()) {
                    for (MyBlock block : this.blocks) {
                        if (this.mc.world.getBlockState(block.blockPos) == Blocks.AIR.getDefaultState()) {
                            this.blocks.remove(block);
                            return;
                        }

                        if (!((BreakIndicators) Modules.get().get(BreakIndicators.class)).isActive()
                            || !((BreakIndicators) Modules.get().get(BreakIndicators.class)).packetMine.get()
                            || !block.mining) {
                            this.render(event, block.blockPos);
                        }
                    }
                }
            }
        }
    }

    public void render(Render3DEvent event, BlockPos blockPos) {
        VoxelShape shape = this.mc.world.getBlockState(blockPos).getOutlineShape(this.mc.world, blockPos);
        double x1 = blockPos.getX();
        double y1 = blockPos.getY();
        double z1 = blockPos.getZ();
        double x2 = blockPos.getX() + 1;
        double y2 = blockPos.getY() + 1;
        double z2 = blockPos.getZ() + 1;
        if (!shape.isEmpty()) {
            x1 = blockPos.getX() + shape.getMin(Axis.X);
            y1 = blockPos.getY() + shape.getMin(Axis.Y);
            z1 = blockPos.getZ() + shape.getMin(Axis.Z);
            x2 = blockPos.getX() + shape.getMax(Axis.X);
            y2 = blockPos.getY() + shape.getMax(Axis.Y);
            z2 = blockPos.getZ() + shape.getMax(Axis.Z);
        }

        event.renderer.box(x1, y1, z1, x2, y2, z2, (Color) this.sideColor.get(), (Color) this.lineColor.get(), (ShapeMode) this.shapeMode.get(), 0);
    }

    public class MyBlock {
        public BlockPos blockPos;
        public BlockState blockState;
        public Block block;
        public Direction direction;
        public int timer;
        public boolean mining;
        public double progress;

        public MyBlock set(BlockHitResult hitResult) {
            this.blockPos = hitResult.getBlockPos();
            this.direction = hitResult.getSide();
            this.blockState = TpMine.this.mc.world.getBlockState(this.blockPos);
            this.block = this.blockState.getBlock();
            this.timer = TpMine.this.delay.get();
            this.mining = false;
            this.progress = 0.0;
            return this;
        }

        public boolean shouldRemove() {
            boolean remove = TpMine.this.mc.world.getBlockState(this.blockPos).getBlock() != this.block
                || Utils.distance(
                TpMine.this.mc.player.getX() - 0.5,
                TpMine.this.mc.player.getY() + TpMine.this.mc.player.getEyeHeight(TpMine.this.mc.player.getPose()),
                TpMine.this.mc.player.getZ() - 0.5,
                this.blockPos.getX() + this.direction.getOffsetX(),
                this.blockPos.getY() + this.direction.getOffsetY(),
                this.blockPos.getZ() + this.direction.getOffsetZ()
            )
                > TpMine.this.mc.player.getBlockInteractionRange();
            if (remove) {
                TpMine.this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.ABORT_DESTROY_BLOCK, this.blockPos, this.direction));
                TpMine.this.mc.getNetworkHandler().sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
            }

            return remove;
        }

        public boolean isReady() {
            return this.progress >= 1.0;
        }

        public void mine() {
            if (TpMine.this.rotate.get()) {
                Rotations.rotate(Rotations.getYaw(this.blockPos), Rotations.getPitch(this.blockPos), 50, this::sendMinePackets);
            } else {
                this.sendMinePackets();
            }

            double bestScore = -1.0;
            int bestSlot = -1;

            for (int i = 0; i < 9; i++) {
                double score = TpMine.this.mc.player.getInventory().getStack(i).getMiningSpeedMultiplier(this.blockState);
                if (score > bestScore) {
                    bestScore = score;
                    bestSlot = i;
                }
            }

            this.progress = this.progress + BlockUtils.getBreakDelta(bestSlot != -1 ? bestSlot : TpMine.this.mc.player.getInventory().selectedSlot, this.blockState);
        }

        private void sendMinePackets() {
            if (this.timer <= 0) {
                if (!this.mining) {
                    TpMine.this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.START_DESTROY_BLOCK, this.blockPos, this.direction));
                    TpMine.this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.STOP_DESTROY_BLOCK, this.blockPos, this.direction));
                    this.mining = true;
                }
            } else {
                this.timer--;
            }
        }

        public void render(Render3DEvent event) {
            VoxelShape shape = TpMine.this.mc.world.getBlockState(this.blockPos).getOutlineShape(TpMine.this.mc.world, this.blockPos);
            double x1 = this.blockPos.getX();
            double y1 = this.blockPos.getY();
            double z1 = this.blockPos.getZ();
            double x2 = this.blockPos.getX() + 1;
            double y2 = this.blockPos.getY() + 1;
            double z2 = this.blockPos.getZ() + 1;
            if (!shape.isEmpty()) {
                x1 = this.blockPos.getX() + shape.getMin(Axis.X);
                y1 = this.blockPos.getY() + shape.getMin(Axis.Y);
                z1 = this.blockPos.getZ() + shape.getMin(Axis.Z);
                x2 = this.blockPos.getX() + shape.getMax(Axis.X);
                y2 = this.blockPos.getY() + shape.getMax(Axis.Y);
                z2 = this.blockPos.getZ() + shape.getMax(Axis.Z);
            }

            if (this.isReady()) {
                event.renderer
                    .box(
                        x1, y1, z1, x2, y2, z2, (Color) TpMine.this.readySideColor.get(), (Color) TpMine.this.readyLineColor.get(), (ShapeMode) TpMine.this.shapeMode.get(), 0
                    );
            } else {
                event.renderer
                    .box(x1, y1, z1, x2, y2, z2, (Color) TpMine.this.sideColor.get(), (Color) TpMine.this.lineColor.get(), (ShapeMode) TpMine.this.shapeMode.get(), 0);
            }
        }
    }
}
