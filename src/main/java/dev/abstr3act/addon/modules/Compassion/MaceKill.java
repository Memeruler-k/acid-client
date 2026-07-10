package dev.abstr3act.addon.modules.Compassion;

import dev.abstr3act.addon.Compassion;
import dev.abstr3act.addon.module.CompassionModule;
import dev.abstr3act.addon.utils.abnormally.AChatUtils;
import meteordevelopment.meteorclient.events.entity.player.AttackEntityEvent;
import meteordevelopment.meteorclient.events.packets.PacketEvent.Send;
import meteordevelopment.meteorclient.events.world.TickEvent.Pre;
import meteordevelopment.meteorclient.mixininterface.IPlayerInteractEntityC2SPacket;
import meteordevelopment.meteorclient.mixininterface.IVec3d;
import meteordevelopment.meteorclient.settings.EnumSetting.Builder;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.utils.entity.Target;
import meteordevelopment.meteorclient.utils.player.Rotations;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket.OnGroundOnly;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket.PositionAndOnGround;
import net.minecraft.network.packet.c2s.play.VehicleMoveC2SPacket;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;

import javax.annotation.Nullable;
import java.lang.reflect.Method;

public class MaceKill extends CompassionModule {
    private final SettingGroup specialGroup = this.settings.createGroup("Values higher than 10 only work on Paper/Spigot");
    private final Setting<Mode> mode = this.specialGroup
        .add(((Builder) ((Builder) ((Builder) new Builder().name("Mode")).description("Bypass Mode")).defaultValue(Mode.Normal)).build());
    private final Setting<VulcanMode> vmode = this.specialGroup
        .add(
            ((Builder) ((Builder) ((Builder) ((Builder) new Builder().name("Vulcan Jump Bypass")).description("Bypass Mode")).defaultValue(VulcanMode.Jump))
                .visible(() -> ((Mode) this.mode.get()).equals(Mode.Vulcan)))
                .build()
        );
    private final Setting<Boolean> maxPower = this.specialGroup
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("Maximum Mace Power (Paper/Spigot servers only)"))
                .description("Simulates a fall from the highest air gap within 170 blocks"))
                .defaultValue(false))
                .visible(() -> ((Mode) this.mode.get()).equals(Mode.Normal)))
                .build()
        );
    private final Setting<Boolean> packetDisable = this.specialGroup
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("Disable When Blocked"))
                .description("Does not send movement packets if the attack was blocked. (prevents death)"))
                .defaultValue(true))
                .visible(() -> ((Mode) this.mode.get()).equals(Mode.Normal)))
                .build()
        );
    private final Setting<Integer> fallHeight = this.specialGroup
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("Mace Power (Fall height)"))
                .description("Simulates a fall from this distance"))
                .defaultValue(10))
                .sliderRange(1, 170)
                .min(1)
                .build()
        );
    private final Setting<Boolean> debug = this.specialGroup
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("Debug"))
                .description("."))
                .defaultValue(false))
                .build()
        );
    private Vec3d previouspos;
    private PlayerInteractEntityC2SPacket attackPacket;
    private HandSwingC2SPacket swingPacket;
    private boolean sendPackets = false;
    private int sendTimer;
    private Entity target;

    public MaceKill() {
        super(Compassion.COMPASSION, "MaceKill", "Makes the Mace powerful when swung.");
    }

    public void debug(String s) {
        if (this.debug.get()) {
            AChatUtils.sendMsgCompassion(Text.of(Formatting.DARK_GRAY + "[" + Formatting.GRAY + "Debugger" + Formatting.DARK_GRAY + "] " + Formatting.GRAY + s));
        }
    }

    public void onActivate() {
        this.attackPacket = null;
        this.swingPacket = null;
        this.sendPackets = false;
        this.sendTimer = 0;
    }

    private void warp(@Nullable Vec3d pos, boolean onGround) {
        if (this.mc.player.hasVehicle()) {
            Entity vehicle = this.mc.player.getVehicle();
            if (vehicle == null) {
                return;
            }

            this.debug("vehicle entity found: " + vehicle);
            vehicle.setPosition(pos);
            this.sendPacket(new VehicleMoveC2SPacket(vehicle));
        }

        if (pos != null) {
            this.sendPacket(new PositionAndOnGround(pos.x, pos.y, pos.z, onGround));
        } else {
            this.sendPacket(new OnGroundOnly(onGround));
        }

        this.debug("warp entity: " + pos + ", onGround = " + onGround);
    }

    private int determineHeight() {
        Box boundingBox = this.mc.player.getBoundingBox();

        for (int i = this.fallHeight.get(); i >= 1; i--) {
            Box newBoundingBox = boundingBox.offset(0.0, i, 0.0);
            boolean noCollisions = true;

            for (VoxelShape shape : this.mc.world.getBlockCollisions(this.mc.player, newBoundingBox)) {
                if (!shape.equals(VoxelShapes.empty())) {
                    noCollisions = false;
                    break;
                }
            }

            if (noCollisions) {
                return i;
            }
        }

        return 0;
    }

    @EventHandler
    private void onAttackEvent(AttackEntityEvent event) {
        if (this.mc.player.getInventory().getMainHandStack().getItem() == Items.MACE && event.entity instanceof LivingEntity) {
            switch ((Mode) this.mode.get()) {
                case Normal:
                    LivingEntity targetEntityxx = (LivingEntity) event.entity;
                    if (this.packetDisable.get()
                        && (targetEntityxx.isBlocking() && targetEntityxx.blockedByShield(targetEntityxx.getRecentDamageSource()) || targetEntityxx.isInvulnerable())) {
                        return;
                    }

                    this.previouspos = this.mc.player.getPos();
                    int blocks = this.getMaxHeightAbovePlayer();
                    int packetsRequired = (int) Math.ceil(Math.abs(blocks / 10));
                    if (packetsRequired > 20) {
                        packetsRequired = 1;
                    }

                    BlockPos isopenair1 = this.mc.player.getBlockPos().add(0, blocks, 0);
                    BlockPos isopenair2 = this.mc.player.getBlockPos().add(0, blocks + 1, 0);
                    if (this.isSafeBlock(isopenair1) && this.isSafeBlock(isopenair2)) {
                        if (this.mc.player.hasVehicle()) {
                            for (int packetNumber = 0; packetNumber < packetsRequired - 1; packetNumber++) {
                                this.mc.player.networkHandler.sendPacket(new VehicleMoveC2SPacket(this.mc.player.getVehicle()));
                            }

                            this.mc
                                .player
                                .getVehicle()
                                .setPosition(this.mc.player.getVehicle().getX(), this.mc.player.getVehicle().getY() + blocks, this.mc.player.getVehicle().getZ());
                            this.mc.player.networkHandler.sendPacket(new VehicleMoveC2SPacket(this.mc.player.getVehicle()));
                        } else {
                            for (int packetNumber = 0; packetNumber < packetsRequired - 1; packetNumber++) {
                                this.mc.player.networkHandler.sendPacket(new OnGroundOnly(false));
                            }

                            this.mc
                                .player
                                .networkHandler
                                .sendPacket(new PositionAndOnGround(this.mc.player.getX(), this.mc.player.getY() + blocks, this.mc.player.getZ(), false));
                        }

                        if (this.mc.player.hasVehicle()) {
                            this.mc.player.getVehicle().setPosition(this.previouspos);
                            this.mc.player.networkHandler.sendPacket(new VehicleMoveC2SPacket(this.mc.player.getVehicle()));
                            this.mc.player.getVehicle().setPosition(this.previouspos);
                            this.mc.player.networkHandler.sendPacket(new VehicleMoveC2SPacket(this.mc.player.getVehicle()));
                        } else {
                            this.mc
                                .player
                                .networkHandler
                                .sendPacket(new PositionAndOnGround(this.previouspos.getX(), this.previouspos.getY(), this.previouspos.getZ(), false));
                            this.mc
                                .player
                                .networkHandler
                                .sendPacket(new PositionAndOnGround(this.previouspos.getX(), this.previouspos.getY(), this.previouspos.getZ(), false));
                        }
                    }
                    break;
                case Strict:
                    LivingEntity targetEntity = (LivingEntity) event.entity;
                    if (this.skipCrit()) {
                        return;
                    }

                    if (this.packetDisable.get()
                        && (targetEntity.isBlocking() && targetEntity.blockedByShield(targetEntity.getRecentDamageSource()) || targetEntity.isInvulnerable())) {
                        return;
                    }

                    int height = this.determineHeight();
                    if (height > 10) {
                        int iterations = (int) Math.ceil(Math.abs(height / 10.0));

                        for (int i = 0; i < iterations; i++) {
                            this.warp(null, false);
                        }
                    } else {
                        this.warp(null, this.mc.player.isOnGround());
                        this.warp(null, this.mc.player.isOnGround());
                    }

                    this.warp(this.mc.player.getPos().add(0.0, height, 0.0), false);
                    this.warp(this.mc.player.getPos(), false);
                    break;
                case Vulcan:
                    LivingEntity targetEntityx = (LivingEntity) event.entity;
                    if (this.skipCrit()) {
                        return;
                    }

                    if (this.packetDisable.get()
                        && (targetEntityx.isBlocking() && targetEntityx.blockedByShield(targetEntityx.getRecentDamageSource()) || targetEntityx.isInvulnerable())) {
                        return;
                    }

                    if (!this.sendPackets) {
                        this.sendPackets = true;
                        this.sendTimer = ((VulcanMode) this.vmode.get()).equals(VulcanMode.MiniJump) ? 2 : 4;
                        this.target = event.entity;
                        if (((VulcanMode) this.vmode.get()).equals(VulcanMode.MiniJump)) {
                            ((IVec3d) this.mc.player.getVelocity()).setY(0.25);
                        } else {
                            this.mc.player.jump();
                        }

                        event.cancel();
                    }
            }
        }
    }

    @EventHandler(
        priority = 100
    )
    private void onSendPacket(Send event) {
        if (event.packet instanceof IPlayerInteractEntityC2SPacket packet) {
            try {
                Class<?> packetClass = packet.getClass();
                Method getTypeMethod = packetClass.getDeclaredMethod("getType");
                getTypeMethod.setAccessible(true);
                Enum<?> interactType = (Enum<?>) getTypeMethod.invoke(packet);
                if (interactType.name().equals("ATTACK")
                    && this.mc.player.getInventory().getMainHandStack().getItem() == Items.MACE
                    && packet.getEntity() instanceof LivingEntity) {
                    switch ((Mode) this.mode.get()) {
                        case Normal:
                            LivingEntity targetEntityxx = (LivingEntity) packet.getEntity();
                            if (this.packetDisable.get()
                                && (targetEntityxx.isBlocking() && targetEntityxx.blockedByShield(targetEntityxx.getRecentDamageSource()) || targetEntityxx.isInvulnerable())) {
                                return;
                            }

                            this.previouspos = this.mc.player.getPos();
                            int blocks = this.getMaxHeightAbovePlayer();
                            int packetsRequired = (int) Math.ceil(Math.abs(blocks / 10));
                            if (packetsRequired > 20) {
                                packetsRequired = 1;
                            }

                            BlockPos isopenair1 = this.mc.player.getBlockPos().add(0, blocks, 0);
                            BlockPos isopenair2 = this.mc.player.getBlockPos().add(0, blocks + 1, 0);
                            if (this.isSafeBlock(isopenair1) && this.isSafeBlock(isopenair2)) {
                                if (this.mc.player.hasVehicle()) {
                                    for (int packetNumber = 0; packetNumber < packetsRequired - 1; packetNumber++) {
                                        this.mc.player.networkHandler.sendPacket(new VehicleMoveC2SPacket(this.mc.player.getVehicle()));
                                    }

                                    this.mc
                                        .player
                                        .getVehicle()
                                        .setPosition(this.mc.player.getVehicle().getX(), this.mc.player.getVehicle().getY() + blocks, this.mc.player.getVehicle().getZ());
                                    this.mc.player.networkHandler.sendPacket(new VehicleMoveC2SPacket(this.mc.player.getVehicle()));
                                } else {
                                    for (int packetNumber = 0; packetNumber < packetsRequired - 1; packetNumber++) {
                                        this.mc.player.networkHandler.sendPacket(new OnGroundOnly(false));
                                    }

                                    this.mc
                                        .player
                                        .networkHandler
                                        .sendPacket(new PositionAndOnGround(this.mc.player.getX(), this.mc.player.getY() + blocks, this.mc.player.getZ(), false));
                                }

                                if (this.mc.player.hasVehicle()) {
                                    this.mc.player.getVehicle().setPosition(this.previouspos);
                                    this.mc.player.networkHandler.sendPacket(new VehicleMoveC2SPacket(this.mc.player.getVehicle()));
                                    this.mc.player.getVehicle().setPosition(this.previouspos);
                                    this.mc.player.networkHandler.sendPacket(new VehicleMoveC2SPacket(this.mc.player.getVehicle()));
                                } else {
                                    this.mc
                                        .player
                                        .networkHandler
                                        .sendPacket(new PositionAndOnGround(this.previouspos.getX(), this.previouspos.getY(), this.previouspos.getZ(), false));
                                    this.mc
                                        .player
                                        .networkHandler
                                        .sendPacket(new PositionAndOnGround(this.previouspos.getX(), this.previouspos.getY(), this.previouspos.getZ(), false));
                                }
                            }
                            break;
                        case Strict:
                            LivingEntity targetEntityx = (LivingEntity) packet.getEntity();
                            if (this.skipCrit()) {
                                return;
                            }

                            if (!this.packetDisable.get()
                                || (!targetEntityx.isBlocking() || !targetEntityx.blockedByShield(targetEntityx.getRecentDamageSource())) && !targetEntityx.isInvulnerable()) {
                                int height = this.determineHeight();
                                if (height > 10) {
                                    int iterations = (int) Math.ceil(Math.abs(height / 10.0));

                                    for (int i = 0; i < iterations; i++) {
                                        this.warp(null, false);
                                    }
                                } else {
                                    this.warp(null, this.mc.player.isOnGround());
                                    this.warp(null, this.mc.player.isOnGround());
                                }

                                this.warp(this.mc.player.getPos().add(0.0, height, 0.0), false);
                                this.warp(this.mc.player.getPos(), false);
                                break;
                            }

                            return;
                        case Vulcan:
                            LivingEntity targetEntity = (LivingEntity) packet.getEntity();
                            if (this.skipCrit()) {
                                return;
                            }

                            if (this.packetDisable.get()
                                && (targetEntity.isBlocking() && targetEntity.blockedByShield(targetEntity.getRecentDamageSource()) || targetEntity.isInvulnerable())) {
                                return;
                            }

                            if (!this.sendPackets) {
                                this.sendPackets = true;
                                this.sendTimer = ((VulcanMode) this.vmode.get()).equals(VulcanMode.MiniJump) ? 2 : 4;
                                this.target = packet.getEntity();
                                this.attackPacket = (PlayerInteractEntityC2SPacket) event.packet;
                                if (((VulcanMode) this.vmode.get()).equals(VulcanMode.MiniJump)) {
                                    ((IVec3d) this.mc.player.getVelocity()).setY(0.25);
                                } else {
                                    this.mc.player.jump();
                                }

                                this.debug("sent packet: " + event.packet.toString() + ", ID = " + event.packet.getPacketId());
                                event.cancel();
                            }
                    }
                }
            } catch (Exception var12) {
                var12.printStackTrace();
            }
        } else if (event.packet instanceof HandSwingC2SPacket) {
            if (this.skipCrit()) {
                return;
            }

            if (this.sendPackets && this.swingPacket == null) {
                this.swingPacket = (HandSwingC2SPacket) event.packet;
                event.cancel();
            }
        }
    }

    private int getMaxHeightAbovePlayer() {
        BlockPos playerPos = this.mc.player.getBlockPos();
        int maxHeight = playerPos.getY() + (this.maxPower.get() ? 170 : this.fallHeight.get());

        for (int i = maxHeight; i > playerPos.getY(); i--) {
            BlockPos isopenair1 = new BlockPos(playerPos.getX(), i, playerPos.getZ());
            BlockPos isopenair2 = isopenair1.up(1);
            if (this.isSafeBlock(isopenair1) && this.isSafeBlock(isopenair2)) {
                return i - playerPos.getY();
            }
        }

        return 0;
    }

    private boolean skipCrit() {
        return !this.mc.player.isOnGround() || this.mc.player.isSubmergedInWater() || this.mc.player.isInLava() || this.mc.player.isClimbing();
    }

    @EventHandler
    private void onTickEvent(Pre e) {
        if (this.sendPackets) {
            if (this.sendTimer <= 0) {
                this.sendPackets = false;
                if (this.mc.player.hasStatusEffect(StatusEffects.SPEED)) {
                    this.mc.player.setVelocity(this.mc.player.getVelocity().add(0.0, -0.5, 0.0));
                } else {
                    this.mc.player.setVelocity(this.mc.player.getVelocity().add(0.0, -0.37F, 0.0));
                }

                this.debug("velocity = " + this.mc.player.getVelocity().x + ", " + this.mc.player.getVelocity().y + ", " + this.mc.player.getVelocity().z);
                this.warp(null, false);
                int height = this.determineHeight();
                if (height > 10) {
                    int iterations = (int) Math.ceil(Math.abs(height / 10.0));

                    for (int i = 0; i < iterations; i++) {
                        this.warp(null, false);
                    }
                } else {
                    this.warp(null, this.mc.player.isOnGround());
                    this.warp(null, this.mc.player.isOnGround());
                }

                this.warp(this.mc.player.getPos().add(0.0, height, 0.0), false);
                this.warp(this.mc.player.getPos(), false);
                if (this.attackPacket == null || this.swingPacket == null) {
                    return;
                }

                if (this.target != null) {
                    Rotations.rotate(Rotations.getYaw(this.target), Rotations.getPitch(this.target, Target.Body));
                }

                this.mc.getNetworkHandler().sendPacket(this.attackPacket);
                this.mc.getNetworkHandler().sendPacket(this.swingPacket);
                this.attackPacket = null;
                this.swingPacket = null;
                this.mc.player.setOnGround(true);
            } else {
                this.sendTimer--;
            }
        }
    }

    private boolean isSafeBlock(BlockPos pos) {
        return this.mc.world.getBlockState(pos).isReplaceable()
            && this.mc.world.getFluidState(pos).isEmpty()
            && !this.mc.world.getBlockState(pos).isOf(Blocks.POWDER_SNOW);
    }

    static enum Mode {
        Normal,
        Strict,
        Vulcan,
        NCP;
    }

    public static enum VulcanMode {
        Jump,
        MiniJump;
    }
}
