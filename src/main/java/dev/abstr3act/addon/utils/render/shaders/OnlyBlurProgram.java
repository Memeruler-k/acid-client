package dev.abstr3act.addon.utils.render.shaders;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.abstr3act.addon.utils.render.WindowResizeCallback;
import dev.abstr3act.addon.utils.render.shaders.satin.api.managed.ManagedCoreShader;
import dev.abstr3act.addon.utils.render.shaders.satin.api.managed.ShaderEffectManager;
import dev.abstr3act.addon.utils.render.shaders.satin.api.managed.uniform.SamplerUniform;
import dev.abstr3act.addon.utils.render.shaders.satin.api.managed.uniform.Uniform1f;
import dev.abstr3act.addon.utils.render.shaders.satin.api.managed.uniform.Uniform2f;
import meteordevelopment.meteorclient.MeteorClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.SimpleFramebuffer;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Identifier;
import org.lwjgl.opengl.GL30;

public class OnlyBlurProgram {
    public static final ManagedCoreShader BLUR = ShaderEffectManager.getInstance().manageCoreShader(Identifier.of("acid", "blur"), VertexFormats.POSITION);
    private Uniform2f uSize;
    private Uniform2f uLocation;
    private Uniform1f radius;
    private Uniform2f inputResolution;
    private Uniform1f quality;
    private SamplerUniform sampler;
    private Framebuffer input;

    public OnlyBlurProgram() {
        this.setup();
    }

    public void setParameters(float x, float y, float width, float height, float r, float blurStrenth) {
        if (this.input == null) {
            this.input = new SimpleFramebuffer(
                MeteorClient.mc.getWindow().getScaledWidth(), MeteorClient.mc.getWindow().getScaledHeight(), false, MinecraftClient.IS_SYSTEM_MAC
            );
        }

        float i = (float) MeteorClient.mc.getWindow().getScaleFactor();
        this.radius.set(r * i);
        this.uLocation.set(x * i, -y * i + MeteorClient.mc.getWindow().getScaledHeight() * i - height * i);
        this.uSize.set(width * i, height * i);
        this.quality.set(blurStrenth);
        this.sampler.set(this.input.getColorAttachment());
    }

    public void use() {
        Framebuffer buffer = MinecraftClient.getInstance().getFramebuffer();
        this.input.beginWrite(false);
        GL30.glBindFramebuffer(36008, buffer.fbo);
        GL30.glBlitFramebuffer(0, 0, buffer.textureWidth, buffer.textureHeight, 0, 0, buffer.textureWidth, buffer.textureHeight, 16384, 9729);
        buffer.beginWrite(false);
        if (this.input != null
            && (
            this.input.textureWidth != MeteorClient.mc.getWindow().getFramebufferWidth()
                || this.input.textureHeight != MeteorClient.mc.getWindow().getFramebufferHeight()
        )) {
            this.input.resize(MeteorClient.mc.getWindow().getFramebufferWidth(), MeteorClient.mc.getWindow().getFramebufferHeight(), MinecraftClient.IS_SYSTEM_MAC);
        }

        this.inputResolution.set(buffer.textureWidth, buffer.textureHeight);
        this.sampler.set(this.input.getColorAttachment());
        RenderSystem.setShader(BLUR::getProgram);
    }

    protected void setup() {
        this.inputResolution = BLUR.findUniform2f("InputResolution");
        this.quality = BLUR.findUniform1f("Quality");
        this.uSize = BLUR.findUniform2f("uSize");
        this.uLocation = BLUR.findUniform2f("uLocation");
        this.radius = BLUR.findUniform1f("radius");
        this.sampler = BLUR.findSampler("InputSampler");
        WindowResizeCallback.EVENT.register((WindowResizeCallback) (client, window) -> {
            if (this.input != null) {
                this.input.resize(window.getFramebufferWidth(), window.getFramebufferHeight(), MinecraftClient.IS_SYSTEM_MAC);
            }
        });
    }
}
