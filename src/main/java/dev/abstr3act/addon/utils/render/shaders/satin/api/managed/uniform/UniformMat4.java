package dev.abstr3act.addon.utils.render.shaders.satin.api.managed.uniform;

import org.joml.Matrix4f;

public interface UniformMat4 {
    void set(Matrix4f var1);

    void setFromArray(float[] var1);
}
