package com.github.tatercertified.carpetskyadditionals.dimensions;

import com.github.tatercertified.carpetskyadditionals.CarpetSkyAdditionals;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.world.GameMode;

import java.util.Objects;

public class SkyIslandUtils {

    public static SkyIslandWorld getSkyIsland(String name) {
        for (SkyIslandWorld island : SkyIslandManager.islands) {
            if (Objects.equals(island.getName(), name)) {
                return island;
            }
        }
        return null;
    }

    public static SkyIslandWorld getSkyIsland(ServerWorld island) {
        Identifier id = island.getRegistryKey().getRegistry();
        if (Objects.equals(id.getNamespace(), CarpetSkyAdditionals.MOD_ID)) {
            String name = id.getPath().replace("-nether", "").replace("-end", "");
            return getSkyIsland(name);
        } else {
            return getVanillaIsland();
        }
    }

    public static void teleportToIsland(ServerPlayerEntity player, ServerWorld island, GameMode set_gamemode) {
        player.teleport(island, 0, 60, 0, player.getYaw(), player.getPitch());
        player.changeGameMode(set_gamemode);
    }

    public static SkyIslandWorld getVanillaIsland() {
        for (SkyIslandWorld island : SkyIslandManager.islands) {
            if (island instanceof VanillaWorldOverride) {
                return island;
            }
        }
        return null;
    }
}
