package dev.abstr3act.addon.utils.render.shaders.satin.impl;

import net.minecraft.client.gl.PostEffectPass;
import net.minecraft.client.gl.ShaderProgram;

import java.util.List;

public abstract class ManagedUniformBase {
    protected final String name;

    public ManagedUniformBase(String name) {
        this.name = name;
    }

    public abstract boolean findUniformTargets(List<PostEffectPass> var1);

    public abstract boolean findUniformTarget(ShaderProgram var1);

    public String getName() {
        return this.name;
    }
}
