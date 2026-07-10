package dev.abstr3act.addon.modules.Seraphim;

import dev.abstr3act.addon.module.SeraphimModule;
import meteordevelopment.meteorclient.events.render.Render2DEvent;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.renderer.text.TextRenderer;
import meteordevelopment.meteorclient.settings.DoubleSetting.Builder;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.utils.entity.DamageUtils;
import meteordevelopment.meteorclient.utils.render.NametagUtils;
import meteordevelopment.meteorclient.utils.render.RenderUtils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.BedBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.RespawnAnchorBlock;
import net.minecraft.block.enums.BedPart;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.List;

public class ExplosiveESP extends SeraphimModule {
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

    public ExplosiveESP() {
        super(Categories.Render, "ExplosiveESP", "Renders explosive damages.");
    }

    public void onActivate() {
    }

    public void onDeactivate() {
    }

    public List<BlockPos> anchors(World world, BlockPos center) {
        List<BlockPos> chargedAnchors = new ArrayList<>();
        if (world instanceof ClientWorld) {
            Box searchBox = new Box(center.add(-15, -15, -15).toCenterPos(), center.add(15, 15, 15).toCenterPos());

            for (BlockPos pos : BlockPos.iterate(
                (int) searchBox.minX, (int) searchBox.minY, (int) searchBox.minZ, (int) searchBox.maxX, (int) searchBox.maxY, (int) searchBox.maxZ
            )) {
                if (world.getBlockState(pos).isOf(Blocks.RESPAWN_ANCHOR)) {
                    BlockState Block = world.getBlockState(pos);
                    if (Block.getBlock() instanceof RespawnAnchorBlock) {
                        int charges = Block.get(Properties.CHARGES);
                        if (charges > 0) {
                            chargedAnchors.add(pos.add(this.x.get(), this.y.get(), this.z.get()));
                        }
                    }
                }
            }
        }

        return chargedAnchors;
    }

    public List<BlockPos> crystals(double maxDistance) {
        List<BlockPos> crystalPositions = new ArrayList<>();
        if (this.mc != null && this.mc.world != null && this.mc.player != null) {
            Vec3d playerPos = this.mc.player.getPos();

            for (Entity entity : this.mc.world.getEntities()) {
                if (entity instanceof EndCrystalEntity) {
                    Vec3d crystalPos = entity.getPos();
                    if (crystalPos.squaredDistanceTo(playerPos) <= maxDistance * maxDistance) {
                        crystalPositions.add(BlockPos.ofFloored(crystalPos));
                    }
                }
            }

            return crystalPositions;
        } else {
            return crystalPositions;
        }
    }

    public List<BlockPos> beds(World world, BlockPos center) {
        List<BlockPos> beds = new ArrayList<>();
        if (world instanceof ClientWorld) {
            Box searchBox = new Box(center.add(-15, -15, -15).toCenterPos(), center.add(15, 15, 15).toCenterPos());

            for (BlockPos pos : BlockPos.iterate(
                (int) searchBox.minX, (int) searchBox.minY, (int) searchBox.minZ, (int) searchBox.maxX, (int) searchBox.maxY, (int) searchBox.maxZ
            )) {
                if (world.getBlockState(pos).getBlock() instanceof BedBlock bed && world.getBlockState(pos).get(Properties.BED_PART) == BedPart.HEAD) {
                    beds.add(pos.add(this.x.get(), this.y.get(), this.z.get()));
                }
            }
        }

        return beds;
    }

