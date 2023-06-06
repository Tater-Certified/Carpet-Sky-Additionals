package com.github.tatercertified.carpetskyadditionals.command;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;

public class CSAAdminCommand {

    public static void registerAdminCommand() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(CommandManager.literal("islands-admin")
                .requires(source -> source.hasPermissionLevel(4))
                .executes(context -> {
                    new SkyIslandGUI(context.getSource().getPlayer(), true);
                    return 0;
                })
        ));
    }
}
