package com.github.tatercertified.carpetskyadditionals;

import carpet.CarpetExtension;
import carpet.CarpetServer;
import com.github.tatercertified.carpetskyadditionals.command.TestCommand;
import com.github.tatercertified.carpetskyadditionals.dimensions.SkyIslandManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;

public class CarpetSkyAdditionals implements CarpetExtension, ModInitializer {

    public static final String MOD_ID = "carpet_sky_additionals";

    public CarpetSkyAdditionals() {
        CarpetServer.manageExtension(this);
    }

    @Override
    public void onInitialize() {
        TestCommand.register();
        ServerLifecycleEvents.SERVER_STARTED.register((SkyIslandManager::loadIslands));
    }


}
