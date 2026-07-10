package dev.abstr3act.addon.utils;

import dev.abstr3act.addon.events.EventFixVelocity;
import dev.abstr3act.addon.events.EventKeyboardInput;
import dev.abstr3act.addon.events.EventPlayerJump;
import dev.abstr3act.addon.events.EventSync;
import dev.abstr3act.addon.mixin.accessor.IClientPlayerEntity;
import dev.abstr3act.addon.module.BaseModule;
import dev.abstr3act.addon.modules.Compassion.MoveFix;
import dev.abstr3act.addon.utils.math.MathUtility;
import dev.abstr3act.addon.utils.seraphim.PlayerUtils;
import dev.abstr3act.addon.utils.timers.Timer;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.packets.PacketEvent.Receive;
import meteordevelopment.meteorclient.events.packets.PacketEvent.Send;
import meteordevelopment.meteorclient.events.world.TickEvent.Post;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.network.packet.s2c.play.UpdateSelectedSlotS2CPacket;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.*;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.RaycastContext.FluidHandling;
import net.minecraft.world.RaycastContext.ShapeType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayDeque;

public class PlayerManager {
    public final Timer switchTimer = new Timer();
    private final ArrayDeque<Float> speedResult = new ArrayDeque<>(20);
    public float yaw;
    public float pitch;
    public float lastYaw;
    public float lastPitch;
    public float currentPlayerSpeed;
    public float averagePlayerSpeed;
    public int ticksElytraFlying;
    public int serverSideSlot;
    public float bodyYaw;
    public float prevBodyYaw;
    public boolean inInventory;

    public static boolean shieldBreaker(boolean instant, Entity target) {
        int axeSlot = InventoryUtility.getAxe().slot();
        if (axeSlot == -1) {
            return false;
        } else if (!(target instanceof PlayerEntity)) {
            return false;
        } else if (!((PlayerEntity) target).isUsingItem() && !instant) {
            return false;
        } else if (((PlayerEntity) target).getOffHandStack().getItem() != Items.SHIELD && ((PlayerEntity) target).getMainHandStack().getItem() != Items.SHIELD) {
            return false;
        } else {
            if (axeSlot >= 9) {
                MeteorClient.mc
                    .interactionManager
                    .clickSlot(
                        MeteorClient.mc.player.currentScreenHandler.syncId,
                        axeSlot,
                        MeteorClient.mc.player.getInventory().selectedSlot,
                        SlotActionType.SWAP,
                        MeteorClient.mc.player
                    );
                PlayerUtils.sendPacket(new CloseHandledScreenC2SPacket(MeteorClient.mc.player.currentScreenHandler.syncId));
                MeteorClient.mc.interactionManager.attackEntity(MeteorClient.mc.player, target);
                Wrapper.swingHand(false);
                MeteorClient.mc
                    .interactionManager
                    .clickSlot(
                        MeteorClient.mc.player.currentScreenHandler.syncId,
                        axeSlot,
                        MeteorClient.mc.player.getInventory().selectedSlot,
                        SlotActionType.SWAP,
                        MeteorClient.mc.player
                    );
                PlayerUtils.sendPacket(new CloseHandledScreenC2SPacket(MeteorClient.mc.player.currentScreenHandler.syncId));
            } else {
                PlayerUtils.sendPacket(new UpdateSelectedSlotC2SPacket(axeSlot));
                MeteorClient.mc.interactionManager.attackEntity(MeteorClient.mc.player, target);
                Wrapper.swingHand(false);
                PlayerUtils.sendPacket(new UpdateSelectedSlotC2SPacket(MeteorClient.mc.player.getInventory().selectedSlot));
            }

            return true;
        }
    }

    public static float[] calcAngle(Vec3d to) {
        if (to == null) {
            return null;
        } else {
            double difX = to.x - MeteorClient.mc.player.getEyePos().x;
            double difY = (to.y - MeteorClient.mc.player.getEyePos().y) * -1.0;
            double difZ = to.z - MeteorClient.mc.player.getEyePos().z;
            double dist = MathHelper.sqrt((float) (difX * difX + difZ * difZ));
            return new float[]{
                (float) MathHelper.wrapDegrees(Math.toDegrees(Math.atan2(difZ, difX)) - 90.0), (float) MathHelper.wrapDegrees(Math.toDegrees(Math.atan2(difY, dist)))
            };
        }
    }

