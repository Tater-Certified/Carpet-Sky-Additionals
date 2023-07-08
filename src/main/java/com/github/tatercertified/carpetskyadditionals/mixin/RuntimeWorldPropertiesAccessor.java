package com.github.tatercertified.carpetskyadditionals.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import xyz.nucleoid.fantasy.RuntimeWorldConfig;
import xyz.nucleoid.fantasy.RuntimeWorldProperties;

@Mixin(RuntimeWorldProperties.class)
public interface RuntimeWorldPropertiesAccessor {
    @Accessor()
    RuntimeWorldConfig getConfig();
}
