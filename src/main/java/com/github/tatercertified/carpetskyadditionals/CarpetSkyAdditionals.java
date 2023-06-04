package com.github.tatercertified.carpetskyadditionals;

import carpet.CarpetExtension;
import carpet.CarpetServer;
import com.github.tatercertified.carpetskyadditionals.command.TestCommand;
import com.github.tatercertified.carpetskyadditionals.dimensions.*;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.SharedConstants;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;

import java.io.IOException;
import java.util.List;

public class CarpetSkyAdditionals implements CarpetExtension, ModInitializer {

    public static final String MOD_ID = "carpet_sky_additionals";

    public CarpetSkyAdditionals() {
        CarpetServer.manageExtension(this);
    }

    @Override
    public void onInitialize() {
        TestCommand.register();

        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            ServerWorld overworld = server.getOverworld();
            PersistentStateManager manager = overworld.getPersistentStateManager();
            NbtList list = null;

            if (manager.get(t -> new IslandPersistenState(), "islands-list") != null) {
                //TODO TESTING
                System.out.println("TESTING*1*!!!!!!!! ");
                NbtCompound compound;
                try {
                    compound = manager.readNbt("islands-list", SharedConstants.getGameVersion().getSaveVersion().getId());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                list = compound.getList("islands-list1", NbtElement.COMPOUND_TYPE);

            }
            SkyIslandManager.loadIslands(server, list);
        });

        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            ServerWorld overworld = server.getOverworld();
            PersistentStateManager manager = overworld.getPersistentStateManager();
            List<SkyIslandWorld> islands = SkyIslandUtils.getAllIslandsWithoutVanilla();
            NbtList nbt_list = new NbtList();

            for (SkyIslandWorld island : islands) {
                nbt_list.add(island.getNBT());
            }

            PersistentState state = new IslandPersistenState();
            NbtCompound compound = new NbtCompound();
            compound.put("islands-list1", nbt_list);
            //TODO TESTING
            System.out.println("TESTING*3*!!!!!!!! " + nbt_list);

            state.writeNbt(compound);

            manager.set("islands-list", state);
            manager.save();
        });
    }


}
