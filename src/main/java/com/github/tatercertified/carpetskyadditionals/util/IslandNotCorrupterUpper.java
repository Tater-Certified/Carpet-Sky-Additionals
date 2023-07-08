package com.github.tatercertified.carpetskyadditionals.util;

import com.github.tatercertified.carpetskyadditionals.dimensions.SkyIslandManager;
import com.github.tatercertified.carpetskyadditionals.dimensions.SkyIslandUtils;
import com.github.tatercertified.carpetskyadditionals.dimensions.SkyIslandWorld;
import com.github.tatercertified.carpetskyadditionals.offline_player_utils.OfflinePlayerUtils;
import net.minecraft.nbt.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.WorldSavePath;
import xyz.nucleoid.fantasy.Fantasy;

import java.io.File;
import java.nio.file.Path;
import java.util.*;
import java.util.logging.Logger;

public class IslandNotCorrupterUpper {
    private static final int data_version = 1;
    private static final Logger logger = Logger.getLogger("Sky Island Not Corrupter Upper");
    private static final List<UUID> migrated = new ArrayList<>();
    public static boolean should_update_player_data;
    private static int migration_type;
    public static SkyIslandWorld runIslandNotCorrupterUpper(NbtCompound data, MinecraftServer server, Fantasy fantasy) {
        if (!data.contains("data_version")) {
            data.putInt("data_version", 0);
        }
        int island_data_ver = data.getInt("data_version");
        if (island_data_ver != data_version) {
            switch (island_data_ver) {
                case 0 -> migrateFrom0(data, server);
            }
            should_update_player_data = true;
        }

        return new SkyIslandWorld(data_version, data.getLong("id"), data.getString("name"), server, fantasy, data.getLong("seed"), DragonNbtConverter.fromNBT(data.getCompound("dragon_fight")), data.getCompound("trader"));
    }

    public static int getDataVersion() {
        return data_version;
    }

    private static SkyIslandWorld legacyIslandSearch(String name) {
        for (SkyIslandWorld island : SkyIslandUtils.getAllIslandsWithoutVanilla().values()) {
            if (Objects.equals(island.getName(), name)) {
                return island;
            }
        }
        return null;
    }

    private static void migrateFrom0(NbtCompound data, MinecraftServer server) {
        logger.warning("Upgrading " + data.getString("name") + " to a newer nbt version!");

        if (data.contains("max_members")) {
            data.remove("max_members");
        }
        data.put("id", NbtLong.of(SkyIslandManager.updateCounter(server)));

        // Migrate dimension names
        String base_dimension_path = server.getSavePath(WorldSavePath.ROOT) + "/dimensions/carpet_sky_additionals/";
        String overworld_path = base_dimension_path + data.getString("name");
        String nether_path = overworld_path + "-nether";
        String end_path = overworld_path + "-end";

        File overworld = Path.of(overworld_path).toFile();
        File new_oveworld = Path.of(base_dimension_path + "/" + Long.toHexString(data.getLong("id"))).toFile();
        File nether = Path.of(nether_path).toFile();
        File new_nether = Path.of(base_dimension_path + "/" + Long.toHexString(data.getLong("id")) + "-nether").toFile();
        File end = Path.of(end_path).toFile();
        File new_end = Path.of(base_dimension_path + "/" + Long.toHexString(data.getLong("id")) + "-end").toFile();

        overworld.renameTo(new_oveworld);
        nether.renameTo(new_nether);
        end.renameTo(new_end);

        migration_type = 0;
        data.put("data_version", NbtInt.of(data_version));
    }
    public static void migratePlayerData(MinecraftServer server) {
        logger.warning("Migrating player data to a newer nbt version!");
        switch (migration_type) {
            case 0 -> migratePlayerDataFrom0(server);
        }
    }

    private static void migratePlayerDataFrom0(MinecraftServer server) {
        for (SkyIslandWorld island : SkyIslandUtils.getAllIslandsWithoutVanilla().values()) {
            for (UUID id : island.getMembersUUID()) {
                if (!migrated.contains(id)) {
                    Optional<ServerPlayerEntity> player = OfflinePlayerUtils.getPlayer(server, id);
                    if (player.isPresent()) {
                        NbtCompound nbt = OfflinePlayerUtils.getPlayerData(server, player.get());
                        NbtList list = nbt.getList("home-islands", NbtElement.COMPOUND_TYPE);
                        for (int i = 0; i < list.size(); i++) {
                            NbtCompound compound = list.getCompound(i);
                            if (compound.contains("island-name")) {
                                SkyIslandWorld island_world = legacyIslandSearch(compound.getString("island-name"));
                                compound.putLong("island-id", island_world.getIdentification());
                                compound.remove("island-name");
                            }
                        }
                        OfflinePlayerUtils.savePlayerData(player.get(), nbt, server);
                        migrated.add(id);
                    }
                }
            }
        }
    }
}
