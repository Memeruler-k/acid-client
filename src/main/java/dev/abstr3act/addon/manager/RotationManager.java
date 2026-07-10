package dev.abstr3act.addon.manager;

import dev.abstr3act.addon.Compassion;
import dev.abstr3act.addon.events.Event;
import dev.abstr3act.addon.events.madcat.*;
import dev.abstr3act.addon.modules.Compassion.MoveFix;
import dev.abstr3act.addon.utils.render.MathUtility;
import dev.abstr3act.addon.utils.timers.Timer;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.packets.PacketEvent.Receive;
import meteordevelopment.meteorclient.events.packets.PacketEvent.Send;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket.Full;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket.LookAndOnGround;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.network.packet.s2c.play.PositionFlag;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class RotationManager {
    public static final Timer ROTATE_TIMER = new Timer();
    public static Vec3d directionVec = null;
    public static boolean lastGround;
    private static float renderPitch;
    private static float renderYawOffset;
    private static float prevPitch;
    private static float prevRenderYawOffset;
    private static float prevRotationYawHead;
    private static float rotationYawHead;
    public float nextYaw;
    public float nextPitch;
    public float rotationYaw = 0.0F;
    public float rotationPitch = 0.0F;
    public float lastYaw = 0.0F;
    public float lastPitch = 0.0F;
    private int ticksExisted;

    public static float getRenderPitch() {
        return renderPitch;
    }

    public static float getRotationYawHead() {
        return rotationYawHead;
    }

    public static float getRenderYawOffset() {
        return renderYawOffset;
    }

    public static float getPrevPitch() {
        return prevPitch;
    }

    public static float getPrevRotationYawHead() {
        return prevRotationYawHead;
    }

    public static float getPrevRenderYawOffset() {
        return prevRenderYawOffset;
    }

    public void init() {
        MeteorClient.EVENT_BUS.subscribe(this);
    }

    public void disable() {
        MeteorClient.EVENT_BUS.unsubscribe(this);
    }

    public void snapBack() {
        MeteorClient.mc
            .getNetworkHandler()
            .sendPacket(
                new Full(
                    MeteorClient.mc.player.getX(),
                    MeteorClient.mc.player.getY(),
                    MeteorClient.mc.player.getZ(),
                    Compassion.ROTATION.rotationYaw,
                    Compassion.ROTATION.rotationPitch,
                    MeteorClient.mc.player.isOnGround()
                )
            );
    }

    public void lookAt(Vec3d directionVec) {
        this.rotationTo(directionVec);
        this.snapAt(directionVec);
    }

    public void lookAt(BlockPos pos, Direction side) {
        Vec3d hitVec = pos.toCenterPos().add(new Vec3d(side.getVector().getX() * 0.5, side.getVector().getY() * 0.5, side.getVector().getZ() * 0.5));
        this.lookAt(hitVec);
    }

    public void snapAt(float yaw, float pitch) {
        this.setRenderRotation(yaw, pitch, true);
        if (MoveFix.INSTANCE.grimRotation.get()) {
            MeteorClient.mc
                .getNetworkHandler()
                .sendPacket(
                    new Full(MeteorClient.mc.player.getX(), MeteorClient.mc.player.getY(), MeteorClient.mc.player.getZ(), yaw, pitch, MeteorClient.mc.player.isOnGround())
                );
        } else {
            MeteorClient.mc.getNetworkHandler().sendPacket(new LookAndOnGround(yaw, pitch, MeteorClient.mc.player.isOnGround()));
        }
    }

    public void snapAt(Vec3d directionVec) {
        float[] angle = this.getRotation(directionVec);
        if (!MoveFix.INSTANCE.noSpamRotation.get()
            || !(MathHelper.angleBetween(angle[0], Compassion.ROTATION.lastYaw) < (MoveFix.INSTANCE.fov.get()).floatValue())
            || !(Math.abs(angle[1] - Compassion.ROTATION.lastPitch) < (MoveFix.INSTANCE.fov.get()).floatValue())) {
            this.snapAt(angle[0], angle[1]);
        }
    }

    public float[] getRotation(Vec3d eyesPos, Vec3d vec) {
        double diffX = vec.x - eyesPos.x;
        double diffY = vec.y - eyesPos.y;
        double diffZ = vec.z - eyesPos.z;
        double diffXZ = Math.sqrt(diffX * diffX + diffZ * diffZ);
        float yaw = (float) Math.toDegrees(Math.atan2(diffZ, diffX)) - 90.0F;
        float pitch = (float) (-Math.toDegrees(Math.atan2(diffY, diffXZ)));
        return new float[]{MathHelper.wrapDegrees(yaw), MathHelper.wrapDegrees(pitch)};
    }

    public float[] getRotation(Vec3d vec) {
        Vec3d eyesPos = EntityUtil.getEyesPos();
        return this.getRotation(eyesPos, vec);
    }

    public void rotationTo(Vec3d vec3d) {
        ROTATE_TIMER.reset();
        directionVec = vec3d;
    }

    public boolean inFov(Vec3d directionVec, float fov) {
        float[] angle = this.getRotation(
            new Vec3d(
                MeteorClient.mc.player.getX(),
                MeteorClient.mc.player.getY() + MeteorClient.mc.player.getEyeHeight(MeteorClient.mc.player.getPose()),
                MeteorClient.mc.player.getZ()
            ),
            directionVec
        );
        return this.inFov(angle[0], angle[1], fov);
    }

    public boolean inFov(float yaw, float pitch, float fov) {
        return MathHelper.angleBetween(yaw, this.rotationYaw) + Math.abs(pitch - this.rotationPitch) <= fov;
    }

    @EventHandler
    public void update(MovementPacketsEvent event) {
        if (MoveFix.INSTANCE.isActive()) {
            event.setYaw(this.nextYaw);
            event.setPitch(this.nextPitch);
        } else {
            RotateEvent event1 = new RotateEvent(event.getYaw(), event.getPitch());
            MeteorClient.EVENT_BUS.post(event1);
            event.setYaw(event1.getYaw());
            event.setPitch(event1.getPitch());
        }
    }

    @EventHandler(
        priority = -200
    )
    public void update(UpdateWalkingPlayerEvent event) {
        if (MoveFix.INSTANCE.isActive() && !((MoveFix.UpdateMode) MoveFix.INSTANCE.updateMode.get()).equals(MoveFix.UpdateMode.UpdateMouse) && event.isPost()) {
            this.updateNext();
        }
    }

    @EventHandler(
        priority = -200
    )
    public void update(MouseUpdateEvent event) {
        if (MeteorClient.mc.player != null
            && MoveFix.INSTANCE.isActive()
            && !((MoveFix.UpdateMode) MoveFix.INSTANCE.updateMode.get()).equals(MoveFix.UpdateMode.MovementPacket)) {
            this.updateNext();
        }
    }

    private void updateNext() {
        RotateEvent rotateEvent = new RotateEvent(MeteorClient.mc.player.getYaw(), MeteorClient.mc.player.getPitch());
        MeteorClient.EVENT_BUS.post(rotateEvent);
        if (rotateEvent.isModified()) {
            this.nextYaw = rotateEvent.getYaw();
            this.nextPitch = rotateEvent.getPitch();
        } else {
            float[] newAngle = this.injectStep(new float[]{rotateEvent.getYaw(), rotateEvent.getPitch()}, (MoveFix.INSTANCE.step.get()).floatValue());
            this.nextYaw = newAngle[0];
            this.nextPitch = newAngle[1];
        }

        MoveFix.INSTANCE.fixRotation = this.nextYaw;
        MoveFix.fixPitch = this.nextPitch;
    }

    @EventHandler(
        priority = -200
    )
    public void onLastRotation(RotateEvent event) {
        LookAtEvent lookAtEvent = new LookAtEvent();
        MeteorClient.EVENT_BUS.post(lookAtEvent);
        if (lookAtEvent.getRotation()) {
            float[] newAngle = this.injectStep(new float[]{lookAtEvent.getYaw(), lookAtEvent.getPitch()}, lookAtEvent.getSpeed());
            event.setYaw(newAngle[0]);
            event.setPitch(newAngle[1]);
        } else if (lookAtEvent.getTarget() != null) {
            float[] newAngle = this.injectStep(lookAtEvent.getTarget(), lookAtEvent.getSpeed());
            event.setYaw(newAngle[0]);
            event.setPitch(newAngle[1]);
        } else if (!event.isModified()
            && MoveFix.INSTANCE.look.get()
            && directionVec != null
            && !ROTATE_TIMER.passed((long) (MoveFix.INSTANCE.rotationTime.get() * 1000.0))) {
            float[] newAngle = this.injectStep(directionVec, (MoveFix.INSTANCE.step.get()).floatValue());
            event.setYaw(newAngle[0]);
            event.setPitch(newAngle[1]);
        }
    }

    public float[] injectStep(Vec3d vec, float steps) {
        float currentYaw = MoveFix.INSTANCE.forceSync.get() ? this.lastYaw : this.rotationYaw;
        float currentPitch = MoveFix.INSTANCE.forceSync.get() ? this.lastPitch : this.rotationPitch;
        float yawDelta = MathHelper.wrapDegrees(
            (float) MathHelper.wrapDegrees(Math.toDegrees(Math.atan2(vec.z - MeteorClient.mc.player.getZ(), vec.x - MeteorClient.mc.player.getX())) - 90.0)
                - currentYaw
        );
        float pitchDelta = (float) (
            -Math.toDegrees(
                Math.atan2(
                    vec.y - (MeteorClient.mc.player.getPos().y + MeteorClient.mc.player.getEyeHeight(MeteorClient.mc.player.getPose())),
                    Math.sqrt(Math.pow(vec.x - MeteorClient.mc.player.getX(), 2.0) + Math.pow(vec.z - MeteorClient.mc.player.getZ(), 2.0))
                )
            )
        )
            - currentPitch;
        float angleToRad = (float) Math.toRadians(27 * (MeteorClient.mc.player.age % 30));
        float var15 = (float) (yawDelta + Math.sin(angleToRad) * 3.0) + MathUtility.random(-1.0F, 1.0F);
        float var16 = pitchDelta + MathUtility.random(-0.6F, 0.6F);
        if (var15 > 180.0F) {
            var15 -= 180.0F;
        }

        float yawStepVal = 180.0F * steps;
        float clampedYawDelta = MathHelper.clamp(MathHelper.abs(var15), -yawStepVal, yawStepVal);
        float clampedPitchDelta = MathHelper.clamp(var16, -45.0F, 45.0F);
        float newYaw = currentYaw + (var15 > 0.0F ? clampedYawDelta : -clampedYawDelta);
        float newPitch = MathHelper.clamp(currentPitch + clampedPitchDelta, -90.0F, 90.0F);
        double gcdFix = Math.pow(MeteorClient.mc.options.getMouseSensitivity().getValue() * 0.6 + 0.2, 3.0) * 1.2;
        return new float[]{(float) (newYaw - (newYaw - currentYaw) % gcdFix), (float) (newPitch - (newPitch - currentPitch) % gcdFix)};
    }

    public float[] injectStep(float[] angle, float steps) {
        if (steps < 0.01F) {
            steps = 0.01F;
        }

        if (steps > 1.0F) {
            steps = 1.0F;
        }

        if (steps < 1.0F && angle != null) {
            float packetYaw = MoveFix.INSTANCE.forceSync.get() ? this.lastYaw : this.rotationYaw;
            float diff = MathHelper.angleBetween(angle[0], packetYaw);
            if (Math.abs(diff) > 180.0F * steps) {
                angle[0] = packetYaw + diff * (180.0F * steps / Math.abs(diff));
            }

            float packetPitch = MoveFix.INSTANCE.forceSync.get() ? this.lastPitch : this.rotationPitch;
            diff = angle[1] - packetPitch;
            if (Math.abs(diff) > 90.0F * steps) {
                angle[1] = packetPitch + diff * (90.0F * steps / Math.abs(diff));
            }
        }

        return new float[]{angle[0], angle[1]};
    }

    @EventHandler(
        priority = -999
    )
    public void onPacketSend(Send event) {
        if (MeteorClient.mc.player != null && !event.isCancelled()) {
            if (event.packet instanceof PlayerMoveC2SPacket packet) {
                if (packet.changesLook()) {
                    this.lastYaw = packet.getYaw(this.lastYaw);
                    this.lastPitch = packet.getPitch(this.lastPitch);
                    this.setRenderRotation(this.lastYaw, this.lastPitch, false);
                }

                lastGround = packet.isOnGround();
            }
        }
    }

    @EventHandler(
        priority = 100
    )
    public void onReceivePacket(Receive event) {
        if (MeteorClient.mc.player != null) {
            if (event.packet instanceof PlayerPositionLookS2CPacket packet) {
                if (packet.getFlags().contains(PositionFlag.X_ROT)) {
                    this.lastYaw = this.lastYaw + packet.getYaw();
                } else {
                    this.lastYaw = packet.getYaw();
                }

                if (packet.getFlags().contains(PositionFlag.Y_ROT)) {
                    this.lastPitch = this.lastPitch + packet.getPitch();
                } else {
                    this.lastPitch = packet.getPitch();
                }

                this.setRenderRotation(this.lastYaw, this.lastPitch, true);
            }
        }
    }

    @EventHandler
    public void onUpdateWalkingPost(UpdateWalkingPlayerEvent event) {
        if (event.getStage() == Event.Stage.Post) {
            this.setRenderRotation(this.lastYaw, this.lastPitch, false);
        }
    }

    public void setRenderRotation(float yaw, float pitch, boolean force) {
        if (MeteorClient.mc.player != null) {
            if (MeteorClient.mc.player.age != this.ticksExisted || force) {
                this.ticksExisted = MeteorClient.mc.player.age;
                prevPitch = renderPitch;
                prevRenderYawOffset = renderYawOffset;
                renderYawOffset = this.getRenderYawOffset(yaw, prevRenderYawOffset);
                prevRotationYawHead = rotationYawHead;
                rotationYawHead = yaw;
                renderPitch = pitch;
            }
        }
    }

    private float getRenderYawOffset(float yaw, float offsetIn) {
        float result = offsetIn;
        double xDif = MeteorClient.mc.player.getX() - MeteorClient.mc.player.prevX;
        double zDif = MeteorClient.mc.player.getZ() - MeteorClient.mc.player.prevZ;
        if (xDif * xDif + zDif * zDif > 0.0025000002F) {
            float offset = (float) MathHelper.atan2(zDif, xDif) * (180.0F / (float) Math.PI) - 90.0F;
            float wrap = MathHelper.abs(MathHelper.wrapDegrees(yaw) - offset);
            if (95.0F < wrap && wrap < 265.0F) {
                result = offset - 180.0F;
            } else {
                result = offset;
            }
        }

        if (MeteorClient.mc.player.handSwingProgress > 0.0F) {
            result = yaw;
        }

        result = offsetIn + MathHelper.wrapDegrees(result - offsetIn) * 0.3F;
        float offset = MathHelper.wrapDegrees(yaw - result);
        if (offset < -75.0F) {
            offset = -75.0F;
        } else if (offset >= 75.0F) {
            offset = 75.0F;
        }

        float var11 = yaw - offset;
        if (offset * offset > 2500.0F) {
            var11 += offset * 0.2F;
        }

        return var11;
    }
}
