package com.github.tatercertified.carpetskyadditionals.interfaces;

import com.github.tatercertified.carpetskyadditionals.dimensions.SkyIslandWorld;

import java.util.List;

public interface PlayerIslandDataInterface {
    List<SkyIslandWorld> getHomeIslands();

    void addHomeIsland(SkyIslandWorld island);

    void removeHomeIsland(SkyIslandWorld island);

    void setHomeIslands(List<SkyIslandWorld> islands);
}
