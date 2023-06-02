package com.github.tatercertified.carpetskyadditionals.mixin;

import com.github.tatercertified.carpetskyadditionals.dimensions.SkyIslandUtils;
import com.github.tatercertified.carpetskyadditionals.dimensions.SkyIslandWorld;
import com.github.tatercertified.carpetskyadditionals.interfaces.EntityIslandDataInterface;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Entity.class)
public abstract class EntityMixin implements EntityIslandDataInterface {

    @Shadow public abstract World getWorld();

    @Override
    public SkyIslandWorld getCurrentIsland() {
        return SkyIslandUtils.getSkyIsland((ServerWorld) this.getWorld());
    }
}
