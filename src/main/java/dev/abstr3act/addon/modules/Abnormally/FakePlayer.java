package dev.abstr3act.addon.modules.Abnormally;

import com.mojang.authlib.GameProfile;
import dev.abstr3act.addon.Compassion;
import dev.abstr3act.addon.events.EventTotemPop;
import dev.abstr3act.addon.module.AbnormallyModule;
import dev.abstr3act.addon.utils.InventoryUtility;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.entity.player.AttackEntityEvent;
import meteordevelopment.meteorclient.events.packets.PacketEvent.Receive;
import meteordevelopment.meteorclient.events.world.TickEvent.Pre;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.StringSetting.Builder;
import meteordevelopment.meteorclient.systems.modules.combat.Criticals;
import meteordevelopment.meteorclient.utils.entity.DamageUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.entity.Entity.RemovalReason;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.minecraft.network.packet.s2c.play.ExplosionS2CPacket;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FakePlayer extends AbnormallyModule {
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
    private final Setting<String> name = this.sgGeneral
        .add(((Builder) ((Builder) ((Builder) new Builder().name("Name")).description("Name of FakePlayer")).defaultValue("NoStrict")).build());
    private final Setting<Boolean> copyInventory = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("Copy Inventory"))
                .description("Copy player inventory"))
                .defaultValue(false))
                .build()
        );
    private final Setting<Boolean> record = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("Record"))
                .description("Record player"))
                .defaultValue(false))
                .build()
        );
    private final Setting<Boolean> play = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("Play"))
                .description("Allow movement"))
                .defaultValue(false))
                .build()
        );
    private final Setting<Boolean> autoTotem = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("AutoTotem"))
                .description("Auto totem"))
                .defaultValue(true))
                .build()
        );
    private final List<PlayerState> positions = new ArrayList<>();
    private OtherClientPlayerEntity fakePlayer;
    private int movementTick;
    private int deathTime;

    public FakePlayer() {
        super(Compassion.ABNORMALLY, "Fake Player", "Spawns a client-side fake player for testing usages. No need to be active.");
    }

    public void onActivate() {
        this.fakePlayer = new OtherClientPlayerEntity(
            this.mc.world, new GameProfile(UUID.fromString("66123666-6666-6666-6666-666666666600"), (String) this.name.get())
        );
        this.fakePlayer.copyPositionAndRotation(this.mc.player);
        if (this.copyInventory.get()) {
            this.fakePlayer.setStackInHand(Hand.MAIN_HAND, this.mc.player.getMainHandStack().copy());
            this.fakePlayer.setStackInHand(Hand.OFF_HAND, this.mc.player.getOffHandStack().copy());
            this.fakePlayer.getInventory().setStack(36, this.mc.player.getInventory().getStack(36).copy());
            this.fakePlayer.getInventory().setStack(37, this.mc.player.getInventory().getStack(37).copy());
            this.fakePlayer.getInventory().setStack(38, this.mc.player.getInventory().getStack(38).copy());
            this.fakePlayer.getInventory().setStack(39, this.mc.player.getInventory().getStack(39).copy());
        }

        this.mc.world.addEntity(this.fakePlayer);
        this.fakePlayer.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 9999, 2));
        this.fakePlayer.addStatusEffect(new StatusEffectInstance(StatusEffects.ABSORPTION, 9999, 4));
        this.fakePlayer.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 9999, 1));
    }

    @EventHandler
    public void onPacketReceive(Receive e) {
        if (e.packet instanceof ExplosionS2CPacket explosion && this.fakePlayer != null && this.fakePlayer.hurtTime == 0) {
            this.fakePlayer.onDamaged(this.mc.world.getDamageSources().generic());
            this.fakePlayer
                .setHealth(
                    this.fakePlayer.getHealth()
                        + this.fakePlayer.getAbsorptionAmount()
                        - DamageUtils.explosionDamage(
                        this.fakePlayer,
                        this.fakePlayer.getPos(),
                        this.fakePlayer.getBoundingBox(),
                        new Vec3d(explosion.getX(), explosion.getY(), explosion.getZ()),
                        6.0F,
                        DamageUtils.HIT_FACTORY
                    )
                );
            if (this.fakePlayer.isDead() && this.fakePlayer.tryUseTotem(this.mc.world.getDamageSources().generic())) {
                this.fakePlayer.setHealth(10.0F);
                MeteorClient.EVENT_BUS.post(new EventTotemPop(this.fakePlayer, 1));
            }
        }
    }

    @EventHandler
    public void onTick(Pre event) {
        if (this.record.get()) {
            this.positions
                .add(
                    new PlayerState(this.mc.player.getX(), this.mc.player.getY(), this.mc.player.getZ(), this.mc.player.getYaw(), this.mc.player.getPitch())
                );
        } else {
            if (this.fakePlayer != null) {
                if (this.play.get() && !this.positions.isEmpty()) {
                    this.movementTick++;
                    if (this.movementTick >= this.positions.size()) {
                        this.movementTick = 0;
                        return;
                    }

                    PlayerState p = this.positions.get(this.movementTick);
                    this.fakePlayer.setYaw(p.yaw);
                    this.fakePlayer.setPitch(p.pitch);
                    this.fakePlayer.setHeadYaw(p.yaw);
                    this.fakePlayer.updateTrackedPosition(p.x, p.y, p.z);
                    this.fakePlayer.updateTrackedPositionAndAngles(p.x, p.y, p.z, p.yaw, p.pitch, 3);
                } else {
                    this.movementTick = 0;
                }

                if (this.autoTotem.get() && this.fakePlayer.getOffHandStack().getItem() != Items.TOTEM_OF_UNDYING) {
                    this.fakePlayer.setStackInHand(Hand.OFF_HAND, new ItemStack(Items.TOTEM_OF_UNDYING));
                }

                if (this.fakePlayer.isDead()) {
                    this.deathTime++;
                    if (this.deathTime > 10) {
                        this.toggle();
                    }
                }
            }
        }
    }

    @EventHandler
    public void onAttack(AttackEntityEvent e) {
        if (this.fakePlayer != null && e.entity == this.fakePlayer && this.fakePlayer.hurtTime == 0) {
            this.mc
                .world
                .playSound(
                    this.mc.player,
                    this.fakePlayer.getX(),
                    this.fakePlayer.getY(),
                    this.fakePlayer.getZ(),
                    SoundEvents.ENTITY_PLAYER_HURT,
                    SoundCategory.PLAYERS,
                    1.0F,
                    1.0F
                );
            Criticals criticals = new Criticals();
            if (this.mc.player.fallDistance > 0.0F || criticals.isActive()) {
                this.mc
                    .world
                    .playSound(
                        this.mc.player,
                        this.fakePlayer.getX(),
                        this.fakePlayer.getY(),
                        this.fakePlayer.getZ(),
                        SoundEvents.ENTITY_PLAYER_ATTACK_CRIT,
                        SoundCategory.PLAYERS,
                        1.0F,
                        1.0F
                    );
            }

            this.fakePlayer.onDamaged(this.mc.world.getDamageSources().generic());
            if (this.mc.player.getAttackCooldownProgress(0.0F) >= 0.85) {
                this.fakePlayer
                    .setHealth(
                        this.fakePlayer.getHealth()
                            + this.fakePlayer.getAbsorptionAmount()
                            - InventoryUtility.getHitDamage(this.mc.player.getMainHandStack(), this.fakePlayer)
                    );
            } else {
                this.fakePlayer.setHealth(this.fakePlayer.getHealth() + this.fakePlayer.getAbsorptionAmount() - 1.0F);
            }

            if (this.fakePlayer.isDead() && this.fakePlayer.tryUseTotem(this.mc.world.getDamageSources().generic())) {
                this.fakePlayer.setHealth(10.0F);
                new EntityStatusS2CPacket(this.fakePlayer, (byte) 35).apply(this.mc.player.networkHandler);
            }
        }
    }

    public void onDeactivate() {
        if (this.fakePlayer != null) {
            this.fakePlayer.kill();
            this.fakePlayer.setRemoved(RemovalReason.KILLED);
            this.fakePlayer.onRemoved();
            this.fakePlayer = null;
            this.positions.clear();
            this.deathTime = 0;
        }
    }

    private record PlayerState(double x, double y, double z, float yaw, float pitch) {
    }
}
