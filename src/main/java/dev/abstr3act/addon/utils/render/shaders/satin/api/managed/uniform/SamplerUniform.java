package dev.abstr3act.addon.utils.render.shaders.satin.api.managed.uniform;

import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.texture.AbstractTexture;

public interface SamplerUniform {
    void set(AbstractTexture var1);

    void set(Framebuffer var1);

    void set(int var1);
}
