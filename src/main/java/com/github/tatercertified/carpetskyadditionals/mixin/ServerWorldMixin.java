package com.github.tatercertified.carpetskyadditionals.mixin;

import com.github.tatercertified.carpetskyadditionals.dimensions.SkyIslandUtils;
import com.github.tatercertified.carpetskyadditionals.dimensions.SkyIslandWorld;
import net.minecraft.entity.boss.dragon.EnderDragonFight;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.WorldGenerationProgressListener;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.dimension.DimensionOptions;
import net.minecraft.world.dimension.DimensionTypes;
import net.minecraft.world.level.ServerWorldProperties;
import net.minecraft.world.level.storage.LevelStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.concurrent.Executor;

@Mixin(ServerWorld.class)
public abstract class ServerWorldMixin {
    @Mutable @Shadow @Final @Nullable private EnderDragonFight enderDragonFight;

    @Shadow @NotNull public abstract MinecraftServer getServer();

    @Inject(method = "<init>", at = @At("TAIL"))
    private void setDragonFight(MinecraftServer server, Executor workerExecutor, LevelStorage.Session session, ServerWorldProperties properties, RegistryKey worldKey, DimensionOptions dimensionOptions, WorldGenerationProgressListener worldGenerationProgressListener, boolean debugWorld, long seed, List spawners, boolean shouldTickTime, CallbackInfo ci) {
        SkyIslandWorld island = SkyIslandUtils.getSkyIsland((ServerWorld) (Object) this);
        if (((ServerWorld)(Object)this).getDimensionEntry().matchesKey(DimensionTypes.THE_END)) {
            // TODO Rewrite EnderDragonFight so that it is per Dimension rather than server-wide
            this.enderDragonFight = new EnderDragonFight(((ServerWorld) (Object) this), island.getSeed(), island.getDragonFight());

        }
    }

}
