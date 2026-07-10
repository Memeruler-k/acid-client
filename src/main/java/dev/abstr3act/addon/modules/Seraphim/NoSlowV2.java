package dev.abstr3act.addon.modules.Seraphim;

import dev.abstr3act.addon.Compassion;
import dev.abstr3act.addon.events.EventKeyboardInput;
import dev.abstr3act.addon.events.EventUpdate;
import dev.abstr3act.addon.module.AmritaModule;
import meteordevelopment.meteorclient.settings.EnumSetting.Builder;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket.Action;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Direction;

public class NoSlowV2 extends AmritaModule {
    private final SettingGroup sgGeneral = this.settings.createGroup("General");
    public final Setting<Mode> mode = this.sgGeneral
        .add(((Builder) ((Builder) ((Builder) new Builder().name("Mode")).description("The mode of operation.")).defaultValue(Mode.NCP)).build());
    private final SettingGroup sgSelection = this.settings.createGroup("Selection");
    public final Setting<Boolean> soulSand = this.sgSelection
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("SoulSand"))
                .description("Affect soul sand movement."))
                .defaultValue(true))
                .build()
        );
    public final Setting<Boolean> honey = this.sgSelection
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("Honey"))
                .description("Affect honey block movement."))
                .defaultValue(true))
                .build()
        );
    public final Setting<Boolean> slime = this.sgSelection
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("Slime"))
                .description("Affect slime block movement."))
                .defaultValue(true))
                .build()
        );
    public final Setting<Boolean> ice = this.sgSelection
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("Ice"))
                .description("Affect ice movement."))
                .defaultValue(true))
                .build()
        );
    public final Setting<Boolean> sweetBerryBush = this.sgSelection
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("SweetBerryBush"))
                .description("Affect movement through sweet berry bushes."))
                .defaultValue(true))
                .build()
        );
    public final Setting<Boolean> sneak = this.sgSelection
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("Sneak"))
                .description("Enable sneaking behavior."))
                .defaultValue(false))
                .build()
        );
    public final Setting<Boolean> crawl = this.sgSelection
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("Crawl"))
                .description("Enable crawling behavior."))
                .defaultValue(false))
                .build()
        );
    private final Setting<Boolean> mainHand = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("MainHand"))
                .description("Whether to use the main hand."))
                .defaultValue(true))
                .build()
        );
    private final Setting<Boolean> food = this.sgSelection
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("Food"))
                .description("Allow food usage."))
                .defaultValue(true))
                .build()
        );
    private final Setting<Boolean> projectiles = this.sgSelection
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("Projectiles"))
                .description("Allow projectiles."))
                .defaultValue(true))
                .build()
        );
    private final Setting<Boolean> shield = this.sgSelection
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("Shield"))
                .description("Enable shield usage."))
                .defaultValue(true))
                .build()
        );
    private boolean returnSneak;

    public NoSlowV2() {
        super(Compassion.AMRITA, "NoSlowV2", "");
    }

    @EventHandler
    public void onUpdate(EventUpdate eventUpdate) {
        if (this.returnSneak) {
            this.mc.options.sneakKey.setPressed(false);
            this.mc.player.setSprinting(true);
            this.returnSneak = false;
        }

        if (this.mc.player.isUsingItem() && !this.mc.player.isRiding() && !this.mc.player.isFallFlying()) {
            switch ((Mode) this.mode.get()) {
                case StrictNCP:
                    this.sendPacket(new UpdateSelectedSlotC2SPacket(this.mc.player.getInventory().selectedSlot));
                    break;
                case Matrix:
                    if (this.mc.player.isOnGround() && !this.mc.options.jumpKey.isPressed()) {
                        this.mc.player.setVelocity(this.mc.player.getVelocity().x * 0.3, this.mc.player.getVelocity().y, this.mc.player.getVelocity().z * 0.3);
                    } else if (this.mc.player.fallDistance > 0.2F) {
                        this.mc.player.setVelocity(this.mc.player.getVelocity().x * 0.95F, this.mc.player.getVelocity().y, this.mc.player.getVelocity().z * 0.95F);
                    }
                    break;
                case Grim:
                    if (this.mc.player.getActiveHand() == Hand.OFF_HAND) {
                        this.sendPacket(new UpdateSelectedSlotC2SPacket(this.mc.player.getInventory().selectedSlot % 8 + 1));
                        this.sendPacket(new UpdateSelectedSlotC2SPacket(this.mc.player.getInventory().selectedSlot));
                    } else if (this.mainHand.get()) {
                        this.sendSequencedPacket(id -> new PlayerInteractItemC2SPacket(Hand.OFF_HAND, id, this.mc.player.getYaw(), this.mc.player.getPitch()));
                    }
                    break;
                case MusteryGrief:
                    if (this.mc.player.isOnGround() && this.mc.options.jumpKey.isPressed()) {
                        this.mc.options.sneakKey.setPressed(true);
                        this.returnSneak = true;
                    }
                    break;
                case GrimNew:
                    if (this.mc.player.getActiveHand() == Hand.OFF_HAND) {
                        this.sendPacket(new UpdateSelectedSlotC2SPacket(this.mc.player.getInventory().selectedSlot % 8 + 1));
                        this.sendPacket(new UpdateSelectedSlotC2SPacket(this.mc.player.getInventory().selectedSlot));
                    } else if (this.mainHand.get() && (this.mc.player.getItemUseTime() <= 3 || this.mc.player.age % 2 == 0)) {
                        this.sendSequencedPacket(id -> new PlayerInteractItemC2SPacket(Hand.OFF_HAND, id, this.mc.player.getYaw(), this.mc.player.getPitch()));
                    }
                    break;
                case Matrix2:
                    if (this.mc.player.isOnGround()) {
                        if (this.mc.player.age % 2 == 0) {
                            this.mc.player.setVelocity(this.mc.player.getVelocity().x * 0.5, this.mc.player.getVelocity().y, this.mc.player.getVelocity().z * 0.5);
                        } else {
                            this.mc.player.setVelocity(this.mc.player.getVelocity().x * 0.95F, this.mc.player.getVelocity().y, this.mc.player.getVelocity().z * 0.95F);
                        }
                    }
                    break;
                case LFCraft:
                    if (this.mc.player.getItemUseTime() <= 3) {
                        this.sendSequencedPacket(id -> new PlayerActionC2SPacket(Action.ABORT_DESTROY_BLOCK, this.mc.player.getBlockPos().up(), Direction.NORTH, id));
                    }
            }
        }
    }

    @EventHandler
    public void onKeyboardInput(EventKeyboardInput e) {
        if (this.mode.get() == Mode.Matrix3 && this.mc.player.isUsingItem() && !this.mc.player.isFallFlying()) {
            this.mc.player.input.movementForward *= 5.0F;
            this.mc.player.input.movementSideways *= 5.0F;
            float mult = 1.0F;
            if (this.mc.player.isOnGround()) {
                if (this.mc.player.input.movementForward != 0.0F && this.mc.player.input.movementSideways != 0.0F) {
                    this.mc.player.input.movementForward *= 0.35F;
                    this.mc.player.input.movementSideways *= 0.35F;
                } else {
                    this.mc.player.input.movementForward *= 0.5F;
                    this.mc.player.input.movementSideways *= 0.5F;
                }
            } else if (this.mc.player.input.movementForward != 0.0F && this.mc.player.input.movementSideways != 0.0F) {
                mult = 0.47F;
            } else {
                mult = 0.67F;
            }

            this.mc.player.input.movementForward *= mult;
            this.mc.player.input.movementSideways *= mult;
        }
    }

    public boolean canNoSlow() {
        if (this.mode.get() == Mode.Matrix3) {
            return false;
        } else if (!this.food.get() && this.mc.player.getActiveItem().getComponents().contains(DataComponentTypes.FOOD)) {
            return false;
        } else if (!this.shield.get() && this.mc.player.getActiveItem().getItem() == Items.SHIELD) {
            return false;
        } else if (this.projectiles.get()
            || this.mc.player.getActiveItem().getItem() != Items.CROSSBOW
            && this.mc.player.getActiveItem().getItem() != Items.BOW
            && this.mc.player.getActiveItem().getItem() != Items.TRIDENT) {
            if (this.mode.get() == Mode.MusteryGrief && this.mc.player.isOnGround() && !this.mc.options.jumpKey.isPressed()) {
                return false;
            } else {
                return !this.mainHand.get() && this.mc.player.getActiveHand() == Hand.MAIN_HAND
                    ? false
                    : !this.mc.player.getOffHandStack().getComponents().contains(DataComponentTypes.FOOD) && this.mc.player.getOffHandStack().getItem() != Items.SHIELD
                    || this.mode.get() != Mode.GrimNew && this.mode.get() != Mode.Grim
                    || this.mc.player.getActiveHand() != Hand.MAIN_HAND;
            }
        } else {
            return false;
        }
    }

    public static enum Mode {
        NCP,
        StrictNCP,
        Matrix,
        Grim,
        MusteryGrief,
        GrimNew,
        Matrix2,
        LFCraft,
        Matrix3;
    }
}
