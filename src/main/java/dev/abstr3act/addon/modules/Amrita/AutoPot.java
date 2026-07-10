package dev.abstr3act.addon.modules.Amrita;

import dev.abstr3act.addon.Compassion;
import dev.abstr3act.addon.module.AmritaModule;
import meteordevelopment.meteorclient.events.entity.player.ItemUseCrosshairTargetEvent;
import meteordevelopment.meteorclient.events.world.TickEvent.Pre;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.StatusEffectListSetting.Builder;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.combat.AnchorAura;
import meteordevelopment.meteorclient.systems.modules.combat.BedAura;
import meteordevelopment.meteorclient.systems.modules.combat.CrystalAura;
import meteordevelopment.meteorclient.systems.modules.combat.KillAura;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.player.Rotations;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;

import java.util.ArrayList;
import java.util.List;

public class AutoPot extends AmritaModule {
    private static final Class<? extends Module>[] AURAS = new Class[]{KillAura.class, CrystalAura.class, AnchorAura.class, BedAura.class};
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
    private final Setting<List<StatusEffect>> usablePotions = this.sgGeneral
        .add(
            ((Builder) ((Builder) new Builder().name("potions-to-use")).description("The potions to use."))
                .defaultValue(new StatusEffect[]{(StatusEffect) StatusEffects.INSTANT_HEALTH.value(), (StatusEffect) StatusEffects.STRENGTH.value()})
                .build()
        );
    private final Setting<Boolean> useSplashPots = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("splash-potions"))
                .description("Allow the use of splash potions"))
                .defaultValue(true))
                .build()
        );
    private final Setting<Integer> health = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("health"))
                .description("If health goes below this point, Healing potions will trigger."))
                .defaultValue(15))
                .min(0)
                .sliderMax(20)
                .build()
        );
    private final Setting<Boolean> pauseAuras = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("pause-auras"))
                .description("Pauses all auras when eating."))
                .defaultValue(true))
                .build()
        );
    private final Setting<Boolean> pauseBaritone = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("pause-baritone"))
                .description("Pause baritone when eating."))
                .defaultValue(true))
                .build()
        );
    private final Setting<Boolean> lookDown = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("rotate"))
                .description("Forces you to rotate downwards when throwing splash potions."))
                .defaultValue(true))
                .build()
        );
    private final List<Class<? extends Module>> wasAura = new ArrayList<>();
    private int slot;
    private int prevSlot;
    private boolean drinking;
    private boolean splashing;
    private boolean wasBaritone;

    public AutoPot() {
        super(Compassion.AMRITA, "AutoPot", "Automatically Drinks Potions");
    }

    public void onDeactivate() {
        this.stopPotionUsage();
    }

    @EventHandler
    private void onTick(Pre event) {
        if (!this.mc.player.isUsingItem()) {
            for (StatusEffect statusEffect : this.usablePotions.get()) {
                RegistryEntry<StatusEffect> registryEntry = Registries.STATUS_EFFECT.getEntry(statusEffect);
                if (!this.mc.player.hasStatusEffect(registryEntry)) {
                    this.slot = this.potionSlot(statusEffect);
                    if (this.slot != -1) {
                        if (registryEntry == StatusEffects.INSTANT_HEALTH && this.ShouldDrinkHealth()) {
                            this.startPotionUse();
                            return;
                        }

                        if (registryEntry == StatusEffects.INSTANT_HEALTH) {
                            return;
                        }

                        this.startPotionUse();
                    }
                }
            }
        }
    }

    @EventHandler
    private void onItemUseCrosshairTarget(ItemUseCrosshairTargetEvent event) {
        if (this.drinking) {
            event.target = null;
        }
    }

    private void setPressed(boolean pressed) {
        this.mc.options.useKey.setPressed(pressed);
    }

    private void drink() {
        this.changeSlot(this.slot);
        this.setPressed(true);
        if (!this.mc.player.isUsingItem()) {
            Utils.rightClick();
        }

        this.drinking = true;
    }

    private void splash() {
        this.changeSlot(this.slot);
        this.setPressed(true);
        this.splashing = true;
    }

    private void stopPotionUsage() {
        this.changeSlot(this.prevSlot);
        this.setPressed(false);
        this.drinking = false;
        this.splashing = false;
        if (this.pauseAuras.get()) {
            for (Class<? extends Module> klass : AURAS) {
                Module module = Modules.get().get(klass);
                if (this.wasAura.contains(klass) && !module.isActive()) {
                    module.toggle();
                }
            }
        }
    }

    private double trueHealth() {
        assert this.mc.player != null;

        return this.mc.player.getHealth();
    }

    private void changeSlot(int slot) {
        this.mc.player.getInventory().selectedSlot = slot;
        this.slot = slot;
    }

    private int potionSlot(StatusEffect statusEffect) {
        int slot = -1;

        for (int i = 0; i < 9; i++) {
            ItemStack stack = this.mc.player.getInventory().getStack(i);
            if (!stack.isEmpty() && (stack.getItem() == Items.POTION || stack.getItem() == Items.SPLASH_POTION && this.useSplashPots.get())) {
                PotionContentsComponent effects = (PotionContentsComponent) stack.getComponents()
                    .getOrDefault(DataComponentTypes.POTION_CONTENTS, PotionContentsComponent.DEFAULT);

                for (StatusEffectInstance effectInstance : effects.getEffects()) {
                    if (effectInstance.getTranslationKey().equals(statusEffect.getTranslationKey())) {
                        slot = i;
                        break;
                    }
                }
            }
        }

        return slot;
    }

    private void startPotionUse() {
        this.prevSlot = this.mc.player.getInventory().selectedSlot;
        if (this.useSplashPots.get()) {
            if (this.lookDown.get()) {
                Rotations.rotate(this.mc.player.getYaw(), 90.0);
                this.splash();
            } else {
                this.splash();
            }
        } else {
            this.drink();
        }

        this.wasAura.clear();
        if (this.pauseAuras.get()) {
            for (Class<? extends Module> klass : AURAS) {
                Module module = Modules.get().get(klass);
                if (module.isActive()) {
                    this.wasAura.add(klass);
                    module.toggle();
                }
            }
        }

        this.wasBaritone = false;
    }

    private boolean ShouldDrinkHealth() {
        return this.trueHealth() < (this.health.get()).intValue();
    }
}
