package com.github.tatercertified.carpetskyadditionals;

import carpet.CarpetExtension;
import carpet.CarpetServer;
import com.github.tatercertified.carpetskyadditionals.command.CSACommand;
import com.github.tatercertified.carpetskyadditionals.dimensions.IslandPersistentState;
import com.github.tatercertified.carpetskyadditionals.dimensions.SkyIslandManager;
import com.github.tatercertified.carpetskyadditionals.dimensions.SkyIslandUtils;
import com.github.tatercertified.carpetskyadditionals.dimensions.SkyIslandWorld;
import com.github.tatercertified.carpetskyadditionals.interfaces.PlayerIslandDataInterface;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.nbt.NbtList;

public class CarpetSkyAdditionals implements CarpetExtension, ModInitializer {

    public static final String MOD_ID = "carpet_sky_additionals";

    public CarpetSkyAdditionals() {
        CarpetServer.manageExtension(this);
    }

    @Override
    public void onInitialize() {
        CSACommand.register();

        ServerLifecycleEvents.SERVER_STARTED.register(server -> {

            IslandPersistentState state = IslandPersistentState.getServerState(server);
            SkyIslandManager.loadIslands(server, state.islands);
            state.markDirty();
        });

        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            NbtList nbt_list = new NbtList();

            for (SkyIslandWorld island : SkyIslandUtils.getAllIslandsWithoutVanilla().values()) {
                nbt_list.add(island.getNBT());
            }

            IslandPersistentState state = IslandPersistentState.getServerState(server);
            state.islands = nbt_list;
            state.markDirty();
        });

        ServerPlayerEvents.COPY_FROM.register((oldPlayer, newPlayer, alive) -> ((PlayerIslandDataInterface)newPlayer).setPlayerIslands(((PlayerIslandDataInterface)oldPlayer).getPlayerIslands()));
    }

}
