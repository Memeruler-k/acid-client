package dev.abstr3act.addon.modules.Amrita;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.abstr3act.addon.Compassion;
import dev.abstr3act.addon.events.EventUpdate;
import dev.abstr3act.addon.font.FontRenderers;
import dev.abstr3act.addon.module.AmritaModule;
import dev.abstr3act.addon.utils.math.MathUtility;
import dev.abstr3act.addon.utils.render.CaptureMark;
import dev.abstr3act.addon.utils.render.Render2DEngine;
import dev.abstr3act.addon.utils.render.shaders.AnimationUtility;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.settings.EnumSetting.Builder;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.AirBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RotationAxis;

import java.util.HashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class HitParticles extends AmritaModule {
    private final SettingGroup sgGeneral = this.settings.createGroup("General");
    private final Setting<Mode> mode = this.sgGeneral
        .add(((Builder) ((Builder) ((Builder) new Builder().name("mode")).description("Mode setting.")).defaultValue(Mode.Stars)).build());
    private final Setting<Double> starsScale = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("scale"))
                .description("Scale setting."))
                .defaultValue(3.0)
                .min(1.0)
                .max(10.0)
                .visible(() -> this.mode.get() != Mode.Orbiz))
                .build()
        );
    private final Setting<SettingColor> colorH = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                .name("heal-color"))
                .description("Heal color."))
                .defaultValue(new Color(3142544))
                .visible(() -> this.mode.get() == Mode.Text))
                .build()
        );
    private final Setting<SettingColor> colorD = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                .name("damage-color"))
                .description("Damage color."))
                .defaultValue(new Color(15811379))
                .visible(() -> this.mode.get() == Mode.Text))
                .build()
        );
    private final Setting<Physics> physics = this.sgGeneral
        .add(((Builder) ((Builder) ((Builder) new Builder().name("physics")).description("Physics mode.")).defaultValue(Physics.Fall)).build());
    private final Setting<SettingColor> colorrr = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                .name("color"))
                .description("Color setting."))
                .defaultValue(new Color(-2013200640))
                .build()
        );
    private final Setting<Boolean> selfp = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("self"))
                .description("Self setting."))
                .defaultValue(false))
                .build()
        );
    private final Setting<Integer> amount = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("amount"))
                .description("Amount setting."))
                .defaultValue(2))
                .min(1)
                .max(5)
                .build()
        );
    private final Setting<Integer> lifeTime = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("lifetime"))
                .description("Lifetime setting."))
                .defaultValue(2))
                .min(1)
                .max(10)
                .build()
        );
    private final Setting<Integer> speed = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("speed"))
                .description("Speed setting."))
                .defaultValue(2))
                .min(1)
                .max(20)
                .build()
        );
    private final Setting<ColorMode> colorMode = this.sgGeneral
        .add(((Builder) ((Builder) ((Builder) new Builder().name("color-mode")).description("Color mode setting.")).defaultValue(ColorMode.Sync)).build());
    private final HashMap<Integer, Float> healthMap = new HashMap<>();
    private final CopyOnWriteArrayList<Particle> particles = new CopyOnWriteArrayList<>();

    public HitParticles() {
        super(Compassion.AMRITA, "HitParticles", "");
    }

    @EventHandler
    public void onUpdate(EventUpdate event) {
        this.particles.removeIf(Particle::update);
        if (((Mode) this.mode.get()).equals(Mode.Text)) {
            for (Entity entity : this.mc.world.getEntities()) {
                if (entity != null && !(this.mc.player.squaredDistanceTo(entity) > 256.0) && entity.isAlive() && entity instanceof LivingEntity lent) {
                    java.awt.Color c = this.colorMode.get() == ColorMode.Sync
                        ? CaptureMark.getColor((int) MathUtility.random(1.0F, 228.0F))
                        : new java.awt.Color(((SettingColor) this.colorrr.get()).getPacked(), true);
                    float health = lent.getHealth() + lent.getAbsorptionAmount();
                    float lastHealth = this.healthMap.getOrDefault(entity.getId(), health);
                    this.healthMap.put(entity.getId(), health);
                    if (lastHealth != health) {
                        this.particles
                            .add(
                                new Particle(
                                    (float) lent.getX(),
                                    MathUtility.random((float) (lent.getY() + lent.getHeight()), (float) lent.getY()),
                                    (float) lent.getZ(),
                                    c,
                                    MathUtility.random(0.0F, 180.0F),
                                    MathUtility.random(10.0F, 60.0F),
                                    health - lastHealth
                                )
                            );
                    }
                }
            }
        } else {
            for (PlayerEntity player : this.mc.world.getPlayers()) {
                if ((this.selfp.get() || player != this.mc.player) && player.hurtTime > 0) {
                    java.awt.Color c = this.colorMode.get() == ColorMode.Sync
                        ? CaptureMark.getColor((int) MathUtility.random(1.0F, 228.0F))
                        : new java.awt.Color(((SettingColor) this.colorrr.get()).getPacked(), true);

                    for (int i = 0; i < this.amount.get(); i++) {
                        this.particles
                            .add(
                                new Particle(
                                    (float) player.getX(),
                                    MathUtility.random((float) (player.getY() + player.getHeight()), (float) player.getY()),
                                    (float) player.getZ(),
                                    c,
                                    MathUtility.random(0.0F, 180.0F),
                                    MathUtility.random(10.0F, 60.0F),
                                    0.0F
                                )
                            );
                    }
                }
            }
        }
    }

    @EventHandler
    public void onRender3D(Render3DEvent stack) {
        RenderSystem.disableDepthTest();
        if (this.mc.player != null && this.mc.world != null) {
            for (Particle particle : this.particles) {
                particle.render(stack);
            }
        }

        RenderSystem.enableDepthTest();
    }

    public static enum ColorMode {
        Custom,
        Sync;
    }

    public static enum Mode {
        Orbiz,
        Stars,
        Hearts,
        Bloom,
        Text,
        Arcaea;
    }

    public static enum Physics {
        Fall,
        Fly;
    }

    public class Particle {
        float x;
        float y;
        float z;
        float px;
        float py;
        float pz;
        float motionX;
        float motionY;
        float motionZ;
        float rotationAngle;
        float rotationSpeed;
        float health;
        long time;
        java.awt.Color color;

        public Particle(float x, float y, float z, java.awt.Color color, float rotationAngle, float rotationSpeed, float health) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.px = x;
            this.py = y;
            this.pz = z;
            this.motionX = MathUtility.random(
                -(HitParticles.this.speed.get()).intValue() / 50.0F, (HitParticles.this.speed.get()).intValue() / 50.0F
            );
            this.motionY = MathUtility.random(
                -(HitParticles.this.speed.get()).intValue() / 50.0F, (HitParticles.this.speed.get()).intValue() / 50.0F
            );
            this.motionZ = MathUtility.random(
                -(HitParticles.this.speed.get()).intValue() / 50.0F, (HitParticles.this.speed.get()).intValue() / 50.0F
            );
            this.time = System.currentTimeMillis();
            this.color = color;
            this.rotationAngle = rotationAngle;
            this.rotationSpeed = rotationSpeed;
            this.health = health;
        }

        public long getTime() {
            return this.time;
        }

        public boolean update() {
            double sp = Math.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);
            this.px = this.x;
            this.py = this.y;
            this.pz = this.z;
            this.x = this.x + this.motionX;
            this.y = this.y + this.motionY;
            this.z = this.z + this.motionZ;
            if (this.posBlock(this.x, this.y - HitParticles.this.starsScale.get() / 10.0, this.z)) {
                this.motionY = -this.motionY / 1.1F;
                this.motionX /= 1.1F;
                this.motionZ /= 1.1F;
            } else if (this.posBlock(this.x - sp, this.y, this.z - sp)
                || this.posBlock(this.x + sp, this.y, this.z + sp)
                || this.posBlock(this.x + sp, this.y, this.z - sp)
                || this.posBlock(this.x - sp, this.y, this.z + sp)
                || this.posBlock(this.x + sp, this.y, this.z)
                || this.posBlock(this.x - sp, this.y, this.z)
                || this.posBlock(this.x, this.y, this.z + sp)
                || this.posBlock(this.x, this.y, this.z - sp)) {
                this.motionX = -this.motionX;
                this.motionZ = -this.motionZ;
            }

            if (HitParticles.this.physics.get() == Physics.Fall) {
                this.motionY -= 0.035F;
            }

            this.motionX /= 1.005F;
            this.motionZ /= 1.005F;
            this.motionY /= 1.005F;
            return System.currentTimeMillis() - this.getTime() > HitParticles.this.lifeTime.get() * 1000;
        }

        public void render(Render3DEvent matrixStack) {
            float size = (HitParticles.this.starsScale.get()).floatValue();
            float scale = ((Mode) HitParticles.this.mode.get()).equals(Mode.Text) ? 0.025F * size : 0.07F;
            double posX = Render2DEngine.interpolate(this.px, this.x, HitParticles.this.mc.getRenderTickCounter().getTickDelta(true))
                - HitParticles.this.mc.getEntityRenderDispatcher().camera.getPos().getX();
            double posY = Render2DEngine.interpolate(this.py, this.y, HitParticles.this.mc.getRenderTickCounter().getTickDelta(true))
                + 0.1
                - HitParticles.this.mc.getEntityRenderDispatcher().camera.getPos().getY();
            double posZ = Render2DEngine.interpolate(this.pz, this.z, HitParticles.this.mc.getRenderTickCounter().getTickDelta(true))
                - HitParticles.this.mc.getEntityRenderDispatcher().camera.getPos().getZ();
            matrixStack.matrices.push();
            matrixStack.matrices.translate(posX, posY, posZ);
            matrixStack.matrices.scale(scale, scale, scale);
            matrixStack.matrices.translate(size / 2.0F, size / 2.0F, size / 2.0F);
            matrixStack.matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-HitParticles.this.mc.gameRenderer.getCamera().getYaw()));
            matrixStack.matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(HitParticles.this.mc.gameRenderer.getCamera().getPitch()));
            if (((Mode) HitParticles.this.mode.get()).equals(Mode.Text)) {
                matrixStack.matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(180.0F));
            } else {
                matrixStack.matrices
                    .multiply(RotationAxis.POSITIVE_Z.rotationDegrees(this.rotationAngle = this.rotationAngle + AnimationUtility.deltaTime() * this.rotationSpeed));
            }

            matrixStack.matrices.translate(-size / 2.0F, -size / 2.0F, -size / 2.0F);
            switch ((Mode) HitParticles.this.mode.get()) {
                case Orbiz:
                    Render2DEngine.drawOrbiz(matrixStack.matrices, 0.0F, 0.3, this.color);
                    Render2DEngine.drawOrbiz(matrixStack.matrices, -0.1F, 0.5, this.color);
                    Render2DEngine.drawOrbiz(matrixStack.matrices, -0.2F, 0.7, this.color);
                    break;
                case Stars:
                    Render2DEngine.drawStar(matrixStack.matrices, this.color, size);
                    break;
                case Hearts:
                    Render2DEngine.drawHeart(matrixStack.matrices, this.color, size);
                    break;
                case Bloom:
                    Render2DEngine.drawBloom(matrixStack.matrices, this.color, size);
                    break;
                case Text:
                    FontRenderers.sf_medium
                        .drawCenteredString(
                            matrixStack.matrices,
                            MathUtility.round2(this.health) + " ",
                            0.0,
                            0.0,
                            this.health > 0.0F
                                ? new java.awt.Color(((SettingColor) HitParticles.this.colorH.get()).getPacked(), true)
                                : new java.awt.Color(((SettingColor) HitParticles.this.colorD.get()).getPacked(), true)
                        );
                    break;
                case Arcaea:
                    Render2DEngine.drawPure(matrixStack.matrices, this.color, size);
            }

            matrixStack.matrices.scale(0.8F, 0.8F, 0.8F);
            matrixStack.matrices.pop();
        }

        private boolean posBlock(double x, double y, double z) {
            Block b = HitParticles.this.mc.world.getBlockState(BlockPos.ofFloored(x, y, z)).getBlock();
            return !(b instanceof AirBlock) && b != Blocks.WATER && b != Blocks.LAVA;
        }
    }
}
