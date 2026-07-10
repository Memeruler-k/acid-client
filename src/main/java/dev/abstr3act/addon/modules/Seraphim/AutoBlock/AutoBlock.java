package dev.abstr3act.addon.modules.Seraphim.AutoBlock;

import dev.abstr3act.addon.Compassion;
import dev.abstr3act.addon.events.legacy.HandleInputEvent;
import dev.abstr3act.addon.module.SeraphimModule;
import dev.abstr3act.addon.utils.fragment.blinkUtils.PacketUtil;
import meteordevelopment.meteorclient.events.entity.player.AttackEntityEvent;
import meteordevelopment.meteorclient.settings.EnumSetting.Builder;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.item.ShieldItem;
import net.minecraft.item.SwordItem;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.util.Hand;

public class AutoBlock extends SeraphimModule {
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
    private final Setting<Mode> mode = this.sgGeneral
        .add(((Builder) ((Builder) ((Builder) new Builder().name("Mode")).description(".")).defaultValue(Mode.Vanilla)).build());
    private final Setting<Double> extraRange = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("ExtraRange"))
                .description("."))
                .defaultValue(0.5)
                .range(0.0, 10.0)
                .build()
        );
    Entity target;
    private boolean wasBlinking = false;
    private int abTick = 0;
    private boolean wasPacketBlocking = false;

    public AutoBlock() {
        super(Compassion.SERAPHIM, "AutoBlock", "AutoBlock.");
    }

    private double getRealAttackReach() {
        return this.mc.player.getEntityInteractionRange() + this.extraRange.get();
    }

    private void resetPacketUnblocking() {
        if (this.wasPacketBlocking) {
            int oldSlot = this.mc.player.getInventory().selectedSlot;
            if (oldSlot == 0) {
                PacketUtil.sendPacket(new UpdateSelectedSlotC2SPacket(oldSlot + 1));
                PacketUtil.sendPacket(new UpdateSelectedSlotC2SPacket(oldSlot));
            } else {
                PacketUtil.sendPacket(new UpdateSelectedSlotC2SPacket(oldSlot - 1));
                PacketUtil.sendPacket(new UpdateSelectedSlotC2SPacket(oldSlot));
            }

            this.wasPacketBlocking = false;
        }
    }

    public void onDeactivate() {
        if (this.mc.player != null) {
            this.resetPacketUnblocking();
        }

        this.wasPacketBlocking = false;
        this.reset();
    }

    private void reset() {
        if (((Mode) this.mode.get()).equals(Mode.HypixelFull) && this.wasBlinking) {
            BlinkUtil.sync(true, true);
            BlinkUtil.stopBlink();
        }

        this.abTick = 0;
        this.target = null;
        this.wasBlinking = false;
    }

    @EventHandler
    public void onHandleInput(HandleInputEvent event) {
        if (this.mc.player != null && this.mc.world != null) {
            if (this.target != null) {
                if (this.mc.player.distanceTo(this.target) > this.getRealAttackReach()) {
                    switch ((Mode) this.mode.get()) {
                        case Vanilla:
                            this.resetPacketUnblocking();
                            break;
                        case HypixelFull:
                            if (this.wasBlinking) {
                                BlinkUtil.sync(true, true);
                                BlinkUtil.stopBlink();
                                this.wasBlinking = false;
                            }
                    }
                }
            }
        }
    }

    @EventHandler
    private void onAttackEntity(AttackEntityEvent event) {
        this.target = event.entity;
        if (this.mc.player.getMainHandStack().getItem() instanceof SwordItem) {
            this.abTick++;
            switch ((Mode) this.mode.get()) {
                case Vanilla:
                    if (this.mc.player.getOffHandStack().getItem() instanceof ShieldItem) {
                        PacketUtil.sendPacket(new PlayerInteractItemC2SPacket(Hand.OFF_HAND, 0, this.mc.player.getYaw(), this.mc.player.getPitch()));
                    } else if (this.mc.player.getMainHandStack().getItem() instanceof SwordItem && !this.wasPacketBlocking) {
                        PacketUtil.sendPacket(new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, 0, this.mc.player.getYaw(), this.mc.player.getPitch()));
                    }
                    break;
                case HypixelFull:
                    if (this.mc.player.getMainHandStack().getItem() instanceof SwordItem && this.abTick >= 1) {
                        BlinkUtil.doBlink();
                        PacketUtil.sendPacket(new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, 0, this.mc.player.getYaw(), this.mc.player.getPitch()));
                        this.wasBlinking = true;
                        this.abTick = 0;
                    }
            }
        }
    }

    public String getInfoString() {
        return ((Mode) this.mode.get()).name();
    }

    static enum Mode {
        Vanilla,
        HypixelFull;
    }
}
