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

    public static SkyIslandWorld getSkyIsland(String name) {
        return getAllIslands().get(name);
    }

    public static SkyIslandWorld getSkyIsland(ServerWorld island) {
        Identifier id = island.getRegistryKey().getValue();
        if (Objects.equals(id.getNamespace(), CarpetSkyAdditionals.MOD_ID)) {
            String name = id.getPath().replace("-nether", "").replace("-end", "");
            return getSkyIsland(name);
        } else {
            return getVanillaIsland();
        }
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
        SkyIslandWorld island = getSkyIsland(name);

        if (dimension.equals(name)) {
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
        return SkyIslandManager.islands.get("overworld");
    }

    public static Map<String, SkyIslandWorld> getAllIslands() {
        return SkyIslandManager.islands;
    }

    public static Map<String, SkyIslandWorld> getAllIslandsWithoutVanilla() {
        Map<String, SkyIslandWorld> map = new HashMap<>(Map.copyOf(getAllIslands()));
        map.remove("overworld");
        return map;
    }

    public static Map<String, SkyIslandWorld> getAllUsersIslands(ServerPlayerEntity user) {
        return verifyIslandList(((PlayerIslandDataInterface) user).getHomeIslands());
    }

    public static Map<String, SkyIslandWorld> getAllNotUsersIslands(ServerPlayerEntity user) {
        Map<String, SkyIslandWorld> map = new HashMap<>(Map.copyOf(getAllIslandsWithoutVanilla()));
        Map<String, SkyIslandWorld> userIslands = getAllUsersIslands(user);

        map.entrySet().removeIf(entry -> userIslands.containsValue(entry.getValue()));

        return map;
    }

    public static Map<String, SkyIslandWorld> getAllOwnersIslands(ServerPlayerEntity owner) {
        List<SkyIslandWorld> owned = new ArrayList<>();
        for (SkyIslandWorld island : getAllUsersIslands(owner).values()) {
            if (Objects.equals(island.getOwner(), owner.getUuid())) {
                owned.add(island);
            }
        }
        return listToMap(owned);
    }

    private static Map<String, SkyIslandWorld> verifyIslandList(List<SkyIslandWorld> islands) {
        if (getAllIslands().values().containsAll(islands)) {
            return listToMap(islands);
        } else {
            islands.removeIf(island -> !getAllIslands().containsValue(island));
        }

        return listToMap(islands);
    }

    public static boolean islandAvailable(String name) {
        return !getAllIslands().containsKey(name);
    }

    private static Map<String, SkyIslandWorld> listToMap(List<SkyIslandWorld> islands) {
        Map<String, SkyIslandWorld> map = new HashMap<>();

        for (SkyIslandWorld island : islands) {
            map.put(island.getName(), island);
        }

        return map;
    }
}
