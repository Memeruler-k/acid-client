package dev.abstr3act.addon.modules.Abnormally;

import dev.abstr3act.addon.Compassion;
import dev.abstr3act.addon.module.AbnormallyModule;
import dev.abstr3act.addon.utils.TargetUtils;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent.Pre;
import meteordevelopment.meteorclient.settings.EnumSetting.Builder;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.utils.entity.SortPriority;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.RaycastContext.FluidHandling;
import net.minecraft.world.RaycastContext.ShapeType;

import java.util.Random;

public class Confuse extends AbnormallyModule {
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
    private final Setting<Mode> mode = this.sgGeneral
        .add(((Builder) ((Builder) ((Builder) new Builder().name("mode")).defaultValue(Mode.RandomTP)).description("Mode")).build());
    private final Setting<Integer> delay = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("delay"))
                .description("Delay"))
                .defaultValue(3))
                .min(0)
                .sliderMax(20)
                .build()
        );
    private final Setting<Integer> range = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("radius"))
                .description("Range to confuse opponents"))
                .defaultValue(6))
                .min(0)
                .sliderMax(10)
                .build()
        );
    private final Setting<SortPriority> priority = this.sgGeneral
        .add(((Builder) ((Builder) ((Builder) new Builder().name("priority")).description("Targetting priority")).defaultValue(SortPriority.LowestHealth)).build());
    private final Setting<Integer> circleSpeed = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("circle-speed"))
                .description("Circle mode speed"))
                .defaultValue(10))
                .min(1)
                .sliderMax(180)
                .build()
        );
    private final Setting<Boolean> moveThroughBlocks = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("move-through-blocks"))
                .defaultValue(false))
                .build()
        );
    private final Setting<Boolean> budgetGraphics = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("budget-graphics"))
                .defaultValue(false))
                .build()
        );
    private final Setting<SettingColor> circleColor = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                .name("circle-color"))
                .description("Color for circle rendering"))
                .defaultValue(new SettingColor(0, 255, 0))
                .visible(this.budgetGraphics::get))
                .build()
        );
    int delayWaited = 0;
    double circleProgress = 0.0;
    double addition = 0.0;
    Entity target = null;

    public Confuse() {
        super(Compassion.ABNORMALLY, "confuse", "Makes your enemies shit themselves");
    }

    public void onActivate() {
        this.delayWaited = 0;
        this.circleProgress = 0.0;
        this.addition = 0.0;
        this.target = null;
    }

    @EventHandler
    private void onTick(Pre event) {
        this.delayWaited++;
        if (this.delayWaited >= this.delay.get()) {
            this.delayWaited = 0;
            this.target = TargetUtils.getPlayerTarget((this.range.get()).intValue(), (SortPriority) this.priority.get());
            if (this.target != null) {
                Vec3d entityPos = this.target.getPos();
                Vec3d playerPos = this.mc.player.getPos();
                Random r = new Random();
                int halfRange = this.range.get() / 2;
                switch ((Mode) this.mode.get()) {
                    case RandomTP:
                        double x = r.nextDouble() * (this.range.get()).intValue() - halfRange;
                        double y = 0.0;
                        double z = r.nextDouble() * (this.range.get()).intValue() - halfRange;
                        Vec3d addend = new Vec3d(x, y, z);
                        Vec3d goal = entityPos.add(addend);
                        if (this.mc.world.getBlockState(BlockPos.ofFloored(goal.x, goal.y, goal.z)).getBlock() != Blocks.AIR) {
                            goal = new Vec3d(x, playerPos.y, z);
                        }

                        if (this.mc.world.getBlockState(BlockPos.ofFloored(goal.x, goal.y, goal.z)).getBlock() == Blocks.AIR) {
                            BlockHitResult hitx = this.mc
                                .world
                                .raycast(new RaycastContext(this.mc.player.getPos(), goal, ShapeType.COLLIDER, FluidHandling.ANY, this.mc.player));
                            if (!this.moveThroughBlocks.get() && hitx.isInsideBlock()) {
                                this.delayWaited = this.delay.get() - 1;
                            } else {
                                this.mc.player.updatePosition(goal.x, goal.y, goal.z);
                            }
                        } else {
                            this.delayWaited = this.delay.get() - 1;
                        }
                        break;
                    case Switch:
                        Vec3d diff = entityPos.subtract(playerPos);
                        Vec3d diff1 = new Vec3d(
                            MathHelper.clamp(diff.x, -halfRange, halfRange), MathHelper.clamp(diff.y, -halfRange, halfRange), MathHelper.clamp(diff.z, -halfRange, halfRange)
                        );
                        Vec3d goal2 = entityPos.add(diff1);
                        BlockHitResult hit = this.mc
                            .world
                            .raycast(new RaycastContext(this.mc.player.getPos(), goal2, ShapeType.COLLIDER, FluidHandling.ANY, this.mc.player));
                        if (!this.moveThroughBlocks.get() && hit.isInsideBlock()) {
                            this.delayWaited = this.delay.get() - 1;
                        } else {
                            this.mc.player.updatePosition(goal2.x, goal2.y, goal2.z);
                        }
                        break;
                    case Circle:
                        this.delay.set(0);
                        this.circleProgress = this.circleProgress + (this.circleSpeed.get()).intValue();
                        if (this.circleProgress > 360.0) {
                            this.circleProgress -= 360.0;
                        }

                        double rad = Math.toRadians(this.circleProgress);
                        double sin = Math.sin(rad) * 3.0;
                        double cos = Math.cos(rad) * 3.0;
                        Vec3d current = new Vec3d(entityPos.x + sin, playerPos.y, entityPos.z + cos);
                        BlockHitResult hit2 = this.mc
                            .world
                            .raycast(new RaycastContext(this.mc.player.getPos(), current, ShapeType.COLLIDER, FluidHandling.ANY, this.mc.player));
                        if (this.moveThroughBlocks.get() || !hit2.isInsideBlock()) {
                            this.mc.player.updatePosition(current.x, current.y, current.z);
                        }
                }
            }
        }
    }

    @EventHandler
    private void onRender(Render3DEvent event) {
        if (this.target != null) {
            boolean flag = this.budgetGraphics.get();
            Vec3d last = null;
            this.addition += flag ? 0.0 : 1.0;
            if (this.addition > 360.0) {
                this.addition = 0.0;
            }

            for (int i = 0; i < 360; i += flag ? 7 : 1) {
                Color c1;
                if (flag) {
                    c1 = (Color) this.circleColor.get();
                } else {
                    double rot = 765.0 * ((i + this.addition) % 360.0 / 360.0);
                    int seed = (int) Math.floor(rot / 255.0);
                    double current = rot % 255.0;
                    double red = seed == 0 ? current : (seed == 1 ? Math.abs(current - 255.0) : 0.0);
                    double green = seed == 1 ? current : (seed == 2 ? Math.abs(current - 255.0) : 0.0);
                    double blue = seed == 2 ? current : (seed == 0 ? Math.abs(current - 255.0) : 0.0);
                    c1 = new Color((int) red, (int) green, (int) blue);
                }

                Vec3d tp = this.target.getPos();
                double rad = Math.toRadians(i);
                double sin = Math.sin(rad) * 3.0;
                double cos = Math.cos(rad) * 3.0;
                Vec3d c = new Vec3d(tp.x + sin, tp.y + this.target.getHeight() / 2.0F, tp.z + cos);
                if (last != null) {
                    event.renderer.line(last.x, last.y, last.z, c.x, c.y, c.z, c1);
                }

                last = c;
            }
        }
    }

    public static enum Mode {
        RandomTP,
        Switch,
        Circle;
    }
}