    public static Vec2f calcAngleVec(Vec3d to) {
        if (to == null) {
            return null;
        } else {
            double difX = to.x - MeteorClient.mc.player.getEyePos().x;
            double difY = (to.y - MeteorClient.mc.player.getEyePos().y) * -1.0;
            double difZ = to.z - MeteorClient.mc.player.getEyePos().z;
            double dist = MathHelper.sqrt((float) (difX * difX + difZ * difZ));
            return new Vec2f(
                (float) MathHelper.wrapDegrees(Math.toDegrees(Math.atan2(difZ, difX)) - 90.0), (float) MathHelper.wrapDegrees(Math.toDegrees(Math.atan2(difY, dist)))
            );
        }
    }

    public static float[] calcAngle(Vec3d from, Vec3d to) {
        if (to == null) {
            return null;
        } else {
            double difX = to.x - from.x;
            double difY = (to.y - from.y) * -1.0;
            double difZ = to.z - from.z;
            double dist = MathHelper.sqrt((float) (difX * difX + difZ * difZ));
            return new float[]{
                (float) MathHelper.wrapDegrees(Math.toDegrees(Math.atan2(difZ, difX)) - 90.0), (float) MathHelper.wrapDegrees(Math.toDegrees(Math.atan2(difY, dist)))
            };
        }
    }

    @EventHandler(
        priority = 200
    )
    public void onSync(EventSync event) {
        if (!BaseModule.fullNullCheck()) {
            this.yaw = MeteorClient.mc.player.getYaw();
            this.pitch = MeteorClient.mc.player.getPitch();
            this.lastYaw = ((IClientPlayerEntity) MeteorClient.mc.player).getLastYaw();
            this.lastPitch = ((IClientPlayerEntity) MeteorClient.mc.player).getLastPitch();
            if (MeteorClient.mc.currentScreen == null) {
                this.inInventory = false;
            }

            if (MeteorClient.mc.player.isFallFlying() && MeteorClient.mc.player.getEquippedStack(EquipmentSlot.CHEST).getItem() == Items.ELYTRA) {
                this.ticksElytraFlying++;
            } else {
                this.ticksElytraFlying = 0;
            }
        }
    }

    @EventHandler
    public void onTick(Post e) {
        this.currentPlayerSpeed = (float) Math.hypot(
            MeteorClient.mc.player.getX() - MeteorClient.mc.player.prevX, MeteorClient.mc.player.getZ() - MeteorClient.mc.player.prevZ
        );
        if (this.speedResult.size() > 20) {
            this.speedResult.poll();
        }

        this.speedResult.add(this.currentPlayerSpeed);
        float average = 0.0F;

        for (Float value : this.speedResult) {
            average += MathUtility.clamp(value, 0.0F, 20.0F);
        }

        this.averagePlayerSpeed = average / this.speedResult.size();
    }

    @EventHandler(
        priority = -200
    )
    public void postSync(Post event) {
        if (MeteorClient.mc.player != null) {
            this.prevBodyYaw = this.bodyYaw;
            this.bodyYaw = this.getBodyYaw();
            MoveFix moveFix = (MoveFix) Modules.get().get(MoveFix.class);
            if (moveFix.isActive()) {
                if (!moveFix.clientLook.get()) {
                    MeteorClient.mc.player.setYaw(this.yaw);
                    MeteorClient.mc.player.setPitch(this.pitch);
                }

                moveFix.fixRotation = Float.NaN;
            }
        }
    }

    @EventHandler
    public void onJump(EventPlayerJump e) {
        ((MoveFix) Modules.get().get(MoveFix.class)).onJump(e);
    }

