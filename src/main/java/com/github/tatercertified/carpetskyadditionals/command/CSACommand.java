package com.github.tatercertified.carpetskyadditionals.command;

import com.github.tatercertified.carpetskyadditionals.gui.SkyIslandGUI;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;

public class CSACommand {
    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(CommandManager.literal("islands")
                .executes(context -> {
                    new SkyIslandGUI(context.getSource().getPlayer(), false);
                    return 0;
                })));
    }
}
