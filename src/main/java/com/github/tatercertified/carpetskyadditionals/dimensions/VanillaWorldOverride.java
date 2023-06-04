package com.github.tatercertified.carpetskyadditionals.dimensions;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import xyz.nucleoid.fantasy.Fantasy;

public class VanillaWorldOverride extends SkyIslandWorld{

    private final MinecraftServer server;
    public VanillaWorldOverride(String name, int max_members, MinecraftServer server, Fantasy fantasy, Long seed, NbtCompound dragon_fight) {
        super(name, max_members, server, fantasy, seed, dragon_fight);
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
