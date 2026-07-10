package dev.abstr3act.addon.mixin.accessor;

import dev.abstr3act.addon.utils.notifications.MeshAccessor;
import meteordevelopment.meteorclient.renderer.Mesh;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin({Mesh.class})
public interface MeshMixin extends MeshAccessor {
    @Accessor(
        remap = false
    )
    int getIndicesCount();

    @Accessor(
        remap = false
    )
    void setIndicesCount(int var1);

    @Accessor(
        remap = false
    )
    long getIndicesPointer();
}
