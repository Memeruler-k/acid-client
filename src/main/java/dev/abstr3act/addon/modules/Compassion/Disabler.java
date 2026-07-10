package dev.abstr3act.addon.modules.Compassion;

import dev.abstr3act.addon.Compassion;
import dev.abstr3act.addon.events.legacy.WorldEvent;
import dev.abstr3act.addon.module.CompassionModule;
import dev.abstr3act.addon.utils.abnormally.AChatUtils;
import dev.abstr3act.addon.utils.abnormally.BlockUtil;
import dev.abstr3act.addon.utils.abnormally.MSTimer;
import dev.abstr3act.addon.utils.abnormally.TPUtil;
import dev.abstr3act.addon.utils.compassion.impl.PlayerUtil;
import meteordevelopment.meteorclient.events.packets.PacketEvent.Receive;
import meteordevelopment.meteorclient.events.packets.PacketEvent.Send;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.events.world.TickEvent.Post;
import meteordevelopment.meteorclient.settings.EnumSetting.Builder;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket.Action;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket.PositionAndOnGround;
import net.minecraft.network.packet.c2s.play.TeleportConfirmC2SPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class Disabler extends CompassionModule {
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
    private final Setting<Mode> mode = this.sgGeneral
        .add(((Builder) ((Builder) ((Builder) new Builder().name("Mode")).description(".")).defaultValue(Mode.Vanilla)).build());
    private final Setting<VersionType> version = this.sgGeneral
        .add(
            ((Builder) ((Builder) ((Builder) ((Builder) new Builder().name("version")).description("mc version")).defaultValue(VersionType.MC1_16))
                .visible(() -> ((Mode) this.mode.get()).equals(Mode.Vanilla)))
                .build()
        );
    private final Setting<Double> range = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("range"))
                .description("Range."))
                .defaultValue(100.0)
                .sliderMax(200.0)
                .sliderMin(1.0)
                .visible(() -> ((Mode) this.mode.get()).equals(Mode.Vanilla)))
                .build()
        );
    private final Setting<Double> searchStep = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("search-step"))
                .description("Step for searching"))
                .defaultValue(1.8)
                .sliderMax(3.0)
                .sliderMin(0.1)
                .visible(() -> ((Mode) this.mode.get()).equals(Mode.Vanilla)))
                .build()
        );
    private final Setting<Double> moveDistance = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("move-distance"))
                .description("Move distance"))
                .defaultValue(100.0)
                .sliderMax(200.0)
                .sliderMin(1.0)
                .visible(() -> ((Mode) this.mode.get()).equals(Mode.Vanilla)))
                .build()
        );
    private final Setting<Integer> tick = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("tick"))
                .description("(packetVec.distanceTo(pos) / tick)"))
                .defaultValue(20))
                .sliderMax(40)
                .sliderMin(1)
                .visible(() -> ((Mode) this.mode.get()).equals(Mode.Vanilla)))
                .build()
        );
    private final Setting<Boolean> allowVoid = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("allow-into-void"))
                .description("Allow search path into void"))
                .defaultValue(false))
                .visible(() -> ((Mode) this.mode.get()).equals(Mode.Vanilla)))
                .build()
        );
    private final Setting<Boolean> limitMessage = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("allow-limit-packet"))
                .description("Send message while too many packet were send"))
                .defaultValue(true))
                .visible(() -> ((Mode) this.mode.get()).equals(Mode.Vanilla)))
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
                .visible(() -> ((Mode) this.mode.get()).equals(Mode.Vanilla) && this.limitMessage.get()))
                .build()
        );
    private final Setting<Boolean> debug = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("debug"))
                .description("Allow debugging"))
                .defaultValue(false))
                .visible(() -> ((Mode) this.mode.get()).equals(Mode.Vanilla)))
                .build()
        );
    private final Setting<Boolean> elytra = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("Elytra"))
                .description("Allow debugging"))
                .defaultValue(false))
                .visible(() -> ((Mode) this.mode.get()).equals(Mode.VulcanFlag)))
                .build()
        );
    private final Setting<Boolean> cancelFallFlying = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("CancelFallFlying"))
                .description("Allow debugging"))
                .defaultValue(false))
                .visible(() -> ((Mode) this.mode.get()).equals(Mode.Vanilla)))
                .build()
        );
    private final Setting<Boolean> C0F = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("CancelC0F"))
                .description("."))
                .defaultValue(false))
                .build()
        );
    public boolean skip = false;
    int count;
    private MSTimer timer;
    private int slot = -1;

    public Disabler() {
        super(Compassion.COMPASSION, "Disabler", "AntiCheat explotion");
    }

    @EventHandler
    private void onPacketSent(Send event) {
        if (this.C0F.get() && event.packet instanceof ClientCommandC2SPacket) {
            event.cancel();
        }
    }

    @EventHandler
    public void onTick(TickEvent event) {
        if (((Mode) this.mode.get()).equals(Mode.Vanilla) && this.timer.hasPassTime(1000)) {
            this.timer.reset();
        }
    }

    public void debug(Text msg) {
        if (this.debug.get()) {
            AChatUtils.sendMsg(msg);
        }
    }

    private boolean checkRiptide() {
        ItemStack offhand = this.mc.player.getOffHandStack();
        return false;
    }

    @EventHandler
    private void onWorldChange(WorldEvent event) {
        this.skip = true;
    }

    @EventHandler
    public void onReceivePacket(Receive event) {
        if (event.packet instanceof PlayerPositionLookS2CPacket && ((Mode) this.mode.get()).equals(Mode.VulcanFlag)) {
            if (this.elytra.get()) {
                FindItemResult elytra = InvUtils.find(new Item[]{Items.ELYTRA});
                this.slot = elytra.slot();
                InvUtils.move().from(this.slot).toArmor(2);
                wait(() -> {
                    InvUtils.move().fromArmor(2).to(this.slot);
                    this.slot = -1;
                }, 20L);
            }

            double distance = MathHelper.sqrt(
                (float) (
                    Math.pow(this.mc.player.getX() - this.mc.player.prevX, 2.0)
                        + Math.pow(this.mc.player.getY() - this.mc.player.prevY, 2.0)
                        + Math.pow(this.mc.player.getZ() - this.mc.player.prevZ, 2.0)
                )
            );
            int packets = (int) (distance / this.moveDistance.get());

            for (int i = 0; i < packets; i++) {
                this.mc
                    .player
                    .networkHandler
                    .sendPacket(new PositionAndOnGround(this.mc.player.prevX, this.mc.player.prevY, this.mc.player.prevZ, this.mc.player.isOnGround()));
            }

            event.cancel();
        }

        if (((Mode) this.mode.get()).equals(Mode.Vanilla)) {
            Packet<?> packet = event.packet;
            if (packet instanceof PlayerPositionLookS2CPacket pplpacket) {
                Vec3d packetVec = new Vec3d(pplpacket.getX(), pplpacket.getY(), pplpacket.getZ());
                Vec3d playerPos;
                if (packetVec.distanceTo(playerPos = this.mc.player.getPos()) > this.range.get()) {
                    return;
                }

                if (this.skip) {
                    this.skip = false;
                    return;
                }

                switch ((VersionType) this.version.get()) {
                    case MC1_21:
                        double distance = MathHelper.sqrt(
                            (float) (
                                Math.pow(this.mc.player.getX() - this.mc.player.prevX, 2.0)
                                    + Math.pow(this.mc.player.getY() - this.mc.player.prevY, 2.0)
                                    + Math.pow(this.mc.player.getZ() - this.mc.player.prevZ, 2.0)
                            )
                        );
                        int packets = (int) (distance / this.moveDistance.get());

                        for (int i = 0; i < packets; i++) {
                            this.mc
                                .player
                                .networkHandler
                                .sendPacket(new PositionAndOnGround(this.mc.player.prevX, this.mc.player.prevY, this.mc.player.prevZ, this.mc.player.isOnGround()));
                        }
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
    }

    private void sendPacket(ClientPlayerEntity player, ClientCommandC2SPacket.Mode mode) {
        player.networkHandler.sendPacket(new ClientCommandC2SPacket(player, mode));
    }

    @EventHandler
    private void onTick(Post event) {
        if (this.cancelFallFlying.get() && ((Mode) this.mode.get()).equals(Mode.Vanilla)) {
            this.mc.player.stopFallFlying();
        }

        if (((Mode) this.mode.get()).equals(Mode.OldVulcanScaffold)
            && !this.mc.player.isInFluid()
            && !this.mc.player.isTouchingWater()
            && !this.mc.player.isDead()
            && !this.mc.player.isHoldingOntoLadder()
            && !this.mc.player.getAbilities().flying) {
            this.mc
                .getNetworkHandler()
                .sendPacket(new ClientCommandC2SPacket(this.mc.player, ClientCommandC2SPacket.Mode.START_SPRINTING));
            this.mc
                .getNetworkHandler()
                .sendPacket(new ClientCommandC2SPacket(this.mc.player, ClientCommandC2SPacket.Mode.STOP_SPRINTING));
            if (this.mc.player.age % 10 == 0) {
                this.mc
                    .getNetworkHandler()
                    .sendPacket(new ClientCommandC2SPacket(this.mc.player, ClientCommandC2SPacket.Mode.PRESS_SHIFT_KEY));
                this.mc
                    .getNetworkHandler()
                    .sendPacket(new ClientCommandC2SPacket(this.mc.player, ClientCommandC2SPacket.Mode.RELEASE_SHIFT_KEY));
            }
        }

        if (((Mode) this.mode.get()).equals(Mode.VulcanScaffold)) {
            if (this.mc.player.isSubmergedInWater()
                || this.mc.player.isTouchingWater()
                || this.mc.player.isDead()
                || this.mc.player.isClimbing()
                || this.mc.player.getAbilities().flying) {
                return;
            }

            this.sendPacket(this.mc.player, ClientCommandC2SPacket.Mode.START_SPRINTING);
            this.sendPacket(this.mc.player, ClientCommandC2SPacket.Mode.STOP_SPRINTING);
            if (this.mc.player.age % 9 == 0 && this.mc.player.isOnGround()) {
                this.sendPacket(this.mc.player, ClientCommandC2SPacket.Mode.RELEASE_SHIFT_KEY);
            }
        }

        if (((Mode) this.mode.get()).equals(Mode.VulcanRiptide) && this.checkRiptide()) {
            wait(() -> this.sendSequencedPacket(p -> new PlayerInteractItemC2SPacket(Hand.OFF_HAND, p, this.mc.player.getYaw(), this.mc.player.getPitch())), 1000L)
                .thenRun(() -> this.sendPacket(new PlayerActionC2SPacket(Action.RELEASE_USE_ITEM, new BlockPos(0, 0, 0), Direction.DOWN)));
        }
    }

    public String getInfoString() {
        return ((Mode) this.mode.get()).name();
    }

    static enum Mode {
        Vanilla,
        OldVulcanScaffold,
        VulcanRiptide,
        VulcanScaffold,
        VulcanMovement,
        VulcanFlag;
    }

    public static enum VersionType {
        MC1_21,
        MC1_16,
        MC1_9;
    }
}
