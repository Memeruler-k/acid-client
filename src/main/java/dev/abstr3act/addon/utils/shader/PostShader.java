package dev.abstr3act.addon.utils.shader;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.abstr3act.addon.mixin.accessor.IPostEffectProcessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.*;
import net.minecraft.util.Identifier;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class PostShader {
    protected final MinecraftClient mc = MinecraftClient.getInstance();
    private final Identifier location;
    private final Map<String, Framebuffer> frameMap = new HashMap<>(256);
    public Consumer<PostShader> initCallback;
    protected PostEffectProcessor shader;

    public PostShader(Identifier id, Consumer<PostShader> initCallback) {
        this.initCallback = initCallback;
        this.location = id;
        this.initShader();
    }

    public ShaderUniform set(String name) {
        return this.findUniform(name);
    }

    public void render(float tickDelta) {
        PostEffectProcessor sg = this.getShader();
        if (sg != null) {
            RenderSystem.disableBlend();
            RenderSystem.disableDepthTest();
            RenderSystem.resetTextureMatrix();
            sg.render(tickDelta);
            MinecraftClient.getInstance().getFramebuffer().beginWrite(true);
            RenderSystem.disableBlend();
            RenderSystem.blendFunc(770, 771);
            RenderSystem.enableDepthTest();
        }
    }

    protected void setup() {
        if (this.initCallback != null) {
            this.initCallback.accept(this);
        }

        this.shader.setupDimensions(this.mc.getWindow().getFramebufferWidth(), this.mc.getWindow().getFramebufferHeight());
    }

    protected ShaderUniform findUniform(String name) {
        if (this.shader == null) {
            this.initShader();
        }

        List<Uniform> uniforms = new ArrayList<>();

        for (PostEffectPass pass : ((IPostEffectProcessor) this.shader).getPasses()) {
            JsonEffectShaderProgram program = pass.getProgram();
            uniforms.add(program.getUniformByNameOrDummy(name));
        }

        return new ShaderUniform(uniforms);
    }

    public PostEffectProcessor getShader() {
        if (this.shader == null) {
            this.initShader();
        }

        return this.shader;
    }

    protected PostEffectProcessor parseShader(MinecraftClient mc, Identifier location) throws IOException {
        return new PostEffectProcessor(mc.getTextureManager(), mc.getResourceManager(), mc.getFramebuffer(), location);
    }

    private void initShader() {
        try {
            this.shader = this.parseShader(this.mc, this.location);
            this.shader.setupDimensions(this.mc.getWindow().getFramebufferWidth(), this.mc.getWindow().getFramebufferHeight());
            if (this.initCallback != null) {
                this.initCallback.accept(this);
            }
        } catch (IOException var2) {
            throw new RuntimeException("Failed to initialized post shader program", var2);
        }
    }

    public void set(String name, int value) {
        this.set(name).set(value);
    }

    public void set(String name, float value) {
        this.set(name).set(value);
    }

    public void set(String name, float v0, float v1) {
        this.set(name).set(v0, v1);
    }

    public void set(String name, float v0, float v1, float v2, float v3) {
        this.set(name).set(v0, v1, v2, v3);
    }

    public void set(String name, float v0, float v1, float v2) {
        this.set(name).set(v0, v1, v2);
    }
}
