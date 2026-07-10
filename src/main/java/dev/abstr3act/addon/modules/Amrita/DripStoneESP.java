package dev.abstr3act.addon.modules.Amrita;

import dev.abstr3act.addon.Compassion;
import dev.abstr3act.addon.module.AmritaModule;
import dev.abstr3act.addon.utils.render.Render2DEngine;
import meteordevelopment.meteorclient.events.render.Render2DEvent;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.renderer.Renderer2D;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.renderer.text.TextRenderer;
import meteordevelopment.meteorclient.settings.DoubleSetting.Builder;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.utils.render.NametagUtils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.PointedDripstoneBlock;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.Mutable;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.World;
import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.List;

public class DripStoneESP extends AmritaModule {
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
    private final Setting<Double> damageTextScale = this.sgGeneral
        .add(((Builder) ((Builder) new Builder().name("damageTextScale")).description(".")).defaultValue(1.0).min(0.0).build());
    private final Setting<Integer> duration = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("duration"))
                .description("."))
                .defaultValue(10))
                .sliderRange(0, 100)
                .build()
        );
    private final Setting<Boolean> fade = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("fade"))
                .description("."))
                .defaultValue(true))
                .build()
        );
    private final Setting<Boolean> shrink = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("shrink"))
                .description("."))
                .defaultValue(true))
                .build()
        );
    private final Setting<Integer> x = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("Y"))
                .description("."))
                .defaultValue(0))
                .sliderRange(-1000, 1000)
                .build()
        );
    private final Setting<Integer> y = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("X"))
                .description("."))
                .defaultValue(0))
                .sliderRange(-1000, 1000)
                .build()
        );
    private final Setting<Integer> z = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("Z"))
                .description("."))
                .defaultValue(0))
                .sliderRange(-1000, 1000)
                .build()
        );
    private final Setting<Double> y2 = this.sgGeneral
        .add(((Builder) ((Builder) new Builder().name("Y2")).description(".")).defaultValue(0.0).sliderRange(-10.0, 10.0).build());
    private final Setting<Double> x2 = this.sgGeneral
        .add(((Builder) ((Builder) new Builder().name("X2")).description(".")).defaultValue(0.0).sliderRange(-10.0, 10.0).build());
    private final Setting<SettingColor> bgColor = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                .name("bgColor"))
                .description("."))
                .defaultValue(new Color(255, 255, 255, 255))
                .build()
        );
    private final Setting<SettingColor> damageColor = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                .name("damageColor"))
                .description("."))
                .defaultValue(new Color(255, 255, 255, 255))
                .build()
        );
    private final Setting<SettingColor> damageColor_d = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                .name("damageColor_d"))
                .description("."))
                .defaultValue(new Color(255, 255, 255, 255))
                .build()
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
                .name("sideColor"))
                .description("."))
                .defaultValue(new Color(255, 255, 255, 255))
                .build()
        );
    private final Setting<SettingColor> lineColor = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                .name("lineColor"))
                .description("."))
                .defaultValue(new Color(255, 255, 255, 255))
                .build()
        );
    private final Vector3d vec3 = new Vector3d();

    public DripStoneESP() {
        super(Compassion.AMRITA, "DripStoneESP", "Renders explosive damages.");
    }

    public static float calculateDamage(int fallDistance, boolean hasHelmet) {
        if (fallDistance <= 2) {
            return 0.0F;
        } else {
            float damage = Math.min((fallDistance - 2) * 3, 40);
            if (hasHelmet) {
                damage *= 0.75F;
            }

            return damage;
        }
    }

    public static float calculateDamage(float fallDistance, boolean hasHelmet) {
        if (fallDistance <= 2.0F) {
            return 0.0F;
        } else {
            float damage = Math.min((fallDistance - 2.0F) * 3.0F, 40.0F);
            if (hasHelmet) {
                damage *= 0.75F;
            }

            return damage;
        }
    }

    public static int countAirBelow(World world, BlockPos pos) {
        int airCount = 0;
        Mutable mutablePos = pos.mutableCopy().move(Direction.DOWN);

        while (mutablePos.getY() > world.getBottomY()) {
            BlockState state = world.getBlockState(mutablePos);
            if (!state.isAir() && !isNonSolidBlock(state)) {
                break;
            }

            airCount++;
            mutablePos.move(Direction.DOWN);
        }

        return airCount;
    }

    private static boolean isNonSolidBlock(BlockState state) {
        return !state.isSolid() || state.isOf(Blocks.POINTED_DRIPSTONE);
    }

    public static float getDamage(World world, BlockPos pos) {
        int fallDistance = 0;
        Mutable mutablePos = pos.mutableCopy();

        while (mutablePos.getY() > world.getBottomY() && world.getBlockState(mutablePos).isOf(Blocks.POINTED_DRIPSTONE)) {
            fallDistance++;
            mutablePos.move(Direction.DOWN);
        }

        return fallDistance > 2 ? Math.min((fallDistance - 2) * 3, 40) : 0.0F;
    }

    public void onActivate() {
    }

    public void onDeactivate() {
    }

    public List<BlockPos> getDripStonePositions(World world, BlockPos center) {
        List<BlockPos> chargedAnchors = new ArrayList<>();
        if (world instanceof ClientWorld) {
            Box searchBox = new Box(center.add(-15, -15, -15).toCenterPos(), center.add(15, 15, 15).toCenterPos());

            for (BlockPos pos : BlockPos.iterate(
                (int) searchBox.minX, (int) searchBox.minY, (int) searchBox.minZ, (int) searchBox.maxX, (int) searchBox.maxY, (int) searchBox.maxZ
            )) {
                if (world.getBlockState(pos).isOf(Blocks.POINTED_DRIPSTONE)) {
                    BlockState Block = world.getBlockState(pos);
                    if (Block.getBlock() instanceof PointedDripstoneBlock) {
                        chargedAnchors.add(pos.add(this.x.get(), this.y.get(), this.z.get()));
                    }
                }
            }
        }

        return chargedAnchors;
    }

    @EventHandler
    private void onRender2D(Render2DEvent event) {
        for (BlockPos pos : this.getDripStonePositions(this.mc.world, this.mc.player.getBlockPos())) {
            this.vec3.set(pos.getX() + 0.5, pos.getY() + 0.5 + this.y2.get(), pos.getZ() + 0.5);
            if (NametagUtils.to2D(this.vec3, this.damageTextScale.get())) {
                NametagUtils.begin(this.vec3);
                TextRenderer.get().begin(1.0, false, true);
                int fallDistance = countAirBelow(this.mc.world, pos);
                float damage = calculateDamage(fallDistance, false);
                String text = String.format("%.1f", damage) + " | " + countAirBelow(this.mc.world, pos) + "B";
                double w = TextRenderer.get().getWidth(text) / 2.0;
                float health = this.mc.player.getHealth() + this.mc.player.getAbsorptionAmount();
                this.drawBg(-w + this.x2.get(), 0.0, TextRenderer.get().getWidth(text), TextRenderer.get().getHeight(true));
                TextRenderer.get()
                    .render(text, -w + this.x2.get(), 0.0, damage >= health ? (Color) this.damageColor_d.get() : (Color) this.damageColor.get(), true);
                TextRenderer.get().end();
                NametagUtils.end();
            }
        }

        for (Entity entity : this.mc.world.getEntities()) {
            if (entity instanceof FallingBlockEntity && ((FallingBlockEntity) entity).getBlockState().getBlock() instanceof PointedDripstoneBlock) {
                double tPosX = Render2DEngine.interpolate(entity.prevX, entity.getX(), this.mc.getRenderTickCounter().getTickDelta(true));
                double tPosY = Render2DEngine.interpolate(entity.prevY, entity.getY(), this.mc.getRenderTickCounter().getTickDelta(true));
                double tPosZ = Render2DEngine.interpolate(entity.prevZ, entity.getZ(), this.mc.getRenderTickCounter().getTickDelta(true));
                this.vec3.set(tPosX, tPosY + this.y2.get(), tPosZ);
                if (NametagUtils.to2D(this.vec3, this.damageTextScale.get())) {
                    NametagUtils.begin(this.vec3);
                    TextRenderer.get().begin(1.0, false, true);
                    int fallDistance = countAirBelow(this.mc.world, new BlockPos((int) tPosX, (int) tPosY, (int) tPosZ));
                    float damage = calculateDamage(entity.fallDistance, false);
                    String text = String.format("%.1f", damage) + " | " + fallDistance + "B";
                    double w = TextRenderer.get().getWidth(text) / 2.0;
                    float health = this.mc.player.getHealth() + this.mc.player.getAbsorptionAmount();
                    this.drawBg(-w + this.x2.get(), 0.0, TextRenderer.get().getWidth(text), TextRenderer.get().getHeight(true));
                    TextRenderer.get()
                        .render(text, -w + this.x2.get(), 0.0, damage >= health ? (Color) this.damageColor_d.get() : (Color) this.damageColor.get(), true);
                    TextRenderer.get().end();
                    NametagUtils.end();
                }
            }
        }
    }

    private void drawBg(double x, double y, double width, double height) {
        Renderer2D.COLOR.begin();
        Renderer2D.COLOR.quad(x - 1.0, y - 1.0, width + 2.0, height + 2.0, (Color) this.bgColor.get());
        Renderer2D.COLOR.render(null);
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
                (Color) this.sideColor.get(),
                (Color) this.lineColor.get(),
                (ShapeMode) this.shapeMode.get(),
                0
            );
    }

    @EventHandler
    private void onRender(Render3DEvent event) {
        for (BlockPos pos : this.getDripStonePositions(this.mc.world, this.mc.player.getBlockPos())) {
            BlockState state = this.mc.world.getBlockState(pos);
            VoxelShape shape = state.getOutlineShape(this.mc.world, pos);
            if (shape.isEmpty()) {
                return;
            }

            for (Box b : shape.getBoundingBoxes()) {
                this.render(event, pos, b);
            }
        }
    }
}
