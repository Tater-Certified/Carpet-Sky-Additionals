package com.github.tatercertified.carpetskyadditionals.command;

import com.github.tatercertified.carpetskyadditionals.dimensions.SkyIslandUtils;
import com.github.tatercertified.carpetskyadditionals.dimensions.SkyIslandWorld;
import com.github.tatercertified.carpetskyadditionals.interfaces.PlayerIslandDataInterface;
import com.mojang.brigadier.arguments.LongArgumentType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.argument.UuidArgumentType;
import net.minecraft.server.network.ServerPlayerEntity;


import java.util.UUID;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class EmergencyCommand {

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(literal("islands-emergency")
                .requires(source -> source.hasPermissionLevel(4))
                .then(literal("add-home"))
                .then(argument("uuid", UuidArgumentType.uuid())
                        .then(argument("id", LongArgumentType.longArg())
                                .executes(context -> {
                                    addHome(LongArgumentType.getLong(context, "id"), UuidArgumentType.getUuid(context, "uuid"));
                                    return 0;
                                })
                ))));
    }

    public static void addHome(long island_id, UUID uuid) {
        SkyIslandWorld island = SkyIslandUtils.getSkyIsland(island_id);
        ServerPlayerEntity player = island.getServer().getPlayerManager().getPlayer(uuid);
        assert player != null;
        ((PlayerIslandDataInterface)player).addHomeIsland(island);
    }
}
