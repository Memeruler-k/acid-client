package dev.abstr3act.addon.utils.render.shaders;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.abstr3act.addon.utils.render.shaders.satin.api.managed.ManagedCoreShader;
import dev.abstr3act.addon.utils.render.shaders.satin.api.managed.ShaderEffectManager;
import dev.abstr3act.addon.utils.render.shaders.satin.api.managed.uniform.Uniform1f;
import dev.abstr3act.addon.utils.render.shaders.satin.api.managed.uniform.Uniform2f;
import dev.abstr3act.addon.utils.render.shaders.satin.api.managed.uniform.Uniform4f;
import meteordevelopment.meteorclient.MeteorClient;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Identifier;

import java.awt.*;

public class RectangleShader {
    public static final ManagedCoreShader RECTANGLE_SHADER = ShaderEffectManager.getInstance()
        .manageCoreShader(Identifier.of("acid", "rectangle"), VertexFormats.POSITION);
    private Uniform2f uSize;
    private Uniform2f uLocation;
    private Uniform1f radius;
    private Uniform4f color1;
    private Uniform4f color2;
    private Uniform4f color3;
    private Uniform4f color4;

    public RectangleShader() {
        this.setup();
    }

    public void setParameters(float x, float y, float width, float height, float r, float alpha, Color c1, Color c2, Color c3, Color c4) {
        int i = MeteorClient.mc.options.getGuiScale().getValue();
        this.radius.set(r * i);
        this.uLocation.set(x * i, -y * i + MeteorClient.mc.getWindow().getScaledHeight() * i - height * i);
        this.uSize.set(width * i, height * i);
        this.color1.set(c1.getRed() / 255.0F, c1.getGreen() / 255.0F, c1.getBlue() / 255.0F, alpha);
        this.color2.set(c2.getRed() / 255.0F, c2.getGreen() / 255.0F, c2.getBlue() / 255.0F, alpha);
        this.color3.set(c3.getRed() / 255.0F, c3.getGreen() / 255.0F, c3.getBlue() / 255.0F, alpha);
        this.color4.set(c4.getRed() / 255.0F, c4.getGreen() / 255.0F, c4.getBlue() / 255.0F, alpha);
    }

    public void use() {
        RenderSystem.setShader(RECTANGLE_SHADER::getProgram);
    }

    protected void setup() {
        this.uSize = RECTANGLE_SHADER.findUniform2f("uSize");
        this.uLocation = RECTANGLE_SHADER.findUniform2f("uLocation");
        this.radius = RECTANGLE_SHADER.findUniform1f("radius");
        this.color1 = RECTANGLE_SHADER.findUniform4f("color1");
        this.color2 = RECTANGLE_SHADER.findUniform4f("color2");
        this.color3 = RECTANGLE_SHADER.findUniform4f("color3");
        this.color4 = RECTANGLE_SHADER.findUniform4f("color4");
    }
}
