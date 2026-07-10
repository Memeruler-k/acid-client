package dev.abstr3act.addon.events;

import dev.abstr3act.addon.utils.Renderer3D;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;

public class EventRender3D {
    private static final EventRender3D INSTANCE = new EventRender3D();
    public MatrixStack matrices;
    public Matrix4f matrix4f;
    public Renderer3D renderer;
    public double frameTime;
    public float tickDelta;
    public double offsetX;
    public double offsetY;
    public double offsetZ;

    public static EventRender3D get(MatrixStack matrices, Renderer3D renderer, float tickDelta, double offsetX, double offsetY, double offsetZ, Matrix4f matrix4f) {
        INSTANCE.matrices = matrices;
        INSTANCE.renderer = renderer;
        INSTANCE.tickDelta = tickDelta;
        INSTANCE.offsetX = offsetX;
        INSTANCE.offsetY = offsetY;
        INSTANCE.offsetZ = offsetZ;
        INSTANCE.matrix4f = matrix4f;
        return INSTANCE;
    }

    public static class Unlimited {
        private static final Unlimited INSTANCE = new Unlimited();
        public MatrixStack matrices;
        public double frameTime;
        public float tickDelta;

        public static Unlimited get(MatrixStack matrices, float tickDelta) {
            INSTANCE.matrices = matrices;
            INSTANCE.tickDelta = tickDelta;
            return INSTANCE;
        }
    }
}
