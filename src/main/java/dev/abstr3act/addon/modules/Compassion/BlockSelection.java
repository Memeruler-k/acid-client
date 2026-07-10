package dev.abstr3act.addon.modules.Compassion;

import dev.abstr3act.addon.Compassion;
import dev.abstr3act.addon.module.CompassionModule;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.BoolSetting.Builder;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.BlockState;
import net.minecraft.item.Items;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;

public class BlockSelection extends CompassionModule {
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
    private final Setting<Boolean> advanced = this.sgGeneral
        .add(
            ((Builder) ((Builder) ((Builder) new Builder().name("advanced")).description("Shows a more advanced outline on different types of shape blocks."))
                .defaultValue(true))
                .build()
        );
    private final Setting<Boolean> oneSide = this.sgGeneral
        .add(
            ((Builder) ((Builder) ((Builder) new Builder().name("single-side")).description("Only renders the side you are looking at.")).defaultValue(false)).build()
        );
    private final Setting<ShapeMode> shapeMode = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) new meteordevelopment.meteorclient.settings.EnumSetting.Builder()
                .name("shape-mode"))
                .description("How the shapes are rendered."))
                .defaultValue(ShapeMode.Both))
                .build()
        );
    private final Setting<SettingColor> sideColor = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                .name("side-color"))
                .description("The side color."))
                .defaultValue(new SettingColor(255, 255, 255, 50))
                .build()
        );
    private final Setting<SettingColor> sideColor2 = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                .name("side-color-2"))
                .description("The side color."))
                .defaultValue(new SettingColor(255, 255, 255, 50))
                .build()
        );
    private final Setting<SettingColor> lineColor = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                .name("line-color"))
                .description("The line color."))
                .defaultValue(new SettingColor(255, 255, 255, 255))
                .build()
        );
    private final Setting<SettingColor> lineColor2 = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                .name("line-color-2"))
                .description("The line color."))
                .defaultValue(new SettingColor(255, 255, 255, 255))
                .build()
        );
    private final Setting<Boolean> hideInside = this.sgGeneral
        .add(
            ((Builder) ((Builder) ((Builder) new Builder().name("hide-when-inside-block")).description("Hide selection when inside target block.")).defaultValue(true))
                .build()
        );
    private final Setting<Double> rainbowSpeed = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("rainbow-speed"))
                .description("Rainbow speed of rainbow color mode."))
                .defaultValue(0.05)
                .sliderMin(0.01)
                .sliderMax(0.2)
                .decimalPlaces(4)
                .build()
        );
    private final Color c1 = new Color(255, 255, 255);
    private final Color c2 = new Color(255, 255, 255);
    private Color color;
    private Color color2;
    private double r1;
    private double r2;
    private double r3;
    private double r4;

    public BlockSelection() {
        super(Compassion.COMPASSION, "NewBlockSelection", "Modifies how your block selection is rendered.");
    }

    private void updateColor(Render3DEvent renderer) {
        this.r1 = this.r1 + this.rainbowSpeed.get() * renderer.tickDelta;
        if (this.r1 > 1.0) {
            this.r1--;
        } else if (this.r1 < 0.0) {
            this.r1++;
        }

        this.r2 = this.r1;
        this.r1 = this.r1 + this.rainbowSpeed.get() * renderer.tickDelta;
        if (this.r1 > 1.0) {
            this.r1--;
        } else if (this.r1 < 0.0) {
            this.r1++;
        }

        double progress = Math.sin((this.r1 + 1.0) * Math.PI * 2.0);
        progress = (progress + 1.0) / 2.0;
        Color redColor = (Color) this.sideColor.get();
        Color whiteColor = (Color) this.sideColor2.get();
        int red = (int) (redColor.r * (1.0 - progress) + whiteColor.r * progress);
        int green = (int) (redColor.g * (1.0 - progress) + whiteColor.g * progress);
        int blue = (int) (redColor.b * (1.0 - progress) + whiteColor.b * progress);
        int alpha = redColor.a;
        this.c1.set(red, green, blue, alpha);
        this.color = this.c1;
        this.r1 = this.r1 + this.rainbowSpeed.get() * renderer.tickDelta;
        if (this.r1 > 1.0) {
            this.r1--;
        } else if (this.r1 < 0.0) {
            this.r1++;
        }

        this.r4 = this.r3;
        this.r3 = this.r3 + this.rainbowSpeed.get() * renderer.tickDelta;
        if (this.r3 > 1.0) {
            this.r3--;
        } else if (this.r3 < 0.0) {
            this.r3++;
        }

        double progress2 = Math.sin((this.r3 + 1.0) * Math.PI * 2.0);
        progress2 = (progress2 + 1.0) / 2.0;
        Color redColor2 = (Color) this.sideColor.get();
        Color whiteColor2 = (Color) this.sideColor2.get();
        int red2 = (int) (redColor2.r * (1.0 - progress2) + whiteColor2.r * progress2);
        int green2 = (int) (redColor2.g * (1.0 - progress2) + whiteColor2.g * progress2);
        int blue2 = (int) (redColor2.b * (1.0 - progress2) + whiteColor2.b * progress2);
        int alpha2 = redColor2.a;
        this.c2.set(red2, green2, blue2, alpha2);
        this.color2 = this.c2;
    }

    @EventHandler
    private void onRender(Render3DEvent event) {
        if (this.mc.crosshairTarget != null && this.mc.crosshairTarget instanceof BlockHitResult result) {
            if (this.mc.player.getMainHandStack().getItem() != Items.NETHERITE_PICKAXE) {
                if (!this.hideInside.get() || !result.isInsideBlock()) {
                    this.updateColor(event);
                    BlockPos bp = result.getBlockPos();
                    Direction side = result.getSide();
                    BlockState state = this.mc.world.getBlockState(bp);
                    VoxelShape shape = state.getOutlineShape(this.mc.world, bp);
                    if (!shape.isEmpty()) {
                        Box box = shape.getBoundingBox();
                        if (this.oneSide.get()) {
                            if (side == Direction.UP || side == Direction.DOWN) {
                                event.renderer
                                    .sideHorizontal(
                                        bp.getX() + box.minX,
                                        bp.getY() + (side == Direction.DOWN ? box.minY : box.maxY),
                                        bp.getZ() + box.minZ,
                                        bp.getX() + box.maxX,
                                        bp.getZ() + box.maxZ,
                                        this.color,
                                        this.color2,
                                        (ShapeMode) this.shapeMode.get()
                                    );
                            } else if (side != Direction.SOUTH && side != Direction.NORTH) {
                                double x = side == Direction.WEST ? box.minX : box.maxX;
                                event.renderer
                                    .sideVertical(
                                        bp.getX() + x,
                                        bp.getY() + box.minY,
                                        bp.getZ() + box.minZ,
                                        bp.getX() + x,
                                        bp.getY() + box.maxY,
                                        bp.getZ() + box.maxZ,
                                        this.color,
                                        this.color2,
                                        (ShapeMode) this.shapeMode.get()
                                    );
                            } else {
                                double z = side == Direction.NORTH ? box.minZ : box.maxZ;
                                event.renderer
                                    .sideVertical(
                                        bp.getX() + box.minX,
                                        bp.getY() + box.minY,
                                        bp.getZ() + z,
                                        bp.getX() + box.maxX,
                                        bp.getY() + box.maxY,
                                        bp.getZ() + z,
                                        this.color,
                                        this.color2,
                                        (ShapeMode) this.shapeMode.get()
                                    );
                            }
                        } else if (this.advanced.get()) {
                            if (this.shapeMode.get() == ShapeMode.Both || this.shapeMode.get() == ShapeMode.Lines) {
                                shape.forEachEdge(
                                    (minX, minY, minZ, maxX, maxY, maxZ) -> event.renderer
                                        .line(
                                            bp.getX() + minX, bp.getY() + minY, bp.getZ() + minZ, bp.getX() + maxX, bp.getY() + maxY, bp.getZ() + maxZ, (Color) this.lineColor.get()
                                        )
                                );
                            }

                            if (this.shapeMode.get() == ShapeMode.Both || this.shapeMode.get() == ShapeMode.Sides) {
                                for (Box b : shape.getBoundingBoxes()) {
                                    this.render(event, bp, b);
                                }
                            }
                        } else {
                            this.render(event, bp, box);
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    private void onRender2(Render3DEvent event) {
        if (this.mc.crosshairTarget != null && this.mc.crosshairTarget instanceof BlockHitResult result) {
            if (this.mc.player.getMainHandStack().getItem() == Items.NETHERITE_PICKAXE) {
                if (!this.hideInside.get() || !result.isInsideBlock()) {
                    this.updateColor(event);
                    BlockPos bp = result.getBlockPos();
                    Direction playerFacing = this.mc.player.getHorizontalFacing();

                    for (int dy = -1; dy <= 1; dy++) {
                        for (int dOther = -1; dOther <= 1; dOther++) {
                            int offsetX = playerFacing != Direction.EAST && playerFacing != Direction.WEST ? dOther : 0;
                            int offsetZ = playerFacing != Direction.NORTH && playerFacing != Direction.SOUTH ? dOther : 0;
                            BlockPos newPos = bp.add(offsetX, dy, offsetZ);
                            BlockState state = this.mc.world.getBlockState(newPos);
                            VoxelShape shape = state.getOutlineShape(this.mc.world, newPos);
                            if (!shape.isEmpty()) {
                                Box box = shape.getBoundingBox();
                                if (!this.advanced.get()) {
                                    this.render(event, newPos, box);
                                } else {
                                    if (this.shapeMode.get() == ShapeMode.Both || this.shapeMode.get() == ShapeMode.Lines) {
                                        shape.forEachEdge(
                                            (minX, minY, minZ, maxX, maxY, maxZ) -> event.renderer
                                                .line(
                                                    newPos.getX() + minX,
                                                    newPos.getY() + minY,
                                                    newPos.getZ() + minZ,
                                                    newPos.getX() + maxX,
                                                    newPos.getY() + maxY,
                                                    newPos.getZ() + maxZ,
                                                    (Color) this.lineColor.get()
                                                )
                                        );
                                    }

                                    if (this.shapeMode.get() == ShapeMode.Both || this.shapeMode.get() == ShapeMode.Sides) {
                                        for (Box b : shape.getBoundingBoxes()) {
                                            this.render(event, newPos, b);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private void render(Render3DEvent event, BlockPos bp, Box box) {
        event.renderer
            .box(
                bp.getX() + box.minX,
                bp.getY() + box.minY,
                bp.getZ() + box.minZ,
                bp.getX() + box.maxX,
                bp.getY() + box.maxY,
                bp.getZ() + box.maxZ,
                this.color,
                this.color2,
                (ShapeMode) this.shapeMode.get(),
                0
            );
    }
}
