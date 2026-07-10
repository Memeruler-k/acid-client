package dev.abstr3act.addon.modules.Seraphim;

import dev.abstr3act.addon.Compassion;
import dev.abstr3act.addon.events.EventAfterRotate;
import dev.abstr3act.addon.module.SeraphimModule;
import dev.abstr3act.addon.utils.timers.Timer;
import meteordevelopment.meteorclient.events.world.TickEvent.Post;
import meteordevelopment.meteorclient.settings.BoolSetting.Builder;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SplashPotionItem;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket.Full;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Hand;

public final class AutoBuff extends SeraphimModule {
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
    private final SettingGroup sgHeal = this.settings.createGroup("InstantHealing");
    private final SettingGroup sgRegen = this.settings.createGroup("Regeneration");
    private final Setting<Boolean> strength = this.sgGeneral
        .add(((Builder) ((Builder) ((Builder) new Builder().name("Strength")).description(".")).defaultValue(true)).build());
    private final Setting<Boolean> speed = this.sgGeneral
        .add(((Builder) ((Builder) ((Builder) new Builder().name("Speed")).description(".")).defaultValue(true)).build());
    private final Setting<Boolean> boost = this.sgGeneral
        .add(((Builder) ((Builder) ((Builder) new Builder().name("HealthBoost")).description(".")).defaultValue(true)).build());
    private final Setting<Boolean> fire = this.sgGeneral
        .add(((Builder) ((Builder) ((Builder) new Builder().name("FireResistance")).description(".")).defaultValue(true)).build());
    private final Setting<Boolean> heal = this.sgGeneral
        .add(((Builder) ((Builder) ((Builder) new Builder().name("InstantHealing")).description(".")).defaultValue(true)).build());
    public final Setting<Integer> healthH = this.sgHeal
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("Health"))
                .description("."))
                .defaultValue(8))
                .sliderMax(100)
                .min(0)
                .visible(this.heal::get))
                .build()
        );
    private final Setting<Boolean> regen = this.sgGeneral
        .add(((Builder) ((Builder) ((Builder) new Builder().name("Regeneration")).description(".")).defaultValue(true)).build());
    private final Setting<TriggerOn> triggerOn = this.sgRegen
        .add(
            ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) new meteordevelopment.meteorclient.settings.EnumSetting.Builder()
                .name("Trigger"))
                .description("."))
                .defaultValue(TriggerOn.LackOfRegen))
                .visible(this.regen::get))
                .build()
        );
    public final Setting<Integer> healthR = this.sgRegen
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("HP"))
                .description("."))
                .defaultValue(8))
                .sliderMax(100)
                .min(0)
                .visible(() -> ((TriggerOn) this.triggerOn.get()).equals(TriggerOn.Health) && this.regen.get()))
                .build()
        );
    private final Setting<SwitchMode> switchMode = this.sgRegen
        .add(
            ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) new meteordevelopment.meteorclient.settings.EnumSetting.Builder()
                .name("SwitchMode"))
                .description("."))
                .defaultValue(SwitchMode.InvPick))
                .build()
        );
    private final Setting<Boolean> onDaGround = this.sgGeneral
        .add(((Builder) ((Builder) ((Builder) new Builder().name("OnlyOnGround")).description(".")).defaultValue(true)).build());
    private final Setting<Boolean> legit = this.sgGeneral
        .add(((Builder) ((Builder) ((Builder) new Builder().name("LegitMode")).description(".")).defaultValue(false)).build());
    private final Setting<Boolean> blatantRotation = this.sgGeneral
        .add(((Builder) ((Builder) ((Builder) new Builder().name("BlatantRotation")).description(".")).defaultValue(true)).build());
    private final Setting<Boolean> stopMotion = this.sgGeneral
        .add(((Builder) ((Builder) ((Builder) new Builder().name("StopMotion")).description(".")).defaultValue(true)).build());
    public Timer timer = new Timer();
    private boolean spoofed = false;

    public AutoBuff() {
        super(Compassion.SERAPHIM, "AutoBuff", "Throws potions for you");
    }

    public int getPotionSlot(Potions potion) {
        for (int i = 0; i < 9; i++) {
            if (this.isStackPotion(this.mc.player.getInventory().getStack(i), potion)) {
                return i;
            }
        }

        return -1;
    }

    public int getPotionSlotInventory(Potions potion) {
        for (int i = 0; i < this.mc.player.getInventory().size(); i++) {
            if (this.isStackPotion(this.mc.player.getInventory().getStack(i), potion)) {
                return i;
            }
        }

        return -1;
    }

    public boolean isPotionOnHotBar(Potions potions) {
        return ((SwitchMode) this.switchMode.get()).equals(SwitchMode.InvPick)
            ? this.getPotionSlotInventory(potions) != -1
            : this.getPotionSlot(potions) != -1;
    }

    public boolean isStackPotion(ItemStack stack, Potions potion) {
        if (stack == null) {
            return false;
        } else {
            if (stack.getItem() instanceof SplashPotionItem) {
                PotionContentsComponent potionContentsComponent = (PotionContentsComponent) stack.getOrDefault(
                    DataComponentTypes.POTION_CONTENTS, PotionContentsComponent.DEFAULT
                );
                RegistryEntry<StatusEffect> id = null;
                switch (potion) {
                    case STRENGTH:
                        id = StatusEffects.STRENGTH;
                        break;
                    case SPEED:
                        id = StatusEffects.SPEED;
                        break;
                    case FIRERES:
                        id = StatusEffects.FIRE_RESISTANCE;
                        break;
                    case HEAL:
                        id = StatusEffects.INSTANT_HEALTH;
                        break;
                    case REGEN:
                        id = StatusEffects.REGENERATION;
                        break;
                    case BOOST:
                        id = StatusEffects.HEALTH_BOOST;
                }

                for (StatusEffectInstance effect : potionContentsComponent.getEffects()) {
                    if (effect.getEffectType() == id) {
                        return true;
                    }
                }
            }

            return false;
        }
    }

    @EventHandler
    public void onPostRotationSet(EventAfterRotate event) {
        if (this.mc.player.age > 80 && this.shouldThrow()) {
            this.spoofed = true;
        }
    }

    private boolean shouldThrow() {
        return !this.mc.player.hasStatusEffect(StatusEffects.SPEED) && this.isPotionOnHotBar(Potions.SPEED) && this.speed.get()
            || !this.mc.player.hasStatusEffect(StatusEffects.STRENGTH) && this.isPotionOnHotBar(Potions.STRENGTH) && this.strength.get()
            || !this.mc.player.hasStatusEffect(StatusEffects.FIRE_RESISTANCE) && this.isPotionOnHotBar(Potions.FIRERES) && this.fire.get()
            || this.mc.player.getHealth() + this.mc.player.getAbsorptionAmount() < (this.healthH.get()).intValue()
            && this.isPotionOnHotBar(Potions.HEAL)
            && this.heal.get()
            || !this.mc.player.hasStatusEffect(StatusEffects.REGENERATION)
            && ((TriggerOn) this.triggerOn.get()).equals(TriggerOn.LackOfRegen)
            && this.isPotionOnHotBar(Potions.REGEN)
            && this.regen.get()
            || this.mc.player.getHealth() + this.mc.player.getAbsorptionAmount() < (this.healthR.get()).intValue()
            && ((TriggerOn) this.triggerOn.get()).equals(TriggerOn.Health)
            && this.isPotionOnHotBar(Potions.REGEN)
            && this.regen.get()
            || !this.mc.player.hasStatusEffect(StatusEffects.HEALTH_BOOST) && this.isPotionOnHotBar(Potions.BOOST) && this.boost.get();
    }

    @EventHandler
    public void onPostSync(Post e) {
        if (!this.onDaGround.get() || this.mc.player.isOnGround()) {
            if (this.mc.player.age > 80 && this.shouldThrow() && this.timer.passedMs(1000L) && this.spoofed) {
                if (this.stopMotion.get()) {
                    this.mc.player.setVelocity(0.0, this.mc.player.getVelocity().y, 0.0);
                }

                if (this.blatantRotation.get()) {
                    this.sendSequencedPacket(
                        id -> new Full(this.mc.player.getX(), this.mc.player.getY(), this.mc.player.getZ(), this.mc.player.getYaw(), 90.0F, this.mc.player.isOnGround())
                    );
                }

                if (!this.mc.player.hasStatusEffect(StatusEffects.SPEED) && this.isPotionOnHotBar(Potions.SPEED) && this.speed.get()) {
                    this.throwPotion(Potions.SPEED);
                }

                if (!this.mc.player.hasStatusEffect(StatusEffects.STRENGTH) && this.isPotionOnHotBar(Potions.STRENGTH) && this.strength.get()) {
                    this.throwPotion(Potions.STRENGTH);
                }

                if (!this.mc.player.hasStatusEffect(StatusEffects.FIRE_RESISTANCE) && this.isPotionOnHotBar(Potions.FIRERES) && this.fire.get()) {
                    this.throwPotion(Potions.FIRERES);
                }

                if (!this.mc.player.hasStatusEffect(StatusEffects.HEALTH_BOOST) && this.isPotionOnHotBar(Potions.BOOST) && this.boost.get()) {
                    this.throwPotion(Potions.BOOST);
                }

                if (this.mc.player.getHealth() + this.mc.player.getAbsorptionAmount() < (this.healthH.get()).intValue()
                    && this.heal.get()
                    && this.isPotionOnHotBar(Potions.HEAL)) {
                    this.throwPotion(Potions.HEAL);
                }

                if ((
                    !this.mc.player.hasStatusEffect(StatusEffects.REGENERATION) && ((TriggerOn) this.triggerOn.get()).equals(TriggerOn.LackOfRegen)
                        || this.mc.player.getHealth() + this.mc.player.getAbsorptionAmount() < (this.healthR.get()).intValue()
                        && ((TriggerOn) this.triggerOn.get()).equals(TriggerOn.Health)
                )
                    && this.isPotionOnHotBar(Potions.REGEN)
                    && this.regen.get()) {
                    this.throwPotion(Potions.REGEN);
                }

                if (this.blatantRotation.get()) {
                    this.sendSequencedPacket(
                        id -> new Full(
                            this.mc.player.getX(),
                            this.mc.player.getY(),
                            this.mc.player.getZ(),
                            this.mc.player.getYaw(),
                            this.mc.player.getPitch(),
                            this.mc.player.isOnGround()
                        )
                    );
                }

                this.timer.reset();
                this.spoofed = false;
            }
        }
    }

    public void throwPotion(Potions potion) {
        int slot = -1;
        switch ((SwitchMode) this.switchMode.get()) {
            case Normal:
                InvUtils.swap(this.getPotionSlot(potion), false);
                break;
            case Silent:
                this.sendPacket(new UpdateSelectedSlotC2SPacket(this.getPotionSlot(potion)));
                break;
            case InvPick:
                slot = this.getPotionSlotInventory(potion);
                InvUtils.move().from(this.getPotionSlotInventory(potion)).to(this.mc.player.getInventory().selectedSlot);
        }

        this.sendSequencedPacket(id -> new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, id, this.mc.player.getYaw(), 90.0F));
        if (((SwitchMode) this.switchMode.get()).equals(SwitchMode.InvPick) && slot != -1) {
            InvUtils.move().from(this.mc.player.getInventory().selectedSlot).to(slot);
        } else if (!((SwitchMode) this.switchMode.get()).equals(SwitchMode.Normal)) {
            this.sendPacket(new UpdateSelectedSlotC2SPacket(this.mc.player.getInventory().selectedSlot));
        }
    }

    public static enum Potions {
        STRENGTH,
        SPEED,
        FIRERES,
        HEAL,
        REGEN,
        BOOST;
    }

    public static enum SwitchMode {
        Normal,
        Silent,
        InvPick;
    }

    public static enum TriggerOn {
        LackOfRegen,
        Health;
    }
}
