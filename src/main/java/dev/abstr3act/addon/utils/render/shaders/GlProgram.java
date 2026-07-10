package dev.abstr3act.addon.utils.render.shaders;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.abstr3act.addon.mixin.accessor.ShaderProgramAccessor;
import net.minecraft.client.gl.GlUniform;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.resource.ResourceFactory;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class GlProgram {
    private static final List<Pair<Function<ResourceFactory, ShaderProgram>, Consumer<ShaderProgram>>> REGISTERED_PROGRAMS = new ArrayList<>();
    public ShaderProgram backingProgram;

    public GlProgram(Identifier id, VertexFormat vertexFormat) {
        try {
            REGISTERED_PROGRAMS.add(new Pair((Function<ResourceFactory, ShaderProgram>) resourceFactory -> {
                try {
                    return new THShaderProgram(resourceFactory, id.toString(), vertexFormat);
                } catch (IOException var4x) {
                    var4x.printStackTrace();
                    throw new RuntimeException("Failed to initialize shader program", var4x);
                }
            }, (Consumer<ShaderProgram>) program -> {
                this.backingProgram = program;
                this.setup();
            }));
        } catch (Exception var4) {
            var4.printStackTrace();
        }
    }

    public static void setUpProgram() {
    }

    @Internal
    public static void forEachProgram(Consumer<Pair<Function<ResourceFactory, ShaderProgram>, Consumer<ShaderProgram>>> loader) {
        REGISTERED_PROGRAMS.forEach(loader);
    }

    public void use() {
        RenderSystem.setShader(() -> this.backingProgram);
    }

    protected void setup() {
    }

    @Nullable
    protected GlUniform findUniform(String name) {
        return ((ShaderProgramAccessor) this.backingProgram).getUniformsHook().get(name);
    }

    public static class THShaderProgram extends ShaderProgram {
        private THShaderProgram(ResourceFactory factory, String name, VertexFormat format) throws IOException {
            super(factory, name, format);
        }
    }
}
