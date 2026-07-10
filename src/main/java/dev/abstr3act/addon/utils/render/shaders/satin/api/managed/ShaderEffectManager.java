package dev.abstr3act.addon.utils.render.shaders.satin.api.managed;

import dev.abstr3act.addon.utils.render.shaders.satin.impl.ReloadableShaderEffectManager;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.util.Identifier;

import java.util.function.Consumer;

public interface ShaderEffectManager {
    static ShaderEffectManager getInstance() {
        return ReloadableShaderEffectManager.INSTANCE;
    }

    ManagedShaderEffect manage(Identifier var1);

    ManagedShaderEffect manage(Identifier var1, Consumer<ManagedShaderEffect> var2);

    ManagedCoreShader manageCoreShader(Identifier var1);

    ManagedCoreShader manageCoreShader(Identifier var1, VertexFormat var2);

    ManagedCoreShader manageCoreShader(Identifier var1, VertexFormat var2, Consumer<ManagedCoreShader> var3);
}
