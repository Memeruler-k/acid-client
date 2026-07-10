package dev.abstr3act.addon.utils.render.shaders;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.abstr3act.addon.utils.render.CaptureMark;
import dev.abstr3act.addon.utils.render.WindowResizeCallback;
import dev.abstr3act.addon.utils.render.shaders.satin.api.managed.ManagedCoreShader;
import dev.abstr3act.addon.utils.render.shaders.satin.api.managed.ShaderEffectManager;
import dev.abstr3act.addon.utils.render.shaders.satin.api.managed.uniform.Uniform1f;
import dev.abstr3act.addon.utils.render.shaders.satin.api.managed.uniform.Uniform2f;
import dev.abstr3act.addon.utils.render.shaders.satin.api.managed.uniform.Uniform4f;
import meteordevelopment.meteorclient.MeteorClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.SimpleFramebuffer;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Identifier;
import org.lwjgl.opengl.GL30;

import java.awt.*;

public class HudShader {
    public static final ManagedCoreShader hudShader = ShaderEffectManager.getInstance()
        .manageCoreShader(Identifier.of("acid", "hudshader"), VertexFormats.POSITION);
    private Uniform2f uSize;
    private Uniform2f uLocation;
    private Uniform1f radius;
    private Uniform1f blend;
    private Uniform1f alpha;
    private Uniform1f outline;
    private Uniform1f glow;
    private Uniform4f color1;
    private Uniform4f color2;
    private Uniform4f color3;
    private Uniform4f color4;
    private Framebuffer input;

    public HudShader() {
        this.setup();
    }

    public void setParameters(float x, float y, float width, float height, float r, float externalAlpha, float internalAlpha) {
        float i = (float) MeteorClient.mc.getWindow().getScaleFactor();
        if (this.input == null) {
            this.input = new SimpleFramebuffer(
                MeteorClient.mc.getWindow().getScaledWidth(), MeteorClient.mc.getWindow().getScaledHeight(), false, MinecraftClient.IS_SYSTEM_MAC
            );
        }

        this.radius.set(r * i);
        this.uLocation.set(x * i, -y * i + MeteorClient.mc.getWindow().getScaledHeight() * i - height * i);
        this.uSize.set(width * i, height * i);
        Color c1 = CaptureMark.getColor(270);
        Color c2 = CaptureMark.getColor(0);
        Color c3 = CaptureMark.getColor(180);
        Color c4 = CaptureMark.getColor(90);
        this.color1.set(c1.getRed() / 255.0F, c1.getGreen() / 255.0F, c1.getBlue() / 255.0F, externalAlpha);
        this.color2.set(c2.getRed() / 255.0F, c2.getGreen() / 255.0F, c2.getBlue() / 255.0F, externalAlpha);
        this.color3.set(c3.getRed() / 255.0F, c3.getGreen() / 255.0F, c3.getBlue() / 255.0F, externalAlpha);
        this.color4.set(c4.getRed() / 255.0F, c4.getGreen() / 255.0F, c4.getBlue() / 255.0F, externalAlpha);
        this.blend.set(10.0F);
        this.outline.set(10.0F);
        this.glow.set(10.0F);
        this.alpha.set(internalAlpha);
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

        RenderSystem.setShader(hudShader::getProgram);
    }

    protected void setup() {
        this.uSize = hudShader.findUniform2f("uSize");
        this.uLocation = hudShader.findUniform2f("uLocation");
        this.radius = hudShader.findUniform1f("radius");
        this.blend = hudShader.findUniform1f("blend");
        this.alpha = hudShader.findUniform1f("alpha");
        this.color1 = hudShader.findUniform4f("color1");
        this.color2 = hudShader.findUniform4f("color2");
        this.color3 = hudShader.findUniform4f("color3");
        this.color4 = hudShader.findUniform4f("color4");
        this.outline = hudShader.findUniform1f("outline");
        this.glow = hudShader.findUniform1f("glow");
        WindowResizeCallback.EVENT.register((WindowResizeCallback) (client, window) -> {
            if (this.input != null) {
                this.input.resize(window.getFramebufferWidth(), window.getFramebufferHeight(), MinecraftClient.IS_SYSTEM_MAC);
            }
        });
    }
}
