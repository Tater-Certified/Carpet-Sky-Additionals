package com.github.tatercertified.carpetskyadditionals.mixin;

import com.jsorrell.carpetskyadditions.commands.SkyIslandCommand;
import net.minecraft.registry.Registry;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(SkyIslandCommand.class)
public interface SkyIslandCommandInvoker {
    @Invoker
    static ConfiguredFeature<?, ?> invokeGetIslandFeature(Registry<ConfiguredFeature<?, ?>> cfr) {
        throw new AssertionError();
    }
}
