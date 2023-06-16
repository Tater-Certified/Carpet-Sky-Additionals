package com.github.tatercertified.carpetskyadditionals.offline_player_utils;

import com.github.tatercertified.carpetskyadditionals.dimensions.PlayerSkyIslandWorld;
import com.github.tatercertified.carpetskyadditionals.dimensions.SkyIslandUtils;
import com.github.tatercertified.carpetskyadditionals.dimensions.SkyIslandWorld;
import com.mojang.authlib.GameProfile;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class OfflinePlayerUtils {

    /**
     * Gets a ServerPlayerEntity regardless if it is online or not
     * @param uuid UUID of the player
     * @param server MinecraftServer
     * @return Returns Optional of a ServerPlayerEntity if it exists
     */
    public static Optional<ServerPlayerEntity> getPlayer(MinecraftServer server, UUID uuid) {
        Optional<ServerPlayerEntity> player = Optional.ofNullable(server.getPlayerManager().getPlayer(uuid));
        if (player.isPresent()) {
            return player;
        } else {
            return getOfflinePlayer(server, uuid);
        }
    }

    /**
     * Creates a ServerPlayerEntity from the UserCache
     * @param server MinecraftServer
     * @param uuid UUID of the player
     * @return Returns Optional of a ServerPlayerEntity if the user exists in the UserCache
     */
    public static Optional<ServerPlayerEntity> getOfflinePlayer(MinecraftServer server, UUID uuid) {
        Optional<GameProfile> profile = server.getUserCache().getByUuid(uuid);
        return profile.map(gameProfile -> server.getPlayerManager().createPlayer(gameProfile));
    }

    /**
     * Loads the player data for offline users
     * @param server MinecraftServer
     * @param player ServerPlayerEntity that is getting its data loaded
     * @return PlayerData in the form of NbtCompound
     */
    public static NbtCompound getPlayerData(MinecraftServer server, ServerPlayerEntity player) {
        return server.getPlayerManager().loadPlayerData(player);
    }

    /**
     * Saves the player data and safely removes the ServerPlayerEntity from the MinecraftServer
     * @param player ServerPlayerEntity whose data is being saved
     * @param player_data ServerPlayerEntity's PlayerData
     * @param server MinecraftServer
     */
    public static void savePlayerData(ServerPlayerEntity player, NbtCompound player_data, MinecraftServer server) {
        player.saveNbt(player_data);
        player.remove(Entity.RemovalReason.DISCARDED);
        server.getPlayerManager().remove(player);
    }

    /**
     * Get the Optional of a GameProfile
     * @param uuid UUID of player
     * @param server MinecraftServer
     * @return Returns the Optional of a GameProfile
     */
    public static Optional<GameProfile> getGameProfile(UUID uuid, MinecraftServer server) {
        return server.getUserCache().getByUuid(uuid);
    }

    /**
     * Gets UUID of an offline ServerPlayerEntity
     * @param nbt ServerPlayerEntity's PlayerData
     * @return UUID of ServerPlayerEntity
     */
    public static UUID getUUID(NbtCompound nbt) {
        return nbt.getUuid("UUID");
    }

    /**
     * Checks if a ServerPlayerEntity is online
     * @param player ServerPlayerEntity
     * @param server MinecraftServer
     * @return Returns true if the ServerPlayerEntity is online
     */
    public static boolean isPlayerOnline(ServerPlayerEntity player, MinecraftServer server) {
        return server.getPlayerManager().getPlayerList().contains(player);
    }

    /**
     * Correctly sets the position of an offline player
     * @param player ServerPlayerEntity that is being teleported
     * @param pos Vec3d
     */
    public static void teleportOffline(ServerPlayerEntity player, Vec3d pos) {
        player.setPosition(pos);
    }

    /**
     * Correctly sets the dimension of an offline player
     * @param player_data NbtData for the ServerPlayerEntity
     * @param dimension ServerWorld that you want to place the player in
     */
    public static void setDimension(NbtCompound player_data, ServerWorld dimension) {
        player_data.putString("Dimension", dimension.getRegistryKey().getValue().toString());
    }

    /**
     * Correctly sets the GameMode of an offline player
     * @param player_data NbtData for the ServerPlayerEntity
     * @param mode GameMode that is being set
     */
    public static void setGameMode(NbtCompound player_data, GameMode mode) {
        player_data.putInt("playerGameType", mode.getId());
    }

    /**
     * Reads a ServerPlayerEntity's nbt data to extract their PlayerSkyIslandWorlds
     * @param nbt ServerPlayerEntity's PlayerData
     * @return a List of their PlayerSkyIslandWorlds
     */
    public static List<PlayerSkyIslandWorld> readPlayerIslandsNbt(NbtCompound nbt) {
        List<PlayerSkyIslandWorld> homes = new ArrayList<>();
        NbtList list = nbt.getList("home-islands", NbtElement.COMPOUND_TYPE);
        for (int i = 0; i < list.size(); i++) {
            NbtCompound compound = list.getCompound(i);
            PlayerSkyIslandWorld p_island;
            SkyIslandWorld home = SkyIslandUtils.getSkyIsland(compound.getLong("island-id"));
            if (home != null) {
                p_island = new PlayerSkyIslandWorld(home);
                p_island.fromNbt(compound);
                homes.add(p_island);
            }
        }
        return homes;
    }

    /**
     * Writes a List of PlayerSkyIslandWorlds to a ServerPlayerEntity's PlayerData
     * @param homes Player's List of PlayerSkyIslandWorlds
     * @param nbt Player's Nbt data
     */
    public static void writePlayerIslandNbt(List<PlayerSkyIslandWorld> homes, NbtCompound nbt) {
        NbtList nbtList = new NbtList();

        for (PlayerSkyIslandWorld island : homes) {
            NbtCompound compound = island.toNbt();
            nbtList.add(compound);
        }
        nbt.put("home-islands", nbtList);
    }

}
