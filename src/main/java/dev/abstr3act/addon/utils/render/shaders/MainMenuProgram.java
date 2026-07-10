package dev.abstr3act.addon.utils.render.shaders;

import dev.abstr3act.addon.utils.color.Color;
import dev.abstr3act.addon.utils.render.CaptureMark;
import dev.abstr3act.addon.utils.render.shaders.satin.api.managed.ManagedCoreShader;
import dev.abstr3act.addon.utils.render.shaders.satin.api.managed.ShaderEffectManager;
import dev.abstr3act.addon.utils.render.shaders.satin.api.managed.uniform.Uniform2f;
import meteordevelopment.meteorclient.MeteorClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.SimpleFramebuffer;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.Window;
import net.minecraft.util.Identifier;
import org.lwjgl.opengl.GL30;

public class MainMenuProgram {
    public static final ManagedCoreShader MAIN_MENU = ShaderEffectManager.getInstance()
        .manageCoreShader(Identifier.of("acid", "mainmenu"), VertexFormats.POSITION);
    public static float time_ = 10000.0F;
    private Uniform2f Time;
    private Uniform2f uSize;
    private Uniform2f color;
    private Framebuffer input;

    public MainMenuProgram() {
        this.setup();
    }

    public void setParameters(float x, float y, float width, float height) {
        float i = (float) MeteorClient.mc.getWindow().getScaleFactor();
        this.uSize.set(width * i, height * i);
        time_ = time_ + (float) (0.55 * AnimationUtility.deltaTime());
        this.Time.set(time_);
        this.color
            .set(
                new Color(
                    CaptureMark.getColor(0).getRed() / 255.0F,
                    CaptureMark.getColor(0).getGreen() / 255.0F,
                    CaptureMark.getColor(0).getBlue() / 255.0F,
                    CaptureMark.getColor(0).getAlpha() / 255.0F
                )
                    .getPacked()
            );
    }

    public void use() {
        Framebuffer buffer = MinecraftClient.getInstance().getFramebuffer();
        this.input.beginWrite(false);
        GL30.glBindFramebuffer(36008, buffer.fbo);
        GL30.glBlitFramebuffer(0, 0, buffer.textureWidth, buffer.textureHeight, 0, 0, buffer.textureWidth, buffer.textureHeight, 16384, 9729);
        buffer.beginWrite(false);
    }

    protected void setup() {
        this.uSize = MAIN_MENU.findUniform2f("uSize");
        this.Time = MAIN_MENU.findUniform2f("Time");
        this.color = MAIN_MENU.findUniform2f("color");
        Window window = MinecraftClient.getInstance().getWindow();
        this.input = new SimpleFramebuffer(window.getFramebufferWidth(), window.getFramebufferHeight(), false, MinecraftClient.IS_SYSTEM_MAC);
    }
}
