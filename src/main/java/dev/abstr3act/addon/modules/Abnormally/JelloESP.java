package dev.abstr3act.addon.modules.Abnormally;

import dev.abstr3act.addon.Compassion;
import dev.abstr3act.addon.module.AbnormallyModule;
import dev.abstr3act.addon.utils.Render3DUtils;
import dev.abstr3act.addon.utils.luna.CountdownUtils;
import meteordevelopment.meteorclient.events.entity.player.AttackEntityEvent;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent.Post;
import meteordevelopment.meteorclient.settings.BoolSetting.Builder;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.utils.entity.EntityUtils;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;

import java.util.Set;

public class JelloESP extends AbnormallyModule {
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
    public final Setting<Boolean> onlyOnAttack = this.sgGeneral
        .add(((Builder) ((Builder) ((Builder) new Builder().name("only-attack")).description("Ignores yourself drawing the shader.")).defaultValue(true)).build());
    public final Setting<Boolean> ignoreSelf = this.sgGeneral
        .add(((Builder) ((Builder) ((Builder) new Builder().name("ignore-self")).description("Ignores yourself drawing the shader.")).defaultValue(true)).build());
    public final Setting<Integer> segments = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("segments"))
                .description("Ignores yourself drawing the shader."))
                .defaultValue(30))
                .sliderRange(10, 100)
                .build()
        );
    public final Setting<Double> speed = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("speed"))
                .description("Speed of the aura"))
                .defaultValue(0.15F)
                .sliderRange(0.01F, 2.0)
                .build()
        );
    private final SettingGroup sgColors = this.settings.createGroup("Colors");
    private final Setting<Set<EntityType<?>>> entities = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.EntityTypeListSetting.Builder) ((meteordevelopment.meteorclient.settings.EntityTypeListSetting.Builder) new meteordevelopment.meteorclient.settings.EntityTypeListSetting.Builder()
                .name("entities"))
                .description("Select specific entities."))
                .defaultValue(new EntityType[]{EntityType.PLAYER})
                .build()
        );
    private final Setting<mode> Mode = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) new meteordevelopment.meteorclient.settings.EnumSetting.Builder()
                .name("Mode"))
                .description("Mode of the aura"))
                .defaultValue(mode.Rainbow))
                .build()
        );
    private final Setting<SettingColor> Color = this.sgColors
        .add(
            ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                .name("jello-color"))
                .description("The misc color."))
                .defaultValue(new SettingColor(175, 175, 175, 255))
                .visible(() -> this.Mode.get() != mode.DoubleColor))
                .build()
        );
    private final Setting<SettingColor> startColor = this.sgColors
        .add(
            ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                .name("jello-color-start"))
                .description("The misc color."))
                .defaultValue(new SettingColor(175, 175, 175, 255))
                .visible(() -> this.Mode.get() == mode.DoubleColor && this.Mode.get() != mode.Rainbow))
                .build()
        );
    private final Setting<SettingColor> endColor = this.sgColors
        .add(
            ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                .name("jello-color-end"))
                .description("The misc color."))
                .defaultValue(new SettingColor(175, 175, 175, 255))
                .visible(() -> this.Mode.get() == mode.DoubleColor && this.Mode.get() != mode.Rainbow))
                .build()
        );
    private final Setting<Boolean> attackDiscoloration = this.sgColors
        .add(
            ((Builder) ((Builder) ((Builder) new Builder().name("Attack-discoloration")).description("Change color while attacking target")).defaultValue(false))
                .build()
        );
    private final Setting<mode> DisCMode = this.sgColors
        .add(
            ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) new meteordevelopment.meteorclient.settings.EnumSetting.Builder()
                .name("Mode-attack"))
                .description("Mode of the aura"))
                .defaultValue(mode.Rainbow))
                .visible(this.attackDiscoloration::get))
                .build()
        );
    private final Setting<SettingColor> DisColor = this.sgColors
        .add(
            ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                .name("jello-color-attack"))
                .description("The misc color."))
                .defaultValue(new SettingColor(175, 175, 175, 255))
                .visible(() -> this.DisCMode.get() != mode.DoubleColor && this.attackDiscoloration.get()))
                .build()
        );
    private final Setting<SettingColor> DisStartColor = this.sgColors
        .add(
            ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                .name("jello-color-start-attack"))
                .description("The misc color."))
                .defaultValue(new SettingColor(175, 175, 175, 255))
                .visible(
                    () -> this.DisCMode.get() == mode.DoubleColor && this.DisCMode.get() != mode.Rainbow && this.attackDiscoloration.get()
                ))
                .build()
        );
    private final Setting<SettingColor> DisEndColor = this.sgColors
        .add(
            ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                .name("jello-color-end-attack"))
                .description("The misc color."))
                .defaultValue(new SettingColor(175, 175, 175, 255))
                .visible(
                    () -> this.DisCMode.get() == mode.DoubleColor && this.DisCMode.get() != mode.Rainbow && this.attackDiscoloration.get()
                ))
                .build()
        );
    public final Setting<Double> ignoreAttackTime = this.sgColors
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("Show duration"))
                .description("Duration of change color."))
                .defaultValue(3.0)
                .sliderRange(0.1, 100.0)
                .visible(this.attackDiscoloration::get))
                .build()
        );
    public boolean attacked = false;
    Entity target;
    private int count;

    public JelloESP() {
        super(Compassion.ABNORMALLY, "JelloEsp", "Renders entities through walls.");
    }

    @EventHandler
    private void onTickEvent(Post event) {
        if (CountdownUtils.isEnd && this.attacked) {
            this.attacked = false;
        }

        Render3DUtils.updateJello((this.speed.get()).floatValue());
    }

    @EventHandler
    private void onRender3D(Render3DEvent event) {
        this.count = 0;
        if (!this.onlyOnAttack.get()) {
            for (Entity entity : this.mc.world.getEntities()) {
                if (!this.shouldSkip(entity) && this.target != null) {
                    if (this.attacked && this.attackDiscoloration.get()) {
                        switch ((mode) this.DisCMode.get()) {
                            case SingleColor:
                                Render3DUtils.drawJello(event.matrices, this.target, (SettingColor) this.DisColor.get(), this.segments.get());
                                break;
                            case DoubleColor:
                                Render3DUtils.drawJello(
                                    event.matrices, this.target, (SettingColor) this.DisStartColor.get(), (SettingColor) this.DisEndColor.get(), this.segments.get()
                                );
                                break;
                            case Rainbow:
                                Render3DUtils.drawJelloRainbow(event.matrices, this.target, (SettingColor) this.DisColor.get(), this.segments.get());
                        }
                    } else {
                        switch ((mode) this.Mode.get()) {
                            case SingleColor:
                                Render3DUtils.drawJello(event.matrices, entity, (SettingColor) this.Color.get(), this.segments.get());
                                break;
                            case DoubleColor:
                                Render3DUtils.drawJello(
                                    event.matrices, entity, (SettingColor) this.startColor.get(), (SettingColor) this.endColor.get(), this.segments.get()
                                );
                                break;
                            case Rainbow:
                                Render3DUtils.drawJelloRainbow(event.matrices, entity, (SettingColor) this.Color.get(), this.segments.get());
                        }
                    }

                    this.count++;
                }
            }
        } else if (this.target != null && this.shouldSkipAttack(this.target)) {
            if (this.attacked && this.attackDiscoloration.get()) {
                switch ((mode) this.DisCMode.get()) {
                    case SingleColor:
                        Render3DUtils.drawJello(event.matrices, this.target, (SettingColor) this.DisColor.get(), this.segments.get());
                        break;
                    case DoubleColor:
                        Render3DUtils.drawJello(
                            event.matrices, this.target, (SettingColor) this.DisStartColor.get(), (SettingColor) this.DisEndColor.get(), this.segments.get()
                        );
                        break;
                    case Rainbow:
                        Render3DUtils.drawJelloRainbow(event.matrices, this.target, (SettingColor) this.DisColor.get(), this.segments.get());
                }
            } else {
                switch ((mode) this.Mode.get()) {
                    case SingleColor:
                        Render3DUtils.drawJello(event.matrices, this.target, (SettingColor) this.Color.get(), this.segments.get());
                        break;
                    case DoubleColor:
                        Render3DUtils.drawJello(
                            event.matrices, this.target, (SettingColor) this.startColor.get(), (SettingColor) this.endColor.get(), this.segments.get()
                        );
                        break;
                    case Rainbow:
                        Render3DUtils.drawJelloRainbow(event.matrices, this.target, (SettingColor) this.Color.get(), this.segments.get());
                }
            }
        }
    }

    @EventHandler
    private void onAttack(AttackEntityEvent event) {
        if (event.entity instanceof LivingEntity) {
            this.target = event.entity;
        }
    }

    public boolean shouldSkip(Entity entity) {
        if (!((Set) this.entities.get()).contains(entity.getType())) {
            return true;
        } else if (entity == this.mc.player && this.ignoreSelf.get()) {
            return true;
        } else {
            return entity == this.mc.cameraEntity && this.mc.options.getPerspective().isFirstPerson() ? true : !EntityUtils.isInRenderDistance(entity);
        }
    }

    public boolean shouldSkipAttack(Entity e) {
        return e.isAlive() ? true : !EntityUtils.isInRenderDistance(e);
    }

    public String getInfoString() {
        return Integer.toString(this.count);
    }

    public static enum mode {
        SingleColor,
        DoubleColor,
        Rainbow;
    }
}
