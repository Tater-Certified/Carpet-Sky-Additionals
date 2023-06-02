package com.github.tatercertified.carpetskyadditionals.mixin;

import com.github.tatercertified.carpetskyadditionals.dimensions.SkyIslandUtils;
import com.github.tatercertified.carpetskyadditionals.dimensions.SkyIslandWorld;
import com.github.tatercertified.carpetskyadditionals.interfaces.PlayerIslandDataInterface;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin implements PlayerIslandDataInterface {

    private SkyIslandWorld home = null;

    @Inject(method = "readCustomDataFromNbt", at = @At("HEAD"))
    public void readNBT(NbtCompound nbt, CallbackInfo ci) {
        home = SkyIslandUtils.getSkyIsland(nbt.getString("home-island"));
    }

    @Inject(method = "writeCustomDataToNbt", at = @At("HEAD"))
    public void writeNBT(NbtCompound nbt, CallbackInfo ci) {
        if (home != null) {
            nbt.putString("home-island", home.getName());
        }
    }

    @Override
    public SkyIslandWorld getHomeIsland() {
        return home;
    }

    @Override
    public void setHomeIsland(SkyIslandWorld island) {
        home = island;
    }
}
