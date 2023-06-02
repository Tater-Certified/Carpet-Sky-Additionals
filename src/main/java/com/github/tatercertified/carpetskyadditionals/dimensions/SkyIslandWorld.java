package com.github.tatercertified.carpetskyadditionals.dimensions;

import com.github.tatercertified.carpetskyadditionals.CarpetSkyAdditionals;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.world.Difficulty;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionTypes;
import org.joml.Random;
import xyz.nucleoid.fantasy.Fantasy;
import xyz.nucleoid.fantasy.RuntimeWorldConfig;
import xyz.nucleoid.fantasy.RuntimeWorldHandle;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SkyIslandWorld {
    private String name;
    private int max_members;
    private RuntimeWorldHandle overworld_handle;
    private RuntimeWorldHandle nether_handle;
    private RuntimeWorldHandle end_handle;
    private final List<ServerPlayerEntity> members = new ArrayList<>();

    public SkyIslandWorld(String name, int max_members, MinecraftServer server, Fantasy fantasy) {
        this.name = name;
        this.max_members = max_members;
        createAllWorlds(server, fantasy);
    }
    public void createAllWorlds(MinecraftServer server, Fantasy fantasy) {
        // Overworld

        RuntimeWorldConfig worldConfig = new RuntimeWorldConfig()
                .setDimensionType(DimensionTypes.OVERWORLD)
                .setDifficulty(Difficulty.HARD)
                .setGenerator(server.getOverworld().getChunkManager().getChunkGenerator());

        overworld_handle = fantasy.getOrOpenPersistentWorld(new Identifier(CarpetSkyAdditionals.MOD_ID, name), worldConfig);

        // Nether

        RuntimeWorldConfig worldConfig1 = new RuntimeWorldConfig()
                .setDimensionType(DimensionTypes.THE_NETHER)
                .setDifficulty(Difficulty.HARD)
                .setGenerator(Objects.requireNonNull(server.getWorld(World.NETHER)).getChunkManager().getChunkGenerator());

        nether_handle = fantasy.getOrOpenPersistentWorld(new Identifier(CarpetSkyAdditionals.MOD_ID, name + "-nether"), worldConfig1);

        // End

        RuntimeWorldConfig worldConfig2 = new RuntimeWorldConfig()
                .setDimensionType(DimensionTypes.THE_END)
                .setDifficulty(Difficulty.HARD)
                .setGenerator(server.getWorld(World.END).getChunkManager().getChunkGenerator());

        end_handle = fantasy.getOrOpenPersistentWorld(new Identifier(CarpetSkyAdditionals.MOD_ID, name + "-end"), worldConfig2);
    }

    public ServerWorld getOverworld() {
        return overworld_handle.asWorld();
    }

    public ServerWorld getNether() {
        return nether_handle.asWorld();
    }

    public ServerWorld getEnd() {
        return end_handle.asWorld();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getMaxMembers() {
        return max_members;
    }

    public void setMaxMembers(int new_max) {
        this.max_members = new_max;
    }

    public boolean tryAddMember(ServerPlayerEntity player) {
        if (members.size() < max_members) {
            members.add(player);
            return true;
        }
        return false;
    }

    public void removeMember(ServerPlayerEntity removed) {
        members.remove(removed);
    }

    public void remove() {
        overworld_handle.delete();
        nether_handle.delete();
        end_handle.delete();
    }
}
