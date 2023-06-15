package com.github.tatercertified.carpetskyadditionals.dimensions;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import xyz.nucleoid.fantasy.Fantasy;

public class VanillaWorldOverride extends SkyIslandWorld{

    private final MinecraftServer server;

    public VanillaWorldOverride(int data_version, long id, String name, MinecraftServer server, Fantasy fantasy, long seed, NbtCompound dragon_fight) {
        super(data_version, id, name, server, fantasy, seed, dragon_fight);
        this.server = server;
    }


    @Override
    public ServerWorld getOverworld() {
        return server.getOverworld();
    }

    @Override
    public ServerWorld getNether() {
        return server.getWorld(World.NETHER);
    }

    @Override
    public ServerWorld getEnd() {
        return server.getWorld(World.END);
    }

    @Override
    public void createAllWorlds(MinecraftServer server, Fantasy fantasy) {
    }


}
