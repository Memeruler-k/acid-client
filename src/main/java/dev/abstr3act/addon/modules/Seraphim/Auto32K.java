package dev.abstr3act.addon.modules.Seraphim;

import dev.abstr3act.addon.Compassion;
import dev.abstr3act.addon.module.SeraphimModule;
import dev.abstr3act.addon.utils.TargetUtils;
import dev.abstr3act.addon.utils.abnormally.MSTimer;
import meteordevelopment.meteorclient.events.world.TickEvent.Pre;
import meteordevelopment.meteorclient.settings.EntityTypeListSetting.Builder;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.combat.Criticals;
import meteordevelopment.meteorclient.utils.entity.SortPriority;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.util.Hand;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class Auto32K extends SeraphimModule {
    private SettingGroup sgGeneral = this.settings.getDefaultGroup();
    private final Setting<Set<EntityType<?>>> targetTypes = this.sgGeneral
        .add(
            ((Builder) ((Builder) new Builder().name("target-type")).description("Entities to attack."))
                .onlyAttackable()
                .defaultValue(new EntityType[]{EntityType.PLAYER})
                .build()
        );
    private final Setting<SortPriority> priority = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) new meteordevelopment.meteorclient.settings.EnumSetting.Builder()
                .name("target-priority"))
                .description("Entity sorting priority"))
                .defaultValue(SortPriority.LowestDistance))
                .build()
        );
    private final Setting<Double> range = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("Range"))
                .description("range."))
                .defaultValue(3.0)
                .sliderMax(1.0)
                .sliderMin(10.0)
                .build()
        );
    private final Setting<Boolean> swingHand = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("swing-hand"))
                .description("Whether swing hand when attacking."))
                .defaultValue(false))
                .build()
        );
    private List<Entity> targets = new ArrayList<>();
    private MSTimer delayTimer = new MSTimer();
    private Setting<Boolean> useCooldown = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("use-cooldown"))
                .description("Whether use the item cooldown to determine when attack."))
                .defaultValue(true))
                .build()
        );
    private final Setting<Double> useCooldownBaseTime = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("use-cooldown-base-time"))
                .description("The base time for using cooldown."))
                .defaultValue(0.75)
                .sliderMax(1.0)
                .sliderMin(0.1)
                .visible(() -> this.useCooldown.get()))
                .build()
        );
    private final Setting<Integer> attackDelay = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("attack-delay"))
                .description("attack delay."))
                .defaultValue(50))
                .sliderMax(2000)
                .sliderMin(1)
                .visible(() -> !this.useCooldown.get()))
                .build()
        );

    public Auto32K() {
        super(Compassion.SERAPHIM, "Auto32K", "Kill Aura.");
    }

    private static boolean isSword(Item item) {
        return item == Items.WOODEN_SWORD
            || item == Items.STONE_SWORD
            || item == Items.IRON_SWORD
            || item == Items.GOLDEN_SWORD
            || item == Items.DIAMOND_SWORD
            || item == Items.NETHERITE_SWORD;
    }

    public void onActivate() {
        this.targets.clear();
    }

    public void onDeactivate() {
        this.targets.clear();
    }

    private boolean isReadyToAttack() {
        return this.useCooldown.get()
            ? this.mc.player.getAttackCooldownProgress(-1.0F) >= this.useCooldownBaseTime.get()
            : this.delayTimer.hasPassTime(this.attackDelay.get());
    }

    @EventHandler
    public void onTick(Pre event) {
        this.updateTarget();
        this.doAura();
    }

    private final boolean getCriticals() {
        return ((Criticals) Modules.get().get(Criticals.class)).isActive();
    }

    private void updateTarget() {
        List<Entity> entities = new ArrayList<>();
        TargetUtils.getList(entities, this::_targetCheck, (SortPriority) this.priority.get(), 1);
        this.targets.clear();
        this.targets.addAll(entities);
    }

    private boolean _targetCheck(Entity t) {
        if (!((Set) this.targetTypes.get()).contains(t.getType())) {
            return false;
        } else if (!(t instanceof LivingEntity) || !((LivingEntity) t).isDead() && !(((LivingEntity) t).getHealth() <= 0.0F) && ((LivingEntity) t).deathTime <= 0) {
            if (t instanceof PlayerEntity p) {
                if (((PlayerEntity) t).isDead() || ((PlayerEntity) t).getHealth() <= 0.0F || ((PlayerEntity) t).deathTime > 0) {
                    return false;
                } else if (p.isCreative()) {
                    return false;
                } else {
                    return p == this.mc.player ? false : Friends.get().shouldAttack(p);
                }
            } else {
                return true;
            }
        } else {
            return false;
        }
    }

    public int getSwordSlot() {
        if (this.mc.player == null) {
            return -1;
        } else {
            for (int slot = 0; slot < 9; slot++) {
                ItemStack stack = this.mc.player.getInventory().getStack(slot);
                if (isSword(stack.getItem())) {
                    return slot;
                }
            }

            return -1;
        }
    }

    public int getNonSwordSlot() {
        if (this.mc.player == null) {
            return -1;
        } else {
            for (int slot = 0; slot < 9; slot++) {
                ItemStack stack = this.mc.player.getInventory().getStack(slot);
                if (!isSword(stack.getItem())) {
                    return slot;
                }
            }

            return -1;
        }
    }

    private void doAura() {
        if (!this.targets.isEmpty()) {
            if (this.isReadyToAttack()) {
                if (!(this.mc.player.distanceTo(this.targets.getFirst()) > this.range.get())) {
                    int n = Math.min(1, this.targets.size());

                    for (int i = 0; i < n; i++) {
                        this.attack(this.targets.get(i));
                        if (this.swingHand.get()) {
                            this.mc.player.swingHand(Hand.MAIN_HAND);
                        }
                    }
                }
            }
        }
    }

    private void attack(Entity entity) {
        if (this.getNonSwordSlot() != -1) {
            this.sendPacket(new UpdateSelectedSlotC2SPacket(this.getNonSwordSlot()));
            this.mc.interactionManager.attackEntity(this.mc.player, entity);
        }

        if (this.getSwordSlot() != -1) {
            this.sendPacket(new UpdateSelectedSlotC2SPacket(this.getSwordSlot()));
            this.mc.interactionManager.attackEntity(this.mc.player, entity);
            this.mc.interactionManager.attackEntity(this.mc.player, entity);
        }

        this.sendPacket(new UpdateSelectedSlotC2SPacket(this.mc.player.getInventory().selectedSlot));
    }
}
