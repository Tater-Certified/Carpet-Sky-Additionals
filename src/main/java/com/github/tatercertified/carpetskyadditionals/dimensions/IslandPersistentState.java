package com.github.tatercertified.carpetskyadditionals.dimensions;

import com.github.tatercertified.carpetskyadditionals.CarpetSkyAdditionals;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtLong;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.World;

public class IslandPersistentState extends PersistentState {
    public NbtList islands = new NbtList();
    public long counter = 0;

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        nbt.put("islands-list", islands);
        nbt.put("counter", NbtLong.of(counter));
        return nbt;
    }

    public static IslandPersistentState createFromNbt(NbtCompound nbt) {
        IslandPersistentState state = new IslandPersistentState();
        state.islands = nbt.getList("islands-list", NbtElement.COMPOUND_TYPE);
        state.counter = nbt.getLong("counter");
        return state;
    }


    public static IslandPersistentState getServerState(MinecraftServer server) {
        PersistentStateManager persistentStateManager = server.getWorld(World.OVERWORLD).getPersistentStateManager();

        return persistentStateManager.getOrCreate(
                IslandPersistentState::createFromNbt,
                IslandPersistentState::new,
                CarpetSkyAdditionals.MOD_ID);
    }
}
