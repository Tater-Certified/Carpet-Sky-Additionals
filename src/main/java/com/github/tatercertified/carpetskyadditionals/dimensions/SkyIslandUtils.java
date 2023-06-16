package com.github.tatercertified.carpetskyadditionals.dimensions;

import com.github.tatercertified.carpetskyadditionals.CarpetSkyAdditionals;
import com.github.tatercertified.carpetskyadditionals.interfaces.PlayerIslandDataInterface;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.world.GameMode;
import net.minecraft.world.World;

import java.util.*;

public class SkyIslandUtils {

    public static Map<Long, SkyIslandWorld> converter = new HashMap<>();

    public static void addToConverter(SkyIslandWorld island) {
        converter.put(island.getIdentification(), island);
    }

    public static void removeFromConverter(SkyIslandWorld island) {
        converter.remove(island.getIdentification());
    }

    public static SkyIslandWorld getSkyIsland(long id) {
        return converter.get(id);
    }

    public static SkyIslandWorld getSkyIsland(ServerWorld island) {
        Identifier id = island.getRegistryKey().getValue();
        if (Objects.equals(id.getNamespace(), CarpetSkyAdditionals.MOD_ID)) {
            String name = id.getPath().replace("-nether", "").replace("-end", "");
            return getSkyIsland(getIdFromHex(name));
        } else {
            return getVanillaIsland();
        }
    }

    public static long getIdFromHex(String hex) {
        return Long.parseLong(hex, 16);
    }

    public static RegistryKey<World> getDimensionFromString(String dimension) {
        switch (dimension) {
            case "ovewrold" -> {
                return World.OVERWORLD;
            }
            case "the_nether" -> {
                return World.NETHER;
            }
            case "the_end" -> {
                return World.END;
            }
        }
        String name = dimension.replace("-nether", "").replace("-end", "");
        SkyIslandWorld island = getSkyIsland(getIdFromHex(name));

        if (dimension.equals(String.valueOf(island.getIdentification()))) {
            return island.getOverworld().getRegistryKey();
        } else if (dimension.contains("-nether")) {
            return island.getNether().getRegistryKey();
        } else {
            return island.getEnd().getRegistryKey();
        }
    }

    public static void teleportToIsland(ServerPlayerEntity player, ServerWorld island, GameMode set_gamemode) {
        player.teleport(island, 8.5, 64, 9.5, player.getYaw(), player.getPitch());
        player.changeGameMode(set_gamemode);
    }

    public static SkyIslandWorld getVanillaIsland() {
        return getSkyIsland(-1);
    }

    public static Map<Long, SkyIslandWorld> getAllIslands() {
        return converter;
    }

    public static Map<Long, SkyIslandWorld> getAllIslandsWithoutVanilla() {
        Map<Long, SkyIslandWorld> map = new HashMap<>(Map.copyOf(getAllIslands()));
        map.remove(-1L);
        return map;
    }

    public static Map<Long, SkyIslandWorld> getAllUsersIslands(ServerPlayerEntity user) {
        return verifyIslandList(((PlayerIslandDataInterface) user).getHomeIslands());
    }

    public static Map<Long, SkyIslandWorld> getAllNotUsersIslands(ServerPlayerEntity user) {
        Map<Long, SkyIslandWorld> map = new HashMap<>(Map.copyOf(getAllIslandsWithoutVanilla()));
        Map<Long, SkyIslandWorld> userIslands = getAllUsersIslands(user);

        map.entrySet().removeIf(entry -> userIslands.containsValue(entry.getValue()));

        return map;
    }

    public static Map<Long, SkyIslandWorld> getAllOwnersIslands(ServerPlayerEntity owner) {
        List<SkyIslandWorld> owned = new ArrayList<>();
        for (SkyIslandWorld island : getAllUsersIslands(owner).values()) {
            if (Objects.equals(island.getOwner(), owner.getUuid())) {
                owned.add(island);
            }
        }
        return listToMap(owned);
    }

    private static Map<Long, SkyIslandWorld> verifyIslandList(List<SkyIslandWorld> islands) {
        if (getAllIslands().values().containsAll(islands)) {
            return listToMap(islands);
        } else {
            islands.removeIf(island -> !getAllIslands().containsValue(island));
        }

        return listToMap(islands);
    }

    private static Map<Long, SkyIslandWorld> listToMap(List<SkyIslandWorld> islands) {
        Map<Long, SkyIslandWorld> map = new HashMap<>();

        for (SkyIslandWorld island : islands) {
            map.put(island.getIdentification(), island);
        }

        return map;
    }
}