    @EventHandler
    public void onPlayerMove(EventFixVelocity e) {
        ((MoveFix) Modules.get().get(MoveFix.class)).onPlayerMove(e);
    }

    @EventHandler
    public void onKeyInput(EventKeyboardInput e) {
        ((MoveFix) Modules.get().get(MoveFix.class)).onKeyInput(e);
    }

    @EventHandler
    public void onSyncWithServer(@NotNull Send event) {
        if (event.packet instanceof ClickSlotC2SPacket) {
            this.inInventory = true;
        }

        if (event.packet instanceof UpdateSelectedSlotC2SPacket slot) {
            this.switchTimer.reset();
            this.serverSideSlot = slot.getSelectedSlot();
        }

        if (event.packet instanceof CloseHandledScreenC2SPacket) {
            this.inInventory = false;
        }
    }

    @EventHandler
    public void onPacketReceive(@NotNull Receive event) {
        if (event.packet instanceof UpdateSelectedSlotS2CPacket slot) {
            this.switchTimer.reset();
            this.serverSideSlot = slot.getSlot();
        }
    }

    private float getBodyYaw() {
        double x = MeteorClient.mc.player.getX() - MeteorClient.mc.player.prevX;
        double z = MeteorClient.mc.player.getZ() - MeteorClient.mc.player.prevZ;
        float offset = this.bodyYaw;
        if (x * x + z * z > 0.0025000002F) {
            offset = (float) (MathHelper.atan2(z, x) * 180.0F / (float) Math.PI - 90.0);
        }

        if (MeteorClient.mc.player.handSwingProgress > 0.0F) {
            offset = ((IClientPlayerEntity) MinecraftClient.getInstance().player).getLastYaw();
        }

        float deltaBodyYaw = MathHelper.clamp(
            MathHelper.wrapDegrees(
                ((IClientPlayerEntity) MinecraftClient.getInstance().player).getLastYaw() - (this.bodyYaw + MathHelper.wrapDegrees(offset - this.bodyYaw) * 0.3F)
            ),
            -45.0F,
            75.0F
        );
        return (deltaBodyYaw > 50.0F ? deltaBodyYaw * 0.2F : 0.0F) + ((IClientPlayerEntity) MinecraftClient.getInstance().player).getLastYaw() - deltaBodyYaw;
    }

    public boolean isInWeb() {
        Box pBox = MeteorClient.mc.player.getBoundingBox();
        BlockPos pBlockPos = BlockPos.ofFloored(MeteorClient.mc.player.getPos());

        for (int x = pBlockPos.getX() - 2; x <= pBlockPos.getX() + 2; x++) {
            for (int y = pBlockPos.getY() - 1; y <= pBlockPos.getY() + 4; y++) {
                for (int z = pBlockPos.getZ() - 2; z <= pBlockPos.getZ() + 2; z++) {
                    BlockPos bp = new BlockPos(x, y, z);
                    if (pBox.intersects(new Box(bp)) && MeteorClient.mc.world.getBlockState(bp).getBlock() == Blocks.COBWEB) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public HitResult rayTrace(double dst, float yaw, float pitch, double x, double y, double z) {
        Vec3d vec3d = new Vec3d(x, y, z);
        Vec3d vec3d2 = this.getRotationVector(pitch, yaw);
        Vec3d vec3d3 = vec3d.add(vec3d2.x * dst, vec3d2.y * dst, vec3d2.z * dst);
        return MeteorClient.mc.world.raycast(new RaycastContext(vec3d, vec3d3, ShapeType.OUTLINE, FluidHandling.NONE, MeteorClient.mc.player));
    }

    @NotNull
    public Vec3d getRotationVector(float yaw, float pitch) {
        return new Vec3d(
            MathHelper.sin(-pitch * (float) (Math.PI / 180.0)) * MathHelper.cos(yaw * (float) (Math.PI / 180.0)),
            -MathHelper.sin(yaw * (float) (Math.PI / 180.0)),
            MathHelper.cos(-pitch * (float) (Math.PI / 180.0)) * MathHelper.cos(yaw * (float) (Math.PI / 180.0))
        );
    }
}
