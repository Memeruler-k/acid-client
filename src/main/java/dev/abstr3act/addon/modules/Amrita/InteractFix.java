package dev.abstr3act.addon.modules.Amrita;

import dev.abstr3act.addon.Compassion;
import dev.abstr3act.addon.module.AmritaModule;
import meteordevelopment.meteorclient.events.packets.PacketEvent.Send;
import meteordevelopment.meteorclient.settings.BoolSetting.Builder;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket.Action;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket.Full;

public class InteractFix extends AmritaModule {
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
    private final Setting<Boolean> interactItem = this.sgGeneral
        .add(((Builder) ((Builder) ((Builder) new Builder().name("InteractItem")).description("")).defaultValue(true)).build());
    private final Setting<Boolean> releaseItem = this.sgGeneral
        .add(((Builder) ((Builder) ((Builder) new Builder().name("ReleaseItem")).description("")).defaultValue(true)).build());
    private final Setting<Boolean> interactBlock = this.sgGeneral
        .add(((Builder) ((Builder) ((Builder) new Builder().name("interactBlock")).description("")).defaultValue(true)).build());
    private final Setting<Boolean> interactEntity = this.sgGeneral
        .add(((Builder) ((Builder) ((Builder) new Builder().name("interactEntity")).description("")).defaultValue(true)).build());

    public InteractFix() {
        super(Compassion.AMRITA, "InteractionFix", "1.21+ Packet fix");
    }

    public void onDeactivate() {
    }

    @EventHandler
    private void onPacketSend(Send event) {
        if (event.packet instanceof PlayerInteractItemC2SPacket && this.interactItem.get()) {
            this.sendPacket(
                new Full(
                    this.mc.player.getX(), this.mc.player.getY(), this.mc.player.getZ(), this.mc.player.getYaw(), this.mc.player.getPitch(), this.mc.player.isOnGround()
                )
            );
        }

        if (event.packet instanceof PlayerActionC2SPacket packet && this.releaseItem.get() && packet.getAction() == Action.RELEASE_USE_ITEM) {
            this.sendPacket(
                new Full(
                    this.mc.player.getX(), this.mc.player.getY(), this.mc.player.getZ(), this.mc.player.getYaw(), this.mc.player.getPitch(), this.mc.player.isOnGround()
                )
            );
            this.mc.player.stopUsingItem();
        }

        if (event.packet instanceof PlayerInteractEntityC2SPacket && this.interactEntity.get()) {
            this.sendPacket(
                new Full(
                    this.mc.player.getX(), this.mc.player.getY(), this.mc.player.getZ(), this.mc.player.getYaw(), this.mc.player.getPitch(), this.mc.player.isOnGround()
                )
            );
        }

        if (event.packet instanceof PlayerInteractBlockC2SPacket && this.interactBlock.get()) {
            this.sendPacket(
                new Full(
                    this.mc.player.getX(), this.mc.player.getY(), this.mc.player.getZ(), this.mc.player.getYaw(), this.mc.player.getPitch(), this.mc.player.isOnGround()
                )
            );
        }
    }
}
