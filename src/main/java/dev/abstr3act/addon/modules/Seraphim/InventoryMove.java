package dev.abstr3act.addon.modules.Seraphim;

import dev.abstr3act.addon.Compassion;
import dev.abstr3act.addon.events.EventClickSlot;
import dev.abstr3act.addon.events.EventPlayerUpdate;
import dev.abstr3act.addon.module.SeraphimModule;
import dev.abstr3act.addon.utils.MovementUtility;
import meteordevelopment.meteorclient.events.packets.PacketEvent.Send;
import meteordevelopment.meteorclient.events.packets.PacketEvent.Sent;
import meteordevelopment.meteorclient.settings.EnumSetting.Builder;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket.Mode;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket.PositionAndOnGround;
import net.minecraft.screen.slot.SlotActionType;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;

public class InventoryMove extends SeraphimModule {
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
    private final Setting<Bypass> clickBypass = this.sgGeneral
        .add(((Builder) ((Builder) ((Builder) new Builder().name("Bypass")).description(".")).defaultValue(Bypass.None)).build());
    private final Setting<Boolean> rotateOnArrows = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("RotateOnArrows"))
                .description("."))
                .defaultValue(true))
                .build()
        );
    private final Setting<Boolean> sneak = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("Sneak"))
                .description("."))
                .defaultValue(false))
                .build()
        );
    private final Queue<ClickSlotC2SPacket> storedClicks = new LinkedList<>();
    private AtomicBoolean pause = new AtomicBoolean();

    public InventoryMove() {
        super(Compassion.SERAPHIM, "InventoryMove", ".");
    }

    @EventHandler
    public void onUpdate(EventPlayerUpdate event) {
        if (this.mc.currentScreen != null && !(this.mc.currentScreen instanceof ChatScreen)) {
            for (KeyBinding k : new KeyBinding[]{
                this.mc.options.forwardKey,
                this.mc.options.backKey,
                this.mc.options.leftKey,
                this.mc.options.rightKey,
                this.mc.options.jumpKey,
                this.mc.options.sprintKey
            }) {
                k.setPressed(this.isKeyPressed(InputUtil.fromTranslationKey(k.getBoundKeyTranslationKey()).getCode()));
            }

            float deltaX = 0.0F;
            float deltaY = 0.0F;
            if (this.rotateOnArrows.get()) {
                if (this.isKeyPressed(264)) {
                    deltaY += 30.0F;
                }

                if (this.isKeyPressed(265)) {
                    deltaY -= 30.0F;
                }

                if (this.isKeyPressed(262)) {
                    deltaX += 30.0F;
                }

                if (this.isKeyPressed(263)) {
                    deltaX -= 30.0F;
                }

                if (deltaX != 0.0F || deltaY != 0.0F) {
                    this.mc.player.changeLookDirection(deltaX, deltaY);
                }
            }

            if (this.sneak.get()) {
                this.mc.options.sneakKey.setPressed(this.isKeyPressed(InputUtil.fromTranslationKey(this.mc.options.sneakKey.getBoundKeyTranslationKey()).getCode()));
            }
        }
    }

    @EventHandler
    public void onClickSlot(EventClickSlot e) {
        if (((Bypass) this.clickBypass.get()).equals(Bypass.DisableClicks)
            && (MovementUtility.isMoving() || this.mc.options.jumpKey.isPressed())) {
            e.cancel();
        }
    }

    @EventHandler
    public void onPacketSend(Send e) {
        if (MovementUtility.isMoving() && this.mc.options.jumpKey.isPressed() && !this.pause.get()) {
            if (e.packet instanceof ClickSlotC2SPacket click) {
                switch ((Bypass) this.clickBypass.get()) {
                    case StrictNCP:
                        if (this.mc.player.isOnGround()
                            && !this.mc.world.getBlockCollisions(this.mc.player, this.mc.player.getBoundingBox().offset(0.0, 0.0656, 0.0)).iterator().hasNext()) {
                            if (this.mc.player.isSprinting()) {
                                this.sendPacket(new ClientCommandC2SPacket(this.mc.player, Mode.STOP_SPRINTING));
                            }

                            this.sendPacket(new PositionAndOnGround(this.mc.player.getX(), this.mc.player.getY() + 0.0656, this.mc.player.getZ(), false));
                        }
                        break;
                    case GrimSwap:
                        if (click.getActionType() != SlotActionType.PICKUP && click.getActionType() != SlotActionType.PICKUP_ALL) {
                            this.sendPacket(new CloseHandledScreenC2SPacket(0));
                        }
                        break;
                    case MatrixNcp:
                        this.sendPacket(new ClientCommandC2SPacket(this.mc.player, Mode.STOP_SPRINTING));
                        this.mc.options.forwardKey.setPressed(false);
                        this.mc.player.input.movementForward = 0.0F;
                        this.mc.player.input.pressingForward = false;
                        break;
                    case Delay:
                        this.storedClicks.add(click);
                        e.cancel();
                        break;
                    case StrictNCP2:
                        if (this.mc.player.isOnGround()
                            && !this.mc.world.getBlockCollisions(this.mc.player, this.mc.player.getBoundingBox().offset(0.0, 2.71875E-7, 0.0)).iterator().hasNext()) {
                            if (this.mc.player.isSprinting()) {
                                this.sendPacket(new ClientCommandC2SPacket(this.mc.player, Mode.STOP_SPRINTING));
                            }

                            this.sendPacket(new PositionAndOnGround(this.mc.player.getX(), this.mc.player.getY() + 2.71875E-7, this.mc.player.getZ(), false));
                        }
                }
            }

            if (e.packet instanceof CloseHandledScreenC2SPacket && ((Bypass) this.clickBypass.get()).equals(Bypass.Delay)) {
                this.pause.set(true);

                while (!this.storedClicks.isEmpty()) {
                    this.sendPacket((Packet<?>) this.storedClicks.poll());
                }

                this.pause.set(false);
            }
        }
    }

    @EventHandler
    public void onPacketSendPost(Sent e) {
        if (e.packet instanceof ClickSlotC2SPacket
            && this.mc.player.isSprinting()
            && ((Bypass) this.clickBypass.get()).equals(Bypass.StrictNCP)) {
            this.sendPacket(new ClientCommandC2SPacket(this.mc.player, Mode.START_SPRINTING));
        }
    }

    private static enum Bypass {
        DisableClicks,
        None,
        StrictNCP,
        GrimSwap,
        MatrixNcp,
        Delay,
        StrictNCP2;
    }
}
