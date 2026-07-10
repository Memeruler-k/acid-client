package dev.abstr3act.addon.modules.Compassion;

import dev.abstr3act.addon.Compassion;
import dev.abstr3act.addon.events.EventPlayerUpdate;
import dev.abstr3act.addon.events.EventUpdate;
import dev.abstr3act.addon.events.madcat.RotateEvent;
import dev.abstr3act.addon.events.madcat.SprintEvent;
import dev.abstr3act.addon.module.CompassionModule;
import dev.abstr3act.addon.utils.seraphim.PlayerUtils;
import meteordevelopment.meteorclient.events.packets.PacketEvent.Send;
import meteordevelopment.meteorclient.mixininterface.IPlayerInteractEntityC2SPacket;
import meteordevelopment.meteorclient.settings.EnumSetting.Builder;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.movement.NoSlow;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Blocks;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket.InteractType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

public class AutoSprint extends CompassionModule {
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
    public final Setting<Boolean> sprint = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("KeepSprint"))
                .description("."))
                .defaultValue(true))
                .build()
        );
    public final Setting<Double> motion = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("KeepSprint"))
                .description("."))
                .defaultValue(1.0)
                .sliderRange(0.0, 1.0)
                .visible(this.sprint::get))
                .build()
        );
    public final Setting<Boolean> stopWhileUsing = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("StopWhileUsing"))
                .description("."))
                .defaultValue(false))
                .build()
        );
    private final Setting<Mode> mode = this.sgGeneral
        .add(((Builder) ((Builder) ((Builder) new Builder().name("Mode")).description(".")).defaultValue(Mode.Grim)).build());
    private final Setting<Boolean> unsprintOnHit = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("unsprint-on-hit"))
                .description("Whether to stop sprinting when attacking, to ensure you get crits and sweep attacks."))
                .defaultValue(false))
                .build()
        );
    private final Setting<Boolean> unsprintPredict = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("UnSprintPredict"))
                .description("Whether to stop sprinting when attacking, to ensure you get crits and sweep attacks."))
                .defaultValue(false))
                .build()
        );

    public AutoSprint() {
        super(Compassion.COMPASSION, "AutoSprint", ".");
    }

    @EventHandler
    public void onUpdate2(EventPlayerUpdate event) {
        if (((Mode) this.mode.get()).equals(Mode.Normal)) {
            if (!this.autoCrit()) {
                this.mc
                    .player
                    .setSprinting(
                        this.mc.player.getHungerManager().getFoodLevel() > 6
                            && !this.mc.player.horizontalCollision
                            && this.mc.player.input.movementForward > 0.0F
                            && (!this.mc.player.isSneaking() || ((NoSlow) Modules.get().get(NoSlow.class)).isActive())
                            && (!this.mc.player.isUsingItem() || !this.stopWhileUsing.get())
                    );
            } else {
                this.mc.player.setSprinting(false);
            }
        }
    }

    private boolean autoCrit() {
        boolean reasonForSkipCrit = this.mc.player.getAbilities().flying
            || this.mc.player.isFallFlying()
            || this.mc.player.hasStatusEffect(StatusEffects.BLINDNESS)
            || this.mc.player.isHoldingOntoLadder()
            || this.mc.world.getBlockState(BlockPos.ofFloored(this.mc.player.getPos())).getBlock() == Blocks.COBWEB;
        if (this.mc.player.fallDistance > 1.0F && this.mc.player.fallDistance < 1.14F) {
            return false;
        } else if (this.mc.player.isInLava()) {
            return true;
        } else {
            return reasonForSkipCrit ? true : !this.mc.player.isOnGround() && this.mc.player.fallDistance > 0.0F;
        }
    }

    @EventHandler
    private void onPacketSend(Send event) {
        if (!((Mode) this.mode.get()).equals(Mode.Normal)) {
            if (this.unsprintOnHit.get() && event.packet instanceof IPlayerInteractEntityC2SPacket packet && packet.getType() == InteractType.ATTACK) {
                this.mc
                    .getNetworkHandler()
                    .sendPacket(new ClientCommandC2SPacket(this.mc.player, ClientCommandC2SPacket.Mode.STOP_SPRINTING));
                this.mc.player.setSprinting(false);
            }
        }
    }

    @EventHandler
    public void onUpdate(EventUpdate event) {
        if (!((Mode) this.mode.get()).equals(Mode.Normal)) {
            if (this.mode.get() == Mode.PressKey) {
                this.mc.options.sprintKey.setPressed(true);
            } else {
                this.mc.player.setSprinting(this.shouldSprint());
            }
        }
    }

    @EventHandler
    public void sprint(SprintEvent event) {
        event.cancel();
        event.setSprint(this.shouldSprint());
    }

    private boolean shouldSprint() {
        if ((this.mc.player.getHungerManager().getFoodLevel() > 6 || this.mc.player.isCreative())
            && PlayerUtils.isMoving()
            && !this.mc.player.isSneaking()
            && !this.mc.player.isRiding()
            && !this.mc.player.isHoldingOntoLadder()
            && !this.mc.player.hasStatusEffect(StatusEffects.BLINDNESS)) {
            switch ((Mode) this.mode.get()) {
                case Rage:
                    return true;
                case Grim:
                    if (MoveFix.INSTANCE.isActive()) {
                        return this.mc.player.input.movementForward == 1.0F;
                    }

                    return this.mc.options.forwardKey.isPressed() && MathHelper.angleBetween(this.mc.player.getYaw(), Compassion.ROTATION.rotationYaw) < 40.0F;
                case Rotation:
                    if (MoveFix.INSTANCE.isActive()) {
                        return this.mc.player.input.movementForward == 1.0F;
                    }

                    return MathHelper.angleBetween(this.getSprintYaw(this.mc.player.getYaw()), Compassion.ROTATION.rotationYaw) < 40.0F;
            }
        }

        return false;
    }

    public float getSprintYaw(float yaw) {
        if (this.mc.options.forwardKey.isPressed() && !this.mc.options.backKey.isPressed()) {
            if (this.mc.options.leftKey.isPressed() && !this.mc.options.rightKey.isPressed()) {
                yaw -= 45.0F;
            } else if (this.mc.options.rightKey.isPressed() && !this.mc.options.leftKey.isPressed()) {
                yaw += 45.0F;
            }
        } else if (this.mc.options.backKey.isPressed() && !this.mc.options.forwardKey.isPressed()) {
            yaw += 180.0F;
            if (this.mc.options.leftKey.isPressed() && !this.mc.options.rightKey.isPressed()) {
                yaw += 45.0F;
            } else if (this.mc.options.rightKey.isPressed() && !this.mc.options.leftKey.isPressed()) {
                yaw -= 45.0F;
            }
        } else if (this.mc.options.leftKey.isPressed() && !this.mc.options.rightKey.isPressed()) {
            yaw -= 90.0F;
        } else if (this.mc.options.rightKey.isPressed() && !this.mc.options.leftKey.isPressed()) {
            yaw += 90.0F;
        }

        return MathHelper.wrapDegrees(yaw);
    }

    @EventHandler(
        priority = -100
    )
    public void rotate(RotateEvent event) {
        if (!((Mode) this.mode.get()).equals(Mode.Normal)) {
            if ((this.mc.player.getHungerManager().getFoodLevel() > 6 || this.mc.player.isCreative())
                && PlayerUtils.isMoving()
                && !this.mc.player.isFallFlying()
                && !this.mc.player.isSneaking()
                && !this.mc.player.isRiding()
                && !this.mc.player.isTouchingWater()
                && !this.mc.player.isInLava()
                && !this.mc.player.isHoldingOntoLadder()
                && !this.mc.player.hasStatusEffect(StatusEffects.BLINDNESS)
                && ((Mode) this.mode.get()).equals(Mode.Rotation)
                && !event.isModified()) {
                event.setYaw(this.getSprintYaw(this.mc.player.getYaw()));
            }
        }
    }

    public static enum Mode {
        PressKey,
        Rage,
        Grim,
        Rotation,
        Normal;
    }
}
