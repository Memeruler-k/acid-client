package dev.abstr3act.addon.utils.luna;

import dev.abstr3act.addon.utils.timers.Timer;
import meteordevelopment.meteorclient.MeteorClient;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class RotateManager {
    public static final Timer ROTATE_TIMER = new Timer();
    public static Vec3d directionVec = null;
    private static float renderPitch;
    private static float renderYawOffset;
    private static float prevPitch;
    private static float prevRenderYawOffset;
    private static float prevRotationYawHead;
    private static float rotationYawHead;
    public float rotateYaw = 0.0F;
    public float rotatePitch = 0.0F;
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

    public void setRotation(float yaw, float pitch, boolean force) {
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

    public int getYaw4D() {
        return MathHelper.floor(MeteorClient.mc.player.getYaw() * 4.0F / 360.0F + 0.5) & 3;
    }

    public String getDirection4D(boolean northRed) {
        int yaw = this.getYaw4D();
        if (yaw == 0) {
            return "South (+Z)";
        } else if (yaw == 1) {
            return "West (-X)";
        } else if (yaw == 2) {
            return (northRed ? "Â§c" : "") + "North (-Z)";
        } else {
            return yaw == 3 ? "East (+X)" : "Loading...";
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
