package com.github.tatercertified.carpetskyadditionals;

import carpet.CarpetExtension;
import carpet.CarpetServer;
import com.github.tatercertified.carpetskyadditionals.carpet.CarpetSkyAdditionalsSettings;
import com.github.tatercertified.carpetskyadditionals.command.CSAAdminCommand;
import com.github.tatercertified.carpetskyadditionals.command.CSACommand;
import com.github.tatercertified.carpetskyadditionals.dimensions.IslandPersistentState;
import com.github.tatercertified.carpetskyadditionals.dimensions.SkyIslandManager;
import com.github.tatercertified.carpetskyadditionals.dimensions.SkyIslandUtils;
import com.github.tatercertified.carpetskyadditionals.dimensions.SkyIslandWorld;
import com.github.tatercertified.carpetskyadditionals.interfaces.PlayerIslandDataInterface;
import com.google.common.reflect.TypeToken;
import com.google.gson.GsonBuilder;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.nbt.NbtList;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;

public class CarpetSkyAdditionals implements CarpetExtension {

    public static final String MOD_ID = "carpet_sky_additionals";

    public static void init() {}
    static {
        CarpetServer.manageExtension(new CarpetSkyAdditionals());
    }

    @Override
    public void onGameStarted() {
        CarpetServer.settingsManager.parseSettingsClass(CarpetSkyAdditionalsSettings.class);

        CSACommand.register();
        CSAAdminCommand.registerAdminCommand();

        ServerLifecycleEvents.SERVER_STARTED.register(server -> {

            IslandPersistentState state = IslandPersistentState.getServerState(server);
            SkyIslandManager.the_counter = state.counter;
            SkyIslandManager.loadIslands(server, state.islands);
            state.markDirty();
        });

        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            NbtList nbt_list = new NbtList();

            for (SkyIslandWorld island : SkyIslandUtils.getAllIslandsWithoutVanilla().values()) {
                nbt_list.add(island.getNBT());
            }

            IslandPersistentState state = IslandPersistentState.getServerState(server);
            state.counter = SkyIslandManager.the_counter;
            state.islands = nbt_list;
            state.markDirty();
        });

        ServerPlayerEvents.COPY_FROM.register((oldPlayer, newPlayer, alive) -> ((PlayerIslandDataInterface)newPlayer).setPlayerIslands(((PlayerIslandDataInterface)oldPlayer).getPlayerIslands()));
    }

    @Override
    public Map<String, String> canHasTranslations(String lang) {
        InputStream langFile = CarpetSkyAdditionals.class.getClassLoader().getResourceAsStream("assets/carpet-sky-additionals/lang/%s.json".formatted(lang));
        if (langFile == null) {
            return Collections.emptyMap();
        }
        String jsonData;
        try {
            jsonData = IOUtils.toString(langFile, StandardCharsets.UTF_8);
        } catch (IOException e) {
            return Collections.emptyMap();
        }
        return new GsonBuilder().create().fromJson(jsonData, new TypeToken<Map<String, String>>() {}.getType());
    }
}
