package dev.abstr3act.addon.modules.Abnormally;

import dev.abstr3act.addon.Compassion;
import dev.abstr3act.addon.module.AbnormallyModule;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent.Pre;
import meteordevelopment.meteorclient.settings.EnumSetting.Builder;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class KillEffects extends AbnormallyModule {
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
    private final Setting<Mode> mode = this.sgGeneral
        .add(((Builder) ((Builder) ((Builder) new Builder().name("Mode")).description(".")).defaultValue(Mode.LightningBolt)).build());
    private final Setting<Boolean> playSound = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("Play Sound"))
                .description("."))
                .defaultValue(true))
                .visible(() -> !((Mode) this.mode.get()).equals(Mode.FallingLava)))
                .build()
        );
    private final Map<Entity, Long> renderEntities = new ConcurrentHashMap<>();
    private final Map<Entity, Long> lightingEntities = new ConcurrentHashMap<>();

    public KillEffects() {
        super(Compassion.ABNORMALLY, "Kill Effects", "Render some things where enemy died.");
    }

    @EventHandler
    public void onRender(Render3DEvent event) {
        switch ((Mode) this.mode.get()) {
            case FallingLava:
                this.renderEntities.keySet().forEach(entity -> {
                    for (int i = 0; i < entity.getHeight() * 10.0F; i++) {
                        for (int j = 0; j < entity.getWidth() * 10.0F; j++) {
                            for (int k = 0; k < entity.getWidth() * 10.0F; k++) {
                                this.mc.world.addParticle(ParticleTypes.FALLING_LAVA, entity.getX() + j * 0.1, entity.getY() + i * 0.1, entity.getZ() + k * 0.1, 0.0, 0.0, 0.0);
                            }
                        }
                    }

                    this.renderEntities.remove(entity);
                });
                break;
            case LightningBolt:
                this.renderEntities
                    .forEach(
                        (entity, time) -> {
                            LightningEntity lightningEntity = new LightningEntity(EntityType.LIGHTNING_BOLT, this.mc.world);
                            lightningEntity.refreshPositionAfterTeleport(entity.getX(), entity.getY(), entity.getZ());
                            this.mc.world.addEntity(lightningEntity);
                            if (this.playSound.get()) {
                                this.mc
                                    .world
                                    .playSound(
                                        this.mc.player,
                                        entity.getX(),
                                        entity.getY(),
                                        entity.getZ(),
                                        SoundEvents.ENTITY_LIGHTNING_BOLT_THUNDER,
                                        SoundCategory.WEATHER,
                                        10000.0F,
                                        0.16000001F
                                    );
                                this.mc
                                    .world
                                    .playSound(
                                        this.mc.player, entity.getX(), entity.getY(), entity.getZ(), SoundEvents.ENTITY_LIGHTNING_BOLT_IMPACT, SoundCategory.WEATHER, 2.0F, 0.1F
                                    );
                            }

                            this.renderEntities.remove(entity);
                            this.lightingEntities.put(entity, System.currentTimeMillis());
                        }
                    );
        }
    }

    @EventHandler
    public void onTick(Pre event) {
        this.mc.world.getEntities().forEach(entity -> {
            if (entity instanceof PlayerEntity) {
                if (entity != this.mc.player && !this.renderEntities.containsKey(entity) && !this.lightingEntities.containsKey(entity)) {
                    if (!entity.isAlive() && ((PlayerEntity) entity).getHealth() == 0.0F) {
                        this.renderEntities.put(entity, System.currentTimeMillis());
                    }
                }
            }
        });
        if (!this.lightingEntities.isEmpty()) {
            this.lightingEntities.forEach((entity, time) -> {
                if (System.currentTimeMillis() - time > 5000L) {
                    this.lightingEntities.remove(entity);
                }
            });
        }
    }

    public static enum Mode {
        FallingLava,
        LightningBolt;
    }
}
