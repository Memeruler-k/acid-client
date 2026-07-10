package dev.abstr3act.addon.utils.render.shaders.satin.api.managed;

import dev.abstr3act.addon.utils.render.shaders.satin.api.managed.uniform.UniformFinder;
import net.minecraft.client.gl.ShaderProgram;

public interface ManagedCoreShader extends UniformFinder {
    ShaderProgram getProgram();

    void release();
}
