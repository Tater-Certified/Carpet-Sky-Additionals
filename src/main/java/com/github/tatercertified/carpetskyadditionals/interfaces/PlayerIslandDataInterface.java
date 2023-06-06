package com.github.tatercertified.carpetskyadditionals.interfaces;

import com.github.tatercertified.carpetskyadditionals.dimensions.PlayerSkyIslandWorld;
import com.github.tatercertified.carpetskyadditionals.dimensions.SkyIslandWorld;
import net.minecraft.server.world.ServerWorld;

import java.util.List;

public interface PlayerIslandDataInterface {
    List<SkyIslandWorld> getHomeIslands();

    void addHomeIsland(SkyIslandWorld island);

    void removeHomeIsland(SkyIslandWorld island);

    List<PlayerSkyIslandWorld> getPlayerIslands();

    void setPlayerIslands(List<PlayerSkyIslandWorld> islands);

    PlayerSkyIslandWorld getPIsland(ServerWorld world);
}
