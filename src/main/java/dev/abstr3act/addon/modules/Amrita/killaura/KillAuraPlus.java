package dev.abstr3act.addon.modules.Amrita.killaura;

import dev.abstr3act.addon.Compassion;
import dev.abstr3act.addon.module.AmritaModule;
import dev.abstr3act.addon.modules.Amrita.killaura.modes.Matrix;
import meteordevelopment.meteorclient.events.packets.PacketEvent.Send;
import meteordevelopment.meteorclient.events.world.TickEvent.Post;
import meteordevelopment.meteorclient.events.world.TickEvent.Pre;
import meteordevelopment.meteorclient.settings.EnumSetting.Builder;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.combat.KillAura.ShieldMode;
import meteordevelopment.meteorclient.systems.modules.combat.KillAura.Weapon;
import meteordevelopment.meteorclient.utils.entity.SortPriority;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.EntityType;
import org.spongepowered.asm.mixin.Unique;

import java.util.Set;

public class KillAuraPlus extends AmritaModule {
    public final SettingGroup sgGeneral = this.settings.getDefaultGroup();
    public final Setting<Weapon> weapon = this.sgGeneral
        .add(
            ((Builder) ((Builder) ((Builder) new Builder().name("weapon")).description("Only attacks an entity when a specified weapon is in your hand."))
                .defaultValue(Weapon.Any))
                .build()
        );
    public final Setting<Boolean> autoSwitch = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("auto-switch"))
                .description("Switches to your selected weapon when attacking the target."))
                .defaultValue(false))
                .build()
        );
    public final Setting<ShieldMode> shieldMode = this.sgGeneral
        .add(
            ((Builder) ((Builder) ((Builder) ((Builder) new Builder().name("shield-mode")).description("Will try and use an axe to break target shields."))
                .defaultValue(ShieldMode.Break))
                .visible(() -> this.autoSwitch.get() && this.weapon.get() != Weapon.Axe))
                .build()
        );
    private final SettingGroup sgTargeting = this.settings.createGroup("Targeting");
    public final Setting<Set<EntityType<?>>> entities = this.sgTargeting
        .add(
            ((meteordevelopment.meteorclient.settings.EntityTypeListSetting.Builder) ((meteordevelopment.meteorclient.settings.EntityTypeListSetting.Builder) new meteordevelopment.meteorclient.settings.EntityTypeListSetting.Builder()
                .name("entities"))
                .description("Entities to attack."))
                .onlyAttackable()
                .defaultValue(new EntityType[]{EntityType.PLAYER})
                .build()
        );
    public final Setting<SortPriority> priority = this.sgTargeting
        .add(
            ((Builder) ((Builder) ((Builder) new Builder().name("priority")).description("How to filter targets within range.")).defaultValue(SortPriority.ClosestAngle))
                .build()
        );
    public final Setting<Double> range = this.sgTargeting
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("range"))
                .description("The maximum range the entity can be to attack it."))
                .defaultValue(4.5)
                .min(0.0)
                .sliderMax(6.0)
                .build()
        );
    public final Setting<Double> wallsRange = this.sgTargeting
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("walls-range"))
                .description("The maximum range the entity can be attacked through walls."))
                .defaultValue(3.5)
                .min(0.0)
                .sliderMax(6.0)
                .build()
        );
    public final Setting<Boolean> ignorePassive = this.sgTargeting
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("ignore-passive"))
                .description("Will only attack sometimes passive mobs if they are targeting you."))
                .defaultValue(true))
                .build()
        );
    @Unique
    public final Setting<Boolean> onlyCrits = this.sgTargeting
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("only-crits"))
                .description("Attack enemy only if this attack crit after jump."))
                .defaultValue(true))
                .build()
        );
    public final Setting<Boolean> ignoreTamed = this.sgTargeting
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("ignore-tamed"))
                .description("Will avoid attacking mobs you tamed."))
                .defaultValue(false))
                .build()
        );
    private final SettingGroup sgRotation = this.settings.createGroup("Rotation");
    public final Setting<Matrix.Type> rotationType = this.sgRotation
        .add(((Builder) ((Builder) new Builder().name("rotation-type")).defaultValue(Matrix.Type.Smooth)).build());
    public final Setting<Boolean> speedUpRotationWhenAttacking = this.sgRotation
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("Speed-up-the-rotation-when-attacking"))
                .defaultValue(false))
                .build()
        );
    private KillAuraPlusMode currentMode;
    public final Setting<KillAuraPlusModes> mode = this.sgGeneral
        .add(new Builder<KillAuraPlusModes>().name("mode").description("KillAura mode.").defaultValue(KillAuraPlusModes.Matrix)
                .onModuleActivated(modesSetting -> this.onModeChanged((KillAuraPlusModes) modesSetting.get()))
                .onChanged(this::onModeChanged)
                .build()
        );

    public KillAuraPlus() {
        super(Compassion.AMRITA, "KillAuraV3", "Better killaura.");
    }

    private void onModeChanged(KillAuraPlusModes mode) {
        switch (mode) {
            case Matrix:
                this.currentMode = new Matrix();
        }
    }

    @EventHandler
    private void onTickPre(Pre event) {
        this.currentMode.onTickPre(event);
    }

    @EventHandler
    private void onTickPost(Post event) {
        this.currentMode.onTickPost(event);
    }

    @EventHandler
    private void onSendPacket(Send event) {
        this.currentMode.onSendPacket(event);
    }

    public void onDeactivate() {
        this.currentMode.onDeactivate();
    }

    public void onActivate() {
        this.currentMode.onActivate();
    }

    public String getInfoString() {
        return this.currentMode.getInfoString();
    }
}
