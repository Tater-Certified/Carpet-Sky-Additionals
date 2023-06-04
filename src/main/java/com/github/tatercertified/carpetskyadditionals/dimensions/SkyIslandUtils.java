package com.github.tatercertified.carpetskyadditionals.dimensions;

import com.github.tatercertified.carpetskyadditionals.CarpetSkyAdditionals;
import com.github.tatercertified.carpetskyadditionals.interfaces.PlayerIslandDataInterface;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.WorldSavePath;
import net.minecraft.world.GameMode;
import org.apache.logging.log4j.LogManager;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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
        Identifier id = island.getRegistryKey().getValue();
        if (Objects.equals(id.getNamespace(), CarpetSkyAdditionals.MOD_ID)) {
            String name = id.getPath().replace("-nether", "").replace("-end", "");
            return getSkyIsland(name);
        } else {
            return getVanillaIsland();
        }
    }

    public static void teleportToIsland(ServerPlayerEntity player, ServerWorld island, GameMode set_gamemode) {
        player.teleport(island, 8.5, 64, 9.5, player.getYaw(), player.getPitch());
        player.changeGameMode(set_gamemode);
    }

    public static void offlineTeleportToHub(ServerPlayerEntity player, NbtCompound data) {
        player.setPosition(8.5, 64, 9.5);
        data.putInt("playerGameType", 0);
        player.setGameMode(data);
        savePlayerData(player);
    }

    public static void savePlayerData(ServerPlayerEntity player) {
        File playerDataDir = player.getServer().getSavePath(WorldSavePath.PLAYERDATA).toFile();
        try {
            NbtCompound compoundTag = player.writeNbt(new NbtCompound());
            File file = File.createTempFile(player.getUuidAsString() + "-", ".dat", playerDataDir);
            NbtIo.writeCompressed(compoundTag, file);
            File file2 = new File(playerDataDir, player.getUuidAsString() + ".dat");
            File file3 = new File(playerDataDir, player.getUuidAsString() + ".dat_old");
            Util.backupAndReplace(file2, file, file3);
        } catch (Exception var6) {
            LogManager.getLogger().warn("Failed to save player data for {}", player.getName().getString());
        }
    }

    public static SkyIslandWorld getVanillaIsland() {
        for (SkyIslandWorld island : SkyIslandManager.islands) {
            if (island instanceof VanillaWorldOverride) {
                return island;
            }
        }
        return null;
    }

    public static List<SkyIslandWorld> getAllIslands() {
        return SkyIslandManager.islands;
    }

    public static List<SkyIslandWorld> getAllIslandsWithoutVanilla() {
        List<SkyIslandWorld> list = getAllIslands();
        list.remove(getVanillaIsland());
        return list;
    }

    public static List<SkyIslandWorld> getAllUsersIslands(ServerPlayerEntity user) {
        //TODO Add verification
        return ((PlayerIslandDataInterface)user).getHomeIslands();
    }

    public static List<SkyIslandWorld> getAllNotUsersIslands(ServerPlayerEntity user) {
        List<SkyIslandWorld> list = getAllIslandsWithoutVanilla();
        list.removeAll(getAllUsersIslands(user));
        return list;
    }

    public static List<SkyIslandWorld> getAllOwnersIslands(ServerPlayerEntity owner) {
        List<SkyIslandWorld> owned = new ArrayList<>();
        for (SkyIslandWorld island : getAllUsersIslands(owner)) {
            if (Objects.equals(island.getOwner(), owner.getName().getString())) {
                owned.add(island);
            }
        }
        return owned;
    }

    private static List<SkyIslandWorld> verifyIslandList(List<SkyIslandWorld> islands) {
        if (new HashSet<>(getAllIslands()).containsAll(islands)) {
            return islands;
        } else {
            islands.removeIf(island -> !getAllIslands().contains(island));
        }
        return islands;
    }
}
