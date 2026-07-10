package dev.abstr3act.addon.modules.Abnormally;

import dev.abstr3act.addon.Compassion;
import dev.abstr3act.addon.module.AbnormallyModule;
import dev.abstr3act.addon.utils.abnormally.AChatUtils;
import dev.abstr3act.addon.utils.abnormally.BlockUtil;
import dev.abstr3act.addon.utils.abnormally.MSTimer;
import dev.abstr3act.addon.utils.abnormally.TPUtil;
import dev.abstr3act.addon.utils.compassion.impl.PlayerUtil;
import meteordevelopment.meteorclient.events.packets.PacketEvent.Receive;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.EnumSetting.Builder;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.TeleportConfirmC2SPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class NoLagback extends AbnormallyModule {
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
    private final Setting<VersionType> version = this.sgGeneral
        .add(((Builder) ((Builder) ((Builder) new Builder().name("version")).description("mc version")).defaultValue(VersionType.MC1_16)).build());
    private final Setting<Double> range = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("range"))
                .description("Range."))
                .defaultValue(100.0)
                .sliderMax(200.0)
                .sliderMin(1.0)
                .build()
        );
    private final Setting<Double> searchStep = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("search-step"))
                .description("Step for searching"))
                .defaultValue(1.8)
                .sliderMax(3.0)
                .sliderMin(0.1)
                .build()
        );
    private final Setting<Double> moveDistance = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("move-distance"))
                .description("Move distance"))
                .defaultValue(100.0)
                .sliderMax(200.0)
                .sliderMin(1.0)
                .build()
        );
    private final Setting<Integer> tick = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("tick"))
                .description("(packetVec.distanceTo(pos) / tick)"))
                .defaultValue(20))
                .sliderMax(40)
                .sliderMin(1)
                .build()
        );
    private final Setting<Boolean> allowVoid = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("allow-into-void"))
                .description("Allow search path into void"))
                .defaultValue(false))
                .build()
        );
    private final Setting<Boolean> limitMessage = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("allow-limit-packet"))
                .description("Send message while too many packet were send"))
                .defaultValue(true))
                .build()
        );
    private final Setting<Integer> limitPacket = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("packet-limit"))
                .description("The packet send limit"))
                .defaultValue(20))
                .sliderMax(40)
                .sliderMin(1)
                .visible(this.limitMessage::get))
                .build()
        );
    private final Setting<Boolean> debug = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("debug"))
                .description("Allow debugging"))
                .defaultValue(false))
                .build()
        );
    private MSTimer timer;

    public NoLagback() {
        super(Compassion.ABNORMALLY, "GAntiLag", "anti lag.");
    }

    @EventHandler
    public void onTick(TickEvent event) {
        if (this.timer.hasPassTime(1000)) {
            this.timer.reset();
        }
    }

    public void debug(Text msg) {
        if (this.debug.get()) {
            AChatUtils.sendMsg(msg);
        }
    }

    @EventHandler
    public void onReceivePacket(Receive event) {
        Packet<?> packet = event.packet;
        if (packet instanceof PlayerPositionLookS2CPacket pplpacket) {
            Vec3d packetVec = new Vec3d(pplpacket.getX(), pplpacket.getY(), pplpacket.getZ());
            Vec3d playerPos;
            if (packetVec.distanceTo(playerPos = this.mc.player.getPos()) > this.range.get()) {
                return;
            }

            switch ((VersionType) this.version.get()) {
                case MC1_16:
                    if (BlockUtil.isSafeBlock(BlockPos.ofFloored(packetVec))) {
                        if (BlockUtil.isSafeBlock(BlockPos.ofFloored(playerPos))) {
                            this.mc.player.networkHandler.sendPacket(new TeleportConfirmC2SPacket(pplpacket.getTeleportId()));
                            Vec3d vec3d = TPUtil.findVClipVecToMove(packetVec, playerPos, this.searchStep.get(), this.allowVoid.get());
                            if (vec3d != null) {
                                Vec3d vec3d2 = TPUtil.findVClipVecToMove(playerPos, packetVec, this.searchStep.get(), this.allowVoid.get());
                                if (vec3d2 != null) {
                                    int a = (int) Math.ceil(packetVec.distanceTo(vec3d) / (this.tick.get()).intValue());
                                    int b = (int) Math.ceil(packetVec.distanceTo(vec3d2) / (this.tick.get()).intValue());
                                    int c = (int) Math.ceil(packetVec.distanceTo(playerPos) / (this.tick.get()).intValue());
                                    int maxPacket = Math.max(Math.max(a, b), c);
                                    if (this.limitMessage.get() && maxPacket > this.limitPacket.get()) {
                                        AChatUtils.sendMsg(Text.literal("TP packet limit exceeded!"));
                                    } else {
                                        for (int i = 1; i <= maxPacket; i++) {
                                            TPUtil.sendMovePacket(packetVec.x, packetVec.y, packetVec.z, false);
                                            this.debug(Text.literal(packetVec.x + " " + packetVec.y + " " + packetVec.z));
                                        }

                                        TPUtil.sendMovePacket(vec3d.x, vec3d.y, vec3d.z, false);
                                        this.debug(Text.literal(vec3d.x + " " + vec3d.y + " " + vec3d.z));
                                        TPUtil.sendMovePacket(playerPos.x, playerPos.y, playerPos.z, false);
                                        this.debug(Text.literal(playerPos.x + " " + playerPos.y + " " + playerPos.z));
                                        this.mc.player.setPosition(playerPos);
                                    }
                                }
                            }

                            this.debug(Text.literal("event.cancel"));
                            event.cancel();
                        }
                    } else if (BlockUtil.isSafeBlock(BlockPos.ofFloored(playerPos))) {
                        this.mc.player.networkHandler.sendPacket(new TeleportConfirmC2SPacket(pplpacket.getTeleportId()));
                        this.debug(Text.literal("Sent TeleportConfirmC2SPacket with teleportID " + pplpacket.getTeleportId()));
                        Vec3d vec3d = this.mc.player.getPos();
                        TPUtil.doTp(packetVec, vec3d, this.moveDistance.get(), false);
                        this.debug(Text.literal("doTP: " + packetVec + " " + vec3d + " " + this.moveDistance.get()));
                        event.cancel();
                    }
                    break;
                case MC1_9:
                    if (dev.abstr3act.addon.utils.compassion.impl.BlockUtil.checkNoPosCollie(packetVec, null, PlayerUtil.PlayerState.Normal)
                        && dev.abstr3act.addon.utils.compassion.impl.BlockUtil.checkNoPosCollie(playerPos, null, PlayerUtil.PlayerState.Normal)) {
                        TPUtil.send(new TeleportConfirmC2SPacket(((PlayerPositionLookS2CPacket) packet).getTeleportId()));
                        TPUtil.doTp(packetVec, playerPos, this.range.get(), false);
                    }

                    event.cancel();
            }
        }
    }

    public static enum VersionType {
        MC1_16,
        MC1_9;
    }
}
