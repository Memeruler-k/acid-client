package dev.abstr3act.addon.modules.Seraphim;

import dev.abstr3act.addon.Compassion;
import dev.abstr3act.addon.events.EventPlayerUpdate;
import dev.abstr3act.addon.module.CompassionModule;
import dev.abstr3act.addon.utils.seraphim.PlayerUtils;
import meteordevelopment.meteorclient.events.entity.player.AttackEntityEvent;
import meteordevelopment.meteorclient.settings.BoolSetting.Builder;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Blocks;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.SwordItem;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.RaycastContext.FluidHandling;
import net.minecraft.world.RaycastContext.ShapeType;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

public final class AutoCrystal extends CompassionModule {
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
    private final SettingGroup sgAttack = this.settings.createGroup("Attack");
    private final Setting<Boolean> swingFix = this.sgGeneral
        .add(((Builder) ((Builder) ((Builder) new Builder().name("SwingFix")).description(".")).defaultValue(true)).build());
    private final Setting<Boolean> onCrystal = this.sgGeneral
        .add(((Builder) ((Builder) ((Builder) new Builder().name("onCrystal")).description(".")).defaultValue(true)).build());
    private final Setting<Boolean> onTotem = this.sgGeneral
        .add(((Builder) ((Builder) ((Builder) new Builder().name("onTotem")).description(".")).defaultValue(true)).build());
    private final Setting<Boolean> onSword = this.sgGeneral
        .add(((Builder) ((Builder) ((Builder) new Builder().name("onSword")).description(".")).defaultValue(true)).build());
    private final Setting<Boolean> antiWeakness = this.sgGeneral
        .add(((Builder) ((Builder) ((Builder) new Builder().name("antiWeakness")).description(".")).defaultValue(true)).build());
    private final Setting<Mode> mode = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) new meteordevelopment.meteorclient.settings.EnumSetting.Builder()
                .name("Mode"))
                .description("."))
                .defaultValue(Mode.Blatant))
                .visible(this.antiWeakness::get))
                .build()
        );
    private final Setting<Boolean> onRightClick = this.sgGeneral
        .add(((Builder) ((Builder) ((Builder) new Builder().name("onRightClick")).description(".")).defaultValue(true)).build());
    private final Setting<Double> attackRange = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("Range"))
                .description("."))
                .defaultValue(3.0)
                .min(0.0)
                .sliderRange(0.0, 7.0)
                .build()
        );
    private final Setting<Integer> crandom1 = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("CrystalAttackDelay 1"))
                .description("."))
                .defaultValue(10))
                .min(0)
                .sliderRange(0, 100)
                .build()
        );
    private final Setting<Integer> crandom2 = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("CrystalAttackDelay 2"))
                .description("."))
                .defaultValue(10))
                .min(0)
                .sliderRange(0, 100)
                .build()
        );
    private final Setting<Integer> prandom1 = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("CrystalPlaceDelay 1"))
                .description("."))
                .defaultValue(10))
                .min(0)
                .sliderRange(0, 100)
                .build()
        );
    private final Setting<Integer> prandom2 = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("CrystalPlaceDelay 2"))
                .description("."))
                .defaultValue(10))
                .min(0)
                .sliderRange(0, 100)
                .build()
        );
    private final Setting<Boolean> ignoreWalls = this.sgAttack
        .add(((Builder) ((Builder) ((Builder) new Builder().name("IgnoreWalls")).description(".")).defaultValue(false)).build());
    private final Setting<Boolean> pauseEating = this.sgAttack
        .add(((Builder) ((Builder) ((Builder) new Builder().name("PauseWhileEating")).description(".")).defaultValue(false)).build());
    int delay2 = 0;
    int delay3 = 0;
    boolean swapped = false;

    public AutoCrystal() {
        super(Compassion.SERAPHIM, "AutoCrystal", ".");
    }

    @EventHandler
    public void onAttackCrystal(EventPlayerUpdate event) {
        if (!this.mc.player.isUsingItem() || !this.pauseEating.get()) {
            Entity ent = this.getRtxTarget(
                this.mc.player.getYaw(), this.mc.player.getPitch(), (this.attackRange.get()).floatValue(), this.ignoreWalls.get()
            );
            if (this.delay2 > 0) {
                this.delay2--;
            } else if (this.mc.options.useKey.isPressed() || !this.onRightClick.get()) {
                if ((this.mc.player.getMainHandStack().getItem().equals(Items.END_CRYSTAL) || !this.onCrystal.get())
                    && (this.mc.player.getMainHandStack().getItem().equals(Items.TOTEM_OF_UNDYING) || !this.onTotem.get())
                    && (this.mc.player.getMainHandStack().getItem() instanceof SwordItem || !this.onSword.get())) {
                    if (this.antiWeakness.get() && ((Mode) this.mode.get()).equals(Mode.Blatant) && this.swapped) {
                        this.swapped = false;
                        this.sendPacket(new UpdateSelectedSlotC2SPacket(this.mc.player.getInventory().selectedSlot));
                    }

                    if (ent instanceof EndCrystalEntity) {
                        if (this.swingFix.get()) {
                            Utils.leftClick();
                        } else {
                            this.mc.interactionManager.attackEntity(this.mc.player, ent);
                            this.mc.player.swingHand(Hand.MAIN_HAND);
                        }

                        this.delay2 = this.crandom1.get() >= this.crandom2.get()
                            ? this.crandom1.get()
                            : new Random().nextInt(this.crandom1.get(), this.crandom2.get());
                    }
                }
            }
        }
    }

    @EventHandler
    public void onPlaceCrystal(EventPlayerUpdate event) {
        if (this.delay3 > 0) {
            this.delay3--;
        } else if (this.mc.options.useKey.isPressed() || !this.onRightClick.get()) {
            if (this.mc.player.getMainHandStack().getItem().equals(Items.END_CRYSTAL)) {
                if (this.antiWeakness.get() && ((Mode) this.mode.get()).equals(Mode.Blatant) && this.swapped) {
                    this.swapped = false;
                    this.sendPacket(new UpdateSelectedSlotC2SPacket(this.mc.player.getInventory().selectedSlot));
                }

                if (this.mc.crosshairTarget instanceof BlockHitResult && this.isAble2Crystal(((BlockHitResult) this.mc.crosshairTarget).getBlockPos())) {
                    Utils.rightClick();
                    this.delay3 = this.prandom1.get() >= this.prandom2.get()
                        ? this.prandom1.get()
                        : new Random().nextInt(this.prandom1.get(), this.prandom2.get());
                }
            }
        }
    }

    public boolean isAble2Crystal(BlockPos pos) {
        ClientWorld world = this.mc.world;
        return world.getBlockState(pos).getBlock().equals(Blocks.OBSIDIAN) || world.getBlockState(pos).getBlock().equals(Blocks.BEDROCK);
    }

    public Entity getRtxTarget(float yaw, float pitch, float distance, boolean ignoreWalls) {
        Entity targetedEntity = null;
        HitResult result = ignoreWalls ? null : this.rayTrace(distance, yaw, pitch);
        Vec3d vec3d = this.mc.player.getPos().add(0.0, this.mc.player.getEyeHeight(this.mc.player.getPose()), 0.0);
        double distancePow2 = Math.pow(distance, 2.0);
        if (result != null) {
            distancePow2 = result.getPos().squaredDistanceTo(vec3d);
        }

        Vec3d vec3d2 = this.getRotationVector(pitch, yaw);
        Vec3d vec3d3 = vec3d.add(vec3d2.x * distance, vec3d2.y * distance, vec3d2.z * distance);
        Box box = this.mc.player.getBoundingBox().stretch(vec3d2.multiply(distance)).expand(1.0, 1.0, 1.0);
        EntityHitResult entityHitResult = ProjectileUtil.raycast(
            this.mc.player, vec3d, vec3d3, box, entity -> !entity.isSpectator() && entity.canHit(), distancePow2
        );
        if (entityHitResult != null) {
            Entity entity2 = entityHitResult.getEntity();
            Vec3d vec3d4 = entityHitResult.getPos();
            double g = vec3d.squaredDistanceTo(vec3d4);
            if ((g < distancePow2 || result == null) && entity2 instanceof Entity) {
                return entity2;
            }
        }

        return targetedEntity;
    }

    public HitResult rayTrace(double dst, float yaw, float pitch) {
        Vec3d vec3d = this.mc.player.getCameraPosVec(this.mc.getRenderTickCounter().getTickDelta(true));
        Vec3d vec3d2 = this.getRotationVector(pitch, yaw);
        Vec3d vec3d3 = vec3d.add(vec3d2.x * dst, vec3d2.y * dst, vec3d2.z * dst);
        return this.mc.world.raycast(new RaycastContext(vec3d, vec3d3, ShapeType.OUTLINE, FluidHandling.NONE, this.mc.player));
    }

    @NotNull
    public Vec3d getRotationVector(float yaw, float pitch) {
        return new Vec3d(
            MathHelper.sin(-pitch * (float) (Math.PI / 180.0)) * MathHelper.cos(yaw * (float) (Math.PI / 180.0)),
            -MathHelper.sin(yaw * (float) (Math.PI / 180.0)),
            MathHelper.cos(-pitch * (float) (Math.PI / 180.0)) * MathHelper.cos(yaw * (float) (Math.PI / 180.0))
        );
    }

    @EventHandler
    public void onAttackEntity(AttackEntityEvent event) {
        if (!PlayerUtils.canBreakCrystal()) {
            if (event.entity instanceof EndCrystalEntity) {
                switch ((Mode) this.mode.get()) {
                    case Legit:
                        if (this.getSword() == -1) {
                            return;
                        }

                        InvUtils.swap(this.getSword(), false);
                        break;
                    case Blatant:
                        StatusEffectInstance weakness = this.mc.player.getStatusEffect(StatusEffects.WEAKNESS);
                        StatusEffectInstance strength = this.mc.player.getStatusEffect(StatusEffects.STRENGTH);
                        if (weakness != null
                            && (strength == null || strength.getAmplifier() <= weakness.getAmplifier())
                            && !PlayerUtils.isValidWeaknessItem(this.mc.player.getMainHandStack())) {
                            this.sendPacket(new UpdateSelectedSlotC2SPacket(this.getSword()));
                            this.swapped = true;
                        }
                }
            }
        }
    }

    public int getSword() {
        for (int i = 0; i < this.mc.player.getInventory().size(); i++) {
            ItemStack stack = this.mc.player.getInventory().getStack(i);
            if (!stack.isEmpty() && stack.getItem() instanceof SwordItem) {
                return i;
            }
        }

        return -1;
    }

    static enum Mode {
        Legit,
        Blatant;
    }
}
