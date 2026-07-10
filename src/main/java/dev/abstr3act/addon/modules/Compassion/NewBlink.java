package dev.abstr3act.addon.modules.Compassion;

import dev.abstr3act.addon.Compassion;
import dev.abstr3act.addon.events.EventPlayerUpdate;
import dev.abstr3act.addon.module.CompassionModule;
import dev.abstr3act.addon.utils.Render3DEngine;
import dev.abstr3act.addon.utils.abnormally.AChatUtils;
import meteordevelopment.meteorclient.events.entity.player.AttackEntityEvent;
import meteordevelopment.meteorclient.events.packets.PacketEvent.Receive;
import meteordevelopment.meteorclient.events.packets.PacketEvent.Send;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.settings.BoolSetting.Builder;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.utils.entity.fakeplayer.FakePlayerEntity;
import meteordevelopment.meteorclient.utils.misc.Keybind;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.common.CommonPongC2SPacket;
import net.minecraft.network.packet.c2s.common.KeepAliveC2SPacket;
import net.minecraft.network.packet.c2s.play.*;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket.LookAndOnGround;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;

import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;

public class NewBlink extends CompassionModule {
    public static Vec3d lastPos = Vec3d.ZERO;
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
    public final Setting<Boolean> pulse = this.sgGeneral
        .add(((Builder) ((Builder) ((Builder) new Builder().name("Pulse")).description(".")).defaultValue(false)).build());
    public final Setting<Boolean> autoDisable = this.sgGeneral
        .add(((Builder) ((Builder) ((Builder) new Builder().name("AutoDisable")).description(".")).defaultValue(false)).build());
    public final Setting<Boolean> disableOnVelocity = this.sgGeneral
        .add(((Builder) ((Builder) ((Builder) new Builder().name("disableOnVelocity")).description(".")).defaultValue(false)).build());
    public final Setting<Boolean> disableOnAttack = this.sgGeneral
        .add(((Builder) ((Builder) ((Builder) new Builder().name("disableOnAttack")).description(".")).defaultValue(false)).build());
    private final Setting<Integer> disablePackets = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("DisablePackets"))
                .description("."))
                .defaultValue(17))
                .min(1)
                .sliderRange(1, 1000)
                .build()
        );
    private final Setting<Integer> pulsePackets = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("PulsePackets"))
                .description("."))
                .defaultValue(20))
                .min(1)
                .sliderRange(1, 1000)
                .build()
        );
    private final Setting<Keybind> cancel = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.KeybindSetting.Builder) ((meteordevelopment.meteorclient.settings.KeybindSetting.Builder) ((meteordevelopment.meteorclient.settings.KeybindSetting.Builder) new meteordevelopment.meteorclient.settings.KeybindSetting.Builder()
                .name("Cancel"))
                .description("."))
                .defaultValue(Keybind.none()))
                .build()
        );
    private final Queue<Packet<?>> storedPackets = new LinkedList<>();
    private final Queue<Packet<?>> storedTransactions = new LinkedList<>();
    private final AtomicBoolean sending = new AtomicBoolean(false);
    private Vec3d prevVelocity = Vec3d.ZERO;
    private float prevYaw = 0.0F;
    private boolean prevSprinting = false;
    private FakePlayerEntity model;

    public NewBlink() {
        super(Compassion.COMPASSION, "NewBlink", ".");
    }

    public void onActivate() {
        if (this.mc.player != null && this.mc.world != null && !this.mc.isIntegratedServerRunning() && this.mc.getNetworkHandler() != null) {
            this.storedTransactions.clear();
            lastPos = this.mc.player.getPos();
            this.prevVelocity = this.mc.player.getVelocity();
            this.prevYaw = this.mc.player.getYaw();
            this.prevSprinting = this.mc.player.isSprinting();
            this.mc
                .world
                .spawnEntity(
                    new ClientPlayerEntity(
                        this.mc,
                        this.mc.world,
                        this.mc.getNetworkHandler(),
                        this.mc.player.getStatHandler(),
                        this.mc.player.getRecipeBook(),
                        this.mc.player.lastSprinting,
                        this.mc.player.isSneaking()
                    )
                );
            this.sending.set(false);
            this.storedPackets.clear();
        } else {
            this.toggle();
        }
    }

    @EventHandler
    public void onRender3D(Render3DEvent event) {
        if (this.mc.player != null && this.mc.world != null) {
            if (lastPos != null) {
                float[] hsb = Color.RGBtoHSB(255, 255, 255, null);
                float hue = (float) (System.currentTimeMillis() % 7200L) / 7200.0F;
                int rgb = Color.getHSBColor(hue, hsb[1], hsb[2]).getRGB();
                ArrayList<Vec3d> vecs = new ArrayList<>();
                double x = lastPos.x;
                double y = lastPos.y;
                double z = lastPos.z;

                for (int i = 0; i <= 360; i++) {
                    Vec3d vec = new Vec3d(x + Math.sin(i * Math.PI / 180.0) * 0.5, y + 0.01, z + Math.cos(i * Math.PI / 180.0) * 0.5);
                    vecs.add(vec);
                }

                for (int j = 0; j < vecs.size() - 1; j++) {
                    Render3DEngine.drawLine(vecs.get(j), vecs.get(j + 1), new Color(rgb));
                    hue += 0.0027777778F;
                    rgb = Color.getHSBColor(hue, hsb[1], hsb[2]).getRGB();
                }
            }
        }
    }

    public void onDeactivate() {
        if (this.mc.world != null && this.mc.player != null) {
            if (this.model != null) {
                this.model.despawn();
            }

            while (!this.storedPackets.isEmpty()) {
                this.sendPacket(this.storedPackets.poll());
            }
        }
    }

    public String getInfoString() {
        return Integer.toString(this.storedPackets.size());
    }

    @EventHandler
    public void onPacketReceive(Receive event) {
        if (event.packet instanceof EntityVelocityUpdateS2CPacket vel && vel.getEntityId() == this.mc.player.getId() && this.disableOnVelocity.get()) {
            this.toggle("Disabled due to velocity!");
        }
    }

    @EventHandler
    public void onAttack(AttackEntityEvent event) {
        if (this.disableOnAttack.get()) {
            event.cancel();
            this.toggle("Disable due to attack");
            this.mc.interactionManager.attackEntity(this.mc.player, event.entity);
        }
    }

    @EventHandler
    public void onPacketSend(Send event) {
        if (!fullNullCheck()) {
            Packet<?> packet = event.packet;
            if (!this.sending.get()) {
                if (packet instanceof CommonPongC2SPacket) {
                    this.storedTransactions.add(packet);
                }

                if (this.pulse.get()) {
                    if (packet instanceof PlayerMoveC2SPacket) {
                        event.cancel();
                        this.storedPackets.add(packet);
                    }
                } else if (!(packet instanceof ChatMessageC2SPacket)
                    && !(packet instanceof TeleportConfirmC2SPacket)
                    && !(packet instanceof KeepAliveC2SPacket)
                    && !(packet instanceof AdvancementTabC2SPacket)
                    && !(packet instanceof ClientStatusC2SPacket)) {
                    event.cancel();
                    this.storedPackets.add(packet);
                }
            }
        }
    }

    @EventHandler
    public void onUpdate(EventPlayerUpdate event) {
        if (!fullNullCheck()) {
            if (!((Keybind) this.cancel.get()).isPressed()) {
                if (this.pulse.get() && this.storedPackets.size() >= this.pulsePackets.get()) {
                    this.sendPackets();
                }

                if (this.autoDisable.get() && this.storedPackets.size() >= this.disablePackets.get()) {
                    this.toggle();
                }
            } else {
                this.storedPackets.clear();
                this.mc.player.setPos(lastPos.getX(), lastPos.getY(), lastPos.getZ());
                this.mc.player.setVelocity(this.prevVelocity);
                this.mc.player.setYaw(this.prevYaw);
                this.mc.player.setSprinting(this.prevSprinting);
                this.mc.player.setSneaking(false);
                this.mc.options.sneakKey.setPressed(false);
                this.sending.set(true);

                while (!this.storedTransactions.isEmpty()) {
                    this.sendPacket(this.storedTransactions.poll());
                }

                this.sending.set(false);
                AChatUtils.sendMsgCompassion(Text.of("Canceling.."));
                this.toggle();
            }
        }
    }

    private void sendPackets() {
        if (this.mc.player != null) {
            this.sending.set(true);

            while (!this.storedPackets.isEmpty()) {
                Packet<?> packet = this.storedPackets.poll();
                this.sendPacket(packet);
                if (packet instanceof PlayerMoveC2SPacket && !(packet instanceof LookAndOnGround)) {
                    lastPos = new Vec3d(
                        ((PlayerMoveC2SPacket) packet).getX(this.mc.player.getX()),
                        ((PlayerMoveC2SPacket) packet).getY(this.mc.player.getY()),
                        ((PlayerMoveC2SPacket) packet).getZ(this.mc.player.getZ())
                    );
                }
            }

            this.sending.set(false);
            this.storedPackets.clear();
        }
    }

    private static enum RenderMode {
        Circle,
        Model,
        Both;
    }
}
