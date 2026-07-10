package dev.abstr3act.addon.modules.Amrita.latency;

import dev.abstr3act.addon.Compassion;
import dev.abstr3act.addon.events.EventAttack;
import dev.abstr3act.addon.events.EventPacket;
import dev.abstr3act.addon.events.TransferOrigin;
import dev.abstr3act.addon.module.CompassionModule;
import dev.abstr3act.addon.utils.render.MathUtility;
import dev.abstr3act.addon.utils.render.Render2DEngine;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent.Post;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.BoolSetting.Builder;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.TrackedPosition;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import net.minecraft.network.packet.c2s.play.CommandExecutionC2SPacket;
import net.minecraft.network.packet.s2c.common.DisconnectS2CPacket;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.network.packet.s2c.play.HealthUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Box;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public final class Backtrack extends CompassionModule {
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
    private final Setting<Boolean> client = this.sgGeneral
        .add(((Builder) ((Builder) ((Builder) new Builder().name("Client")).description(".")).defaultValue(true)).build());
    private final Setting<Double> range1 = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("Range 1"))
                .description("."))
                .defaultValue(1.0)
                .min(0.0)
                .sliderMax(6.0)
                .build()
        );
    private final Setting<Double> range2 = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("Range 2"))
                .description("."))
                .defaultValue(3.0)
                .min(0.0)
                .sliderMax(6.0)
                .build()
        );
    private final Setting<Integer> delay1 = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("Delay1"))
                .description("."))
                .defaultValue(50))
                .min(0)
                .sliderMax(2000)
                .build()
        );
    private final Setting<Integer> delay2 = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("Delay2"))
                .description("."))
                .defaultValue(100))
                .min(0)
                .sliderMax(2000)
                .build()
        );
    private final Set<FakeLagUtils.DelayData> packetQueue = Collections.synchronizedSet(new LinkedHashSet<>());
    private Entity target;
    private TrackedPosition position;

    public Backtrack() {
        super(Compassion.AMRITA, "Backtrack", ".");
    }

    @EventHandler
    public void onPacketEvent(EventPacket event) {
        synchronized (this.packetQueue) {
            if (event.getOrigin() == TransferOrigin.RECEIVE && !event.isCancelled()) {
                if (!this.packetQueue.isEmpty() || this.shouldCancelPackets()) {
                    Packet<?> packet = event.getPacket();
                    if (!(packet instanceof ChatMessageC2SPacket) && !(packet instanceof GameMessageS2CPacket) && !(packet instanceof CommandExecutionC2SPacket)) {
                        if (!(packet instanceof PlayerPositionLookS2CPacket) && !(packet instanceof DisconnectS2CPacket)) {
                            if (!(packet instanceof PlaySoundS2CPacket) || ((PlaySoundS2CPacket) packet).getSound().value() != SoundEvents.ENTITY_PLAYER_HURT) {
                                if (packet instanceof HealthUpdateS2CPacket && ((HealthUpdateS2CPacket) packet).getHealth() <= 0.0F) {
                                    this.clear(true);
                                } else {
                                    event.cancel();
                                    this.packetQueue.add(new FakeLagUtils.DelayData(packet, System.currentTimeMillis()));
                                }
                            }
                        } else {
                            this.clear(true);
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onRenderEvent(Render3DEvent event) {
        if (this.target != null && this.position != null) {
            double tPosX = Render2DEngine.interpolate(this.target.prevX, this.position.pos.x, this.mc.getRenderTickCounter().getTickDelta(true));
            double tPosY = Render2DEngine.interpolate(this.target.prevY, this.position.pos.y, this.mc.getRenderTickCounter().getTickDelta(true));
            double tPosZ = Render2DEngine.interpolate(this.target.prevZ, this.position.pos.z, this.mc.getRenderTickCounter().getTickDelta(true));
            Box box = this.target.getBoundingBox();
            event.renderer.box(box, new Color(255, 255, 255, 80), new Color(255, 255, 255, 255), ShapeMode.Both, 1);
        }
    }

    public String getInfoString() {
        return this.delay1.get() + "-" + this.delay2.get() + " ms";
    }

    @EventHandler
    public void onTickEvent(Post event) {
        if (this.shouldCancelPackets()) {
            this.processPackets(false);
        } else {
            this.clear(false);
        }
    }

    @EventHandler
    public void onAttackEvent(EventAttack event) {
        System.out.println("EventAttack");
        Entity enemy = event.getEntity();
        if (enemy instanceof LivingEntity) {
            if (this.shouldConsiderAsEnemy(enemy)) {
                if (enemy != this.target) {
                    this.clear(false);
                    this.position = new TrackedPosition();
                    this.position.setPos(enemy.getTrackedPosition().pos);
                }

                this.target = enemy;
            }
        }
    }

    public void onActivate() {
        this.clear(false);
    }

    public void onDeactivate() {
        this.clear(true);
    }

    private void processPackets(boolean clear) {
        synchronized (this.packetQueue) {
            this.packetQueue
                .removeIf(
                    data -> {
                        if (!clear
                            && !(
                            (float) data.getDelay()
                                <= (float) System.currentTimeMillis()
                                - MathUtility.random((float) (this.delay1.get()).intValue(), (float) (this.delay2.get()).intValue())
                        )) {
                            return false;
                        } else {
                            this.handlePacket((Packet<ClientPlayNetworkHandler>) data.getPacket());
                            return true;
                        }
                    }
                );
        }
    }

    private void handlePacket(Packet<ClientPlayNetworkHandler> packet) {
        try {
            packet.apply(this.mc.getNetworkHandler());
        } catch (ClassCastException var3) {
            var3.printStackTrace();
        }
    }

    private void clear(boolean handlePackets) {
        if (handlePackets) {
            this.processPackets(true);
        } else {
            synchronized (this.packetQueue) {
                this.packetQueue.clear();
            }
        }

        this.target = null;
        this.position = null;
    }

    private boolean shouldConsiderAsEnemy(Entity target) {
        float range = MathUtility.random((this.range1.get()).floatValue(), (this.range2.get()).floatValue());
        return target.distanceTo(this.mc.player) <= range && this.mc.player.age > 10;
    }

    private boolean shouldCancelPackets() {
        return this.target != null && this.target.isAlive() && this.shouldConsiderAsEnemy(this.target);
    }
}
