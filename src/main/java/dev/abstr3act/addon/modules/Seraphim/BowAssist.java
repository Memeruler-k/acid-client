package dev.abstr3act.addon.modules.Seraphim;

import dev.abstr3act.addon.Compassion;
import dev.abstr3act.addon.module.SeraphimModule;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.settings.BoolSetting.Builder;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.entity.ProjectileEntitySimulator;
import meteordevelopment.meteorclient.utils.misc.Pool;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.TntMinecartEntity;
import net.minecraft.item.BowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.util.math.MathHelper;
import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.List;

public class BowAssist extends SeraphimModule {
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
    public final Setting<Integer> pullTime = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("Pull Timer"))
                .description("How many steps to simulate projectiles. Zero for no limit"))
                .defaultValue(8))
                .sliderMax(100)
                .min(0)
                .build()
        );
    public final Setting<Integer> simulationSteps = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("simulation-steps"))
                .description("How many steps to simulate projectiles. Zero for no limit"))
                .defaultValue(500))
                .sliderMax(5000)
                .build()
        );
    private final Setting<Boolean> accurate = this.sgGeneral
        .add(((Builder) ((Builder) ((Builder) new Builder().name("accurate")).description("Whether or not to calculate more accurate.")).defaultValue(false)).build());
    private final ProjectileEntitySimulator simulator = new ProjectileEntitySimulator();
    private final Pool<Vector3d> vec3s = new Pool(Vector3d::new);
    private final List<Path> paths = new ArrayList<>();

    public BowAssist() {
        super(Compassion.SERAPHIM, "BowAssist", "Predicts the trajectory of throwable items.");
    }

    private Path getEmptyPath() {
        for (Path path : this.paths) {
            if (path.points.isEmpty()) {
                return path;
            }
        }

        Path pathx = new Path();
        this.paths.add(pathx);
        return pathx;
    }

    private void calculateFiredPath(Entity entity, double tickDelta) {
        for (Path path : this.paths) {
            path.clear();
        }

        if (this.simulator.set(entity, this.accurate.get())) {
            this.getEmptyPath().setStart(entity, tickDelta).sample();
        }
    }

    private void calculatePath(PlayerEntity player, double tickDelta) {
        for (Path path : this.paths) {
            path.clear();
        }

        ItemStack itemStack = player.getMainHandStack();
        if (!(itemStack.getItem() instanceof BowItem)) {
            itemStack = player.getOffHandStack();
            if (!(itemStack.getItem() instanceof BowItem)) {
                return;
            }
        }

        if (this.simulator.set(player, itemStack, 0.0, this.accurate.get(), (float) tickDelta)) {
            this.getEmptyPath().sample();
        }
    }

    @EventHandler
    private void onRender(Render3DEvent event) {
        float tickDelta = this.mc.world.getTickManager().isFrozen() ? 1.0F : event.tickDelta;

        for (PlayerEntity player : this.mc.world.getPlayers()) {
            if (player == this.mc.player) {
                this.calculatePath(player, tickDelta);

                for (Path path : this.paths) {
                    if (this.mc.player.getActiveItem() == null) {
                        return;
                    }

                    int useTimeLeft = player.getItemUseTimeLeft();
                    int maxUseTime = this.mc.player.getActiveItem().getMaxUseTime(this.mc.player);
                    int pullTime = maxUseTime - useTimeLeft;
                    if (pullTime < this.pullTime.get()) {
                        return;
                    }

                    if (path.collidingEntity instanceof TntMinecartEntity) {
                        this.mc.options.useKey.setPressed(false);
                        this.mc.interactionManager.stopUsingItem(this.mc.player);
                    }
                }
            }
        }
    }

    private class Path {
        private final List<Vector3d> points = new ArrayList<>();
        public Vector3d lastPoint;
        private Entity collidingEntity;

        public void clear() {
            for (Vector3d point : this.points) {
                BowAssist.this.vec3s.free(point);
            }

            this.points.clear();
            this.collidingEntity = null;
            this.lastPoint = null;
        }

        public void sample() {
            this.addPoint();

            for (int i = 0; i < (BowAssist.this.simulationSteps.get() > 0 ? BowAssist.this.simulationSteps.get() : Integer.MAX_VALUE); i++) {
                HitResult result = BowAssist.this.simulator.tick();
                if (result != null) {
                    this.processHitResult(result);
                    break;
                }

                this.addPoint();
            }
        }

        public Entity getCollidingEntity() {
            return this.collidingEntity;
        }

        public Path setStart(Entity entity, double tickDelta) {
            this.lastPoint = new Vector3d(
                MathHelper.lerp(tickDelta, entity.lastRenderX, entity.getX()),
                MathHelper.lerp(tickDelta, entity.lastRenderY, entity.getY()),
                MathHelper.lerp(tickDelta, entity.lastRenderZ, entity.getZ())
            );
            return this;
        }

        private void addPoint() {
            this.points.add(((Vector3d) BowAssist.this.vec3s.get()).set(BowAssist.this.simulator.pos));
        }

        private void processHitResult(HitResult result) {
            if (result.getType() == Type.BLOCK) {
                this.points.add(Utils.set((Vector3d) BowAssist.this.vec3s.get(), result.getPos()));
            } else if (result.getType() == Type.ENTITY) {
                this.collidingEntity = ((EntityHitResult) result).getEntity();
                this.points.add(Utils.set((Vector3d) BowAssist.this.vec3s.get(), result.getPos()).add(0.0, this.collidingEntity.getHeight() / 2.0F, 0.0));
            }
        }
    }
}