    @EventHandler
    private void onRender2D(Render2DEvent event) {
        for (BlockPos pos : this.anchors(this.mc.world, this.mc.player.getBlockPos())) {
            float renderDamage = DamageUtils.anchorDamage(this.mc.player, pos.toCenterPos());
            this.vec3.set(pos.getX() + 0.5, pos.getY() + 0.5 + this.y2.get(), pos.getZ() + 0.5);
            if (NametagUtils.to2D(this.vec3, this.damageTextScale.get())) {
                NametagUtils.begin(this.vec3);
                TextRenderer.get().begin(1.0, false, true);
                String text = "[A] " + String.format("%.1f", renderDamage);
                double w = TextRenderer.get().getWidth(text) / 2.0;
                float health = this.mc.player.getHealth() + this.mc.player.getAbsorptionAmount();
                TextRenderer.get().render(text, -w, 0.0, renderDamage >= health ? (Color) this.damageColor_d.get() : (Color) this.damageColor.get(), true);
                TextRenderer.get().end();
                NametagUtils.end();
            }
        }

        for (BlockPos posx : this.crystals(15.0)) {
            float renderDamage = DamageUtils.crystalDamage(this.mc.player, posx.toCenterPos());
            this.vec3.set(posx.getX() + 0.5, posx.getY() + 0.5 + this.y2.get() - 1.0, posx.getZ() + 0.5);
            if (NametagUtils.to2D(this.vec3, this.damageTextScale.get())) {
                NametagUtils.begin(this.vec3);
                TextRenderer.get().begin(1.0, false, true);
                String text = String.format("[C] %.1f", renderDamage);
                double w = TextRenderer.get().getWidth(text) / 2.0;
                float health = this.mc.player.getHealth() + this.mc.player.getAbsorptionAmount();
                TextRenderer.get().render(text, -w, 0.0, renderDamage >= health ? (Color) this.damageColor_d.get() : (Color) this.damageColor.get(), true);
                TextRenderer.get().end();
                NametagUtils.end();
            }
        }

        for (BlockPos posxx : this.beds(this.mc.world, this.mc.player.getBlockPos())) {
            float renderDamage = DamageUtils.bedDamage(this.mc.player, posxx.toCenterPos());
            this.vec3.set(posxx.getX() + 0.5, posxx.getY() + 0.5 + this.y2.get(), posxx.getZ() + 0.5);
            if (NametagUtils.to2D(this.vec3, this.damageTextScale.get())) {
                NametagUtils.begin(this.vec3);
                TextRenderer.get().begin(1.0, false, true);
                String text = "[B] " + String.format("%.1f", renderDamage);
                double w = TextRenderer.get().getWidth(text) / 2.0;
                float health = this.mc.player.getHealth() + this.mc.player.getAbsorptionAmount();
                TextRenderer.get().render(text, -w, 0.0, renderDamage >= health ? (Color) this.damageColor_d.get() : (Color) this.damageColor.get(), true);
                TextRenderer.get().end();
                NametagUtils.end();
            }
        }
    }

    @EventHandler
    private void onRender(Render3DEvent event) {
        for (BlockPos pos : this.anchors(this.mc.world, this.mc.player.getBlockPos())) {
            RenderUtils.renderTickingBlock(
                pos,
                (Color) this.sideColor.get(),
                (Color) this.lineColor.get(),
                (ShapeMode) this.shapeMode.get(),
                0,
                this.duration.get(),
                this.fade.get(),
                this.shrink.get()
            );
        }

        for (BlockPos pos : this.crystals(15.0)) {
            RenderUtils.renderTickingBlock(
                pos.add(0, -1, 0),
                (Color) this.sideColor.get(),
                (Color) this.lineColor.get(),
                (ShapeMode) this.shapeMode.get(),
                0,
                this.duration.get(),
                this.fade.get(),
                this.shrink.get()
            );
        }

        for (BlockPos pos : this.beds(this.mc.world, this.mc.player.getBlockPos())) {
            RenderUtils.renderTickingBlock(
                pos,
                (Color) this.sideColor.get(),
                (Color) this.lineColor.get(),
                (ShapeMode) this.shapeMode.get(),
                0,
                this.duration.get(),
                this.fade.get(),
                this.shrink.get()
            );
        }
    }
}
