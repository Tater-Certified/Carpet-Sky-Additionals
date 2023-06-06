package com.github.tatercertified.carpetskyadditionals.mixin;

import com.github.tatercertified.carpetskyadditionals.dimensions.PlayerSkyIslandWorld;
import com.github.tatercertified.carpetskyadditionals.interfaces.PlayerIslandDataInterface;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerManager.class)
public class PlayerManagerMixin {
    private final ThreadLocal<PlayerSkyIslandWorld> p_island = new ThreadLocal<>();

    @Inject(method = "respawnPlayer", at = @At("HEAD"))
    private void respawnPlayer(ServerPlayerEntity player, boolean alive, CallbackInfoReturnable<ServerPlayerEntity> cir) {
        p_island.set(((PlayerIslandDataInterface)player).getPIsland(player.getWorld()));
    }

    @Redirect(method = "respawnPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerEntity;getSpawnPointPosition()Lnet/minecraft/util/math/BlockPos;"))
    public BlockPos carpet_sky_additionals$getIslandBlockPos(ServerPlayerEntity instance) {
        if (p_island.get() != null) {
            return p_island.get().getSpawnPos();
        }
        return null;
    }

    @Redirect(method = "respawnPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;getWorld(Lnet/minecraft/registry/RegistryKey;)Lnet/minecraft/server/world/ServerWorld;"))
    public ServerWorld carpet_sky_additionals$getIslandDimension(MinecraftServer instance, RegistryKey<World> key) {
        if (p_island.get() != null) {
            return instance.getWorld(p_island.get().getSpawnDimension());
        } else {
            return instance.getOverworld();
        }
    }

    @Redirect(method = "respawnPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;getOverworld()Lnet/minecraft/server/world/ServerWorld;"))
    public ServerWorld carpet_sky_additionals$getIslandOverworld(MinecraftServer instance) {
        if (p_island.get() != null) {
            return p_island.get().getSkyIslandWorld().getOverworld();
        } else {
            return instance.getOverworld();
        }
    }

}
