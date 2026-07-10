package dev.abstr3act.addon.mixin.accessor;

import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin({Explosion.class})
public interface IExplosion {
    @Mutable
    @Accessor("x")
    void setX(double var1);

    @Mutable
    @Accessor("y")
    void setY(double var1);

    @Mutable
    @Accessor("z")
    void setZ(double var1);

    @Mutable
    @Accessor("entity")
    void setEntity(Entity var1);

    @Mutable
    @Accessor("power")
    float getPower();

    @Mutable
    @Accessor("world")
    World getWorld();

    @Mutable
    @Accessor("world")
    void setWorld(World var1);

    @Mutable
    @Accessor("damageSource")
    DamageSource getDamageSource();
}
