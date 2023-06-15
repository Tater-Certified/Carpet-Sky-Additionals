package com.github.tatercertified.carpetskyadditionals.util;

import com.github.tatercertified.carpetskyadditionals.dimensions.SkyIslandManager;
import com.github.tatercertified.carpetskyadditionals.dimensions.SkyIslandWorld;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtInt;
import net.minecraft.nbt.NbtLong;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.WorldSavePath;
import xyz.nucleoid.fantasy.Fantasy;

import java.io.File;
import java.nio.file.Path;
import java.util.logging.Logger;

public class IslandNotCorrupterUpper {
    private static final int data_version = 1;
    private static final Logger logger = Logger.getLogger("Sky Island Not Corrupter Upper");
    public static SkyIslandWorld runIslandNotCorrupterUpper(NbtCompound data, MinecraftServer server, Fantasy fantasy) {
        if (!data.contains("data_version")) {
            data.putInt("data_version", 0);
        }
        int island_data_ver = data.getInt("data_version");
        if (island_data_ver != data_version) {
            switch (island_data_ver) {
                case 0 -> migrateFrom0(data, server);
            }
        }

        return new SkyIslandWorld(data_version, data.getLong("id"), data.getString("name"), server, fantasy, data.getLong("seed"), data.getCompound("dragon_fight"));
    }

    public static int getDataVersion() {
        return data_version;
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

        data.put("data_version", NbtInt.of(data_version));
    }
}
