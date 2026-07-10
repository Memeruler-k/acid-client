package dev.abstr3act.addon.modules.Seraphim;

import dev.abstr3act.addon.Compassion;
import dev.abstr3act.addon.module.SeraphimModule;
import dev.abstr3act.addon.utils.PlayerManager;
import meteordevelopment.meteorclient.events.world.TickEvent.Pre;
import meteordevelopment.meteorclient.settings.EntityTypeListSetting.Builder;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Tameable;
import net.minecraft.entity.mob.EndermanEntity;
import net.minecraft.entity.mob.ZombifiedPiglinEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.AxeItem;
import net.minecraft.item.Item;
import net.minecraft.item.MaceItem;
import net.minecraft.item.SwordItem;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.RaycastContext.FluidHandling;
import net.minecraft.world.RaycastContext.ShapeType;

import java.util.Set;
import java.util.function.Predicate;

public class Aura extends SeraphimModule {
    private final SettingGroup sgFilter = this.settings.createGroup("Filter");
    private final SettingGroup sgAttack = this.settings.createGroup("Attack");
    private final SettingGroup sgVisual = this.settings.createGroup("Visual");
    private final Setting<Set<EntityType<?>>> entities = this.sgFilter
        .add(
            ((Builder) ((Builder) new Builder().name("entities")).description("Specifies the entity types to target for attack."))
                .onlyAttackable()
                .defaultValue(new EntityType[]{EntityType.PLAYER})
                .build()
        );
    private final Setting<Double> range = this.sgFilter
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("range"))
                .description("Defines the maximum range for attacking a target entity."))
                .defaultValue(4.5)
                .min(0.0)
                .sliderMax(6.0)
                .build()
        );
    private final Setting<Boolean> ignoreBabies = this.sgFilter
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("ignore-babies"))
                .description("Prevents attacking baby variants of mobs."))
                .defaultValue(true))
                .build()
        );
    private final Setting<Boolean> ignoreNamed = this.sgFilter
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("ignore-named"))
                .description("Prevents attacking named mobs."))
                .defaultValue(false))
                .build()
        );
    private final Setting<Boolean> ignorePassive = this.sgFilter
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("ignore-passive"))
                .description("Allows attacking passive mobs only if they target you."))
                .defaultValue(true))
                .build()
        );
    private final Setting<Boolean> ignoreTamed = this.sgFilter
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("ignore-tamed"))
                .description("Prevents attacking tamed mobs."))
                .defaultValue(false))
                .build()
        );
    private final Setting<Boolean> ignoreFriends = this.sgFilter
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("ignore-friends"))
                .description("Prevents attacking players on your friends list."))
                .defaultValue(true))
                .build()
        );
    private final Setting<Boolean> ignoreWalls = this.sgFilter
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("ignore-walls"))
                .description("Allows attacking through walls."))
                .defaultValue(false))
                .build()
        );
    private final Setting<OnFallMode> onFallMode = this.sgAttack
        .add(
            ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) new meteordevelopment.meteorclient.settings.EnumSetting.Builder()
                .name("on-fall-mode"))
                .description("Chooses an attack strategy when falling to maximize critical damage."))
                .defaultValue(OnFallMode.Value))
                .build()
        );
    private final Setting<Double> onFallValue = this.sgAttack
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("on-fall-value"))
                .description("Defines a specific value for attacking while falling."))
                .min(0.0)
                .defaultValue(0.25)
                .sliderMax(1.0)
                .visible(() -> this.onFallMode.get() == OnFallMode.Value))
                .build()
        );
    private final Setting<Double> onFallMinRandomValue = this.sgAttack
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("on-fall-min-random-value"))
                .description("Specifies the minimum randomized value for attacking while falling."))
                .min(0.0)
                .defaultValue(0.2)
                .sliderMax(1.0)
                .visible(() -> this.onFallMode.get() == OnFallMode.RandomValue))
                .build()
        );
    private final Setting<Double> onFallMaxRandomValue = this.sgAttack
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("on-fall-max-random-value"))
                .description("Specifies the maximum randomized value for attacking while falling."))
                .min(0.0)
                .defaultValue(0.4)
                .sliderMax(1.0)
                .visible(() -> this.onFallMode.get() == OnFallMode.RandomValue))
                .build()
        );
    private final Setting<HitSpeedMode> hitSpeedMode = this.sgAttack
        .add(
            ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) new meteordevelopment.meteorclient.settings.EnumSetting.Builder()
                .name("hit-speed-mode"))
                .description("Selects a hit speed mode for attacking."))
                .defaultValue(HitSpeedMode.Value))
                .build()
        );
    private final Setting<Double> hitSpeedValue = this.sgAttack
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("hit-speed-value"))
                .description("Defines a specific hit speed value for attacking."))
                .defaultValue(0.0)
                .sliderRange(-10.0, 10.0)
                .visible(() -> this.hitSpeedMode.get() == HitSpeedMode.Value))
                .build()
        );
    private final Setting<Double> hitSpeedMinRandomValue = this.sgAttack
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("hit-speed-min-random-value"))
                .description("Specifies the minimum randomized hit speed value."))
                .defaultValue(0.0)
                .sliderRange(-10.0, 10.0)
                .visible(() -> this.hitSpeedMode.get() == HitSpeedMode.RandomValue))
                .build()
        );
    private final Setting<Double> hitSpeedMaxRandomValue = this.sgAttack
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("hit-speed-max-random-value"))
                .description("Specifies the maximum randomized hit speed value."))
                .defaultValue(0.0)
                .sliderRange(-10.0, 10.0)
                .visible(() -> this.hitSpeedMode.get() == HitSpeedMode.RandomValue))
                .build()
        );
    private final Setting<Boolean> swingHand = this.sgVisual
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("swing-hand"))
                .description("Makes hand swing visible client-side."))
                .defaultValue(true))
                .build()
        );
    private final Setting<Boolean> breakShield = this.sgAttack
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("breakShield"))
                .description("Makes hand swing visible client-side."))
                .defaultValue(true))
                .build()
        );
    private final Setting<Boolean> instant = this.sgAttack
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("instant"))
                .description("Makes hand swing visible client-side."))
                .defaultValue(true))
                .build()
        );
    private final Setting<Boolean> onlyWeapon = this.sgAttack
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("OnlyWeapon"))
                .description("."))
                .defaultValue(false))
                .build()
        );
    float randomOnFallFloat = 0.0F;
    float randomHitSpeedFloat = 0.0F;

    public Aura() {
        super(Compassion.SERAPHIM, "Aura", "Killaura which only attacks target if you aim at it.");
    }

    @EventHandler
    private void onTick(Pre event) {
        if (!this.mc.player.isDead() && this.mc.world != null) {
            OnFallMode currOnFallMode = (OnFallMode) this.onFallMode.get();
            if (currOnFallMode != OnFallMode.None) {
                float onFall = currOnFallMode == OnFallMode.Value ? (this.onFallValue.get()).floatValue() : this.randomOnFallFloat;
                if (!(this.mc.player.fallDistance > onFall)) {
                    return;
                }
            }

            HitSpeedMode currHitSpeedMode = (HitSpeedMode) this.hitSpeedMode.get();
            float hitSpeed = currHitSpeedMode == HitSpeedMode.Value ? (this.hitSpeedValue.get()).floatValue() : this.randomHitSpeedFloat;
            if (currHitSpeedMode == HitSpeedMode.None || !(this.mc.player.getAttackCooldownProgress(hitSpeed) * 17.0F < 16.0F)) {
                HitResult hitResult = this.getCrosshairTarget(
                    this.mc.player,
                    this.range.get(),
                    this.ignoreWalls.get(),
                    e -> !e.isSpectator()
                        && e.canHit()
                        && ((Set) this.entities.get()).contains(e.getType())
                        && (!this.ignoreBabies.get() || !(e instanceof AnimalEntity) || !((AnimalEntity) e).isBaby())
                        && (!this.ignoreNamed.get() || !e.hasCustomName())
                        && (
                        !this.ignorePassive.get()
                            || !(e instanceof EndermanEntity enderman && !enderman.isAngry())
                            && !(e instanceof ZombifiedPiglinEntity piglin && !piglin.isAttacking())
                            && !(e instanceof WolfEntity wolf && !wolf.isAttacking())
                    )
                        && (
                        !(this.ignoreTamed.get() && e instanceof Tameable tameable)
                            || tameable.getOwnerUuid() == null
                            || !tameable.getOwnerUuid().equals(this.mc.player.getUuid())
                    )
                        && (!(this.ignoreFriends.get() && e instanceof PlayerEntity player) || Friends.get().shouldAttack(player))
                );
                if (hitResult != null && hitResult.getType() == Type.ENTITY) {
                    Entity entity = ((EntityHitResult) hitResult).getEntity();
                    if (this.breakShield.get()) {
                        PlayerManager.shieldBreaker(this.instant.get(), entity);
                    }

                    if (!this.onlyWeapon.get() || this.isWeapon(this.mc.player.getMainHandStack().getItem())) {
                        LivingEntity livingEntity = (LivingEntity) entity;
                        if (livingEntity.getHealth() > 0.0F) {
                            this.mc.interactionManager.attackEntity(this.mc.player, livingEntity);
                            if (this.swingHand.get()) {
                                this.mc.player.swingHand(Hand.MAIN_HAND);
                            }

                            if (currOnFallMode == OnFallMode.RandomValue) {
                                float min = Math.min((this.onFallMinRandomValue.get()).floatValue(), (this.onFallMaxRandomValue.get()).floatValue());
                                float max = Math.max((this.onFallMinRandomValue.get()).floatValue(), (this.onFallMaxRandomValue.get()).floatValue());
                                this.randomOnFallFloat = min + this.mc.world.random.nextFloat() * (max - min);
                            }

                            if (currHitSpeedMode == HitSpeedMode.RandomValue) {
                                float min = Math.min((this.hitSpeedMinRandomValue.get()).floatValue(), (this.hitSpeedMaxRandomValue.get()).floatValue());
                                float max = Math.max((this.hitSpeedMinRandomValue.get()).floatValue(), (this.hitSpeedMaxRandomValue.get()).floatValue());
                                this.randomHitSpeedFloat = min + this.mc.world.random.nextFloat() * (max - min);
                            }
                        }
                    }
                }
            }
        }
    }

    public boolean isWeapon(Item item) {
        return item instanceof AxeItem || item instanceof SwordItem || item instanceof MaceItem;
    }

    public HitResult getCrosshairTarget(Entity entity, double range, boolean ignoreBlocks, Predicate<Entity> filter) {
        if (entity != null && this.mc.world != null) {
            Vec3d vec3d = entity.getCameraPosVec(1.0F);
            Vec3d vec3d2 = entity.getRotationVec(1.0F);
            Vec3d vec3d3 = vec3d.add(vec3d2.multiply(range));
            Box box = entity.getBoundingBox().stretch(vec3d2.multiply(range)).expand(1.0);
            RaycastContext raycastContext = new RaycastContext(vec3d, vec3d3, ShapeType.COLLIDER, FluidHandling.NONE, entity);
            HitResult hitResult = this.mc.world.raycast(raycastContext);
            double e = range * range;
            if (hitResult != null && !ignoreBlocks) {
                e = hitResult.getPos().squaredDistanceTo(vec3d);
            }

            EntityHitResult entityHitResult = ProjectileUtil.raycast(entity, vec3d, vec3d3, box, filter.and(targetEntity -> !targetEntity.isSpectator()), e);
            if (entityHitResult != null) {
                return entityHitResult;
            } else {
                return !ignoreBlocks ? hitResult : null;
            }
        } else {
            return null;
        }
    }

    public static enum HitSpeedMode {
        None,
        Value,
        RandomValue;
    }

    public static enum OnFallMode {
        None,
        Value,
        RandomValue;
    }
}
