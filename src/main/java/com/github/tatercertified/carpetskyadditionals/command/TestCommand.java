package com.github.tatercertified.carpetskyadditionals.command;

import com.github.tatercertified.carpetskyadditionals.dimensions.SkyIslandManager;
import com.github.tatercertified.carpetskyadditionals.dimensions.SkyIslandUtils;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.text.Text;

public class TestCommand {
    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(CommandManager.literal("islands")
                .executes(context -> {
                    context.getSource().sendMessage(Text.literal(SkyIslandManager.listIslands()));
                    return 0;
                })
                .then(CommandManager.literal("create")
                        .then(CommandManager.argument("name", StringArgumentType.string())
                                .executes(context -> {
                                    SkyIslandManager.createIsland(StringArgumentType.getString(context, "name"), context.getSource().getServer(), context.getSource().getPlayer());
                                    return 0;
                                })))
                .then(CommandManager.literal("remove")
                        .then(CommandManager.argument("name", StringArgumentType.string())
                                .executes(context -> {
                                    SkyIslandManager.removeIsland(StringArgumentType.getString(context, "name"));
                                    return 0;
                                })))
                .then(CommandManager.literal("join")
                        .then(CommandManager.argument("name", StringArgumentType.string())
                                .executes(context -> {
                                    SkyIslandManager.joinIsland(SkyIslandUtils.getSkyIsland(StringArgumentType.getString(context, "name")), context.getSource().getPlayer());
                                    return 0;
                                })))
                .then(CommandManager.literal("leave")
                        .then(CommandManager.argument("name", StringArgumentType.string())
                                .executes(context -> {
                                    SkyIslandManager.leaveIsland(SkyIslandUtils.getSkyIsland(StringArgumentType.getString(context, "name")), context.getSource().getPlayer());
                                    return 0;
                                })))
                .then(CommandManager.literal("visit")
                        .then(CommandManager.argument("name", StringArgumentType.string())
                                .executes(context -> {
                                    SkyIslandManager.visitIsland(SkyIslandUtils.getSkyIsland(StringArgumentType.getString(context, "name")), context.getSource().getPlayer());
                                    return 0;
                                })))
                .then(CommandManager.literal("home")
                        .executes(context -> {
                            SkyIslandManager.teleportHome(context.getSource().getPlayer());
                            return 0;
                        }))
        ));
    }
}
