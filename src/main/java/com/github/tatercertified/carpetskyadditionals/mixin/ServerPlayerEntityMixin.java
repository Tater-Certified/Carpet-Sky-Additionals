package com.github.tatercertified.carpetskyadditionals.mixin;

import com.github.tatercertified.carpetskyadditionals.dimensions.SkyIslandUtils;
import com.github.tatercertified.carpetskyadditionals.dimensions.SkyIslandWorld;
import com.github.tatercertified.carpetskyadditionals.interfaces.PlayerIslandDataInterface;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin implements PlayerIslandDataInterface {

    private final List<SkyIslandWorld> homes = new ArrayList<>();

    @Inject(method = "readCustomDataFromNbt", at = @At("HEAD"))
    public void readNBT(NbtCompound nbt, CallbackInfo ci) {
        NbtList list = nbt.getList("home-islands", NbtElement.STRING_TYPE);
        for (int i = 0; i < list.size(); i++) {
            String name = list.getString(i);
            SkyIslandWorld home = SkyIslandUtils.getSkyIsland(name);
            if (home != null) {
                homes.add(home);
            }
        }
    }

    @Inject(method = "writeCustomDataToNbt", at = @At("HEAD"))
    public void writeNBT(NbtCompound nbt, CallbackInfo ci) {
        NbtList nbtList = new NbtList();
        for (SkyIslandWorld island : homes) {
            NbtString string = NbtString.of(island.getName());
            nbtList.add(string);
        }
        nbt.put("home-islands", nbtList);
    }

    @Override
    public List<SkyIslandWorld> getHomeIslands() {
        return homes;
    }

    @Override
    public void addHomeIsland(SkyIslandWorld island) {
        homes.add(island);
    }

    @Override
    public void removeHomeIsland(SkyIslandWorld island) {
        homes.remove(island);
    }
}
